/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.objdump;

import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;

import com.ledmington.cpu.InstructionDecoder;
import com.ledmington.cpu.InstructionEncoder;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.Section;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

/** Prints the disassembly of a single executable section of an ELF file, in the style of GNU objdump. */
final class Disassembler {

	private Disassembler() {}

	/**
	 * Prints the disassembly of the given section.
	 *
	 * @param out The writer to print the disassembly to.
	 * @param st The ELF file's section table.
	 * @param sectionIndex The index of the section to disassemble.
	 * @param functionNames The names used to introduce a new function (e.g. {@code <foo>:}).
	 * @param allSymbols The names used to resolve any address to the nearest preceding symbol.
	 */
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	/* default */ static void disassembleSection(
			final PrintWriter out,
			final SectionTable st,
			final int sectionIndex,
			final Map<Long, String> functionNames,
			final NavigableMap<Long, String> allSymbols) {
		final Section s = st.getSection(sectionIndex);
		out.printf("Disassembly of section %s:%n", s.getName());
		out.println();

		final long startOfSection = s.header().getVirtualAddress();

		if (!functionNames.containsKey(startOfSection)) {
			out.printf("%016x <%s>:%n", startOfSection, s.getName());
		}

		final byte[] content = ((LoadableSection) s).getLoadableContent();
		final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
		while (b.getPosition() < content.length) {
			final long currentPosition = startOfSection + b.getPosition();

			if (functionNames.containsKey(currentPosition)) {
				if (b.getPosition() > 0L) {
					out.println();
				}
				out.printf("%016x <%s>:%n", currentPosition, functionNames.get(currentPosition));
			}

			final long startOfInstruction = b.getPosition();
			final Instruction inst = InstructionDecoder.fromHex(b);
			final long endOfInstruction = b.getPosition();
			final long lengthOfInstruction = endOfInstruction - startOfInstruction;
			out.printf("%8x:\t", startOfSection + startOfInstruction);
			for (int i = 0; i < 7; i++) {
				if (i < lengthOfInstruction) {
					out.printf("%02x ", content[BitUtils.asInt(startOfInstruction + i)]);
				} else {
					out.print("   ");
				}
			}

			out.print("\t");

			printInstruction(out, st, inst, content, startOfSection, startOfInstruction, endOfInstruction, allSymbols);

			if (lengthOfInstruction >= 8L) {
				out.printf("%8x:\t", startOfSection + startOfInstruction + 7L);
				for (int i = 7; i < 14; i++) {
					if (i < lengthOfInstruction) {
						out.printf("%02x ", content[BitUtils.asInt(startOfInstruction + i)]);
					} else {
						break;
					}
				}
				out.println();
			}
		}
	}

	private static void printInstruction(
			final PrintWriter out,
			final SectionTable st,
			final Instruction inst,
			final byte[] content,
			final long startOfSection,
			final long startOfInstruction,
			final long endOfInstruction,
			final NavigableMap<Long, String> allSymbols) {
		if (inst.opcode() == Opcode.BND_JMP) {
			printBndJmp(out, st, inst, startOfSection, endOfInstruction);
		} else if (isJumpWithImmediate(inst)) {
			printJumpWithImmediate(
					out, inst, content, startOfSection, startOfInstruction, endOfInstruction, allSymbols);
		} else if (isPaddingNopWithCsPrefix(inst)) {
			printPaddingNopWithCsPrefix(out, inst, content, startOfInstruction);
		} else if (hasNotrackPrefix(inst, content, startOfInstruction)) {
			// A DS segment override on an indirect jmp/call is the CET 'notrack' hint, not an actual segment
			// override (there is no memory operand to apply it to).
			out.printf("notrack %s%n", InstructionEncoder.toIntelSyntax(inst, true, 0, true));
		} else {
			// GNU objdump does not pad the mnemonic column when a legacy prefix (e.g. 'rep') is shown before it.
			final String base = InstructionEncoder.toIntelSyntax(inst, true, inst.hasPrefix() ? 0 : 6, true);
			final String ripComment = ripRelativeComment(inst, startOfSection, endOfInstruction, allSymbols);
			out.printf("%s%s%n", base, ripComment == null ? "" : ripComment);
		}
	}

	private static void printBndJmp(
			final PrintWriter out,
			final SectionTable st,
			final Instruction inst,
			final long startOfSection,
			final long endOfInstruction) {
		// bnd jmps and LEAs need to print the address they point to
		final IndirectOperand io = (IndirectOperand) inst.firstOperand();
		final long displacement = io.getDisplacement();
		final long computedOffset = startOfSection + endOfInstruction + displacement;
		final long gotSectionAddress =
				st.getSectionByName(".got.plt").orElseThrow().header().getVirtualAddress();
		out.printf(
				"%s        # %x <_GLOBAL_OFFSET_TABLE_+0x%x>%n",
				InstructionEncoder.toIntelSyntax(inst, true, 6, true),
				computedOffset,
				computedOffset - gotSectionAddress);
	}

	private static void printJumpWithImmediate(
			final PrintWriter out,
			final Instruction inst,
			final byte[] content,
			final long startOfSection,
			final long startOfInstruction,
			final long endOfInstruction,
			final NavigableMap<Long, String> allSymbols) {
		// conditional jumps and 'call' instructions need to be printed differently: instead of just the
		// immediate, we need to add it to the current IP and display the name of the symbol it points to.
		// An address-size-override prefix has no effect on these (there is no memory operand to address),
		// so GNU objdump shows it explicitly as a leading pseudo-prefix rather than silently dropping it.
		final String addressSizeOverride = content[BitUtils.asInt(startOfInstruction)] == (byte) 0x67 ? "addr32 " : "";
		final long jumpOffset = getAsLong((Immediate) inst.firstOperand());
		final long actualPointedAddress = startOfSection + endOfInstruction + jumpOffset;
		final String label = resolveAddressLabel(actualPointedAddress, allSymbols);
		final String mnemonicFormat = addressSizeOverride.isEmpty() ? "%-6s" : "%s";
		if (label == null) {
			out.printf(
					"%s" + mnemonicFormat + " %x%n",
					addressSizeOverride,
					inst.opcode().mnemonic(),
					actualPointedAddress);
		} else {
			out.printf(
					"%s" + mnemonicFormat + " %x <%s>%n",
					addressSizeOverride,
					inst.opcode().mnemonic(),
					actualPointedAddress,
					label);
		}
	}

	private static void printPaddingNopWithCsPrefix(
			final PrintWriter out, final Instruction inst, final byte[] content, final long startOfInstruction) {
		// GNU objdump displays no-op prefixes used purely for instruction-length padding (a CS segment
		// override, and any operand-size-override byte beyond the first one, which is the only one that
		// actually affects the operand size) as leading pseudo-prefix words instead of folding them into the
		// memory operand.
		int redundantOperandSizePrefixes = -1;
		for (long i = startOfInstruction; i < content.length && content[BitUtils.asInt(i)] == (byte) 0x66; i++) {
			redundantOperandSizePrefixes++;
		}
		out.print("data16 ".repeat(Math.max(0, redundantOperandSizePrefixes)));
		out.printf(
				"cs %s%n", InstructionEncoder.toIntelSyntax(inst, true, 0, true).replace("cs:", ""));
	}

	private static boolean isPaddingNopWithCsPrefix(final Instruction inst) {
		return inst.opcode() == Opcode.NOP
				&& inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasSegment()
				&& io.getSegment() == SegmentRegister.CS;
	}

	private static boolean hasNotrackPrefix(
			final Instruction inst, final byte[] content, final long startOfInstruction) {
		return (inst.opcode() == Opcode.JMP || inst.opcode() == Opcode.CALL)
				&& !(inst.firstOperand() instanceof Immediate)
				&& content[BitUtils.asInt(startOfInstruction)] == (byte) 0x3e;
	}

	private static String ripRelativeComment(
			final Instruction inst,
			final long startOfSection,
			final long endOfInstruction,
			final NavigableMap<Long, String> allSymbols) {
		final IndirectOperand io = findRipRelativeOperand(inst);
		if (io == null) {
			return null;
		}
		final long target = startOfSection + endOfInstruction + io.getDisplacement();
		final String label = resolveAddressLabel(target, allSymbols);
		return label == null ? null : String.format("        # %x <%s>", target, label);
	}

	/**
	 * Finds the first RIP-relative indirect operand of the given instruction, if any.
	 *
	 * @param inst The instruction to search.
	 * @return The first RIP-relative operand, or {@code null} if none exists.
	 */
	/* default */ static IndirectOperand findRipRelativeOperand(final Instruction inst) {
		final int numOperands = inst.getNumOperands();
		for (int i = 0; i < numOperands; i++) {
			if (inst.operand(i) instanceof final IndirectOperand io && isRipBase(io)) {
				return io;
			}
		}
		return null;
	}

	private static boolean isRipBase(final IndirectOperand io) {
		return io.hasBase() && io.getBase() == Register64.RIP;
	}

	/**
	 * Resolves an address to a symbolic label the way GNU objdump does: an exact match is printed bare, otherwise the
	 * nearest preceding symbol is printed with a "+0xN" offset. Returns {@code null} if no preceding symbol exists.
	 */
	private static String resolveAddressLabel(final long address, final NavigableMap<Long, String> allSymbols) {
		final Map.Entry<Long, String> floor = allSymbols.floorEntry(address);
		if (floor == null) {
			return null;
		}
		final long offset = address - floor.getKey();
		return offset == 0L ? floor.getValue() : String.format("%s+0x%x", floor.getValue(), offset);
	}

	private static long getAsLong(final Immediate imm) {
		return switch (imm.bits()) {
			case 8 -> imm.asByte();
			case 16 -> imm.asShort();
			case 32 -> imm.asInt();
			case 64 -> imm.asLong();
			default -> throw new IllegalArgumentException("Invalid immediate.");
		};
	}

	private static boolean isJumpWithImmediate(final Instruction inst) {
		return inst.hasFirstOperand()
				&& !inst.hasSecondOperand()
				&& (inst.opcode() == Opcode.JMP
						|| inst.opcode() == Opcode.JA
						|| inst.opcode() == Opcode.JAE
						|| inst.opcode() == Opcode.JB
						|| inst.opcode() == Opcode.JBE
						|| inst.opcode() == Opcode.JE
						|| inst.opcode() == Opcode.JG
						|| inst.opcode() == Opcode.JGE
						|| inst.opcode() == Opcode.JL
						|| inst.opcode() == Opcode.JLE
						|| inst.opcode() == Opcode.JNE
						|| inst.opcode() == Opcode.JNS
						|| inst.opcode() == Opcode.JO
						|| inst.opcode() == Opcode.JNO
						|| inst.opcode() == Opcode.JNP
						|| inst.opcode() == Opcode.JP
						|| inst.opcode() == Opcode.JRCXZ
						|| inst.opcode() == Opcode.JS
						|| inst.opcode() == Opcode.LOOP
						|| inst.opcode() == Opcode.LOOPE
						|| inst.opcode() == Opcode.LOOPNE
						|| inst.opcode() == Opcode.CALL
						|| inst.opcode() == Opcode.XBEGIN)
				&& inst.firstOperand() instanceof Immediate;
	}
}
