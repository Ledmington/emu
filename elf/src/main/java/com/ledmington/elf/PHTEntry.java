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

public final class PHTEntry {

    private final PHTEntryType type;
    private final boolean readable;
    private final boolean writeable;
    private final boolean executable;
    private final long segmentOffset;
    private final long segmentVirtualAddress;
    private final long segmentPhysicalAddress;
    private final long segmentFileSize;
    private final long segmentMemorySize;
    private final long alignment;

    public PHTEntry(
            PHTEntryType type,
            int flags,
            long segmentOffset,
            long segmentVirtualAddress,
            long segmentPhysicalAddress,
            long segmentFileSize,
            long segmentMemorySize,
            long alignment) {
        this.type = type;
        this.readable = (flags & PHTEntryFlags.PF_R.getCode()) != 0;
        this.writeable = (flags & PHTEntryFlags.PF_W.getCode()) != 0;
        this.executable = (flags & PHTEntryFlags.PF_X.getCode()) != 0;

        if ((flags & ~(PHTEntryFlags.PF_R.getCode() | PHTEntryFlags.PF_W.getCode() | PHTEntryFlags.PF_X.getCode()))
                != 0) {
            throw new IllegalArgumentException(String.format("Invalid PHT Entry flags 0x%08x", flags));
        }

        this.segmentOffset = segmentOffset;
        this.segmentVirtualAddress = segmentVirtualAddress;
        this.segmentPhysicalAddress = segmentPhysicalAddress;
        this.segmentFileSize = segmentFileSize;
        this.segmentMemorySize = segmentMemorySize;
        this.alignment = alignment;
    }

    public PHTEntryType type() {
        return type;
    }

    public long segmentVirtualAddress() {
        return segmentVirtualAddress;
    }

    public long segmentMemorySize() {
        return segmentMemorySize;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean isExecutable() {
        return executable;
    }

    @Override
    public String toString() {
        return "Segment type           : " + type
                + '\n'
                + "Flags                  : "
                + (readable ? 'R' : ' ') + (writeable ? 'W' : ' ') + (executable ? 'X' : ' ') + "\n"
                + "Offset                 : "
                + String.format("0x%016x\n", segmentOffset)
                + "Virtual address        : "
                + String.format("0x%016x\n", segmentVirtualAddress)
                + "Physical address       : "
                + String.format("0x%016x\n", segmentPhysicalAddress)
                + "Segment size on file   : "
                + String.format("%,d bytes\n", segmentFileSize)
                + "Segment size in memory : "
                + String.format("%,d bytes\n", segmentMemorySize)
                + "Alignment              : "
                + alignment
                + ((alignment == 0 || alignment == 1) ? " (no alignment)" : "")
                + '\n';
    }
}
