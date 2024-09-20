/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ledmington.utils.BitUtils;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
final class TestPermissions {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    private MemoryController mem;

    @BeforeEach
    void setup() {
        mem = new MemoryController(MemoryInitializer.random());
    }

    @Test
    void cantReadByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(MemoryException.class, () -> mem.read(address));
        }
    }

    @Test
    void cantExecuteByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(MemoryException.class, () -> mem.readCode(address));
        }
    }

    @Test
    void cantWriteByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(MemoryException.class, () -> mem.write(address, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canRead() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, false, false);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
            assertThrows(MemoryException.class, () -> mem.readCode(finalI));
            assertThrows(MemoryException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canWrite() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, true, false);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
            assertThrows(MemoryException.class, () -> mem.read(finalI));
            assertThrows(MemoryException.class, () -> mem.readCode(finalI));
        }
    }

    @Test
    void canReadAndWrite() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, true, false);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
            assertThrows(MemoryException.class, () -> mem.readCode(finalI));
        }
    }

    @Test
    void canExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, false, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertThrows(MemoryException.class, () -> mem.read(finalI));
            assertThrows(MemoryException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canReadAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, false, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertThrows(MemoryException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, true, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
            assertThrows(MemoryException.class, () -> mem.read(finalI));
        }
    }

    @Test
    void canReadWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, true, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }
}
