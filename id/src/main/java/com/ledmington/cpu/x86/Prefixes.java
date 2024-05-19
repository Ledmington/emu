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
package com.ledmington.cpu.x86;

import java.util.Optional;

public record Prefixes(
        Optional<InstructionPrefix> p1,
        Optional<Byte> p2,
        boolean hasOperandSizeOverridePrefix,
        boolean hasAddressSizeOverridePrefix,
        boolean hasRexPrefix,
        RexPrefix rex) {

    @Override
    public String toString() {
        return "Prefixes[p1=" + (p1.isPresent() ? p1.orElseThrow().name() : p1) + ";p2="
                + (p2.isPresent() ? String.format("0x%02x", p2.orElseThrow()) : p2) + ";hasOperandSizeOverridePrefix="
                + hasOperandSizeOverridePrefix + ";hasAddressSizeoverridePrefix=" + hasAddressSizeOverridePrefix
                + ";rex=" + rex.toString() + "]";
    }
}
