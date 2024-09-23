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
package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestIntervalArray {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    private IntervalArray ia;

    @BeforeEach
    public void setup() {
        ia = new IntervalArray();
    }

    @Test
    void initiallyAllFalse() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
        for (final long x : positions) {
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", x));
        }
    }

    private static Stream<Arguments> pairsOfMemoryLocations() {
        return Stream.of(Arguments.of(0L, 0L), Arguments.of(-1L, 1L), Arguments.of(0x1234L, 0x4567L));
    }

    @ParameterizedTest
    @MethodSource("pairsOfMemoryLocations")
    void setAndGet(final long start, final long end) {
        ia.reset(Long.MIN_VALUE, Long.MAX_VALUE);
        ia.set(start, end);
        for (long x = start; x <= end; x++) {
            final long finalX = x;
            assertTrue(ia.get(x), () -> String.format("Value at address 0x%016x was false", finalX));
        }
    }

    @Test
    void setTwiceAndGet() {
        ia.reset(Long.MIN_VALUE, Long.MAX_VALUE);
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
        for (final long x : positions) {
            ia.set(x);
            ia.set(x);
            assertTrue(ia.get(x), () -> String.format("Value at address 0x%016x was false", x));
        }
    }

    @ParameterizedTest
    @MethodSource("pairsOfMemoryLocations")
    void resetAndGet(final long start, final long end) {
        ia.set(Long.MIN_VALUE, Long.MAX_VALUE);
        ia.reset(start, end);
        for (long x = start; x <= end; x++) {
            final long finalX = x;
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", finalX));
        }
    }

    @Test
    void resetTwiceAndGet() {
        ia.set(Long.MIN_VALUE, Long.MAX_VALUE);
        final Set<Long> addresses =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
        for (final long x : addresses) {
            ia.reset(x);
            ia.reset(x);
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", x));
        }
    }
}
