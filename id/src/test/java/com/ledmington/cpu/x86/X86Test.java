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
package com.ledmington.cpu.x86;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.MiniLogger.LoggingLevel;

public class X86Test {

    private static final String testInputFileName = "x86.test.asm";
    private static List<Arguments> args;

    @BeforeAll
    static void setup() {
        MiniLogger.setMinimumLevel(LoggingLevel.DEBUG);

        args = new ArrayList<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (final BufferedReader br =
                Files.newBufferedReader(Paths.get(Objects.requireNonNull(classloader.getResource(testInputFileName))
                                .toURI())
                        .toFile()
                        .toPath()
                        .normalize()
                        .toAbsolutePath())) {
            int i = 0;
            for (String line = br.readLine(); line != null; line = br.readLine(), i++) {
                if (line.isEmpty() || line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                final String[] splitted = line.split("\\|");

                if (splitted.length != 2) {
                    throw new IllegalArgumentException(
                            String.format("Line %,d: '%s' is not formatted correctly", i, line));
                }
                args.add(Arguments.of(splitted[0].strip(), splitted[1].strip()));
            }
        } catch (final IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void teardown() {
        args.clear();
    }

    static Stream<Arguments> instructions() {
        return args.stream();
    }
}
