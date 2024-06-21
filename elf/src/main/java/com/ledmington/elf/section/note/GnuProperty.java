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

import com.ledmington.utils.ReadOnlyByteBuffer;

public record GnuProperty(GnuPropertyType type, int value) {
    public static GnuProperty read(final ReadOnlyByteBuffer robb) {
        final GnuPropertyType type = GnuPropertyType.fromCode(robb.read4());
        final int value =
                switch (type) {
                    case GNU_PROPERTY_X86_FEATURE_1_AND -> robb.read4();
                    default -> throw new IllegalArgumentException(String.format("Unknown GNU property type %s", type));
                };
        return new GnuProperty(type, value);
    }
}
