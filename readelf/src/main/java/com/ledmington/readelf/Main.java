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
package com.ledmington.readelf;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFReader;
import com.ledmington.elf.FileHeader;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.DynamicSection;
import com.ledmington.elf.section.DynamicSymbolTableSection;
import com.ledmington.elf.section.DynamicTableEntry;
import com.ledmington.elf.section.DynamicTableEntryTag;
import com.ledmington.elf.section.GnuHashSection;
import com.ledmington.elf.section.GnuVersionRequirementEntry;
import com.ledmington.elf.section.GnuVersionRequirementsSection;
import com.ledmington.elf.section.GnuVersionSection;
import com.ledmington.elf.section.InterpreterPathSection;
import com.ledmington.elf.section.RelocationAddendEntry;
import com.ledmington.elf.section.RelocationAddendEntryType;
import com.ledmington.elf.section.RelocationAddendSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.SectionHeaderType;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.SymbolTable;
import com.ledmington.elf.section.SymbolTableEntry;
import com.ledmington.elf.section.SymbolTableSection;
import com.ledmington.elf.section.note.GnuPropertyType;
import com.ledmington.elf.section.note.NoteSection;
import com.ledmington.elf.section.note.NoteSectionEntry;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

public final class Main {

    private static final PrintWriter out = System.console() != null
            ? System.console().writer()
            : new PrintWriter(System.out, false, StandardCharsets.UTF_8);

    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static void main(final String[] args) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

        String filename = null;
        boolean displayFileHeader = false;
        boolean displaySectionHeaders = false;
        boolean displaySectionDetails = false;
        boolean displayProgramHeaders = false;
        boolean displaySectionToSegmentMapping = false;
        boolean displayDynamicSymbolTable = false;
        boolean displaySymbolTable = false;
        boolean displayNoteSections = false;
        boolean displayDynamicSection = false;
        boolean displayRelocationSections = false;
        boolean displayVersionSections = false;
        boolean displayGnuHashSection = false;
        boolean displaySectionGroups = false;
        Optional<Integer> sectionIndexToBeHexDumped = Optional.empty();
        Optional<String> sectionNameToBeHexDumped = Optional.empty();
        Optional<Integer> sectionIndexToBeStringDumped = Optional.empty();
        Optional<String> sectionNameToBeStringDumped = Optional.empty();

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            switch (arg) {
                case "-H", "--help":
                    printHelp();
                    out.flush();
                    System.exit(0);
                    break;
                case "-v", "--version":
                    out.println("readelf v0.0.0");
                    out.flush();
                    System.exit(0);
                    break;
                case "-h", "--file-header":
                    displayFileHeader = true;
                    break;
                case "-a", "--all":
                    displayFileHeader = true;
                    displaySectionHeaders = true;
                    displaySectionDetails = true;
                    displayProgramHeaders = true;
                    displaySectionToSegmentMapping = true;
                    displayDynamicSymbolTable = true;
                    displaySymbolTable = true;
                    displayNoteSections = true;
                    displayDynamicSection = true;
                    displayRelocationSections = true;
                    displayVersionSections = true;
                    displayGnuHashSection = true;
                    displaySectionGroups = true;
                    break;
                case "-l", "--program-headers", "--segments":
                    displayProgramHeaders = true;
                    break;
                case "-S", "--section-headers", "--sections":
                    displaySectionHeaders = true;
                    break;
                case "-g", "--section-groups":
                    displaySectionGroups = true;
                    break;
                case "-t", "--section-details":
                    displaySectionDetails = true;
                    break;
                case "-e", "--headers":
                    displayFileHeader = true;
                    displaySectionHeaders = true;
                    displayProgramHeaders = true;
                    displaySectionToSegmentMapping = true;
                    break;
                case "-s", "--syms", "--symbols":
                    displayDynamicSymbolTable = true;
                    displaySymbolTable = true;
                    break;
                case "--dyn-syms":
                    displayDynamicSymbolTable = true;
                    break;
                case "--lto-syms":
                    notImplemented();
                    break;
                case "-n", "--notes":
                    displayNoteSections = true;
                    break;
                case "-r", "--relocs":
                    displayRelocationSections = true;
                    break;
                case "-u", "--unwind":
                    notImplemented();
                    break;
                case "-d", "--dynamic":
                    displayDynamicSection = true;
                    break;
                case "-V", "--version-info":
                    displayVersionSections = true;
                    break;
                case "-A", "--arch-specific":
                    notImplemented();
                    break;
                case "-c", "--archive-index":
                    notImplemented();
                    break;
                case "-D", "--use-dynamic":
                    notImplemented();
                    break;
                case "-L", "--lint", "--enable-checks":
                    notImplemented();
                    break;
                case "-x":
                    // --hex-dump=<number|name>
                    if (i + 1 >= args.length) {
                        out.println("readelf: option requires an argument -- 'x'");
                        printHelp();
                        out.flush();
                        System.exit(0);
                    }
                    i++;
                    if (args[i].chars().allMatch(Character::isDigit)) {
                        sectionIndexToBeHexDumped = Optional.of(Integer.parseInt(args[i]));
                    } else {
                        sectionNameToBeHexDumped = Optional.of(args[i]);
                    }
                    break;
                case "-p":
                    // --string-dump=<number|name>
                    if (i + 1 >= args.length) {
                        out.println("readelf: option requires an argument -- 'p'");
                        printHelp();
                        out.flush();
                        System.exit(0);
                    }
                    i++;
                    if (args[i].chars().allMatch(Character::isDigit)) {
                        sectionIndexToBeStringDumped = Optional.of(Integer.parseInt(args[i]));
                    } else {
                        sectionNameToBeStringDumped = Optional.of(args[i]);
                    }
                    break;
                case "-R":
                    // --relocated-dump=<number|name>
                    notImplemented();
                    break;

                case "-I", "--histogram":
                    notImplemented();
                    break;
                case "-W", "--wide":
                    notImplemented();
                    break;
                case "-T", "--silent-truncation":
                    notImplemented();
                    break;

                    // TODO: add the other CLI flags

                default:
                    if (arg.startsWith("-")) {
                        if (arg.startsWith("--hex-dump=")) {
                            final String s = arg.split("=")[1];
                            if (s.chars().allMatch(Character::isDigit)) {
                                sectionIndexToBeHexDumped = Optional.of(Integer.parseInt(s));
                            } else {
                                sectionNameToBeHexDumped = Optional.of(s);
                            }
                        } else if (arg.startsWith("--string-dump=")) {
                            final String s = arg.split("=")[1];
                            if (s.chars().allMatch(Character::isDigit)) {
                                sectionIndexToBeStringDumped = Optional.of(Integer.parseInt(s));
                            } else {
                                sectionNameToBeStringDumped = Optional.of(s);
                            }
                        } else {
                            out.printf("readelf: Error: Invalid option '%s'%n", arg);
                            printHelp();
                            out.flush();
                            System.exit(0);
                        }
                    } else {
                        filename = arg;
                    }
                    break;
            }
        }

        if (filename == null) {
            printHelp();
            out.flush();
            System.exit(0);
        }

        final ELF elf;
        try {
            elf = ELFReader.read(Files.readAllBytes(Path.of(filename)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (sectionIndexToBeHexDumped.isPresent()) {
            printHexDumpOfSection(filename, elf, sectionIndexToBeHexDumped.orElseThrow());
        }

        if (sectionNameToBeHexDumped.isPresent()) {
            printHexDumpOfSection(filename, elf, sectionNameToBeHexDumped.orElseThrow());
        }

        if (sectionIndexToBeStringDumped.isPresent()) {
            printStringDumpOfSection(filename, elf, sectionIndexToBeStringDumped.orElseThrow());
        }

        if (sectionNameToBeStringDumped.isPresent()) {
            printStringDumpOfSection(filename, elf, sectionNameToBeStringDumped.orElseThrow());
        }

        if (displayFileHeader) {
            printFileHeader(filename, elf);
        }

        if (displaySectionHeaders) {
            if (displayFileHeader) {
                out.println();
            }
            printSectionHeaders(elf);
        }

        if (displaySectionDetails && !displaySectionHeaders) {
            if (displayFileHeader) {
                out.println();
            }

            printSectionDetails(elf);
        }

        if (displaySectionGroups) {
            if (displayFileHeader) {
                out.println();
            }
            printSectionGroups();
        }

        if (displayProgramHeaders) {
            if (displaySectionGroups) {
                out.println();
            }
            printProgramHeaders(elf);
        }

        if (displaySectionToSegmentMapping) {
            if (displayProgramHeaders) {
                out.println();
            }
            printSectionToSegmentMapping(elf);
        }

        if (displayDynamicSection) {
            if (displaySectionToSegmentMapping) {
                out.println();
            }
            printDynamicSection((DynamicSection) elf.getSectionByName(".dynamic"), elf.sectionTable());
        }

        if (displayRelocationSections) {
            if (displayDynamicSection) {
                out.println();
            }
            printRelocationSection(
                    (RelocationAddendSection) elf.getSectionByName(".rela.dyn"),
                    elf.sectionTable(),
                    elf.fileHeader().is32Bit());
            printRelocationSection(
                    (RelocationAddendSection) elf.getSectionByName(".rela.plt"),
                    elf.sectionTable(),
                    elf.fileHeader().is32Bit());
        }

        if (displayDynamicSymbolTable) {
            if (displayRelocationSections) {
                out.println();
            }
            printSymbolTable((DynamicSymbolTableSection) elf.getSectionByName(".dynsym"), elf.sectionTable());
        }

        if (displaySymbolTable) {
            if (displayDynamicSymbolTable) {
                out.println();
            }
            printSymbolTable((SymbolTableSection) elf.getSectionByName(".symtab"), elf.sectionTable());
        }

        if (displayGnuHashSection) {
            if (displaySymbolTable) {
                out.println();
            }
            printGnuHashSection((GnuHashSection) elf.getSectionByName(".gnu.hash"));
        }

        if (displayVersionSections) {
            if (displayGnuHashSection) {
                out.println();
            }
            printVersionSection(elf.getSectionByName(".gnu.version"), elf.sectionTable());
            out.println();
            printVersionSection(elf.getSectionByName(".gnu.version_r"), elf.sectionTable());
        }

        if (displayNoteSections) {
            if (displayGnuHashSection) {
                out.println();
            }
            for (final Section s : elf.sectionTable()) {
                if (!s.getName().startsWith(".note")) {
                    continue;
                }

                printNoteSection((NoteSection) s);
                out.println();
            }
        }

        out.flush();
        System.exit(0);
    }

    private static void printStringDumpOfSection(final String filename, final ELF elf, final String sectionName) {
        final ReadOnlyByteBuffer b;
        try {
            b = new ReadOnlyByteBufferV1(Files.readAllBytes(Path.of(filename)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        printStringDump(b, elf.getSectionByName(sectionName));
    }

    private static void printStringDumpOfSection(final String filename, final ELF elf, final int sectionIndex) {
        final ReadOnlyByteBuffer b;
        try {
            b = new ReadOnlyByteBufferV1(Files.readAllBytes(Path.of(filename)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        printStringDump(b, elf.sectionTable()[sectionIndex]);
    }

    private static boolean isAsciiPrintable(final byte x) {
        return x >= 32 && x < 127;
    }

    private static void printStringDump(final ReadOnlyByteBuffer b, final Section s) {
        out.printf("%nString dump of section '%s':%n", s.getName());
        final long start = s.getHeader().getFileOffset();
        b.setPosition(start);
        final long length = s.getHeader().getSectionSize();
        while (b.getPosition() - start < length) {
            byte x = b.read1();
            if (!isAsciiPrintable(x)) {
                continue;
            }

            out.printf("  [%6x]  ", b.getPosition() - start - 1L);
            while (b.getPosition() - start < length) {
                if (isAsciiPrintable(x)) {
                    out.printf("%c", (char) x);
                } else {
                    out.println();
                    break;
                }
                x = b.read1();
            }
        }
        out.println();
    }

    private static void printHexDumpOfSection(final String filename, final ELF elf, final String sectionName) {
        final ReadOnlyByteBuffer b;
        try {
            b = new ReadOnlyByteBufferV1(Files.readAllBytes(Path.of(filename)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        printHexDump(b, elf.getSectionByName(sectionName));
    }

    private static void printHexDumpOfSection(final String filename, final ELF elf, final int sectionIndex) {
        final ReadOnlyByteBuffer b;
        try {
            b = new ReadOnlyByteBufferV1(Files.readAllBytes(Path.of(filename)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        printHexDump(b, elf.sectionTable()[sectionIndex]);
    }

    private static void printHexDump(final ReadOnlyByteBuffer b, final Section s) {
        out.printf("%nHex dump of section '%s':%n", s.getName());
        final long start = s.getHeader().getFileOffset();
        b.setPosition(start);
        final long length = s.getHeader().getSectionSize();
        while (b.getPosition() - start < length) {
            final long a = b.getPosition();
            out.printf("  0x%08x", BitUtils.asInt(a));
            for (int i = 0; i < 4; i++) {
                out.print(" ");
                for (int k = 0; k < 4; k++) {
                    if (b.getPosition() - start < length) {
                        out.printf("%02x", b.read1());
                    } else {
                        out.print("  ");
                    }
                }
            }
            out.print(" ");
            b.setPosition(a);
            for (int i = 0; i < 16; i++) {
                if (b.getPosition() - start < length) {
                    final byte x = b.read1();
                    out.printf("%c", isAsciiPrintable(x) ? (char) x : '.');
                } else {
                    out.print(" ");
                }
            }

            out.println();
        }
        out.println();
    }

    private static void printSectionGroups() {
        out.println("There are no section groups in this file.");
    }

    private static void printGnuHashSection(final GnuHashSection s) {
        out.printf("Histogram for `%s' bucket list length (total of %d buckets):%n", s.getName(), s.getBucketsLength());
        out.println(" Length  Number     % of total  Coverage");
        for (int b = 0; b < s.getBucketsLength(); b++) {
            out.printf("  bucket %d: %d TODO%n", b, s.getBucket(b));
        }
    }

    private static void notImplemented() {
        out.println("This flag is not implemented yet");
        out.flush();
        System.exit(-1);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static void printVersionSection(final Section s, final Section... sectionTable) {
        final SectionHeader sh = s.getHeader();
        final Section linkedSection = sectionTable[sh.getLinkedSectionIndex()];

        if (s instanceof GnuVersionRequirementsSection gvrs) {
            out.printf(
                    "Version needs section '%s' contains %d entr%s:%n",
                    s.getName(), gvrs.getRequirementsLength(), gvrs.getRequirementsLength() == 1 ? "y" : "ies");
            out.printf(
                    " Addr: 0x%016x  Offset: 0x%06x  Link: %d (%s)%n",
                    sh.getVirtualAddress(),
                    sh.getFileOffset(),
                    sh.getLinkedSectionIndex(),
                    sectionTable[sh.getLinkedSectionIndex()].getName());

            final StringTableSection strtab = (StringTableSection) linkedSection;
            for (int i = 0; i < gvrs.getRequirementsLength(); i++) {
                final GnuVersionRequirementEntry gvre = gvrs.getEntry(i);
                out.printf(
                        "  %06x: Version: %d  File: %s  Cnt: %d%n",
                        i, gvre.version(), strtab.getString(gvre.fileOffset()), gvre.count());
                out.printf("%s%n", gvre);
            }
        } else if (s instanceof GnuVersionSection gvs) {
            out.printf(
                    "Version symbols section '%s' contains %d entr%s:%n",
                    s.getName(), gvs.getVersionsLength(), gvs.getVersionsLength() == 1 ? "y" : "ies");
            out.printf(
                    " Addr: 0x%016x  Offset: 0x%06x  Link: %d (%s)%n",
                    sh.getVirtualAddress(), sh.getFileOffset(), sh.getLinkedSectionIndex(), linkedSection.getName());

            final SymbolTable symbolTable = (SymbolTable) linkedSection;
            final StringTableSection stringTable =
                    (StringTableSection) sectionTable[linkedSection.getHeader().getLinkedSectionIndex()];
            for (int i = 0; i < gvs.getVersionsLength(); i++) {
                if (i % 4 == 0) {
                    out.printf("  %03x:   ", i);
                }
                out.printf(
                        "%2d (%s)  ",
                        gvs.getVersion(i),
                        gvs.getVersion(i) == 0
                                ? "*local*"
                                : (gvs.getVersion(i) == 1
                                        ? "*global*"
                                        : stringTable.getString(symbolTable
                                                .getSymbolTableEntry(i)
                                                .nameOffset())));
                if (i % 4 == 3) {
                    out.println();
                }
            }
            out.println();
        } else {
            out.printf("Unknown version section '%s'%n", s.getName());
        }
    }

    private static void printDynamicSection(final DynamicSection ds, final Section[] sectionTable) {
        final SectionHeader dsh = ds.getHeader();
        out.printf("Dynamic section at offset 0x%x contains %d entries:%n", dsh.getFileOffset(), ds.getTableLength());
        out.println("  Tag        Type                         Name/Value");

        long dt_strtab_offset = -1L;
        for (int i = 0; i < ds.getTableLength(); i++) {
            final DynamicTableEntry dte = ds.getEntry(i);
            if (dte.getTag() == DynamicTableEntryTag.DT_STRTAB) {
                dt_strtab_offset = dte.getContent();
                break;
            }
        }

        StringTableSection strtab = null;
        for (final Section s : sectionTable) {
            if (s.getHeader().getFileOffset() == dt_strtab_offset) {
                strtab = (StringTableSection) s;
                break;
            }
        }

        for (int i = 0; i < ds.getTableLength(); i++) {
            final DynamicTableEntry dte = ds.getEntry(i);
            out.printf(
                    " 0x%016x %-20s ",
                    dte.getTag().getCode(), "(" + dte.getTag().getName() + ")");
            final long content = dte.getContent();
            if (dte.getTag() == DynamicTableEntryTag.DT_INIT_ARRAYSZ
                    || dte.getTag() == DynamicTableEntryTag.DT_FINI_ARRAYSZ
                    || dte.getTag() == DynamicTableEntryTag.DT_STRSZ
                    || dte.getTag() == DynamicTableEntryTag.DT_SYMENT
                    || dte.getTag() == DynamicTableEntryTag.DT_PLTRELSZ
                    || dte.getTag() == DynamicTableEntryTag.DT_RELASZ
                    || dte.getTag() == DynamicTableEntryTag.DT_RELAENT) {
                out.printf("%d (bytes)%n", content);
            } else if (dte.getTag() == DynamicTableEntryTag.DT_INIT
                    || dte.getTag() == DynamicTableEntryTag.DT_FINI
                    || dte.getTag() == DynamicTableEntryTag.DT_INIT_ARRAY
                    || dte.getTag() == DynamicTableEntryTag.DT_FINI_ARRAY
                    || dte.getTag() == DynamicTableEntryTag.DT_GNU_HASH
                    || dte.getTag() == DynamicTableEntryTag.DT_STRTAB
                    || dte.getTag() == DynamicTableEntryTag.DT_SYMTAB
                    || dte.getTag() == DynamicTableEntryTag.DT_DEBUG
                    || dte.getTag() == DynamicTableEntryTag.DT_PLTGOT
                    || dte.getTag() == DynamicTableEntryTag.DT_JMPREL
                    || dte.getTag() == DynamicTableEntryTag.DT_RELA
                    || dte.getTag() == DynamicTableEntryTag.DT_VERNEED
                    || dte.getTag() == DynamicTableEntryTag.DT_VERSYM
                    || dte.getTag() == DynamicTableEntryTag.DT_NULL) {
                out.printf("0x%x%n", content);
            } else if (dte.getTag() == DynamicTableEntryTag.DT_PLTREL) {
                out.println("RELA");
            } else if (dte.getTag() == DynamicTableEntryTag.DT_FLAGS) {
                out.println("BIND_NOW");
            } else if (dte.getTag() == DynamicTableEntryTag.DT_FLAGS_1) {
                out.println("Flags: NOW PIE");
            } else if (dte.getTag() == DynamicTableEntryTag.DT_VERNEEDNUM
                    || dte.getTag() == DynamicTableEntryTag.DT_RELACOUNT) {
                out.printf("%d%n", content);
            } else if (dte.getTag() == DynamicTableEntryTag.DT_NEEDED) {
                out.printf("Shared library: [%s]%n", strtab.getString(BitUtils.asInt(content)));
            } else {
                out.printf("%nUnknown dynamic table tag '%s'%n", dte.getTag());
            }
        }
    }

    private static void printRelocationSection(
            final RelocationAddendSection ras, final Section[] sectionTable, final boolean is32Bit) {
        final SectionHeader rash = ras.getHeader();
        final SymbolTable symtab = (SymbolTable) sectionTable[ras.getHeader().getLinkedSectionIndex()];
        final StringTableSection symstrtab =
                (StringTableSection) sectionTable[symtab.getHeader().getLinkedSectionIndex()];

        out.printf(
                "Relocation section '%s' at offset 0x%x contains %,d entr%s:%n",
                ras.getName(),
                rash.getFileOffset(),
                ras.getRelocationAddendTableLength(),
                ras.getRelocationAddendTableLength() == 1 ? "y" : "ies");
        out.println("  Offset          Info           Type           Sym. Value    Sym. Name + Addend");

        for (int i = 0; i < ras.getRelocationAddendTableLength(); i++) {
            final RelocationAddendEntry rae = ras.getRelocationAddendEntry(i);
            if (is32Bit) {
                final byte symbolTableIndex = BitUtils.asByte(rae.symbolTableIndex());
                final RelocationAddendEntryType type = rae.type();
                // TODO
            } else {
                final int symbolTableIndex = rae.symbolTableIndex();
                final RelocationAddendEntryType type = rae.type();
                final SymbolTableEntry symbol = symtab.getSymbolTableEntry(symbolTableIndex);
                out.printf(
                        "%012x  %012x %s ",
                        rae.offset(),
                        (BitUtils.asLong(symbolTableIndex) << 32) | BitUtils.asLong(type.getCode()),
                        type.name().substring(0, Math.min(type.name().length(), 17)));

                if (type == RelocationAddendEntryType.R_X86_64_RELATIVE) {
                    // B + A
                    out.printf("                   %x%n", rae.addend());
                } else if (type == RelocationAddendEntryType.R_X86_64_GLOB_DAT
                        || type == RelocationAddendEntryType.R_X86_64_JUMP_SLOT) {
                    // S
                    out.printf(
                            "%016x %s + %d%n", symbol.value(), symstrtab.getString(symbol.nameOffset()), rae.addend());
                } else {
                    out.println("unknown");
                }
            }
        }
        out.println();
    }

    private static void printNoteSection(final NoteSection ns) {
        out.printf("Displaying notes found in: %s%n", ns.getName());
        out.println("  Owner                Data size \tDescription");

        for (final NoteSectionEntry nse : ns.getEntries()) {
            out.printf(
                    "  %-20s 0x%08x\t%s%n",
                    nse.getName(), nse.getDescriptionLength(), nse.getType().getDescription());

            switch (nse.getType()) {
                case NT_GNU_ABI_TAG -> printGNUABITag(nse);
                case NT_GNU_BUILD_ID -> out.printf(
                        "    Build ID: %s%n",
                        IntStream.range(0, nse.getDescriptionLength())
                                .mapToObj(i -> String.format("%02x", nse.getDescriptionByte(i)))
                                .collect(Collectors.joining()));
                case NT_GNU_PROPERTY_TYPE_0 -> printGNUProperties(nse);
                default -> throw new IllegalArgumentException(
                        String.format("Unknown note section entry type '%s'", nse.getType()));
            }
        }
    }

    private static void printGNUProperties(final NoteSectionEntry nse) {
        final byte[] v = new byte[nse.getDescriptionLength()];
        for (int i = 0; i < v.length; i++) {
            v[i] = nse.getDescriptionByte(i);
        }
        final ReadOnlyByteBuffer robb = new ReadOnlyByteBufferV1(v, true);
        while (robb.getPosition() < nse.getDescriptionLength()) {
            final int code = robb.read4();
            final GnuPropertyType type = GnuPropertyType.fromCode(code);
            switch (type) {
                case GNU_PROPERTY_NO_COPY_ON_PROTECTED,
                        GNU_PROPERTY_STACK_SIZE,
                        GNU_PROPERTY_X86_ISA_1_USED -> throw new UnsupportedOperationException(
                        "Unimplemented case: " + type);
                case GNU_PROPERTY_X86_ISA_1_NEEDED -> {
                    out.print("        x86 ISA needed: ");
                    int x = robb.read4();
                    while (robb.getPosition() < nse.getDescriptionLength() && x != 0) {
                        if ((x & 1) != 0) {
                            out.print("x86-64-baseline");
                            x &= 0xfffffffe;
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                        if ((x & 2) != 0) {
                            out.print("x86-64-v2");
                            x &= 0xfffffffc;
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                        if ((x & 4) != 0) {
                            out.print("x86-64-v3");
                            x &= 0xfffffff8;
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                        if ((x & 8) != 0) {
                            out.print("x86-64-v4");
                            x &= 0xfffffff0;
                            if (x != 0) {
                                out.print(", <unknown>");
                            }
                        }

                        if (robb.getPosition() < nse.getDescriptionLength()) {
                            x = robb.read4();
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                    }
                    out.println();
                }
                case GNU_PROPERTY_X86_FEATURE_1_AND -> {
                    out.print("      Properties: x86 feature: ");
                    int x = robb.read4();
                    while (robb.getPosition() < nse.getDescriptionLength() && x != 0) {
                        if ((x & 1) != 0) {
                            out.print("IBT");
                            x &= 0xfffffffe;
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                        if ((x & 2) != 0) {
                            out.print("SHSTK");
                            x &= 0xfffffffc;
                            if (x != 0) {
                                out.print(", <unknown>");
                            }
                        }

                        if (robb.getPosition() < nse.getDescriptionLength()) {
                            x = robb.read4();
                            if (x != 0) {
                                out.print(", ");
                            }
                        }
                    }
                    out.println();
                }
            }
        }
    }

    private static void printGNUABITag(final NoteSectionEntry nse) {
        final byte[] v = new byte[nse.getDescriptionLength()];
        for (int i = 0; i < v.length; i++) {
            v[i] = nse.getDescriptionByte(i);
        }
        final ReadOnlyByteBuffer robb = new ReadOnlyByteBufferV1(v, true);
        final int osCode = robb.read4();
        out.printf(
                "    OS: %s, ABI: %d.%d.%d%n",
                switch (osCode) {
                    case 0 -> "Linux";
                    case 1 -> "GNU";
                    case 2 -> "Solaris2";
                    case 3 -> "FreeBSD";
                    default -> throw new IllegalArgumentException(
                            String.format("Unknown ABI tag %d (0x%08x)", osCode, osCode));
                },
                robb.read4(),
                robb.read4(),
                robb.read4());
    }

    private static void printSectionDetails(final ELF elf) {
        out.print(String.join(
                "\n",
                "Section Headers:",
                "  [Nr] Name",
                "       Type              Address          Offset            Link",
                "       Size              EntSize          Info              Align",
                "       Flags"));

        final Section[] sections = elf.sectionTable();
        for (int i = 0; i < sections.length; i++) {
            final Section s = sections[i];
            final SectionHeader sh = s.getHeader();
            out.printf("  [%2d] %s%n", i, s.getName());
            out.printf(
                    "       %-16s %016x %016x %d%n",
                    sh.getType().getName(), sh.getVirtualAddress(), sh.getFileOffset(), sh.getLinkedSectionIndex());
            out.printf(
                    "       %016x %016x %-16d %d%n",
                    sh.getSectionSize(), sh.getEntrySize(), sh.getInfo(), sh.getAlignment());
            out.printf(
                    "       [%016x]: %s%n",
                    sh.getFlags().stream()
                            .mapToLong(SectionHeaderFlags::getCode)
                            .reduce(0L, (a, b) -> a | b),
                    sh.getFlags().stream().map(SectionHeaderFlags::getName).collect(Collectors.joining(", ")));
        }
    }

    private static void printSymbolTable(final SymbolTable s, final Section... sectionTable) {
        out.printf("Symbol table '%s' contains %d entries:%n", s.getName(), s.getSymbolTableLength());
        out.println("   Num:    Value          Size Type    Bind   Vis      Ndx Name");
        final StringTableSection strtab =
                (StringTableSection) sectionTable[s.getHeader().getLinkedSectionIndex()];
        for (int i = 0; i < s.getSymbolTableLength(); i++) {
            final SymbolTableEntry ste = s.getSymbolTableEntry(i);
            out.printf(
                    "    %2d: %016x  %4d %-6s  %-6s %-7s  %3s %s%n",
                    i,
                    ste.value(),
                    ste.size(),
                    ste.info().getType().getName(),
                    ste.info().getBinding().getName(),
                    ste.visibility().getName(),
                    ste.sectionTableIndex() == 0
                            ? "UND"
                            : (ste.sectionTableIndex() < 0 ? "ABS" : ste.sectionTableIndex()),
                    strtab.getString(ste.nameOffset()));
        }
    }

    private static void printSectionHeaders(final ELF elf) {
        out.println(
                """
                        Section Headers:
                          [Nr] Name              Type             Address           Offset
                               Size              EntSize          Flags  Link  Info  Align""");

        final Section[] sections = elf.sectionTable();
        for (int i = 0; i < sections.length; i++) {
            final Section s = sections[i];
            final SectionHeader sh = s.getHeader();
            out.printf(
                    "  [%2d] %-17s %-16s %016x  %08x%n       %016x  %016x %3s    %4d  %4d     %d%n",
                    i,
                    s.getName().length() > 17 ? s.getName().substring(0, 17 - 5) + "[...]" : s.getName(),
                    sh.getType().getName(),
                    sh.getVirtualAddress(),
                    sh.getFileOffset(),
                    sh.getSectionSize(),
                    sh.getEntrySize(),
                    sh.getFlags().stream()
                            .map(SectionHeaderFlags::getId)
                            .collect(Collector.of(
                                    StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append,
                                    StringBuilder::toString)),
                    sh.getLinkedSectionIndex(),
                    sh.getInfo(),
                    sh.getAlignment());
        }

        out.println(
                """
                        Key to Flags:
                          W (write), A (alloc), X (execute), M (merge), S (strings), I (info),
                          L (link order), O (extra OS processing required), G (group), T (TLS),
                          C (compressed), x (unknown), o (OS specific), E (exclude),
                          D (mbind), l (large), p (processor specific)""");
    }

    private static void printProgramHeaders(final ELF elf) {
        out.println(
                """
                        Program Headers:
                          Type           Offset             VirtAddr           PhysAddr
                                         FileSiz            MemSiz              Flags  Align""");

        final PHTEntry[] pht = elf.programHeaderTable();
        for (final PHTEntry phte : pht) {
            out.printf(
                    "  %-14s 0x%016x 0x%016x 0x%016x%n                 0x%016x 0x%016x  %3s    0x%x%n",
                    phte.getType().getName(),
                    phte.getSegmentOffset(),
                    phte.getSegmentVirtualAddress(),
                    phte.getSegmentPhysicalAddress(),
                    phte.getSegmentFileSize(),
                    phte.getSegmentMemorySize(),
                    (phte.isReadable() ? "R" : " ")
                            + (phte.isWriteable() ? "W" : " ")
                            + (phte.isExecutable() ? "E" : " "),
                    phte.getAlignment());
            if (phte.getType() == PHTEntryType.PT_INTERP) {
                out.printf(
                        "      [Requesting program interpreter: %s]%n",
                        ((InterpreterPathSection) elf.getSectionByName(".interp")).getInterpreterFilePath());
            }
        }
    }

    private static void printSectionToSegmentMapping(final ELF elf) {
        out.println(" Section to Segment mapping:\n" + //
                "  Segment Sections...");

        final PHTEntry[] pht = elf.programHeaderTable();
        final Section[] sections = elf.sectionTable();
        for (int i = 0; i < pht.length; i++) {
            final PHTEntry phte = pht[i];
            out.printf(
                    "   %02d     %s %n",
                    i,
                    Arrays.stream(sections)
                            .filter(s -> {
                                final long segmentStart = phte.getSegmentOffset();
                                final long segmentEnd = segmentStart + phte.getSegmentFileSize();
                                final long sectionStart = s.getHeader().getFileOffset();
                                final long sectionEnd =
                                        sectionStart + s.getHeader().getSectionSize();
                                return s.getHeader().getType() != SectionHeaderType.SHT_NULL
                                        && sectionStart >= segmentStart
                                        && sectionEnd <= segmentEnd;
                            })
                            .map(Section::getName)
                            .collect(Collectors.joining(" ")));
        }
    }

    private static void printFileHeader(final String filename, final ELF elf) {
        out.println("ELF Header:");
        {
            final ReadOnlyByteBuffer bb;
            try {
                bb = new ReadOnlyByteBufferV1(Files.readAllBytes(Path.of(filename)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.printf(
                    "  Magic:   %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x%n",
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1(),
                    bb.read1());
        }

        final FileHeader fh = elf.fileHeader();

        out.printf("  Class:                             %s%n", fh.is32Bit() ? "ELF32" : "ELF64");
        out.printf(
                "  Data:                              %s%n",
                fh.isLittleEndian() ? "2's complement, little endian" : "2's complement, big endian");
        out.println("  Version:                           1 (current)");
        out.printf("  OS/ABI:                            %s%n", fh.getOSABI().getName());
        out.printf("  ABI Version:                       %s%n", fh.getABIVersion());
        out.printf("  Type:                              %s%n", fh.getFileType().getName());
        out.printf("  Machine:                           %s%n", fh.getISA().getName());
        out.printf("  Version:                           0x%x%n", fh.getVersion());
        out.printf("  Entry point address:               0x%x%n", fh.getEntryPointVirtualAddress());
        out.printf("  Start of program headers:          %d (bytes into file)%n", fh.getProgramHeaderTableOffset());
        out.printf("  Start of section headers:          %d (bytes into file)%n", fh.getSectionHeaderTableOffset());
        out.printf("  Flags:                             0x%x%n", fh.getFlags());
        out.printf("  Size of this header:               %d (bytes)%n", fh.getHeaderSize());
        out.printf("  Size of program headers:           %d (bytes)%n", fh.getProgramHeaderTableEntrySize());
        out.printf("  Number of program headers:         %d%n", fh.getNumProgramHeaderTableEntries());
        out.printf("  Size of section headers:           %d (bytes)%n", fh.getSectionHeaderTableEntrySize());
        out.printf("  Number of section headers:         %d%n", fh.getNumSectionHeaderTableEntries());
        out.printf("  Section header string table index: %d%n", fh.getSectionHeaderStringTableIndex());
        out.flush();
    }

    private static void printHelp() {
        out.println(
                """
                        Usage: readelf <option(s)> elf-file(s)
                         Display information about the contents of ELF format files
                         Options are:
                          -a --all               Equivalent to: -h -l -S -s -r -d -V -A -I
                          -h --file-header       Display the ELF file header
                          -l --program-headers   Display the program headers
                             --segments          An alias for --program-headers
                          -S --section-headers   Display the sections' header
                             --sections          An alias for --section-headers
                          -g --section-groups    Display the section groups
                          -t --section-details   Display the section details
                          -e --headers           Equivalent to: -h -l -S
                          -s --syms              Display the symbol table
                             --symbols           An alias for --syms
                             --dyn-syms          Display the dynamic symbol table
                             --lto-syms          Display LTO symbol tables
                          -n --notes             Display the core notes (if present)
                          -r --relocs            Display the relocations (if present)
                          -u --unwind            Display the unwind info (if present)
                          -d --dynamic           Display the dynamic section (if present)
                          -V --version-info      Display the version sections (if present)
                          -A --arch-specific     Display architecture specific information (if any)
                          -c --archive-index     Display the symbol/file index in an archive
                          -D --use-dynamic       Use the dynamic section info when displaying symbols
                          -L --lint|--enable-checks
                                                 Display warning messages for possible problems
                          -x --hex-dump=<number|name>
                                                 Dump the contents of section <number|name> as bytes
                          -p --string-dump=<number|name>
                                                 Dump the contents of section <number|name> as strings
                          -R --relocated-dump=<number|name>
                                                 Dump the relocated contents of section <number|name>
                          -I --histogram         Display histogram of bucket list lengths
                          -W --wide              Allow output width to exceed 80 characters
                          -T --silent-truncation If a symbol name is truncated, do not add [...] suffix
                          -H --help              Display this information
                          -v --version           Display the version number of readelf""");
    }
}
