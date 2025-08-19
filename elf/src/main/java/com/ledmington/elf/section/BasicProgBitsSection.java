/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.elf.section;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** A "non-special" PROGBITS ELF section. */
public final class BasicProgBitsSection implements ProgBitsSection {

	private final String name;
	private final SectionHeader header;
	private final byte[] content;

	/**
	 * Creates a BasicProgBitsSection with the given name and header by parsing bytes read from the ReadOnlyByteBuffer.
	 *
	 * @param name The name of this section.
	 * @param sectionHeader The header of this section.
	 * @param b The buffer to read bytes from.
	 */
	public BasicProgBitsSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);

		b.setPosition(sectionHeader.getFileOffset());
		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);
		final int size = BitUtils.asInt(sectionHeader.getSectionSize());
		this.content = new byte[size];
		for (int i = 0; i < size; i++) {
			this.content[i] = b.read1();
		}
		b.setAlignment(oldAlignment);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SectionHeader header() {
		return header;
	}

	@Override
	public byte[] getLoadableContent() {
		final byte[] v = new byte[content.length];
		System.arraycopy(content, 0, v, 0, content.length);
		return v;
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(content);
		return h;
	}

	@Override
	public String toString() {
		return "BasicProgBitsSection(name=" + name + ";header=" + header + ";content=" + Arrays.toString(content) + ')';
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final BasicProgBitsSection bpbs)) {
			return false;
		}
		return this.name.equals(bpbs.name)
				&& this.header.equals(bpbs.header)
				&& Arrays.equals(this.content, bpbs.content);
	}
}
