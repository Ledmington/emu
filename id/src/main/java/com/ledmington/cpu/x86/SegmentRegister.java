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

import java.util.Objects;

public final class SegmentRegister extends Register {

    private final Register16 seg;
    private final Register reg;

    public SegmentRegister(final Register16 segment, final Register register) {
        super(Objects.requireNonNull(register).toIntelSyntax());
        this.seg = Objects.requireNonNull(segment);
        this.reg = register;
    }

    public Register16 segment() {
        return seg;
    }

    public Register register() {
        return reg;
    }

    @Override
    public int bits() {
        // TODO: check this
        return reg.bits();
    }
}
