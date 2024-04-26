package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class TestReadOnlyByteBuffer {

    private final byte[] arr = new byte[] {
        (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
    };
    private ReadOnlyByteBuffer bb;

    @BeforeEach
    public void setup() {
        this.bb = new ReadOnlyByteBuffer(this.arr);
    }

    @Test
    public void bytesBE() {
        bb.setEndianness(false);
        assertEquals((byte) 0x01, bb.read1());
        assertEquals((byte) 0x23, bb.read1());
        assertEquals((byte) 0x45, bb.read1());
        assertEquals((byte) 0x67, bb.read1());
        assertEquals((byte) 0x89, bb.read1());
        assertEquals((byte) 0xab, bb.read1());
        assertEquals((byte) 0xcd, bb.read1());
        assertEquals((byte) 0xef, bb.read1());
    }

    @Test
    public void bytesLE() {
        bb.setEndianness(true);
        assertEquals((byte) 0x01, bb.read1());
        assertEquals((byte) 0x23, bb.read1());
        assertEquals((byte) 0x45, bb.read1());
        assertEquals((byte) 0x67, bb.read1());
        assertEquals((byte) 0x89, bb.read1());
        assertEquals((byte) 0xab, bb.read1());
        assertEquals((byte) 0xcd, bb.read1());
        assertEquals((byte) 0xef, bb.read1());
    }

    @Test
    public void wordsBE() {
        bb.setEndianness(false);
        assertEquals((short) 0x0123, bb.read2());
        assertEquals((short) 0x4567, bb.read2());
        assertEquals((short) 0x89ab, bb.read2());
        assertEquals((short) 0xcdef, bb.read2());
    }

    @Test
    public void wordsLE() {
        bb.setEndianness(true);
        assertEquals((short) 0x2301, bb.read2());
        assertEquals((short) 0x6745, bb.read2());
        assertEquals((short) 0xab89, bb.read2());
        assertEquals((short) 0xefcd, bb.read2());
    }

    @Test
    public void doubleWordsBE() {
        bb.setEndianness(false);
        assertEquals(0x01234567, bb.read4());
        assertEquals(0x89abcdef, bb.read4());
    }

    @Test
    public void doubleWordsLE() {
        bb.setEndianness(true);
        assertEquals(0x67452301, bb.read4());
        assertEquals(0xefcdab89, bb.read4());
    }

    @Test
    public void doubleWordsAsQuadWordsBE() {
        bb.setEndianness(false);
        assertEquals(0x0000000001234567L, BitUtils.asLong(bb.read4()));
        assertEquals(0x0000000089abcdefL, BitUtils.asLong(bb.read4()));
    }

    @Test
    public void doubleWordsAsQuadWordsLE() {
        bb.setEndianness(true);
        assertEquals(0x0000000067452301L, BitUtils.asLong(bb.read4()));
        assertEquals(0x00000000efcdab89L, BitUtils.asLong(bb.read4()));
    }

    @Test
    public void quadWordsBE() {
        bb.setEndianness(false);
        assertEquals(0x0123456789abcdefL, bb.read8());
    }

    @Test
    public void quadWordsLE() {
        bb.setEndianness(true);
        assertEquals(0xefcdab8967452301L, bb.read8());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    public void setPosition(int pos) {
        bb.setPosition(pos);
        assertEquals(pos, bb.position());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    public void randomRead(int pos) {
        bb.setPosition(pos);
        assertEquals(arr[pos], bb.read1());
    }
}
