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
package com.ledmington.elf.section;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.elf.section.gnu.GnuHashSection;

final class TestGnuHash {

    private static Stream<Arguments> knownHashes() {
        return Stream.of(
                Arguments.of("a", 0x0002b606),
                Arguments.of("__libc_start_main", 0xf63d4e2e),
                Arguments.of("pthread_mutex_lock", 0x4f152227),
                Arguments.of("strcasecmp", 0xb3850d3a));
    }

    @ParameterizedTest
    @MethodSource("knownHashes")
    void testKnownHashes(final String symbolname, final int hash) {
        final int actual = GnuHashSection.hash(symbolname.getBytes(StandardCharsets.UTF_8));
        assertEquals(hash, actual, () -> String.format("Expected 0x%08x but was 0x%08x", hash, actual));
    }
}
