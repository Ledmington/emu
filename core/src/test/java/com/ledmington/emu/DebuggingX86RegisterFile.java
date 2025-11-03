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
package com.ledmington.emu;

import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;

import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.utils.BitUtils;

/**
 * A wrapper class around the usual x86 register file object which throws an exception when reading an uninitialized
 * register.
 *
 * <p>To be used for testing purposes only.
 */
public final class DebuggingX86RegisterFile implements RegisterFile {

	private final RegisterFile rf = new X86RegisterFile();
	private final Set<Register> initialized = new HashSet<>();

	public DebuggingX86RegisterFile(final RandomGenerator rng) {
		// set registers to random values
		for (final Register64 r : Register64.values()) {
			this.rf.set(r, rng.nextLong());
		}
		for (final SegmentRegister r : SegmentRegister.values()) {
			this.rf.set(r, BitUtils.asShort(rng.nextInt()));
		}
	}

	private void checkIsInitialized(final Register r) {
		if (!initialized.contains(r)) {
			throw new IllegalArgumentException(String.format("Register '%s' was used uninitialized.", r));
		}
	}

	@Override
	public void set(final Register8 r, final byte v) {
		this.initialized.add(r);
		this.rf.set(r, v);
	}

	@Override
	public void set(final Register16 r, final short v) {
		this.initialized.add(r);
		this.rf.set(r, v);
	}

	@Override
	public void set(final Register32 r, final int v) {
		this.initialized.add(r);
		this.rf.set(r, v);
	}

	@Override
	public void set(final Register64 r, final long v) {
		this.initialized.add(r);
		this.rf.set(r, v);
	}

	@Override
	public void set(final SegmentRegister r, final short v) {
		this.initialized.add(r);
		this.rf.set(r, v);
	}

	@Override
	public void set(final RFlags f, final boolean v) {
		this.rf.set(f, v);
	}

	@Override
	public void resetFlags() {
		this.rf.resetFlags();
	}

	@Override
	public byte get(final Register8 r) {
		checkIsInitialized(r);
		return this.rf.get(r);
	}

	@Override
	public short get(final Register16 r) {
		checkIsInitialized(r);
		return this.rf.get(r);
	}

	@Override
	public int get(final Register32 r) {
		checkIsInitialized(r);
		return this.rf.get(r);
	}

	@Override
	public long get(final Register64 r) {
		checkIsInitialized(r);
		return this.rf.get(r);
	}

	@Override
	public short get(final SegmentRegister r) {
		checkIsInitialized(r);
		return this.rf.get(r);
	}

	@Override
	public boolean isSet(final RFlags f) {
		return this.rf.isSet(f);
	}
}
