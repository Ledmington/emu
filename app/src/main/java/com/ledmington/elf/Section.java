package com.ledmington.elf;

import java.util.Objects;

public abstract class Section {

    private final String name;
    private final SHTEntry sectionHeader;

    protected Section(final String name, final SHTEntry sectionHeader) {
        this.name = Objects.requireNonNull(name);
        this.sectionHeader = Objects.requireNonNull(sectionHeader);
    }
}
