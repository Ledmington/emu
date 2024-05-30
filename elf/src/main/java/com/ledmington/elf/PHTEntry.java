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

import com.ledmington.utils.HashUtils;

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
        this.type = Objects.requireNonNull(type);
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

    public PHTEntryType getType() {
        return type;
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

    public long getSegmentOffset() {
        return segmentOffset;
    }

    public long getSegmentVirtualAddress() {
        return segmentVirtualAddress;
    }

    public long getSegmentPhysicalAddress() {
        return segmentPhysicalAddress;
    }

    public long getSegmentMemorySize() {
        return segmentMemorySize;
    }

    public long getSegmentFileSize() {
        return segmentFileSize;
    }

    public long getAlignment() {
        return alignment;
    }

    @Override
    public String toString() {
        return "PHTEntry(type=" + type + ";readable="
                + readable + ";writeable="
                + writeable + ";executable="
                + executable + ";segmentOffset="
                + segmentOffset + ";segmentVirtualAddress="
                + segmentVirtualAddress + ";segmentPhysicalAddress="
                + segmentPhysicalAddress + ";segmentFileSize="
                + segmentFileSize + ";segmentMemorySize="
                + segmentMemorySize + ";alignment="
                + alignment + ')';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + type.hashCode();
        h = 31 * h + HashUtils.hash(readable);
        h = 31 * h + HashUtils.hash(writeable);
        h = 31 * h + HashUtils.hash(executable);
        h = 31 * h + HashUtils.hash(segmentOffset);
        h = 31 * h + HashUtils.hash(segmentVirtualAddress);
        h = 31 * h + HashUtils.hash(segmentPhysicalAddress);
        h = 31 * h + HashUtils.hash(segmentFileSize);
        h = 31 * h + HashUtils.hash(segmentMemorySize);
        h = 31 * h + HashUtils.hash(alignment);
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
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final PHTEntry phte = (PHTEntry) other;
        return this.type.equals(phte.type)
                && this.readable == phte.readable
                && this.writeable == phte.writeable
                && this.executable == phte.executable
                && this.segmentOffset == phte.segmentOffset
                && this.segmentVirtualAddress == phte.segmentVirtualAddress
                && this.segmentPhysicalAddress == phte.segmentPhysicalAddress
                && this.segmentFileSize == phte.segmentFileSize
                && this.segmentMemorySize == phte.segmentMemorySize
                && this.alignment == phte.alignment;
    }
}
