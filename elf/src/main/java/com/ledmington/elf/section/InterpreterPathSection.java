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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.ledmington.utils.ReadOnlyByteBuffer;

/** The .interp ELF section. */
public final class InterpreterPathSection implements ProgBitsSection {

	private final SectionHeader header;
	private final String interpreterFilePath;

	/**
	 * Creates a new .interp ELF section with the given data.
	 *
	 * @param sectionHeader The header of this section.
	 * @param b The ReadOnlyByteBuffer to read data from.
	 */
	public InterpreterPathSection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
		this.header = Objects.requireNonNull(sectionHeader);

		b.setPosition(sectionHeader.getFileOffset());
		final long start = b.getPosition();
		final long size = sectionHeader.getSectionSize();
		b.setAlignment(1L);

		final StringBuilder sb = new StringBuilder();
		char c = (char) b.read1();
		while (b.getPosition() < start + size && c != '\0') {
			sb.append(c);
			c = (char) b.read1();
		}
		this.interpreterFilePath = sb.toString();
	}

	/**
	 * Returns the required interpreter's file path.
	 *
	 * @return The interpreter's file path as a String.
	 */
	public String getInterpreterFilePath() {
		return interpreterFilePath;
	}

	@Override
	public String getName() {
		return ".interp";
	}

	@Override
	public SectionHeader getHeader() {
		return header;
	}

	@Override
	public byte[] getLoadableContent() {
		return interpreterFilePath.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String toString() {
		return "InterpreterPathSection(header=" + header + ";interpreterPath=" + interpreterFilePath + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + header.hashCode();
		h = 31 * h + interpreterFilePath.hashCode();
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final InterpreterPathSection ips)) {
			return false;
		}
		return this.header.equals(ips.header) && this.interpreterFilePath.equals(ips.interpreterFilePath);
	}
}
