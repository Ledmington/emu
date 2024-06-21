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

public enum GnuPropertyType {
    /* Stack size.  */
    GNU_PROPERTY_STACK_SIZE,
    /* No copy relocation on protected data symbol.  */
    GNU_PROPERTY_NO_COPY_ON_PROTECTED,
    GNU_PROPERTY_X86_FEATURE_1_AND;

    public static GnuPropertyType fromCode(final int code) {
        return switch (code) {
            case 1 -> GNU_PROPERTY_STACK_SIZE;
            case 2 -> GNU_PROPERTY_NO_COPY_ON_PROTECTED;
            case 0xc0000002 -> GNU_PROPERTY_X86_FEATURE_1_AND;
            default -> throw new IllegalArgumentException(
                    String.format("Unknown GNU property type code %d (0x%08x)", code, code));
        };
    }
}
