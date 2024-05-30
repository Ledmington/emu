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
package com.ledmington.emu;

import java.util.Objects;

import com.ledmington.cpu.x86.Register64;
import com.ledmington.emu.mem.MemoryController;
import com.ledmington.utils.ReadOnlyByteBuffer;

public final class InstructionFetcher extends ReadOnlyByteBuffer {

    private final X86RegisterFile regFile;
    private final MemoryController mem;

    public InstructionFetcher(final MemoryController mem, final X86RegisterFile regFile) {
        super(false, 1L);
        this.mem = Objects.requireNonNull(mem);
        this.regFile = Objects.requireNonNull(regFile);
    }

    @Override
    public void setPosition(final long newPosition) {
        regFile.set(Register64.RIP, newPosition);
    }

    @Override
    public long getPosition() {
        return regFile.get(Register64.RIP);
    }

    @Override
    protected byte read() {
        return mem.readCode(regFile.get(Register64.RIP));
    }

    @Override
    public ReadOnlyByteBuffer copy() {
        return new InstructionFetcher(this.mem, this.regFile);
    }
}
