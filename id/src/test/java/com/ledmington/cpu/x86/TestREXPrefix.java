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
        assertEquals(w, rp.w());
        assertEquals(r, rp.r());
        assertEquals(x, rp.x());
        assertEquals(b, rp.b());
    }
}
