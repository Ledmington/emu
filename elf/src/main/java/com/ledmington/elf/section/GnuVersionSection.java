package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

/**
 * Reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.2.
 */
public final class GnuVersionSection extends LoadableSection {

    private final short[] versions;

    public GnuVersionSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.sectionSize() / 2);
        this.versions = new short[nEntries];
        for (int i = 0; i < nEntries; i++) {
            versions[i] = b.read2();
        }
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(versions.length * 2);
        for (final short version : versions) {
            bb.write(version);
        }
        return bb.array();
    }
}
