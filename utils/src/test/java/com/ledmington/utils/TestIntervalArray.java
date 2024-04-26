package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestIntervalArray {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    private IntervalArray ia;

    @BeforeEach
    public void setup() {
        ia = new IntervalArray();
    }

    @AfterEach
    public void teardown() {
        ia = null;
    }

    @Test
    public void initiallyAllFalse() {
        Stream.generate(() -> rng.nextLong()).distinct().limit(100).forEach(x -> {
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", x));
        });
    }

    @Test
    public void setAndGet() {
        ia.reset(Long.MIN_VALUE, Long.MAX_VALUE);
        Stream.generate(() -> rng.nextLong()).distinct().limit(100).forEach(x -> {
            ia.set(x);
            assertTrue(ia.get(x), () -> String.format("Value at address 0x%016x was false", x));
        });
    }

    @Test
    public void setTwiceAndGet() {
        ia.reset(Long.MIN_VALUE, Long.MAX_VALUE);
        Stream.generate(() -> rng.nextLong()).distinct().limit(100).forEach(x -> {
            ia.set(x);
            ia.set(x);
            assertTrue(ia.get(x), () -> String.format("Value at address 0x%016x was false", x));
        });
    }

    @Test
    public void resetAndGet() {
        ia.set(Long.MIN_VALUE, Long.MAX_VALUE);
        final List<Long> addresses =
                Stream.generate(() -> rng.nextLong()).distinct().limit(100).toList();
        for (final long x : addresses) {
            ia.reset(x);
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", x));
        }
    }

    @Test
    public void resetTwiceAndGet() {
        ia.set(Long.MIN_VALUE, Long.MAX_VALUE);
        final List<Long> addresses =
                Stream.generate(() -> rng.nextLong()).distinct().limit(100).toList();
        for (final long x : addresses) {
            ia.reset(x);
            ia.reset(x);
            assertFalse(ia.get(x), () -> String.format("Value at address 0x%016x was true", x));
        }
    }
}
