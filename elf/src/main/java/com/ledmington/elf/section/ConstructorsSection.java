package com.ledmington.elf.section;

public final class ConstructorsSection extends LoadableSection {
    public ConstructorsSection(final String name, final SectionHeader sectionHeader) {
        super(name, sectionHeader);
    }

    @Override
    public byte[] content() {
        return new byte[0];
    }
}
