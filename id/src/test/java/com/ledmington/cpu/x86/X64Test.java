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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.MiniLogger.LoggingLevel;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public sealed class X64Test permits TestDecoding, TestIncompleteInstruction {

	private static final String testInputFileName = "x64.test.asm";
	private static List<Arguments> args;

	private static List<Byte> stringToHexList(final String hexCode) {
		if (hexCode.isBlank()) {
			throw new IllegalArgumentException("Empty/blank string.");
		}
		final String[] parsed = hexCode.split(" ");
		final List<Byte> code = new ArrayList<>(parsed.length);
		for (final String s : parsed) {
			code.add(BitUtils.asByte(Integer.parseInt(s, 16)));
		}
		return code;
	}

	@BeforeAll
	static void setup() {
		MiniLogger.setMinimumLevel(LoggingLevel.DEBUG);

		args = new ArrayList<>();
		final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (BufferedReader br =
				Files.newBufferedReader(Paths.get(Objects.requireNonNull(classloader.getResource(testInputFileName))
								.toURI())
						.toFile()
						.toPath()
						.normalize()
						.toAbsolutePath())) {
			int i = 0;
			for (String line = br.readLine(); line != null; line = br.readLine(), i++) {
				if (line.isEmpty() || line.isBlank() || line.charAt(0) == '#') {
					continue;
				}

				final int idx = line.indexOf('|');
				if (idx < 0 || idx != line.lastIndexOf('|')) {
					throw new IllegalArgumentException(
							String.format("Line %,d: '%s' is not formatted correctly", i, line));
				}
				args.add(Arguments.of(
						line.substring(0, idx - 1).strip(),
						stringToHexList(line.substring(idx + 1).strip())));
			}
		} catch (final IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterAll
	static void teardown() {
		args.clear();
	}

	protected static Stream<Arguments> instructions() {
		return args.stream();
	}
}
