package com.ledmington.elf;

import java.util.Objects;

public final class Section {

    private final String name;

    public Section(final String name) {
        this.name = Objects.requireNonNull(name);
    }
}
