package com.ledmington.elf.section;

public final class DestructorsSection extends LoadableSection {
    public DestructorsSection(final String name, final SectionHeader sectionHeader) {
        super(name, sectionHeader);
    }

    @Override
    public byte[] content() {
        return new byte[0];
    }
}
