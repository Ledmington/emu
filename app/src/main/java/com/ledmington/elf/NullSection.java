package com.ledmington.elf;

public final class NullSection extends Section {
    public NullSection(final SectionHeader entry) {
        super("", entry);
    }
}
