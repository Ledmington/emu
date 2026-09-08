/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.cpu;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.MaskRegister;
import com.ledmington.cpu.x86.Operand;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RegisterFPU;
import com.ledmington.cpu.x86.RegisterMMX;
import com.ledmington.cpu.x86.RegisterXMM;
import com.ledmington.cpu.x86.RegisterYMM;
import com.ledmington.cpu.x86.RegisterZMM;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.cpu.x86.SegmentedAddress;

/** Checks whether an {@link Operand} of an {@link Instruction} is compatible with a given {@link OperandType}. */
final class OperandTypeMatcher {

	private OperandTypeMatcher() {}

	/**
	 * Checks whether all the operands of the given instruction match the operand types of the given
	 * {@link OperandTypeList}.
	 *
	 * @param otl The expected operand types.
	 * @param inst The instruction whose operands are to be checked.
	 * @return True if every operand of the instruction matches the corresponding operand type.
	 */
	/* default */ static boolean matches(final OperandTypeList otl, final Instruction inst) {
		final int n = otl.numOperands();
		for (int i = 0; i < n; i++) {
			if (!matches(otl.operandType(i), inst.operand(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean matches(final OperandType opt, final Operand op) {
		return switch (opt) {
			case R8 -> op instanceof Register8;
			case R16 -> op instanceof Register16;
			case R32 -> op instanceof final Register32 r && r != Register32.EIP;
			case R64 -> op instanceof final Register64 r && r != Register64.RIP;
			case RMM -> op instanceof RegisterMMX;
			case RX -> op instanceof RegisterXMM;
			case RY -> op instanceof RegisterYMM;
			case RZ -> op instanceof RegisterZMM;
			case RK -> op instanceof MaskRegister;
			case RF -> op instanceof RegisterFPU;
			case RS -> op instanceof SegmentRegister;
			case M8 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.BYTE_PTR;
			case M16 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.WORD_PTR;
			case M32 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.DWORD_PTR;
			case M64 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.QWORD_PTR;
			case M80 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.TBYTE_PTR;
			case M128 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.XMMWORD_PTR;
			case M256 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.YMMWORD_PTR;
			case M512 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.ZMMWORD_PTR;
			case I8 -> op instanceof final Immediate imm && imm.bits() == 8;
			case I16 -> op instanceof final Immediate imm && imm.bits() == 16;
			case I32 -> op instanceof final Immediate imm && imm.bits() == 32;
			case I64 -> op instanceof final Immediate imm && imm.bits() == 64;
			case S64 ->
				op instanceof final SegmentedAddress sa && sa.immediate().bits() == 64;
		};
	}
}
