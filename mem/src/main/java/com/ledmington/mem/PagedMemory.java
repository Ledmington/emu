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
package com.ledmington.mem;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of {@link Memory} which allocates pages (contiguous blocks) of memory instead of single bytes, to
 * optimize sequential accesses.
 */
public final class PagedMemory implements Memory {

	private static final long DEFAULT_PAGE_SIZE = 4096L; // 4 KiB

	private final MemoryInitializer initializer;
	private final long pageSize;
	private final long pageSizeMask;
	private final Map<Long, Page> pages = new ConcurrentHashMap<>();

	private static final class Page {

		private final byte[] bytes;
		private final boolean[] initialized;

		public Page(final long numBytes, final MemoryInitializer initializer) {
			this.bytes = new byte[Math.toIntExact(numBytes)];
			this.initialized = new boolean[Math.toIntExact(numBytes)];
			for (int i = 0; i < numBytes; i++) {
				this.bytes[i] = initializer.get();
			}
		}

		@Override
		public String toString() {
			return "Page(bytes="
					+ IntStream.range(0, bytes.length)
							.mapToObj(i -> String.format("%02x", bytes[i]))
							.collect(Collectors.joining())
					+ ";initialized="
					+ IntStream.range(0, initialized.length)
							.mapToObj(i -> initialized[i] ? "1" : "0")
							.collect(Collectors.joining())
					+ ")";
		}

		@Override
		public int hashCode() {
			int h = 17;
			h = 31 * h + Arrays.hashCode(bytes);
			h = 31 * h + Arrays.hashCode(initialized);
			return h;
		}

		@Override
		public boolean equals(final Object other) {
			if (other == null) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (!(other instanceof final Page p)) {
				return false;
			}
			return Arrays.equals(this.bytes, p.bytes) && Arrays.equals(this.initialized, p.initialized);
		}
	}

	/**
	 * Creates a new {@link PagedMemory} with the given {@link MemoryInitializer} and page size.
	 *
	 * @param initializer The initializer for unaccessed memory addresses.
	 * @param pageSize The size of a single page to be allocated, in bytes.
	 */
	public PagedMemory(final MemoryInitializer initializer, final long pageSize) {
		Objects.requireNonNull(initializer);
		if (pageSize <= 1L || Long.bitCount(pageSize) != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid page size: %,d, must be a power of two >=2.", pageSize));
		}
		this.initializer = initializer;
		this.pageSize = pageSize;
		this.pageSizeMask = -pageSize;
	}

	/**
	 * Creates a new {@link PagedMemory} with the default page size.
	 *
	 * @param initializer The initializer for unaccessed memory addresses.
	 */
	public PagedMemory(final MemoryInitializer initializer) {
		this(initializer, DEFAULT_PAGE_SIZE);
	}

	/** Returns the given address aligned to a page size boundary. */
	private long getAlignedAddress(final long address) {
		return address & this.pageSizeMask;
	}

	/** Returns the given address localized inside a page. */
	private long getLocalAddress(final long address) {
		return address & (~this.pageSizeMask);
	}

	@Override
	public byte read(final long address) {
		// TODO: add overloaded versions of read2, read4 and read8
		final long alignedAddress = getAlignedAddress(address);
		if (!this.pages.containsKey(alignedAddress)) {
			return this.initializer.get();
		}
		final long localAddress = getLocalAddress(address);
		return this.pages.get(alignedAddress).bytes[Math.toIntExact(localAddress)];
	}

	@Override
	public void write(final long address, final byte value) {
		// TODO: add overloaded versions of write2, write4, write8 and writeN
		final long alignedAddress = getAlignedAddress(address);
		if (!this.pages.containsKey(alignedAddress)) {
			this.pages.put(alignedAddress, new Page(pageSize, initializer));
		}
		final long localAddress = getLocalAddress(address);
		this.pages.get(alignedAddress).initialized[Math.toIntExact(localAddress)] = true;
		this.pages.get(alignedAddress).bytes[Math.toIntExact(localAddress)] = value;
	}

	@Override
	public boolean isInitialized(final long address) {
		final long alignedAddress = getAlignedAddress(address);
		if (!this.pages.containsKey(alignedAddress)) {
			return false;
		}
		final long localAddress = getLocalAddress(address);
		return this.pages.get(alignedAddress).initialized[Math.toIntExact(localAddress)];
	}

	@Override
	public String toString() {
		return "PagedMemory(initializer=" + initializer + ";pageSize=" + pageSize + ";pageSizeMask=" + pageSizeMask
				+ ";pages=" + pages + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + initializer.hashCode();
		h = 31 * h + Long.hashCode(pageSize);
		h = 31 * h + Long.hashCode(pageSizeMask);
		h = 31 * h + pages.hashCode();
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final PagedMemory pm)) {
			return false;
		}
		return this.initializer.equals(pm.initializer)
				&& this.pageSize == pm.pageSize
				&& this.pageSizeMask == pm.pageSizeMask
				&& this.pages.equals(pm.pages);
	}
}
