package com.ledmington.emu.mem;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TestPermissions {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    private MemoryController mem;

    @BeforeEach
    void setup() {
        mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()));
    }

    @AfterEach
    void teardown() {
        mem = null;
    }

    @Test
    void cantDoAnythingByDefault() {
        Stream.generate(rng::nextLong)
                .distinct()
                .limit(100)
                .forEach(x -> assertThrows(IllegalArgumentException.class, () -> mem.read(x)));

        Stream.generate(rng::nextLong)
                .distinct()
                .limit(100)
                .forEach(x -> assertThrows(IllegalArgumentException.class, () -> mem.readCode(x)));

        Stream.generate(rng::nextLong)
                .distinct()
                .limit(100)
                .forEach(x -> assertThrows(
                        IllegalArgumentException.class, () -> mem.write(x, BitUtils.asByte(rng.nextInt()))));
    }

    @Test
    void canRead() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, false, false);

        for (long i = start; i <= end; i++) {
            mem.read(i);
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.readCode(finalI));
            assertThrows(IllegalArgumentException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canWrite() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, true, false);

        for (long i = start; i <= end; i++) {
            mem.write(i, BitUtils.asByte(rng.nextInt()));
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.read(finalI));
            assertThrows(IllegalArgumentException.class, () -> mem.readCode(finalI));
        }
    }

    @Test
    void canReadAndWrite() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, true, false);

        for (long i = start; i <= end; i++) {
            mem.read(i);
            mem.write(i, BitUtils.asByte(rng.nextInt()));
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.readCode(finalI));
        }
    }

    @Test
    void canExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, false, true);

        for (long i = start; i <= end; i++) {
            mem.readCode(i);
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.read(finalI));
            assertThrows(IllegalArgumentException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canReadAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, false, true);

        for (long i = start; i <= end; i++) {
            mem.read(i);
            mem.readCode(i);
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, true, true);

        for (long i = start; i <= end; i++) {
            mem.readCode(i);
            mem.write(i, BitUtils.asByte(rng.nextInt()));
            final long finalI = i;
            assertThrows(IllegalArgumentException.class, () -> mem.read(finalI));
        }
    }

    @Test
    void canReadWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, true, true);

        for (long i = start; i <= end; i++) {
            mem.readCode(i);
            mem.read(i);
            mem.write(i, BitUtils.asByte(rng.nextInt()));
        }
    }
}
