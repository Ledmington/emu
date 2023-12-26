package com.ledmington.elf;

public final class NoBitsSection extends Section {
    public NoBitsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);
    }
}
