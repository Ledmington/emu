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
package com.ledmington.view;

import java.io.File;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class ELFViewer extends Stage {

	private final ELFView view;

	public ELFViewer(final double width, final double height) {
		final BorderPane mainPane = new BorderPane();

		this.view = new ELFView();

		final FlowPane topPane = new FlowPane();
		final Button load = new Button();
		load.setText("Load");
		load.setOnAction(e -> {
			final FileChooser fc = new FileChooser();
			fc.setTitle("Select an ELF file to be loaded");
			fc.getExtensionFilters()
					.addAll(
							new FileChooser.ExtensionFilter("ELF files", "*.elf", "*.bin", "*.out"),
							new FileChooser.ExtensionFilter("Shared objects", "*.o"),
							new FileChooser.ExtensionFilter("Shared libraries", "*.so"),
							new FileChooser.ExtensionFilter("Static libraries", "*.a"),
							new FileChooser.ExtensionFilter("All files", "*"));
			final File selectedFile = fc.showOpenDialog(this);
			if (selectedFile == null) {
				return;
			}

			Platform.runLater(() -> this.view.loadFile(selectedFile));
		});

		final Button settings = new Button();
		settings.setText("Settings");
		settings.setOnAction(e -> new SettingsWindow());

		topPane.setHgap(4);
		topPane.setPadding(new Insets(5));
		topPane.setPrefWrapLength(300);
		topPane.getChildren().addAll(load, settings);

		mainPane.setTop(topPane);
		mainPane.setCenter(this.view);
		final Scene scene = new Scene(mainPane);

		this.setScene(scene);
		this.setTitle("Emu - ELF Viewer");
		this.setWidth(width);
		this.setHeight(height);
		this.centerOnScreen();
		this.show();
	}
}
