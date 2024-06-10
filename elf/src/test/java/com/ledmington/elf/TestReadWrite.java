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
package com.ledmington.elf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.utils.MiniLogger;

@DisabledOnOs(OS.WINDOWS)
final class TestReadWrite {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "/usr/bin/gcc",
                "/usr/bin/g++",
                "/usr/bin/clang",
                "/usr/bin/clang++",
                "/usr/bin/zip",
                "/usr/bin/unzip",
                "/usr/bin/touch",
                "/usr/bin/whoami",
                "/usr/bin/cat",
                "/usr/bin/grep"
            })
    void readWrite(final String filename) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
        final Path filepath = Path.of(filename);
        assumeTrue(Files.exists(filepath));

        final byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(filepath);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final ELF elf = ELFParser.parse(fileBytes);
        final byte[] encoded = ELFWriter.write(elf);

        assertEquals(
                fileBytes.length,
                encoded.length,
                () -> String.format("Expected %,d bytes but %,d were written", fileBytes.length, encoded.length));
        for (int i = 0; i < fileBytes.length; i++) {
            if (fileBytes[i] != encoded[i]) {
                final int bytesPerRow = 16;
                final int rowsBefore = 3;
                final int rowsAfter = 3;

                final String expectedFormatted;
                {
                    final int start = Math.max(0, i - rowsBefore * bytesPerRow - 7);
                    final int end = Math.min(fileBytes.length, start + (rowsBefore + rowsAfter + 1) * bytesPerRow);
                    final StringBuilder sb = new StringBuilder();
                    for (int r = 0; r < rowsBefore + rowsAfter + 1; r++) {
                        sb.append(String.format("0x%08x: ", start + r * bytesPerRow));
                        for (int k = 0; k < bytesPerRow; k++) {
                            if (start + r * bytesPerRow + k == i) {
                                sb.append(String.format("[%02x]", fileBytes[start + r * bytesPerRow + k]));
                            } else {
                                sb.append(String.format(" %02x ", fileBytes[start + r * bytesPerRow + k]));
                            }
                        }
                        sb.append(' ');
                        for (int k = 0; k < bytesPerRow; k++) {
                            sb.append(String.format("%c", (char) fileBytes[start + r * bytesPerRow + k]));
                        }
                        sb.append('\n');
                    }
                    expectedFormatted = sb.toString();
                }

                final String actualFormatted;
                {
                    final int start = Math.max(0, i - rowsBefore * bytesPerRow - 7);
                    final int end = Math.min(encoded.length, start + (rowsBefore + rowsAfter + 1) * bytesPerRow);
                    final StringBuilder sb = new StringBuilder();
                    for (int r = 0; r < rowsBefore + rowsAfter + 1; r++) {
                        sb.append(String.format("0x%08x: ", start + r * bytesPerRow));
                        for (int k = 0; k < bytesPerRow; k++) {
                            if (start + r * bytesPerRow + k == i) {
                                sb.append(String.format("[%02x]", encoded[start + r * bytesPerRow + k]));
                            } else {
                                sb.append(String.format(" %02x ", encoded[start + r * bytesPerRow + k]));
                            }
                        }
                        sb.append(' ');
                        for (int k = 0; k < bytesPerRow; k++) {
                            sb.append(String.format("%c", (char) encoded[start + r * bytesPerRow + k]));
                        }
                        sb.append('\n');
                    }
                    actualFormatted = sb.toString();
                }

                fail(String.format(
                        "Byte at index 0x%08x (%,d) expected to be 0x%02x but was 0x%02x.\n --- Expected --- \n%s\n --- \n --- Actual ---\n%s\n --- ",
                        i, i, fileBytes[i], encoded[i], expectedFormatted, actualFormatted));
            }
        }
    }
}
