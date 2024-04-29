package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.utils.BitUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestX86RegisterFile {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());
    private X86RegisterFile regFile;
    private static final Register8[] all8BitRegisters = new Register8[] {
        Register8.AL,
        Register8.AH,
        Register8.BL,
        Register8.BH,
        Register8.CL,
        Register8.CH,
        Register8.DL,
        Register8.DH,
        Register8.R8B,
        Register8.R9B,
        Register8.R10B,
        Register8.R11B,
        Register8.R12B,
        Register8.R13B,
        Register8.R14B,
        Register8.R15B,
        Register8.BPL,
        Register8.SPL,
        Register8.SIL,
        Register8.DIL
    };
    private static final Register32[] all32BitRegisters = new Register32[] {
        Register32.EAX,
        Register32.EBX,
        Register32.ECX,
        Register32.EDX,
        Register32.ESP,
        Register32.EBP,
        Register32.ESI,
        Register32.EDI,
        Register32.R8D,
        Register32.R9D,
        Register32.R10D,
        Register32.R11D,
        Register32.R12D,
        Register32.R13D,
        Register32.R14D,
        Register32.R15D
    };
    private static final Register64[] all64BitRegisters = new Register64[] {
        Register64.RAX,
        Register64.RBX,
        Register64.RCX,
        Register64.RDX,
        Register64.RSP,
        Register64.RBP,
        Register64.RSI,
        Register64.RDI,
        Register64.R8,
        Register64.R9,
        Register64.R10,
        Register64.R11,
        Register64.R12,
        Register64.R13,
        Register64.R14,
        Register64.R15
    };

    @BeforeEach
    public void setup() {
        regFile = new X86RegisterFile();
    }

    @AfterEach
    public void teardown() {
        regFile = null;
    }

    private static Stream<Arguments> all8BitsRegisters() {
        return Arrays.stream(all8BitRegisters).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("all8BitsRegisters")
    public void initiallyAllZero(final Register8 r) {
        assertEquals((byte) 0x00, regFile.get(r));
    }

    @ParameterizedTest
    @MethodSource("all8BitsRegisters")
    public void setToValue(final Register8 r) {
        final byte x = BitUtils.asByte(rng.nextInt(1, 256));
        regFile.set(r, x);
        assertEquals(x, regFile.get(r));

        for (final Register8 other : all8BitRegisters) {
            if (r == other) {
                continue;
            }
            assertEquals((byte) 0x00, regFile.get(other));
        }
    }

    private static Stream<Arguments> all32BitsRegisters() {
        return Arrays.stream(all32BitRegisters).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("all32BitsRegisters")
    public void initiallyAllZero(final Register32 r) {
        assertEquals(0x00000000, regFile.get(r));
    }

    @ParameterizedTest
    @MethodSource("all32BitsRegisters")
    public void setToValue(final Register32 r) {
        final int x = rng.nextInt();
        regFile.set(r, x);
        assertEquals(x, regFile.get(r));

        for (final Register32 other : all32BitRegisters) {
            if (r == other) {
                continue;
            }
            assertEquals(0x00000000, regFile.get(other));
        }
    }

    private static Stream<Arguments> all64BitsRegisters() {
        return Arrays.stream(all64BitRegisters).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("all64BitsRegisters")
    public void initiallyAllZero(final Register64 r) {
        assertEquals(0x0000000000000000L, regFile.get(r));
    }

    @ParameterizedTest
    @MethodSource("all64BitsRegisters")
    public void setToValue(final Register64 r) {
        final long x = rng.nextLong(1, Long.MAX_VALUE);
        regFile.set(r, x);
        assertEquals(x, regFile.get(r));

        for (final Register64 other : all64BitRegisters) {
            if (r == other) {
                continue;
            }
            assertEquals(0x0000000000000000L, regFile.get(other));
        }
    }
}
