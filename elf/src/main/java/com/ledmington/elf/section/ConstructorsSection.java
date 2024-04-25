package com.ledmington.elf.section;

import com.ledmington.elf.Section;
import com.ledmington.elf.SectionHeader;
import com.ledmington.utils.ByteBuffer;

public final class ConstructorsSection extends Section {
    public ConstructorsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);
    }
}
