/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public final class SettingsWindow extends Stage {

	private final ComboBox<Label> fonts;
	private final Spinner<Integer> fontSize;
	private final Spinner<Integer> maxCodeInstructions;
	private final Spinner<Integer> maxMemoryLines;
	private final Spinner<Integer> memoryBytesPerLine;

	public SettingsWindow() {
		super();

		final BorderPane bPane = new BorderPane();
		final Scene scene = new Scene(bPane);

		final GridPane mainPane = new GridPane(10, 5);
		{
			mainPane.add(LabelFactory.getDefaultLabel("Font"), 0, 0);
			this.fonts = new ComboBox<>();
			for (final String fontFamily : Font.getFamilies()) {
				final Label lbl = getFontLabel(fontFamily);
				this.fonts.getItems().add(lbl);
				if (this.fonts.getValue() == null) {
					this.fonts.setValue(lbl);
				}
			}
			mainPane.add(this.fonts, 1, 0);
		}
		{
			mainPane.add(LabelFactory.getDefaultLabel("Font Size"), 0, 1);
			this.fontSize = new Spinner<>(1, 20, AppConstants.getDefaultFontSize());
			mainPane.add(this.fontSize, 1, 1);
		}
		{
			mainPane.add(LabelFactory.getDefaultLabel("Max emulator instructions"), 0, 2);
			this.maxCodeInstructions = new Spinner<>(1, 1000, AppConstants.getMaxCodeInstructions());
			mainPane.add(this.maxCodeInstructions, 1, 2);
		}
		{
			mainPane.add(LabelFactory.getDefaultLabel("Max emulator memory lines"), 0, 3);
			this.maxMemoryLines = new Spinner<>(1, 1000, AppConstants.getMaxMemoryLines());
			mainPane.add(this.maxMemoryLines, 1, 3);
		}
		{
			mainPane.add(LabelFactory.getDefaultLabel("Memory bytes per line"), 0, 4);
			this.memoryBytesPerLine = new Spinner<>(1, 1000, AppConstants.getMemoryBytesPerLine());
			mainPane.add(this.memoryBytesPerLine, 1, 4);
		}
		mainPane.setPadding(new Insets(5));
		bPane.setCenter(mainPane);

		final FlowPane bottomPane = new FlowPane();
		{
			final Button ok = new Button("OK");
			ok.setBackground(Background.fill(Color.LIGHTBLUE));
			ok.setOnMouseClicked(e -> {
				AppConstants.setDefaultMonospaceFont(this.fonts.getValue().getText());
				AppConstants.setDefaultFontSize(this.fontSize.getValue());
				AppConstants.setMaxCodeInstructions(this.maxCodeInstructions.getValue());
				AppConstants.setMaxMemoryLines(this.maxMemoryLines.getValue());
				AppConstants.setMemoryBytesPerLine(this.memoryBytesPerLine.getValue());
				this.close();
			});
			bottomPane.getChildren().add(ok);
			final Button cancel = new Button("Cancel");
			cancel.setOnMouseClicked(e -> this.close());
			bottomPane.getChildren().add(cancel);
		}
		bottomPane.setPadding(new Insets(5));
		bPane.setBottom(bottomPane);
		bPane.setPadding(new Insets(5));

		this.setTitle("Emu - Settings");
		this.setScene(scene);
		this.show();

		Platform.runLater(this::requestFocus);
	}

	private Label getFontLabel(final String fontFamily) {
		final Label lbl = new Label(fontFamily);
		lbl.setFont(Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, AppConstants.getDefaultFontSize()));
		lbl.setTextFill(Color.BLACK);
		return lbl;
	}
}
