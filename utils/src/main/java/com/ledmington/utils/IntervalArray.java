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

/** A class to contain a list of ranges of boolean values. */
// TODO: this can be optimized into a segment tree.
@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
public final class IntervalArray {

	/**
	 * The list of contiguous blocks of "set" values, meaning that a block represents a contiguous region of
	 * {@code true} values.
	 */
	private final List<Block> blocks = new ArrayList<>();

	private record Block(long start, long end) {
		private Block {
			if (end < start) {
				throw new IllegalArgumentException(
						String.format("Invalid start (0x%016x) and end (0x016%x) of a block.", start, end));
			}
		}

		/**
		 * Checks whether this block contains the given index/position. The block boundaries are both inclusive.
		 *
		 * @param index The index to be checked.
		 * @return True if this block contains the given index, false otherwise.
		 */
		public boolean contains(final long index) {
			return index >= start && index <= end;
		}
	}

	/**
	 * Creates a new IntervalArray.
	 *
	 * @param defaultValue If true, creates a "full" array (meaning that all values are set to {@code true}), otherwise
	 *     creates an "empty" array (with all values set to {@code false}).
	 */
	public IntervalArray(final boolean defaultValue) {
		if (defaultValue) {
			blocks.add(new Block(Long.MIN_VALUE, Long.MAX_VALUE));
		}
	}

	/** Creates an empty IntervalArray. Equivalent to {@code new IntervalArray(false)}. */
	public IntervalArray() {
		this(false);
	}

	/**
	 * Returns the value stored at the given address.
	 *
	 * @param address The address of the value.
	 * @return True is the value is present, false otherwise.
	 */
	public boolean get(final long address) {
		// TODO: can be optimized in a binary search, since we keep the array sorted
		for (final Block b : blocks) {
			if (b.contains(address)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the boolean values in the given range to value. Equivalent to calling {@code value ? set(start, numBytes) :
	 * reset(start, numBytes)}.
	 *
	 * @param start The starting address of the range.
	 * @param numBytes The number of consecutive values to be set to the given value.
	 * @param value The value to set the values in the range to.
	 */
	public void set(final long start, final long numBytes, final boolean value) {
		if (numBytes < 0L) {
			throw new IllegalArgumentException("Negative number of bytes.");
		}
		if (numBytes == 0L) {
			return;
		}
		if (value) {
			doSet(start, numBytes);
		} else {
			doReset(start, numBytes);
		}
	}

	/**
	 * Sets the boolean values in the given range to true. Does not throw exceptions in case it is already true.
	 *
	 * @param start The starting address of the range.
	 * @param numBytes The number of consecutive values to be set to true.
	 */
	public void set(final long start, final long numBytes) {
		if (numBytes < 0L) {
			throw new IllegalArgumentException("Negative number of bytes.");
		}
		if (numBytes == 0L) {
			return;
		}
		doSet(start, numBytes);
	}

	/**
	 * Sets the boolean values in the given range to false. Does not throw exceptions in case it is already false.
	 *
	 * @param start The starting address of the range.
	 * @param numBytes The number of consecutive values to be set to false.
	 */
	public void reset(final long start, final long numBytes) {
		if (numBytes < 0L) {
			throw new IllegalArgumentException("Negative number of bytes.");
		}
		if (numBytes == 0L) {
			return;
		}
		doReset(start, numBytes);
	}

	private void doSet(final long start, final long numBytes) {
		blocks.add(new Block(start, start + numBytes - 1L));
		sortAndMerge();
	}

	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	private void doReset(final long start, final long numBytes) {
		final long end = start + numBytes - 1L;
		int i = 0;
		while (i < blocks.size()) {
			final Block curr = blocks.get(i);
			if (curr.start() <= start) {
				if (curr.end() >= end) {
					// the given range fits entirely in this block
					blocks.remove(i);
					if (start > curr.start()) {
						blocks.add(i, new Block(curr.start(), start - 1));
					}
					if (curr.end() > end) {
						blocks.add(i + 1, new Block(end + 1, curr.end()));
					}
				} else {
					// the given range takes up only the right "half" of this block
					blocks.set(i, new Block(curr.start(), start - 1));
				}
				i++;
			} else {
				if (curr.end() >= end) {
					// the given range takes up only the left "half" of this block
					blocks.set(i, new Block(end + 1, curr.end()));
					i++;
				} else {
					// the given range completely contains this block
					blocks.remove(i);
				}
			}
		}
		sortAndMerge();
	}

	private void sortAndMerge() {
		// sorting by the starting address is sufficient
		blocks.sort(Comparator.comparingLong(Block::start));

		int i = 0;
		while (i < blocks.size() - 1) {
			final Block curr = blocks.get(i);
			final Block next = blocks.get(i + 1);

			// we do not use .contains() here because that would not account for a block completely contained within the
			// previous one
			if (curr.end() >= next.start()) {
				// overlapping
				blocks.remove(i);
				blocks.remove(i);
				blocks.add(i, new Block(curr.start(), next.end()));
			} else {
				// we increment i only when the i-th block does not overlap with the (i+1)-th block
				i++;
			}
		}
	}

	@Override
	public String toString() {
		return "IntervalArray(blocks=" + blocks + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + blocks.hashCode();
		return h;
	}

	@Override
	@SuppressWarnings("PMD.SimplifyBooleanReturns")
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final IntervalArray ia)) {
			return false;
		}
		return this.blocks.equals(ia.blocks);
	}
}
