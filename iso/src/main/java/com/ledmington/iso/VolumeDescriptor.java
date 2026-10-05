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
package com.ledmington.iso;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.utils.BinaryReader;

public final class VolumeDescriptor {

	private final VolumeDescriptorType type;
	private final byte[] data = new byte[2_041];

	public VolumeDescriptor(final VolumeDescriptorType type, final byte[] data) {
		this.type = Objects.requireNonNull(type);
		Objects.requireNonNull(data);
		if (data.length != this.data.length) {
			throw new AssertionError("Wrong volume descriptor data length.");
		}
		System.arraycopy(data, 0, this.data, 0, this.data.length);
	}

	public static VolumeDescriptor read(final BinaryReader reader) {
		final VolumeDescriptorType type = VolumeDescriptorType.fromByte(reader.read1());

		final byte[] identifier = new byte[5];
		for (int i = 0; i < identifier.length; i++) {
			identifier[i] = reader.read1();
		}
		final byte[] expectedIdentifier = "CD001".getBytes(StandardCharsets.UTF_8);
		if (!Arrays.equals(identifier, expectedIdentifier)) {
			throw new AssertionError(String.format(
					"Expected identifier part to be 'CD001' (%s) but was %s.",
					IntStream.range(0, expectedIdentifier.length)
							.mapToObj(i -> String.format("0x%02x", expectedIdentifier[i]))
							.collect(Collectors.joining(" ")),
					IntStream.range(0, identifier.length)
							.mapToObj(i -> String.format("0x%02x", identifier[i]))
							.collect(Collectors.joining(" "))));
		}

		final byte version = reader.read1();
		final byte expectedVersion = 0x01;
		if (version != expectedVersion) {
			throw new AssertionError(
					String.format("Expected version part to be 0x%02x but was 0x%02x.", expectedVersion, version));
		}

		final byte[] data = new byte[2_041];
		for (int i = 0; i < data.length; i++) {
			data[i] = reader.read1();
		}

		return new VolumeDescriptor(type, data);
	}

	public VolumeDescriptorType getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}
}
