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
package com.ledmington.emu;

/**
 * Symbolic values for the entries in the auxiliary table put on the initial stack.
 *
 * <p>Reference <a href="https://github.com/torvalds/linux/blob/v3.19/include/uapi/linux/auxvec.h">here</a> or <a
 * href="https://refspecs.linuxfoundation.org/LSB_2.1.0/LSB-Core-IA64/LSB-Core-IA64/auxiliaryvector.html">here</a>.
 */
public enum AuxiliaryEntryType {

	/** End of vector. */
	AT_NULL(0),

	/** Entry should be ignored. */
	AT_IGNORE(1),

	/** File descriptor of program. */
	AT_EXECFD(2),

	/** Program headers for program. */
	AT_PHDR(3),

	/** Size of program header entry. */
	AT_PHENT(4),

	/** Number of program headers. */
	AT_PHNUM(5),

	/** System page size. */
	AT_PAGESZ(6),

	/** Base address of interpreter. */
	AT_BASE(7),

	/** Flags. */
	AT_FLAGS(8),

	/** Entry point of program. */
	AT_ENTRY(9),

	/** Program is not ELF. */
	AT_NOTELF(10),

	/** Real uid. */
	AT_UID(11),

	/** Effective uid. */
	AT_EUID(12),

	/** Real gid. */
	AT_GID(13),

	/** Effective gid. */
	AT_EGID(14),

	/** String identifying CPU for optimizations. */
	AT_PLATFORM(15),

	/** Arch dependent hints at CPU capabilities. */
	AT_HWCAP(16),

	/** Frequency at which times() increments. */
	AT_CLKTCK(17),

	/* AT_* values 18 through 22 are reserved */

	/** Secure mode boolean. */
	AT_SECURE(23),

	/** String identifying real platform, may differ from AT_PLATFORM. */
	AT_BASE_PLATFORM(24),

	/** Address of 16 random bytes. */
	AT_RANDOM(25),

	/** Extension of AT_HWCAP. */
	AT_HWCAP2(26),

	/** filename of program. */
	AT_EXECFN(31);

	private final long code;

	AuxiliaryEntryType(final long code) {
		if (code < 0L || (code > 26L && code < 31L) || code > 31L) {
			throw new IllegalArgumentException(String.format("Invalid code: %,d.", code));
		}
		this.code = code;
	}

	/**
	 * Returns the code of this type.
	 *
	 * @return The code of this type.
	 */
	public long getCode() {
		return code;
	}
}
