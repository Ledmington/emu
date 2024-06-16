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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFReader;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.DynamicSection;
import com.ledmington.elf.section.DynamicSymbolTableSection;
import com.ledmington.elf.section.DynamicTableEntry;
import com.ledmington.elf.section.DynamicTableEntryTag;
import com.ledmington.elf.section.GnuVersionRequirementEntry;
import com.ledmington.elf.section.GnuVersionRequirementsSection;
import com.ledmington.elf.section.GnuVersionSection;
import com.ledmington.elf.section.InterpreterPathSection;
import com.ledmington.elf.section.NoteSection;
import com.ledmington.elf.section.NoteSectionEntry;
import com.ledmington.elf.section.RelocationAddendEntry;
import com.ledmington.elf.section.RelocationAddendSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.SectionHeaderType;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.SymbolTable;
import com.ledmington.elf.section.SymbolTableEntry;
import com.ledmington.elf.section.SymbolTableSection;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

public final class Main {

    private static final PrintWriter out = System.console() != null ? System.console().writer()
            : new PrintWriter(System.out);

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
        for (final String arg : args) {
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
                    break;
                case "-l", "--program-headers", "--segments":
                    displayProgramHeaders = true;
                    break;
                case "-S", "--section-headers", "--sections":
                    displaySectionHeaders = true;
                    break;
                case "-g", "--section-groups":
                    notImplemented();
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
                    notImplemented();
                    break;
                case "-p":
                    // --string-dump=<number|name>
                    notImplemented();
                    break;
                case "-R":
                    // --relocated-dump=<number|name>
                    notImplemented();
                    break;

                // TODO: add the other CLI flags

                default:
                    if (arg.startsWith("-")) {
                        out.printf("readelf: Error: Invalid option '%s'%n", arg);
                        printHelp();
                        out.flush();
                        System.exit(0);
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

        if (displayFileHeader) {
            printFileHeader(filename, elf);
        }

        if (displaySectionHeaders) {
            if (displayFileHeader) {
                out.println();
            }
            printSectionHeaders(elf);
        }

        if (displaySectionDetails) {
            if (displaySectionHeaders) {
                out.println();
            }
            printSectionDetails(elf);
        }

        if (displayProgramHeaders) {
            if (displaySectionHeaders) {
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

        if (displayDynamicSymbolTable) {
            printSymbolTable((DynamicSymbolTableSection) elf.getSectionByName(".dynsym"), elf.sectionTable());
        }

        if (displaySymbolTable) {
            printSymbolTable((SymbolTableSection) elf.getSectionByName(".symtab"), elf.sectionTable());
        }

        if (displayNoteSections) {
            out.println();
            printNoteSections(elf);
        }

        if (displayRelocationSections) {
            out.println();
            printRelocationSection((RelocationAddendSection) elf.getSectionByName(".rela.dyn"));
            printRelocationSection((RelocationAddendSection) elf.getSectionByName(".rela.plt"));
        }

        if (displayDynamicSection) {
            out.println();
            printDynamicSection((DynamicSection) elf.getSectionByName(".dynamic"),
                    (StringTableSection) elf.getSectionByName(".strtab"));
        }

        if (displayVersionSections) {
            out.println();
            printVersionSection((GnuVersionSection) elf.getSectionByName(".gnu.version"), elf.sectionTable());
            out.println();
            printVersionSection(
                    (GnuVersionRequirementsSection) elf.getSectionByName(".gnu.version_r"), elf.sectionTable());
        }

        out.flush();
        System.exit(0);
    }

    private static void notImplemented() {
        out.println("This flag is not implemented yet");
        out.flush();
        System.exit(-1);
    }

    private static void printVersionSection(final Section s, final Section[] sectionTable) {
        final SectionHeader sh = s.getHeader();
        if (s instanceof GnuVersionRequirementsSection gvrs) {
            final GnuVersionRequirementEntry[] entries = gvrs.getEntries();
            out.printf(
                    "Version needs section '%s' contains %d entr%s:%n",
                    s.getName(), entries.length, entries.length == 1 ? "y" : "ies");
            out.printf(
                    " Addr: 0x%016x Offset: 0x%08x Link: %d (%s)%n",
                    sh.getVirtualAddress(),
                    sh.getFileOffset(),
                    sh.getLinkedSectionIndex(),
                    sectionTable[sh.getLinkedSectionIndex()].getName());
            final StringTableSection strtab = (StringTableSection) sectionTable[sh.getLinkedSectionIndex()];
            for (int i = 0; i < entries.length; i++) {
                out.printf(
                        "  %06x: Version: %d  File: %s  Cnt: %d%n",
                        i, entries[i].version(), strtab.getString(entries[i].fileOffset()), entries[i].count());
                out.printf("%s%n", entries[i]);
            }
        } else if (s instanceof GnuVersionSection gvs) {
            final short[] versions = gvs.getVersions();
            final Section linkedSection = sectionTable[sh.getLinkedSectionIndex()];
            out.printf(
                    "Version symbols section '%s' contains %d entr%s:%n",
                    s.getName(), versions.length, versions.length == 1 ? "y" : "ies");
            out.printf(
                    " Addr: 0x%016x Offset: 0x%08x Link: %d (%s)%n",
                    sh.getVirtualAddress(), sh.getFileOffset(), sh.getLinkedSectionIndex(), linkedSection.getName());
            final SymbolTableEntry[] symbolTable = ((SymbolTable) linkedSection).getSymbolTable();
            final StringTableSection stringTable = (StringTableSection) sectionTable[linkedSection.getHeader()
                    .getLinkedSectionIndex()];
            for (int i = 0; i < versions.length; i++) {
                if (i % 4 == 0) {
                    out.printf("  %03x: ", i);
                }
                out.printf(
                        "%2d (%s)\t",
                        versions[i],
                        versions[i] == 0
                                ? "*local*"
                                : (versions[i] == 1
                                        ? "*global*"
                                        : stringTable.getString(symbolTable[i].getNameOffset())));
                if (i % 4 == 3) {
                    out.println();
                }
            }
            out.println();
        } else {
            out.printf("Unknown version section '%s'%n", s.getName());
        }
    }

    private static void printDynamicSection(final DynamicSection ds, final StringTableSection strtab) {
        final SectionHeader dsh = ds.getHeader();
        final DynamicTableEntry[] dyntab = ds.getDynamicTable();
        out.printf("Dynamic section at offset 0x%x contains %d entries:%n", dsh.getFileOffset(), dyntab.length);
        out.println("  Tag        Type                         Name/Value");

        for (final DynamicTableEntry dte : dyntab) {
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
                // FIXME
                out.printf("Shared object: [%s]%n", strtab.getString(BitUtils.asInt(content)));
            } else {
                out.printf("%nUnknown dynamic table tag '%s'%n", dte.getTag());
            }
        }
    }

    private static void printRelocationSection(final RelocationAddendSection ras) {
        final SectionHeader rash = ras.getHeader();
        final RelocationAddendEntry[] relocationAddendTable = ras.getRelocationAddendTable();
        out.printf(
                "Relocation section '%s' at offset 0x%x contains %,d entr%s:%n",
                ras.getName(),
                rash.getFileOffset(),
                relocationAddendTable.length,
                relocationAddendTable.length == 1 ? "y" : "ies");
        out.println("  Offset          Info           Type           Sym. Value    Sym. Name + Addend");
        for (final RelocationAddendEntry rae : relocationAddendTable) {
            out.printf("%016x  %016x %s%n", rae.offset(), rae.info(), "TODO");
        }
        out.println();
    }

    private static void printNoteSections(final ELF elf) {
        for (final Section s : elf.sectionTable()) {
            if (!s.getName().startsWith(".note")) {
                continue;
            }

            final NoteSection ns = (NoteSection) s;
            out.printf("Displaying notes found in: %s%n", s.getName());
            out.println("  Owner                Data size     Description");

            for (final NoteSectionEntry nse : ns.getEntries()) {
                out.printf("  %-20s 0x%08x     %016x TODO%n", nse.name(), nse.getSize(), nse.type());
            }
            out.println();
        }
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
                    sh.getFlags().stream().mapToLong(f -> f.getCode()).reduce(0L, (a, b) -> a | b),
                    sh.getFlags().stream().map(f -> f.getName()).collect(Collectors.joining(", ")));
        }
    }

    private static void printSymbolTable(final SymbolTable s, final Section[] sectionTable) {
        final SymbolTableEntry[] st = s.getSymbolTable();
        out.printf("Symbol table '%s' contains %d entries:%n", s.getName(), st.length);
        out.println("   Num:    Value          Size Type    Bind   Vis      Ndx Name");
        final StringTableSection strtab = (StringTableSection) sectionTable[s.getHeader().getLinkedSectionIndex()];
        for (int i = 0; i < st.length; i++) {
            final SymbolTableEntry ste = st[i];
            out.printf(
                    "    %2d: %016x  %4d %-6s  %-6s %-7s  %3s %s%n",
                    i,
                    ste.getValue(),
                    ste.getSize(),
                    ste.getInfo().getType().getName(),
                    ste.getInfo().getBinding().getName(),
                    ste.getVisibility().getName(),
                    ste.getSectionTableIndex() == 0
                            ? "UND"
                            : (ste.getSectionTableIndex() < 0 ? "ABS" : ste.getSectionTableIndex()),
                    strtab.getString(ste.getNameOffset()));
        }
        out.println();
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
                    "   %02d    %s%n",
                    i,
                    Arrays.stream(sections)
                            .filter(s -> {
                                final long segmentStart = phte.getSegmentOffset();
                                final long segmentEnd = segmentStart + phte.getSegmentFileSize();
                                final long sectionStart = s.getHeader().getFileOffset();
                                final long sectionEnd = sectionStart + s.getHeader().getSectionSize();
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
        out.printf("  Class:                             %s%n", elf.fileHeader().is32Bit() ? "ELF32" : "ELF64");
        out.printf(
                "  Data:                              %s%n",
                elf.fileHeader().isLittleEndian() ? "2's complement, little-endian" : "2's complement, big-endian");
        out.println("  Version:                           1 (current)");
        out.printf(
                "  OS/ABI:                            %s%n",
                elf.fileHeader().getOSABI().getName());
        out.printf("  ABI Version:                       %s%n", elf.fileHeader().getABIVersion());
        out.printf(
                "  Type:                              %s%n",
                elf.fileHeader().getFileType().getName());
        out.printf(
                "  Machine:                           %s%n",
                elf.fileHeader().getISA().getName());
        out.printf(
                "  Version:                           0x%x%n", elf.fileHeader().getVersion());
        out.printf(
                "  Entry point address:               0x%x%n", elf.fileHeader().getEntryPointVirtualAddress());
        out.printf(
                "  Start of program headers:          %d (bytes into file)%n",
                elf.fileHeader().getProgramHeaderTableOffset());
        out.printf(
                "  Start of section headers:          %d (bytes into file)%n",
                elf.fileHeader().getSectionHeaderTableOffset());
        out.printf(
                "  Flags:                             0x%x%n", elf.fileHeader().getFlags());
        out.printf(
                "  Size of this header:               %d (bytes)%n",
                elf.fileHeader().getHeaderSize());
        out.printf(
                "  Size of program headers:           %d (bytes)%n",
                elf.fileHeader().getProgramHeaderTableEntrySize());
        out.printf("  Number of program headers:         %d%n", elf.fileHeader().getNumProgramHeaderTableEntries());
        out.printf(
                "  Size of section headers:           %d (bytes)%n",
                elf.fileHeader().getSectionHeaderTableEntrySize());
        out.printf("  Number of section headers:         %d%n", elf.fileHeader().getNumSectionHeaderTableEntries());
        out.printf("  Section header string table index: %d%n", elf.fileHeader().getSectionHeaderStringTableIndex());
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
                          -H --help              Display this information
                          -v --version           Display the version number of readelf""");
    }
}
