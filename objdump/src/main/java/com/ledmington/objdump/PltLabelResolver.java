/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.objdump;

import java.util.HashMap;
import java.util.Map;

import com.ledmington.cpu.InstructionDecoder;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.Section;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

/**
 * Finds the '&lt;symbol@plt&gt;'-style labels of PLT-like sections (e.g. '.plt', '.plt.got', '.plt.sec') by correlating
 * each stub's RIP-relative jump/call/push target with the GOT slot addresses touched by relocations.
 */
final class PltLabelResolver {

	private PltLabelResolver() {}

	@SuppressWarnings("PMD.UseConcurrentHashMap")
	/* default */ static Map<Long, String> findPltLabels(
			final SectionTable st, final Map<Long, SymbolResolver.RelocatedSymbol> relocatedSymbols) {
		final Map<Long, String> labels = new HashMap<>();
		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section s = st.getSection(i);
			if (!s.getName().startsWith(".plt") || !(s instanceof final LoadableSection ls)) {
				continue;
			}
			final long entrySize = s.header().getEntrySize();
			final long noEntries = 0L;
			if (entrySize <= noEntries) {
				continue;
			}
			final long sectionStart = s.header().getVirtualAddress();
			final byte[] content = ls.getLoadableContent();
			// A new buffer is unavoidably needed for every PLT-like section being scanned.
			@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
			final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
			while (b.getPosition() < content.length) {
				final long instructionStart = b.getPosition();
				final Instruction inst;
				try {
					inst = InstructionDecoder.fromHex(b);
				} catch (final DecodingException | ArrayIndexOutOfBoundsException e) {
					// Reached the padding/non-instruction bytes at the end of the PLT-like section.
					break;
				}
				final long instructionEnd = b.getPosition();
				final IndirectOperand io = Disassembler.findRipRelativeOperand(inst);
				if (io == null) {
					continue;
				}
				final long target = sectionStart + instructionEnd + io.getDisplacement();
				final SymbolResolver.RelocatedSymbol rs = relocatedSymbols.get(target);
				if (rs != null) {
					final long stubStart = sectionStart + instructionStart / entrySize * entrySize;
					labels.put(stubStart, rs.bareName() + "@plt");
				}
			}
		}
		return labels;
	}
}
