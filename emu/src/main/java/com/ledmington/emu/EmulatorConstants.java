package com.ledmington.emu;

import java.util.Objects;
import java.util.function.Supplier;

import com.ledmington.emu.mem.MemoryInitializer;

public final class EmulatorConstants {

    /**
     * The default memory initializer
     */
    private static Supplier<MemoryInitializer> memoryInitializer = () -> MemoryInitializer.random();

    private EmulatorConstants() {}

    public static void setMemoryInitializer(final Supplier<MemoryInitializer> memInit) {
        memoryInitializer = Objects.requireNonNull(memInit);
    }

    public static MemoryInitializer getMemoryInitializer() {
        return memoryInitializer.get();
    }
}
