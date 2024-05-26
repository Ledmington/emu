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
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.DynamicSymbolTableSection;
import com.ledmington.elf.section.InterpreterPathSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderType;
import com.ledmington.elf.section.SymbolTable;
import com.ledmington.elf.section.SymbolTableEntry;
import com.ledmington.elf.section.SymbolTableSection;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

public final class Main {

    private static final PrintWriter out =
            System.console() != null ? System.console().writer() : new PrintWriter(System.out);

    public static void main(final String[] args) {
        MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

        String filename = null;
        boolean displayFileHeader = false;
        boolean displaySectionHeaders = false;
        boolean displayProgramHeaders = false;
        boolean displaySectionToSegmentMapping = false;
        boolean displayDynamicSymbolTable = false;
        boolean displaySymbolTable = false;
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
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-l", "--program-headers", "--segments":
                    displayProgramHeaders = true;
                    break;
                case "-S", "--section-headers", "--sections":
                    displaySectionHeaders = true;
                    break;
                case "-g", "--section-groups":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-t", "--section-details":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
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
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "--lto-syms":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-n", "--notes":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-r", "--relocs":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-u", "--unwind":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-d", "--dynamic":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-V", "--version-info":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-A", "--arch-specific":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-c", "--archive-index":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-D", "--use-dynamic":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-L", "--lint", "--enable-checks":
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-x":
                    // --hex-dump=<number|name>
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-p":
                    // --string-dump=<number|name>
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;
                case "-R":
                    // --relocated-dump=<number|name>
                    System.out.println("This flag is not implemented yet");
                    System.exit(-1);
                    break;

                    // TODO: add the other CLI flags

                default:
                    if (arg.startsWith("-")) {
                        out.printf("readelf: Error: Invalid option '%s'\n", arg);
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
            elf = ELFParser.parse(Files.readAllBytes(Path.of(filename)));
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
            printSymbolTable((DynamicSymbolTableSection) elf.getFirstSectionByName(".dynsym"));
        }

        if (displaySymbolTable) {
            printSymbolTable((SymbolTableSection) elf.getFirstSectionByName(".symtab"));
        }

        out.flush();
        System.exit(0);
    }

    private static void printSymbolTable(final SymbolTable s) {
        final SymbolTableEntry[] st = s.getSymbolTable();
        out.printf("Symbol table '%s' contains %d entries:\n", s.getName(), st.length);
        out.println("   Num:    Value          Size Type    Bind   Vis      Ndx Name");
        for (int i = 0; i < st.length; i++) {
            final SymbolTableEntry ste = st[i];
            out.printf(
                    "    %2d: %016x  %4d %-6s  %-6s %-7s  %3s %s\n",
                    i,
                    ste.getValue(),
                    ste.getSize(),
                    ste.getInfo().getType().getName(),
                    ste.getInfo().getBind().getName(),
                    ste.getVisibility().getName(),
                    ste.getSectionTableIndex() == 0
                            ? "UND"
                            : (ste.getSectionTableIndex() < 0 ? "ABS" : ste.getSectionTableIndex()),
                    "TODO");
        }
        out.println();
    }

    private static void printSectionHeaders(final ELF elf) {
        out.println("Section Headers:\n" + //
                "  [Nr] Name              Type             Address           Offset\n"
                + //
                "       Size              EntSize          Flags  Link  Info  Align");

        final Section[] sections = elf.getSectionTable();
        final int maxNameLength = 17;
        final int maxTypeLength = 16;
        final String formatString = String.format(
                "  [%%2d] %%-%ds %%-%ds %%016x  %%08x\n       %%016x  %%016x %%3s    %%4d  %%4d     %%d\n",
                maxNameLength, maxTypeLength);
        for (int i = 0; i < sections.length; i++) {
            final Section s = sections[i];
            final SectionHeader sh = s.getHeader();
            out.printf(
                    formatString,
                    i,
                    s.getName().length() > maxNameLength
                            ? s.getName().substring(0, maxNameLength - 5) + "[...]"
                            : s.getName(),
                    sh.getType().getName(),
                    sh.getVirtualAddress(),
                    sh.getFileOffset(),
                    sh.getSectionSize(),
                    sh.getEntrySize(),
                    Arrays.stream(sh.getFlags())
                            .map(shf -> shf.getId())
                            .collect(Collector.of(
                                    StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append,
                                    StringBuilder::toString)),
                    sh.getLinkedSectionIndex(),
                    sh.getInfo(),
                    sh.getAlignment());
        }

        out.println("Key to Flags:\n" + "  W (write), A (alloc), X (execute), M (merge), S (strings), I (info),\n"
                + "  L (link order), O (extra OS processing required), G (group), T (TLS),\n"
                + "  C (compressed), x (unknown), o (OS specific), E (exclude),\n"
                + "  D (mbind), l (large), p (processor specific)");
    }

    private static void printProgramHeaders(final ELF elf) {
        out.println("Program Headers:\n" + //
                "  Type           Offset             VirtAddr           PhysAddr\n"
                + //
                "                 FileSiz            MemSiz              Flags  Align");

        final PHTEntry[] pht = elf.getProgramHeaderTable();
        for (final PHTEntry phte : pht) {
            out.printf(
                    "  %-14s 0x%016x 0x%016x 0x%016x\n                 0x%016x 0x%016x  %3s    0x%x\n",
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
                        "      [Requesting program interpreter: %s]\n",
                        ((InterpreterPathSection) elf.getFirstSectionByName(".interp")).getInterpreterFilePath());
            }
        }
    }

    private static void printSectionToSegmentMapping(final ELF elf) {
        out.println(" Section to Segment mapping:\n" + //
                "  Segment Sections...");

        final PHTEntry[] pht = elf.getProgramHeaderTable();
        final Section[] sections = elf.getSectionTable();
        for (int i = 0; i < pht.length; i++) {
            final PHTEntry phte = pht[i];
            out.printf(
                    "   %02d    %s\n",
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
                            .map(s -> s.getName())
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
                    "  Magic:   %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n",
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
        out.printf(
                "  Class:                             %s\n", elf.getFileHeader().is32Bit() ? "ELF32" : "ELF64");
        out.printf(
                "  Data:                              %s\n",
                elf.getFileHeader().isLittleEndian() ? "2's complement, little-endian" : "2's complement, big-endian");
        out.println("  Version:                           1 (current)");
        out.printf(
                "  OS/ABI:                            %s\n",
                elf.getFileHeader().getOSABI().getName());
        out.printf(
                "  ABI Version:                       %s\n", elf.getFileHeader().getABIVersion());
        out.printf(
                "  Type:                              %s\n",
                elf.getFileHeader().getFileType().getName());
        out.printf(
                "  Machine:                           %s\n",
                elf.getFileHeader().getISA().getName());
        out.printf(
                "  Version:                           0x%x\n",
                elf.getFileHeader().getVersion());
        out.printf(
                "  Entry point address:               0x%x\n",
                elf.getFileHeader().getEntryPointVirtualAddress());
        out.printf(
                "  Start of program headers:          %d (bytes into file)\n",
                elf.getFileHeader().getProgramHeaderTableOffset());
        out.printf(
                "  Start of section headers:          %d (bytes into file)\n",
                elf.getFileHeader().getSectionHeaderTableOffset());
        out.printf(
                "  Flags:                             0x%x\n",
                elf.getFileHeader().getFlags());
        out.printf(
                "  Size of this header:               %d (bytes)\n",
                elf.getFileHeader().getHeaderSize());
        out.printf(
                "  Size of program headers:           %d (bytes)\n",
                elf.getFileHeader().getProgramHeaderTableEntrySize());
        out.printf(
                "  Number of program headers:         %d\n", elf.getFileHeader().getNumProgramHeaderTableEntries());
        out.printf(
                "  Size of section headers:           %d (bytes)\n",
                elf.getFileHeader().getSectionHeaderTableEntrySize());
        out.printf(
                "  Number of section headers:         %d\n", elf.getFileHeader().getNumSectionHeaderTableEntries());
        out.printf(
                "  Section header string table index: %d\n", elf.getFileHeader().getSectionHeaderStringTableIndex());
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
                        	 --sym-base=[0|8|10|16]
                        						 Force base for symbol sizes.  The options are
                        						 mixed (the default), octal, decimal, hexadecimal.
                          -C --demangle[=STYLE]  Decode mangled/processed symbol names
                        						   STYLE can be "none", "auto", "gnu-v3", "java",
                        						   "gnat", "dlang", "rust"
                        	 --no-demangle       Do not demangle low-level symbol names.  (default)
                        	 --recurse-limit     Enable a demangling recursion limit.  (default)
                        	 --no-recurse-limit  Disable a demangling recursion limit
                        	 -U[dlexhi] --unicode=[default|locale|escape|hex|highlight|invalid]
                        						 Display unicode characters as determined by the current locale
                        						  (default), escape sequences, "<hex sequences>", highlighted
                        						  escape sequences, or treat them as invalid and display as
                        						  "{hex sequences}"
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
                          -z --decompress        Decompress section before dumping it
                          -w --debug-dump[a/=abbrev, A/=addr, r/=aranges, c/=cu_index, L/=decodedline,
                        				  f/=frames, F/=frames-interp, g/=gdb_index, i/=info, o/=loc,
                        				  m/=macro, p/=pubnames, t/=pubtypes, R/=Ranges, l/=rawline,
                        				  s/=str, O/=str-offsets, u/=trace_abbrev, T/=trace_aranges,
                        				  U/=trace_info]
                        						 Display the contents of DWARF debug sections
                          -wk --debug-dump=links Display the contents of sections that link to separate
                        						  debuginfo files
                          -P --process-links     Display the contents of non-debug sections in separate
                        						  debuginfo files.  (Implies -wK)
                          -wK --debug-dump=follow-links
                        						 Follow links to separate debug info files (default)
                          -wN --debug-dump=no-follow-links
                        						 Do not follow links to separate debug info files
                          --dwarf-depth=N        Do not display DIEs at depth N or greater
                          --dwarf-start=N        Display DIEs starting at offset N
                          --ctf=<number|name>    Display CTF info from section <number|name>
                          --ctf-parent=<name>    Use CTF archive member <name> as the CTF parent
                          --ctf-symbols=<number|name>
                        						 Use section <number|name> as the CTF external symtab
                          --ctf-strings=<number|name>
                        						 Use section <number|name> as the CTF external strtab
                          -I --histogram         Display histogram of bucket list lengths
                          -W --wide              Allow output width to exceed 80 characters
                          -T --silent-truncation If a symbol name is truncated, do not add [...] suffix
                          @<file>                Read options from <file>
                          -H --help              Display this information
                          -v --version           Display the version number of readelf""");
    }
}
