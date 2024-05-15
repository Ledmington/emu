package com.ledmington.emu.mem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

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

    @Test
    void cantReadByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(IllegalArgumentException.class, () -> mem.read(address));
        }
    }

    @Test
    void cantExecuteByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(IllegalArgumentException.class, () -> mem.readCode(address));
        }
    }

    @Test
    void cantWriteByDefault() {
        final Set<Long> positions =
                Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

        for (final long address : positions) {
            assertThrows(IllegalArgumentException.class, () -> mem.write(address, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canRead() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, false, false);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
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
            final long finalI = i;
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
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
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
            assertThrows(IllegalArgumentException.class, () -> mem.readCode(finalI));
        }
    }

    @Test
    void canExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, false, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
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
            final long finalI = i;
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertThrows(IllegalArgumentException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }

    @Test
    void canWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, false, true, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
            assertThrows(IllegalArgumentException.class, () -> mem.read(finalI));
        }
    }

    @Test
    void canReadWriteAndExecute() {
        final long start = rng.nextLong();
        final long end = start + 100L;
        mem.setPermissions(start, end, true, true, true);

        for (long i = start; i <= end; i++) {
            final long finalI = i;
            assertDoesNotThrow(() -> mem.readCode(finalI));
            assertDoesNotThrow(() -> mem.read(finalI));
            assertDoesNotThrow(() -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
        }
    }
}
