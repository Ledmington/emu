package com.ledmington.elf;

public final class GnuPropertySection extends NoteSection {
    public GnuPropertySection(final String name, final SectionHeader entry, final ByteBuffer b) {
        super(name, entry, b);

        if (this.owner.length() != 4 || !this.owner.equals("GNU\0")) {
            throw new IllegalArgumentException(String.format(
                    "Invalid owner for .note.gnu.property section: expected 'GNU' but was '%s'", this.owner));
        }
    }
}
