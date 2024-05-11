package com.ledmington.cpu.x86;

public enum PointerSize {
    BYTE_PTR(8),
    WORD_PTR(16),
    DWORD_PTR(32),
    QWORD_PTR(64),
    XMMWORD_PTR(128);

    private final int size;

    PointerSize(final int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }

    public static PointerSize fromSize(final int size) {
        return switch (size) {
            case 8 -> BYTE_PTR;
            case 16 -> WORD_PTR;
            case 32 -> DWORD_PTR;
            case 64 -> QWORD_PTR;
            case 128 -> XMMWORD_PTR;
            default -> throw new IllegalStateException("Unexpected value: " + size);
        };
    }
}
