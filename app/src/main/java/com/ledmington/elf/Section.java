package com.ledmington.elf;

import java.util.Objects;

public abstract class Section {

    private final String name;
    private final SectionHeader sectionHeader;

    protected Section(final String name, final SectionHeader sectionHeader) {
        this.name = Objects.requireNonNull(name);
        this.sectionHeader = Objects.requireNonNull(sectionHeader);
    }
}
