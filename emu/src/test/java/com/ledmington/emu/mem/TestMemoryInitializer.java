package com.ledmington.emu.mem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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
                LongStream.range(0, 100)
                                .mapToObj(mem::read)
                                .collect(Collectors.toSet())
                                .size()
                        > 1,
                () -> "mem.read() returned always the same value");
    }
}
