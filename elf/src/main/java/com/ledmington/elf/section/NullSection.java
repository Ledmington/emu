package com.ledmington.elf.section;

import com.ledmington.elf.Section;
import com.ledmington.elf.SectionHeader;

public final class NullSection extends Section {
    public NullSection(final SectionHeader entry) {
        super("", entry);
    }
}
