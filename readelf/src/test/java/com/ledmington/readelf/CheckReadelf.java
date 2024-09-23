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
package com.ledmington.readelf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public final class CheckReadelf {

    private static final boolean isWindows =
            System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");
    private static final String fatJarPath;

    static {
        try {
            fatJarPath = Files.find(
                            Path.of(".", "build").normalize().toAbsolutePath(), 999, (p, bfa) -> bfa.isRegularFile())
                    .filter(p -> p.getFileName().toString().startsWith("emu-readelf")
                            && p.getFileName().toString().endsWith(".jar"))
                    .max((a, b) -> Long.compare(a.toFile().length(), b.toFile().length()))
                    .orElseThrow()
                    .normalize()
                    .toAbsolutePath()
                    .toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isELF(final Path p) {
        try (final InputStream is = Files.newInputStream(p, StandardOpenOption.READ)) {
            final byte[] buffer = new byte[4];
            is.read(buffer);
            return buffer[0] == (byte) 0x7f
                    && buffer[1] == (byte) 0x45
                    && buffer[2] == (byte) 0x4c
                    && buffer[3] == (byte) 0x46;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getRefFilename(final Path p, final boolean wide) {
        final String filename = p.getFileName().toString();
        try {
            final File tempFile = File.createTempFile(
                    (filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename) + "_all_"
                            + (wide ? "wide_" : "") + "ref_",
                    ".out");
            // tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCustomFilename(final Path p, final boolean wide) {
        final String filename = p.getFileName().toString();
        try {
            final File tempFile = File.createTempFile(
                    (filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename) + "_all_"
                            + (wide ? "wide_" : ""),
                    ".out");
            // tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void run(final String outputFile, final String... cmd) {
        System.out.printf(" '%s'\n", String.join(" ", cmd));
        final Process process;
        try {
            if (outputFile == null) {
                process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(line);
                    line = reader.readLine();
                }
            } else {
                process = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .redirectOutput(new File(outputFile))
                        .start();
            }
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.printf(" ERROR: exit code = %d\n", exitCode);
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String runSystemReadelf(final Path p, final boolean wide) {
        final String outputFile = getRefFilename(p, wide);
        final String systemReadelf = "/usr/bin/readelf";
        final String[] cmd = wide
                ? new String[] {systemReadelf, "-a", "-W", p.toString()}
                : new String[] {systemReadelf, "-a", p.toString()};
        run(outputFile, cmd);
        return outputFile;
    }

    private static String runCustomReadelf(final Path p, final boolean wide) {
        final String outputFile = getCustomFilename(p, wide);
        final String[] cmd = wide
                ? new String[] {"java", "-jar", fatJarPath, "-a", "-W", p.toString()}
                : new String[] {"java", "-jar", fatJarPath, "-a", p.toString()};
        run(outputFile, cmd);
        return outputFile;
    }

    private static void checkDiff(final String outputSystemFile, final String outputCustomFile) {
        final String[] cmd = new String[] {"diff", outputSystemFile, outputCustomFile};
        run(null, cmd);
    }

    private static void test(final Path p, final boolean wide) {
        final String outputSystemFile = runSystemReadelf(p, wide);
        final String outputCustomFile = runCustomReadelf(p, wide);
        checkDiff(outputSystemFile, outputCustomFile);
        System.out.println();
    }

    public static void main(final String[] args) {
        if (args.length > 0) {
            System.out.printf("Arguments were passed on the command line but were not needed");
        }

        if (isWindows) {
            System.out.printf("It seems that you are running on a windows machine. This test will be disabled.\n");
            System.exit(0);
        }

        try {
            Files.find(
                            Path.of("/usr/bin").normalize().toAbsolutePath(),
                            1,
                            (p, bfa) -> bfa.isRegularFile() && p.toFile().length() >= 4L && isELF(p))
                    .forEach(p -> {
                        test(p, false);
                        test(p, true);
                    });
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
