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
package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestREXPrefix {

    static Stream<Arguments> wrongPrefixes() {
        return IntStream.range(0, 256)
                .mapToObj(BitUtils::asByte)
                .filter(x -> BitUtils.and((byte) 0xf0, x) != (byte) 0x40)
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("wrongPrefixes")
    void error(final byte rex) {
        assertFalse(RexPrefix.isREXPrefix(rex));
        assertThrows(IllegalArgumentException.class, () -> new RexPrefix(rex));
    }

    static Stream<Arguments> correctPrefixes() {
        final List<Arguments> args = new ArrayList<>();
        final boolean[] v = new boolean[] {false, true};
        for (final boolean w : v) {
            for (final boolean r : v) {
                for (final boolean x : v) {
                    for (final boolean b : v) {
                        final byte prefix = BitUtils.or(
                                (byte) 0x40,
                                (w ? (byte) 0x08 : 0),
                                (r ? (byte) 0x04 : 0),
                                (x ? (byte) 0x02 : 0),
                                (b ? (byte) 0x01 : 0));

                        args.add(Arguments.of(w, r, x, b, prefix));
                    }
                }
            }
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("correctPrefixes")
    void correct(final boolean w, final boolean r, final boolean x, final boolean b, final byte rex) {
        final RexPrefix rp = new RexPrefix(rex);
        final String expected = (w ? "W" : "") + (r ? "R" : "") + (x ? "X" : "") + (b ? "B" : "");
        final String actual = (rp.w() ? "W" : "") + (rp.r() ? "R" : "") + (rp.x() ? "X" : "") + (rp.b() ? "B" : "");
        assertEquals(
                expected,
                actual,
                () -> String.format("Expected REX prefix 0x%02x to be equal to %s but was %s", rex, expected, actual));
    }
}
