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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.cpu.InstructionDecoder;
import com.ledmington.cpu.InstructionEncoder;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.FileHeader;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.sym.SymbolTableEntry;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
import com.ledmington.elf.section.sym.SymbolTableSection;
import com.ledmington.emu.ELFLoader;
import com.ledmington.emu.Emu;
import com.ledmington.emu.EmulatorConstants;
import com.ledmington.emu.ExecutionContext;
import com.ledmington.emu.ImmutableRegisterFile;
import com.ledmington.emu.RFlags;
import com.ledmington.emu.RegisterFile;
import com.ledmington.emu.X86Cpu;
import com.ledmington.emu.X86RegisterFile;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.PagedMemory;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.TerminalUtils;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

@SuppressWarnings({
	"PMD.CyclomaticComplexity",
	"PMD.AvoidInstantiatingObjectsInLoops",
	"PMD.AvoidDuplicateLiterals",
	"PMD.CouplingBetweenObjects"
})
public final class EmuDB {

	private static final PrintWriter out = System.console() == null
			? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
			: System.console().writer();

	private Path filepath = null;
	private String[] savedArguments = null;
	private ExecutionContext context = null;
	private ELF currentFile = null; // TODO: should we put this into ExecutionContext, too?
	private ELFLoader loader = null; // TODO: should we put this into ExecutionContext, too?
	private final List<Breakpoint> breakpoints = new ArrayList<>();
	private long lastBreakpointAddress = -1L; // last breakpoint we hit

	private final Map<String, Command> commands = Map.ofEntries(
			Map.entry("quit", new Command("Terminates the debugger", ignored -> System.exit(0))),
			Map.entry("help", new Command("Prints this message", ignored -> printHelp())),
			Map.entry("load", new Command("Loads a file", this::loadFile)),
			Map.entry("run", new Command("Executes a file", this::runFile)),
			Map.entry(
					"restart",
					new Command("Stop current execution and starts from the beginning", ignored -> restart())),
			Map.entry("mem", new Command("Shows the content of the memory", this::showMemory)),
			Map.entry("regs", new Command("Shows the content of the register file", ignored -> showRegisters())),
			Map.entry("asm", new Command("Shows the assembly at the current position", this::showAsm)),
			Map.entry("break", new Command("Sets up a breakpoint at the given function", this::setBreakpoint)),
			Map.entry("where", new Command("Shows the stack", ignored -> showStack())),
			Map.entry("step", new Command("Executes a single instruction", ignored -> step())));

	public EmuDB() {}

	private void printHelp() {
		for (final Map.Entry<String, Command> e : commands.entrySet()) {
			out.printf(
					"%s%s%s -- %s%n",
					TerminalUtils.ANSI_BOLD,
					e.getKey(),
					TerminalUtils.ANSI_RESET,
					e.getValue().description());
		}
	}

	private void showStack() {
		final int maxStackTraceDepth = 100; // TODO: is this a sane value?

		final long baseStackAddress = EmulatorConstants.getBaseStackAddress();
		final long stackSize = EmulatorConstants.getStackSize();
		final long baseStackValue = EmulatorConstants.getBaseStackValue();
		final long stackTop = ELFLoader.alignAddress(baseStackAddress); // highest address (initial RSP)
		final long stackBottom = stackTop - stackSize; // lowest address (stack limit)

		// The first stack frame to be printed is always the one at [RIP]
		final long rip = this.context.cpu().getRegisters().get(Register64.RIP);
		final Position initialPos = findFunctionName(rip);
		out.printf(
				"#%-2d %s0x%016x%s in %s%s%s ()%n",
				0,
				TerminalUtils.ANSI_BLUE,
				rip,
				TerminalUtils.ANSI_RESET,
				TerminalUtils.ANSI_YELLOW,
				initialPos.functionName(),
				TerminalUtils.ANSI_RESET);

		long rbp = this.context.cpu().getRegisters().get(Register64.RBP);
		int stackLevel = 1;
		for (;
				rbp != 0L && rbp != baseStackValue && rbp != baseStackAddress && stackLevel < maxStackTraceDepth;
				stackLevel++) {
			final Position pos = findFunctionName(rbp);
			out.printf(
					"#%-2d %s0x%016x%s in %s%s%s ()%n",
					stackLevel,
					TerminalUtils.ANSI_BLUE,
					rbp,
					TerminalUtils.ANSI_RESET,
					TerminalUtils.ANSI_YELLOW,
					pos.functionName(),
					TerminalUtils.ANSI_RESET);

			final long nextRbp = this.context.memory().read8(rbp);
			// TODO: should we check if nextRbp is aligned?
			if (nextRbp <= rbp || nextRbp > stackTop || nextRbp < stackBottom) {
				// out.println("<Invalid frame or corrupted stack>");
				break;
			}
			rbp = nextRbp;
		}

		if (stackLevel >= maxStackTraceDepth) {
			out.println("<Max stack size reached>");
		}
	}

	private record Position(String functionName, long offset) {
		Position {
			Objects.requireNonNull(functionName);
			if (functionName.isBlank()) {
				throw new IllegalArgumentException("Empty function name.");
			}
			final boolean negative = offset < 0L;
			if (negative) {
				throw new IllegalArgumentException("Negative offset.");
			}
		}

		@Override
		public String toString() {
			return (offset == 0L) ? functionName : (functionName + "+" + offset);
		}
	}

	private Position findFunctionName(final long ip) {
		final Optional<Section> symbolTable = this.currentFile.getSectionByName(".symtab");
		final Optional<Section> stringTable = this.currentFile.getSectionByName(".strtab");
		final boolean hasDebugInfo = symbolTable.isPresent() && stringTable.isPresent();

		if (hasDebugInfo) {
			final SymbolTableSection symtab = (SymbolTableSection) symbolTable.orElseThrow();
			final StringTableSection strtab = (StringTableSection) stringTable.orElseThrow();

			final Optional<SymbolTableEntry> closestMatch = IntStream.range(0, symtab.getSymbolTableLength())
					.mapToObj(symtab::getSymbolTableEntry)
					// only functions
					.filter(e -> e.info().getType() == SymbolTableEntryType.STT_FUNC)
					// only functions that are "before" or exactly on the instruction pointer
					.filter(e -> e.value() <= ip)
					// take the closest to the instruction pointer
					.min(Comparator.comparingLong(e -> ip - e.value()));
			if (closestMatch.isPresent()) {
				final SymbolTableEntry e = closestMatch.orElseThrow();
				final String name = strtab.getString(e.nameOffset());
				final long distance = ip - e.value();
				return new Position(name, distance);
			}
		}

		return new Position("??", 0L);
	}

	private void printBreakpoint(final int breakpointIndex) {
		out.printf(
				"Breakpoint %,d at %s0x%x%s %s%s%s%n",
				breakpointIndex,
				TerminalUtils.ANSI_BLUE,
				breakpoints.get(breakpointIndex).address(),
				TerminalUtils.ANSI_RESET,
				TerminalUtils.ANSI_YELLOW,
				breakpoints.get(breakpointIndex).name(),
				TerminalUtils.ANSI_RESET);
	}

	private void setBreakpoint(final String... args) {
		if (hasNotLoadedFile()) {
			out.println("You have not loaded a file. Try 'run'.");
			return;
		}
		if (args.length == 0) {
			if (breakpoints.isEmpty()) {
				out.println("No breakpoints set up.");
			} else {
				for (int i = 0; i < breakpoints.size(); i++) {
					printBreakpoint(i);
				}
			}
			return;
		}

		final String functionName = args[0];
		final Optional<Section> symbolTable = this.currentFile.getSectionByName(".symtab");
		final Optional<Section> stringTable = this.currentFile.getSectionByName(".strtab");
		if (symbolTable.isEmpty() || stringTable.isEmpty()) {
			out.println("No debugging info present. Impossible to set up a breakpoint.");
			return;
		}

		final SymbolTableSection symtab = (SymbolTableSection) symbolTable.orElseThrow();
		final StringTableSection strtab = (StringTableSection) stringTable.orElseThrow();
		boolean found = false;
		for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
			final SymbolTableEntry e = symtab.getSymbolTableEntry(i);
			if (e.info().getType() != SymbolTableEntryType.STT_FUNC) {
				continue;
			}
			final String name = strtab.getString(e.nameOffset());
			if (name.equals(functionName)) {
				final Breakpoint b = new Breakpoint(e.value(), name);
				final List<Integer> sameBreakpoints = new ArrayList<>();
				for (int j = 0; j < breakpoints.size(); j++) {
					if (breakpoints.get(j).address() == b.address()) {
						sameBreakpoints.add(j);
					}
				}
				if (!sameBreakpoints.isEmpty()) {
					out.printf(
							"Note: breakpoint%s %s are also set at 0x%x '%s'%n",
							sameBreakpoints.size() == 1 ? "" : "s",
							(sameBreakpoints.size() > 1
											? IntStream.range(0, sameBreakpoints.size() - 1)
															.mapToObj(String::valueOf)
															.collect(Collectors.joining(", "))
													+ " and "
											: "")
									+ sameBreakpoints.getLast(),
							b.address(),
							b.name());
				}
				breakpoints.add(b);
				printBreakpoint(breakpoints.size() - 1);
				found = true;
				break;
			}
		}
		if (!found) {
			out.printf("Function '%s' not defined.%n", functionName);
		}
	}

	private void showAsm(final String... args) {
		if (hasNotLoadedFile()) {
			out.println("You have not loaded a file. Try 'run'.");
			return;
		}

		if (args.length == 0) {
			showAssemblyAt(this.context.cpu().getRegisters().get(Register64.RIP));
			return;
		}

		final Optional<Long> parsed = parseAddress(args[0]);
		if (parsed.isEmpty()) {
			out.printf(
					"'%s' is not a valid address, enter a 64-bit address in decimal or hexadecimal (prefixed with '0x').%n",
					args[0]);
			return;
		}
		final long address = parsed.orElseThrow();

		showAssemblyAt(address);
	}

	private void showAssemblyAt(final long address) {
		final int maxInstructions = 5;
		final ReadOnlyByteBuffer bb = new MemoryByteBuffer(address, context.memory());

		for (int i = 0; i < maxInstructions; i++) {
			final long pos = bb.getPosition();
			String str;
			try {
				final Instruction decoded = InstructionDecoder.fromHex(bb);
				str = InstructionEncoder.toIntelSyntax(decoded);
			} catch (final DecodingException e) {
				str = "<unknown: " + e.getMessage() + ">";
			}
			out.printf("0x%016x : %s%n", pos, str);
		}
	}

	private void showRegisters() {
		if (hasNotLoadedFile()) {
			out.println("You have not loaded a file. Try 'run'.");
			return;
		}

		final String registerFormatString = "%-14s 0x%-16x  %s%n";

		final ImmutableRegisterFile rf = this.context.cpu().getRegisters();
		for (final Register64 r : Register64.values()) {
			final long regValue = rf.get(r);
			final String interpreted;
			if (r == Register64.RBP || r == Register64.RSP) {
				interpreted = String.format("0x%x", regValue);
			} else if (r == Register64.RIP) {
				interpreted = String.format("0x%x <%s>", regValue, findFunctionName(regValue));
			} else {
				interpreted = String.valueOf(regValue);
			}
			out.printf(registerFormatString, r.toIntelSyntax(), regValue, interpreted);
		}
		out.printf(
				registerFormatString,
				"eflags",
				RFlags.defaultValue()
						| Arrays.stream(RFlags.values())
								.filter(rf::isSet)
								.mapToLong(x -> 1L << x.bit())
								.reduce(0L, (a, b) -> a | b),
				"[ "
						+ Arrays.stream(RFlags.values())
								.sorted(Comparator.comparing(RFlags::bit))
								.filter(rf::isSet)
								.map(RFlags::getSymbol)
								.collect(Collectors.joining(" "))
						+ " ]");

		for (final SegmentRegister sr : SegmentRegister.values()) {
			out.printf(registerFormatString, sr.toIntelSyntax(), rf.get(sr), rf.get(sr));
		}
		out.printf(registerFormatString, "fs_base", 0, 0);
		out.printf(registerFormatString, "gs_base", 0, 0);
	}

	private void showMemory(final String... args) {
		if (hasNotLoadedFile()) {
			out.println("You have not loaded a file. Try 'run'.");
			return;
		}
		if (args.length == 0) {
			out.println("Command 'mem' expects an address.");
			return;
		}

		final Optional<Long> parsed = parseAddress(args[0]);
		if (parsed.isEmpty()) {
			out.printf(
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
		final String addressFormatString =
				"0x%0" + String.format("%x", actualStartAddress + numTotalBytes).length() + "x";
		for (int i = 0; i < numTotalBytes; i++) {
			final long currentAddress = actualStartAddress + i;
			if (i % numBytesPerRow == 0) {
				out.printf(
						"%s" + addressFormatString + "%s:",
						TerminalUtils.ANSI_BLUE,
						currentAddress,
						TerminalUtils.ANSI_RESET);
			}
			final String s = mem.isInitialized(currentAddress) ? String.format("%02x", mem.read(currentAddress)) : "xx";
			out.printf(currentAddress == address ? "[" + s + "]" : " " + s + " ");
			if (i % numBytesPerRow == numBytesPerRow - 1) {
				out.println();
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

	private void step() {
		if (hasNotLoadedFile()) {
			out.println("No instruction to execute.");
			return;
		}

		executeOneInstruction();

		final long here = this.context.cpu().getRegisters().get(Register64.RIP);
		out.printf("%s0x%016x%s%n", TerminalUtils.ANSI_BLUE, here, TerminalUtils.ANSI_RESET);
	}

	private boolean executeOneInstruction() {
		final Optional<Integer> breakpointIndex = checkBreakpoints();
		final boolean hasHitABreakpoint = breakpointIndex.isPresent();
		if (hasHitABreakpoint) {
			final Breakpoint b = this.breakpoints.get(breakpointIndex.orElseThrow());
			// set interrupt flag when hitting a breakpoint
			((RegisterFile) this.context.cpu().getRegisters()).set(RFlags.INTERRUPT_ENABLE, true);
			out.printf(
					"Breakpoint %,d, %s0x%016x%s in %s%s%s ()%n",
					breakpointIndex.orElseThrow(),
					TerminalUtils.ANSI_BLUE,
					b.address(),
					TerminalUtils.ANSI_RESET,
					TerminalUtils.ANSI_YELLOW,
					b.name(),
					TerminalUtils.ANSI_RESET);
		} else {
			this.context.cpu().turnOn();
			this.context.cpu().executeOne();
		}

		return hasHitABreakpoint;
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private void runFile(final String... args) {
		if (hasNotLoadedFile()) {
			if (args.length == 0) {
				out.println("Command 'run' expects the path of a file.");
				return;
			}

			loadFile(args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		out.printf("Starting program: %s%s%s", TerminalUtils.ANSI_GREEN, this.filepath, TerminalUtils.ANSI_RESET);
		for (final String arg : this.savedArguments) {
			out.printf(" %s", arg);
		}
		out.println();
		out.println();

		try {
			this.context.cpu().turnOn();
			while (true) {
				final boolean hasHitABreakpoint = executeOneInstruction();
				if (hasHitABreakpoint) {
					break;
				}
			}
		} catch (final RuntimeException e) {
			out.println(e);
		}
	}

	private Optional<Integer> checkBreakpoints() {
		final long here = this.context.cpu().getRegisters().get(Register64.RIP);
		// TODO: can be optimized into a HashMap
		for (int i = 0; i < this.breakpoints.size(); i++) {
			final long breakpointAddress =
					EmulatorConstants.getBaseAddress() + this.breakpoints.get(i).address();
			if (here == breakpointAddress && here != lastBreakpointAddress) {
				lastBreakpointAddress = breakpointAddress;
				return Optional.of(i);
			}
		}
		lastBreakpointAddress = -1L;
		return Optional.empty();
	}

	private ExecutionContext createDefaultExecutionContext() {
		final Memory rawMem = new PagedMemory(MemoryInitializer.random());
		// Proper memory controller for execution (checks permissions)
		final MemoryController mc = new MemoryController(
				rawMem,
				EmulatorConstants.shouldBreakOnWrongPermissions(),
				EmulatorConstants.shouldBreakWhenReadingUninitializedMemory());
		// Memory controller used directly by the debugger (does not check permissions)
		final MemoryController mem = new MemoryController(rawMem, false, false);
		final X86Cpu cpu = new X86Cpu(mc, new X86RegisterFile(), EmulatorConstants.shouldCheckInstruction());

		this.loader = new ELFLoader(cpu, mc);

		return new ExecutionContext(cpu, mem);
	}

	private void restart() {
		if (hasNotLoadedFile()) {
			out.println("No execution to restart. Try 'run' first.");
			return;
		}

		this.breakpoints.clear();

		this.loadFile(this.filepath.toString(), this.savedArguments);
	}

	private void loadFile(final String... args) {
		if (args.length == 0) {
			out.println("Command 'load' expects the path of a file.");
			return;
		}

		loadFile(args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	private void loadFile(final String filename, final String... commandLineArguments) {
		this.filepath = Path.of(filename).normalize().toAbsolutePath();
		this.savedArguments = commandLineArguments;
		this.currentFile = ELFParser.parse(filename);

		this.context = createDefaultExecutionContext();

		final Emu emu = new Emu(this.context, this.loader);
		emu.load(filename, commandLineArguments);

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

		int[] prev = new int[n + 1];
		int[] curr = new int[n + 1];

		for (int i = 0; i <= n; i++) {
			prev[i] = i;
		}

		for (int i = 1; i <= m; i++) {
			curr[0] = i;

			for (int j = 1; j <= n; j++) {
				final int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
				curr[j] = Math.min(Math.min(prev[j] + 1, curr[j - 1] + 1), prev[j - 1] + cost);
			}

			final int[] temp = prev;
			prev = curr;
			curr = temp;
		}

		return prev[n];
	}

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	private void runInteractively() {
		try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
			final LineReader reader =
					LineReaderBuilder.builder().terminal(terminal).build();
			while (true) {
				final String line;
				try {
					line = reader.readLine("(emudb) ").strip().toLowerCase(Locale.US);
				} catch (final UserInterruptException | EndOfFileException e) {
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
					out.printf("Command '%s' not found. Try 'help'.%n", command);
					final List<String> similarCommands = commands.keySet().stream()
							.filter(c -> levenshteinDistance(c, command) <= 2)
							.toList();
					if (!similarCommands.isEmpty()) {
						out.printf(
								"Maybe you meant one of these: %s.%n",
								similarCommands.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")));
					}
				}

				out.flush();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void run(final String... args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			out.println();
			out.flush();
		}));

		if (args.length > 0) {
			this.loadFile(args);
		}
		runInteractively();

		out.println();
		out.flush();
	}
}
