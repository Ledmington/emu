/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.elf.section;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.MiniLogger;

/**
 * The tag of an entry of the Dynamic section (.dyn).
 *
 * <p>Useful reference <a href=
 * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/chapter6-14428/index.html">here</a>.
 */
public final class DynamicTableEntryTag {

    private static final MiniLogger logger = MiniLogger.getLogger("dyntab-entry-tag");
    private static final Map<Long, DynamicTableEntryTag> codeToTag = new HashMap<>();

    /** The last entry of the dynamic section has this tag. */
    public static final DynamicTableEntryTag DT_NULL = new DynamicTableEntryTag(0L, "NULL");

    /**
     * This element holds the string table offset of a null-terminated string, giving the name of a needed library. The
     * offset is an index into the table recorded in the DT_STRTAB entry. The dynamic array may contain multiple entries
     * with this type. These entries' relative order is significant, though their relation to entries of other types is
     * not.
     */
    public static final DynamicTableEntryTag DT_NEEDED = new DynamicTableEntryTag(1L, "NEEDED");

    /**
     * This element holds the total size, in bytes, of the relocation entries associated with the procedure linkage
     * table. If an entry of type DT_JMPREL is present, a DT_PLTRELSZ must accompany it.
     */
    public static final DynamicTableEntryTag DT_PLTRELSZ = new DynamicTableEntryTag(2L, "PLTRELSZ");

    /** This element holds an address associated with the procedure linkage table and/or the global offset table. */
    public static final DynamicTableEntryTag DT_PLTGOT = new DynamicTableEntryTag(3L, "PLTGOT");

    /**
     * This element holds the address of the symbol hash table. This hash table refers to the symbol table referenced by
     * the DT_SYMTAB element.
     */
    public static final DynamicTableEntryTag DT_HASH = new DynamicTableEntryTag(4L, "HASH");

    /**
     * This element holds the address of the string table. Symbol names, library names, and other strings reside in this
     * table.
     */
    public static final DynamicTableEntryTag DT_STRTAB = new DynamicTableEntryTag(5L, "STRTAB");

    /** This element holds the address of the symbol table. */
    public static final DynamicTableEntryTag DT_SYMTAB = new DynamicTableEntryTag(6L, "SYMTAB");

    /**
     * This element holds the address of a relocation table. Entries in the table have explicit addends. An object file
     * may have multiple relocation sections. When building the relocation table for an executable or shared object
     * file, the link editor catenates those sections to form a single table. Although the sections remain independent
     * in the object file, the dynamic linker sees a single table. When the dynamic linker creates the process image for
     * an executable file or adds a shared object to the process image, it reads the relocation table and performs the
     * associated actions. If this element is present, the dynamic structure must also have DT_RELASZ and DT_RELAENT
     * elements. When relocation is "mandatory" for a file, either DT_RELA or DT_REL may occur (both are permitted but
     * not required).
     */
    public static final DynamicTableEntryTag DT_RELA = new DynamicTableEntryTag(7L, "RELA");

    /** This element holds the total size, in bytes, of the DT_RELA relocation table. */
    public static final DynamicTableEntryTag DT_RELASZ = new DynamicTableEntryTag(8L, "RELASZ");

    /** This element holds the size, in bytes, of the DT_RELA relocation entry. */
    public static final DynamicTableEntryTag DT_RELAENT = new DynamicTableEntryTag(9L, "RELAENT");

    /** This element holds the size, in bytes, of the string table. */
    public static final DynamicTableEntryTag DT_STRSZ = new DynamicTableEntryTag(10L, "STRSZ");

    /** This element holds the size, in bytes, of a symbol table entry. */
    public static final DynamicTableEntryTag DT_SYMENT = new DynamicTableEntryTag(11L, "SYMENT");

    /** This element holds the address of the initialization function. */
    public static final DynamicTableEntryTag DT_INIT = new DynamicTableEntryTag(12L, "INIT");

    /** This element holds the address of the termination function. */
    public static final DynamicTableEntryTag DT_FINI = new DynamicTableEntryTag(13L, "FINI");

    /**
     * This element holds the string table offset of a null-terminated string, giving the name of the shared object. The
     * offset is an index into the table recorded in the DT_STRTAB entry.
     */
    public static final DynamicTableEntryTag DT_SONAME = new DynamicTableEntryTag(14L, "SONAME");

    /**
     * This element holds the string table offset of a null-terminated search library search path string. The offset is
     * an index into the table recorded in the DT_STRTAB entry.
     */
    public static final DynamicTableEntryTag DT_RPATH = new DynamicTableEntryTag(15L, "RPATH");

    /**
     * This element's presence in a shared object library alters the dynamic linker's symbol resolution algorithm for
     * references within the library. Instead of starting a symbol search with the executable file, the dynamic linker
     * starts from the shared object itself. If the shared object fails to supply the referenced symbol, the dynamic
     * linker then searches the executable file and other shared objects as usual.
     */
    public static final DynamicTableEntryTag DT_SYMBOLIC = new DynamicTableEntryTag(16L, "SYMBOLIC");

    /**
     * This element is similar to DT_RELA, except its table has implicit addends. If this element is present, the
     * dynamic structure must also have DT_RELSZ and DT_RELENT elements.
     */
    public static final DynamicTableEntryTag DT_REL = new DynamicTableEntryTag(17L, "REL");

    /** This element holds the total size, in bytes, of the DT_REL relocation table. */
    public static final DynamicTableEntryTag DT_RELSZ = new DynamicTableEntryTag(18L, "RELSZ");

    /** This element holds the size, in bytes, of the DT_REL relocation entry */
    public static final DynamicTableEntryTag DT_RELENT = new DynamicTableEntryTag(19L, "RELENT");

    /**
     * This member specifies the type of relocation entry to which the procedure linkage table refers. The d_val member
     * holds DT_REL or DT_RELA, as appropriate. All relocations in a procedure linkage table must use the same
     * relocation.
     */
    public static final DynamicTableEntryTag DT_PLTREL = new DynamicTableEntryTag(20L, "PLTREL");

    /** This member is used for debugging. */
    public static final DynamicTableEntryTag DT_DEBUG = new DynamicTableEntryTag(21L, "DEBUG");

    /**
     * This member's absence signifies that no relocation entry should cause a modification to a non-writable segment,
     * as specified by the segment permissions in the program header table. If this member is present, one or more
     * relocation entries might request modifications to a non-writable segment, and the dynamic linker can prepare
     * accordingly.
     */
    public static final DynamicTableEntryTag DT_TEXTREL = new DynamicTableEntryTag(22L, "TEXTREL");

    /**
     * If present, this entry's d_ptr member holds the address of relocation entries associated solely with the
     * procedure linkage table. Separating these relocation entries lets the dynamic linker ignore them during process
     * initialization, if lazy binding is enabled. If this entry is present, the related entries of types DT_PLTRELSZ
     * and DT_PLTREL must also be present.
     */
    public static final DynamicTableEntryTag DT_JMPREL = new DynamicTableEntryTag(23L, "JMPREL");

    /**
     * If present in a shared object or executable, this entry instructs the dynamic linker to process all relocations
     * for the object containing this entry before transferring control to the program. The presence of this entry takes
     * precedence over a directive to use lazy binding for this object when specified through the environment or via
     * dlopen(BA_LIB).
     */
    public static final DynamicTableEntryTag DT_BIND_NOW = new DynamicTableEntryTag(24L, "BIND_NOW");

    /**
     * The address of an array of pointers to initialization functions. This element requires that a DT_INIT_ARRAYSZ
     * element also be present.
     *
     * <p>Note: this is generally stored in a .init_array section.
     */
    public static final DynamicTableEntryTag DT_INIT_ARRAY = new DynamicTableEntryTag(25L, "INIT_ARRAY");

    /**
     * The address of an array of pointers to termination functions. This element requires that a DT_FINI_ARRAYSZ
     * element also be present.
     *
     * <p>Note: this is generally stored in a .fini_array section.
     */
    public static final DynamicTableEntryTag DT_FINI_ARRAY = new DynamicTableEntryTag(26L, "FINI_ARRAY");

    /** The total size, in bytes, of the DT_INIT_ARRAY array. */
    public static final DynamicTableEntryTag DT_INIT_ARRAYSZ = new DynamicTableEntryTag(27L, "INIT_ARRAYSZ");

    /** The total size, in bytes, of the DT_FINI_ARRAY array. */
    public static final DynamicTableEntryTag DT_FINI_ARRAYSZ = new DynamicTableEntryTag(28L, "FINI_ARRAYSZ");

    /** The DT_STRTAB string table offset of a null-terminated library search path string. */
    public static final DynamicTableEntryTag DT_RUNPATH = new DynamicTableEntryTag(29L, "RUNPATH");

    /**
     * Flag values specific to this object.
     *
     * <p>See <a href=
     * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/6n33n7fe2/index.html#chapter7-tbl-5">here</a>.
     */
    public static final DynamicTableEntryTag DT_FLAGS = new DynamicTableEntryTag(30L, "FLAGS");

    /**
     * The address of an array of pointers to pre-initialization functions. This element requires that a
     * DT_PREINIT_ARRAYSZ element also be present. This array is processed only in an executable file. This array is
     * ignored if contained in a shared object.
     */
    public static final DynamicTableEntryTag DT_PREINIT_ARRAY = new DynamicTableEntryTag(32L, "PREINIT_ARRAY");

    /** The total size, in bytes, of the DT_PREINIT_ARRAY array. */
    public static final DynamicTableEntryTag DT_PREINIT_ARRAYSZ = new DynamicTableEntryTag(33L, "PREINIT_ARRAYSZ");

    /** Holds the address of .gnu.hash. */
    public static final DynamicTableEntryTag DT_GNU_HASH = new DynamicTableEntryTag(0x000000006ffffef5L, "GNU_HASH");

    /** Address of the table provided by the .gnu.version section. */
    public static final DynamicTableEntryTag DT_VERSYM = new DynamicTableEntryTag(0x000000006ffffff0L, "VERSYM");

    /**
     * Indicates the RELATIVE relocation count, which is produced from the concatenation of all Elf32_Rela, or
     * Elf64_Rela relocations.
     */
    public static final DynamicTableEntryTag DT_RELACOUNT = new DynamicTableEntryTag(0x000000006ffffff9L, "RELACOUNT");

    /**
     * Flag values specific to this object.
     *
     * <p>See <a href=
     * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/6n33n7fe2/index.html#chapter6-tbl-53">here</a>.
     */
    public static final DynamicTableEntryTag DT_FLAGS_1 = new DynamicTableEntryTag(0x000000006ffffffbL, "FLAGS_1");

    /**
     * The address of the version definition table. Elements within this table contain indexes into the string table
     * DT_STRTAB. This element requires that the DT_VERDEFNUM element also be present.
     */
    public static final DynamicTableEntryTag DT_VERDEF = new DynamicTableEntryTag(0x000000006ffffffcL, "VERDEF");

    /** The number of entries in the DT_VERDEF table. */
    public static final DynamicTableEntryTag DT_VERDEFNUM = new DynamicTableEntryTag(0x000000006ffffffdL, "VERDEFNUM");

    /**
     * The address of the version dependency table. Elements within this table contain indexes into the string table
     * DT_STRTAB. This element requires that the DT_VERNEEDNUM element also be present.
     */
    public static final DynamicTableEntryTag DT_VERNEED = new DynamicTableEntryTag(0x000000006ffffffeL, "VERNEED");

    /** The number of entries in the DT_VERNEEDNUM table. */
    public static final DynamicTableEntryTag DT_VERNEEDNUM =
            new DynamicTableEntryTag(0x000000006fffffffL, "VERNEEDNUM");

    /** Values in the inclusive range from this one to DT_HIOS are reserved for OS-specific semantics. */
    public static final DynamicTableEntryTag DT_LOOS =
            new DynamicTableEntryTag(0x0000000060000000, "OS-specific", false);

    /** Values in the inclusive range from DT_LOOS to this one are reserved for OS-specific semantics. */
    public static final DynamicTableEntryTag DT_HIOS =
            new DynamicTableEntryTag(0x000000006fffffff, "OS-specific", false);

    /** Values in the inclusive range from this one to DT_HIPROC are reserved for processor-specific semantics. */
    public static final DynamicTableEntryTag DT_LOPROC =
            new DynamicTableEntryTag(0x0000000070000000, "Processor-specific", false);

    /** Values in the inclusive range from DT_LOPROC to this one are reserved for processor-specific semantics. */
    public static final DynamicTableEntryTag DT_HIPROC =
            new DynamicTableEntryTag(0x000000007fffffff, "Processor-specific", false);

    /**
     * Returns the Dynamic table tag corresponding to the given code.
     *
     * @param code The 64-bit code of a Dynamic table tag.
     * @return A Dynamic table tag object.
     */
    public static DynamicTableEntryTag fromCode(final long code) {
        if (!codeToTag.containsKey(code)) {
            if (code >= DT_LOOS.code && code <= DT_HIOS.code) {
                logger.warning("Unknown Dynamic table entry tag found: 0x%016x", code);
                return new DynamicTableEntryTag(code, "OS-specific", false);
            }
            if (code >= DT_LOPROC.code && code <= DT_HIPROC.code) {
                logger.warning("Unknown Dynamic table entry tag found: 0x%016x", code);
                return new DynamicTableEntryTag(code, "Processor-specific", false);
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Dynamic table entry tag identifier: 0x%016x", code));
        }
        return codeToTag.get(code);
    }

    private final long code;
    private final String name;

    private DynamicTableEntryTag(final long code, final String name, final boolean addToMap) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (addToMap) {
            if (codeToTag.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Dynamic table entry tag value with code %d (0x%016x) already exists", code, code));
            }
            codeToTag.put(code, this);
        }
    }

    private DynamicTableEntryTag(final long code, final String name) {
        this(code, name, true);
    }

    /**
     * Return the code of this Dynamic table tag object.
     *
     * @return The 64-bit code.
     */
    public long getCode() {
        return code;
    }

    /**
     * Returns the name of this Dynamic table tag object.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DynamicTableEntryTag(code=" + code + ";name=" + name + ')';
    }
}
