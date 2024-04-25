package com.ledmington.elf.section;

import com.ledmington.utils.ByteBuffer;

public final class DestructorsSection extends Section {
    public DestructorsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);
    }
}
