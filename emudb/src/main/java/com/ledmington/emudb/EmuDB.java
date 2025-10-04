/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.emudb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionEncoder;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.elf.ELF;
import com.ledmington.elf.FileHeader;
import com.ledmington.emu.ELFLoader;
import com.ledmington.emu.Emu;
import com.ledmington.emu.EmulatorConstants;
import com.ledmington.emu.ExecutionContext;
import com.ledmington.emu.ImmutableRegisterFile;
import com.ledmington.emu.RFlags;
import com.ledmington.emu.X86Cpu;
import com.ledmington.emu.X86RegisterFile;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.mem.exc.IllegalMemoryAccessException;
import com.ledmington.utils.ReadOnlyByteBuffer;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public final class EmuDB {

	private Emu emu = null;
	private ExecutionContext context = null;
	private ELF currentFile = null; // TODO: should we put this into ExecutionContext, too?
	private ELFLoader loader = null; // TODO: should we put this into ExecutionContext, too?

	private final Map<String, Command> commands = Map.of(
			"quit",
			new Command("Terminates the debugger", ignored -> System.exit(0)),
			"help",
			new Command("Prints this message", ignored -> printHelp()),
			"load",
			new Command("Loads a file", this::loadFile),
			"run",
			new Command("Executes a file", this::runFile),
			"restart",
			new Command("Stop current execution and starts from the beginning", ignored -> restart()),
			"mem",
			new Command("Shows the content of the memory", this::showMemory),
			"regs",
			new Command("Shows the content of the register file", ignored -> showRegisters()),
			"asm",
			new Command("Show the assembly at the current position", this::showAsm));

	public EmuDB() {}

	private void printHelp() {
		for (final Map.Entry<String, Command> e : commands.entrySet()) {
			System.out.printf(
					"\u001b[1m%s\u001b[0m -- %s%n", e.getKey(), e.getValue().description());
		}
	}

	private void showAsm(final String[] args) {
		if (this.currentFile == null) {
			System.out.println("You have not loaded a file. Try 'run'.");
			return;
		}

		if (args.length == 0) {
			showAssemblyAt(this.context.cpu().getRegisters().get(Register64.RIP));
			return;
		}

		final Optional<Long> parsed = parseAddress(args[0]);
		if (parsed.isEmpty()) {
			System.out.printf(
					"'%s' is not a valid address, enter a 64-bit address in decimal or hexadecimal (prefixed with '0x').%n",
					args[0]);
			return;
		}
		final long address = parsed.orElseThrow();

		showAssemblyAt(address);
	}

	private void showAssemblyAt(final long address) {
		final int maxInstructions = 5;
		final ReadOnlyByteBuffer bb = new ReadOnlyByteBuffer() {

			private long position = address;

			@Override
			public boolean isLittleEndian() {
				return true;
			}

			@Override
			public void setEndianness(final boolean isLittleEndian) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setAlignment(final long newAlignment) {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getAlignment() {
				return 1L;
			}

			@Override
			public void setPosition(final long newPosition) {
				this.position = newPosition;
			}

			@Override
			public long getPosition() {
				return this.position;
			}

			@Override
			public byte read() {
				return context.memory().read(position);
			}
		};
		for (int i = 0; i < maxInstructions; i++) {
			final long pos = bb.getPosition();
			final Instruction decoded = InstructionDecoder.fromHex(bb);
			System.out.printf("0x%016x : %s%n", pos, InstructionEncoder.toIntelSyntax(decoded));
		}
	}

	private void showRegisters() {
		if (hasNotLoadedFile()) {
			System.out.println("You have not loaded a file. Try 'run'.");
			return;
		}

		final ImmutableRegisterFile rf = this.context.cpu().getRegisters();

		for (final Register64 r : Register64.values()) {
			final long regValue = rf.get(r);
			System.out.printf(" %-3s : 0x%016x (%,d)%n", r.name(), regValue, regValue);
		}
		System.out.printf(
				" RFLAGS : %s%n",
				Arrays.stream(RFlags.values())
						.sorted(Comparator.comparing(RFlags::bit))
						.filter(rf::isSet)
						.map(x -> " " + x.getSymbol())
						.toList());
	}

	private void showMemory(final String[] args) {
		if (hasNotLoadedFile()) {
			System.out.println("You have not loaded a file. Try 'run'.");
			return;
		}
		if (args.length == 0) {
			System.out.println("Command 'mem' expects an address.");
			return;
		}

		final Optional<Long> parsed = parseAddress(args[0]);
		if (parsed.isEmpty()) {
			System.out.printf(
					"'%s' is not a valid address, enter a 64-bit address in decimal or hexadecimal (prefixed with '0x').%n",
					args[0]);
			return;
		}
		final long address = parsed.orElseThrow();

		final Memory mem = context.memory();

		final int numBytesPerRow = 16;
		final int numRowsBefore = 5;
		final int numRowsAfter = 5;
		final long actualStartAddress = (address / numBytesPerRow - numRowsBefore) * numBytesPerRow;
		final long numTotalBytes = (numRowsBefore + numRowsAfter + 1) * numBytesPerRow;
		for (int i = 0; i < numTotalBytes; i++) {
			final long currentAddress = actualStartAddress + i;
			if (i % numBytesPerRow == 0) {
				System.out.printf("0x%016x:", currentAddress);
			}
			final String s = mem.isInitialized(currentAddress) ? String.format("%02x", mem.read(currentAddress)) : "xx";
			System.out.printf(currentAddress == address ? "[" + s + "]" : " " + s + " ");
			if (i % numBytesPerRow == numBytesPerRow - 1) {
				System.out.println();
			}
		}
	}

	private boolean hasNotLoadedFile() {
		return this.context == null;
	}

	private Optional<Long> parseAddress(final String arg) {
		try {
			if (arg.startsWith("0x")) {
				return Optional.of(Long.parseLong(arg.substring(2), 16));
			} else {
				return Optional.of(Long.parseLong(arg, 10));
			}
		} catch (final NumberFormatException e) {
			return Optional.empty();
		}
	}

	private void runFile(final String[] args) {
		if (hasNotLoadedFile()) {
			if (args.length == 0) {
				System.out.println("Command 'run' expects the path of a file.");
				return;
			}

			final Path filepath = Path.of(args[0]).normalize().toAbsolutePath();
			final String[] arguments = Arrays.copyOfRange(args, 1, args.length);
			loadFile(String.valueOf(filepath), arguments);
		}

		try {
			this.emu.run();
		} catch (final IllegalMemoryAccessException e) {
			System.out.println(e);
		}
	}

	private ExecutionContext createDefaultExecutionContext() {
		final RandomAccessMemory ram = new RandomAccessMemory(MemoryInitializer.random());
		// Proper memory controller for execution (checks permissions)
		final MemoryController mc = new MemoryController(
				ram,
				EmulatorConstants.shouldBreakOnWrongPermissions(),
				EmulatorConstants.shouldBreakWhenReadingUninitializedMemory());
		// Memory controller used directly by the debugger (does not check permissions)
		final MemoryController mem = new MemoryController(ram, false, false);
		final X86Cpu cpu = new X86Cpu(mc, new X86RegisterFile(), EmulatorConstants.shouldCheckInstruction());

		this.loader = new ELFLoader(cpu, mc);

		return new ExecutionContext(cpu, mem);
	}

	private void restart() {
		if (hasNotLoadedFile()) {
			System.out.println("No execution to restart. Try 'run' first.");
			return;
		}

		this.context = createDefaultExecutionContext();
	}

	private void loadFile(final String[] args) {
		if (args.length == 0) {
			System.out.println("Command 'load' expects the path of a file.");
			return;
		}

		loadFile(args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	private void loadFile(final String filepath, final String[] arguments) {
		this.emu = new Emu();
		this.emu.load(filepath, arguments);

		this.context = createDefaultExecutionContext();

		loader.load(
				this.currentFile,
				arguments,
				EmulatorConstants.getBaseAddress(),
				EmulatorConstants.getBaseStackAddress(),
				EmulatorConstants.getStackSize(),
				EmulatorConstants.getBaseStackValue());

		final FileHeader fh = this.currentFile.getFileHeader();
		this.context.cpu().setInstructionPointer(EmulatorConstants.getBaseAddress() + fh.entryPointVirtualAddress());
	}

	private int levenshteinDistance(final String a, final String b) {
		final int m = a.length();
		final int n = b.length();

		if (m == 0) {
			return n;
		}
		if (n == 0) {
			return m;
		}

		final int[][] dp = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		// Fill the matrix
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				final int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
				dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
			}
		}

		return dp[m][n];
	}

	private void runInteractively() {
		final Terminal terminal;
		try {
			terminal = TerminalBuilder.builder().system(true).build();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		final LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
		while (true) {
			final String line;
			try {
				line = reader.readLine("(emudb) ").strip().toLowerCase(Locale.US);
			} catch (final UserInterruptException e) {
				break;
			}
			if (line.isBlank()) {
				continue;
			}

			final String[] splitted = line.split(" +");
			final String command = splitted[0];
			final String[] commandArguments = Arrays.copyOfRange(splitted, 1, splitted.length);
			if (commands.containsKey(command)) {
				commands.get(command).command().accept(commandArguments);
			} else {
				System.out.printf("Command '%s' not found. Try 'help'.%n", command);
				final List<String> similarCommands = commands.keySet().stream()
						.filter(c -> levenshteinDistance(c, command) == 1)
						.toList();
				if (!similarCommands.isEmpty()) {
					System.out.printf(
							"Maybe you meant one of these: %s.%n",
							similarCommands.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")));
				}
			}
		}
	}

	public void run(final String[] args) {
		if (args.length > 0) {
			this.loadFile(args);
		}
		runInteractively();
	}
}
