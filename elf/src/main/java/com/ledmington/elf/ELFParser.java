/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import com.ledmington.elf.section.BasicProgBitsSection;
import com.ledmington.elf.section.ConstructorsSection;
import com.ledmington.elf.section.DestructorsSection;
import com.ledmington.elf.section.DynamicSection;
import com.ledmington.elf.section.HashTableSection;
import com.ledmington.elf.section.InterpreterPathSection;
import com.ledmington.elf.section.NoBitsSection;
import com.ledmington.elf.section.NullSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderType;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.X86_64_Unwind;
import com.ledmington.elf.section.gnu.GnuHashSection;
import com.ledmington.elf.section.gnu.GnuVersionDefinitionSection;
import com.ledmington.elf.section.gnu.GnuVersionRequirementsSection;
import com.ledmington.elf.section.gnu.GnuVersionSection;
import com.ledmington.elf.section.note.BasicNoteSection;
import com.ledmington.elf.section.note.FDOPackagingMetadata;
import com.ledmington.elf.section.note.GnuBuildIDSection;
import com.ledmington.elf.section.note.GnuGoldVersion;
import com.ledmington.elf.section.note.GnuPropertySection;
import com.ledmington.elf.section.note.NoteABITagSection;
import com.ledmington.elf.section.note.SystemtapUSDTSection;
import com.ledmington.elf.section.rel.RelocationAddendSection;
import com.ledmington.elf.section.rel.RelocationSection;
import com.ledmington.elf.section.sym.DynamicSymbolTableSection;
import com.ledmington.elf.section.sym.SymbolTableSection;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

/** A parser of ELF files. This class is not meant to be instantiated but to be used through its static methods. */
public final class ELFParser {

	private static final MiniLogger logger = MiniLogger.getLogger("elf-parser");

	private static ReadOnlyByteBuffer b;

	private ELFParser() {}

	/**
	 * Parses the given file and returns an {@link ELF} file object.
	 *
	 * @param filename The name of the file to be parsed.
	 * @return An ELF file object.
	 */
	public static ELF parse(final String filename) {
		final File file = new File(filename);
		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("File '%s' does not exist.", filename));
		}

		final byte[] bytes;
		try {
			bytes = Files.readAllBytes(Paths.get(filename));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		logger.info("The file '%s' is %,d bytes long", filename, bytes.length);

		return parse(bytes);
	}

	/**
	 * Parses the given byte-array and returns an {@link ELF} file object.
	 *
	 * @param bytes The byte-array to be parsed.
	 * @return An ELF file object.
	 */
	public static ELF parse(final byte[] bytes) {
		b = new ReadOnlyByteBufferV1(bytes);
		final FileHeader fileHeader = parseFileHeader();
		final PHTEntry[] programHeaderTable = parseProgramHeaderTable(fileHeader);
		final SectionHeader[] sectionHeaderTable = parseSectionHeaderTable(fileHeader);
		final Section[] sectionTable = parseSectionTable(fileHeader, sectionHeaderTable);
		return new ELF(fileHeader, programHeaderTable, sectionTable);
	}

	private static FileHeader parseFileHeader() {
		final int magicNumber = b.read4BE();
		final int ELF_MAGIC_NUMBER = 0x7f454c46;
		if (magicNumber != ELF_MAGIC_NUMBER) {
			throw new IllegalArgumentException(
					String.format("Invalid magic number, expected 0x7f454c46 but was 0x%08x", magicNumber));
		}

		final byte format = b.read1();
		if (format != 1 && format != 2) {
			throw new IllegalArgumentException(
					String.format("Invalid bit format, expected 1 or 2 but was %,d (0x%02x)", format, format));
		}
		final boolean is32Bit = format == 1;

		final byte endianness = b.read1();
		if (endianness != 1 && endianness != 2) {
			throw new IllegalArgumentException(
					String.format("Invalid endianness, expected 1 or 2 but was %,d (0x%02x)", endianness, endianness));
		}
		final boolean isLittleEndian = endianness == 1;
		b.setEndianness(isLittleEndian);

		final byte ELFVersion = b.read1();
		if (ELFVersion != (byte) 0x01) {
			throw new IllegalArgumentException(
					String.format("Invalid ELF EI_VERSION, expected 1 but was %,d (0x%02x)", ELFVersion, ELFVersion));
		}

		final byte osabi = b.read1();
		if (!OSABI.isValid(osabi)) {
			throw new IllegalArgumentException(String.format("Invalid OS ABI value, was %,d (0x%02x)", osabi, osabi));
		}

		final byte ABIVersion = b.read1();

		for (int idx = 0; idx < 7; idx++) {
			final byte x = b.read1();
			if (x != (byte) 0x00) {
				logger.warning("Byte %,d (0x%08x) of EI_PAD was not 0x00 but 0x%02x", idx, b.getPosition(), x);
			}
		}

		final short fileType = b.read2();
		if (!FileType.isValid(fileType)) {
			throw new IllegalArgumentException(
					String.format("Invalid file type value, was %,d (0x%04x)", fileType, fileType));
		}

		final short isa = b.read2();
		if (!ISA.isValid(isa)) {
			throw new IllegalArgumentException(String.format("Invalid ISA value, was %,d (0x%04x)", isa, isa));
		}

		final int ELFVersion_2 = b.read4();
		final int currentELFVersion = 1;
		if (ELFVersion_2 != currentELFVersion) {
			throw new IllegalArgumentException(String.format(
					"Invalid ELF e_version, expected 1 but was %,d (0x%08x)", ELFVersion_2, ELFVersion_2));
		}
		if (BitUtils.asInt(ELFVersion) != ELFVersion_2) {
			throw new IllegalArgumentException(String.format(
					"ERROR: wrong ELF version, expected to be equal to EI_VERSION byte but was %,d (0x%08x)",
					ELFVersion_2, ELFVersion_2));
		}

		final long entryPointVirtualAddress = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long PHTOffset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long SHTOffset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();

		final int flags = b.read4();

		final short headerSize = b.read2();
		if ((is32Bit && headerSize != 52) || (!is32Bit && headerSize != 64)) {
			throw new IllegalArgumentException(String.format(
					"Invalid header size value, expected %,d but was %,d (0x%04x)",
					is32Bit ? 52 : 64, headerSize, headerSize));
		}

		final short PHTEntrySize = b.read2();
		final short nPHTEntries = b.read2();
		final short SHTEntrySize = b.read2();
		final short nSHTEntries = b.read2();
		final short namesSHTEntryIndex = b.read2();

		return new FileHeader(
				is32Bit,
				isLittleEndian,
				ELFVersion,
				OSABI.fromCode(osabi),
				ABIVersion,
				FileType.fromCode(fileType),
				ISA.fromCode(isa),
				entryPointVirtualAddress,
				PHTOffset,
				SHTOffset,
				flags,
				headerSize,
				PHTEntrySize,
				nPHTEntries,
				SHTEntrySize,
				nSHTEntries,
				namesSHTEntryIndex);
	}

	private static PHTEntry parseProgramHeaderEntry(final boolean is32Bit) {
		final int segmentType = b.read4();
		if (!PHTEntryType.isValid(segmentType)) {
			throw new IllegalArgumentException(
					String.format("Invalid segment type value, was %,d (0x%08x)", segmentType, segmentType));
		}

		int flags = 0;
		if (!is32Bit) {
			flags = b.read4();
		}

		final long segmentOffset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long segmentVirtualAddress = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long segmentPhysicalAddress = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long segmentSizeOnFile = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long segmentSizeInMemory = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();

		if (is32Bit) {
			flags = b.read4();
		}

		final long alignment = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		if (alignment != 0 && Long.bitCount(alignment) != 1) {
			throw new IllegalArgumentException(String.format(
					"Invalid value for alignment: expected 0 or a power of two but was %,d (0x%016x)",
					alignment, alignment));
		} else if (alignment != 0 && (segmentVirtualAddress % alignment) != (segmentOffset % alignment)) {
			throw new IllegalArgumentException(String.format(
					"Invalid value for alignment: expected 0x%016x %% %,d to be equal to 0x%016x %% %,d but wasn't",
					segmentVirtualAddress, alignment, segmentOffset, alignment));
		}

		return new PHTEntry(
				PHTEntryType.fromCode(segmentType),
				flags,
				segmentOffset,
				segmentVirtualAddress,
				segmentPhysicalAddress,
				segmentSizeOnFile,
				segmentSizeInMemory,
				alignment);
	}

	private static SectionHeader parseSectionHeaderEntry(final boolean is32Bit) {
		final int nameOffset = b.read4();
		final int type = b.read4();
		final long flags = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long virtualAddress = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long fileOffset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final long size = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		final int linkedSectionIndex = b.read4();
		final int sh_info = b.read4();

		final long alignment = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
		if (alignment != 0 && Long.bitCount(alignment) != 1) {
			throw new IllegalArgumentException(String.format(
					"Invalid value for alignment: expected a power of two but was %,d (0x%016x)",
					alignment, alignment));
		}

		final long entrySize = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();

		return new SectionHeader(
				nameOffset,
				SectionHeaderType.fromCode(type),
				flags,
				virtualAddress,
				fileOffset,
				size,
				linkedSectionIndex,
				sh_info,
				alignment,
				entrySize);
	}

	private static PHTEntry[] parseProgramHeaderTable(final FileHeader fileHeader) {
		final int nPHTEntries = fileHeader.numProgramHeaderTableEntries();
		final PHTEntry[] programHeaderTable = new PHTEntry[nPHTEntries];
		final int PHTOffset = (int) fileHeader.programHeaderTableOffset();
		final int PHTEntrySize = fileHeader.programHeaderTableEntrySize();

		for (int k = 0; k < nPHTEntries; k++) {
			b.setPosition(PHTOffset + k * PHTEntrySize);
			final PHTEntry programHeaderEntry = parseProgramHeaderEntry(fileHeader.is32Bit());
			programHeaderTable[k] = programHeaderEntry;
		}

		return programHeaderTable;
	}

	private static SectionHeader[] parseSectionHeaderTable(final FileHeader fileHeader) {
		final int nSHTEntries = fileHeader.numSectionHeaderTableEntries();
		final SectionHeader[] sectionHeaderTable = new SectionHeader[nSHTEntries];
		final int SHTOffset = (int) fileHeader.sectionHeaderTableOffset();
		final int SHTEntrySize = fileHeader.sectionHeaderTableEntrySize();

		for (int k = 0; k < nSHTEntries; k++) {
			b.setPosition(SHTOffset + k * SHTEntrySize);
			final SectionHeader sectionHeaderEntry = parseSectionHeaderEntry(fileHeader.is32Bit());
			sectionHeaderTable[k] = sectionHeaderEntry;
		}

		return sectionHeaderTable;
	}

	private static Section[] parseSectionTable(final FileHeader fileHeader, final SectionHeader... sectionHeaderTable) {
		final Section[] sectionTable = new Section[fileHeader.numSectionHeaderTableEntries()];

		final int shstrndx = fileHeader.sectionHeaderStringTableIndex();
		final int shstr_offset = (int) sectionHeaderTable[shstrndx].getFileOffset();
		DynamicSection dynamicSection = null;
		sectionTable[shstrndx] = new StringTableSection(".shstrtab", sectionHeaderTable[shstrndx], b);
		final StringTableSection shstrtab = (StringTableSection) sectionTable[shstrndx];

		// Since some section may require the .dynamic section for correct parsing, we
		// need to parse that first
		for (int k = 0; k < sectionHeaderTable.length; k++) {
			final SectionHeader sectionHeader = sectionHeaderTable[k];
			final String typeName = sectionHeader.getType().getName();

			if (typeName.equals(SectionHeaderType.SHT_DYNAMIC.getName())) {
				final String name = shstrtab.getString(sectionHeader.getNameOffset());
				logger.debug("Parsing %s (%s)", name, typeName);
				sectionTable[k] = new DynamicSection(name, sectionHeader, b, fileHeader.is32Bit());
				dynamicSection = (DynamicSection) sectionTable[k];
				break;
			}
		}

		for (int k = 0; k < sectionHeaderTable.length; k++) {
			if (sectionTable[k] != null) {
				// we already parsed this section
				continue;
			}

			final SectionHeader sh = sectionHeaderTable[k];

			b.setPosition(shstr_offset + sh.getNameOffset());
			final String name = shstrtab.getString(sh.getNameOffset());
			final String typeName = sh.getType().getName();
			logger.debug("Parsing %s (%s)", name, typeName);

			if (typeName.equals(SectionHeaderType.SHT_NULL.getName())) {
				sectionTable[k] = new NullSection(sh);
			} else if (".symtab".equals(name) || typeName.equals(SectionHeaderType.SHT_SYMTAB.getName())) {
				sectionTable[k] = new SymbolTableSection(name, sh, b, fileHeader.is32Bit());
			} else if (".shstrtab".equals(name)
					|| ".strtab".equals(name)
					|| typeName.equals(SectionHeaderType.SHT_STRTAB.getName())) {
				sectionTable[k] = new StringTableSection(name, sh, b);
			} else if (".dynsym".equals(name) || typeName.equals(SectionHeaderType.SHT_DYNSYM.getName())) {
				sectionTable[k] = new DynamicSymbolTableSection(name, sh, b, fileHeader.is32Bit());
			} else if (typeName.equals(SectionHeaderType.SHT_NOTE.getName())) {
				sectionTable[k] = switch (name) {
					case ".note.gnu.property" -> new GnuPropertySection(sh, b);
					case ".note.gnu.build-id" -> new GnuBuildIDSection(sh, b);
					case ".note.ABI-tag" -> new NoteABITagSection(sh, b);
					case ".note.gnu.gold-version" -> new GnuGoldVersion(sh, b);
					case ".note.stapsdt" -> new SystemtapUSDTSection(sh, b);
					case ".note.package" -> new FDOPackagingMetadata(sh, b);
					default -> new BasicNoteSection(name, sh, b);
				};
			} else if (typeName.equals(SectionHeaderType.SHT_GNU_HASH.getName())) {
				sectionTable[k] = new GnuHashSection(name, sh, b, fileHeader.is32Bit());
			} else if (typeName.equals(SectionHeaderType.SHT_HASH.getName())) {
				sectionTable[k] = new HashTableSection(name, sh, b);
			} else if (typeName.equals(SectionHeaderType.SHT_PROGBITS.getName())) {
				sectionTable[k] = ".interp".equals(name)
						? new InterpreterPathSection(sh, b)
						: new BasicProgBitsSection(name, sh, b);
			} else if (typeName.equals(SectionHeaderType.SHT_NOBITS.getName())) {
				sectionTable[k] = new NoBitsSection(name, sh);
			} else if (typeName.equals(SectionHeaderType.SHT_RELA.getName())) {
				sectionTable[k] = new RelocationAddendSection(name, sh, b, fileHeader.is32Bit(), fileHeader.isa());
			} else if (typeName.equals(SectionHeaderType.SHT_REL.getName())) {
				sectionTable[k] = new RelocationSection(name, sh, b, fileHeader.is32Bit());
			} else if (GnuVersionSection.getStandardName().equals(name)) {
				sectionTable[k] = new GnuVersionSection(sh, b);
			} else if (GnuVersionRequirementsSection.getStandardName().equals(name)) {
				sectionTable[k] = new GnuVersionRequirementsSection(sh, b, dynamicSection);
			} else if (GnuVersionDefinitionSection.getStandardName().equals(name)) {
				sectionTable[k] = new GnuVersionDefinitionSection(sh, b, dynamicSection);
			} else if (typeName.equals(SectionHeaderType.SHT_INIT_ARRAY.getName())) {
				sectionTable[k] = new ConstructorsSection(name, sh, b, dynamicSection, fileHeader.is32Bit());
			} else if (typeName.equals(SectionHeaderType.SHT_FINI_ARRAY.getName())) {
				sectionTable[k] = new DestructorsSection(name, sh, b, dynamicSection, fileHeader.is32Bit());
			} else if (typeName.equals(SectionHeaderType.SHT_X86_64_UNWIND.getName())) {
				sectionTable[k] = new X86_64_Unwind(name, sh);
			} else {
				throw new IllegalArgumentException(String.format(
						"Don't know how to parse section n.%,d with type '%s' and name '%s'", k, typeName, name));
			}
		}

		for (int i = 0; i < sectionHeaderTable.length; i++) {
			Objects.requireNonNull(sectionTable[i]);
		}

		return sectionTable;
	}
}
