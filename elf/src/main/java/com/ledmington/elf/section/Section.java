package com.ledmington.elf.section;

import java.util.Objects;

public abstract class Section {

    private final String name;
    private final SectionHeader sectionHeader;

    protected Section(final String name, final SectionHeader sectionHeader) {
        this.name = Objects.requireNonNull(name);
        this.sectionHeader = Objects.requireNonNull(sectionHeader);
    }

    public final String name() {
        return name;
    }

    public final SectionHeader header() {
        return sectionHeader;
    }
}
