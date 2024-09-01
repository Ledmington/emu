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

import javafx.scene.text.Font;

public final class AppConstants {

    private AppConstants() {}

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
    public static final String monospaceFontFamily = Font.getFamilies().contains("Consolas")
            ? "Consolas"
            : (Font.getFamilies().contains("Courier")
                    ? "Courier"
                    : (Font.getFamilies().contains("Monospaced")
                            ? "Monospaced"
                            : Font.getDefault().getFamily()));
}
