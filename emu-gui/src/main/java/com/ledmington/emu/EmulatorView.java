/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.emu;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionDecoderV1;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.exc.ReservedOpcode;
import com.ledmington.cpu.x86.exc.UnknownOpcode;
import com.ledmington.mem.MemoryController;

public final class EmulatorView extends Stage {

	private X86Emulator cpu;
	private X86RegisterFile regFile;
	private MemoryController mem;
	private InstructionDecoder decoder;
	private final TextArea codeArea;
	private final TextArea memoryArea;
	private final Map<Register64, Label> regLabels = new HashMap<>();

	public EmulatorView() {
		final BorderPane mainPane = new BorderPane();

		final FlowPane topPane = new FlowPane();
		final Button load = new Button("Load");
		load.setOnAction(e -> {
			final FileChooser fc = new FileChooser();
			fc.setTitle("Select an ELF file to be loaded");
			fc.getExtensionFilters()
					.addAll(
							new FileChooser.ExtensionFilter("ELF files", "*.elf", "*.bin", "*.out"),
							new FileChooser.ExtensionFilter("All files", "*.*"));
			final File selectedFile = fc.showOpenDialog(this);
			if (selectedFile != null) {
				this.loadFile(selectedFile);
			}
		});

		final Button settings = new Button("Settings");
		settings.setOnAction(e -> new SettingsWindow());

		topPane.setHgap(4);
		topPane.setPadding(new Insets(5));
		topPane.setPrefWrapLength(300);
		topPane.getChildren().addAll(load, settings);

		mainPane.setTop(topPane);

		final BorderPane centerPane = new BorderPane();

		final GridPane registerPane = new GridPane();
		registerPane.add(new Label("Registers"), 0, 0);
		{
			int row = 1;
			for (final Register64 r : Register64.values()) {
				registerPane.add(LabelFactory.getDefaultLabel(r.name()), 0, row);
				final Label rl = LabelFactory.getDefaultLabel("0x" + "0".repeat(16));
				registerPane.add(rl, 1, row);
				regLabels.put(r, rl);
				row++;
			}
		}
		centerPane.setLeft(registerPane);

		final BorderPane codePane = new BorderPane();
		codePane.setTop(new Label("Code"));
		this.codeArea = new TextArea();
		this.codeArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		codePane.setCenter(this.codeArea);
		centerPane.setCenter(codePane);

		final BorderPane memoryPane = new BorderPane();
		memoryPane.setTop(new Label("Memory"));
		this.memoryArea = new TextArea();
		this.memoryArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		memoryPane.setCenter(this.memoryArea);
		centerPane.setRight(memoryPane);

		centerPane.setPadding(new Insets(5));
		mainPane.setCenter(centerPane);

		final FlowPane bottomPane = new FlowPane();
		final int maxIconSize = 20;
		final Button step = new Button();
		final ImageView imageStep =
				new ImageView(new Image(getResourceStream("icons/step.png"), maxIconSize, maxIconSize, true, true));
		imageStep.setPreserveRatio(true);
		imageStep.setSmooth(true);
		imageStep.setCache(true);
		step.setGraphic(imageStep);
		step.setOnMouseClicked(e -> this.cpu.executeOne());
		step.setTooltip(new Tooltip("Step"));
		final Button run = new Button();
		final ImageView imageRun =
				new ImageView(new Image(getResourceStream("icons/run.png"), maxIconSize, maxIconSize, true, true));
		imageRun.setPreserveRatio(true);
		imageRun.setSmooth(true);
		imageRun.setCache(true);
		run.setGraphic(imageRun);
		run.setOnMouseClicked(e -> this.cpu.execute());
		run.setTooltip(new Tooltip("Run"));
		bottomPane.setHgap(4);
		bottomPane.setPadding(new Insets(5));
		bottomPane.setPrefWrapLength(300);
		bottomPane.getChildren().addAll(step, run);

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
		System.out.printf("Loading file '%s'\n", file.toString());
		this.mem = new MemoryController(EmulatorConstants.getMemoryInitializer(), false);
		this.regFile = new X86RegisterFile();
		this.cpu = new X86Emulator(regFile, mem);
		this.decoder = new InstructionDecoderV1(new InstructionFetcher(mem, regFile));

		// TODO: implement this
		final String[] commandLineArguments = new String[0];

		/*try {
			ELFLoader.load(
					ELFReader.read(Files.readAllBytes(file.toPath())),
					cpu,
					mem,
					commandLineArguments,
					EmulatorConstants.getbaseAddress(),
					EmulatorConstants.getStackSize(),
					this.regFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/

		updateRegisters();
		updateCode();
		updateMemory();
	}

	private void updateRegisters() {
		for (final Register64 r : Register64.values()) {
			this.regLabels.get(r).setText(String.format("0x%016x", this.regFile.get(r)));
		}
	}

	private void updateCode() {
		final StringBuilder sb = new StringBuilder();
		final int n = AppConstants.getMaxCodeInstructions();
		for (int i = 0; i < n; i++) {
			final long rip = this.regFile.get(Register64.RIP);
			String inst;
			try {
				inst = this.decoder.decode().toIntelSyntax();
			} catch (final UnknownOpcode | ReservedOpcode | IllegalArgumentException e) {
				inst = String.format(".byte 0x%02x", this.mem.readCode(rip));
			}
			sb.append(" 0x")
					.append(String.format("%016x", rip))
					.append(" : ")
					.append(inst)
					.append('\n');
		}
		this.codeArea.setText(sb.toString());
	}

	private void updateMemory() {
		final long baseAddress = 0L;
		final StringBuilder sb = new StringBuilder();
		final int n = AppConstants.getMaxMemoryLines();
		final int k = AppConstants.getMemoryBytesPerLine();
		for (int i = 0; i < n * k; i++) {
			final long address = baseAddress + (long) i * k;
			if (i % k == 0) {
				sb.append(" 0x").append(String.format("%016x", address)).append(" : ");
			}
			if (this.mem.isInitialized(address)) {
				sb.append(String.format(" %02x ", this.mem.read(address)));
			} else {
				sb.append(" xx ");
			}
			if (i % k == k - 1) {
				sb.append('\n');
			}
		}
		this.memoryArea.setText(sb.toString());
	}
}
