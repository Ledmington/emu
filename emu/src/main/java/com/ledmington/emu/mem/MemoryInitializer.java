package com.ledmington.emu.mem;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import com.ledmington.utils.BitUtils;

/**
 * A procedure to initialize the memory.
 */
public interface MemoryInitializer extends Supplier<Byte> {

    /**
     * Initializes the memory with random values, mimicking the "garbage" values you usually get normally when reading uninitialized memory.
     */
    static MemoryInitializer random() {
        final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
        return () -> BitUtils.asByte(rng.nextInt());
    }

    /**
     * Initializes the memory to all zeroes.
     */
    static MemoryInitializer zero() {
        return () -> (byte) 0x00;
    }
}
