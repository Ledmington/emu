package com.ledmington.elf;

import java.util.Objects;

public final class Section {

    private final String name;
    private final SHTEntry sectionHeader;

    public Section(final String name, final SHTEntry sectionHeader) {
        this.name = Objects.requireNonNull(name);
        this.sectionHeader = Objects.requireNonNull(sectionHeader);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name            : ");
        sb.append(name);
        if (name.isBlank() || name.isEmpty()) {
            sb.append("(empty)");
        }
        sb.append('\n');
        sb.append(sectionHeader);
        return sb.toString();
    }
}
