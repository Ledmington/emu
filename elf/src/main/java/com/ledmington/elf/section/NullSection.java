package com.ledmington.elf.section;

public final class NullSection extends Section {
    public NullSection(final SectionHeader entry) {
        super("", entry);
    }
}
