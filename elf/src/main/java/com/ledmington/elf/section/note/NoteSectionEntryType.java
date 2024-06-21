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
package com.ledmington.elf.section.note;

import java.util.Objects;

public enum NoteSectionEntryType {
    NT_GNU_ABI_TAG(1, "NT_GNU_ABI_TAG (ABI version tag)"),
    NT_GNU_HWCAP(2, "NT_GNU_HWCAP"),
    NT_GNU_BUILD_ID(3, "NT_GNU_BUILD_ID (unique build ID bitstring)"),
    NT_GNU_GOLD_VERSION(4, "NT_GNU_GOLD_VERSION"),
    NT_GNU_PROPERTY_TYPE_0(5, "NT_GNU_PROPERTY_TYPE_0");

    private final int code;
    private final String description;

    NoteSectionEntryType(final int code, final String description) {
        this.code = code;
        this.description = Objects.requireNonNull(description);
    }

    public static NoteSectionEntryType fromCode(final int type) {
        return switch (type) {
            case 1 -> NT_GNU_ABI_TAG;
            case 2 -> NT_GNU_HWCAP;
            case 3 -> NT_GNU_BUILD_ID;
            case 4 -> NT_GNU_GOLD_VERSION;
            case 5 -> NT_GNU_PROPERTY_TYPE_0;
            default -> throw new IllegalArgumentException(
                    String.format("Unknown note section entry type %d (0x%08x)", type, type));
        };
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
