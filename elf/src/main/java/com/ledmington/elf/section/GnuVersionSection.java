package com.ledmington.elf.section;

import com.ledmington.elf.Section;
import com.ledmington.elf.SectionHeader;
import com.ledmington.utils.ByteBuffer;

/**
 * Reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.2.
 */
public final class GnuVersionSection extends Section {

    private final short[] versions;

    public GnuVersionSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.size() / 2);
        this.versions = new short[nEntries];
        for (int i = 0; i < nEntries; i++) {
            versions[i] = b.read2();
        }
    }
}
