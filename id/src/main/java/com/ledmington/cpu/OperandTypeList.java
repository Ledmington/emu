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
public enum OperandTypeList {
	NO_ARGS(),
	R8(OperandType.R8),
	R16(OperandType.R16),
	R32(OperandType.R32),
	R64(OperandType.R64),
	M8(OperandType.M8),
	M16(OperandType.M16),
	M32(OperandType.M32),
	M64(OperandType.M64),
	I8(OperandType.I8),
	I16(OperandType.I16),
	I32(OperandType.I32),
	I16_I8(OperandType.I16, OperandType.I8),
	R8_R8(OperandType.R8, OperandType.R8),
	R8_R16(OperandType.R8, OperandType.R16),
	R16_R8(OperandType.R16, OperandType.R8),
	R16_R16(OperandType.R16, OperandType.R16),
	R16_R32(OperandType.R16, OperandType.R32),
	R32_R8(OperandType.R32, OperandType.R8),
	R32_R16(OperandType.R32, OperandType.R16),
	R32_R32(OperandType.R32, OperandType.R32),
	R32_RX(OperandType.R32, OperandType.RX),
	R32_RY(OperandType.R32, OperandType.RY),
	R64_R8(OperandType.R64, OperandType.R8),
	R64_R16(OperandType.R64, OperandType.R16),
	R64_R32(OperandType.R64, OperandType.R32),
	R64_R64(OperandType.R64, OperandType.R64),
	R64_RX(OperandType.R64, OperandType.RX),
	R64_RK(OperandType.R64, OperandType.RK),
	RMM_R32(OperandType.RMM, OperandType.R32),
	RMM_R64(OperandType.RMM, OperandType.R64),
	RMM_RMM(OperandType.RMM, OperandType.RMM),
	RX_R32(OperandType.RX, OperandType.R32),
	RX_R64(OperandType.RX, OperandType.R64),
	RX_RX(OperandType.RX, OperandType.RX),
	RY_RX(OperandType.RY, OperandType.RX),
	RZ_R32(OperandType.RZ, OperandType.R32),
	RZ_RX(OperandType.RZ, OperandType.RX),
	R32_RK(OperandType.R32, OperandType.RK),
	RK_R32(OperandType.RK, OperandType.R32),
	RK_R64(OperandType.RK, OperandType.R64),
	RK_RK(OperandType.RK, OperandType.RK),
	R8_I8(OperandType.R8, OperandType.I8),
	R16_I8(OperandType.R16, OperandType.I8),
	R16_I16(OperandType.R16, OperandType.I16),
	R32_I8(OperandType.R32, OperandType.I8),
	R32_I32(OperandType.R32, OperandType.I32),
	R64_I8(OperandType.R64, OperandType.I8),
	R64_I32(OperandType.R64, OperandType.I32),
	R64_I64(OperandType.R64, OperandType.I64),
	RX_I8(OperandType.RX, OperandType.I8),
	R8_S64(OperandType.R8, OperandType.S64),
	R32_S64(OperandType.R32, OperandType.S64),
	S64_R8(OperandType.S64, OperandType.R8),
	S64_R32(OperandType.S64, OperandType.R32),
	M8_I8(OperandType.M8, OperandType.I8),
	M16_I8(OperandType.M16, OperandType.I8),
	M16_I16(OperandType.M16, OperandType.I16),
	M32_I8(OperandType.M32, OperandType.I8),
	M32_I32(OperandType.M32, OperandType.I32),
	M64_I8(OperandType.M64, OperandType.I8),
	M64_I32(OperandType.M64, OperandType.I32),
	M8_M8(OperandType.M8, OperandType.M8),
	M16_M16(OperandType.M16, OperandType.M16),
	M32_M32(OperandType.M32, OperandType.M32),
	M8_R8(OperandType.M8, OperandType.R8),
	M8_R16(OperandType.M8, OperandType.R16),
	M16_R16(OperandType.M16, OperandType.R16),
	M16_RS(OperandType.M16, OperandType.RS),
	M32_R8(OperandType.M32, OperandType.R8),
	M32_R16(OperandType.M32, OperandType.R16),
	M32_R32(OperandType.M32, OperandType.R32),
	M64_R8(OperandType.M64, OperandType.R8),
	M64_R64(OperandType.M64, OperandType.R64),
	M64_RX(OperandType.M64, OperandType.RX),
	M128_RX(OperandType.M128, OperandType.RX),
	M256_RY(OperandType.M256, OperandType.RY),
	M512_RZ(OperandType.M512, OperandType.RZ),
	R8_M8(OperandType.R8, OperandType.M8),
	R16_M8(OperandType.R16, OperandType.M8),
	R16_M16(OperandType.R16, OperandType.M16),
	R16_M32(OperandType.R16, OperandType.M32),
	R32_M8(OperandType.R32, OperandType.M8),
	R32_M16(OperandType.R32, OperandType.M16),
	R32_M32(OperandType.R32, OperandType.M32),
	R64_M8(OperandType.R64, OperandType.M8),
	R64_M16(OperandType.R64, OperandType.M16),
	R64_M32(OperandType.R64, OperandType.M32),
	R64_M64(OperandType.R64, OperandType.M64),
	RMM_M64(OperandType.RMM, OperandType.M64),
	RX_M32(OperandType.RX, OperandType.M32),
	RX_M64(OperandType.RX, OperandType.M64),
	RX_M128(OperandType.RX, OperandType.M128),
	RY_M256(OperandType.RY, OperandType.M256),
	RZ_M512(OperandType.RZ, OperandType.M512),
	RS_M16(OperandType.RS, OperandType.M16),
	I8_R8(OperandType.I8, OperandType.R8),
	I8_R32(OperandType.I8, OperandType.R32),
	R32_R32_I8(OperandType.R32, OperandType.R32, OperandType.I8),
	R32_R32_I32(OperandType.R32, OperandType.R32, OperandType.I32),
	R64_R64_I32(OperandType.R64, OperandType.R64, OperandType.I32),
	R32_RMM_I8(OperandType.R32, OperandType.RMM, OperandType.I8),
	R64_R64_I8(OperandType.R64, OperandType.R64, OperandType.I8),
	RMM_RMM_I8(OperandType.RMM, OperandType.RMM, OperandType.I8),
	RX_RX_I8(OperandType.RX, OperandType.RX, OperandType.I8),
	RX_M128_I8(OperandType.RX, OperandType.M128, OperandType.I8),
	R32_M32_I32(OperandType.R32, OperandType.M32, OperandType.I32),
	R64_M64_I32(OperandType.R64, OperandType.M64, OperandType.I32),
	R32_R32_R32(OperandType.R32, OperandType.R32, OperandType.R32),
	R64_R64_R8(OperandType.R64, OperandType.R64, OperandType.R8),
	R64_R64_R64(OperandType.R64, OperandType.R64, OperandType.R64),
	RK_RX_M128(OperandType.RK, OperandType.RX, OperandType.M128),
	RK_RY_M256(OperandType.RK, OperandType.RY, OperandType.M256),
	RX_RX_RX(OperandType.RX, OperandType.RX, OperandType.RX),
	RX_RX_M128(OperandType.RX, OperandType.RX, OperandType.M128),
	RY_RY_RY(OperandType.RY, OperandType.RY, OperandType.RY),
	RK_RX_RX(OperandType.RK, OperandType.RX, OperandType.RX),
	RK_RY_RY(OperandType.RK, OperandType.RY, OperandType.RY),
	RK_RK_RK(OperandType.RK, OperandType.RK, OperandType.RK),
	RY_RY_M256(OperandType.RY, OperandType.RY, OperandType.M256),
	RX_RX_M128_I8(OperandType.RX, OperandType.RX, OperandType.M128, OperandType.I8),
	RY_RY_M256_I8(OperandType.RY, OperandType.RY, OperandType.M256, OperandType.I8),
	RY_RY_RY_I8(OperandType.RY, OperandType.RY, OperandType.RY, OperandType.I8);

	private final OperandType op1;
	private final OperandType op2;
	private final OperandType op3;
	private final OperandType op4;

	@SuppressWarnings("PMD.NullAssignment")
	/* default */ OperandTypeList(final OperandType... ot) {
		Objects.requireNonNull(ot);
		this.op1 = ot.length > 0 ? Objects.requireNonNull(ot[0]) : null;
		this.op2 = ot.length > 1 ? Objects.requireNonNull(ot[1]) : null;
		this.op3 = ot.length > 2 ? Objects.requireNonNull(ot[2]) : null;
		this.op4 = ot.length > 3 ? Objects.requireNonNull(ot[3]) : null;
		final int maxOperands = 4;
		if (ot.length > maxOperands) {
			throw new IllegalArgumentException("Too many operand types.");
		}
	}

	public int numOperands() {
		if (op4 != null) {
			return 4;
		}
		if (op3 != null) {
			return 3;
		}
		if (op2 != null) {
			return 2;
		}
		if (op1 != null) {
			return 1;
		}
		return 0;
	}

	public OperandType firstOperandType() {
		return op1;
	}

	public OperandType secondOperandType() {
		return op2;
	}

	public OperandType thirdOperandType() {
		return op3;
	}

	public OperandType fourthOperandType() {
		return op4;
	}
}
