/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

public final class InstructionEncoder {

	private InstructionEncoder() {}

	public static byte[] encode(final Instruction... code) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		for (int i = 0; i < code.length; i++) {
			encode(wb, code[i]);
		}
		return wb.array();
	}

	public static byte[] encode(final Instruction inst) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		encode(wb, inst);
		return wb.array();
	}

	private static void encode(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case Opcode.NOP -> wb.write((byte) 0x90);
			case Opcode.CWDE -> wb.write((byte) 0x98);
			case Opcode.CDQE -> wb.write((byte) 0x48, (byte) 0x98);
			case Opcode.CDQ -> wb.write((byte) 0x99);
			case Opcode.INC -> {
				if (inst.firstOperand() instanceof Register32 r) {
					wb.write((byte) 0xff);
					wb.write((byte) 0xc0 + r.ordinal());
				} else {
					wb.write((byte) 0xfe);
				}
			}
			case Opcode.DEC -> wb.write((byte) 0xff);
			case Opcode.SETG -> wb.write((byte) 0x0f, (byte) 0x9f);
			case Opcode.SAHF -> wb.write((byte) 0x9e);
			case Opcode.LAHF -> wb.write((byte) 0x9f);
			case Opcode.XGETBV -> wb.write((byte) 0x0f, (byte) 0x01, (byte) 0xd0);
			case Opcode.UD2 -> wb.write((byte) 0x0f, (byte) 0x0b);
			case Opcode.INCSSPQ -> wb.write((byte) 0xf3);
			case Opcode.BSWAP -> wb.write((byte) 0x0f, (byte) 0xcc);
			case Opcode.ENDBR64 -> wb.write((byte) 0xf3, (byte) 0x0f, (byte) 0x1e, (byte) 0xfa);

				// jumps
			case Opcode.JA -> {
				if (inst.firstOperand() instanceof RelativeOffset ro) {
					if (ro.bits() == 32) {
						wb.write((byte) 0x0f, (byte) 0x87);
						wb.write(BitUtils.asInt(ro.getValue()));
					} else {
						wb.write((byte) 0x77, BitUtils.asByte(ro.getValue()));
					}
				} else {
					wb.write(0);
				}
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}
}
