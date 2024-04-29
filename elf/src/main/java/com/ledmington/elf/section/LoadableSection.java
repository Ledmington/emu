package com.ledmington.elf.section;

public abstract class LoadableSection extends Section {
    protected LoadableSection(final String name, final SectionHeader sectionHeader) {
        super(name, sectionHeader);
    }

    public abstract byte[] content();
}
