package com.ledmington.emu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.utils.MiniLogger;

public final class Main {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private static ELF parseELF(final String filename) {
        final File file = new File(filename);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File '%s' does not exist\n", filename));
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(filename));
        } catch (final IOException e) {
            throw new IllegalStateException(String.format("There was an error reading file '%s'\n", filename));
        }

        logger.info("The file '%s' is %,d bytes long", filename, bytes.length);

        return new ELFParser().parse(bytes);
    }

    private static void run(final String filename) {
        final ELF elf = parseELF(filename);
        logger.info("ELF file parsed successfully");

        final Emulator emu = new Emulator(elf);

        logger.info(" ### Execution start ### ");
        emu.run();
        logger.info(" ### Execution end ### ");
    }

    public static void main(final String[] args) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

        String filename = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                System.out.println(String.join(
                        "\n",
                        "",
                        " emu - CPU emulator",
                        "",
                        " Usage: emu [-v|-vv] FILE",
                        "",
                        " Command line options:",
                        "",
                        " -h, --help  Shows this help message and exits.",
                        " -q, --quiet Sets the verbosity level to ERROR.",
                        " -v          Sets the verbosity level to INFO.",
                        " -vv         Sets the verbosity level to DEBUG.",
                        " FILE       The ELF executable file to emulate.",
                        ""));
                System.exit(0);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
            } else if (args[i].equals("-v")) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
            } else if (args[i].equals("-vv")) {
                MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
            } else {
                if (filename != null) {
                    System.err.println("Cannot set filename twice");
                    System.exit(-1);
                } else {
                    filename = args[i];
                }
            }
        }

        try {
            run(filename);
        } catch (final Throwable t) {
            logger.error(t);
            System.exit(-1);
        }
    }
}
