package com.ledmington.elf.section;

/**
 * Reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.4.
 */
public final class GnuVersionRequirementsSection extends LoadableSection {
    public GnuVersionRequirementsSection(final String name, final SectionHeader sectionHeader) {
        super(name, sectionHeader);
    }

    @Override
    public byte[] content() {
        return new byte[0];
    }
}
