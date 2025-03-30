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
package com.ledmington.view;

import java.util.Objects;

import javafx.scene.text.Font;

public final class AppConstants {

	/**
	 * Shorthand field which holds the name of the Operating System MiniSim is running on. NOTE: for Linux-based
	 * distributions, this will be just "Linux".
	 */
	public static final String OSName = System.getProperty("os.name");

	/**
	 * Shorthand field which holds the version of the Operating System MiniSim is running on. NOTE: for Linux-based
	 * distributions, this will be the Linux kernel version.
	 */
	public static final String OSVersion = System.getProperty("os.version");

	/** Shorthand field which holds the version of Java MiniSim is running on. */
	public static final String javaVersion = System.getProperty("java.version");

	/**
	 * Shorthand field which holds the version of JVM MiniSim is running on. NOTE: usually this is similar to
	 * {@link #javaVersion}.
	 */
	public static final String jvmVersion = System.getProperty("java.vm.version");

	/** Shorthand field which holds the version of JavaFX MiniSim is running on. */
	public static final String javafxVersion = System.getProperty("javafx.version");

	/** Shorthand field which holds the path where all the temporary files/folders are stored by the OS. */
	public static final String tmpDirectoryPath = System.getProperty("java.io.tmpdir");

	/** Shorthand monospace font family. */
	private static String MONOSPACE_FONT_FAMILY = Font.getFamilies().contains("Consolas")
			? "Consolas"
			: Font.getFamilies().contains("Cousine")
					? "Cousine"
					: Font.getDefault().getFamily();

	/** Shorthand default font size. */
	private static int DEFAULT_FONT_SIZE = 12;

	private static int MAX_CODE_INSTRUCTIONS = 50;
	private static int MAX_MEMORY_LINES = 50;
	private static int MEMORY_BYTES_PER_LINE = 16;

	private AppConstants() {}

	public static String getDefaultMonospaceFont() {
		return MONOSPACE_FONT_FAMILY;
	}

	public static void setDefaultMonospaceFont(final String monospaceFontFamily) {
		Objects.requireNonNull(monospaceFontFamily);
		if (monospaceFontFamily.isBlank() || !Font.getFamilies().contains(monospaceFontFamily)) {
			throw new IllegalArgumentException(String.format("Illegal font family name '%s'", monospaceFontFamily));
		}
		MONOSPACE_FONT_FAMILY = monospaceFontFamily;
	}

	public static int getDefaultFontSize() {
		return DEFAULT_FONT_SIZE;
	}

	public static void setDefaultFontSize(final int newFontSize) {
		final int minFontSize = 1;
		if (newFontSize <= minFontSize) {
			throw new IllegalArgumentException(String.format("Invalid font size: %,d", newFontSize));
		}
		DEFAULT_FONT_SIZE = newFontSize;
	}

	public static int getMaxCodeInstructions() {
		return MAX_CODE_INSTRUCTIONS;
	}

	public static void setMaxCodeInstructions(final int n) {
		final int minCodeInstructions = 1;
		if (n < minCodeInstructions) {
			throw new IllegalArgumentException(String.format("Invalid max code instructions %,d", n));
		}
		MAX_CODE_INSTRUCTIONS = n;
	}

	public static int getMaxMemoryLines() {
		return MAX_MEMORY_LINES;
	}

	public static void setMaxMemoryLines(final int n) {
		final int minMemoryLines = 1;
		if (n < minMemoryLines) {
			throw new IllegalArgumentException(String.format("Invalid max memory lines %,d", n));
		}
		MAX_MEMORY_LINES = n;
	}

	public static int getMemoryBytesPerLine() {
		return MEMORY_BYTES_PER_LINE;
	}

	public static void setMemoryBytesPerLine(final int n) {
		final int minMemoryBytesPerLine = 1;
		if (n < minMemoryBytesPerLine) {
			throw new IllegalArgumentException(String.format("Invalid memory bytes per line %,d", n));
		}
		MEMORY_BYTES_PER_LINE = n;
	}
}
