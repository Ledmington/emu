package com.ledmington.elf.section;

import java.util.Objects;

public abstract class Section {

    private final String name;
    private final SectionHeader header;

    protected Section(final String name, final SectionHeader sectionHeader) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);
    }

    public final String getName() {
        return name;
    }

    public final SectionHeader getHeader() {
        return header;
    }
}
