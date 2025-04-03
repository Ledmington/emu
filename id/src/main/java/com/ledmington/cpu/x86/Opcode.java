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
package com.ledmington.cpu.x86;

import java.util.Objects;

/** X86 opcode mnemonics. */
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum Opcode {

	/** Add with carry. */
	ADC("adc"),

	/** Add. */
	ADD("add"),

	/** Add scalar double-precision floating-point values. */
	ADDSD("addsd"),

	/** Logical AND. */
	AND("and"),

	/** Bit scan reverse. */
	BSR("bsr"),

	/** Bit swap. */
	BSWAP("bswap"),

	/** Bit test. */
	BT("bt"),

	/** Bit test and complement. */
	BTC("btc"),

	/** Bit test and reset. */
	BTR("btr"),

	/** Bit test and set. */
	BTS("bts"),

	/** Call procedure. */
	CALL("call"),

	/** Convert doubleword to quadword. */
	CDQ("cdq"),

	/** Convert doubleword to quadword. */
	CDQE("cdqe"),

	/** Conditional move if above. */
	CMOVA("cmova"),

	/** Conditional move if above or equal. */
	CMOVAE("cmovae"),

	/** Conditional move if below. */
	CMOVB("cmovb"),

	/** Conditional move if below or equal. */
	CMOVBE("cmovbe"),

	/** Conditional move if equal. */
	CMOVE("cmove"),

	/** Conditional move if greater. */
	CMOVG("cmovg"),

	/** Conditional move if greater or equal. */
	CMOVGE("cmovge"),

	/** Conditional move if less. */
	CMOVL("cmovl"),

	/** Conditional move if less or equal. */
	CMOVLE("cmovle"),

	/** Conditional move if not equal. */
	CMOVNE("cmovne"),

	/** Conditional move if not signed. */
	CMOVNS("cmovns"),

	/** Conditional move if signed. */
	CMOVS("cmovs"),

	/** Compare two operands. */
	CMP("cmp"),

	/** Compare and exchange. */
	CMPXCHG("cmpxchg"),

	/** CPU identification. */
	CPUID("cpuid"),

	/** Convert doubleword integer to scalar double precision floating-point value. */
	CVTSI2SD("cvtsi2sd"),

	/** Convert word to doubleword. */
	CWDE("cwde"),

	/** Decrement. */
	DEC("dec"),

	/** Unsigned divide. */
	DIV("div"),

	/** Divide scalar double-precision floating-point values. */
	DIVSD("divsd"),

	/** Terminate an indirect branch in 32-bit mode. */
	ENDBR32("endbr32"),

	/** Terminate an indirect branch in 64-bit mode. */
	ENDBR64("endbr64"),

	/** Halt. */
	HLT("hlt"),

	/** Signed divide. */
	IDIV("idiv"),

	/** Signed multiply. */
	IMUL("imul"),

	/** Increment. */
	INC("inc"),

	/** Increment shadow stack pointer. */
	INCSSPQ("incsspq"),

	/** Call to interrupt procedure. */
	INT3("int3"),

	/** Unsigned conditional jump if above. */
	JA("ja"),

	/** Unsigned conditional jump if above or equal. */
	JAE("jae"),

	/** Unsigned conditional jump if below. */
	JB("jb"),

	/** Unsigned conditional jump if below or equal. */
	JBE("jbe"),

	/** Unsigned conditional jump if equal. */
	JE("je"),

	/** Signed conditional jump if greater. */
	JG("jg"),

	/** Signed conditional jump if greater or equal. */
	JGE("jge"),

	/** Signed conditional jump if less. */
	JL("jl"),

	/** Signed conditional jump if less or equal. */
	JLE("jle"),

	/** Unconditional jump. */
	JMP("jmp"),

	/** Unsigned conditional jump if not equal. */
	JNE("jne"),

	/** Conditional jump if not signed. */
	JNS("jns"),

	/** Conditional jump if parity. */
	JP("jp"),

	/** Conditional jump if signed. */
	JS("js"),

	/** Load status flags into AH register. */
	LAHF("lahf"),

	/** Load effective address. */
	LEA("lea"),

	/** High-level procedure exit. */
	LEAVE("leave"),

	/** Move to/from registers and memory. */
	MOV("mov"),

	/** Move a 64-bit immediate into a 64-bit register. */
	MOVABS("movabs"),

	/** Move aligned packed double-precision floating-point values. */
	MOVAPD("movapd"),

	/** Move aligned packed single-precision floating-point values. */
	MOVAPS("movaps"),

	/** Move doubleword. */
	MOVD("movd"),

	/** Move aligned double-quadword. */
	MOVDQA("movdqa"),

	/** Move packed single-precision floating-point values high to low. */
	MOVHLPS("movhlps"),

	/**
	 * Move high packed double precision floating-point values to and from the high quadword of an XMM register and
	 * memory.
	 */
	MOVHPD("movhpd"),

	/**
	 * Move high packed single precision floating-point values to and from the high quadword of an XMM register and
	 * memory.
	 */
	MOVHPS("movhps"),

	/** Move quadword. */
	MOVQ("movq"),

	/** Move string. */
	MOVS("movs"),

	/** Move double-word string. */
	MOVSD("movsd"),

	/** Move and sign-extend. */
	MOVSX("movsx"),

	/** Move and sign-extend doubleword. */
	MOVSXD("movsxd"),

	/** Move unaligned packed single precision floating-point values into XMM register. */
	MOVUPS("movups"),

	/** Move and zero-extend. */
	MOVZX("movzx"),

	/** Unsigned multiply. */
	MUL("mul"),

	/** Two's complement negation. */
	NEG("neg"),

	/** No operation. */
	NOP("nop"),

	/** Logical NOT. */
	NOT("not"),

	/** Logical OR. */
	OR("or"),

	/** Add packed quadword integers. */
	PADDQ("paddq"),

	/** Logical AND (MMX/XMM registers). */
	PAND("pand"),

	/** Compare packed bytes for equal. */
	PCMPEQB("pcmpeqb"),

	/** Compare packed doublewords for equal. */
	PCMPEQD("pcmpeqd"),

	/** Compare packed words for equal. */
	PCMPEQW("pcmpeqw"),

	/** Pop a value from the stack. */
	POP("pop"),

	/** Logical OR (MMX/XMM registers). */
	POR("por"),

	/**
	 * Non-temporal data—fetch data into location close to the processor, minimizing cache pollution. Pentium III
	 * processor: 1st-level cache Pentium 4 and Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHNTA("prefetchnta"),

	/**
	 * Temporal data—fetch data into all levels of cache hierarchy. Pentium III processor: 1st-level cache or 2nd-level
	 * cache Pentium 4 and Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT0("prefetcht0"),

	/**
	 * Temporal data—fetch data into level 2 cache and higher. Pentium III processor: 2nd-level cache Pentium 4 and
	 * Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT1("prefetcht1"),

	/**
	 * Temporal data—fetch data into level 2 cache and higher. Pentium III processor: 2nd-level cache Pentium 4 and
	 * Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT2("prefetcht2"),

	/** Shuffle packed doublewords. */
	PSHUFD("pshufd"),

	/** Shuffle packed integer word in MMX register. */
	PSHUFW("pshufw"),

	/** Subtract packed quadword integers. */
	PSUBQ("psubq"),

	/** Unpack high data. */
	PUNPCKHQDQ("punpckhqdq"),

	/** Unpack low-order doublewords. */
	PUNPCKLDQ("punpckldq"),

	/** Interleave low-order quadword from xmm1 and xmm2/m128 into xmm1 register. */
	PUNPCKLQDQ("punpcklqdq"),

	/** Push word, doubleword or quadword onto the stack. */
	PUSH("push"),

	/** Logical XOR (MMX/XMM registers). */
	PXOR("pxor"),

	/** Rotate thru carry left. */
	RCL("rcl"),

	/** Rotate thru carry right. */
	RCR("rcr"),

	/** Read random number. */
	RDRAND("rdrand"),

	/** Read random seed. */
	RDSEED("rdseed"),

	/** Read shadow stack pointer. */
	RDSSPQ("rdsspq"),

	/** Return from procedure. */
	RET("ret"),

	/** Rotate left. */
	ROL("rol"),

	/** Rotate right. */
	ROR("ror"),

	/** Load status flags into AH register. */
	SAHF("sahf"),

	/** Arithmetic shift right. */
	SAR("sar"),

	/** Integer subtraction with borrow. */
	SBB("sbb"),

	/** Set byte if above. */
	SETA("seta"),

	/** Set byte if above or equal. */
	SETAE("setae"),

	/** Set byte if below. */
	SETB("setb"),

	/** Set byte if below or equal. */
	SETBE("setbe"),

	/** Set byte if equal. */
	SETE("sete"),

	/** Set byte if greater. */
	SETG("setg"),

	/** Set byte if greater or equal. */
	SETGE("setge"),

	/** Set byte if less. */
	SETL("setl"),

	/** Set byte if less or equal. */
	SETLE("setle"),

	/** Set byte if not equal. */
	SETNE("setne"),

	/** Set byte if not overflow. */
	SETNO("setno"),

	/** Set byte if overflow. */
	SETO("seto"),

	/** Logical shift left. */
	SHL("shl"),

	/** Logical shift right. */
	SHR("shr"),

	/** Shuffles values in packed double-precision floating-point operands. */
	SHUFPD("shufpd"),

	/** Shuffles values in packed single-precision floating-point operands. */
	SHUFPS("shufps"),

	/** Store string. */
	STOS("stos"),

	/** Subtract. */
	SUB("sub"),

	/** Fast system call. */
	SYSCALL("syscall"),

	/** Logical compare. */
	TEST("test"),

	/** Unordered compare scalar double-precision floating-point values and set EFLAGS. */
	UCOMISD("ucomisd"),

	/** Unordered compare scalar single-precision floating-point values and set EFLAGS. */
	UCOMISS("ucomiss"),

	/** Undefined instruction. */
	UD2("ud2"),

	/** Exchange and add. */
	XADD("xadd"),

	/** Exchange. */
	XCHG("xchg"),

	/** Reads the state of an extended control register. */
	XGETBV("xgetbv"),

	/** Logical XOR. */
	XOR("xor"),

	/** Bitwise logical XOR of single-precision ploating-point values. */
	XORPS("xorps");

	private final String mnemonicString;

	Opcode(final String mnemonicString) {
		this.mnemonicString = Objects.requireNonNull(mnemonicString);
	}

	/**
	 * Returns the mnemonic for this opcode.
	 *
	 * @return The mnemonic for this opcode.
	 */
	public String mnemonic() {
		return mnemonicString;
	}
}
