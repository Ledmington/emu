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

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public final class SettingsWindow extends Stage {

    private final ComboBox<Label> fonts;

    public SettingsWindow() {
        final BorderPane bPane = new BorderPane();
        final Scene scene = new Scene(bPane);

        final FlowPane mainPane = new FlowPane();
        {
            mainPane.getChildren().add(new Label("Font"));
            fonts = new ComboBox<>();
            for (final String fontName : Font.getFontNames()) {
                final Label lbl = new Label(fontName);
                lbl.setFont(Font.font(fontName, FontWeight.NORMAL, FontPosture.REGULAR, 11));
                fonts.getItems().add(lbl);
            }
            fonts.setOnMouseClicked(e -> System.out.printf("Clicked '%s'\n", e));
            mainPane.getChildren().add(fonts);
        }
        bPane.setCenter(mainPane);

        final FlowPane bottomPane = new FlowPane();
        {
            final Button ok = new Button("OK");
            ok.setBackground(Background.fill(Color.LIGHTBLUE));
            ok.setOnMouseClicked(e -> {
                AppConstants.setDefaultMonospaceFont(fonts.getValue().getText());
                this.close();
            });
            bottomPane.getChildren().add(ok);
            final Button cancel = new Button("Cancel");
            cancel.setOnMouseClicked(e -> this.close());
            bottomPane.getChildren().add(cancel);
        }
        bPane.setBottom(bottomPane);

        this.setTitle("Emu - Settings");
        this.setScene(scene);
        this.show();

        Platform.runLater(this::requestFocus);
    }
}
