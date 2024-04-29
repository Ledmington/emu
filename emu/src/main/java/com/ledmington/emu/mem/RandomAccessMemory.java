package com.ledmington.emu.mem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RandomAccessMemory implements Memory {

    private final MemoryInitializer init;
    private final Map<Long, Byte> m = new HashMap<>();

    public RandomAccessMemory(final MemoryInitializer init) {
        this.init = Objects.requireNonNull(init);
    }

    @Override
    public byte read(final long address) {
        return m.containsKey(address) ? m.get(address) : init.get();
    }

    @Override
    public void write(final long address, final byte value) {
        m.put(address, value);
    }
}
