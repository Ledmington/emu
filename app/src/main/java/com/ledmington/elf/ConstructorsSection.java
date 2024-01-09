package com.ledmington.elf;

import com.ledmington.utils.ByteBuffer;

public final class ConstructorsSection extends Section {
    public ConstructorsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);
    }
}
