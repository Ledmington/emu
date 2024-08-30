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
import java.io.IOException;
import java.nio.file.Files;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class ELFViewer extends Stage {

    private final TextArea textArea = new TextArea();

    public ELFViewer(final double width, final double height) {
        final VBox vbox = new VBox();
        final Button load = new Button();
        load.setText("Load");
        load.setOnAction(e -> {
            final FileChooser fc = new FileChooser();
            fc.setTitle("Select an ELF file to be loaded");
            fc.getExtensionFilters()
                    .addAll(
                            new FileChooser.ExtensionFilter("ELF files", "*.elf"),
                            new FileChooser.ExtensionFilter("Shared libraries", "*.so"),
                            new FileChooser.ExtensionFilter("Static libraries", "*.a"),
                            new FileChooser.ExtensionFilter("All files", "*.*"));
            final File selectedFile = fc.showOpenDialog(this);
            if (selectedFile != null) {
                this.loadFile(selectedFile);
            }
        });

        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", 12));
        vbox.getChildren().addAll(load, textArea);
        final Scene scene = new Scene(vbox);

        this.setScene(scene);
        this.setTitle("ELF Viewer");
        this.setWidth(width);
        this.setHeight(height);
        this.centerOnScreen();
        this.show();
    }

    private void loadFile(final File elfFile) {
        final byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(elfFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fileBytes.length; i++) {
            if (i % 16 == 0) {
                // start of the row
                sb.append(" 0x").append(String.format("%08x", i)).append(" : ");
            }
            sb.append(String.format("%02x", fileBytes[i]));
            if (i % 2 == 1) {
                sb.append(' ');
            }
            if (i % 16 == 15) {
                sb.append('\n');
            }
        }
        this.textArea.setText(sb.toString());
    }
}
