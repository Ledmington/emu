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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFReader;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.utils.MiniLogger;

public final class Main {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");
    private static final PrintWriter out = System.console() == null
            ? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
            : System.console().writer();

    private static ELF parseELF(final String filename) {
        final File file = new File(filename);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File '%s' does not exist%n", filename));
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(filename));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("The file '%s' is %,d bytes long", filename, bytes.length);

        return ELFReader.read(bytes);
    }

    private static void run(final String filename, final String... commandLineArguments) {
        final ELF elf = parseELF(filename);
        logger.info("ELF file parsed successfully");

        final Emulator emu = new X86Emulator();

        logger.info(" ### Execution start ### ");
        emu.run(elf, EmulatorConstants.getMemoryInitializer(), commandLineArguments);
        logger.info(" ### Execution end ### ");
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static void main(final String[] args) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

        String filename = null;

        String[] innerArgs = null;

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if ("-h".equals(arg) || "--help".equals(arg)) {
                out.print(String.join(
                        "\n",
                        "",
                        " emu - CPU emulator",
                        "",
                        " Usage: emu [OPTIONS] FILE",
                        "",
                        " Command line options:",
                        "",
                        " -h, --help   Shows this help message and exits.",
                        " -q, --quiet  Only errors are reported.",
                        " -v           Errors, warnings and info messages are reported.",
                        " -vv          All messages are reported.",
                        "",
                        " --mem-init-random  Uninitialized memory has random values (default).",
                        " --mem-init-zero    Uninitialized memory contains binary zero.",
                        "",
                        " FILE       The ELF executable file to emulate.",
                        ""));
                out.flush();
                System.exit(0);
            } else if ("-q".equals(arg) || "--quiet".equals(arg)) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
            } else if ("-v".equals(arg)) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
            } else if ("-vv".equals(arg)) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
            } else if ("--mem-init-random".equals(arg)) {
                EmulatorConstants.setMemoryInitializer(MemoryInitializer::random);
            } else if ("--mem-init-zero".equals(arg)) {
                EmulatorConstants.setMemoryInitializer(MemoryInitializer::zero);
            } else if ("-V".equals(arg) || "--version".equals(arg)) {
                out.print(String.join("\n", "", " emu - CPU emulator", " v0.0.0", ""));
                out.flush();
                System.exit(0);
            } else {
                filename = arg;

                // all the next command-line arguments are considered to be related to the
                // executable to be emulated
                innerArgs = new String[args.length - i - 1];
                System.arraycopy(args, i + 1, innerArgs, 0, innerArgs.length);
                break;
            }
        }

        if (filename == null) {
            out.println("Expected the name of the file to run.");
            out.flush();
            System.exit(-1);
        }

        try {
            run(filename, innerArgs);
        } catch (final Throwable t) {
            logger.error(t);
            out.flush();
            System.exit(-1);
        }
        out.flush();
    }
}
