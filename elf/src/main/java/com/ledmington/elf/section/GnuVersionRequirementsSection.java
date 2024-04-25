package com.ledmington.elf.section;

import com.ledmington.elf.Section;
import com.ledmington.elf.SectionHeader;
import com.ledmington.utils.ByteBuffer;

/**
 * Reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.4.
 */
public final class GnuVersionRequirementsSection extends Section {

    public GnuVersionRequirementsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);
    }
}
