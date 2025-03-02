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
package com.ledmington.view;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.ledmington.cpu.x86.*;
import com.ledmington.cpu.x86.exc.ReservedOpcode;
import com.ledmington.cpu.x86.exc.UnknownOpcode;
import com.ledmington.cpu.x86.exc.UnrecognizedPrefix;
import com.ledmington.elf.ELFParser;
import com.ledmington.emu.ELFLoader;
import com.ledmington.emu.EmulatorConstants;
import com.ledmington.emu.ImmutableRegisterFile;
import com.ledmington.emu.InstructionFetcher;
import com.ledmington.emu.RFlags;
import com.ledmington.emu.RegisterFile;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.utils.MiniLogger;

public final class EmulatorView extends Stage {

	private static final MiniLogger logger = MiniLogger.getLogger("emu-gui");
	private static final int ADDRESS_BYTES = 8;

	// The CPU used to emulate
	private X86CpuAdapter cpu;
	// The memory controller used by the CPU
	private MemoryController mem;
	private final Button stepBtn = new Button();
	private final Button runBtn = new Button();
	private final TextArea codeArea = new TextArea();
	private final TextArea memoryArea = new TextArea();
	private final Map<Register64, Label> regLabels = new ConcurrentHashMap<>(Register64.values().length);
	private final Map<Register16, Label> segLabels = new ConcurrentHashMap<>(Register16.values().length);
	private final Label rflagsLabel;

	public EmulatorView() {
		super();

		final BorderPane mainPane = new BorderPane();

		final FlowPane topPane = new FlowPane();
		final Button load = new Button("Load");
		load.setOnAction(event -> {
			final FileChooser fc = new FileChooser();
			fc.setTitle("Select an ELF file to be loaded");
			fc.getExtensionFilters()
					.addAll(
							new FileChooser.ExtensionFilter("ELF files", "*.elf", "*.bin", "*.out"),
							new FileChooser.ExtensionFilter("All files", "*"));
			final File selectedFile = fc.showOpenDialog(this);
			if (selectedFile == null) {
				return;
			}

			this.stepBtn.setDisable(false);
			this.runBtn.setDisable(false);

			// TODO: find a better name for this thread
			final Thread th = new Thread(() -> this.loadFile(selectedFile), "file-loader");
			th.start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					if (th.isAlive()) {
						th.interrupt();
						th.join();
					}
				} catch (final InterruptedException e) {
					// Do we really need to re-throw the exception if we are killing the thread?
					throw new IllegalStateException(e);
				}
			}));
		});

		final Button settings = new Button("Settings");
		settings.setOnAction(e -> new SettingsWindow());

		topPane.setHgap(4);
		topPane.setPadding(new Insets(5));
		topPane.setPrefWrapLength(300);
		topPane.getChildren().addAll(load, settings);

		mainPane.setTop(topPane);

		final BorderPane centerPane = new BorderPane();

		final GridPane registerPane = new GridPane(10, 5);
		registerPane.add(LabelFactory.getDefaultLabel("Registers"), 0, 0);
		{
			int row = 1;
			for (final Register64 r : Register64.values()) {
				registerPane.add(LabelFactory.getDefaultLabel(r.name()), 0, row);
				final Label rl = LabelFactory.getDefaultLabel("0x" + "0".repeat(16));
				rl.setTooltip(TooltipFactory.getDefaultTooltip("Click to see the memory at [" + r.name() + "]"));
				final long maxMemoryBytes = AppConstants.getMaxMemoryLines() * AppConstants.getMemoryBytesPerLine();
				rl.setOnMouseClicked(e -> updateMemory(cpu.getRegisters().get(r) - maxMemoryBytes / 2L));
				registerPane.add(rl, 1, row);
				regLabels.put(r, rl);
				row++;
			}

			registerPane.add(LabelFactory.getDefaultLabel("RFLAGS"), 0, row);
			this.rflagsLabel = LabelFactory.getDefaultLabel("");
			registerPane.add(rflagsLabel, 1, row);
			row++;

			for (final Register16 s : new Register16[] {
				Register16.CS, Register16.DS, Register16.SS, Register16.ES, Register16.FS, Register16.GS
			}) {
				registerPane.add(LabelFactory.getDefaultLabel(s.name()), 0, row);
				final Label rl = LabelFactory.getDefaultLabel("0x" + "0".repeat(4));
				registerPane.add(rl, 1, row);
				segLabels.put(s, rl);
				row++;
			}
		}
		centerPane.setLeft(registerPane);

		final BorderPane codePane = new BorderPane();
		codePane.setTop(LabelFactory.getDefaultLabel("Code"));
		this.codeArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		this.codeArea.setEditable(false);
		codePane.setCenter(this.codeArea);
		centerPane.setCenter(codePane);

		final BorderPane memoryPane = new BorderPane();
		memoryPane.setTop(LabelFactory.getDefaultLabel("Memory"));
		this.memoryArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		this.memoryArea.setEditable(false);
		memoryPane.setCenter(this.memoryArea);
		centerPane.setRight(memoryPane);

		centerPane.setPadding(new Insets(5));
		mainPane.setCenter(centerPane);

		final FlowPane bottomPane = new FlowPane();
		final int maxIconSize = 20;

		final ImageView imageStep =
				new ImageView(new Image(getResourceStream("icons/step.png"), maxIconSize, maxIconSize, true, true));
		imageStep.setPreserveRatio(true);
		imageStep.setSmooth(true);
		imageStep.setCache(true);
		this.stepBtn.setDisable(true);
		this.stepBtn.setGraphic(imageStep);
		this.stepBtn.setOnMouseClicked(e -> {
			this.cpu.doExecuteOne();
			updateRegisters();
			updateCode();
			updateMemory(cpu.getRegisters().get(Register64.RIP));
		});
		this.stepBtn.setTooltip(new Tooltip("Step"));

		final ImageView imageRun =
				new ImageView(new Image(getResourceStream("icons/run.png"), maxIconSize, maxIconSize, true, true));
		imageRun.setPreserveRatio(true);
		imageRun.setSmooth(true);
		imageRun.setCache(true);
		this.runBtn.setDisable(true);
		this.runBtn.setGraphic(imageRun);
		this.runBtn.setOnMouseClicked(e -> {
			this.cpu.doExecute();
			updateRegisters();
			updateCode();
			updateMemory(cpu.getRegisters().get(Register64.RIP));
		});
		this.runBtn.setTooltip(new Tooltip("Run"));

		bottomPane.setHgap(4);
		bottomPane.setPadding(new Insets(5));
		bottomPane.setPrefWrapLength(300);
		bottomPane.getChildren().addAll(this.stepBtn, this.runBtn);

		mainPane.setBottom(bottomPane);

		final Scene scene = new Scene(mainPane);

		this.setScene(scene);
		this.setTitle("Emu - Emulator View");
		this.sizeToScene();
		this.centerOnScreen();
		this.show();
	}

	private InputStream getResourceStream(final String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}

	private void loadFile(final File file) {
		logger.info("Loading file '%s'", file.toString());
		this.mem = new MemoryController(new RandomAccessMemory(EmulatorConstants.getMemoryInitializer()), false, false);
		this.cpu = new X86CpuAdapter(mem);

		// TODO: implement this
		final String[] commandLineArguments = new String[0];

		ELFLoader.load(
				ELFParser.parse(file.toPath().toString()),
				cpu,
				mem,
				commandLineArguments,
				EmulatorConstants.getBaseAddress(),
				EmulatorConstants.getStackSize());

		updateRegisters();
		updateCode();
		updateMemory(cpu.getRegisters().get(Register64.RIP));
	}

	private void updateRegisters() {
		final ImmutableRegisterFile regFile = this.cpu.getRegisters();

		for (final Register64 r : Register64.values()) {
			this.regLabels.get(r).setText(String.format("0x%016x", regFile.get(r)));
		}

		for (final Register16 s : new Register16[] {
			Register16.CS, Register16.DS, Register16.SS, Register16.ES, Register16.FS, Register16.GS
		}) {
			this.segLabels.get(s).setText(String.format("0x%04x", regFile.get(s)));
		}

		this.rflagsLabel.setText("|"
				+ Arrays.stream(RFlags.values())
						.sorted(Comparator.comparingInt(RFlags::bit))
						.map(f -> regFile.isSet(f) ? f.getSymbol() : "")
						.collect(Collectors.joining("|"))
				+ "|");
	}

	private void updateCode() {
		final RegisterFile regFile = (RegisterFile) cpu.getRegisters();
		final StringBuilder sb = new StringBuilder();
		final int n = AppConstants.getMaxCodeInstructions();
		final long originalRIP = regFile.get(Register64.RIP);
		long rip = originalRIP;
		for (int i = 0; i < n; i++) {
			final long startRIP = rip;
			regFile.set(Register64.RIP, rip);
			sb.append("0x").append(String.format("%0" + (2 * ADDRESS_BYTES) + "x", rip));

			String instString;
			try {
				final Instruction inst = InstructionDecoder.fromHex(new InstructionFetcher(this.mem, regFile));
				instString = InstructionEncoder.toIntelSyntax(inst);
				rip = regFile.get(Register64.RIP);
			} catch (final UnknownOpcode | ReservedOpcode | UnrecognizedPrefix e) {
				instString = String.format(".byte 0x%02x", this.mem.readCode(rip));
				rip = startRIP + 1L;
			}
			sb.append(" : ").append(instString).append('\n');
		}
		this.codeArea.setText(sb.toString());
		regFile.set(Register64.RIP, originalRIP);
	}

	private void updateMemory(final long baseAddress) {
		final StringBuilder sb = new StringBuilder();
		final int n = AppConstants.getMaxMemoryLines();
		final int k = AppConstants.getMemoryBytesPerLine();
		for (int i = 0; i < n * k; i++) {
			final long address = baseAddress + (long) i * k;
			if (i % k == 0) {
				sb.append("0x")
						.append(String.format("%0" + (2 * ADDRESS_BYTES) + "x", address))
						.append(" :");
			}
			if (this.mem.isInitialized(address)) {
				sb.append(String.format(" %02x", this.mem.read(address)));
			} else {
				sb.append(" xx");
			}
			if (i % k == k - 1) {
				sb.append('\n');
			}
		}
		this.memoryArea.setText(sb.toString());
	}
}
