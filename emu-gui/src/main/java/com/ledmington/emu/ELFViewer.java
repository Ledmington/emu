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

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class ELFViewer extends Stage {

    private final TextArea textArea = new TextArea();

    public ELFViewer(final double width, final double height) {
        final BorderPane mainPane = new BorderPane();

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
                            new FileChooser.ExtensionFilter("All files", "*.*"));
            final File selectedFile = fc.showOpenDialog(this);
            if (selectedFile != null) {
                this.loadFile(selectedFile);
            }
        });

        final Button settings = new Button();
        settings.setText("Settings");
        settings.setOnAction(e -> new SettingsWindow());

        textArea.setEditable(false);
        topPane.setHgap(4);
        topPane.setPadding(new Insets(5));
        topPane.setPrefWrapLength(300);
        topPane.getChildren().addAll(load, settings);
        mainPane.setTop(topPane);
        mainPane.setCenter(textArea);
        final Scene scene = new Scene(mainPane);

        this.setScene(scene);
        this.setTitle("Emu - ELF Viewer");
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
        final int nBytesPerRow = 16;
        final int nBytesPerBlock = 4;
        for (int i = 0; i < fileBytes.length; i++) {
            if (i % nBytesPerRow == 0) {
                // start of the row
                sb.append(" 0x").append(String.format("%08x", i)).append(" : ");
            }
            sb.append(String.format("%02x", fileBytes[i]));
            if (i % nBytesPerBlock == nBytesPerBlock - 1) {
                sb.append(' ');
            }
            if (i % nBytesPerRow == nBytesPerRow - 1) {
                sb.append('\n');
            }
        }
        textArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
        this.textArea.setText(sb.toString());
    }
}
