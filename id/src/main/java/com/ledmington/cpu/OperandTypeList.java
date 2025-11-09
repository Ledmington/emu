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
package com.ledmington.cpu;

import java.util.Objects;

/** All possible operand type combinations. */
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum OperandTypeList {

	/** The 'list of operands' corresponding to no operands. */
	NO_ARGS(),

	/** The list of operands made of an 8-bit general-purpose register. */
	R8(OperandType.R8),

	/** The list of operands made of a 16-bit general-purpose register. */
	R16(OperandType.R16),

	/** The list of operands made of a 32-bit general-purpose register. */
	R32(OperandType.R32),

	/** The list of operands made of a 64-bit general-purpose register. */
	R64(OperandType.R64),

	/** The list of operands made of a BYTE PTR indirect operand. */
	M8(OperandType.M8),

	/** The list of operands made of a WORD PTR indirect operand. */
	M16(OperandType.M16),

	/** The list of operands made of a DWORD PTR indirect operand. */
	M32(OperandType.M32),

	/** The list of operands made of a QWORD PTR indirect operand. */
	M64(OperandType.M64),

	/** The list of operands made of an 8-bit immediate value. */
	I8(OperandType.I8),

	/** The list of operands made of a 16-bit immediate value. */
	I16(OperandType.I16),

	/** The list of operands made of a 32-bit immediate value. */
	I32(OperandType.I32),

	/** The list of operands made of a 16-bit immediate value and an 8-bit immediate value. */
	I16_I8(OperandType.I16, OperandType.I8),

	/** The list of operands made of an 8-bit general-purpose register and an 8-bit general-purpose register. */
	R8_R8(OperandType.R8, OperandType.R8),

	/** The list of operands made of an 8-bit general-purpose register and a 16-bit general-purpose register. */
	R8_R16(OperandType.R8, OperandType.R16),

	/** The list of operands made of a 16-bit general-purpose register and an 8-bit general-purpose register. */
	R16_R8(OperandType.R16, OperandType.R8),

	/** The list of operands made of a 16-bit general-purpose register and a 16-bit general-purpose register. */
	R16_R16(OperandType.R16, OperandType.R16),

	/** The list of operands made of a 16-bit general-purpose register and a 32-bit general-purpose register. */
	R16_R32(OperandType.R16, OperandType.R32),

	/** The list of operands made of a 32-bit general-purpose register and an 8-bit general-purpose register. */
	R32_R8(OperandType.R32, OperandType.R8),

	/** The list of operands made of a 32-bit general-purpose register and a 16-bit general-purpose register. */
	R32_R16(OperandType.R32, OperandType.R16),

	/** The list of operands made of a 32-bit general-purpose register and a 32-bit general-purpose register. */
	R32_R32(OperandType.R32, OperandType.R32),

	/** The list of operands made of a 32-bit general-purpose register and a 128-bit XMM vector register. */
	R32_RX(OperandType.R32, OperandType.RX),

	/** The list of operands made of a 32-bit general-purpose register and a 256-bit YMM vector register. */
	R32_RY(OperandType.R32, OperandType.RY),

	/** The list of operands made of a 64-bit general-purpose register and an 8-bit general-purpose register. */
	R64_R8(OperandType.R64, OperandType.R8),

	/** The list of operands made of a 64-bit general-purpose register and a 16-bit general-purpose register. */
	R64_R16(OperandType.R64, OperandType.R16),

	/** The list of operands made of a 64-bit general-purpose register and a 32-bit general-purpose register. */
	R64_R32(OperandType.R64, OperandType.R32),

	/** The list of operands made of a 64-bit general-purpose register and a 64-bit general-purpose register. */
	R64_R64(OperandType.R64, OperandType.R64),

	/** The list of operands made of a 64-bit general-purpose register and a 128-bit XMM vector register. */
	R64_RX(OperandType.R64, OperandType.RX),

	/** The list of operands made of a 64-bit general-purpose register and a vector mask register. */
	R64_RK(OperandType.R64, OperandType.RK),

	/** The list of operands made of a 64-bit MMX vector register and a 32-bit general-purpose register. */
	RMM_R32(OperandType.RMM, OperandType.R32),

	/** The list of operands made of a 64-bit MMX vector register and a 64-bit general-purpose register. */
	RMM_R64(OperandType.RMM, OperandType.R64),

	/** The list of operands made of a 64-bit MMX vector register and a 64-bit MMX vector register. */
	RMM_RMM(OperandType.RMM, OperandType.RMM),

	/** The list of operands made of a 128-bit XMM vector register and a 32-bit general-purpose register. */
	RX_R32(OperandType.RX, OperandType.R32),

	/** The list of operands made of a 128-bit XMM vector register and a 64-bit general-purpose register. */
	RX_R64(OperandType.RX, OperandType.R64),

	/** The list of operands made of a 128-bit XMM vector register and a 128-bit XMM vector register. */
	RX_RX(OperandType.RX, OperandType.RX),

	/** The list of operands made of a 256-bit YMM vector register and a 128-bit XMM vector register. */
	RY_RX(OperandType.RY, OperandType.RX),

	/** The list of operands made of a 512-bit ZMM vector register and a 32-bit general-purpose register. */
	RZ_R32(OperandType.RZ, OperandType.R32),

	/** The list of operands made of a 512-bit ZMM vector register and a 128-bit XMM vector register. */
	RZ_RX(OperandType.RZ, OperandType.RX),

	/** The list of operands made of a 32-bit general-purpose register and a vector mask register. */
	R32_RK(OperandType.R32, OperandType.RK),

	/** The list of operands made of a vector mask register and a 32-bit general-purpose register. */
	RK_R32(OperandType.RK, OperandType.R32),

	/** The list of operands made of a vector mask register and a 64-bit general-purpose register. */
	RK_R64(OperandType.RK, OperandType.R64),

	/** The list of operands made of a vector mask register and a vector mask register. */
	RK_RK(OperandType.RK, OperandType.RK),

	/** The list of operands made of an 8-bit general-purpose register and an 8-bit immediate value. */
	R8_I8(OperandType.R8, OperandType.I8),

	/** The list of operands made of a 16-bit general-purpose register and an 8-bit immediate value. */
	R16_I8(OperandType.R16, OperandType.I8),

	/** The list of operands made of a 16-bit general-purpose register and a 16-bit immediate value. */
	R16_I16(OperandType.R16, OperandType.I16),

	/** The list of operands made of a 32-bit general-purpose register and an 8-bit immediate value. */
	R32_I8(OperandType.R32, OperandType.I8),

	/** The list of operands made of a 32-bit general-purpose register and a 32-bit immediate value. */
	R32_I32(OperandType.R32, OperandType.I32),

	/** The list of operands made of a 64-bit general-purpose register and an 8-bit immediate value. */
	R64_I8(OperandType.R64, OperandType.I8),

	/** The list of operands made of a 64-bit general-purpose register and a 32-bit immediate value. */
	R64_I32(OperandType.R64, OperandType.I32),

	/** The list of operands made of a 64-bit general-purpose register and a 64-bit immediate value. */
	R64_I64(OperandType.R64, OperandType.I64),

	/** The list of operands made of a 128-bit XMM vector register and an 8-bit immediate value. */
	RX_I8(OperandType.RX, OperandType.I8),

	/**
	 * The list of operands made of an 8-bit general-purpose register and a segmented address with a 64-bit immediate
	 * value.
	 */
	R8_S64(OperandType.R8, OperandType.S64),

	/**
	 * The list of operands made of a 32-bit general-purpose register and a segmented address with a 64-bit immediate
	 * value.
	 */
	R32_S64(OperandType.R32, OperandType.S64),

	/**
	 * The list of operands made of a segmented address with a 64-bit immediate value and an 8-bit general-purpose
	 * register.
	 */
	S64_R8(OperandType.S64, OperandType.R8),

	/**
	 * The list of operands made of a segmented address with a 64-bit immediate value and a 32-bit general-purpose
	 * register.
	 */
	S64_R32(OperandType.S64, OperandType.R32),

	/** The list of operands made of a BYTE PTR indirect operand and an 8-bit immediate value. */
	M8_I8(OperandType.M8, OperandType.I8),

	/** The list of operands made of a WORD PTR indirect operand and an 8-bit immediate value. */
	M16_I8(OperandType.M16, OperandType.I8),

	/** The list of operands made of a WORD PTR indirect operand and a 16-bit immediate value. */
	M16_I16(OperandType.M16, OperandType.I16),

	/** The list of operands made of a DWORD PTR indirect operand and an 8-bit immediate value. */
	M32_I8(OperandType.M32, OperandType.I8),

	/** The list of operands made of a DWORD PTR indirect operand and a 32-bit immediate value. */
	M32_I32(OperandType.M32, OperandType.I32),

	/** The list of operands made of a QWORD PTR indirect operand and an 8-bit immediate value. */
	M64_I8(OperandType.M64, OperandType.I8),

	/** The list of operands made of a QWORD PTR indirect operand and a 32-bit immediate value. */
	M64_I32(OperandType.M64, OperandType.I32),

	/** The list of operands made of a BYTE PTR indirect operand and a BYTE PTR indirect operand. */
	M8_M8(OperandType.M8, OperandType.M8),

	/** The list of operands made of a WORD PTR indirect operand and a WORD PTR indirect operand. */
	M16_M16(OperandType.M16, OperandType.M16),

	/** The list of operands made of a DWORD PTR indirect operand and a DWORD PTR indirect operand. */
	M32_M32(OperandType.M32, OperandType.M32),

	/** The list of operands made of a BYTE PTR indirect operand and an 8-bit general-purpose register. */
	M8_R8(OperandType.M8, OperandType.R8),

	/** The list of operands made of a BYTE PTR indirect operand and a 16-bit general-purpose register. */
	M8_R16(OperandType.M8, OperandType.R16),

	/** The list of operands made of a WORD PTR indirect operand and a 16-bit general-purpose register. */
	M16_R16(OperandType.M16, OperandType.R16),

	/** The list of operands made of a WORD PTR indirect operand and a 16-bit segment register. */
	M16_RS(OperandType.M16, OperandType.RS),

	/** The list of operands made of a DWORD PTR indirect operand and an 8-bit general-purpose register. */
	M32_R8(OperandType.M32, OperandType.R8),

	/** The list of operands made of a DWORD PTR indirect operand and a 16-bit general-purpose register. */
	M32_R16(OperandType.M32, OperandType.R16),

	/** The list of operands made of a DWORD PTR indirect operand and a 32-bit general-purpose register. */
	M32_R32(OperandType.M32, OperandType.R32),

	/** The list of operands made of a QWORD PTR indirect operand and an 8-bit general-purpose register. */
	M64_R8(OperandType.M64, OperandType.R8),

	/** The list of operands made of a QWORD PTR indirect operand and a 64-bit general-purpose register. */
	M64_R64(OperandType.M64, OperandType.R64),

	/** The list of operands made of a QWORD PTR indirect operand and a 128-bit XMM vector register. */
	M64_RX(OperandType.M64, OperandType.RX),

	/** The list of operands made of a XMMWORD PTR indirect operand and a 128-bit XMM vector register. */
	M128_RX(OperandType.M128, OperandType.RX),

	/** The list of operands made of a YMMWORD PTR indirect operand and a 256-bit YMM vector register. */
	M256_RY(OperandType.M256, OperandType.RY),

	/** The list of operands made of a ZMMWORD PTR indirect operand and a 512-bit ZMM vector register. */
	M512_RZ(OperandType.M512, OperandType.RZ),

	/** The list of operands made of an 8-bit general-purpose register and a BYTE PTR indirect operand. */
	R8_M8(OperandType.R8, OperandType.M8),

	/** The list of operands made of a 16-bit general-purpose register and a BYTE PTR indirect operand. */
	R16_M8(OperandType.R16, OperandType.M8),

	/** The list of operands made of a 16-bit general-purpose register and a WORD PTR indirect operand. */
	R16_M16(OperandType.R16, OperandType.M16),

	/** The list of operands made of a 16-bit general-purpose register and a DWORD PTR indirect operand. */
	R16_M32(OperandType.R16, OperandType.M32),

	/** The list of operands made of a 32-bit general-purpose register and a BYTE PTR indirect operand. */
	R32_M8(OperandType.R32, OperandType.M8),

	/** The list of operands made of a 32-bit general-purpose register and a WORD PTR indirect operand. */
	R32_M16(OperandType.R32, OperandType.M16),

	/** The list of operands made of a 32-bit general-purpose register and a DWORD PTR indirect operand. */
	R32_M32(OperandType.R32, OperandType.M32),

	/** The list of operands made of a 64-bit general-purpose register and a BYTE PTR indirect operand. */
	R64_M8(OperandType.R64, OperandType.M8),

	/** The list of operands made of a 64-bit general-purpose register and a WORD PTR indirect operand. */
	R64_M16(OperandType.R64, OperandType.M16),

	/** The list of operands made of a 64-bit general-purpose register and a DWORD PTR indirect operand. */
	R64_M32(OperandType.R64, OperandType.M32),

	/** The list of operands made of a 64-bit general-purpose register and a QWORD PTR indirect operand. */
	R64_M64(OperandType.R64, OperandType.M64),

	/** The list of operands made of a 64-bit MMX vector register and a QWORD PTR indirect operand. */
	RMM_M64(OperandType.RMM, OperandType.M64),

	/** The list of operands made of a 128-bit XMM vector register and a DWORD PTR indirect operand. */
	RX_M32(OperandType.RX, OperandType.M32),

	/** The list of operands made of a 128-bit XMM vector register and a QWORD PTR indirect operand. */
	RX_M64(OperandType.RX, OperandType.M64),

	/** The list of operands made of a 128-bit XMM vector register and a XMMWORD PTR indirect operand. */
	RX_M128(OperandType.RX, OperandType.M128),

	/** The list of operands made of a 256-bit YMM vector register and a YMMWORD PTR indirect operand. */
	RY_M256(OperandType.RY, OperandType.M256),

	/** The list of operands made of a 512-bit ZMM vector register and a ZMMWORD PTR indirect operand. */
	RZ_M512(OperandType.RZ, OperandType.M512),

	/** The list of operands made of a 16-bit segment register and a WORD PTR indirect operand. */
	RS_M16(OperandType.RS, OperandType.M16),

	/** The list of operands made of an 8-bit immediate value and an 8-bit general-purpose register. */
	I8_R8(OperandType.I8, OperandType.R8),

	/** The list of operands made of an 8-bit immediate value and a 32-bit general-purpose register. */
	I8_R32(OperandType.I8, OperandType.R32),

	/**
	 * The list of operands made of a 32-bit general-purpose register, a 32-bit general-purpose register and an 8-bit
	 * immediate value.
	 */
	R32_R32_I8(OperandType.R32, OperandType.R32, OperandType.I8),

	/**
	 * The list of operands made of a 32-bit general-purpose register, a 32-bit general-purpose register and a 32-bit
	 * immediate value.
	 */
	R32_R32_I32(OperandType.R32, OperandType.R32, OperandType.I32),

	/**
	 * The list of operands made of a 64-bit general-purpose register, a 64-bit general-purpose register and a 32-bit
	 * immediate value.
	 */
	R64_R64_I32(OperandType.R64, OperandType.R64, OperandType.I32),

	/**
	 * The list of operands made of a 32-bit general-purpose register, a 64-bit MMX vector register and an 8-bit
	 * immediate value.
	 */
	R32_RMM_I8(OperandType.R32, OperandType.RMM, OperandType.I8),

	/**
	 * The list of operands made of a 64-bit general-purpose register, a 64-bit general-purpose register and an 8-bit
	 * immediate value.
	 */
	R64_R64_I8(OperandType.R64, OperandType.R64, OperandType.I8),

	/**
	 * The list of operands made of a 64-bit MMX vector register, a 64-bit MMX vector register and an 8-bit immediate
	 * value.
	 */
	RMM_RMM_I8(OperandType.RMM, OperandType.RMM, OperandType.I8),

	/**
	 * The list of operands made of a 128-bit XMM vector register, a 128-bit XMM vector register and an 8-bit immediate
	 * value.
	 */
	RX_RX_I8(OperandType.RX, OperandType.RX, OperandType.I8),

	/**
	 * The list of operands made of a 128-bit XMM vector register, a XMMWORD PTR indirect operand and an 8-bit immediate
	 * value.
	 */
	RX_M128_I8(OperandType.RX, OperandType.M128, OperandType.I8),

	/**
	 * The list of operands made of a 32-bit general-purpose register, a DWORD PTR indirect operand and a 32-bit
	 * immediate value.
	 */
	R32_M32_I32(OperandType.R32, OperandType.M32, OperandType.I32),

	/**
	 * The list of operands made of a 64-bit general-purpose register, a QWORD PTR indirect operand and a 32-bit
	 * immediate value.
	 */
	R64_M64_I32(OperandType.R64, OperandType.M64, OperandType.I32),

	/**
	 * The list of operands made of a 32-bit general-purpose register, a 32-bit general-purpose register and a 32-bit
	 * general-purpose register.
	 */
	R32_R32_R32(OperandType.R32, OperandType.R32, OperandType.R32),

	/**
	 * The list of operands made of a 64-bit general-purpose register, a 64-bit general-purpose register and an 8-bit
	 * general-purpose register.
	 */
	R64_R64_R8(OperandType.R64, OperandType.R64, OperandType.R8),

	/**
	 * The list of operands made of a 64-bit general-purpose register, a 64-bit general-purpose register and a 64-bit
	 * general-purpose register.
	 */
	R64_R64_R64(OperandType.R64, OperandType.R64, OperandType.R64),

	/**
	 * The list of operands made of a vector mask register, a 128-bit XMM vector register and a XMMWORD PTR indirect
	 * operand.
	 */
	RK_RX_M128(OperandType.RK, OperandType.RX, OperandType.M128),

	/**
	 * The list of operands made of a vector mask register, a 256-bit YMM vector register and a YMMWORD PTR indirect
	 * operand.
	 */
	RK_RY_M256(OperandType.RK, OperandType.RY, OperandType.M256),

	/**
	 * The list of operands made of a 128-bit XMM vector register, a 128-bit XMM vector register and a 128-bit XMM
	 * vector register.
	 */
	RX_RX_RX(OperandType.RX, OperandType.RX, OperandType.RX),

	/**
	 * The list of operands made of a 128-bit XMM vector register, a 128-bit XMM vector register and a XMMWORD PTR
	 * indirect operand.
	 */
	RX_RX_M128(OperandType.RX, OperandType.RX, OperandType.M128),

	/**
	 * The list of operands made of a 256-bit YMM vector register, a 256-bit YMM vector register and a 256-bit YMM
	 * vector register.
	 */
	RY_RY_RY(OperandType.RY, OperandType.RY, OperandType.RY),

	/**
	 * The list of operands made of a vector mask register, a 128-bit XMM vector register and a 128-bit XMM vector
	 * register.
	 */
	RK_RX_RX(OperandType.RK, OperandType.RX, OperandType.RX),

	/**
	 * The list of operands made of a vector mask register, a 256-bit YMM vector register and a 256-bit YMM vector
	 * register.
	 */
	RK_RY_RY(OperandType.RK, OperandType.RY, OperandType.RY),

	/** The list of operands made of a vector mask register, a vector mask register and a vector mask register. */
	RK_RK_RK(OperandType.RK, OperandType.RK, OperandType.RK),

	/**
	 * The list of operands made of a 256-bit YMM vector register, a 256-bit YMM vector register and a YMMWORD PTR
	 * indirect operand.
	 */
	RY_RY_M256(OperandType.RY, OperandType.RY, OperandType.M256),

	/**
	 * The list of operands made of a 128-bit XMM vector register, a 128-bit XMM vector register, a XMMWORD PTR indirect
	 * operand and an 8-bit immediate value.
	 */
	RX_RX_M128_I8(OperandType.RX, OperandType.RX, OperandType.M128, OperandType.I8),

	/**
	 * The list of operands made of a 256-bit YMM vector register, a 256-bit YMM vector register, a YMMWORD PTR indirect
	 * operand and an 8-bit immediate value.
	 */
	RY_RY_M256_I8(OperandType.RY, OperandType.RY, OperandType.M256, OperandType.I8),

	/**
	 * The list of operands made of a 256-bit YMM vector register, a 256-bit YMM vector register, a 256-bit YMM vector
	 * register and an 8-bit immediate value.
	 */
	RY_RY_RY_I8(OperandType.RY, OperandType.RY, OperandType.RY, OperandType.I8);

	private final OperandType[] opt;

	/* default */ OperandTypeList(final OperandType... ot) {
		Objects.requireNonNull(ot);
		final int maxOperands = 4;
		if (ot.length > maxOperands) {
			throw new IllegalArgumentException("Too many operand types.");
		}
		this.opt = new OperandType[ot.length];
		for (int i = 0; i < ot.length; i++) {
			this.opt[i] = Objects.requireNonNull(ot[i], "Null operand type.");
		}
	}

	/**
	 * Returns the number of operand types.
	 *
	 * @return The number of operand types.
	 */
	public int numOperands() {
		return this.opt.length;
	}

	/**
	 * Returns the i-th operand type.
	 *
	 * @param idx The index of the operand type.
	 * @return The i-th operand type.
	 */
	public OperandType operandType(final int idx) {
		return this.opt[idx];
	}
}
