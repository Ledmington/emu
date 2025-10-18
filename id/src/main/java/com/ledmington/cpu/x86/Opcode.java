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

import java.util.Locale;

/** X86 opcode mnemonics. */
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum Opcode {

	/** Add with carry. */
	ADC,

	/** Add. */
	ADD,

	/** Add scalar double-precision floating-point values. */
	ADDSD,

	/** Logical AND. */
	AND,

	/** Bitwise logical AND of packed double-precision floating-point values. */
	ANDPD,

	/** Bounded jump. */
	BND_JMP("bnd jmp"),

	/** Bit scan forward. */
	BSF,

	/** Bit scan reverse. */
	BSR,

	/** Bit swap. */
	BSWAP,

	/** Bit test. */
	BT,

	/** Bit test and complement. */
	BTC,

	/** Bit test and reset. */
	BTR,

	/** Bit test and set. */
	BTS,

	/** Zero high bits starting from specified bit position. */
	BZHI,

	/** Call procedure. */
	CALL,

	/** Convert doubleword to quadword. */
	CDQ,

	/** Convert doubleword to quadword. */
	CDQE,

	/** Clear carry flag. */
	CLC,

	/** Clear direction flag. */
	CLD,

	/** Clear interrupt flag. */
	CLI,

	/** Complement carry flag. */
	CMC,

	/** Conditional move if above. */
	CMOVA,

	/** Conditional move if above or equal. */
	CMOVAE,

	/** Conditional move if below. */
	CMOVB,

	/** Conditional move if below or equal. */
	CMOVBE,

	/** Conditional move if equal. */
	CMOVE,

	/** Conditional move if greater. */
	CMOVG,

	/** Conditional move if greater or equal. */
	CMOVGE,

	/** Conditional move if less. */
	CMOVL,

	/** Conditional move if less or equal. */
	CMOVLE,

	/** Conditional move if not equal. */
	CMOVNE,

	/** Conditional move if not signed. */
	CMOVNS,

	/** Conditional move if signed. */
	CMOVS,

	/** Compare two operands. */
	CMP,

	/** Compare string operands. */
	CMPS,

	/** Compare and exchange. */
	CMPXCHG,

	/** CPU identification. */
	CPUID,

	/** Convert doubleword integer to scalar double precision floating-point value. */
	CVTSI2SD,

	/** Convert word to doubleword. */
	CWDE,

	/** Decrement. */
	DEC,

	/** Unsigned divide. */
	DIV,

	/** Divide packed double-precision floating-point values. */
	DIVPD,

	/** Divide packed single-precision floating-point values. */
	DIVPS,

	/** Divide scalar double-precision floating-point values. */
	DIVSD,

	/** Divide scalar single-precision floating-point values. */
	DIVSS,

	/** Execute an Enclave user function of specified leaf number. */
	ENCLU,

	/** Terminate an indirect branch in 32-bit mode. */
	ENDBR32,

	/** Terminate an indirect branch in 64-bit mode. */
	ENDBR64,

	/** Make stack frame for high level procedure. */
	ENTER,

	/** Add. */
	FADD,

	/** Add integer. */
	FIADD,

	/** Load integer. */
	FILD,

	/** Load real. */
	FLD,

	/** Wait for floating-point unit. */
	FWAIT,

	/** Restore x87 FPU and SIMD state. */
	FXRSTOR,

	/** Save x87 FPU and SIMD state. */
	FXSAVE,

	/** Halt. */
	HLT,

	/** Signed divide. */
	IDIV,

	/** Signed multiply. */
	IMUL,

	/** Input from port. */
	IN,

	/** Increment. */
	INC,

	/** Increment shadow stack pointer. */
	INCSSPQ,

	/** Input string. */
	INS,

	/** Call to interrupt. */
	INT,

	/** Call to interrupt procedure. */
	INT3,

	/** Interrupt return. */
	IRET,

	/** Unsigned conditional jump if above. */
	JA,

	/** Unsigned conditional jump if above or equal. */
	JAE,

	/** Unsigned conditional jump if below. */
	JB,

	/** Unsigned conditional jump if below or equal. */
	JBE,

	/** Unsigned conditional jump if equal. */
	JE,

	/** Signed conditional jump if greater. */
	JG,

	/** Signed conditional jump if greater or equal. */
	JGE,

	/** Signed conditional jump if less. */
	JL,

	/** Signed conditional jump if less or equal. */
	JLE,

	/** Unconditional jump. */
	JMP,

	/** Unsigned conditional jump if not equal. */
	JNE,

	/** Conditional jump if not signed. */
	JNS,

	/** Conditional jump if overflow. */
	JO,

	/** Conditional jump if not overflow. */
	JNO,

	/** Conditional jump if not parity. */
	JNP,

	/** Conditional jump if parity. */
	JP,

	/** Jump short if RCX is 0. */
	JRCXZ,

	/** Conditional jump if signed. */
	JS,

	/** Move from and to mask registers. */
	KMOVB,

	/** Move from and to mask registers. */
	KMOVD,

	/** Move from and to mask registers. */
	KMOVQ,

	/** Move from and to mask registers. */
	KMOVW,

	/** Bitwise logical OR masks. */
	KORD,

	/** OR masks and set flags. */
	KORTESTD,

	/** Unpack for Mask Registers. */
	KUNPCKBW,

	/** Unpack for Mask Registers. */
	KUNPCKDQ,

	/** Load status flags into AH register. */
	LAHF,

	/** Load unaligned integer 128-bit. */
	LDDQU,

	/** Load effective address. */
	LEA,

	/** High-level procedure exit. */
	LEAVE,

	/** Load string operand. */
	LODS,

	/** Loop count. */
	LOOP,

	/** Loop count while zero/equal. */
	LOOPE,

	/** Loop count while not zero/equal. */
	LOOPNE,

	/** Move to/from registers and memory. */
	MOV,

	/** Move a 64-bit immediate into a 64-bit register. */
	MOVABS,

	/** Move aligned packed double-precision floating-point values. */
	MOVAPD,

	/** Move aligned packed single-precision floating-point values. */
	MOVAPS,

	/** Move data after swapping bytes. */
	MOVBE,

	/** Move doubleword. */
	MOVD,

	/** Move aligned double-quadword. */
	MOVDQA,

	/** Move unaligned double-quadword. */
	MOVDQU,

	/** Move packed single-precision floating-point values high to low. */
	MOVHLPS,

	/**
	 * Move high packed double precision floating-point values to and from the high quadword of an XMM register and
	 * memory.
	 */
	MOVHPD,

	/**
	 * Move high packed single precision floating-point values to and from the high quadword of an XMM register and
	 * memory.
	 */
	MOVHPS,

	/** Extract packed single-precision floating-point values. */
	MOVMSKPS,

	/** Store double quadword using non-temporal hint. */
	MOVNTDQ,

	/** Store packed single precision floating-point values using non-temporal hint. */
	MOVNTPS,

	/** Move quadword. */
	MOVQ,

	/** Move string. */
	MOVS,

	/** Move double-word string. */
	MOVSD,

	/** Move and sign-extend. */
	MOVSX,

	/** Move and sign-extend doubleword. */
	MOVSXD,

	/** Move unaligned packed single precision floating-point values into XMM register. */
	MOVUPS,

	/** Move and zero-extend. */
	MOVZX,

	/** Unsigned multiply. */
	MUL,

	/** Two's complement negation. */
	NEG,

	/** No operation. */
	NOP,

	/** Logical NOT. */
	NOT,

	/** Logical OR. */
	OR,

	/** Output to port. */
	OUT,

	/** Output string to port. */
	OUTS,

	/** Add packed doubleword integers. */
	PADDD,

	/** Add packed quadword integers. */
	PADDQ,

	/** Packed align right. */
	PALIGNR,

	/** Logical AND . */
	PAND,

	/** Compare packed bytes for equal. */
	PCMPEQB,

	/** Compare packed doublewords for equal. */
	PCMPEQD,

	/** Compare packed words for equal. */
	PCMPEQW,

	/** Compare packed signed integers for greater than. */
	PCMPGTB,

	/** Packed compare implicit-length strings. */
	PCMPISTRI,

	/** Extract word. */
	PEXTRW,

	/** Maximum of packed unsigned byte integers. */
	PMAXUB,

	/** Minimum of packed unsigned byte integers. */
	PMINUB,

	/** Minimum of packed unsigned integers. */
	PMINUD,

	/** Move byte mask. */
	PMOVMSKB,

	/** Pop a value from the stack. */
	POP,

	/** Pop stack into flags register. */
	POPF,

	/** Logical OR . */
	POR,

	/**
	 * Non-temporal data—fetch data into location close to the processor, minimizing cache pollution. Pentium III
	 * processor: 1st-level cache Pentium 4 and Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHNTA,

	/**
	 * Temporal data—fetch data into all levels of cache hierarchy. Pentium III processor: 1st-level cache or 2nd-level
	 * cache Pentium 4 and Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT0,

	/**
	 * Temporal data—fetch data into level 2 cache and higher. Pentium III processor: 2nd-level cache Pentium 4 and
	 * Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT1,

	/**
	 * Temporal data—fetch data into level 2 cache and higher. Pentium III processor: 2nd-level cache Pentium 4 and
	 * Intel Xeon processor: 2nd-level cache
	 */
	PREFETCHT2,

	/** Packed shuffle bytes. */
	PSHUFB,

	/** Shuffle packed doublewords. */
	PSHUFD,

	/** Shuffle packed integer word in MMX register. */
	PSHUFW,

	/** Shift packed doublewords left logical. */
	PSLLD,

	/** Shift packed double quadwords left logical. */
	PSLLDQ,

	/** Shift packed quadwords left logical. */
	PSLLQ,

	/** Shift packed words left logical. */
	PSLLW,

	/** Shift packed doublewords right arithmetic. */
	PSRAD,

	/** Shift packed words right arithmetic. */
	PSRAW,

	/** Shift packed doublewords right logical. */
	PSRLD,

	/** Shift packed double quadwords right logical. */
	PSRLDQ,

	/** Shift packed quadwords right logical. */
	PSRLQ,

	/** Shift packed words right logical. */
	PSRLW,

	/** Subtract packed byte integers. */
	PSUBB,

	/** Subtract packed doubleword integers. */
	PSUBD,

	/** Subtract packed quadword integers. */
	PSUBQ,

	/** Subtract packed word integers. */
	PSUBW,

	/** Unpack high data. */
	PUNPCKHDQ,

	/** Unpack high data. */
	PUNPCKHQDQ,

	/** Unpack low-order bytes. */
	PUNPCKLBW,

	/** Unpack low-order doublewords. */
	PUNPCKLDQ,

	/** Interleave low-order quadword from xmm1 and xmm2/m128 into xmm1 register. */
	PUNPCKLQDQ,

	/** Unpack low data. */
	PUNPCKLWD,

	/** Push word, doubleword or quadword onto the stack. */
	PUSH,

	/** Push flags register onto the stack. */
	PUSHF,

	/** Logical XOR. */
	PXOR,

	/** Rotate thru carry left. */
	RCL,

	/** Rotate thru carry right. */
	RCR,

	/** Read random number. */
	RDRAND,

	/** Read random seed. */
	RDSEED,

	/** Read shadow stack pointer. */
	RDSSPQ,

	/** Return from procedure (near). */
	RET,

	/** Return from procedure (far). */
	RETF,

	/** Rotate left. */
	ROL,

	/** Rotate right. */
	ROR,

	/** Load status flags into AH register. */
	SAHF,

	/** Arithmetic shift right. */
	SAR,

	/** Arithmetic shift right without affecting flags. */
	SARX,

	/** Integer subtraction with borrow. */
	SBB,

	/** Scan string. */
	SCAS,

	/** Set byte if above. */
	SETA,

	/** Set byte if above or equal. */
	SETAE,

	/** Set byte if below. */
	SETB,

	/** Set byte if below or equal. */
	SETBE,

	/** Set byte if equal. */
	SETE,

	/** Set byte if greater. */
	SETG,

	/** Set byte if greater or equal. */
	SETGE,

	/** Set byte if less. */
	SETL,

	/** Set byte if less or equal. */
	SETLE,

	/** Set byte if not equal. */
	SETNE,

	/** Set byte if not overflow. */
	SETNO,

	/** Set byte if not signed. */
	SETNS,

	/** Set byte if overflow. */
	SETO,

	/** Set byte if signed. */
	SETS,

	/** Store fence. */
	SFENCE,

	/** Logical shift left. */
	SHL,

	/** Double-precision shift left. */
	SHLD,

	/** Logical shift right. */
	SHR,

	/** Double-precision shift right. */
	SHRD,

	/** Shuffles values in packed double-precision floating-point operands. */
	SHUFPD,

	/** Shuffles values in packed single-precision floating-point operands. */
	SHUFPS,

	/** Store local descriptor table register. */
	SLDT,

	/** Set carry flag. */
	STC,

	/** Set direction flag. */
	STD,

	/** Set interrupt flag. */
	STI,

	/** Store string. */
	STOS,

	/** Store MXCSR register state. */
	STMXCSR,

	/** Subtract. */
	SUB,

	/** Fast system call. */
	SYSCALL,

	/** Logical compare. */
	TEST,

	/** Count the number of trailing zero bits. */
	TZCNT,

	/** Unordered compare scalar double-precision floating-point values and set EFLAGS. */
	UCOMISD,

	/** Unordered compare scalar single-precision floating-point values and set EFLAGS. */
	UCOMISS,

	/** Undefined instruction. */
	UD2,

	/** Load with broadcast floating-point data. */
	VBROADCASTSS,

	/** Move aligned packed single-precision floating-point values. */
	VMOVAPS,

	/** Packed align right. */
	VPALIGNR,

	/** Load with broadcast integer data from general-purpose register. */
	VPBROADCASTB,

	/** Load with broadcast integer data from general-purpose register. */
	VPBROADCASTD,

	/** Load with broadcast integer data from general-purpose register. */
	VPBROADCASTQ,

	/** Load with broadcast integer data from general-purpose register. */
	VPBROADCASTW,

	/** Compare packed data for equal. */
	VPCMPEQB,

	/** Compare packed data for equal. */
	VPCMPEQD,

	/** Compare packed data for equal. */
	VPCMPEQQ,

	/** Compare packed signed integers for greater than. */
	VPCMPGTB,

	/** Packed compare implicit length strings, return index. */
	VPCMPISTRI,

	/** Compare packed data for not equal. */
	VPCMPNEQB,

	/** Compare packed byte values into mask. */
	VPCMPNEQUB,

	/** Minimum of packed unsigned byte integers. */
	VPMINUB,

	/** Minimum of packed unsigned integers. */
	VPMINUD,

	/** Packed shuffle bytes. */
	VPSHUFB,

	/** Shift double quadword left logical. */
	VPSLLDQ,

	/** Shift double quadword right logical. */
	VPSRLDQ,

	/** Subtract packed integers. */
	VPSUBB,

	/** Bitwise ternary logic. */
	VPTERNLOGD,

	/** Logical AND and set mask. */
	VPTESTMB,

	/** Invoke VM function. */
	VMFUNC,

	/** Move doubleword. */
	VMOVD,

	/** Move unaligned packed integer values. */
	VMOVDQU,

	/** Move unaligned packed integer values. */
	VMOVDQU8,

	/** Move unaligned packed integer values. */
	VMOVDQU16,

	/** Move unaligned packed integer values. */
	VMOVDQU32,

	/** Move unaligned packed integer values. */
	VMOVDQU64,

	/** Store double quadword using non-temporal hint. */
	VMOVNTDQ,

	/** Move quadword. */
	VMOVQ,

	/** Move unaligned packed single precision floating-point values. */
	VMOVUPS,

	/** Logical AND. */
	VPAND,

	/** Logical AND NOT. */
	VPANDN,

	/** Move byte mask. */
	VPMOVMSKB,

	/** Logical OR. */
	VPOR,

	/** Logical OR. */
	VPORQ,

	/** Logical exclusive OR. */
	VPXOR,

	/** Logical exclusive OR. */
	VPXORD,

	/** Logical exclusive OR. */
	VPXORQ,

	/** Zero all XMM, YMM and ZMM registers. */
	VZEROALL,

	/** Exchange and add. */
	XADD,

	/** Transactional begin. */
	XBEGIN,

	/** Exchange. */
	XCHG,

	/** Transactional end. */
	XEND,

	/** Reads the state of an extended control register. */
	XGETBV,

	/** Table look-up translation. */
	XLAT,

	/** Logical XOR. */
	XOR,

	/** Bitwise logical XOR of single-precision floating-point values. */
	XORPS,

	/** Restore processor extended states. */
	XRSTOR,

	/** Save processor extended states. */
	XSAVE,

	/** Save processor extended states with compaction. */
	XSAVEC,

	/** Set extended control register. */
	XSETBV,

	/** Test if in transactional execution. */
	XTEST;

	private final String mnemonicString;

	Opcode() {
		this.mnemonicString = name().toLowerCase(Locale.US);
	}

	Opcode(final String mnemonic) {
		this.mnemonicString = mnemonic;
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
