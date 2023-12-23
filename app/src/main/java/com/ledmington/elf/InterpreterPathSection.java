package com.ledmington.elf;

public final class InterpreterPathSection extends Section {

    private final String interpreterFilePath;

    public InterpreterPathSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);

        final int start = (int) sectionHeader.fileOffset();
        b.setPosition(start);

        final StringBuilder sb = new StringBuilder();
        char c = (char) b.read1();
        while (c != '\0') {
            sb.append(c);
            c = (char) b.read1();
        }
        this.interpreterFilePath = sb.toString();
    }
}
