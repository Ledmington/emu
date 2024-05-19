package com.ledmington.elf.section;

public final class DynamicTableEntry {

    private final DynamicTableEntryTag tag;
    private final long content;

    public DynamicTableEntry(final long tag, final long content) {
        this.tag = DynamicTableEntryTag.fromCode(tag);
        this.content = content;
    }

    public DynamicTableEntryTag getTag() {
        return tag;
    }

    public long getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("0x%016x %-12s 0x%016x", tag.code(), tag.name(), content);
    }
}
