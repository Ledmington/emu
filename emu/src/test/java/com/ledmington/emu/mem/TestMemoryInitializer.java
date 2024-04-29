package com.ledmington.emu.mem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public final class TestMemoryInitializer {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    @Test
    public void zero() {
        final Memory mem = new RandomAccessMemory(MemoryInitializer.zero());
        for (int i = 0; i < 100; i++) {
            assertEquals((byte) 0x00, mem.read(rng.nextLong()));
        }
    }

    @Test
    public void random() {
        final Memory mem = new RandomAccessMemory(MemoryInitializer.random());
        assertTrue(
                Stream.generate(() -> rng.nextLong())
                                .map(x -> mem.read(x))
                                .limit(100)
                                .collect(Collectors.toSet())
                                .size()
                        > 1,
                () -> "mem.read() returned always the same value");
    }
}