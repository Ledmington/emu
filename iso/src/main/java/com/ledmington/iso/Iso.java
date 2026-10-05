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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Iso {

	private final Sector systemArea[] = new Sector[16];
	private final VolumeDescriptor volumeDescriptors[];

	public Iso(final Sector[] sectors, final List<VolumeDescriptor> volumeDescriptors) {
		Objects.requireNonNull(sectors);
		for (final Sector s : sectors) {
			Objects.requireNonNull(s);
		}
		if (sectors.length != 16) {
			throw new IllegalArgumentException("System area is expected to be made of 16 sectors.");
		}
		System.arraycopy(sectors, 0, this.systemArea, 0, 16);

		Objects.requireNonNull(volumeDescriptors);
		this.volumeDescriptors = new VolumeDescriptor[volumeDescriptors.size()];
		for (int i = 0; i < volumeDescriptors.size(); i++) {
			this.volumeDescriptors[i] = Objects.requireNonNull(volumeDescriptors.get(i));
		}
		if (Arrays.stream(this.volumeDescriptors).noneMatch(vd -> vd.getType().equals(VolumeDescriptorType.PRIMARY))) {
			throw new AssertionError("No primary volume descriptor was found.");
		}
		if (Arrays.stream(this.volumeDescriptors)
				.noneMatch(vd -> vd.getType().equals(VolumeDescriptorType.SET_TERMINATOR))) {
			throw new AssertionError("No set terminator volume descriptor was found.");
		}
	}
}
