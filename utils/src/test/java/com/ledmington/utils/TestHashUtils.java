package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

final class TestHashUtils {
    @Test
    void hashBooleans() {
        final int f = HashUtils.hash(false);
        final int t = HashUtils.hash(true);
        assertNotEquals(
                f,
                t,
                () -> String.format("Expected hashes of booleans to be different but were 0x%08x and 0x%08x", f, t));
    }

    @Test
    void hashBytes() {
        final Set<Integer> v = new HashSet<>();
        for (int i = 0; i < 256; i++) {
            v.add(HashUtils.hash(BitUtils.asByte(i)));
        }
        assertEquals(256, v.size(), () -> String.format("Expected to have 256 unique values but were %,d", v.size()));
    }

    @Test
    void hashShorts() {
        final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
        final Set<Integer> v = new HashSet<>();
        final short x = BitUtils.asShort(rng.nextInt());
        final int limit = 100;
        for (int i = 0; i < limit; i++) {
            v.add(HashUtils.hash(BitUtils.asShort(x + BitUtils.asShort(i))));
        }
        assertEquals(
                limit,
                v.size(),
                () -> String.format("Expected to have %,d unique values but were %,d", limit, v.size()));
    }

    @Test
    void hashLongs() {
        final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
        final Set<Integer> v = new HashSet<>();
        final long x = rng.nextLong();
        final int limit = 100;
        for (int i = 0; i < limit; i++) {
            v.add(HashUtils.hash(x + BitUtils.asLong(i)));
        }
        assertEquals(
                limit,
                v.size(),
                () -> String.format("Expected to have %,d unique values but were %,d", limit, v.size()));
    }
}
