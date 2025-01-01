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
package com.ledmington.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A class to contain list of ranges of boolean values. */
// TODO: this can be optimized into a segment tree.
public final class IntervalArray {

	private record Block(long start, long end) {
		private Block {
			if (end < start) {
				throw new IllegalArgumentException(
						String.format("Invalid start (0x%x) and end (0x%x) of a block", start, end));
			}
		}
	}

	private final List<Block> blocks = new ArrayList<>();

	/** Creates an empty IntervalArray. */
	public IntervalArray() {}

	/**
	 * Returns the value stored at the given address.
	 *
	 * @param address The address of the value.
	 * @return True is the value is present, false otherwise.
	 */
	public boolean get(final long address) {
		// TODO: can be optimized in a binary search
		for (final Block b : blocks) {
			if (address >= b.start() && address <= b.end()) {
				return true;
			}
		}
		return false;
	}

	private void sortBlocks() {
		// sorting by the starting address is sufficient
		blocks.sort(Comparator.comparingLong(Block::start));
	}

	private void mergeBlocks() {
		int i = 0;
		while (i < blocks.size() - 1) {
			final Block curr = blocks.get(i);
			final Block next = blocks.get(i + 1);
			if (curr.end() >= next.start()) {
				// overlapping
				blocks.remove(i);
				blocks.remove(i);
				blocks.add(i, new Block(curr.start(), next.end()));
			} else {
				i++;
			}
		}
	}

	/**
	 * Sets the boolean value at the given address to true. Does not throw exceptions in case it is already true. It is
	 * equivalent to calling {@code set(address, address)}.
	 *
	 * @param address The address of the value.
	 */
	public void set(final long address) {
		set(address, address);
	}

	/**
	 * Sets the boolean values in the given range to true. Does not throw exceptions in case it is already true.
	 *
	 * @param startAddress The start (inclusive) of the range.
	 * @param endAddress The end (inclusive) of the range.
	 */
	public void set(final long startAddress, final long endAddress) {
		blocks.add(new Block(startAddress, endAddress));
		sortBlocks();
		mergeBlocks();
	}

	/**
	 * Sets the boolean value at the given address to false. Does not throw exceptions in case it is already false. It
	 * is equivalent to calling {@code reset(address, address)}.
	 *
	 * @param address The address of the value.
	 */
	public void reset(final long address) {
		reset(address, address);
	}

	/**
	 * Sets the boolean values in the given range to false. Does not throw exceptions in case it is already false.
	 *
	 * @param startAddress The start (inclusive) of the range.
	 * @param endAddress The end (inclusive) of the range.
	 */
	public void reset(final long startAddress, final long endAddress) {
		int i = 0;
		while (i < blocks.size()) {
			final Block curr = blocks.get(i);
			if (curr.start() <= startAddress) {

				if (curr.end() >= endAddress) {
					// the given range fits entirely in this block
					blocks.remove(i);
					blocks.add(i, new Block(curr.start(), startAddress - 1));
					blocks.add(i + 1, new Block(endAddress + 1, curr.end()));
				} else {
					// the given range takes up only the right "half" of this block
					blocks.set(i, new Block(curr.start(), startAddress - 1));
				}
				i++;
			} else {
				if (curr.end() >= endAddress) {
					// the given range takes up only the left "half" of this block
					blocks.set(i, new Block(endAddress + 1, curr.end()));
					i++;
				} else {
					// the given range completely contains this block
					blocks.remove(i);
				}
			}
		}
		sortBlocks();
		mergeBlocks();
	}
}
