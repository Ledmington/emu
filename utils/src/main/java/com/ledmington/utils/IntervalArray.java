package com.ledmington.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: this can be optimized into a segment tree.
 */
public final class IntervalArray {

    private static final class Block {

        private final long start;
        private final long end;

        public Block(final long start, final long end) {
            if (end < start) {
                throw new IllegalArgumentException(
                        String.format("Invalid start (0x%016x) and end (0x%016x) of a block", start, end));
            }
            this.start = start;
            this.end = end;
        }

        private long start() {
            return start;
        }

        private long end() {
            return end;
        }
    }

    private final List<Block> blocks = new ArrayList<>();

    public IntervalArray() {}

    public boolean get(final long address) {
        for (final Block b : blocks) {
            if (address >= b.start() && address <= b.end()) {
                return true;
            }
        }
        return false;
    }

    private void sortBlocks() {
        // sorting by the starting address is sufficient
        blocks.sort((a, b) -> Long.compare(a.start(), b.start()));
    }

    private void mergeBlocks() {
        for (int i = 0; i < blocks.size() - 1; i++) {
            final Block curr = blocks.get(i);
            final Block next = blocks.get(i + 1);
            if (curr.end() >= next.start()) {
                // overlapping
                blocks.remove(i);
                blocks.remove(i);
                blocks.add(i, new Block(curr.start(), next.end()));
                i--;
            }
        }
    }

    public void set(final long address) {
        set(address, address);
    }

    /**
     * Sets the boolean values in the given range to true. Does not throw
     * exceptions in case it is already true.
     */
    public void set(final long startAddress, final long endAddress) {
        blocks.add(new Block(startAddress, endAddress));
        sortBlocks();
        mergeBlocks();
    }

    public void reset(final long address) {
        reset(address, address);
    }

    /**
     * Sets the boolean values in the given range to false. Does not throw
     * exceptions in case it is already false.
     */
    public void reset(final long startAddress, final long endAddress) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).start() <= startAddress) {
                if (blocks.get(i).end() >= endAddress) {
                    // the given range fits entirely in this block
                    final Block curr = blocks.get(i);
                    blocks.remove(i);
                    blocks.add(i, new Block(curr.start(), startAddress - 1));
                    blocks.add(i + 1, new Block(endAddress + 1, curr.end()));
                } else {
                    // the given range takes up only the right "half" of this block
                    final Block curr = blocks.get(i);
                    blocks.set(i, new Block(curr.start(), startAddress - 1));
                }
            } else {
                if (blocks.get(i).end() >= endAddress) {
                    // the given range takes up only the left "half" of this block
                    final Block curr = blocks.get(i);
                    blocks.set(i, new Block(endAddress + 1, curr.end()));
                } else {
                    // the given range completely contains this block
                    blocks.remove(i);
                    i--;
                }
            }
        }
        sortBlocks();
        mergeBlocks();
    }
}
