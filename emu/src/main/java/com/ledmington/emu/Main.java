package com.ledmington.emu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.ProgBitsSection;
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

        final byte[] code = ((ProgBitsSection) elf.getFirstSectionByName(".text")).content();
        final InstructionDecoder dec = new InstructionDecoder(code);
        final Emulator emu = new Emulator(elf, dec);

        logger.info(" ### Execution start ### ");
        emu.run();
        logger.info(" ### Execution end ### ");
    }

    public static void main(final String[] args) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
        if (args.length == 0) {
            logger.error("expected 1 argument");
            System.exit(-1);
        }
        if (args.length > 1) {
            logger.warning("Expected 1 argument but received %,d, the others will be ignored\n", args.length);
        }

        final String filename = args[0];

        try {
            run(filename);
        } catch (final Throwable t) {
            logger.error(t);
            System.exit(-1);
        }
    }
}
