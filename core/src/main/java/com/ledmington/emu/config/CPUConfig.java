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
package com.ledmington.emu.config;

import java.util.Objects;

/**
 * CPU configuration. Can be queried to answer CPUID. Reference: <a
 * href="https://github.com/util-linux/util-linux/blob/master/sys-utils/lscpu.c">here</a>. Useful program: <a
 * href="https://godbolt.org/z/1z5fj9M8f">here</a>.
 */
// TODO: can this be an enum with some factory methods?
public final class CPUConfig {

	private static final int VALUES_FOR_EACH_LEAF = 4;

	/** The CPU config of a generic Intel processor. */
	public static final CPUConfig GENERIC_INTEL = new CPUConfig(
			new int[] {
				1,
				0x47656e75, // 'Genu'
				0x696e6549, // 'ineI'
				0x6e74656c, // 'ntel'
			},
			new int[] {0x000406f1, 0x01020800, 0xfffa3203, 0x178bfbff});

	private final int[][] leaves;

	private CPUConfig(final int[]... leaves) {
		Objects.requireNonNull(leaves);
		for (final int[] leaf : leaves) {
			Objects.requireNonNull(leaf);
			if (leaf.length != VALUES_FOR_EACH_LEAF) {
				throw new AssertionError();
			}
		}
		this.leaves = leaves;
	}

	/**
	 * The maximum standard CPUID leaf supported.
	 *
	 * @return The maximum standard CPUID leaf supported.
	 */
	public int getMaxSupportedStandardLeaf() {
		return this.leaves.length;
	}

	/**
	 * Returns the values for EAX, EBX, ECX and EDX. In a C fashion, values is an output parameter.
	 *
	 * @param eax The value of eax, meaning the requested leaf.
	 * @param values The pre-allocated array of values (must be of length 4) where to place the result.
	 */
	public void setLeafValues(final int eax, final int... values) {
		if (values.length != VALUES_FOR_EACH_LEAF) {
			throw new IllegalArgumentException();
		}
		System.arraycopy(this.leaves[eax], 0, values, 0, VALUES_FOR_EACH_LEAF);
	}
}
