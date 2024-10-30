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

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import com.ledmington.utils.MiniLogger;

public final class Emu {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");

	public Emu(final Stage stage) {
		logger.info("Emu is running on:");
		logger.info(" - %s %s", AppConstants.OSName, AppConstants.OSVersion);
		logger.info(" - Java %s", AppConstants.javaVersion);
		logger.info(" - JVM %s", AppConstants.jvmVersion);
		logger.info(" - JavaFX %s", AppConstants.javafxVersion);

		final BorderPane bPane = new BorderPane();
		final Scene scene = new Scene(bPane);

		final FlowPane topPane = new FlowPane();
		{
			topPane.setHgap(4);
			topPane.setPadding(new Insets(5));
			topPane.setPrefWrapLength(300);
			final Button elfViewerButton = new Button("ELF Viewer");
			elfViewerButton.setOnMouseClicked(e -> new ELFViewer(stage.getWidth(), stage.getHeight()).show());
			topPane.getChildren().add(elfViewerButton);
			final Button emulatorViewButton = new Button("Emulator");
			emulatorViewButton.setOnMouseClicked(e -> new EmulatorView().show());
			topPane.getChildren().add(emulatorViewButton);
			final Button settingsButton = new Button("Settings");
			settingsButton.setOnMouseClicked(e -> new SettingsWindow().show());
			topPane.getChildren().add(settingsButton);
		}
		bPane.setTop(topPane);

		final BorderPane mainPane = new BorderPane();
		{
			mainPane.setCenter(
					new Label(
							String.join(
									"\n",
									"Welcome to Emu, a processor emulator made by Filippo Barbari (filippo.barbari@gmail.com).",
									"",
									"If you happen to find any bugs, please report them at https://github.com/Ledmington/emu/issues.")));
		}
		bPane.setCenter(mainPane);

		final FlowPane bottomPane = new FlowPane();
		{
			bottomPane.setHgap(4);
			bottomPane.setPadding(new Insets(5));
			bottomPane.setPrefWrapLength(300);
			final Label memoryUsageLabel = new Label();
			bottomPane.getChildren().add(memoryUsageLabel);
			final AnimationTimer timer = new AnimationTimer() {

				private long lastTime = 0L;

				@Override
				public void handle(final long now) {
					// update the label every second
					final long oneSecondInNanoseconds = 1_000_000_000L;
					if ((now - lastTime) < oneSecondInNanoseconds) {
						return;
					}

					final long totalMemory = Runtime.getRuntime().totalMemory();
					final long memoryUsed = totalMemory - Runtime.getRuntime().freeMemory();

					memoryUsageLabel.setText(
							String.format("Java heap: %d / %d MB", memoryUsed / 1_000_000L, totalMemory / 1_000_000L));
					lastTime = now;
				}
			};
			timer.start();
		}
		bPane.setBottom(bottomPane);

		stage.setTitle("Emu - A processor emulator");
		stage.setScene(scene);
		stage.show();

		Platform.runLater(stage::requestFocus);
	}
}
