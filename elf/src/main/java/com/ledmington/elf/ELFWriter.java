/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf;

import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

public final class ELFWriter {
    private ELFWriter() {}

    public static byte[] write(final ELF elf) {
        Objects.requireNonNull(elf);
        final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1();
        writeFileHeader(wb, elf.fileHeader());
        writeProgramHeaderTable(wb, elf);
        writeSectionHeaderTable(wb, elf);
        writeSectionTable(wb, elf);
        return wb.array();
    }

    private static void writeFileHeader(final WriteOnlyByteBuffer wb, final FileHeader fh) {
        wb.write(0x7f454c46);
        wb.write(fh.is32Bit() ? (byte) 1 : (byte) 2);
        wb.write(fh.isLittleEndian() ? (byte) 1 : (byte) 2);
        wb.write((byte) 1);
        wb.write(fh.getOSABI().getCode());
        wb.write(fh.getABIVersion());
        for (int i = 0; i < 7; i++) {
            wb.write((byte) 0x00);
        }
        wb.write(fh.getFileType().getCode());
        wb.write(fh.getISA().getCode());
        wb.write(1);
        if (fh.is32Bit()) {
            wb.write(BitUtils.asInt(fh.getEntryPointVirtualAddress()));
            wb.write(BitUtils.asInt(fh.getProgramHeaderTableOffset()));
            wb.write(BitUtils.asInt(fh.getSectionHeaderTableOffset()));
        } else {
            wb.write(fh.getEntryPointVirtualAddress());
            wb.write(fh.getProgramHeaderTableOffset());
            wb.write(fh.getSectionHeaderTableOffset());
        }
        wb.write(fh.getFlags());
        wb.write(fh.getHeaderSize());
        wb.write(fh.getProgramHeaderTableEntrySize());
        wb.write(fh.getNumProgramHeaderTableEntries());
        wb.write(fh.getSectionHeaderTableEntrySize());
        wb.write(fh.getNumSectionHeaderTableEntries());
        wb.write(fh.getSectionHeaderStringTableIndex());
    }

    private static void writeProgramHeaderTable(final WriteOnlyByteBuffer wb, final ELF elf) {
        final boolean is32Bit = elf.fileHeader().is32Bit();
    }

    private static void writeSectionHeaderTable(final WriteOnlyByteBuffer wb, final ELF elf) {
        //
    }

    private static void writeSectionTable(final WriteOnlyByteBuffer wb, final ELF elf) {
        //
    }
}
