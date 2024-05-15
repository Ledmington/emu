package com.ledmington.elf.section;

/**
 * An ELF Null section.
 */
public final class NullSection extends Section {

    /**
     * Creates a Null section with the given section header entry.
     *
     * @param entry
     *      The section header entry corresponding to this section.
     */
    public NullSection(final SectionHeader entry) {
        super("", entry);
    }
}
