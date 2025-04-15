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

import static com.ledmington.cpu.x86.PointerSize.BYTE_PTR;
import static com.ledmington.cpu.x86.PointerSize.DWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.QWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.WORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.XMMWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.YMMWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.ZMMWORD_PTR;
import static com.ledmington.cpu.x86.Register16.AX;
import static com.ledmington.cpu.x86.Register16.BP;
import static com.ledmington.cpu.x86.Register16.BX;
import static com.ledmington.cpu.x86.Register16.CS;
import static com.ledmington.cpu.x86.Register16.CX;
import static com.ledmington.cpu.x86.Register16.DI;
import static com.ledmington.cpu.x86.Register16.DS;
import static com.ledmington.cpu.x86.Register16.DX;
import static com.ledmington.cpu.x86.Register16.ES;
import static com.ledmington.cpu.x86.Register16.R10W;
import static com.ledmington.cpu.x86.Register16.R11W;
import static com.ledmington.cpu.x86.Register16.R12W;
import static com.ledmington.cpu.x86.Register16.R13W;
import static com.ledmington.cpu.x86.Register16.R14W;
import static com.ledmington.cpu.x86.Register16.R15W;
import static com.ledmington.cpu.x86.Register16.R8W;
import static com.ledmington.cpu.x86.Register16.R9W;
import static com.ledmington.cpu.x86.Register16.SI;
import static com.ledmington.cpu.x86.Register16.SP;
import static com.ledmington.cpu.x86.Register32.EAX;
import static com.ledmington.cpu.x86.Register32.EBP;
import static com.ledmington.cpu.x86.Register32.EBX;
import static com.ledmington.cpu.x86.Register32.ECX;
import static com.ledmington.cpu.x86.Register32.EDI;
import static com.ledmington.cpu.x86.Register32.EDX;
import static com.ledmington.cpu.x86.Register32.EIP;
import static com.ledmington.cpu.x86.Register32.ESI;
import static com.ledmington.cpu.x86.Register32.ESP;
import static com.ledmington.cpu.x86.Register32.R10D;
import static com.ledmington.cpu.x86.Register32.R11D;
import static com.ledmington.cpu.x86.Register32.R12D;
import static com.ledmington.cpu.x86.Register32.R13D;
import static com.ledmington.cpu.x86.Register32.R14D;
import static com.ledmington.cpu.x86.Register32.R15D;
import static com.ledmington.cpu.x86.Register32.R8D;
import static com.ledmington.cpu.x86.Register32.R9D;
import static com.ledmington.cpu.x86.Register64.R10;
import static com.ledmington.cpu.x86.Register64.R11;
import static com.ledmington.cpu.x86.Register64.R12;
import static com.ledmington.cpu.x86.Register64.R13;
import static com.ledmington.cpu.x86.Register64.R14;
import static com.ledmington.cpu.x86.Register64.R15;
import static com.ledmington.cpu.x86.Register64.R8;
import static com.ledmington.cpu.x86.Register64.R9;
import static com.ledmington.cpu.x86.Register64.RAX;
import static com.ledmington.cpu.x86.Register64.RBP;
import static com.ledmington.cpu.x86.Register64.RBX;
import static com.ledmington.cpu.x86.Register64.RCX;
import static com.ledmington.cpu.x86.Register64.RDI;
import static com.ledmington.cpu.x86.Register64.RDX;
import static com.ledmington.cpu.x86.Register64.RIP;
import static com.ledmington.cpu.x86.Register64.RSI;
import static com.ledmington.cpu.x86.Register64.RSP;
import static com.ledmington.cpu.x86.Register8.AH;
import static com.ledmington.cpu.x86.Register8.AL;
import static com.ledmington.cpu.x86.Register8.BH;
import static com.ledmington.cpu.x86.Register8.BL;
import static com.ledmington.cpu.x86.Register8.BPL;
import static com.ledmington.cpu.x86.Register8.CH;
import static com.ledmington.cpu.x86.Register8.CL;
import static com.ledmington.cpu.x86.Register8.DH;
import static com.ledmington.cpu.x86.Register8.DIL;
import static com.ledmington.cpu.x86.Register8.DL;
import static com.ledmington.cpu.x86.Register8.R10B;
import static com.ledmington.cpu.x86.Register8.R11B;
import static com.ledmington.cpu.x86.Register8.R12B;
import static com.ledmington.cpu.x86.Register8.R13B;
import static com.ledmington.cpu.x86.Register8.R14B;
import static com.ledmington.cpu.x86.Register8.R15B;
import static com.ledmington.cpu.x86.Register8.R8B;
import static com.ledmington.cpu.x86.Register8.R9B;
import static com.ledmington.cpu.x86.Register8.SIL;
import static com.ledmington.cpu.x86.Register8.SPL;
import static com.ledmington.cpu.x86.RegisterMMX.MM0;
import static com.ledmington.cpu.x86.RegisterMMX.MM1;
import static com.ledmington.cpu.x86.RegisterMMX.MM2;
import static com.ledmington.cpu.x86.RegisterMMX.MM3;
import static com.ledmington.cpu.x86.RegisterMMX.MM4;
import static com.ledmington.cpu.x86.RegisterMMX.MM5;
import static com.ledmington.cpu.x86.RegisterMMX.MM6;
import static com.ledmington.cpu.x86.RegisterMMX.MM7;
import static com.ledmington.cpu.x86.RegisterXMM.XMM0;
import static com.ledmington.cpu.x86.RegisterXMM.XMM1;
import static com.ledmington.cpu.x86.RegisterXMM.XMM10;
import static com.ledmington.cpu.x86.RegisterXMM.XMM11;
import static com.ledmington.cpu.x86.RegisterXMM.XMM12;
import static com.ledmington.cpu.x86.RegisterXMM.XMM13;
import static com.ledmington.cpu.x86.RegisterXMM.XMM14;
import static com.ledmington.cpu.x86.RegisterXMM.XMM15;
import static com.ledmington.cpu.x86.RegisterXMM.XMM2;
import static com.ledmington.cpu.x86.RegisterXMM.XMM3;
import static com.ledmington.cpu.x86.RegisterXMM.XMM4;
import static com.ledmington.cpu.x86.RegisterXMM.XMM5;
import static com.ledmington.cpu.x86.RegisterXMM.XMM6;
import static com.ledmington.cpu.x86.RegisterXMM.XMM7;
import static com.ledmington.cpu.x86.RegisterXMM.XMM8;
import static com.ledmington.cpu.x86.RegisterXMM.XMM9;
import static com.ledmington.cpu.x86.RegisterYMM.YMM0;
import static com.ledmington.cpu.x86.RegisterYMM.YMM1;
import static com.ledmington.cpu.x86.RegisterYMM.YMM10;
import static com.ledmington.cpu.x86.RegisterYMM.YMM11;
import static com.ledmington.cpu.x86.RegisterYMM.YMM2;
import static com.ledmington.cpu.x86.RegisterYMM.YMM3;
import static com.ledmington.cpu.x86.RegisterYMM.YMM5;
import static com.ledmington.cpu.x86.RegisterYMM.YMM6;
import static com.ledmington.cpu.x86.RegisterYMM.YMM8;
import static com.ledmington.cpu.x86.RegisterZMM.ZMM0;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

public sealed class X64Encodings permits TestDecoding, TestDecodeIncompleteInstruction {

	protected record X64EncodingTestCase(Instruction instruction, String intelSyntax, byte[] hex) {}

	private static final Immediate one = new Immediate((byte) 1);
	private static final Immediate bimm = new Immediate((byte) 0x12);
	private static final Immediate simm = new Immediate((short) 0x1234);
	private static final Immediate iimm = new Immediate(0x12345678);

	private static X64EncodingTestCase test(final Instruction instruction, final String intelSyntax, final String hex) {
		final String[] splitted = hex.strip().split(" ");
		final byte[] code = new byte[splitted.length];
		for (int i = 0; i < splitted.length; i++) {
			code[i] = BitUtils.asByte(Integer.parseInt(splitted[i], 16));
		}
		return new X64EncodingTestCase(instruction, intelSyntax, code);
	}

	private static List<X64EncodingTestCase> nop() {
		return List.of(
				test(new Instruction(Opcode.NOP), "nop", "90"),
				//
				test(new Instruction(Opcode.NOP, AX), "nop ax", "66 0f 1f c0"),
				test(new Instruction(Opcode.NOP, EAX), "nop eax", "0f 1f c0"),
				test(new Instruction(Opcode.NOP, RAX), "nop rax", "48 0f 1f c0"),
				//
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EAX)
										.build()),
						"nop DWORD PTR [eax]",
						"67 0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"nop DWORD PTR [rax]",
						"0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RAX)
										.scale(1)
										.build()),
						"nop DWORD PTR [rax+rax*1]",
						"0f 1f 04 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RAX)
										.scale(1)
										.displacement((byte) 0)
										.build()),
						"nop DWORD PTR [rax+rax*1+0x0]",
						"0f 1f 44 00 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"nop DWORD PTR [rbx+r12*4+0x12345678]",
						"42 0f 1f 84 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EAX)
										.build()),
						"nop QWORD PTR [eax]",
						"67 48 0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"nop QWORD PTR [rax]",
						"48 0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"nop QWORD PTR [rbx+r12*4+0x12345678]",
						"4a 0f 1f 84 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EAX)
										.build()),
						"nop WORD PTR [eax]",
						"67 66 0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.build()),
						"nop WORD PTR [rax]",
						"66 0f 1f 00"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RBX)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"nop WORD PTR [rbx+r12*4+0x12345678]",
						"66 42 0f 1f 84 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.NOP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(CS, RBX))
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"nop WORD PTR cs:[rbx+r12*4+0x12345678]",
						"2e 66 42 0f 1f 84 a3 78 56 34 12"));
	}

	private static List<X64EncodingTestCase> mov() {
		return List.of(
				//  Mov
				test(new Instruction(Opcode.MOV, R10D, R10D), "mov r10d,r10d", "45 89 d2"),
				test(new Instruction(Opcode.MOV, R10D, R11D), "mov r10d,r11d", "45 89 da"),
				test(new Instruction(Opcode.MOV, R10D, R12D), "mov r10d,r12d", "45 89 e2"),
				test(new Instruction(Opcode.MOV, R10D, R13D), "mov r10d,r13d", "45 89 ea"),
				test(new Instruction(Opcode.MOV, R10D, R14D), "mov r10d,r14d", "45 89 f2"),
				test(new Instruction(Opcode.MOV, R10D, R15D), "mov r10d,r15d", "45 89 fa"),
				test(new Instruction(Opcode.MOV, R10D, R8D), "mov r10d,r8d", "45 89 c2"),
				test(new Instruction(Opcode.MOV, R10D, R9D), "mov r10d,r9d", "45 89 ca"),
				test(new Instruction(Opcode.MOV, R11D, R10D), "mov r11d,r10d", "45 89 d3"),
				test(new Instruction(Opcode.MOV, R11D, R11D), "mov r11d,r11d", "45 89 db"),
				test(new Instruction(Opcode.MOV, R11D, R12D), "mov r11d,r12d", "45 89 e3"),
				test(new Instruction(Opcode.MOV, R11D, R13D), "mov r11d,r13d", "45 89 eb"),
				test(new Instruction(Opcode.MOV, R11D, R14D), "mov r11d,r14d", "45 89 f3"),
				test(new Instruction(Opcode.MOV, R11D, R15D), "mov r11d,r15d", "45 89 fb"),
				test(new Instruction(Opcode.MOV, R11D, R8D), "mov r11d,r8d", "45 89 c3"),
				test(new Instruction(Opcode.MOV, R11D, R9D), "mov r11d,r9d", "45 89 cb"),
				test(new Instruction(Opcode.MOV, R12D, R10D), "mov r12d,r10d", "45 89 d4"),
				test(new Instruction(Opcode.MOV, R12D, R11D), "mov r12d,r11d", "45 89 dc"),
				test(new Instruction(Opcode.MOV, R12D, R12D), "mov r12d,r12d", "45 89 e4"),
				test(new Instruction(Opcode.MOV, R12D, R13D), "mov r12d,r13d", "45 89 ec"),
				test(new Instruction(Opcode.MOV, R12D, R14D), "mov r12d,r14d", "45 89 f4"),
				test(new Instruction(Opcode.MOV, R12D, R15D), "mov r12d,r15d", "45 89 fc"),
				test(new Instruction(Opcode.MOV, R12D, R8D), "mov r12d,r8d", "45 89 c4"),
				test(new Instruction(Opcode.MOV, R12D, R9D), "mov r12d,r9d", "45 89 cc"),
				test(new Instruction(Opcode.MOV, R13D, R10D), "mov r13d,r10d", "45 89 d5"),
				test(new Instruction(Opcode.MOV, R13D, R11D), "mov r13d,r11d", "45 89 dd"),
				test(new Instruction(Opcode.MOV, R13D, R12D), "mov r13d,r12d", "45 89 e5"),
				test(new Instruction(Opcode.MOV, R13D, R13D), "mov r13d,r13d", "45 89 ed"),
				test(new Instruction(Opcode.MOV, R13D, R14D), "mov r13d,r14d", "45 89 f5"),
				test(new Instruction(Opcode.MOV, R13D, R15D), "mov r13d,r15d", "45 89 fd"),
				test(new Instruction(Opcode.MOV, R13D, R8D), "mov r13d,r8d", "45 89 c5"),
				test(new Instruction(Opcode.MOV, R13D, R9D), "mov r13d,r9d", "45 89 cd"),
				test(new Instruction(Opcode.MOV, R14D, R10D), "mov r14d,r10d", "45 89 d6"),
				test(new Instruction(Opcode.MOV, R14D, R11D), "mov r14d,r11d", "45 89 de"),
				test(new Instruction(Opcode.MOV, R14D, R12D), "mov r14d,r12d", "45 89 e6"),
				test(new Instruction(Opcode.MOV, R14D, R13D), "mov r14d,r13d", "45 89 ee"),
				test(new Instruction(Opcode.MOV, R14D, R14D), "mov r14d,r14d", "45 89 f6"),
				test(new Instruction(Opcode.MOV, R14D, R15D), "mov r14d,r15d", "45 89 fe"),
				test(new Instruction(Opcode.MOV, R14D, R8D), "mov r14d,r8d", "45 89 c6"),
				test(new Instruction(Opcode.MOV, R14D, R9D), "mov r14d,r9d", "45 89 ce"),
				test(new Instruction(Opcode.MOV, R15D, R10D), "mov r15d,r10d", "45 89 d7"),
				test(new Instruction(Opcode.MOV, R15D, R11D), "mov r15d,r11d", "45 89 df"),
				test(new Instruction(Opcode.MOV, R15D, R12D), "mov r15d,r12d", "45 89 e7"),
				test(new Instruction(Opcode.MOV, R15D, R13D), "mov r15d,r13d", "45 89 ef"),
				test(new Instruction(Opcode.MOV, R15D, R14D), "mov r15d,r14d", "45 89 f7"),
				test(new Instruction(Opcode.MOV, R15D, R15D), "mov r15d,r15d", "45 89 ff"),
				test(new Instruction(Opcode.MOV, R15D, R8D), "mov r15d,r8d", "45 89 c7"),
				test(new Instruction(Opcode.MOV, R15D, R9D), "mov r15d,r9d", "45 89 cf"),
				test(new Instruction(Opcode.MOV, R8D, R10D), "mov r8d,r10d", "45 89 d0"),
				test(new Instruction(Opcode.MOV, R8D, R11D), "mov r8d,r11d", "45 89 d8"),
				test(new Instruction(Opcode.MOV, R8D, R12D), "mov r8d,r12d", "45 89 e0"),
				test(new Instruction(Opcode.MOV, R8D, R13D), "mov r8d,r13d", "45 89 e8"),
				test(new Instruction(Opcode.MOV, R8D, R14D), "mov r8d,r14d", "45 89 f0"),
				test(new Instruction(Opcode.MOV, R8D, R15D), "mov r8d,r15d", "45 89 f8"),
				test(new Instruction(Opcode.MOV, R8D, R8D), "mov r8d,r8d", "45 89 c0"),
				test(new Instruction(Opcode.MOV, R8D, R9D), "mov r8d,r9d", "45 89 c8"),
				test(new Instruction(Opcode.MOV, R9D, R10D), "mov r9d,r10d", "45 89 d1"),
				test(new Instruction(Opcode.MOV, R9D, R11D), "mov r9d,r11d", "45 89 d9"),
				test(new Instruction(Opcode.MOV, R9D, R12D), "mov r9d,r12d", "45 89 e1"),
				test(new Instruction(Opcode.MOV, R9D, R13D), "mov r9d,r13d", "45 89 e9"),
				test(new Instruction(Opcode.MOV, R9D, R14D), "mov r9d,r14d", "45 89 f1"),
				test(new Instruction(Opcode.MOV, R9D, R15D), "mov r9d,r15d", "45 89 f9"),
				test(new Instruction(Opcode.MOV, R9D, R8D), "mov r9d,r8d", "45 89 c1"),
				test(new Instruction(Opcode.MOV, R9D, R9D), "mov r9d,r9d", "45 89 c9"),
				//
				test(new Instruction(Opcode.MOV, EAX, EAX), "mov eax,eax", "89 c0"),
				test(new Instruction(Opcode.MOV, EAX, EBP), "mov eax,ebp", "89 e8"),
				test(new Instruction(Opcode.MOV, EAX, EBX), "mov eax,ebx", "89 d8"),
				test(new Instruction(Opcode.MOV, EAX, ECX), "mov eax,ecx", "89 c8"),
				test(new Instruction(Opcode.MOV, EAX, EDI), "mov eax,edi", "89 f8"),
				test(new Instruction(Opcode.MOV, EAX, EDX), "mov eax,edx", "89 d0"),
				test(new Instruction(Opcode.MOV, EAX, ESI), "mov eax,esi", "89 f0"),
				test(new Instruction(Opcode.MOV, EAX, ESP), "mov eax,esp", "89 e0"),
				test(new Instruction(Opcode.MOV, EBP, EAX), "mov ebp,eax", "89 c5"),
				test(new Instruction(Opcode.MOV, EBP, EBP), "mov ebp,ebp", "89 ed"),
				test(new Instruction(Opcode.MOV, EBP, EBX), "mov ebp,ebx", "89 dd"),
				test(new Instruction(Opcode.MOV, EBP, ECX), "mov ebp,ecx", "89 cd"),
				test(new Instruction(Opcode.MOV, EBP, EDI), "mov ebp,edi", "89 fd"),
				test(new Instruction(Opcode.MOV, EBP, EDX), "mov ebp,edx", "89 d5"),
				test(new Instruction(Opcode.MOV, EBP, ESI), "mov ebp,esi", "89 f5"),
				test(new Instruction(Opcode.MOV, EBP, ESP), "mov ebp,esp", "89 e5"),
				test(new Instruction(Opcode.MOV, EBX, EAX), "mov ebx,eax", "89 c3"),
				test(new Instruction(Opcode.MOV, EBX, EBP), "mov ebx,ebp", "89 eb"),
				test(new Instruction(Opcode.MOV, EBX, EBX), "mov ebx,ebx", "89 db"),
				test(new Instruction(Opcode.MOV, EBX, ECX), "mov ebx,ecx", "89 cb"),
				test(new Instruction(Opcode.MOV, EBX, EDI), "mov ebx,edi", "89 fb"),
				test(new Instruction(Opcode.MOV, EBX, EDX), "mov ebx,edx", "89 d3"),
				test(new Instruction(Opcode.MOV, EBX, ESI), "mov ebx,esi", "89 f3"),
				test(new Instruction(Opcode.MOV, EBX, ESP), "mov ebx,esp", "89 e3"),
				test(new Instruction(Opcode.MOV, ECX, EAX), "mov ecx,eax", "89 c1"),
				test(new Instruction(Opcode.MOV, ECX, EBP), "mov ecx,ebp", "89 e9"),
				test(new Instruction(Opcode.MOV, ECX, EBX), "mov ecx,ebx", "89 d9"),
				test(new Instruction(Opcode.MOV, ECX, ECX), "mov ecx,ecx", "89 c9"),
				test(new Instruction(Opcode.MOV, ECX, EDI), "mov ecx,edi", "89 f9"),
				test(new Instruction(Opcode.MOV, ECX, EDX), "mov ecx,edx", "89 d1"),
				test(new Instruction(Opcode.MOV, ECX, ESI), "mov ecx,esi", "89 f1"),
				test(new Instruction(Opcode.MOV, ECX, ESP), "mov ecx,esp", "89 e1"),
				test(new Instruction(Opcode.MOV, EDI, EAX), "mov edi,eax", "89 c7"),
				test(new Instruction(Opcode.MOV, EDI, EBP), "mov edi,ebp", "89 ef"),
				test(new Instruction(Opcode.MOV, EDI, EBX), "mov edi,ebx", "89 df"),
				test(new Instruction(Opcode.MOV, EDI, ECX), "mov edi,ecx", "89 cf"),
				test(new Instruction(Opcode.MOV, EDI, EDI), "mov edi,edi", "89 ff"),
				test(new Instruction(Opcode.MOV, EDI, EDX), "mov edi,edx", "89 d7"),
				test(new Instruction(Opcode.MOV, EDI, ESI), "mov edi,esi", "89 f7"),
				test(new Instruction(Opcode.MOV, EDI, ESP), "mov edi,esp", "89 e7"),
				test(new Instruction(Opcode.MOV, EDX, EAX), "mov edx,eax", "89 c2"),
				test(new Instruction(Opcode.MOV, EDX, EBP), "mov edx,ebp", "89 ea"),
				test(new Instruction(Opcode.MOV, EDX, EBX), "mov edx,ebx", "89 da"),
				test(new Instruction(Opcode.MOV, EDX, ECX), "mov edx,ecx", "89 ca"),
				test(new Instruction(Opcode.MOV, EDX, EDI), "mov edx,edi", "89 fa"),
				test(new Instruction(Opcode.MOV, EDX, EDX), "mov edx,edx", "89 d2"),
				test(new Instruction(Opcode.MOV, EDX, ESI), "mov edx,esi", "89 f2"),
				test(new Instruction(Opcode.MOV, EDX, ESP), "mov edx,esp", "89 e2"),
				test(new Instruction(Opcode.MOV, ESI, EAX), "mov esi,eax", "89 c6"),
				test(new Instruction(Opcode.MOV, ESI, EBP), "mov esi,ebp", "89 ee"),
				test(new Instruction(Opcode.MOV, ESI, EBX), "mov esi,ebx", "89 de"),
				test(new Instruction(Opcode.MOV, ESI, ECX), "mov esi,ecx", "89 ce"),
				test(new Instruction(Opcode.MOV, ESI, EDI), "mov esi,edi", "89 fe"),
				test(new Instruction(Opcode.MOV, ESI, EDX), "mov esi,edx", "89 d6"),
				test(new Instruction(Opcode.MOV, ESI, ESI), "mov esi,esi", "89 f6"),
				test(new Instruction(Opcode.MOV, ESI, ESP), "mov esi,esp", "89 e6"),
				test(new Instruction(Opcode.MOV, ESP, EAX), "mov esp,eax", "89 c4"),
				test(new Instruction(Opcode.MOV, ESP, EBP), "mov esp,ebp", "89 ec"),
				test(new Instruction(Opcode.MOV, ESP, EBX), "mov esp,ebx", "89 dc"),
				test(new Instruction(Opcode.MOV, ESP, ECX), "mov esp,ecx", "89 cc"),
				test(new Instruction(Opcode.MOV, ESP, EDI), "mov esp,edi", "89 fc"),
				test(new Instruction(Opcode.MOV, ESP, EDX), "mov esp,edx", "89 d4"),
				test(new Instruction(Opcode.MOV, ESP, ESI), "mov esp,esi", "89 f4"),
				test(new Instruction(Opcode.MOV, ESP, ESP), "mov esp,esp", "89 e4"),
				//
				test(new Instruction(Opcode.MOV, RAX, RBX), "mov rax,rbx", "48 89 d8"),
				test(new Instruction(Opcode.MOV, RCX, RDX), "mov rcx,rdx", "48 89 d1"),
				test(new Instruction(Opcode.MOV, RSI, RDI), "mov rsi,rdi", "48 89 fe"),
				test(new Instruction(Opcode.MOV, RSP, RAX), "mov rsp,rax", "48 89 c4"),
				test(new Instruction(Opcode.MOV, RSP, RBP), "mov rsp,rbp", "48 89 ec"),
				test(new Instruction(Opcode.MOV, RSP, RBX), "mov rsp,rbx", "48 89 dc"),
				test(new Instruction(Opcode.MOV, RSP, RCX), "mov rsp,rcx", "48 89 cc"),
				test(new Instruction(Opcode.MOV, RSP, RDI), "mov rsp,rdi", "48 89 fc"),
				test(new Instruction(Opcode.MOV, RSP, RDX), "mov rsp,rdx", "48 89 d4"),
				test(new Instruction(Opcode.MOV, RSP, RSI), "mov rsp,rsi", "48 89 f4"),
				test(new Instruction(Opcode.MOV, RSP, RSP), "mov rsp,rsp", "48 89 e4"),
				//
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"mov BYTE PTR [r11+r8*4+0x12345678],0x99",
						"43 c6 84 83 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"mov BYTE PTR [rbx+r8*4+0x12345678],0x99",
						"42 c6 84 83 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDI)
										.build(),
								BL),
						"mov BYTE PTR [rdi],bl",
						"88 1f"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSP)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								BH),
						"mov BYTE PTR [rsp+rcx*4+0x12345678],bh",
						"88 bc 8c 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSP)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								CL),
						"mov BYTE PTR [rsp+rcx*4+0x12345678],cl",
						"88 8c 8c 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSP)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								DIL),
						"mov BYTE PTR [rsp+rcx*4+0x12345678],dil",
						"40 88 bc 8c 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSP)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9B),
						"mov BYTE PTR [rsp+rcx*4+0x12345678],r9b",
						"44 88 8c 8c 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc6a86)
										.build(),
								EAX),
						"mov DWORD PTR [rip+0xc6a86],eax",
						"89 05 86 6a 0c 00"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0xdeadbeef)),
						"mov DWORD PTR [r11+r8*4+0x12345678],0xdeadbeef",
						"43 c7 84 83 78 56 34 12 ef be ad de"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc6ac2)
										.build(),
								new Immediate(1)),
						"mov DWORD PTR [rip+0xc6ac2],0x00000001",
						"c7 05 c2 6a 0c 00 01 00 00 00"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement(0x7eadbeef)
										.build(),
								iimm),
						"mov DWORD PTR [rbp+0x7eadbeef],0x12345678",
						"c7 85 ef be ad 7e 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build(),
								iimm),
						"mov DWORD PTR [rbp+0x0],0x12345678",
						"c7 45 00 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement(0x7eadbeef)
										.build(),
								iimm),
						"mov QWORD PTR [rbp+0x7eadbeef],0x12345678",
						"48 c7 85 ef be ad 7e 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RSI),
						"mov QWORD PTR [rbp+r9*4+0x12345678],rsi",
						"4a 89 b4 8d 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((short) 0xbeef)),
						"mov WORD PTR [r11+r8*4+0x12345678],0xbeef",
						"66 43 c7 84 83 78 56 34 12 ef be"),
				test(
						new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R15)
										.build(),
								SI),
						"mov WORD PTR [r15],si",
						"66 41 89 37"),
				test(
						new Instruction(
								Opcode.MOV,
								AL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"mov al,BYTE PTR [rax+rbx*8+0x12345678]",
						"8a 84 d8 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								CX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSP)
										.displacement((byte) 0x10)
										.build()),
						"mov cx,WORD PTR [rsp+0x10]",
						"66 8b 4c 24 10"),
				test(new Instruction(Opcode.MOV, ESI, iimm), "mov esi,0x12345678", "be 78 56 34 12"),
				test(new Instruction(Opcode.MOV, R11B, bimm), "mov r11b,0x12", "41 b3 12"),
				test(new Instruction(Opcode.MOV, BL, bimm), "mov bl,0x12", "b3 12"),
				test(new Instruction(Opcode.MOV, DH, bimm), "mov dh,0x12", "b6 12"),
				test(new Instruction(Opcode.MOV, R8W, simm), "mov r8w,0x1234", "66 41 b8 34 12"),
				test(new Instruction(Opcode.MOV, R9, iimm), "mov r9,0x12345678", "49 c7 c1 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								RSI,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"mov rsi,QWORD PTR [rbp+r9*4+0x12345678]",
						"4a 8b b4 8d 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOV,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc6aaf)
										.build()),
						"mov eax,DWORD PTR [rip+0xc6aaf]",
						"8b 05 af 6a 0c 00"));
	}

	private static List<X64EncodingTestCase> movsxd() {
		return List.of(
				test(new Instruction(Opcode.MOVSXD, R10, EAX), "movsxd r10,eax", "4c 63 d0"),
				test(new Instruction(Opcode.MOVSXD, R10, EBP), "movsxd r10,ebp", "4c 63 d5"),
				test(new Instruction(Opcode.MOVSXD, R10, EBX), "movsxd r10,ebx", "4c 63 d3"),
				test(new Instruction(Opcode.MOVSXD, R10, ECX), "movsxd r10,ecx", "4c 63 d1"),
				test(new Instruction(Opcode.MOVSXD, R10, EDI), "movsxd r10,edi", "4c 63 d7"),
				test(new Instruction(Opcode.MOVSXD, R10, EDX), "movsxd r10,edx", "4c 63 d2"),
				test(new Instruction(Opcode.MOVSXD, R10, ESI), "movsxd r10,esi", "4c 63 d6"),
				test(new Instruction(Opcode.MOVSXD, R10, ESP), "movsxd r10,esp", "4c 63 d4"),
				test(new Instruction(Opcode.MOVSXD, R10, R10D), "movsxd r10,r10d", "4d 63 d2"),
				test(new Instruction(Opcode.MOVSXD, R10, R11D), "movsxd r10,r11d", "4d 63 d3"),
				test(new Instruction(Opcode.MOVSXD, R10, R12D), "movsxd r10,r12d", "4d 63 d4"),
				test(new Instruction(Opcode.MOVSXD, R10, R13D), "movsxd r10,r13d", "4d 63 d5"),
				test(new Instruction(Opcode.MOVSXD, R10, R14D), "movsxd r10,r14d", "4d 63 d6"),
				test(new Instruction(Opcode.MOVSXD, R10, R15D), "movsxd r10,r15d", "4d 63 d7"),
				test(new Instruction(Opcode.MOVSXD, R10, R8D), "movsxd r10,r8d", "4d 63 d0"),
				test(new Instruction(Opcode.MOVSXD, R10, R9D), "movsxd r10,r9d", "4d 63 d1"),
				test(new Instruction(Opcode.MOVSXD, R11, EAX), "movsxd r11,eax", "4c 63 d8"),
				test(new Instruction(Opcode.MOVSXD, R11, EBP), "movsxd r11,ebp", "4c 63 dd"),
				test(new Instruction(Opcode.MOVSXD, R11, EBX), "movsxd r11,ebx", "4c 63 db"),
				test(new Instruction(Opcode.MOVSXD, R11, ECX), "movsxd r11,ecx", "4c 63 d9"),
				test(new Instruction(Opcode.MOVSXD, R11, EDI), "movsxd r11,edi", "4c 63 df"),
				test(new Instruction(Opcode.MOVSXD, R11, EDX), "movsxd r11,edx", "4c 63 da"),
				test(new Instruction(Opcode.MOVSXD, R11, ESI), "movsxd r11,esi", "4c 63 de"),
				test(new Instruction(Opcode.MOVSXD, R11, ESP), "movsxd r11,esp", "4c 63 dc"),
				test(new Instruction(Opcode.MOVSXD, R11, R10D), "movsxd r11,r10d", "4d 63 da"),
				test(new Instruction(Opcode.MOVSXD, R11, R11D), "movsxd r11,r11d", "4d 63 db"),
				test(new Instruction(Opcode.MOVSXD, R11, R12D), "movsxd r11,r12d", "4d 63 dc"),
				test(new Instruction(Opcode.MOVSXD, R11, R13D), "movsxd r11,r13d", "4d 63 dd"),
				test(new Instruction(Opcode.MOVSXD, R11, R14D), "movsxd r11,r14d", "4d 63 de"),
				test(new Instruction(Opcode.MOVSXD, R11, R15D), "movsxd r11,r15d", "4d 63 df"),
				test(new Instruction(Opcode.MOVSXD, R11, R8D), "movsxd r11,r8d", "4d 63 d8"),
				test(new Instruction(Opcode.MOVSXD, R11, R9D), "movsxd r11,r9d", "4d 63 d9"),
				test(new Instruction(Opcode.MOVSXD, R12, EAX), "movsxd r12,eax", "4c 63 e0"),
				test(new Instruction(Opcode.MOVSXD, R12, EBP), "movsxd r12,ebp", "4c 63 e5"),
				test(new Instruction(Opcode.MOVSXD, R12, EBX), "movsxd r12,ebx", "4c 63 e3"),
				test(new Instruction(Opcode.MOVSXD, R12, ECX), "movsxd r12,ecx", "4c 63 e1"),
				test(new Instruction(Opcode.MOVSXD, R12, EDI), "movsxd r12,edi", "4c 63 e7"),
				test(new Instruction(Opcode.MOVSXD, R12, EDX), "movsxd r12,edx", "4c 63 e2"),
				test(new Instruction(Opcode.MOVSXD, R12, ESI), "movsxd r12,esi", "4c 63 e6"),
				test(new Instruction(Opcode.MOVSXD, R12, ESP), "movsxd r12,esp", "4c 63 e4"),
				test(new Instruction(Opcode.MOVSXD, R12, R10D), "movsxd r12,r10d", "4d 63 e2"),
				test(new Instruction(Opcode.MOVSXD, R12, R11D), "movsxd r12,r11d", "4d 63 e3"),
				test(new Instruction(Opcode.MOVSXD, R12, R12D), "movsxd r12,r12d", "4d 63 e4"),
				test(new Instruction(Opcode.MOVSXD, R12, R13D), "movsxd r12,r13d", "4d 63 e5"),
				test(new Instruction(Opcode.MOVSXD, R12, R14D), "movsxd r12,r14d", "4d 63 e6"),
				test(new Instruction(Opcode.MOVSXD, R12, R15D), "movsxd r12,r15d", "4d 63 e7"),
				test(new Instruction(Opcode.MOVSXD, R12, R8D), "movsxd r12,r8d", "4d 63 e0"),
				test(new Instruction(Opcode.MOVSXD, R12, R9D), "movsxd r12,r9d", "4d 63 e1"),
				test(new Instruction(Opcode.MOVSXD, R13, EAX), "movsxd r13,eax", "4c 63 e8"),
				test(new Instruction(Opcode.MOVSXD, R13, EBP), "movsxd r13,ebp", "4c 63 ed"),
				test(new Instruction(Opcode.MOVSXD, R13, EBX), "movsxd r13,ebx", "4c 63 eb"),
				test(new Instruction(Opcode.MOVSXD, R13, ECX), "movsxd r13,ecx", "4c 63 e9"),
				test(new Instruction(Opcode.MOVSXD, R13, EDI), "movsxd r13,edi", "4c 63 ef"),
				test(new Instruction(Opcode.MOVSXD, R13, EDX), "movsxd r13,edx", "4c 63 ea"),
				test(new Instruction(Opcode.MOVSXD, R13, ESI), "movsxd r13,esi", "4c 63 ee"),
				test(new Instruction(Opcode.MOVSXD, R13, ESP), "movsxd r13,esp", "4c 63 ec"),
				test(new Instruction(Opcode.MOVSXD, R13, R10D), "movsxd r13,r10d", "4d 63 ea"),
				test(new Instruction(Opcode.MOVSXD, R13, R11D), "movsxd r13,r11d", "4d 63 eb"),
				test(new Instruction(Opcode.MOVSXD, R13, R12D), "movsxd r13,r12d", "4d 63 ec"),
				test(new Instruction(Opcode.MOVSXD, R13, R13D), "movsxd r13,r13d", "4d 63 ed"),
				test(new Instruction(Opcode.MOVSXD, R13, R14D), "movsxd r13,r14d", "4d 63 ee"),
				test(new Instruction(Opcode.MOVSXD, R13, R15D), "movsxd r13,r15d", "4d 63 ef"),
				test(new Instruction(Opcode.MOVSXD, R13, R8D), "movsxd r13,r8d", "4d 63 e8"),
				test(new Instruction(Opcode.MOVSXD, R13, R9D), "movsxd r13,r9d", "4d 63 e9"),
				test(new Instruction(Opcode.MOVSXD, R14, EAX), "movsxd r14,eax", "4c 63 f0"),
				test(new Instruction(Opcode.MOVSXD, R14, EBP), "movsxd r14,ebp", "4c 63 f5"),
				test(new Instruction(Opcode.MOVSXD, R14, EBX), "movsxd r14,ebx", "4c 63 f3"),
				test(new Instruction(Opcode.MOVSXD, R14, ECX), "movsxd r14,ecx", "4c 63 f1"),
				test(new Instruction(Opcode.MOVSXD, R14, EDI), "movsxd r14,edi", "4c 63 f7"),
				test(new Instruction(Opcode.MOVSXD, R14, EDX), "movsxd r14,edx", "4c 63 f2"),
				test(new Instruction(Opcode.MOVSXD, R14, ESI), "movsxd r14,esi", "4c 63 f6"),
				test(new Instruction(Opcode.MOVSXD, R14, ESP), "movsxd r14,esp", "4c 63 f4"),
				test(new Instruction(Opcode.MOVSXD, R14, R10D), "movsxd r14,r10d", "4d 63 f2"),
				test(new Instruction(Opcode.MOVSXD, R14, R11D), "movsxd r14,r11d", "4d 63 f3"),
				test(new Instruction(Opcode.MOVSXD, R14, R12D), "movsxd r14,r12d", "4d 63 f4"),
				test(new Instruction(Opcode.MOVSXD, R14, R13D), "movsxd r14,r13d", "4d 63 f5"),
				test(new Instruction(Opcode.MOVSXD, R14, R14D), "movsxd r14,r14d", "4d 63 f6"),
				test(new Instruction(Opcode.MOVSXD, R14, R15D), "movsxd r14,r15d", "4d 63 f7"),
				test(new Instruction(Opcode.MOVSXD, R14, R8D), "movsxd r14,r8d", "4d 63 f0"),
				test(new Instruction(Opcode.MOVSXD, R14, R9D), "movsxd r14,r9d", "4d 63 f1"),
				test(new Instruction(Opcode.MOVSXD, R15, EAX), "movsxd r15,eax", "4c 63 f8"),
				test(new Instruction(Opcode.MOVSXD, R15, EBP), "movsxd r15,ebp", "4c 63 fd"),
				test(new Instruction(Opcode.MOVSXD, R15, EBX), "movsxd r15,ebx", "4c 63 fb"),
				test(new Instruction(Opcode.MOVSXD, R15, ECX), "movsxd r15,ecx", "4c 63 f9"),
				test(new Instruction(Opcode.MOVSXD, R15, EDI), "movsxd r15,edi", "4c 63 ff"),
				test(new Instruction(Opcode.MOVSXD, R15, EDX), "movsxd r15,edx", "4c 63 fa"),
				test(new Instruction(Opcode.MOVSXD, R15, ESI), "movsxd r15,esi", "4c 63 fe"),
				test(new Instruction(Opcode.MOVSXD, R15, ESP), "movsxd r15,esp", "4c 63 fc"),
				test(new Instruction(Opcode.MOVSXD, R15, R10D), "movsxd r15,r10d", "4d 63 fa"),
				test(new Instruction(Opcode.MOVSXD, R15, R11D), "movsxd r15,r11d", "4d 63 fb"),
				test(new Instruction(Opcode.MOVSXD, R15, R12D), "movsxd r15,r12d", "4d 63 fc"),
				test(new Instruction(Opcode.MOVSXD, R15, R13D), "movsxd r15,r13d", "4d 63 fd"),
				test(new Instruction(Opcode.MOVSXD, R15, R14D), "movsxd r15,r14d", "4d 63 fe"),
				test(new Instruction(Opcode.MOVSXD, R15, R15D), "movsxd r15,r15d", "4d 63 ff"),
				test(new Instruction(Opcode.MOVSXD, R15, R8D), "movsxd r15,r8d", "4d 63 f8"),
				test(new Instruction(Opcode.MOVSXD, R15, R9D), "movsxd r15,r9d", "4d 63 f9"),
				test(new Instruction(Opcode.MOVSXD, R8, EAX), "movsxd r8,eax", "4c 63 c0"),
				test(new Instruction(Opcode.MOVSXD, R8, EBP), "movsxd r8,ebp", "4c 63 c5"),
				test(new Instruction(Opcode.MOVSXD, R8, EBX), "movsxd r8,ebx", "4c 63 c3"),
				test(new Instruction(Opcode.MOVSXD, R8, ECX), "movsxd r8,ecx", "4c 63 c1"),
				test(new Instruction(Opcode.MOVSXD, R8, EDI), "movsxd r8,edi", "4c 63 c7"),
				test(new Instruction(Opcode.MOVSXD, R8, EDX), "movsxd r8,edx", "4c 63 c2"),
				test(new Instruction(Opcode.MOVSXD, R8, ESI), "movsxd r8,esi", "4c 63 c6"),
				test(new Instruction(Opcode.MOVSXD, R8, ESP), "movsxd r8,esp", "4c 63 c4"),
				test(new Instruction(Opcode.MOVSXD, R8, R10D), "movsxd r8,r10d", "4d 63 c2"),
				test(new Instruction(Opcode.MOVSXD, R8, R11D), "movsxd r8,r11d", "4d 63 c3"),
				test(new Instruction(Opcode.MOVSXD, R8, R12D), "movsxd r8,r12d", "4d 63 c4"),
				test(new Instruction(Opcode.MOVSXD, R8, R13D), "movsxd r8,r13d", "4d 63 c5"),
				test(new Instruction(Opcode.MOVSXD, R8, R14D), "movsxd r8,r14d", "4d 63 c6"),
				test(new Instruction(Opcode.MOVSXD, R8, R15D), "movsxd r8,r15d", "4d 63 c7"),
				test(new Instruction(Opcode.MOVSXD, R8, R8D), "movsxd r8,r8d", "4d 63 c0"),
				test(new Instruction(Opcode.MOVSXD, R8, R9D), "movsxd r8,r9d", "4d 63 c1"),
				test(new Instruction(Opcode.MOVSXD, R9, EAX), "movsxd r9,eax", "4c 63 c8"),
				test(new Instruction(Opcode.MOVSXD, R9, EBP), "movsxd r9,ebp", "4c 63 cd"),
				test(new Instruction(Opcode.MOVSXD, R9, EBX), "movsxd r9,ebx", "4c 63 cb"),
				test(new Instruction(Opcode.MOVSXD, R9, ECX), "movsxd r9,ecx", "4c 63 c9"),
				test(new Instruction(Opcode.MOVSXD, R9, EDI), "movsxd r9,edi", "4c 63 cf"),
				test(new Instruction(Opcode.MOVSXD, R9, EDX), "movsxd r9,edx", "4c 63 ca"),
				test(new Instruction(Opcode.MOVSXD, R9, ESI), "movsxd r9,esi", "4c 63 ce"),
				test(new Instruction(Opcode.MOVSXD, R9, ESP), "movsxd r9,esp", "4c 63 cc"),
				test(new Instruction(Opcode.MOVSXD, R9, R10D), "movsxd r9,r10d", "4d 63 ca"),
				test(new Instruction(Opcode.MOVSXD, R9, R11D), "movsxd r9,r11d", "4d 63 cb"),
				test(new Instruction(Opcode.MOVSXD, R9, R12D), "movsxd r9,r12d", "4d 63 cc"),
				test(new Instruction(Opcode.MOVSXD, R9, R13D), "movsxd r9,r13d", "4d 63 cd"),
				test(new Instruction(Opcode.MOVSXD, R9, R14D), "movsxd r9,r14d", "4d 63 ce"),
				test(new Instruction(Opcode.MOVSXD, R9, R15D), "movsxd r9,r15d", "4d 63 cf"),
				test(new Instruction(Opcode.MOVSXD, R9, R8D), "movsxd r9,r8d", "4d 63 c8"),
				test(new Instruction(Opcode.MOVSXD, R9, R9D), "movsxd r9,r9d", "4d 63 c9"),
				test(new Instruction(Opcode.MOVSXD, RAX, EAX), "movsxd rax,eax", "48 63 c0"),
				test(new Instruction(Opcode.MOVSXD, RAX, EBP), "movsxd rax,ebp", "48 63 c5"),
				test(new Instruction(Opcode.MOVSXD, RAX, EBX), "movsxd rax,ebx", "48 63 c3"),
				test(new Instruction(Opcode.MOVSXD, RAX, ECX), "movsxd rax,ecx", "48 63 c1"),
				test(new Instruction(Opcode.MOVSXD, RAX, EDI), "movsxd rax,edi", "48 63 c7"),
				test(new Instruction(Opcode.MOVSXD, RAX, EDX), "movsxd rax,edx", "48 63 c2"),
				test(new Instruction(Opcode.MOVSXD, RAX, ESI), "movsxd rax,esi", "48 63 c6"),
				test(new Instruction(Opcode.MOVSXD, RAX, ESP), "movsxd rax,esp", "48 63 c4"),
				test(new Instruction(Opcode.MOVSXD, RAX, R10D), "movsxd rax,r10d", "49 63 c2"),
				test(new Instruction(Opcode.MOVSXD, RAX, R11D), "movsxd rax,r11d", "49 63 c3"),
				test(new Instruction(Opcode.MOVSXD, RAX, R12D), "movsxd rax,r12d", "49 63 c4"),
				test(new Instruction(Opcode.MOVSXD, RAX, R13D), "movsxd rax,r13d", "49 63 c5"),
				test(new Instruction(Opcode.MOVSXD, RAX, R14D), "movsxd rax,r14d", "49 63 c6"),
				test(new Instruction(Opcode.MOVSXD, RAX, R15D), "movsxd rax,r15d", "49 63 c7"),
				test(new Instruction(Opcode.MOVSXD, RAX, R8D), "movsxd rax,r8d", "49 63 c0"),
				test(new Instruction(Opcode.MOVSXD, RAX, R9D), "movsxd rax,r9d", "49 63 c1"),
				test(new Instruction(Opcode.MOVSXD, RBP, EAX), "movsxd rbp,eax", "48 63 e8"),
				test(new Instruction(Opcode.MOVSXD, RBP, EBP), "movsxd rbp,ebp", "48 63 ed"),
				test(new Instruction(Opcode.MOVSXD, RBP, EBX), "movsxd rbp,ebx", "48 63 eb"),
				test(new Instruction(Opcode.MOVSXD, RBP, ECX), "movsxd rbp,ecx", "48 63 e9"),
				test(new Instruction(Opcode.MOVSXD, RBP, EDI), "movsxd rbp,edi", "48 63 ef"),
				test(new Instruction(Opcode.MOVSXD, RBP, EDX), "movsxd rbp,edx", "48 63 ea"),
				test(new Instruction(Opcode.MOVSXD, RBP, ESI), "movsxd rbp,esi", "48 63 ee"),
				test(new Instruction(Opcode.MOVSXD, RBP, ESP), "movsxd rbp,esp", "48 63 ec"),
				test(new Instruction(Opcode.MOVSXD, RBP, R10D), "movsxd rbp,r10d", "49 63 ea"),
				test(new Instruction(Opcode.MOVSXD, RBP, R11D), "movsxd rbp,r11d", "49 63 eb"),
				test(new Instruction(Opcode.MOVSXD, RBP, R12D), "movsxd rbp,r12d", "49 63 ec"),
				test(new Instruction(Opcode.MOVSXD, RBP, R13D), "movsxd rbp,r13d", "49 63 ed"),
				test(new Instruction(Opcode.MOVSXD, RBP, R14D), "movsxd rbp,r14d", "49 63 ee"),
				test(new Instruction(Opcode.MOVSXD, RBP, R15D), "movsxd rbp,r15d", "49 63 ef"),
				test(new Instruction(Opcode.MOVSXD, RBP, R8D), "movsxd rbp,r8d", "49 63 e8"),
				test(new Instruction(Opcode.MOVSXD, RBP, R9D), "movsxd rbp,r9d", "49 63 e9"),
				test(new Instruction(Opcode.MOVSXD, RBX, EAX), "movsxd rbx,eax", "48 63 d8"),
				test(new Instruction(Opcode.MOVSXD, RBX, EBP), "movsxd rbx,ebp", "48 63 dd"),
				test(new Instruction(Opcode.MOVSXD, RBX, EBX), "movsxd rbx,ebx", "48 63 db"),
				test(new Instruction(Opcode.MOVSXD, RBX, ECX), "movsxd rbx,ecx", "48 63 d9"),
				test(new Instruction(Opcode.MOVSXD, RBX, EDI), "movsxd rbx,edi", "48 63 df"),
				test(new Instruction(Opcode.MOVSXD, RBX, EDX), "movsxd rbx,edx", "48 63 da"),
				test(new Instruction(Opcode.MOVSXD, RBX, ESI), "movsxd rbx,esi", "48 63 de"),
				test(new Instruction(Opcode.MOVSXD, RBX, ESP), "movsxd rbx,esp", "48 63 dc"),
				test(new Instruction(Opcode.MOVSXD, RBX, R10D), "movsxd rbx,r10d", "49 63 da"),
				test(new Instruction(Opcode.MOVSXD, RBX, R11D), "movsxd rbx,r11d", "49 63 db"),
				test(new Instruction(Opcode.MOVSXD, RBX, R12D), "movsxd rbx,r12d", "49 63 dc"),
				test(new Instruction(Opcode.MOVSXD, RBX, R13D), "movsxd rbx,r13d", "49 63 dd"),
				test(new Instruction(Opcode.MOVSXD, RBX, R14D), "movsxd rbx,r14d", "49 63 de"),
				test(new Instruction(Opcode.MOVSXD, RBX, R15D), "movsxd rbx,r15d", "49 63 df"),
				test(new Instruction(Opcode.MOVSXD, RBX, R8D), "movsxd rbx,r8d", "49 63 d8"),
				test(new Instruction(Opcode.MOVSXD, RBX, R9D), "movsxd rbx,r9d", "49 63 d9"),
				test(new Instruction(Opcode.MOVSXD, RCX, EAX), "movsxd rcx,eax", "48 63 c8"),
				test(new Instruction(Opcode.MOVSXD, RCX, EBP), "movsxd rcx,ebp", "48 63 cd"),
				test(new Instruction(Opcode.MOVSXD, RCX, EBX), "movsxd rcx,ebx", "48 63 cb"),
				test(new Instruction(Opcode.MOVSXD, RCX, ECX), "movsxd rcx,ecx", "48 63 c9"),
				test(new Instruction(Opcode.MOVSXD, RCX, EDI), "movsxd rcx,edi", "48 63 cf"),
				test(new Instruction(Opcode.MOVSXD, RCX, EDX), "movsxd rcx,edx", "48 63 ca"),
				test(new Instruction(Opcode.MOVSXD, RCX, ESI), "movsxd rcx,esi", "48 63 ce"),
				test(new Instruction(Opcode.MOVSXD, RCX, ESP), "movsxd rcx,esp", "48 63 cc"),
				test(new Instruction(Opcode.MOVSXD, RCX, R10D), "movsxd rcx,r10d", "49 63 ca"),
				test(new Instruction(Opcode.MOVSXD, RCX, R11D), "movsxd rcx,r11d", "49 63 cb"),
				test(new Instruction(Opcode.MOVSXD, RCX, R12D), "movsxd rcx,r12d", "49 63 cc"),
				test(new Instruction(Opcode.MOVSXD, RCX, R13D), "movsxd rcx,r13d", "49 63 cd"),
				test(new Instruction(Opcode.MOVSXD, RCX, R14D), "movsxd rcx,r14d", "49 63 ce"),
				test(new Instruction(Opcode.MOVSXD, RCX, R15D), "movsxd rcx,r15d", "49 63 cf"),
				test(new Instruction(Opcode.MOVSXD, RCX, R8D), "movsxd rcx,r8d", "49 63 c8"),
				test(new Instruction(Opcode.MOVSXD, RCX, R9D), "movsxd rcx,r9d", "49 63 c9"),
				test(new Instruction(Opcode.MOVSXD, RDI, EAX), "movsxd rdi,eax", "48 63 f8"),
				test(new Instruction(Opcode.MOVSXD, RDI, EBP), "movsxd rdi,ebp", "48 63 fd"),
				test(new Instruction(Opcode.MOVSXD, RDI, EBX), "movsxd rdi,ebx", "48 63 fb"),
				test(new Instruction(Opcode.MOVSXD, RDI, ECX), "movsxd rdi,ecx", "48 63 f9"),
				test(new Instruction(Opcode.MOVSXD, RDI, EDI), "movsxd rdi,edi", "48 63 ff"),
				test(new Instruction(Opcode.MOVSXD, RDI, EDX), "movsxd rdi,edx", "48 63 fa"),
				test(new Instruction(Opcode.MOVSXD, RDI, ESI), "movsxd rdi,esi", "48 63 fe"),
				test(new Instruction(Opcode.MOVSXD, RDI, ESP), "movsxd rdi,esp", "48 63 fc"),
				test(new Instruction(Opcode.MOVSXD, RDI, R10D), "movsxd rdi,r10d", "49 63 fa"),
				test(new Instruction(Opcode.MOVSXD, RDI, R11D), "movsxd rdi,r11d", "49 63 fb"),
				test(new Instruction(Opcode.MOVSXD, RDI, R12D), "movsxd rdi,r12d", "49 63 fc"),
				test(new Instruction(Opcode.MOVSXD, RDI, R13D), "movsxd rdi,r13d", "49 63 fd"),
				test(new Instruction(Opcode.MOVSXD, RDI, R14D), "movsxd rdi,r14d", "49 63 fe"),
				test(new Instruction(Opcode.MOVSXD, RDI, R15D), "movsxd rdi,r15d", "49 63 ff"),
				test(new Instruction(Opcode.MOVSXD, RDI, R8D), "movsxd rdi,r8d", "49 63 f8"),
				test(new Instruction(Opcode.MOVSXD, RDI, R9D), "movsxd rdi,r9d", "49 63 f9"),
				test(new Instruction(Opcode.MOVSXD, RDX, EAX), "movsxd rdx,eax", "48 63 d0"),
				test(new Instruction(Opcode.MOVSXD, RDX, EBP), "movsxd rdx,ebp", "48 63 d5"),
				test(new Instruction(Opcode.MOVSXD, RDX, EBX), "movsxd rdx,ebx", "48 63 d3"),
				test(new Instruction(Opcode.MOVSXD, RDX, ECX), "movsxd rdx,ecx", "48 63 d1"),
				test(new Instruction(Opcode.MOVSXD, RDX, EDI), "movsxd rdx,edi", "48 63 d7"),
				test(new Instruction(Opcode.MOVSXD, RDX, EDX), "movsxd rdx,edx", "48 63 d2"),
				test(new Instruction(Opcode.MOVSXD, RDX, ESI), "movsxd rdx,esi", "48 63 d6"),
				test(new Instruction(Opcode.MOVSXD, RDX, ESP), "movsxd rdx,esp", "48 63 d4"),
				test(new Instruction(Opcode.MOVSXD, RDX, R10D), "movsxd rdx,r10d", "49 63 d2"),
				test(new Instruction(Opcode.MOVSXD, RDX, R11D), "movsxd rdx,r11d", "49 63 d3"),
				test(new Instruction(Opcode.MOVSXD, RDX, R12D), "movsxd rdx,r12d", "49 63 d4"),
				test(new Instruction(Opcode.MOVSXD, RDX, R13D), "movsxd rdx,r13d", "49 63 d5"),
				test(new Instruction(Opcode.MOVSXD, RDX, R14D), "movsxd rdx,r14d", "49 63 d6"),
				test(new Instruction(Opcode.MOVSXD, RDX, R15D), "movsxd rdx,r15d", "49 63 d7"),
				test(new Instruction(Opcode.MOVSXD, RDX, R8D), "movsxd rdx,r8d", "49 63 d0"),
				test(new Instruction(Opcode.MOVSXD, RDX, R9D), "movsxd rdx,r9d", "49 63 d1"),
				test(new Instruction(Opcode.MOVSXD, RSI, EAX), "movsxd rsi,eax", "48 63 f0"),
				test(new Instruction(Opcode.MOVSXD, RSI, EBP), "movsxd rsi,ebp", "48 63 f5"),
				test(new Instruction(Opcode.MOVSXD, RSI, EBX), "movsxd rsi,ebx", "48 63 f3"),
				test(new Instruction(Opcode.MOVSXD, RSI, ECX), "movsxd rsi,ecx", "48 63 f1"),
				test(new Instruction(Opcode.MOVSXD, RSI, EDI), "movsxd rsi,edi", "48 63 f7"),
				test(new Instruction(Opcode.MOVSXD, RSI, EDX), "movsxd rsi,edx", "48 63 f2"),
				test(new Instruction(Opcode.MOVSXD, RSI, ESI), "movsxd rsi,esi", "48 63 f6"),
				test(new Instruction(Opcode.MOVSXD, RSI, ESP), "movsxd rsi,esp", "48 63 f4"),
				test(new Instruction(Opcode.MOVSXD, RSI, R10D), "movsxd rsi,r10d", "49 63 f2"),
				test(new Instruction(Opcode.MOVSXD, RSI, R11D), "movsxd rsi,r11d", "49 63 f3"),
				test(new Instruction(Opcode.MOVSXD, RSI, R12D), "movsxd rsi,r12d", "49 63 f4"),
				test(new Instruction(Opcode.MOVSXD, RSI, R13D), "movsxd rsi,r13d", "49 63 f5"),
				test(new Instruction(Opcode.MOVSXD, RSI, R14D), "movsxd rsi,r14d", "49 63 f6"),
				test(new Instruction(Opcode.MOVSXD, RSI, R15D), "movsxd rsi,r15d", "49 63 f7"),
				test(new Instruction(Opcode.MOVSXD, RSI, R8D), "movsxd rsi,r8d", "49 63 f0"),
				test(new Instruction(Opcode.MOVSXD, RSI, R9D), "movsxd rsi,r9d", "49 63 f1"),
				test(new Instruction(Opcode.MOVSXD, RSP, EAX), "movsxd rsp,eax", "48 63 e0"),
				test(new Instruction(Opcode.MOVSXD, RSP, EBP), "movsxd rsp,ebp", "48 63 e5"),
				test(new Instruction(Opcode.MOVSXD, RSP, EBX), "movsxd rsp,ebx", "48 63 e3"),
				test(new Instruction(Opcode.MOVSXD, RSP, ECX), "movsxd rsp,ecx", "48 63 e1"),
				test(new Instruction(Opcode.MOVSXD, RSP, EDI), "movsxd rsp,edi", "48 63 e7"),
				test(new Instruction(Opcode.MOVSXD, RSP, EDX), "movsxd rsp,edx", "48 63 e2"),
				test(new Instruction(Opcode.MOVSXD, RSP, ESI), "movsxd rsp,esi", "48 63 e6"),
				test(new Instruction(Opcode.MOVSXD, RSP, ESP), "movsxd rsp,esp", "48 63 e4"),
				test(new Instruction(Opcode.MOVSXD, RSP, R10D), "movsxd rsp,r10d", "49 63 e2"),
				test(new Instruction(Opcode.MOVSXD, RSP, R11D), "movsxd rsp,r11d", "49 63 e3"),
				test(new Instruction(Opcode.MOVSXD, RSP, R12D), "movsxd rsp,r12d", "49 63 e4"),
				test(new Instruction(Opcode.MOVSXD, RSP, R13D), "movsxd rsp,r13d", "49 63 e5"),
				test(new Instruction(Opcode.MOVSXD, RSP, R14D), "movsxd rsp,r14d", "49 63 e6"),
				test(new Instruction(Opcode.MOVSXD, RSP, R15D), "movsxd rsp,r15d", "49 63 e7"),
				test(new Instruction(Opcode.MOVSXD, RSP, R8D), "movsxd rsp,r8d", "49 63 e0"),
				test(new Instruction(Opcode.MOVSXD, RSP, R9D), "movsxd rsp,r9d", "49 63 e1"),
				//
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r10,DWORD PTR [r10]",
						"4d 63 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r10,DWORD PTR [r11]",
						"4d 63 13"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r10,DWORD PTR [r12]",
						"4d 63 14 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.displacement((byte) 0)
										.build()),
						"movsxd r10,DWORD PTR [r12+0x0]",
						"4d 63 54 24 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r10,DWORD PTR [r13+0x0]",
						"4d 63 55 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r10,DWORD PTR [r14]",
						"4d 63 16"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r10,DWORD PTR [r15]",
						"4d 63 17"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r10,DWORD PTR [r8]",
						"4d 63 10"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r10,DWORD PTR [r9]",
						"4d 63 11"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r10,DWORD PTR [rax]",
						"4c 63 10"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r10,DWORD PTR [rbp+0x0]",
						"4c 63 55 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r10,DWORD PTR [rbx]",
						"4c 63 13"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r10,DWORD PTR [rcx]",
						"4c 63 11"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r10,DWORD PTR [rdi]",
						"4c 63 17"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r10,DWORD PTR [rdx]",
						"4c 63 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r10,DWORD PTR [rsi]",
						"4c 63 16"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R10,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r10,DWORD PTR [rsp]",
						"4c 63 14 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r11,DWORD PTR [r10]",
						"4d 63 1a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r11,DWORD PTR [r11]",
						"4d 63 1b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r11,DWORD PTR [r12]",
						"4d 63 1c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r11,DWORD PTR [r13+0x0]",
						"4d 63 5d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r11,DWORD PTR [r14]",
						"4d 63 1e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r11,DWORD PTR [r15]",
						"4d 63 1f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r11,DWORD PTR [r8]",
						"4d 63 18"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r11,DWORD PTR [r9]",
						"4d 63 19"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r11,DWORD PTR [rax]",
						"4c 63 18"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r11,DWORD PTR [rbp+0x0]",
						"4c 63 5d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r11,DWORD PTR [rbx]",
						"4c 63 1b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r11,DWORD PTR [rcx]",
						"4c 63 19"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r11,DWORD PTR [rdi]",
						"4c 63 1f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r11,DWORD PTR [rdx]",
						"4c 63 1a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r11,DWORD PTR [rsi]",
						"4c 63 1e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R11,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r11,DWORD PTR [rsp]",
						"4c 63 1c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r12,DWORD PTR [r10]",
						"4d 63 22"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r12,DWORD PTR [r11]",
						"4d 63 23"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r12,DWORD PTR [r12]",
						"4d 63 24 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r12,DWORD PTR [r13+0x0]",
						"4d 63 65 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r12,DWORD PTR [r14]",
						"4d 63 26"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r12,DWORD PTR [r15]",
						"4d 63 27"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r12,DWORD PTR [r8]",
						"4d 63 20"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r12,DWORD PTR [r9]",
						"4d 63 21"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r12,DWORD PTR [rax]",
						"4c 63 20"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r12,DWORD PTR [rbp+0x0]",
						"4c 63 65 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r12,DWORD PTR [rbx]",
						"4c 63 23"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r12,DWORD PTR [rcx]",
						"4c 63 21"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r12,DWORD PTR [rdi]",
						"4c 63 27"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r12,DWORD PTR [rdx]",
						"4c 63 22"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r12,DWORD PTR [rsi]",
						"4c 63 26"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R12,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r12,DWORD PTR [rsp]",
						"4c 63 24 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r13,DWORD PTR [r10]",
						"4d 63 2a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r13,DWORD PTR [r11]",
						"4d 63 2b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r13,DWORD PTR [r12]",
						"4d 63 2c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r13,DWORD PTR [r13+0x0]",
						"4d 63 6d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r13,DWORD PTR [r14]",
						"4d 63 2e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r13,DWORD PTR [r15]",
						"4d 63 2f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r13,DWORD PTR [r8]",
						"4d 63 28"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r13,DWORD PTR [r9]",
						"4d 63 29"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r13,DWORD PTR [rax]",
						"4c 63 28"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r13,DWORD PTR [rbp+0x0]",
						"4c 63 6d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r13,DWORD PTR [rbx]",
						"4c 63 2b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r13,DWORD PTR [rcx]",
						"4c 63 29"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r13,DWORD PTR [rdi]",
						"4c 63 2f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r13,DWORD PTR [rdx]",
						"4c 63 2a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r13,DWORD PTR [rsi]",
						"4c 63 2e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r13,DWORD PTR [rsp]",
						"4c 63 2c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r14,DWORD PTR [r10]",
						"4d 63 32"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r14,DWORD PTR [r11]",
						"4d 63 33"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r14,DWORD PTR [r12]",
						"4d 63 34 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r14,DWORD PTR [r13+0x0]",
						"4d 63 75 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r14,DWORD PTR [r14]",
						"4d 63 36"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r14,DWORD PTR [r15]",
						"4d 63 37"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r14,DWORD PTR [r8]",
						"4d 63 30"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r14,DWORD PTR [r9]",
						"4d 63 31"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r14,DWORD PTR [rax]",
						"4c 63 30"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r14,DWORD PTR [rbp+0x0]",
						"4c 63 75 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r14,DWORD PTR [rbx]",
						"4c 63 33"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r14,DWORD PTR [rcx]",
						"4c 63 31"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r14,DWORD PTR [rdi]",
						"4c 63 37"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r14,DWORD PTR [rdx]",
						"4c 63 32"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r14,DWORD PTR [rsi]",
						"4c 63 36"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R14,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r14,DWORD PTR [rsp]",
						"4c 63 34 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r15,DWORD PTR [r10]",
						"4d 63 3a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r15,DWORD PTR [r11]",
						"4d 63 3b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r15,DWORD PTR [r12]",
						"4d 63 3c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r15,DWORD PTR [r13+0x0]",
						"4d 63 7d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r15,DWORD PTR [r14]",
						"4d 63 3e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r15,DWORD PTR [r15]",
						"4d 63 3f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r15,DWORD PTR [r8]",
						"4d 63 38"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r15,DWORD PTR [r9]",
						"4d 63 39"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r15,DWORD PTR [rax]",
						"4c 63 38"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r15,DWORD PTR [rbp+0x0]",
						"4c 63 7d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r15,DWORD PTR [rbx]",
						"4c 63 3b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r15,DWORD PTR [rcx]",
						"4c 63 39"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r15,DWORD PTR [rdi]",
						"4c 63 3f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r15,DWORD PTR [rdx]",
						"4c 63 3a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r15,DWORD PTR [rsi]",
						"4c 63 3e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R15,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r15,DWORD PTR [rsp]",
						"4c 63 3c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r8,DWORD PTR [r10]",
						"4d 63 02"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r8,DWORD PTR [r11]",
						"4d 63 03"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r8,DWORD PTR [r12]",
						"4d 63 04 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r8,DWORD PTR [r13+0x0]",
						"4d 63 45 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r8,DWORD PTR [r14]",
						"4d 63 06"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r8,DWORD PTR [r15]",
						"4d 63 07"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r8,DWORD PTR [r8]",
						"4d 63 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.displacement((byte) 0x12)
										.build()),
						"movsxd r8,DWORD PTR [r8+0x12]",
						"4d 63 40 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r8,DWORD PTR [r9]",
						"4d 63 01"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r8,DWORD PTR [rax]",
						"4c 63 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.displacement((byte) 0x12)
										.build()),
						"movsxd r8,DWORD PTR [rax+0x12]",
						"4c 63 40 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r8,DWORD PTR [rbp+0x0]",
						"4c 63 45 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r8,DWORD PTR [rbx]",
						"4c 63 03"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r8,DWORD PTR [rcx]",
						"4c 63 01"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r8,DWORD PTR [rdi]",
						"4c 63 07"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r8,DWORD PTR [rdx]",
						"4c 63 02"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r8,DWORD PTR [rsi]",
						"4c 63 06"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R8,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r8,DWORD PTR [rsp]",
						"4c 63 04 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd r9,DWORD PTR [r10]",
						"4d 63 0a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd r9,DWORD PTR [r11]",
						"4d 63 0b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd r9,DWORD PTR [r12]",
						"4d 63 0c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd r9,DWORD PTR [r13+0x0]",
						"4d 63 4d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd r9,DWORD PTR [r14]",
						"4d 63 0e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd r9,DWORD PTR [r15]",
						"4d 63 0f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd r9,DWORD PTR [r8]",
						"4d 63 08"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd r9,DWORD PTR [r9]",
						"4d 63 09"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd r9,DWORD PTR [rax]",
						"4c 63 08"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd r9,DWORD PTR [rbp+0x0]",
						"4c 63 4d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd r9,DWORD PTR [rbx]",
						"4c 63 0b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd r9,DWORD PTR [rcx]",
						"4c 63 09"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd r9,DWORD PTR [rdi]",
						"4c 63 0f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd r9,DWORD PTR [rdx]",
						"4c 63 0a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd r9,DWORD PTR [rsi]",
						"4c 63 0e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								R9,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd r9,DWORD PTR [rsp]",
						"4c 63 0c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rax,DWORD PTR [r10]",
						"49 63 02"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rax,DWORD PTR [r11]",
						"49 63 03"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rax,DWORD PTR [r12]",
						"49 63 04 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rax,DWORD PTR [r13+0x0]",
						"49 63 45 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rax,DWORD PTR [r14]",
						"49 63 06"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rax,DWORD PTR [r15]",
						"49 63 07"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rax,DWORD PTR [r8]",
						"49 63 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.displacement((byte) 0x12)
										.build()),
						"movsxd rax,DWORD PTR [r8+0x12]",
						"49 63 40 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rax,DWORD PTR [r9]",
						"49 63 01"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rax,DWORD PTR [rax]",
						"48 63 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rax,DWORD PTR [rbp+0x0]",
						"48 63 45 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rax,DWORD PTR [rbx]",
						"48 63 03"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rax,DWORD PTR [rcx]",
						"48 63 01"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rax,DWORD PTR [rdi]",
						"48 63 07"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rax,DWORD PTR [rdx]",
						"48 63 02"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rax,DWORD PTR [rsi]",
						"48 63 06"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rax,DWORD PTR [rsp]",
						"48 63 04 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rbp,DWORD PTR [r10]",
						"49 63 2a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rbp,DWORD PTR [r11]",
						"49 63 2b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rbp,DWORD PTR [r12]",
						"49 63 2c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rbp,DWORD PTR [r13+0x0]",
						"49 63 6d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rbp,DWORD PTR [r14]",
						"49 63 2e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rbp,DWORD PTR [r15]",
						"49 63 2f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rbp,DWORD PTR [r8]",
						"49 63 28"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rbp,DWORD PTR [r9]",
						"49 63 29"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rbp,DWORD PTR [rax]",
						"48 63 28"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rbp,DWORD PTR [rbp+0x0]",
						"48 63 6d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rbp,DWORD PTR [rbx]",
						"48 63 2b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rbp,DWORD PTR [rcx]",
						"48 63 29"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rbp,DWORD PTR [rdi]",
						"48 63 2f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rbp,DWORD PTR [rdx]",
						"48 63 2a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rbp,DWORD PTR [rsi]",
						"48 63 2e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rbp,DWORD PTR [rsp]",
						"48 63 2c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rbx,DWORD PTR [r10]",
						"49 63 1a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rbx,DWORD PTR [r11]",
						"49 63 1b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rbx,DWORD PTR [r12]",
						"49 63 1c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rbx,DWORD PTR [r13+0x0]",
						"49 63 5d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rbx,DWORD PTR [r14]",
						"49 63 1e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rbx,DWORD PTR [r15]",
						"49 63 1f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rbx,DWORD PTR [r8]",
						"49 63 18"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rbx,DWORD PTR [r9]",
						"49 63 19"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rbx,DWORD PTR [rax]",
						"48 63 18"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rbx,DWORD PTR [rbp+0x0]",
						"48 63 5d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rbx,DWORD PTR [rbx]",
						"48 63 1b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rbx,DWORD PTR [rcx]",
						"48 63 19"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rbx,DWORD PTR [rdi]",
						"48 63 1f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rbx,DWORD PTR [rdx]",
						"48 63 1a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rbx,DWORD PTR [rsi]",
						"48 63 1e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rbx,DWORD PTR [rsp]",
						"48 63 1c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rcx,DWORD PTR [r10]",
						"49 63 0a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rcx,DWORD PTR [r11]",
						"49 63 0b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rcx,DWORD PTR [r12]",
						"49 63 0c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rcx,DWORD PTR [r13+0x0]",
						"49 63 4d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rcx,DWORD PTR [r14]",
						"49 63 0e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rcx,DWORD PTR [r15]",
						"49 63 0f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rcx,DWORD PTR [r8]",
						"49 63 08"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rcx,DWORD PTR [r9]",
						"49 63 09"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rcx,DWORD PTR [rax]",
						"48 63 08"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rcx,DWORD PTR [rbp+0x0]",
						"48 63 4d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rcx,DWORD PTR [rbx]",
						"48 63 0b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rcx,DWORD PTR [rcx]",
						"48 63 09"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rcx,DWORD PTR [rdi]",
						"48 63 0f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rcx,DWORD PTR [rdx]",
						"48 63 0a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rcx,DWORD PTR [rsi]",
						"48 63 0e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RCX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rcx,DWORD PTR [rsp]",
						"48 63 0c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rdi,DWORD PTR [r10]",
						"49 63 3a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rdi,DWORD PTR [r11]",
						"49 63 3b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rdi,DWORD PTR [r12]",
						"49 63 3c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rdi,DWORD PTR [r13+0x0]",
						"49 63 7d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rdi,DWORD PTR [r14]",
						"49 63 3e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rdi,DWORD PTR [r15]",
						"49 63 3f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rdi,DWORD PTR [r8]",
						"49 63 38"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rdi,DWORD PTR [r9]",
						"49 63 39"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rdi,DWORD PTR [rax]",
						"48 63 38"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rdi,DWORD PTR [rbp+0x0]",
						"48 63 7d 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rdi,DWORD PTR [rbx]",
						"48 63 3b"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rdi,DWORD PTR [rcx]",
						"48 63 39"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rdi,DWORD PTR [rdi]",
						"48 63 3f"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rdi,DWORD PTR [rdx]",
						"48 63 3a"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rdi,DWORD PTR [rsi]",
						"48 63 3e"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rdi,DWORD PTR [rsp]",
						"48 63 3c 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rdx,DWORD PTR [r10]",
						"49 63 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rdx,DWORD PTR [r11]",
						"49 63 13"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rdx,DWORD PTR [r12]",
						"49 63 14 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rdx,DWORD PTR [r13+0x0]",
						"49 63 55 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rdx,DWORD PTR [r14]",
						"49 63 16"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rdx,DWORD PTR [r15]",
						"49 63 17"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rdx,DWORD PTR [r8]",
						"49 63 10"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rdx,DWORD PTR [r9]",
						"49 63 11"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rdx,DWORD PTR [rax]",
						"48 63 10"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rdx,DWORD PTR [rbp+0x0]",
						"48 63 55 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rdx,DWORD PTR [rbx]",
						"48 63 13"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rdx,DWORD PTR [rcx]",
						"48 63 11"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rdx,DWORD PTR [rdi]",
						"48 63 17"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rdx,DWORD PTR [rdx]",
						"48 63 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rdx,DWORD PTR [rsi]",
						"48 63 16"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rdx,DWORD PTR [rsp]",
						"48 63 14 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rsi,DWORD PTR [r10]",
						"49 63 32"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rsi,DWORD PTR [r11]",
						"49 63 33"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rsi,DWORD PTR [r12]",
						"49 63 34 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rsi,DWORD PTR [r13+0x0]",
						"49 63 75 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rsi,DWORD PTR [r14]",
						"49 63 36"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rsi,DWORD PTR [r15]",
						"49 63 37"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rsi,DWORD PTR [r8]",
						"49 63 30"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rsi,DWORD PTR [r9]",
						"49 63 31"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rsi,DWORD PTR [rax]",
						"48 63 30"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rsi,DWORD PTR [rbp+0x0]",
						"48 63 75 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rsi,DWORD PTR [rbx]",
						"48 63 33"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rsi,DWORD PTR [rcx]",
						"48 63 31"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rsi,DWORD PTR [rdi]",
						"48 63 37"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rsi,DWORD PTR [rdx]",
						"48 63 32"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rsi,DWORD PTR [rsi]",
						"48 63 36"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rsi,DWORD PTR [rsp]",
						"48 63 34 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"movsxd rsp,DWORD PTR [r10]",
						"49 63 22"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"movsxd rsp,DWORD PTR [r11]",
						"49 63 23"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"movsxd rsp,DWORD PTR [r12]",
						"49 63 24 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.displacement((byte) 0)
										.build()),
						"movsxd rsp,DWORD PTR [r13+0x0]",
						"49 63 65 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.build()),
						"movsxd rsp,DWORD PTR [r14]",
						"49 63 26"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build()),
						"movsxd rsp,DWORD PTR [r15]",
						"49 63 27"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.build()),
						"movsxd rsp,DWORD PTR [r8]",
						"49 63 20"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.build()),
						"movsxd rsp,DWORD PTR [r9]",
						"49 63 21"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"movsxd rsp,DWORD PTR [rax]",
						"48 63 20"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"movsxd rsp,DWORD PTR [rbp+0x0]",
						"48 63 65 00"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"movsxd rsp,DWORD PTR [rbx]",
						"48 63 23"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build()),
						"movsxd rsp,DWORD PTR [rcx]",
						"48 63 21"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movsxd rsp,DWORD PTR [rdi]",
						"48 63 27"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.build()),
						"movsxd rsp,DWORD PTR [rdx]",
						"48 63 22"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build()),
						"movsxd rsp,DWORD PTR [rsi]",
						"48 63 26"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RSP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"movsxd rsp,DWORD PTR [rsp]",
						"48 63 24 24"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(R15)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsxd rdx,DWORD PTR [r11+r15*4+0x12345678]",
						"4b 63 94 bb 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVSXD,
								RDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(RDI)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsxd rdx,DWORD PTR [r11+rdi*4+0x12345678]",
						"49 63 94 bb 78 56 34 12"));
	}

	private static List<X64EncodingTestCase> cmp() {
		return List.of(
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build(),
								DH),
						"cmp BYTE PTR [eax],dh",
						"67 38 30"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EDI)
										.build(),
								new Immediate((byte) 0x77)),
						"cmp BYTE PTR [edi],0x77",
						"67 80 3f 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R13)
										.index(R12)
										.scale(2)
										.displacement((byte) 0x12)
										.build(),
								new Immediate((byte) 0x77)),
						"cmp BYTE PTR [r13+r12*2+0x12],0x77",
						"43 80 7c 65 12 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R13)
										.index(RCX)
										.scale(2)
										.displacement((byte) 0x12)
										.build(),
								new Immediate((byte) 0x77)),
						"cmp BYTE PTR [r13+rcx*2+0x12],0x77",
						"41 80 7c 4d 12 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"cmp BYTE PTR [r9+rcx*4+0x12345678],0x99",
						"41 80 bc 89 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9B),
						"cmp BYTE PTR [rbx+r9*4+0x12345678],r9b",
						"46 38 8c 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9B),
						"cmp BYTE PTR [rbx+rcx*4+0x12345678],r9b",
						"44 38 8c 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDI)
										.build(),
								new Immediate((byte) 0x77)),
						"cmp BYTE PTR [rdi],0x77",
						"80 3f 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EBP)
										.displacement(-0xe8)
										.build(),
								R15D),
						"cmp DWORD PTR [ebp-0xe8],r15d",
						"67 44 39 bd 18 ff ff ff"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EBP)
										.displacement((byte) -0x78)
										.build(),
								R15D),
						"cmp DWORD PTR [ebp-0x78],r15d",
						"67 44 39 7d 88"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13D)
										.displacement(-0xe8)
										.build(),
								R15D),
						"cmp DWORD PTR [r13d-0xe8],r15d",
						"67 45 39 bd 18 ff ff ff"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EDI)
										.build(),
								iimm),
						"cmp DWORD PTR [edi],0x12345678",
						"67 81 3f 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R13)
										.index(RCX)
										.scale(2)
										.displacement((byte) 0x12)
										.build(),
								new Immediate(0x66778899)),
						"cmp DWORD PTR [r13+rcx*2+0x12],0x66778899",
						"41 81 7c 4d 12 99 88 77 66"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0xdeadbeef)),
						"cmp DWORD PTR [r9+rcx*4+0x12345678],0xdeadbeef",
						"41 81 bc 89 78 56 34 12 ef be ad de"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement(0xffffff18)
										.build(),
								R15D),
						"cmp DWORD PTR [rbp-0xe8],r15d",
						"44 39 bd 18 ff ff ff"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build(),
								iimm),
						"cmp DWORD PTR [rdi],0x12345678",
						"81 3f 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EDI)
										.build(),
								new Immediate(0x12345678)),
						"cmp QWORD PTR [edi],0x12345678",
						"67 48 81 3f 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDI)
										.build(),
								new Immediate(0x12345678)),
						"cmp QWORD PTR [rdi],0x12345678",
						"48 81 3f 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EDI)
										.build(),
								new Immediate((short) 0x7788)),
						"cmp WORD PTR [edi],0x7788",
						"67 66 81 3f 88 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R13)
										.index(RCX)
										.scale(2)
										.displacement((byte) 0x12)
										.build(),
								new Immediate((byte) 0x77)),
						"cmp WORD PTR [r13+rcx*2+0x12],0x77",
						"66 41 83 7c 4d 12 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R13)
										.index(RCX)
										.scale(2)
										.displacement((byte) 0x12)
										.build(),
								new Immediate((short) 0x7788)),
						"cmp WORD PTR [r13+rcx*2+0x12],0x7788",
						"66 41 81 7c 4d 12 88 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((short) 0xbeef)),
						"cmp WORD PTR [r9+rcx*4+0x12345678],0xbeef",
						"66 41 81 bc 89 78 56 34 12 ef be"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RDI)
										.build(),
								new Immediate((short) 0x7788)),
						"cmp WORD PTR [rdi],0x7788",
						"66 81 3f 88 77"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RIP)
										.displacement(0xc6afd)
										.build(),
								RBX),
						"cmp QWORD PTR [rip+0xc6afd],rbx",
						"48 39 1d fd 6a 0c 00"),
				//
				test(
						new Instruction(
								Opcode.CMP,
								DH,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build()),
						"cmp dh,BYTE PTR [eax]",
						"67 3a 30"),
				test(
						new Instruction(
								Opcode.CMP,
								DH,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"cmp dh,BYTE PTR [rax]",
						"3a 30"),
				test(
						new Instruction(
								Opcode.CMP,
								DX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EAX)
										.build()),
						"cmp dx,WORD PTR [eax]",
						"67 66 3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								DX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.build()),
						"cmp dx,WORD PTR [rax]",
						"66 3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								EBP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmp ebp,DWORD PTR [rbx+r9*4+0x12345678]",
						"42 3b ac 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMP,
								EDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EAX)
										.build()),
						"cmp edx,DWORD PTR [eax]",
						"67 3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								EDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"cmp edx,DWORD PTR [rax]",
						"3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								RDX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EAX)
										.build()),
						"cmp rdx,QWORD PTR [eax]",
						"67 48 3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								RDX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"cmp rdx,QWORD PTR [rax]",
						"48 3b 10"),
				test(
						new Instruction(
								Opcode.CMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDI)
										.displacement((byte) -8)
										.build(),
								new Immediate((byte) 0)),
						"cmp QWORD PTR [rdi-0x8],0x00",
						"48 83 7f f8 00"),
				//
				test(new Instruction(Opcode.CMP, AL, new Immediate((byte) 0x99)), "cmp al,0x99", "3c 99"),
				test(new Instruction(Opcode.CMP, AL, DH), "cmp al,dh", "38 f0"),
				test(new Instruction(Opcode.CMP, CX, simm), "cmp cx,0x1234", "66 81 f9 34 12"),
				test(new Instruction(Opcode.CMP, DH, new Immediate((byte) 0x99)), "cmp dh,0x99", "80 fe 99"),
				test(new Instruction(Opcode.CMP, SI, bimm), "cmp si,0x12", "66 83 fe 12"),
				test(new Instruction(Opcode.CMP, EAX, bimm), "cmp eax,0x12", "83 f8 12"),
				test(new Instruction(Opcode.CMP, EAX, iimm), "cmp eax,0x12345678", "3d 78 56 34 12"),
				test(new Instruction(Opcode.CMP, EDI, iimm), "cmp edi,0x12345678", "81 ff 78 56 34 12"),
				test(new Instruction(Opcode.CMP, ESP, R13D), "cmp esp,r13d", "44 39 ec"),
				test(new Instruction(Opcode.CMP, R8B, bimm), "cmp r8b,0x12", "41 80 f8 12"),
				test(new Instruction(Opcode.CMP, R8W, DX), "cmp r8w,dx", "66 41 39 d0"),
				test(new Instruction(Opcode.CMP, RAX, bimm), "cmp rax,0x12", "48 83 f8 12"),
				test(
						new Instruction(Opcode.CMP, RAX, new Immediate(0x12345678)),
						"cmp rax,0x12345678",
						"48 3d 78 56 34 12"),
				test(
						new Instruction(Opcode.CMP, RDI, new Immediate(0x12345678)),
						"cmp rdi,0x12345678",
						"48 81 ff 78 56 34 12"),
				test(new Instruction(Opcode.CMP, RSP, R8), "cmp rsp,r8", "4c 39 c4"),
				test(new Instruction(Opcode.CMP, SP, R13W), "cmp sp,r13w", "66 44 39 ec"));
	}

	private static List<X64EncodingTestCase> call() {
		return List.of(
				//  The output of these instructions is different from what you can see from other tools such as objdump
				//  because here we keep the addition to the instruction pointer implicit.
				//  In reality, it would look like 'call rip+0x....'
				test(new Instruction(Opcode.CALL, iimm), "call 0x12345678", "e8 78 56 34 12"),
				test(new Instruction(Opcode.CALL, new Immediate(0xf8563412)), "call 0xf8563412", "e8 12 34 56 f8"),
				test(new Instruction(Opcode.CALL, new Immediate(0xffffff18)), "call 0xffffff18", "e8 18 ff ff ff"),
				//  the following ones are calls with registers (as offsets?)
				test(new Instruction(Opcode.CALL, R10), "call r10", "41 ff d2"),
				test(new Instruction(Opcode.CALL, R11), "call r11", "41 ff d3"),
				test(new Instruction(Opcode.CALL, R12), "call r12", "41 ff d4"),
				test(new Instruction(Opcode.CALL, R13), "call r13", "41 ff d5"),
				test(new Instruction(Opcode.CALL, R14), "call r14", "41 ff d6"),
				test(new Instruction(Opcode.CALL, R15), "call r15", "41 ff d7"),
				test(new Instruction(Opcode.CALL, R8), "call r8", "41 ff d0"),
				test(new Instruction(Opcode.CALL, R9), "call r9", "41 ff d1"),
				test(new Instruction(Opcode.CALL, RAX), "call rax", "ff d0"),
				test(new Instruction(Opcode.CALL, RBP), "call rbp", "ff d5"),
				test(new Instruction(Opcode.CALL, RBX), "call rbx", "ff d3"),
				test(new Instruction(Opcode.CALL, RCX), "call rcx", "ff d1"),
				test(new Instruction(Opcode.CALL, RDI), "call rdi", "ff d7"),
				test(new Instruction(Opcode.CALL, RDX), "call rdx", "ff d2"),
				test(new Instruction(Opcode.CALL, RSI), "call rsi", "ff d6"),
				test(new Instruction(Opcode.CALL, RSP), "call rsp", "ff d4"),
				//
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EBX)
										.build()),
						"call DWORD PTR [ebx]",
						"67 66 ff 1b"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"call DWORD PTR [r11+r12*4+0x12345678]",
						"66 43 ff 9c a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11D)
										.index(R12D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"call DWORD PTR [r11d+r12d*4+0x12345678]",
						"67 66 43 ff 9c a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"call DWORD PTR [rsp]",
						"66 ff 1c 24"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(ESP)
										.build()),
						"call DWORD PTR [esp]",
						"67 66 ff 1c 24"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12)
										.build()),
						"call DWORD PTR [r12]",
						"66 41 ff 1c 24"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R12D)
										.build()),
						"call DWORD PTR [r12d]",
						"67 66 41 ff 1c 24"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement((byte) 0)
										.build()),
						"call DWORD PTR [rbp+0x0]",
						"66 ff 5d 00"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EAX)
										.build()),
						"call QWORD PTR [eax]",
						"67 ff 10"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"call QWORD PTR [r11+r12*4+0x12345678]",
						"43 ff 94 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RIP)
										.displacement(0x23393)
										.build()),
						"call QWORD PTR [rip+0x23393]",
						"ff 15 93 33 02 00"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDX)
										.build()),
						"call QWORD PTR [rdx]",
						"ff 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(ECX)
										.build()),
						"call WORD PTR [ecx]",
						"67 66 ff 11"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"call WORD PTR [r11+r12*4+0x12345678]",
						"66 43 ff 94 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11D)
										.index(R12D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"call WORD PTR [r11d+r12d*4+0x12345678]",
						"67 66 43 ff 94 a3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CALL,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSI)
										.build()),
						"call WORD PTR [rsi]",
						"66 ff 16"));
	}

	private static List<X64EncodingTestCase> jump() {
		return List.of(
				//  The output of these instructions is different from what you can see from other tools such as objdump
				//  because here we keep the addition to the instruction pointer implicit.
				//  In reality, it would look like 'jXX rip+0x....'
				//  Ja
				test(new Instruction(Opcode.JA, bimm), "ja 0x12", "77 12"),
				test(new Instruction(Opcode.JA, iimm), "ja 0x12345678", "0f 87 78 56 34 12"),
				//  Jae
				test(new Instruction(Opcode.JAE, bimm), "jae 0x12", "73 12"),
				test(new Instruction(Opcode.JAE, iimm), "jae 0x12345678", "0f 83 78 56 34 12"),
				//  Jb
				test(new Instruction(Opcode.JB, bimm), "jb 0x12", "72 12"),
				test(new Instruction(Opcode.JB, iimm), "jb 0x12345678", "0f 82 78 56 34 12"),
				//  Jbe
				test(new Instruction(Opcode.JBE, bimm), "jbe 0x12", "76 12"),
				test(new Instruction(Opcode.JBE, iimm), "jbe 0x12345678", "0f 86 78 56 34 12"),
				//  Jg
				test(new Instruction(Opcode.JG, bimm), "jg 0x12", "7f 12"),
				test(new Instruction(Opcode.JG, iimm), "jg 0x12345678", "0f 8f 78 56 34 12"),
				//  Je
				test(new Instruction(Opcode.JE, bimm), "je 0x12", "74 12"),
				test(new Instruction(Opcode.JE, iimm), "je 0x12345678", "0f 84 78 56 34 12"),
				//  Jl
				test(new Instruction(Opcode.JL, bimm), "jl 0x12", "7c 12"),
				test(new Instruction(Opcode.JL, iimm), "jl 0x12345678", "0f 8c 78 56 34 12"),
				//  Jle
				test(new Instruction(Opcode.JLE, bimm), "jle 0x12", "7e 12"),
				test(new Instruction(Opcode.JLE, iimm), "jle 0x12345678", "0f 8e 78 56 34 12"),
				//  Jge
				test(new Instruction(Opcode.JGE, bimm), "jge 0x12", "7d 12"),
				test(new Instruction(Opcode.JGE, iimm), "jge 0x12345678", "0f 8d 78 56 34 12"),
				//  Jne
				test(new Instruction(Opcode.JNE, bimm), "jne 0x12", "75 12"),
				test(new Instruction(Opcode.JNE, iimm), "jne 0x12345678", "0f 85 78 56 34 12"),
				//  Jns
				test(new Instruction(Opcode.JNS, bimm), "jns 0x12", "79 12"),
				test(new Instruction(Opcode.JNS, iimm), "jns 0x12345678", "0f 89 78 56 34 12"),
				//  Js
				test(new Instruction(Opcode.JS, bimm), "js 0x12", "78 12"),
				test(new Instruction(Opcode.JS, iimm), "js 0x12345678", "0f 88 78 56 34 12"),
				//  Jp
				test(new Instruction(Opcode.JP, bimm), "jp 0x12", "7a 12"),
				test(new Instruction(Opcode.JP, iimm), "jp 0x12345678", "0f 8a 78 56 34 12"),
				//  Jmp
				test(new Instruction(Opcode.JMP, bimm), "jmp 0x12", "eb 12"),
				test(new Instruction(Opcode.JMP, iimm), "jmp 0x12345678", "e9 78 56 34 12"),
				//
				test(new Instruction(Opcode.JMP, AX), "jmp ax", "66 ff e0"),
				test(new Instruction(Opcode.JMP, R11), "jmp r11", "41 ff e3"),
				test(new Instruction(Opcode.JMP, R11W), "jmp r11w", "66 41 ff e3"),
				test(new Instruction(Opcode.JMP, RAX), "jmp rax", "ff e0"),
				//
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.build()),
						"jmp DWORD PTR [r11]",
						"66 41 ff 2b"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11D)
										.build()),
						"jmp DWORD PTR [r11d]",
						"67 66 41 ff 2b"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11)
										.build()),
						"jmp QWORD PTR [r11]",
						"41 ff 23"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11D)
										.build()),
						"jmp QWORD PTR [r11d]",
						"67 41 ff 23"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.build()),
						"jmp WORD PTR [r11]",
						"66 41 ff 23"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11D)
										.build()),
						"jmp WORD PTR [r11d]",
						"67 66 41 ff 23"),
				//
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EAX)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp DWORD PTR [eax+ecx*4+0x12345678]",
						"67 66 ff ac 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp DWORD PTR [rax+rcx*4+0x12345678]",
						"66 ff ac 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EAX)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp QWORD PTR [eax+ecx*4+0x12345678]",
						"67 ff a4 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp QWORD PTR [rax+rcx*4+0x12345678]",
						"ff a4 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EAX)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp WORD PTR [eax+ecx*4+0x12345678]",
						"67 66 ff a4 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.JMP,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"jmp WORD PTR [rax+rcx*4+0x12345678]",
						"66 ff a4 88 78 56 34 12"));
	}

	private static List<X64EncodingTestCase> cmov() {
		return List.of(
				//  Cmove
				test(
						new Instruction(
								Opcode.CMOVE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmove ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 44 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVE,
								RDX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement((byte) 8)
										.build()),
						"cmove rdx,QWORD PTR [rsp+0x8]",
						"48 0f 44 54 24 08"),
				test(new Instruction(Opcode.CMOVE, R15, RCX), "cmove r15,rcx", "4c 0f 44 f9"),
				test(new Instruction(Opcode.CMOVE, RCX, R15), "cmove rcx,r15", "49 0f 44 cf"),
				test(new Instruction(Opcode.CMOVE, EDI, ESI), "cmove edi,esi", "0f 44 fe"),
				//  Cmovns
				test(
						new Instruction(
								Opcode.CMOVNS,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovns ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 49 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVNS, R15, RCX), "cmovns r15,rcx", "4c 0f 49 f9"),
				test(new Instruction(Opcode.CMOVNS, RCX, R15), "cmovns rcx,r15", "49 0f 49 cf"),
				test(new Instruction(Opcode.CMOVNS, EBX, EBP), "cmovns ebx,ebp", "0f 49 dd"),
				//  Cmovae
				test(
						new Instruction(
								Opcode.CMOVAE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovae ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 43 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVAE, R15, RCX), "cmovae r15,rcx", "4c 0f 43 f9"),
				test(new Instruction(Opcode.CMOVAE, RCX, R15), "cmovae rcx,r15", "49 0f 43 cf"),
				test(new Instruction(Opcode.CMOVAE, ESI, R9D), "cmovae esi,r9d", "41 0f 43 f1"),
				//  Cmovb
				test(
						new Instruction(
								Opcode.CMOVB,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovb ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 42 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVB,
								RAX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement((byte) 0x28)
										.build()),
						"cmovb rax,QWORD PTR [rsp+0x28]",
						"48 0f 42 44 24 28"),
				test(new Instruction(Opcode.CMOVB, R15, RCX), "cmovb r15,rcx", "4c 0f 42 f9"),
				test(new Instruction(Opcode.CMOVB, RCX, R15), "cmovb rcx,r15", "49 0f 42 cf"),
				test(new Instruction(Opcode.CMOVB, R11D, R9D), "cmovb r11d,r9d", "45 0f 42 d9"),
				//  Cmovbe
				test(
						new Instruction(
								Opcode.CMOVBE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovbe ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 46 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVBE, R15, RCX), "cmovbe r15,rcx", "4c 0f 46 f9"),
				test(new Instruction(Opcode.CMOVBE, RCX, R15), "cmovbe rcx,r15", "49 0f 46 cf"),
				test(new Instruction(Opcode.CMOVBE, R12D, EAX), "cmovbe r12d,eax", "44 0f 46 e0"),
				//  Cmovne
				test(
						new Instruction(
								Opcode.CMOVNE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovne ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 45 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVNE,
								RSI,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.displacement((byte) 0x16)
										.build()),
						"cmovne rsi,QWORD PTR [rbx+0x16]",
						"48 0f 45 73 16"),
				test(new Instruction(Opcode.CMOVNE, R15, RDX), "cmovne r15,rdx", "4c 0f 45 fa"),
				test(new Instruction(Opcode.CMOVNE, RDX, R15), "cmovne rdx,r15", "49 0f 45 d7"),
				test(new Instruction(Opcode.CMOVNE, ESI, EDX), "cmovne esi,edx", "0f 45 f2"),
				//  Cmovg
				test(
						new Instruction(
								Opcode.CMOVG,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovg ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 4f 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVG,
								R9,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement((byte) 0x64)
										.build()),
						"cmovg r9,QWORD PTR [rsp+0x64]",
						"4c 0f 4f 4c 24 64"),
				test(new Instruction(Opcode.CMOVG, R15, RDX), "cmovg r15,rdx", "4c 0f 4f fa"),
				test(new Instruction(Opcode.CMOVG, RDX, R15), "cmovg rdx,r15", "49 0f 4f d7"),
				test(new Instruction(Opcode.CMOVG, EBX, EDX), "cmovg ebx,edx", "0f 4f da"),
				//  Cmovge
				test(
						new Instruction(
								Opcode.CMOVGE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovge ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 4d 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVGE, R15, RDX), "cmovge r15,rdx", "4c 0f 4d fa"),
				test(new Instruction(Opcode.CMOVGE, RDX, R15), "cmovge rdx,r15", "49 0f 4d d7"),
				test(new Instruction(Opcode.CMOVGE, EBX, EBP), "cmovge ebx,ebp", "0f 4d dd"),
				//  Cmovs
				test(
						new Instruction(
								Opcode.CMOVS,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovs ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 48 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVS,
								RBX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement((byte) 0x16)
										.build()),
						"cmovs rbx,QWORD PTR [rbp+0x16]",
						"48 0f 48 5d 16"),
				test(new Instruction(Opcode.CMOVS, ECX, EAX), "cmovs ecx,eax", "0f 48 c8"),
				test(new Instruction(Opcode.CMOVS, EDX, R9D), "cmovs edx,r9d", "41 0f 48 d1"),
				//  Cmova
				test(
						new Instruction(
								Opcode.CMOVA,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmova ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 47 8c 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMOVA,
								R15,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement(0x160)
										.build()),
						"cmova r15,QWORD PTR [rbp+0x160]",
						"4c 0f 47 bd 60 01 00 00"),
				test(new Instruction(Opcode.CMOVA, ECX, EAX), "cmova ecx,eax", "0f 47 c8"),
				test(new Instruction(Opcode.CMOVA, EDX, R9D), "cmova edx,r9d", "41 0f 47 d1"),
				//  Cmovl
				test(
						new Instruction(
								Opcode.CMOVL,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovl ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 4c 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVL, R15, RDX), "cmovl r15,rdx", "4c 0f 4c fa"),
				test(new Instruction(Opcode.CMOVL, RDX, R15), "cmovl rdx,r15", "49 0f 4c d7"),
				test(new Instruction(Opcode.CMOVL, EAX, EDI), "cmovl eax,edi", "0f 4c c7"),
				//  Cmovle
				test(
						new Instruction(
								Opcode.CMOVLE,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"cmovle ecx,DWORD PTR [r8+rax*4+0x12345678]",
						"41 0f 4e 8c 80 78 56 34 12"),
				test(new Instruction(Opcode.CMOVLE, R15, RDX), "cmovle r15,rdx", "4c 0f 4e fa"),
				test(new Instruction(Opcode.CMOVLE, RDX, R15), "cmovle rdx,r15", "49 0f 4e d7"),
				test(new Instruction(Opcode.CMOVLE, EAX, ECX), "cmovle eax,ecx", "0f 4e c1"));
	}

	private static List<X64EncodingTestCase> lea() {
		return List.of(
				test(
						new Instruction(
								Opcode.LEA,
								AX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EBX)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea ax,[ebx+ecx*4+0x12345678]",
						"67 66 8d 84 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								CX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RBX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea cx,[rbx+rcx*4+0x12345678]",
						"66 8d 8c 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EBX)
										.build()),
						"lea eax,[ebx]",
						"67 8d 03"),
				test(
						new Instruction(
								Opcode.LEA,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.build()),
						"lea eax,[rbx]",
						"8d 03"),
				test(
						new Instruction(
								Opcode.LEA,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.index(RBP)
										.scale(2)
										.displacement((byte) 0)
										.build()),
						"lea ecx,[rdx+rbp*2+0x0]",
						"8d 4c 6a 00"),
				test(
						new Instruction(
								Opcode.LEA,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.index(RBP)
										.scale(2)
										.build()),
						"lea ecx,[rdx+rbp*2]",
						"8d 0c 6a"),
				test(
						new Instruction(
								Opcode.LEA,
								ESI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EDI)
										.index(R12D)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"lea esi,[edi+r12d*2+0x12345678]",
						"67 42 8d b4 67 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								R10W,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(EBX)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea r10w,[ebx+ecx*4+0x12345678]",
						"67 66 44 8d 94 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								R13D,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea r13d,[rdi+r8*4+0x12345678]",
						"46 8d ac 87 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								R14W,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RBX)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea r14w,[rbx+rcx*4+0x12345678]",
						"66 44 8d b4 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								R9D,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EDX)
										.index(EBP)
										.scale(2)
										.build()),
						"lea r9d,[edx+ebp*2]",
						"67 44 8d 0c 6a"),
				test(
						new Instruction(
								Opcode.LEA,
								R9D,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EDX)
										.index(EBP)
										.scale(2)
										.displacement((byte) 0)
										.build()),
						"lea r9d,[edx+ebp*2+0x0]",
						"67 44 8d 4c 6a 00"),
				test(
						new Instruction(
								Opcode.LEA,
								RAX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EBX)
										.build()),
						"lea rax,[ebx]",
						"67 48 8d 03"),
				test(
						new Instruction(
								Opcode.LEA,
								RAX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.build()),
						"lea rax,[rbx]",
						"48 8d 03"),
				test(
						new Instruction(
								Opcode.LEA,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EDX)
										.index(EBP)
										.scale(2)
										.build()),
						"lea rcx,[edx+ebp*2]",
						"67 48 8d 0c 6a"),
				test(
						new Instruction(
								Opcode.LEA,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EDX)
										.index(EBP)
										.scale(2)
										.displacement((byte) 0)
										.build()),
						"lea rcx,[edx+ebp*2+0x0]",
						"67 48 8d 4c 6a 00"),
				test(
						new Instruction(
								Opcode.LEA,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDX)
										.index(RBP)
										.scale(2)
										.build()),
						"lea rcx,[rdx+rbp*2]",
						"48 8d 0c 6a"),
				test(
						new Instruction(
								Opcode.LEA,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDX)
										.index(RBP)
										.scale(2)
										.displacement((byte) 0)
										.build()),
						"lea rcx,[rdx+rbp*2+0x0]",
						"48 8d 4c 6a 00"),
				test(
						new Instruction(
								Opcode.LEA,
								RSI,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EDI)
										.index(R9D)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"lea rsi,[edi+r9d*2+0x12345678]",
						"67 4a 8d b4 4f 78 56 34 12"),
				test(
						new Instruction(
								Opcode.LEA,
								RSI,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDI)
										.index(R8)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"lea rsi,[rdi+r8*4+0x12345678]",
						"4a 8d b4 87 78 56 34 12"));
	}

	private static List<X64EncodingTestCase> movzx() {
		return List.of(
				test(new Instruction(Opcode.MOVZX, ESI, BL), "movzx esi,bl", "0f b6 f3"),
				test(new Instruction(Opcode.MOVZX, R12D, R10W), "movzx r12d,r10w", "45 0f b7 e2"),
				test(
						new Instruction(
								Opcode.MOVZX,
								R9D,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RCX)
										.build()),
						"movzx r9d,BYTE PTR [rcx]",
						"44 0f b6 09"),
				test(
						new Instruction(
								Opcode.MOVZX,
								R9D,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movzx r9d,BYTE PTR [rdx+rax*4+0x12345678]",
						"44 0f b6 8c 82 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVZX,
								R9D,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RCX)
										.build()),
						"movzx r9d,WORD PTR [rcx]",
						"44 0f b7 09"),
				test(
						new Instruction(
								Opcode.MOVZX,
								R9D,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RDX)
										.index(RAX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movzx r9d,WORD PTR [rdx+rax*4+0x12345678]",
						"44 0f b7 8c 82 78 56 34 12"),
				test(new Instruction(Opcode.MOVZX, RSI, BL), "movzx rsi,bl", "48 0f b6 f3"),
				test(new Instruction(Opcode.MOVZX, RSI, DI), "movzx rsi,di", "48 0f b7 f7"));
	}

	private static List<X64EncodingTestCase> movsx() {
		return List.of(
				test(
						new Instruction(
								Opcode.MOVSX,
								EDI,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsx edi,BYTE PTR [rax+rbx*4+0x12345678]",
						"0f be bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVSX,
								EDI,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsx edi,WORD PTR [rax+rbx*4+0x12345678]",
						"0f bf bc 98 78 56 34 12"),
				test(new Instruction(Opcode.MOVSX, ESI, BL), "movsx esi,bl", "0f be f3"),
				test(
						new Instruction(
								Opcode.MOVSX,
								RDI,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsx rdi,BYTE PTR [rax+rbx*4+0x12345678]",
						"48 0f be bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVSX,
								RDI,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movsx rdi,WORD PTR [rax+rbx*4+0x12345678]",
						"48 0f bf bc 98 78 56 34 12"),
				test(new Instruction(Opcode.MOVSX, RSI, BL), "movsx rsi,bl", "48 0f be f3"),
				test(new Instruction(Opcode.MOVSX, RSI, DI), "movsx rsi,di", "48 0f bf f7"));
	}

	private static List<X64EncodingTestCase> push() {
		return List.of(
				test(new Instruction(Opcode.PUSH, iimm), "push 0x12345678", "68 78 56 34 12"),
				test(new Instruction(Opcode.PUSH, AX), "push ax", "66 50"),
				test(new Instruction(Opcode.PUSH, BP), "push bp", "66 55"),
				test(new Instruction(Opcode.PUSH, BX), "push bx", "66 53"),
				test(new Instruction(Opcode.PUSH, CX), "push cx", "66 51"),
				test(new Instruction(Opcode.PUSH, DI), "push di", "66 57"),
				test(new Instruction(Opcode.PUSH, DX), "push dx", "66 52"),
				test(new Instruction(Opcode.PUSH, R10), "push r10", "41 52"),
				test(new Instruction(Opcode.PUSH, R10W), "push r10w", "66 41 52"),
				test(new Instruction(Opcode.PUSH, R11), "push r11", "41 53"),
				test(new Instruction(Opcode.PUSH, R11W), "push r11w", "66 41 53"),
				test(new Instruction(Opcode.PUSH, R12), "push r12", "41 54"),
				test(new Instruction(Opcode.PUSH, R12W), "push r12w", "66 41 54"),
				test(new Instruction(Opcode.PUSH, R13), "push r13", "41 55"),
				test(new Instruction(Opcode.PUSH, R13W), "push r13w", "66 41 55"),
				test(new Instruction(Opcode.PUSH, R14), "push r14", "41 56"),
				test(new Instruction(Opcode.PUSH, R14W), "push r14w", "66 41 56"),
				test(new Instruction(Opcode.PUSH, R15), "push r15", "41 57"),
				test(new Instruction(Opcode.PUSH, R15W), "push r15w", "66 41 57"),
				test(new Instruction(Opcode.PUSH, R8), "push r8", "41 50"),
				test(new Instruction(Opcode.PUSH, R8W), "push r8w", "66 41 50"),
				test(new Instruction(Opcode.PUSH, R9), "push r9", "41 51"),
				test(new Instruction(Opcode.PUSH, R9W), "push r9w", "66 41 51"),
				test(new Instruction(Opcode.PUSH, RAX), "push rax", "50"),
				test(new Instruction(Opcode.PUSH, RBP), "push rbp", "55"),
				test(new Instruction(Opcode.PUSH, RBX), "push rbx", "53"),
				test(new Instruction(Opcode.PUSH, RCX), "push rcx", "51"),
				test(new Instruction(Opcode.PUSH, RDI), "push rdi", "57"),
				test(new Instruction(Opcode.PUSH, RDX), "push rdx", "52"),
				test(new Instruction(Opcode.PUSH, RSI), "push rsi", "56"),
				test(new Instruction(Opcode.PUSH, RSP), "push rsp", "54"),
				test(new Instruction(Opcode.PUSH, SI), "push si", "66 56"),
				test(new Instruction(Opcode.PUSH, SP), "push sp", "66 54"),
				//
				test(new Instruction(Opcode.PUSH, bimm), "push 0x12", "6a 12"),
				test(
						new Instruction(
								Opcode.PUSH,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EDX)
										.build()),
						"push QWORD PTR [edx]",
						"67 ff 32"),
				test(
						new Instruction(
								Opcode.PUSH,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11)
										.index(RSI)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"push QWORD PTR [r11+rsi*8+0x12345678]",
						"41 ff b4 f3 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PUSH,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11D)
										.index(EDI)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"push QWORD PTR [r11d+edi*8+0x12345678]",
						"67 41 ff b4 fb 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PUSH,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RDX)
										.build()),
						"push QWORD PTR [rdx]",
						"ff 32"));
	}

	private static List<X64EncodingTestCase> pop() {
		return List.of(
				test(new Instruction(Opcode.POP, AX), "pop ax", "66 58"),
				test(new Instruction(Opcode.POP, BP), "pop bp", "66 5d"),
				test(new Instruction(Opcode.POP, BX), "pop bx", "66 5b"),
				test(new Instruction(Opcode.POP, CX), "pop cx", "66 59"),
				test(new Instruction(Opcode.POP, DI), "pop di", "66 5f"),
				test(new Instruction(Opcode.POP, DX), "pop dx", "66 5a"),
				test(new Instruction(Opcode.POP, R10), "pop r10", "41 5a"),
				test(new Instruction(Opcode.POP, R10W), "pop r10w", "66 41 5a"),
				test(new Instruction(Opcode.POP, R11), "pop r11", "41 5b"),
				test(new Instruction(Opcode.POP, R11W), "pop r11w", "66 41 5b"),
				test(new Instruction(Opcode.POP, R12), "pop r12", "41 5c"),
				test(new Instruction(Opcode.POP, R12W), "pop r12w", "66 41 5c"),
				test(new Instruction(Opcode.POP, R13), "pop r13", "41 5d"),
				test(new Instruction(Opcode.POP, R13W), "pop r13w", "66 41 5d"),
				test(new Instruction(Opcode.POP, R14), "pop r14", "41 5e"),
				test(new Instruction(Opcode.POP, R14W), "pop r14w", "66 41 5e"),
				test(new Instruction(Opcode.POP, R15), "pop r15", "41 5f"),
				test(new Instruction(Opcode.POP, R15W), "pop r15w", "66 41 5f"),
				test(new Instruction(Opcode.POP, R8), "pop r8", "41 58"),
				test(new Instruction(Opcode.POP, R8W), "pop r8w", "66 41 58"),
				test(new Instruction(Opcode.POP, R9), "pop r9", "41 59"),
				test(new Instruction(Opcode.POP, R9W), "pop r9w", "66 41 59"),
				test(new Instruction(Opcode.POP, RAX), "pop rax", "58"),
				test(new Instruction(Opcode.POP, RBP), "pop rbp", "5d"),
				test(new Instruction(Opcode.POP, RBX), "pop rbx", "5b"),
				test(new Instruction(Opcode.POP, RCX), "pop rcx", "59"),
				test(new Instruction(Opcode.POP, RDI), "pop rdi", "5f"),
				test(new Instruction(Opcode.POP, RDX), "pop rdx", "5a"),
				test(new Instruction(Opcode.POP, RSI), "pop rsi", "5e"),
				test(new Instruction(Opcode.POP, RSP), "pop rsp", "5c"),
				test(new Instruction(Opcode.POP, SI), "pop si", "66 5e"),
				test(new Instruction(Opcode.POP, SP), "pop sp", "66 5c"));
	}

	private static List<X64EncodingTestCase> others() {
		return List.of(
				//  Cdq
				test(new Instruction(Opcode.CDQ), "cdq", "99"),
				//  Cwde
				test(new Instruction(Opcode.CWDE), "cwde", "98"),
				//  Cdqe
				test(new Instruction(Opcode.CDQE), "cdqe", "48 98"),
				//  Leave
				test(new Instruction(Opcode.LEAVE), "leave", "c9"),
				// Int3
				test(new Instruction(Opcode.INT3), "int3", "cc"),
				//  Ret
				test(new Instruction(Opcode.RET), "ret", "c3"),
				//  Cpuid
				test(new Instruction(Opcode.CPUID), "cpuid", "0f a2"),
				//  Hlt
				test(new Instruction(Opcode.HLT), "hlt", "f4"),
				//  Add
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EAX)
										.index(EBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8D),
						"add DWORD PTR [eax+ebx*4+0x12345678],r8d",
						"67 44 01 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8D),
						"add DWORD PTR [rax+rbx*4+0x12345678],r8d",
						"44 01 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9),
						"add QWORD PTR [rax+rbx*4+0x12345678],r9",
						"4c 01 8c 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RSP),
						"add QWORD PTR [rax+rbx*4+0x12345678],rsp",
						"48 01 a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.index(RBP)
										.scale(4)
										.displacement(0x7eadbeef)
										.build(),
								bimm),
						"add QWORD PTR [rsp+rbp*4+0x7eadbeef],0x12",
						"48 83 84 ac ef be ad 7e 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.index(RBP)
										.scale(4)
										.displacement(0x7eadbeef)
										.build(),
								iimm),
						"add QWORD PTR [rsp+rbp*4+0x7eadbeef],0x12345678",
						"48 81 84 ac ef be ad 7e 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8W),
						"add WORD PTR [rax+rbx*4+0x12345678],r8w",
						"66 44 01 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build(),
								AL),
						"add BYTE PTR [rax],al",
						"00 00"),
				test(new Instruction(Opcode.ADD, AL, new Immediate((byte) 0x99)), "add al,0x99", "04 99"),
				test(new Instruction(Opcode.ADD, AX, simm), "add ax,0x1234", "66 05 34 12"),
				test(new Instruction(Opcode.ADD, AX, bimm), "add ax,0x12", "66 83 c0 12"),
				test(new Instruction(Opcode.ADD, CX, simm), "add cx,0x1234", "66 81 c1 34 12"),
				test(new Instruction(Opcode.ADD, EAX, bimm), "add eax,0x12", "83 c0 12"),
				test(new Instruction(Opcode.ADD, EAX, iimm), "add eax,0x12345678", "05 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								ESP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"add esp,DWORD PTR [rax+rbx*4+0x12345678]",
						"03 a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								R11D,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"add r11d,DWORD PTR [rax+rbx*4+0x12345678]",
						"44 03 9c 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								EBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"add ebx,DWORD PTR [r8+rbx*4+0x12345678]",
						"41 03 9c 98 78 56 34 12"),
				test(new Instruction(Opcode.ADD, R8, new Immediate((byte) 1)), "add r8,0x01", "49 83 c0 01"),
				test(new Instruction(Opcode.ADD, R8, R9), "add r8,r9", "4d 01 c8"),
				test(new Instruction(Opcode.ADD, R9, bimm), "add r9,0x12", "49 83 c1 12"),
				test(
						new Instruction(
								Opcode.ADD,
								R9,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"add r9,QWORD PTR [rax+rbx*4+0x12345678]",
						"4c 03 8c 98 78 56 34 12"),
				test(new Instruction(Opcode.ADD, RAX, new Immediate((byte) 1)), "add rax,0x01", "48 83 c0 01"),
				test(new Instruction(Opcode.ADD, RAX, bimm), "add rax,0x12", "48 83 c0 12"),
				test(new Instruction(Opcode.ADD, RAX, iimm), "add rax,0x12345678", "48 05 78 56 34 12"),
				test(new Instruction(Opcode.ADD, RSP, iimm), "add rsp,0x12345678", "48 81 c4 78 56 34 12"),
				test(
						new Instruction(
								Opcode.ADD,
								RSP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"add rsp,QWORD PTR [rax+rbx*4+0x12345678]",
						"48 03 a4 98 78 56 34 12"),
				//  Adc
				test(new Instruction(Opcode.ADC, CX, simm), "adc cx,0x1234", "66 81 d1 34 12"),
				test(new Instruction(Opcode.ADC, RAX, new Immediate((byte) 0)), "adc rax,0x00", "48 83 d0 00"),
				test(new Instruction(Opcode.ADC, ECX, new Immediate((byte) 0xff)), "adc ecx,0xff", "83 d1 ff"),
				test(
						new Instruction(
								Opcode.ADC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.displacement(0x3c9df09a)
										.build(),
								CL),
						"adc BYTE PTR [rax+0x3c9df09a],cl",
						"10 88 9a f0 9d 3c"),
				test(
						new Instruction(
								Opcode.ADC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement(0x2FF0E4B1)
										.build(),
								ESI),
						"adc DWORD PTR [rbp+0x2ff0e4b1],esi",
						"11 b5 b1 e4 f0 2f"),
				//  And
				test(new Instruction(Opcode.AND, AL, bimm), "and al,0x12", "24 12"),
				test(
						new Instruction(
								Opcode.AND,
								AL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"and al,BYTE PTR [rax+rbx*4+0x12345678]",
						"22 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.AND,
								AX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"and ax,WORD PTR [rax+rbx*4+0x12345678]",
						"66 23 84 98 78 56 34 12"),
				test(new Instruction(Opcode.AND, CX, simm), "and cx,0x1234", "66 81 e1 34 12"),
				test(new Instruction(Opcode.AND, DI, new Immediate((short) 0x00f0)), "and di,0x00f0", "66 81 e7 f0 00"),
				test(new Instruction(Opcode.AND, DI, new Immediate((byte) 0xf0)), "and di,0xf0", "66 83 e7 f0"),
				test(
						new Instruction(
								Opcode.AND,
								DX,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R10)
										.build()),
						"and dx,WORD PTR [r10]",
						"66 41 23 12"),
				test(new Instruction(Opcode.AND, EAX, bimm), "and eax,0x12", "83 e0 12"),
				test(new Instruction(Opcode.AND, EAX, iimm), "and eax,0x12345678", "25 78 56 34 12"),
				test(
						new Instruction(
								Opcode.AND,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"and eax,DWORD PTR [rax+rbx*4+0x12345678]",
						"23 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.AND,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"and ecx,DWORD PTR [r10]",
						"41 23 0a"),
				test(
						new Instruction(
								Opcode.AND,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc93cb)
										.build(),
								new Immediate(0xffefffff)),
						"and DWORD PTR [rip+0xc93cb],0xffefffff",
						"81 25 cb 93 0c 00 ff ff ef ff"),
				test(
						new Instruction(
								Opcode.AND,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement((byte) 0x38)
										.build(),
								new Immediate(0xffffff07)),
						"and QWORD PTR [rbp+0x38],0xffffff07",
						"48 81 65 38 07 ff ff ff"),
				test(
						new Instruction(
								Opcode.AND,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RIP)
										.displacement(0xc9305)
										.build(),
								new Immediate((byte) 0xe8)),
						"and BYTE PTR [rip+0xc9305],0xe8",
						"80 25 05 93 0c 00 e8"),
				test(
						new Instruction(
								Opcode.AND,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R14)
										.displacement((byte) 0x8)
										.build(),
								bimm),
						"and QWORD PTR [r14+0x8],0x12",
						"49 83 66 08 12"),
				test(
						new Instruction(
								Opcode.AND,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc898a)
										.build(),
								EAX),
						"and DWORD PTR [rip+0xc898a],eax",
						"21 05 8a 89 0c 00"),
				test(new Instruction(Opcode.AND, EDI, bimm), "and edi,0x12", "83 e7 12"),
				test(new Instruction(Opcode.AND, EDI, iimm), "and edi,0x12345678", "81 e7 78 56 34 12"),
				test(new Instruction(Opcode.AND, EDI, new Immediate((byte) 0xf0)), "and edi,0xf0", "83 e7 f0"),
				test(new Instruction(Opcode.AND, R12, R13), "and r12,r13", "4d 21 ec"),
				test(new Instruction(Opcode.AND, R15D, new Immediate((byte) 0x1f)), "and r15d,0x1f", "41 83 e7 1f"),
				test(new Instruction(Opcode.AND, RAX, bimm), "and rax,0x12", "48 83 e0 12"),
				test(new Instruction(Opcode.AND, RAX, iimm), "and rax,0x12345678", "48 25 78 56 34 12"),
				test(
						new Instruction(
								Opcode.AND,
								RAX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"and rax,QWORD PTR [rax+rbx*4+0x12345678]",
						"48 23 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.AND,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R10)
										.build()),
						"and rcx,QWORD PTR [r10]",
						"49 23 0a"),
				test(new Instruction(Opcode.AND, RDI, bimm), "and rdi,0x12", "48 83 e7 12"),
				test(new Instruction(Opcode.AND, RDI, iimm), "and rdi,0x12345678", "48 81 e7 78 56 34 12"),
				test(
						new Instruction(Opcode.AND, RDI, new Immediate(0xfedcba98)),
						"and rdi,0xfedcba98",
						"48 81 e7 98 ba dc fe"),
				test(new Instruction(Opcode.AND, RDI, new Immediate((byte) 0xf0)), "and rdi,0xf0", "48 83 e7 f0"),
				test(new Instruction(Opcode.AND, RDI, new Immediate((byte) 0xfe)), "and rdi,0xfe", "48 83 e7 fe"),
				test(
						new Instruction(
								Opcode.AND,
								SPL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R10)
										.build()),
						"and spl,BYTE PTR [r10]",
						"41 22 22"),
				//  Sub
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(EAX)
										.index(EBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8D),
						"sub DWORD PTR [eax+ebx*4+0x12345678],r8d",
						"67 44 29 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R12)
										.index(R15)
										.scale(2)
										.build(),
								new Immediate((byte) 0x1)),
						"sub WORD PTR [r12+r15*2],0x01",
						"66 43 83 2c 7c 01"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDI)
										.displacement((byte) 0x4)
										.build(),
								new Immediate((byte) 1)),
						"sub BYTE PTR [rdi+0x4],0x01",
						"80 6f 04 01"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement((byte) 0x8)
										.build(),
								new Immediate(0x15b0)),
						"sub QWORD PTR [rsp+0x8],0x000015b0",
						"48 81 6c 24 08 b0 15 00 00"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8D),
						"sub DWORD PTR [rax+rbx*4+0x12345678],r8d",
						"44 29 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9),
						"sub QWORD PTR [rax+rbx*4+0x12345678],r9",
						"4c 29 8c 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RSP),
						"sub QWORD PTR [rax+rbx*4+0x12345678],rsp",
						"48 29 a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement((byte) 0x28)
										.build(),
								new Immediate((byte) 1)),
						"sub QWORD PTR [rsp+0x28],0x01",
						"48 83 6c 24 28 01"),
				test(
						new Instruction(
								Opcode.SUB,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R8W),
						"sub WORD PTR [rax+rbx*4+0x12345678],r8w",
						"66 44 29 84 98 78 56 34 12"),
				test(new Instruction(Opcode.SUB, CX, simm), "sub cx,0x1234", "66 81 e9 34 12"),
				test(new Instruction(Opcode.SUB, ESI, bimm), "sub esi,0x12", "83 ee 12"),
				test(new Instruction(Opcode.SUB, R10B, new Immediate((byte) 1)), "sub r10b,0x01", "41 80 ea 01"),
				test(
						new Instruction(Opcode.SUB, EDX, new Immediate(0x7ffffffb)),
						"sub edx,0x7ffffffb",
						"81 ea fb ff ff 7f"),
				test(
						new Instruction(
								Opcode.SUB,
								ESP,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"sub esp,DWORD PTR [rax+rbx*4+0x12345678]",
						"2b a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								R11D,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"sub r11d,DWORD PTR [rax+rbx*4+0x12345678]",
						"44 2b 9c 98 78 56 34 12"),
				test(new Instruction(Opcode.SUB, R8, R9), "sub r8,r9", "4d 29 c8"),
				test(new Instruction(Opcode.SUB, R9, R8), "sub r9,r8", "4d 29 c1"),
				test(
						new Instruction(
								Opcode.SUB,
								R9,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"sub r9,QWORD PTR [rax+rbx*4+0x12345678]",
						"4c 2b 8c 98 78 56 34 12"),
				test(new Instruction(Opcode.SUB, RDI, bimm), "sub rdi,0x12", "48 83 ef 12"),
				test(new Instruction(Opcode.SUB, RSP, iimm), "sub rsp,0x12345678", "48 81 ec 78 56 34 12"),
				test(
						new Instruction(
								Opcode.SUB,
								RSP,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"sub rsp,QWORD PTR [rax+rbx*4+0x12345678]",
						"48 2b a4 98 78 56 34 12"),
				//  Sbb
				test(new Instruction(Opcode.SBB, AL, bimm), "sbb al,0x12", "1c 12"),
				test(new Instruction(Opcode.SBB, AX, simm), "sbb ax,0x1234", "66 1d 34 12"),
				test(new Instruction(Opcode.SBB, CX, simm), "sbb cx,0x1234", "66 81 d9 34 12"),
				test(new Instruction(Opcode.SBB, R9D, bimm), "sbb r9d,0x12", "41 83 d9 12"),
				test(new Instruction(Opcode.SBB, RAX, bimm), "sbb rax,0x12", "48 83 d8 12"),
				test(new Instruction(Opcode.SBB, ESI, ESI), "sbb esi,esi", "19 f6"),
				test(new Instruction(Opcode.SBB, R12D, R12D), "sbb r12d,r12d", "45 19 e4"),
				test(new Instruction(Opcode.SBB, RAX, RAX), "sbb rax,rax", "48 19 c0"),
				test(
						new Instruction(
								Opcode.SBB,
								CL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.displacement((byte) 0x8d)
										.build()),
						"sbb cl,BYTE PTR [rax-0x73]",
						"1a 48 8d"),
				//  Shr
				test(new Instruction(Opcode.SHR, BPL, new Immediate((byte) 1)), "shr bpl,0x01", "40 d0 ed"),
				test(new Instruction(Opcode.SHR, BX, bimm), "shr bx,0x12", "66 c1 eb 12"),
				test(new Instruction(Opcode.SHR, DI, new Immediate((byte) 1)), "shr di,0x01", "66 d1 ef"),
				test(new Instruction(Opcode.SHR, EAX, CL), "shr eax,cl", "d3 e8"),
				test(new Instruction(Opcode.SHR, ECX, bimm), "shr ecx,0x12", "c1 e9 12"),
				test(new Instruction(Opcode.SHR, EDX, new Immediate((byte) 1)), "shr edx,0x01", "d1 ea"),
				test(new Instruction(Opcode.SHR, R11B, CL), "shr r11b,cl", "41 d2 eb"),
				test(new Instruction(Opcode.SHR, R9, new Immediate((byte) 1)), "shr r9,0x01", "49 d1 e9"),
				test(new Instruction(Opcode.SHR, RCX, CL), "shr rcx,cl", "48 d3 e9"),
				test(new Instruction(Opcode.SHR, RDX, bimm), "shr rdx,0x12", "48 c1 ea 12"),
				test(new Instruction(Opcode.SHR, SI, CL), "shr si,cl", "66 d3 ee"),
				test(new Instruction(Opcode.SHR, SIL, new Immediate((byte) 1)), "shr sil,0x01", "40 d0 ee"),
				test(new Instruction(Opcode.SHR, SPL, new Immediate((byte) 1)), "shr spl,0x01", "40 d0 ec"),
				//  Sar
				test(new Instruction(Opcode.SAR, BPL, new Immediate((byte) 1)), "sar bpl,0x01", "40 d0 fd"),
				test(new Instruction(Opcode.SAR, BX, bimm), "sar bx,0x12", "66 c1 fb 12"),
				test(new Instruction(Opcode.SAR, DI, new Immediate((byte) 1)), "sar di,0x01", "66 d1 ff"),
				test(new Instruction(Opcode.SAR, EAX, CL), "sar eax,cl", "d3 f8"),
				test(new Instruction(Opcode.SAR, ECX, bimm), "sar ecx,0x12", "c1 f9 12"),
				test(new Instruction(Opcode.SAR, EDX, new Immediate((byte) 1)), "sar edx,0x01", "d1 fa"),
				test(new Instruction(Opcode.SAR, R11B, CL), "sar r11b,cl", "41 d2 fb"),
				test(new Instruction(Opcode.SAR, R9, new Immediate((byte) 1)), "sar r9,0x01", "49 d1 f9"),
				test(new Instruction(Opcode.SAR, RCX, CL), "sar rcx,cl", "48 d3 f9"),
				test(new Instruction(Opcode.SAR, RDX, bimm), "sar rdx,0x12", "48 c1 fa 12"),
				test(new Instruction(Opcode.SAR, SI, CL), "sar si,cl", "66 d3 fe"),
				test(new Instruction(Opcode.SAR, SIL, new Immediate((byte) 1)), "sar sil,0x01", "40 d0 fe"),
				test(new Instruction(Opcode.SAR, SPL, new Immediate((byte) 1)), "sar spl,0x01", "40 d0 fc"),
				//  Shl
				test(new Instruction(Opcode.SHL, BPL, one), "shl bpl,0x01", "40 d0 e5"),
				test(new Instruction(Opcode.SHL, BX, bimm), "shl bx,0x12", "66 c1 e3 12"),
				test(new Instruction(Opcode.SHL, DI, one), "shl di,0x01", "66 d1 e7"),
				test(new Instruction(Opcode.SHL, EAX, CL), "shl eax,cl", "d3 e0"),
				test(new Instruction(Opcode.SHL, ECX, bimm), "shl ecx,0x12", "c1 e1 12"),
				test(new Instruction(Opcode.SHL, EDX, one), "shl edx,0x01", "d1 e2"),
				test(new Instruction(Opcode.SHL, R11B, CL), "shl r11b,cl", "41 d2 e3"),
				test(new Instruction(Opcode.SHL, R9, one), "shl r9,0x01", "49 d1 e1"),
				test(new Instruction(Opcode.SHL, RCX, CL), "shl rcx,cl", "48 d3 e1"),
				test(new Instruction(Opcode.SHL, RDX, bimm), "shl rdx,0x12", "48 c1 e2 12"),
				test(new Instruction(Opcode.SHL, SI, CL), "shl si,cl", "66 d3 e6"),
				test(new Instruction(Opcode.SHL, SIL, one), "shl sil,0x01", "40 d0 e6"),
				test(new Instruction(Opcode.SHL, SPL, one), "shl spl,0x01", "40 d0 e4"),
				//  Imul
				test(new Instruction(Opcode.IMUL, EAX, EBX, bimm), "imul eax,ebx,0x12", "6b c3 12"),
				test(
						new Instruction(Opcode.IMUL, RDX, R8, new Immediate(0x00000600)),
						"imul rdx,r8,0x00000600",
						"49 69 d0 00 06 00 00"),
				test(
						new Instruction(Opcode.IMUL, EDX, R15D, new Immediate(0xff0)),
						"imul edx,r15d,0x00000ff0",
						"41 69 d7 f0 0f 00 00"),
				test(
						new Instruction(
								Opcode.IMUL,
								EDX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R15)
										.build(),
								new Immediate(0xff0)),
						"imul edx,DWORD PTR [r15],0x00000ff0",
						"41 69 17 f0 0f 00 00"),
				test(
						new Instruction(
								Opcode.IMUL,
								EDI,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(R12)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"imul edi,DWORD PTR [rax+r12*8+0x12345678]",
						"42 0f af bc e0 78 56 34 12"),
				test(
						new Instruction(
								Opcode.IMUL,
								R9,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11)
										.index(R12)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0x7eadbeef)),
						"imul r9,QWORD PTR [r11+r12*4+0x12345678],0x7eadbeef",
						"4f 69 8c a3 78 56 34 12 ef be ad 7e"),
				test(
						new Instruction(
								Opcode.IMUL,
								R9,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build(),
								new Immediate(0x7eadbeef)),
						"imul r9,QWORD PTR [rax],0x7eadbeef",
						"4c 69 08 ef be ad 7e"),
				test(new Instruction(Opcode.IMUL, AX, DX), "imul ax,dx", "66 0f af c2"),
				test(new Instruction(Opcode.IMUL, EAX, EDX), "imul eax,edx", "0f af c2"),
				test(new Instruction(Opcode.IMUL, RBX, RBP), "imul rbx,rbp", "48 0f af dd"),
				test(new Instruction(Opcode.IMUL, RDX, R9, bimm), "imul rdx,r9,0x12", "49 6b d1 12"),
				test(new Instruction(Opcode.IMUL, R9, RDX, bimm), "imul r9,rdx,0x12", "4c 6b ca 12"),
				//  Idiv
				test(new Instruction(Opcode.IDIV, EAX), "idiv eax", "f7 f8"),
				test(new Instruction(Opcode.IDIV, ESI), "idiv esi", "f7 fe"),
				test(new Instruction(Opcode.IDIV, R11), "idiv r11", "49 f7 fb"),
				test(new Instruction(Opcode.IDIV, R9D), "idiv r9d", "41 f7 f9"),
				test(new Instruction(Opcode.IDIV, RAX), "idiv rax", "48 f7 f8"),
				test(new Instruction(Opcode.IDIV, RSI), "idiv rsi", "48 f7 fe"),
				//  Div
				test(new Instruction(Opcode.DIV, AH), "div ah", "f6 f4"),
				test(new Instruction(Opcode.DIV, AL), "div al", "f6 f0"),
				test(new Instruction(Opcode.DIV, AX), "div ax", "66 f7 f0"),
				test(new Instruction(Opcode.DIV, BH), "div bh", "f6 f7"),
				test(new Instruction(Opcode.DIV, BL), "div bl", "f6 f3"),
				test(new Instruction(Opcode.DIV, BP), "div bp", "66 f7 f5"),
				test(new Instruction(Opcode.DIV, BPL), "div bpl", "40 f6 f5"),
				test(new Instruction(Opcode.DIV, BX), "div bx", "66 f7 f3"),
				test(new Instruction(Opcode.DIV, CH), "div ch", "f6 f5"),
				test(new Instruction(Opcode.DIV, CL), "div cl", "f6 f1"),
				test(new Instruction(Opcode.DIV, CX), "div cx", "66 f7 f1"),
				test(new Instruction(Opcode.DIV, DH), "div dh", "f6 f6"),
				test(new Instruction(Opcode.DIV, DI), "div di", "66 f7 f7"),
				test(new Instruction(Opcode.DIV, DIL), "div dil", "40 f6 f7"),
				test(new Instruction(Opcode.DIV, DL), "div dl", "f6 f2"),
				test(new Instruction(Opcode.DIV, DX), "div dx", "66 f7 f2"),
				test(new Instruction(Opcode.DIV, EAX), "div eax", "f7 f0"),
				test(new Instruction(Opcode.DIV, EBP), "div ebp", "f7 f5"),
				test(new Instruction(Opcode.DIV, EBX), "div ebx", "f7 f3"),
				test(new Instruction(Opcode.DIV, ECX), "div ecx", "f7 f1"),
				test(new Instruction(Opcode.DIV, EDI), "div edi", "f7 f7"),
				test(new Instruction(Opcode.DIV, EDX), "div edx", "f7 f2"),
				test(new Instruction(Opcode.DIV, ESI), "div esi", "f7 f6"),
				test(new Instruction(Opcode.DIV, ESP), "div esp", "f7 f4"),
				test(new Instruction(Opcode.DIV, R10), "div r10", "49 f7 f2"),
				test(new Instruction(Opcode.DIV, R10B), "div r10b", "41 f6 f2"),
				test(new Instruction(Opcode.DIV, R10D), "div r10d", "41 f7 f2"),
				test(new Instruction(Opcode.DIV, R10W), "div r10w", "66 41 f7 f2"),
				test(new Instruction(Opcode.DIV, R11), "div r11", "49 f7 f3"),
				test(new Instruction(Opcode.DIV, R11B), "div r11b", "41 f6 f3"),
				test(new Instruction(Opcode.DIV, R11D), "div r11d", "41 f7 f3"),
				test(new Instruction(Opcode.DIV, R11W), "div r11w", "66 41 f7 f3"),
				test(new Instruction(Opcode.DIV, R12), "div r12", "49 f7 f4"),
				test(new Instruction(Opcode.DIV, R12B), "div r12b", "41 f6 f4"),
				test(new Instruction(Opcode.DIV, R12D), "div r12d", "41 f7 f4"),
				test(new Instruction(Opcode.DIV, R12W), "div r12w", "66 41 f7 f4"),
				test(new Instruction(Opcode.DIV, R13), "div r13", "49 f7 f5"),
				test(new Instruction(Opcode.DIV, R13B), "div r13b", "41 f6 f5"),
				test(new Instruction(Opcode.DIV, R13D), "div r13d", "41 f7 f5"),
				test(new Instruction(Opcode.DIV, R13W), "div r13w", "66 41 f7 f5"),
				test(new Instruction(Opcode.DIV, R14), "div r14", "49 f7 f6"),
				test(new Instruction(Opcode.DIV, R14B), "div r14b", "41 f6 f6"),
				test(new Instruction(Opcode.DIV, R14D), "div r14d", "41 f7 f6"),
				test(new Instruction(Opcode.DIV, R14W), "div r14w", "66 41 f7 f6"),
				test(new Instruction(Opcode.DIV, R15), "div r15", "49 f7 f7"),
				test(new Instruction(Opcode.DIV, R15B), "div r15b", "41 f6 f7"),
				test(new Instruction(Opcode.DIV, R15D), "div r15d", "41 f7 f7"),
				test(new Instruction(Opcode.DIV, R15W), "div r15w", "66 41 f7 f7"),
				test(new Instruction(Opcode.DIV, R8), "div r8", "49 f7 f0"),
				test(new Instruction(Opcode.DIV, R8B), "div r8b", "41 f6 f0"),
				test(new Instruction(Opcode.DIV, R8D), "div r8d", "41 f7 f0"),
				test(new Instruction(Opcode.DIV, R8W), "div r8w", "66 41 f7 f0"),
				test(new Instruction(Opcode.DIV, R9), "div r9", "49 f7 f1"),
				test(new Instruction(Opcode.DIV, R9B), "div r9b", "41 f6 f1"),
				test(new Instruction(Opcode.DIV, R9D), "div r9d", "41 f7 f1"),
				test(new Instruction(Opcode.DIV, R9W), "div r9w", "66 41 f7 f1"),
				test(new Instruction(Opcode.DIV, RAX), "div rax", "48 f7 f0"),
				test(new Instruction(Opcode.DIV, RBP), "div rbp", "48 f7 f5"),
				test(new Instruction(Opcode.DIV, RBX), "div rbx", "48 f7 f3"),
				test(new Instruction(Opcode.DIV, RCX), "div rcx", "48 f7 f1"),
				test(new Instruction(Opcode.DIV, RDI), "div rdi", "48 f7 f7"),
				test(new Instruction(Opcode.DIV, RDX), "div rdx", "48 f7 f2"),
				test(new Instruction(Opcode.DIV, RSI), "div rsi", "48 f7 f6"),
				test(new Instruction(Opcode.DIV, RSP), "div rsp", "48 f7 f4"),
				test(new Instruction(Opcode.DIV, SI), "div si", "66 f7 f6"),
				test(new Instruction(Opcode.DIV, SIL), "div sil", "40 f6 f6"),
				test(new Instruction(Opcode.DIV, SP), "div sp", "66 f7 f4"),
				test(new Instruction(Opcode.DIV, SPL), "div spl", "40 f6 f4"),
				//
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"div BYTE PTR [rax]",
						"f6 30"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R11)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"div BYTE PTR [rbx+r11*8+0x12345678]",
						"42 f6 b4 db 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"div DWORD PTR [rax]",
						"f7 30"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.index(R11)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"div DWORD PTR [rbx+r11*8+0x12345678]",
						"42 f7 b4 db 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"div QWORD PTR [rax]",
						"48 f7 30"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.index(R11)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"div QWORD PTR [rbx+r11*8+0x12345678]",
						"4a f7 b4 db 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.build()),
						"div WORD PTR [rax]",
						"66 f7 30"),
				test(
						new Instruction(
								Opcode.DIV,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RBX)
										.index(R11)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"div WORD PTR [rbx+r11*8+0x12345678]",
						"66 42 f7 b4 db 78 56 34 12"),
				//  Mul
				test(new Instruction(Opcode.MUL, AH), "mul ah", "f6 e4"),
				test(new Instruction(Opcode.MUL, AL), "mul al", "f6 e0"),
				test(new Instruction(Opcode.MUL, AX), "mul ax", "66 f7 e0"),
				test(new Instruction(Opcode.MUL, BH), "mul bh", "f6 e7"),
				test(new Instruction(Opcode.MUL, BL), "mul bl", "f6 e3"),
				test(new Instruction(Opcode.MUL, BP), "mul bp", "66 f7 e5"),
				test(new Instruction(Opcode.MUL, BPL), "mul bpl", "40 f6 e5"),
				test(new Instruction(Opcode.MUL, BX), "mul bx", "66 f7 e3"),
				test(new Instruction(Opcode.MUL, CH), "mul ch", "f6 e5"),
				test(new Instruction(Opcode.MUL, CL), "mul cl", "f6 e1"),
				test(new Instruction(Opcode.MUL, CX), "mul cx", "66 f7 e1"),
				test(new Instruction(Opcode.MUL, DH), "mul dh", "f6 e6"),
				test(new Instruction(Opcode.MUL, DI), "mul di", "66 f7 e7"),
				test(new Instruction(Opcode.MUL, DIL), "mul dil", "40 f6 e7"),
				test(new Instruction(Opcode.MUL, DL), "mul dl", "f6 e2"),
				test(new Instruction(Opcode.MUL, DX), "mul dx", "66 f7 e2"),
				test(new Instruction(Opcode.MUL, EAX), "mul eax", "f7 e0"),
				test(new Instruction(Opcode.MUL, EBP), "mul ebp", "f7 e5"),
				test(new Instruction(Opcode.MUL, EBX), "mul ebx", "f7 e3"),
				test(new Instruction(Opcode.MUL, ECX), "mul ecx", "f7 e1"),
				test(new Instruction(Opcode.MUL, EDI), "mul edi", "f7 e7"),
				test(new Instruction(Opcode.MUL, EDX), "mul edx", "f7 e2"),
				test(new Instruction(Opcode.MUL, ESI), "mul esi", "f7 e6"),
				test(new Instruction(Opcode.MUL, ESP), "mul esp", "f7 e4"),
				test(new Instruction(Opcode.MUL, R10), "mul r10", "49 f7 e2"),
				test(new Instruction(Opcode.MUL, R10B), "mul r10b", "41 f6 e2"),
				test(new Instruction(Opcode.MUL, R10D), "mul r10d", "41 f7 e2"),
				test(new Instruction(Opcode.MUL, R10W), "mul r10w", "66 41 f7 e2"),
				test(new Instruction(Opcode.MUL, R11), "mul r11", "49 f7 e3"),
				test(new Instruction(Opcode.MUL, R11B), "mul r11b", "41 f6 e3"),
				test(new Instruction(Opcode.MUL, R11D), "mul r11d", "41 f7 e3"),
				test(new Instruction(Opcode.MUL, R11W), "mul r11w", "66 41 f7 e3"),
				test(new Instruction(Opcode.MUL, R12), "mul r12", "49 f7 e4"),
				test(new Instruction(Opcode.MUL, R12B), "mul r12b", "41 f6 e4"),
				test(new Instruction(Opcode.MUL, R12D), "mul r12d", "41 f7 e4"),
				test(new Instruction(Opcode.MUL, R12W), "mul r12w", "66 41 f7 e4"),
				test(new Instruction(Opcode.MUL, R13), "mul r13", "49 f7 e5"),
				test(new Instruction(Opcode.MUL, R13B), "mul r13b", "41 f6 e5"),
				test(new Instruction(Opcode.MUL, R13D), "mul r13d", "41 f7 e5"),
				test(new Instruction(Opcode.MUL, R13W), "mul r13w", "66 41 f7 e5"),
				test(new Instruction(Opcode.MUL, R14), "mul r14", "49 f7 e6"),
				test(new Instruction(Opcode.MUL, R14B), "mul r14b", "41 f6 e6"),
				test(new Instruction(Opcode.MUL, R14D), "mul r14d", "41 f7 e6"),
				test(new Instruction(Opcode.MUL, R14W), "mul r14w", "66 41 f7 e6"),
				test(new Instruction(Opcode.MUL, R15), "mul r15", "49 f7 e7"),
				test(new Instruction(Opcode.MUL, R15B), "mul r15b", "41 f6 e7"),
				test(new Instruction(Opcode.MUL, R15D), "mul r15d", "41 f7 e7"),
				test(new Instruction(Opcode.MUL, R15W), "mul r15w", "66 41 f7 e7"),
				test(new Instruction(Opcode.MUL, R8), "mul r8", "49 f7 e0"),
				test(new Instruction(Opcode.MUL, R8B), "mul r8b", "41 f6 e0"),
				test(new Instruction(Opcode.MUL, R8D), "mul r8d", "41 f7 e0"),
				test(new Instruction(Opcode.MUL, R8W), "mul r8w", "66 41 f7 e0"),
				test(new Instruction(Opcode.MUL, R9), "mul r9", "49 f7 e1"),
				test(new Instruction(Opcode.MUL, R9B), "mul r9b", "41 f6 e1"),
				test(new Instruction(Opcode.MUL, R9D), "mul r9d", "41 f7 e1"),
				test(new Instruction(Opcode.MUL, R9W), "mul r9w", "66 41 f7 e1"),
				test(new Instruction(Opcode.MUL, RAX), "mul rax", "48 f7 e0"),
				test(new Instruction(Opcode.MUL, RBP), "mul rbp", "48 f7 e5"),
				test(new Instruction(Opcode.MUL, RBX), "mul rbx", "48 f7 e3"),
				test(new Instruction(Opcode.MUL, RCX), "mul rcx", "48 f7 e1"),
				test(new Instruction(Opcode.MUL, RDI), "mul rdi", "48 f7 e7"),
				test(new Instruction(Opcode.MUL, RDX), "mul rdx", "48 f7 e2"),
				test(new Instruction(Opcode.MUL, RSI), "mul rsi", "48 f7 e6"),
				test(new Instruction(Opcode.MUL, RSP), "mul rsp", "48 f7 e4"),
				test(new Instruction(Opcode.MUL, SI), "mul si", "66 f7 e6"),
				test(new Instruction(Opcode.MUL, SIL), "mul sil", "40 f6 e6"),
				test(new Instruction(Opcode.MUL, SP), "mul sp", "66 f7 e4"),
				test(new Instruction(Opcode.MUL, SPL), "mul spl", "40 f6 e4"),
				//  Or
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"or BYTE PTR [r11+r9*4+0x12345678],0x99",
						"43 80 8c 8b 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R9B),
						"or BYTE PTR [rbx+r9*4+0x12345678],r9b",
						"46 08 8c 8b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0xdeadbeef)),
						"or DWORD PTR [r11+r9*4+0x12345678],0xdeadbeef",
						"43 81 8c 8b 78 56 34 12 ef be ad de"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement((byte) 0x64)
										.build(),
								new Immediate(0xff07)),
						"or QWORD PTR [rbp+0x64],0x0000ff07",
						"48 81 4d 64 07 ff 00 00"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R8)
										.build(),
								RDX),
						"or QWORD PTR [r8],rdx",
						"49 09 10"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build(),
								RDX),
						"or QWORD PTR [rax],rdx",
						"48 09 10"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0xc9dcb)
										.build(),
								EAX),
						"or DWORD PTR [rip+0xc9dcb],eax",
						"09 05 cb 9d 0c 00"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.displacement(0xff8)
										.build(),
								new Immediate((byte) 0)),
						"or QWORD PTR [rsp+0xff8],0x00",
						"48 83 8c 24 f8 0f 00 00 00"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RSI),
						"or QWORD PTR [r9+rcx*4+0x12345678],rsi",
						"49 09 b4 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.OR,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((short) 0xbeef)),
						"or WORD PTR [r11+r9*4+0x12345678],0xbeef",
						"66 43 81 8c 8b 78 56 34 12 ef be"),
				test(new Instruction(Opcode.OR, AL, bimm), "or al,0x12", "0c 12"),
				test(new Instruction(Opcode.OR, CL, bimm), "or cl,0x12", "80 c9 12"),
				test(new Instruction(Opcode.OR, CX, simm), "or cx,0x1234", "66 81 c9 34 12"),
				test(new Instruction(Opcode.OR, EAX, bimm), "or eax,0x12", "83 c8 12"),
				test(new Instruction(Opcode.OR, EAX, iimm), "or eax,0x12345678", "0d 78 56 34 12"),
				test(new Instruction(Opcode.OR, AL, DL), "or al,dl", "08 d0"),
				test(
						new Instruction(
								Opcode.OR,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"or eax,DWORD PTR [rax+rbx*4+0x12345678]",
						"0b 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.OR,
								ECX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R10)
										.build()),
						"or ecx,DWORD PTR [r10]",
						"41 0b 0a"),
				test(
						new Instruction(
								Opcode.OR,
								DL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R12)
										.build()),
						"or dl,BYTE PTR [r12]",
						"41 0a 14 24"),
				test(new Instruction(Opcode.OR, EDI, bimm), "or edi,0x12", "83 cf 12"),
				test(new Instruction(Opcode.OR, RAX, bimm), "or rax,0x12", "48 83 c8 12"),
				test(new Instruction(Opcode.OR, RAX, iimm), "or rax,0x12345678", "48 0d 78 56 34 12"),
				test(new Instruction(Opcode.OR, RAX, new Immediate((byte) 0xfe)), "or rax,0xfe", "48 83 c8 fe"),
				test(
						new Instruction(
								Opcode.OR,
								RAX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"or rax,QWORD PTR [rax+rbx*4+0x12345678]",
						"48 0b 84 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.OR,
								RCX,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R10)
										.build()),
						"or rcx,QWORD PTR [r10]",
						"49 0b 0a"),
				test(new Instruction(Opcode.OR, RDI, bimm), "or rdi,0x12", "48 83 cf 12"),
				test(new Instruction(Opcode.OR, EDI, EAX), "or edi,eax", "09 c7"),
				test(new Instruction(Opcode.OR, RAX, R14), "or rax,r14", "4c 09 f0"),
				//  Xor
				test(new Instruction(Opcode.XOR, CX, simm), "xor cx,0x1234", "66 81 f1 34 12"),
				test(new Instruction(Opcode.XOR, EAX, bimm), "xor eax,0x12", "83 f0 12"),
				test(new Instruction(Opcode.XOR, EAX, iimm), "xor eax,0x12345678", "35 78 56 34 12"),
				test(new Instruction(Opcode.XOR, EBX, iimm), "xor ebx,0x12345678", "81 f3 78 56 34 12"),
				test(new Instruction(Opcode.XOR, R8, bimm), "xor r8,0x12", "49 83 f0 12"),
				test(new Instruction(Opcode.XOR, R8, iimm), "xor r8,0x12345678", "49 81 f0 78 56 34 12"),
				test(new Instruction(Opcode.XOR, SIL, new Immediate((byte) 0x80)), "xor sil,0x80", "40 80 f6 80"),
				test(
						new Instruction(
								Opcode.XOR,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.displacement(0x2454c60f)
										.build(),
								CL),
						"xor BYTE PTR [rax+0x2454c60f],cl",
						"30 88 0f c6 54 24"),
				test(
						new Instruction(
								Opcode.XOR,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.build(),
								ECX),
						"xor DWORD PTR [rcx],ecx",
						"31 09"),
				test(
						new Instruction(
								Opcode.XOR,
								DH,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDI)
										.displacement(0xc001145d)
										.build()),
						"xor dh,BYTE PTR [rdi-0x3ffeeba3]",
						"32 b7 5d 14 01 c0"),
				test(
						new Instruction(
								Opcode.XOR,
								EBX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.displacement((byte) 0x8)
										.build()),
						"xor ebx,DWORD PTR [rsp+0x8]",
						"33 5c 24 08"),
				test(
						new Instruction(
								Opcode.XOR,
								R14,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.displacement((byte) 0x30)
										.build()),
						"xor r14,QWORD PTR [rbp+0x30]",
						"4c 33 75 30"),
				//  Not
				test(new Instruction(Opcode.NOT, CX), "not cx", "66 f7 d1"),
				test(new Instruction(Opcode.NOT, EAX), "not eax", "f7 d0"),
				test(new Instruction(Opcode.NOT, EBP), "not ebp", "f7 d5"),
				test(new Instruction(Opcode.NOT, EBX), "not ebx", "f7 d3"),
				test(new Instruction(Opcode.NOT, ECX), "not ecx", "f7 d1"),
				test(new Instruction(Opcode.NOT, EDI), "not edi", "f7 d7"),
				test(new Instruction(Opcode.NOT, EDX), "not edx", "f7 d2"),
				test(new Instruction(Opcode.NOT, ESI), "not esi", "f7 d6"),
				test(new Instruction(Opcode.NOT, ESP), "not esp", "f7 d4"),
				test(new Instruction(Opcode.NOT, R10), "not r10", "49 f7 d2"),
				test(new Instruction(Opcode.NOT, R10D), "not r10d", "41 f7 d2"),
				test(new Instruction(Opcode.NOT, R11), "not r11", "49 f7 d3"),
				test(new Instruction(Opcode.NOT, R11D), "not r11d", "41 f7 d3"),
				test(new Instruction(Opcode.NOT, R12), "not r12", "49 f7 d4"),
				test(new Instruction(Opcode.NOT, R12D), "not r12d", "41 f7 d4"),
				test(new Instruction(Opcode.NOT, R13), "not r13", "49 f7 d5"),
				test(new Instruction(Opcode.NOT, R13D), "not r13d", "41 f7 d5"),
				test(new Instruction(Opcode.NOT, R14), "not r14", "49 f7 d6"),
				test(new Instruction(Opcode.NOT, R14D), "not r14d", "41 f7 d6"),
				test(new Instruction(Opcode.NOT, R15), "not r15", "49 f7 d7"),
				test(new Instruction(Opcode.NOT, R15D), "not r15d", "41 f7 d7"),
				test(new Instruction(Opcode.NOT, R8), "not r8", "49 f7 d0"),
				test(new Instruction(Opcode.NOT, R8D), "not r8d", "41 f7 d0"),
				test(new Instruction(Opcode.NOT, R9), "not r9", "49 f7 d1"),
				test(new Instruction(Opcode.NOT, R9D), "not r9d", "41 f7 d1"),
				test(new Instruction(Opcode.NOT, RAX), "not rax", "48 f7 d0"),
				test(new Instruction(Opcode.NOT, RBP), "not rbp", "48 f7 d5"),
				test(new Instruction(Opcode.NOT, RBX), "not rbx", "48 f7 d3"),
				test(new Instruction(Opcode.NOT, RCX), "not rcx", "48 f7 d1"),
				test(new Instruction(Opcode.NOT, RDI), "not rdi", "48 f7 d7"),
				test(new Instruction(Opcode.NOT, RDX), "not rdx", "48 f7 d2"),
				test(new Instruction(Opcode.NOT, RSI), "not rsi", "48 f7 d6"),
				test(new Instruction(Opcode.NOT, RSP), "not rsp", "48 f7 d4"),
				//  Neg
				test(
						new Instruction(
								Opcode.NEG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R8)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"neg DWORD PTR [r8+r9*4+0x12345678]",
						"43 f7 9c 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.NEG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R8)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"neg QWORD PTR [r8+r9*4+0x12345678]",
						"4b f7 9c 88 78 56 34 12"),
				test(new Instruction(Opcode.NEG, EAX), "neg eax", "f7 d8"),
				test(new Instruction(Opcode.NEG, RBX), "neg rbx", "48 f7 db"),
				//  Test
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								bimm),
						"test BYTE PTR [r11+rdx*4+0x12345678],0x12",
						"41 f6 84 93 78 56 34 12 12"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"test BYTE PTR [r11+rdx*4+0x12345678],0x99",
						"41 f6 84 93 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R13B),
						"test BYTE PTR [r11+rdx*4+0x12345678],r13b",
						"45 84 ac 93 78 56 34 12"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R11D)
										.index(EDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate((byte) 0x99)),
						"test BYTE PTR [r11d+edx*4+0x12345678],0x99",
						"67 41 f6 84 93 78 56 34 12 99"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R15)
										.displacement((byte) 0x40)
										.build(),
								new Immediate((byte) 0x08)),
						"test BYTE PTR [r15+0x40],0x08",
						"41 f6 47 40 08"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0xdeadbeef)),
						"test DWORD PTR [r11+rdx*4+0x12345678],0xdeadbeef",
						"41 f7 84 93 78 56 34 12 ef be ad de"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								EBX),
						"test DWORD PTR [r11+rdx*4+0x12345678],ebx",
						"41 85 9c 93 78 56 34 12"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R11D)
										.index(EDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								new Immediate(0xdeadbeef)),
						"test DWORD PTR [r11d+edx*4+0x12345678],0xdeadbeef",
						"67 41 f7 84 93 78 56 34 12 ef be ad de"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RAX),
						"test QWORD PTR [r11+rdx*4+0x12345678],rax",
						"49 85 84 93 78 56 34 12"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								simm),
						"test WORD PTR [r11+rdx*4+0x12345678],0x1234",
						"66 41 f7 84 93 78 56 34 12 34 12"),
				test(
						new Instruction(
								Opcode.TEST,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(R11)
										.index(RDX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R12W),
						"test WORD PTR [r11+rdx*4+0x12345678],r12w",
						"66 45 85 a4 93 78 56 34 12"),
				test(new Instruction(Opcode.TEST, AL, bimm), "test al,0x12", "a8 12"),
				test(new Instruction(Opcode.TEST, AX, simm), "test ax,0x1234", "66 a9 34 12"),
				test(new Instruction(Opcode.TEST, EAX, iimm), "test eax,0x12345678", "a9 78 56 34 12"),
				test(new Instruction(Opcode.TEST, R9B, bimm), "test r9b,0x12", "41 f6 c1 12"),
				test(new Instruction(Opcode.TEST, R9B, R9B), "test r9b,r9b", "45 84 c9"),
				test(new Instruction(Opcode.TEST, R9D, R9D), "test r9d,r9d", "45 85 c9"),
				test(new Instruction(Opcode.TEST, R9W, simm), "test r9w,0x1234", "66 41 f7 c1 34 12"),
				test(new Instruction(Opcode.TEST, R9W, R9W), "test r9w,r9w", "66 45 85 c9"),
				test(new Instruction(Opcode.TEST, RAX, iimm), "test rax,0x12345678", "48 a9 78 56 34 12"),
				test(new Instruction(Opcode.TEST, RBX, RBX), "test rbx,rbx", "48 85 db"),
				//  Ud2
				test(new Instruction(Opcode.UD2), "ud2", "0f 0b"),
				//  Rep/repnz movs
				test(
						new Instruction(
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(DS, RSI))
										.build()),
						"movs BYTE PTR es:[rdi],BYTE PTR ds:[rsi]",
						"a4"),
				test(
						new Instruction(
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(DS, RSI))
										.build()),
						"movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]",
						"a5"),
				test(
						new Instruction(
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"movs WORD PTR es:[edi],WORD PTR ds:[esi]",
						"67 66 a5"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"rep movs BYTE PTR es:[edi],BYTE PTR ds:[esi]",
						"67 f3 a4"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"rep movs DWORD PTR es:[edi],DWORD PTR ds:[esi]",
						"67 f3 a5"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(DS, RSI))
										.build()),
						"rep movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]",
						"f3 a5"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"rep movs WORD PTR es:[edi],WORD PTR ds:[esi]",
						"67 66 f3 a5"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(DS, RSI))
										.build()),
						"rep movs WORD PTR es:[rdi],WORD PTR ds:[rsi]",
						"66 f3 a5"),
				test(
						new Instruction(
								InstructionPrefix.REPNZ,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"repnz movs BYTE PTR es:[edi],BYTE PTR ds:[esi]",
						"67 f2 a4"),
				test(
						new Instruction(
								InstructionPrefix.REPNZ,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"repnz movs DWORD PTR es:[edi],DWORD PTR ds:[esi]",
						"67 f2 a5"),
				test(
						new Instruction(
								InstructionPrefix.REPNZ,
								Opcode.MOVS,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(new SegmentRegister(DS, ESI))
										.build()),
						"repnz movs WORD PTR es:[edi],WORD PTR ds:[esi]",
						"67 66 f2 a5"),
				//  Rep stos
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								AL),
						"rep stos BYTE PTR es:[edi],al",
						"67 f3 aa"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								AL),
						"rep stos BYTE PTR es:[rdi],al",
						"f3 aa"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								EAX),
						"rep stos DWORD PTR es:[rdi],eax",
						"f3 ab"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								RAX),
						"rep stos QWORD PTR es:[edi],rax",
						"67 f3 48 ab"),
				test(
						new Instruction(
								InstructionPrefix.REP,
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(new SegmentRegister(ES, RDI))
										.build(),
								RAX),
						"rep stos QWORD PTR es:[rdi],rax",
						"f3 48 ab"),
				test(
						new Instruction(
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								AL),
						"stos BYTE PTR es:[edi],al",
						"67 aa"),
				test(
						new Instruction(
								Opcode.STOS,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(new SegmentRegister(ES, EDI))
										.build(),
								RAX),
						"stos QWORD PTR es:[edi],rax",
						"67 48 ab"),
				//  Movdqa
				test(
						new Instruction(
								Opcode.MOVDQA,
								XMM2,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSP)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movdqa xmm2,XMMWORD PTR [rsp+r9*4+0x12345678]",
						"66 42 0f 6f 94 8c 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVDQA,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RDI)
										.build(),
								XMM1),
						"movdqa XMMWORD PTR [rdi],xmm1",
						"66 0f 7f 0f"),
				test(new Instruction(Opcode.MOVDQA, XMM11, XMM10), "movdqa xmm11,xmm10", "66 45 0f 6f da"),
				// Movdqu
				test(
						new Instruction(
								Opcode.MOVDQU,
								XMM10,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RIP)
										.displacement(0xb3426)
										.build()),
						"movdqu xmm10,XMMWORD PTR [rip+0xb3426]",
						"f3 44 0f 6f 15 26 34 0b 00"),
				//  Movaps
				test(
						new Instruction(
								Opcode.MOVAPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RIP)
										.displacement(0x12345678)
										.build(),
								XMM6),
						"movaps XMMWORD PTR [rip+0x12345678],xmm6",
						"0f 29 35 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVAPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(EIP)
										.displacement(0x12345678)
										.build(),
								XMM6),
						"movaps XMMWORD PTR [eip+0x12345678],xmm6",
						"67 0f 29 35 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVAPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build(),
								XMM6),
						"movaps XMMWORD PTR [rax+0x12345678],xmm6",
						"0f 29 b0 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVAPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSP)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								XMM7),
						"movaps XMMWORD PTR [rsp+r11*4+0x12345678],xmm7",
						"42 0f 29 bc 9c 78 56 34 12"),
				test(new Instruction(Opcode.MOVAPS, XMM0, XMM0), "movaps xmm0,xmm0", "0f 28 c0"),
				test(
						new Instruction(
								Opcode.MOVAPS,
								XMM6,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RIP)
										.displacement(0x12345678)
										.build()),
						"movaps xmm6,XMMWORD PTR [rip+0x12345678]",
						"0f 28 35 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVAPS,
								XMM7,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSP)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movaps xmm7,XMMWORD PTR [rsp+r11*4+0x12345678]",
						"42 0f 28 bc 9c 78 56 34 12"),
				test(new Instruction(Opcode.MOVAPS, XMM7, XMM5), "movaps xmm7,xmm5", "0f 28 fd"),
				//  Movapd
				test(
						new Instruction(
								Opcode.MOVAPD,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RIP)
										.displacement(0x12345678)
										.build(),
								XMM6),
						"movapd XMMWORD PTR [rip+0x12345678],xmm6",
						"66 0f 29 35 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVAPD,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSP)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								XMM7),
						"movapd XMMWORD PTR [rsp+r11*4+0x12345678],xmm7",
						"66 42 0f 29 bc 9c 78 56 34 12"),
				test(new Instruction(Opcode.MOVAPD, XMM0, XMM0), "movapd xmm0,xmm0", "66 0f 28 c0"),
				test(new Instruction(Opcode.MOVAPD, XMM7, XMM5), "movapd xmm7,xmm5", "66 0f 28 fd"),
				//  Movq
				test(
						new Instruction(
								Opcode.MOVQ,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.index(RSI)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								XMM3),
						"movq QWORD PTR [rbp+rsi*4+0x12345678],xmm3",
						"66 0f d6 9c b5 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVQ,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.index(RSI)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								XMM3),
						"movq QWORD PTR [rsi*4+0x12345678],xmm3",
						"66 0f d6 1c b5 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVQ,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.displacement(0x12345678)
										.build(),
								XMM3),
						"movq QWORD PTR [rsi+0x12345678],xmm3",
						"66 0f d6 9e 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVQ,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build(),
								XMM3),
						"movq QWORD PTR [rsi],xmm3",
						"66 0f d6 1e"),
				test(new Instruction(Opcode.MOVQ, MM0, R9), "movq mm0,r9", "49 0f 6e c1"),
				test(new Instruction(Opcode.MOVQ, MM0, RCX), "movq mm0,rcx", "48 0f 6e c1"),
				test(new Instruction(Opcode.MOVQ, MM3, RSI), "movq mm3,rsi", "48 0f 6e de"),
				test(new Instruction(Opcode.MOVQ, XMM0, R9), "movq xmm0,r9", "66 49 0f 6e c1"),
				test(new Instruction(Opcode.MOVQ, XMM2, RAX), "movq xmm2,rax", "66 48 0f 6e d0"),
				test(
						new Instruction(
								Opcode.MOVQ,
								XMM3,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.index(RSI)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movq xmm3,QWORD PTR [rbp+rsi*4+0x12345678]",
						"f3 0f 7e 9c b5 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVQ,
								XMM6,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.displacement(0x12345678)
										.build()),
						"movq xmm6,QWORD PTR [rsi+0x12345678]",
						"f3 0f 7e b6 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVQ,
								XMM6,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build()),
						"movq xmm6,QWORD PTR [rsi]",
						"f3 0f 7e 36"),
				test(
						new Instruction(
								Opcode.MOVQ,
								XMM6,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R14)
										.build()),
						"movq xmm6,QWORD PTR [r14]",
						"f3 41 0f 7e 36"),
				test(
						new Instruction(
								Opcode.MOVQ,
								MM2,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movq mm2,QWORD PTR [rsp+r9*4+0x12345678]",
						"42 0f 6f 94 8c 78 56 34 12"),
				// Movd
				test(new Instruction(Opcode.MOVD, XMM3, EBX), "movd xmm3,ebx", "66 0f 6e db"),
				test(new Instruction(Opcode.MOVD, MM5, ESI), "movd mm5,esi", "0f 6e ee"),
				test(
						new Instruction(
								Opcode.MOVD,
								XMM0,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(R14)
										.displacement((byte) 0xc)
										.build()),
						"movd xmm0,DWORD PTR [r14+0xc]",
						"66 41 0f 6e 46 0c"),
				test(
						new Instruction(
								Opcode.MOVD,
								MM2,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSP)
										.index(R9)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movd mm2,QWORD PTR [rsp+r9*4+0x12345678]",
						"42 0f 6e 94 8c 78 56 34 12"),
				//  Movhps
				test(
						new Instruction(
								Opcode.MOVHPS,
								XMM3,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EAX)
										.build()),
						"movhps xmm3,QWORD PTR [eax]",
						"67 0f 16 18"),
				test(
						new Instruction(
								Opcode.MOVHPS,
								XMM3,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"movhps xmm3,QWORD PTR [rax]",
						"0f 16 18"),
				test(
						new Instruction(
								Opcode.MOVHPS,
								XMM3,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBP)
										.index(RSI)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"movhps xmm3,QWORD PTR [rbp+rsi*4+0x12345678]",
						"0f 16 9c b5 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVHPS,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RCX)
										.displacement((byte) 0x12)
										.build(),
								XMM1),
						"movhps QWORD PTR [rcx+0x12],xmm1",
						"0f 17 49 12"),
				// Movhpd
				test(
						new Instruction(
								Opcode.MOVHPD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RCX)
										.displacement((byte) 0x12)
										.build(),
								XMM1),
						"movhpd QWORD PTR [rcx+0x12],xmm1",
						"66 0f 17 49 12"),
				//  Movhlps
				test(new Instruction(Opcode.MOVHLPS, XMM0, XMM0), "movhlps xmm0,xmm0", "0f 12 c0"),
				test(new Instruction(Opcode.MOVHLPS, XMM3, XMM7), "movhlps xmm3,xmm7", "0f 12 df"),
				//  Punpcklqdq
				test(new Instruction(Opcode.PUNPCKLQDQ, XMM0, XMM0), "punpcklqdq xmm0,xmm0", "66 0f 6c c0"),
				test(new Instruction(Opcode.PUNPCKLQDQ, XMM3, XMM9), "punpcklqdq xmm3,xmm9", "66 41 0f 6c d9"),
				//  Punpckhqdq
				test(new Instruction(Opcode.PUNPCKHQDQ, XMM0, XMM0), "punpckhqdq xmm0,xmm0", "66 0f 6d c0"),
				test(new Instruction(Opcode.PUNPCKHQDQ, XMM3, XMM9), "punpckhqdq xmm3,xmm9", "66 41 0f 6d d9"),
				//  Punpckldq
				test(new Instruction(Opcode.PUNPCKLDQ, XMM0, XMM0), "punpckldq xmm0,xmm0", "66 0f 62 c0"),
				test(new Instruction(Opcode.PUNPCKLDQ, XMM3, XMM9), "punpckldq xmm3,xmm9", "66 41 0f 62 d9"),
				//  Setae
				test(
						new Instruction(
								Opcode.SETAE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setae BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 93 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETAE, AL), "setae al", "0f 93 c0"),
				test(new Instruction(Opcode.SETAE, R8B), "setae r8b", "41 0f 93 c0"),
				//  Setne
				test(
						new Instruction(
								Opcode.SETNE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setne BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 95 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETNE, AL), "setne al", "0f 95 c0"),
				test(new Instruction(Opcode.SETNE, R8B), "setne r8b", "41 0f 95 c0"),
				//  Setb
				test(
						new Instruction(
								Opcode.SETB,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setb BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 92 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETB, AL), "setb al", "0f 92 c0"),
				test(new Instruction(Opcode.SETB, R8B), "setb r8b", "41 0f 92 c0"),
				//  Sete
				test(
						new Instruction(
								Opcode.SETE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"sete BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 94 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETE, AL), "sete al", "0f 94 c0"),
				test(new Instruction(Opcode.SETE, R8B), "sete r8b", "41 0f 94 c0"),
				//  Seta
				test(
						new Instruction(
								Opcode.SETA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"seta BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 97 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETA, AL), "seta al", "0f 97 c0"),
				test(new Instruction(Opcode.SETA, R8B), "seta r8b", "41 0f 97 c0"),
				//  Setle
				test(
						new Instruction(
								Opcode.SETLE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setle BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 9e 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETLE, AL), "setle al", "0f 9e c0"),
				test(new Instruction(Opcode.SETLE, R8B), "setle r8b", "41 0f 9e c0"),
				//  Setbe
				test(
						new Instruction(
								Opcode.SETBE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setbe BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 96 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETBE, AL), "setbe al", "0f 96 c0"),
				test(new Instruction(Opcode.SETBE, R8B), "setbe r8b", "41 0f 96 c0"),
				//  Setl
				test(
						new Instruction(
								Opcode.SETL,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setl BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 9c 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETL, AL), "setl al", "0f 9c c0"),
				test(new Instruction(Opcode.SETL, R8B), "setl r8b", "41 0f 9c c0"),
				//  Setg
				test(
						new Instruction(
								Opcode.SETG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setg BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 9f 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETG, AL), "setg al", "0f 9f c0"),
				test(new Instruction(Opcode.SETG, R8B), "setg r8b", "41 0f 9f c0"),
				//  Setge
				test(
						new Instruction(
								Opcode.SETGE,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setge BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 9d 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETGE, AL), "setge al", "0f 9d c0"),
				test(new Instruction(Opcode.SETGE, R8B), "setge r8b", "41 0f 9d c0"),
				// Seto
				test(
						new Instruction(
								Opcode.SETO,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"seto BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 90 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETO, AL), "seto al", "0f 90 c0"),
				// Setno
				test(
						new Instruction(
								Opcode.SETNO,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RDX)
										.index(R9)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"setno BYTE PTR [rdx+r9*2+0x12345678]",
						"42 0f 91 84 4a 78 56 34 12"),
				test(new Instruction(Opcode.SETNO, AL), "setno al", "0f 91 c0"),
				//  Movabs
				test(
						new Instruction(Opcode.MOVABS, RCX, new Immediate(0x1234567812345678L)),
						"movabs rcx,0x1234567812345678",
						"48 b9 78 56 34 12 78 56 34 12"),
				test(
						new Instruction(Opcode.MOVABS, RDX, new Immediate(0x12345678L)),
						"movabs rdx,0x0000000012345678",
						"48 ba 78 56 34 12 00 00 00 00"),
				//  Movups
				test(
						new Instruction(
								Opcode.MOVUPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(EBX)
										.index(EDI)
										.scale(8)
										.displacement(0x12345678)
										.build(),
								XMM14),
						"movups XMMWORD PTR [ebx+edi*8+0x12345678],xmm14",
						"67 44 0f 11 b4 fb 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVUPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(R8)
										.build(),
								XMM0),
						"movups XMMWORD PTR [r8],xmm0",
						"41 0f 11 00"),
				test(
						new Instruction(
								Opcode.MOVUPS,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RBX)
										.index(RDI)
										.scale(8)
										.displacement(0x12345678)
										.build(),
								XMM14),
						"movups XMMWORD PTR [rbx+rdi*8+0x12345678],xmm14",
						"44 0f 11 b4 fb 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVUPS,
								XMM0,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RBX)
										.build()),
						"movups xmm0,XMMWORD PTR [rbx]",
						"0f 10 03"),
				//  Movsd
				test(
						new Instruction(
								Opcode.MOVSD,
								XMM0,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R8)
										.build()),
						"movsd xmm0,QWORD PTR [r8]",
						"f2 41 0f 10 00"),
				test(
						new Instruction(
								Opcode.MOVSD,
								XMM0,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.build()),
						"movsd xmm0,QWORD PTR [rbx]",
						"f2 0f 10 03"),
				test(
						new Instruction(
								Opcode.MOVSD,
								XMM14,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(EBX)
										.index(EDI)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"movsd xmm14,QWORD PTR [ebx+edi*8+0x12345678]",
						"67 f2 44 0f 10 b4 fb 78 56 34 12"),
				test(
						new Instruction(
								Opcode.MOVSD,
								XMM14,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RBX)
										.index(RDI)
										.scale(8)
										.displacement(0x12345678)
										.build()),
						"movsd xmm14,QWORD PTR [rbx+rdi*8+0x12345678]",
						"f2 44 0f 10 b4 fb 78 56 34 12"),
				//  Endbr64
				test(new Instruction(Opcode.ENDBR64), "endbr64", "f3 0f 1e fa"),
				//  Inc
				test(new Instruction(Opcode.INC, AH), "inc ah", "fe c4"),
				test(new Instruction(Opcode.INC, AL), "inc al", "fe c0"),
				test(new Instruction(Opcode.INC, BH), "inc bh", "fe c7"),
				test(new Instruction(Opcode.INC, BL), "inc bl", "fe c3"),
				test(new Instruction(Opcode.INC, BPL), "inc bpl", "40 fe c5"),
				test(new Instruction(Opcode.INC, CH), "inc ch", "fe c5"),
				test(new Instruction(Opcode.INC, CL), "inc cl", "fe c1"),
				test(new Instruction(Opcode.INC, DH), "inc dh", "fe c6"),
				test(new Instruction(Opcode.INC, DIL), "inc dil", "40 fe c7"),
				test(new Instruction(Opcode.INC, DL), "inc dl", "fe c2"),
				test(new Instruction(Opcode.INC, AX), "inc ax", "66 ff c0"),
				test(new Instruction(Opcode.INC, EAX), "inc eax", "ff c0"),
				test(new Instruction(Opcode.INC, EBP), "inc ebp", "ff c5"),
				test(new Instruction(Opcode.INC, EBX), "inc ebx", "ff c3"),
				test(new Instruction(Opcode.INC, ECX), "inc ecx", "ff c1"),
				test(new Instruction(Opcode.INC, EDI), "inc edi", "ff c7"),
				test(new Instruction(Opcode.INC, EDX), "inc edx", "ff c2"),
				test(new Instruction(Opcode.INC, ESI), "inc esi", "ff c6"),
				test(new Instruction(Opcode.INC, ESP), "inc esp", "ff c4"),
				test(new Instruction(Opcode.INC, R10), "inc r10", "49 ff c2"),
				test(new Instruction(Opcode.INC, R10B), "inc r10b", "41 fe c2"),
				test(new Instruction(Opcode.INC, R10D), "inc r10d", "41 ff c2"),
				test(new Instruction(Opcode.INC, R11), "inc r11", "49 ff c3"),
				test(new Instruction(Opcode.INC, R11B), "inc r11b", "41 fe c3"),
				test(new Instruction(Opcode.INC, R11D), "inc r11d", "41 ff c3"),
				test(new Instruction(Opcode.INC, R12), "inc r12", "49 ff c4"),
				test(new Instruction(Opcode.INC, R12B), "inc r12b", "41 fe c4"),
				test(new Instruction(Opcode.INC, R12D), "inc r12d", "41 ff c4"),
				test(new Instruction(Opcode.INC, R13), "inc r13", "49 ff c5"),
				test(new Instruction(Opcode.INC, R13B), "inc r13b", "41 fe c5"),
				test(new Instruction(Opcode.INC, R13D), "inc r13d", "41 ff c5"),
				test(new Instruction(Opcode.INC, R14), "inc r14", "49 ff c6"),
				test(new Instruction(Opcode.INC, R14B), "inc r14b", "41 fe c6"),
				test(new Instruction(Opcode.INC, R14D), "inc r14d", "41 ff c6"),
				test(new Instruction(Opcode.INC, R15), "inc r15", "49 ff c7"),
				test(new Instruction(Opcode.INC, R15B), "inc r15b", "41 fe c7"),
				test(new Instruction(Opcode.INC, R15D), "inc r15d", "41 ff c7"),
				test(new Instruction(Opcode.INC, R8), "inc r8", "49 ff c0"),
				test(new Instruction(Opcode.INC, R8B), "inc r8b", "41 fe c0"),
				test(new Instruction(Opcode.INC, R8D), "inc r8d", "41 ff c0"),
				test(new Instruction(Opcode.INC, R9), "inc r9", "49 ff c1"),
				test(new Instruction(Opcode.INC, R9B), "inc r9b", "41 fe c1"),
				test(new Instruction(Opcode.INC, R9D), "inc r9d", "41 ff c1"),
				test(new Instruction(Opcode.INC, RAX), "inc rax", "48 ff c0"),
				test(new Instruction(Opcode.INC, RBP), "inc rbp", "48 ff c5"),
				test(new Instruction(Opcode.INC, RBX), "inc rbx", "48 ff c3"),
				test(new Instruction(Opcode.INC, RCX), "inc rcx", "48 ff c1"),
				test(new Instruction(Opcode.INC, RDI), "inc rdi", "48 ff c7"),
				test(new Instruction(Opcode.INC, RDX), "inc rdx", "48 ff c2"),
				test(new Instruction(Opcode.INC, RSI), "inc rsi", "48 ff c6"),
				test(new Instruction(Opcode.INC, RSP), "inc rsp", "48 ff c4"),
				test(new Instruction(Opcode.INC, SIL), "inc sil", "40 fe c6"),
				test(new Instruction(Opcode.INC, SPL), "inc spl", "40 fe c4"),
				//
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build()),
						"inc BYTE PTR [rax+0x12345678]",
						"fe 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"inc BYTE PTR [rax]",
						"fe 00"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(RCX)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"inc BYTE PTR [rbx+rcx*2+0x12345678]",
						"fe 84 4b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rax+0x12345678]",
						"ff 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"inc DWORD PTR [rax]",
						"ff 00"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rbp+0x12345678]",
						"ff 85 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.index(RSI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rbp+rsi*2+0x12345678]",
						"ff 84 75 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBX)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rbx+0x12345678]",
						"ff 83 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RCX)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rcx+0x12345678]",
						"ff 81 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rdi+0x12345678]",
						"ff 87 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDX)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rdx+0x12345678]",
						"ff 82 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rsi+0x12345678]",
						"ff 86 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.displacement(0x12345678)
										.build()),
						"inc DWORD PTR [rsp+0x12345678]",
						"ff 84 24 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R8)
										.index(RDI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"inc QWORD PTR [r8+rdi*2+0x12345678]",
						"49 ff 84 78 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"inc QWORD PTR [rax]",
						"48 ff 00"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RCX)
										.displacement(0x12345678)
										.build()),
						"inc QWORD PTR [rcx+0x12345678]",
						"48 ff 81 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build()),
						"inc WORD PTR [rax+0x12345678]",
						"66 ff 80 78 56 34 12"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.build()),
						"inc WORD PTR [rax]",
						"66 ff 00"),
				test(
						new Instruction(
								Opcode.INC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RDX)
										.index(RDI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"inc WORD PTR [rdx+rdi*2+0x12345678]",
						"66 ff 84 7a 78 56 34 12"),
				//  Dec
				test(new Instruction(Opcode.DEC, AH), "dec ah", "fe cc"),
				test(new Instruction(Opcode.DEC, AL), "dec al", "fe c8"),
				test(new Instruction(Opcode.DEC, BH), "dec bh", "fe cf"),
				test(new Instruction(Opcode.DEC, BL), "dec bl", "fe cb"),
				test(new Instruction(Opcode.DEC, BPL), "dec bpl", "40 fe cd"),
				test(new Instruction(Opcode.DEC, CH), "dec ch", "fe cd"),
				test(new Instruction(Opcode.DEC, CL), "dec cl", "fe c9"),
				test(new Instruction(Opcode.DEC, DH), "dec dh", "fe ce"),
				test(new Instruction(Opcode.DEC, DIL), "dec dil", "40 fe cf"),
				test(new Instruction(Opcode.DEC, DL), "dec dl", "fe ca"),
				test(new Instruction(Opcode.DEC, EAX), "dec eax", "ff c8"),
				test(new Instruction(Opcode.DEC, EBP), "dec ebp", "ff cd"),
				test(new Instruction(Opcode.DEC, EBX), "dec ebx", "ff cb"),
				test(new Instruction(Opcode.DEC, ECX), "dec ecx", "ff c9"),
				test(new Instruction(Opcode.DEC, EDI), "dec edi", "ff cf"),
				test(new Instruction(Opcode.DEC, EDX), "dec edx", "ff ca"),
				test(new Instruction(Opcode.DEC, ESI), "dec esi", "ff ce"),
				test(new Instruction(Opcode.DEC, ESP), "dec esp", "ff cc"),
				test(new Instruction(Opcode.DEC, R10), "dec r10", "49 ff ca"),
				test(new Instruction(Opcode.DEC, R10B), "dec r10b", "41 fe ca"),
				test(new Instruction(Opcode.DEC, R10D), "dec r10d", "41 ff ca"),
				test(new Instruction(Opcode.DEC, R11), "dec r11", "49 ff cb"),
				test(new Instruction(Opcode.DEC, R11B), "dec r11b", "41 fe cb"),
				test(new Instruction(Opcode.DEC, R11D), "dec r11d", "41 ff cb"),
				test(new Instruction(Opcode.DEC, R12), "dec r12", "49 ff cc"),
				test(new Instruction(Opcode.DEC, R12B), "dec r12b", "41 fe cc"),
				test(new Instruction(Opcode.DEC, R12D), "dec r12d", "41 ff cc"),
				test(new Instruction(Opcode.DEC, R13), "dec r13", "49 ff cd"),
				test(new Instruction(Opcode.DEC, R13B), "dec r13b", "41 fe cd"),
				test(new Instruction(Opcode.DEC, R13D), "dec r13d", "41 ff cd"),
				test(new Instruction(Opcode.DEC, R14), "dec r14", "49 ff ce"),
				test(new Instruction(Opcode.DEC, R14B), "dec r14b", "41 fe ce"),
				test(new Instruction(Opcode.DEC, R14D), "dec r14d", "41 ff ce"),
				test(new Instruction(Opcode.DEC, R15), "dec r15", "49 ff cf"),
				test(new Instruction(Opcode.DEC, R15B), "dec r15b", "41 fe cf"),
				test(new Instruction(Opcode.DEC, R15D), "dec r15d", "41 ff cf"),
				test(new Instruction(Opcode.DEC, R8), "dec r8", "49 ff c8"),
				test(new Instruction(Opcode.DEC, R8B), "dec r8b", "41 fe c8"),
				test(new Instruction(Opcode.DEC, R8D), "dec r8d", "41 ff c8"),
				test(new Instruction(Opcode.DEC, R9), "dec r9", "49 ff c9"),
				test(new Instruction(Opcode.DEC, R9B), "dec r9b", "41 fe c9"),
				test(new Instruction(Opcode.DEC, R9D), "dec r9d", "41 ff c9"),
				test(new Instruction(Opcode.DEC, RAX), "dec rax", "48 ff c8"),
				test(new Instruction(Opcode.DEC, RBP), "dec rbp", "48 ff cd"),
				test(new Instruction(Opcode.DEC, RBX), "dec rbx", "48 ff cb"),
				test(new Instruction(Opcode.DEC, RCX), "dec rcx", "48 ff c9"),
				test(new Instruction(Opcode.DEC, RDI), "dec rdi", "48 ff cf"),
				test(new Instruction(Opcode.DEC, RDX), "dec rdx", "48 ff ca"),
				test(new Instruction(Opcode.DEC, RSI), "dec rsi", "48 ff ce"),
				test(new Instruction(Opcode.DEC, RSP), "dec rsp", "48 ff cc"),
				test(new Instruction(Opcode.DEC, SIL), "dec sil", "40 fe ce"),
				test(new Instruction(Opcode.DEC, SPL), "dec spl", "40 fe cc"),
				//
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build()),
						"dec BYTE PTR [rax+0x12345678]",
						"fe 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"dec BYTE PTR [rax]",
						"fe 08"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(RCX)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec BYTE PTR [rbx+rcx*2+0x12345678]",
						"fe 8c 4b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RBP)
										.index(RSI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec DWORD PTR [rbp+rsi*2+0x12345678]",
						"ff 8c 75 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.displacement(0x12345678)
										.build()),
						"dec DWORD PTR [rsp+0x12345678]",
						"ff 8c 24 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSP)
										.build()),
						"dec DWORD PTR [rsp]",
						"ff 0c 24"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(R8)
										.index(RDI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec QWORD PTR [r8+rdi*2+0x12345678]",
						"49 ff 8c 78 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.build()),
						"dec QWORD PTR [rax]",
						"48 ff 08"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RCX)
										.displacement(0x12345678)
										.build()),
						"dec QWORD PTR [rcx+0x12345678]",
						"48 ff 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.index(RAX)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec WORD PTR [rax*2+0x12345678]",
						"66 ff 0c 45 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RBP)
										.index(RAX)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec WORD PTR [rbp+rax*2+0x12345678]",
						"66 ff 8c 45 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.index(RBP)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec WORD PTR [rbp*2+0x12345678]",
						"66 ff 0c 6d 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.displacement(0x12345678)
										.build()),
						"dec WORD PTR [rax+0x12345678]",
						"66 ff 88 78 56 34 12"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(1)
										.build()),
						"dec WORD PTR [rax+rbx*1]",
						"66 ff 0c 18"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(1)
										.displacement((byte) 0)
										.build()),
						"dec WORD PTR [rax+rbx*1+0x0]",
						"66 ff 4c 18 00"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.build()),
						"dec WORD PTR [rax]",
						"66 ff 08"),
				test(
						new Instruction(
								Opcode.DEC,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RDX)
										.index(RDI)
										.scale(2)
										.displacement(0x12345678)
										.build()),
						"dec WORD PTR [rdx+rdi*2+0x12345678]",
						"66 ff 8c 7a 78 56 34 12"),
				//  Pshufd
				test(new Instruction(Opcode.PSHUFD, XMM0, XMM1, bimm), "pshufd xmm0,xmm1,0x12", "66 0f 70 c1 12"),
				//  Pshufw
				test(new Instruction(Opcode.PSHUFW, MM0, MM1, bimm), "pshufw mm0,mm1,0x12", "0f 70 c1 12"),
				//  Shufpd
				test(new Instruction(Opcode.SHUFPD, XMM0, XMM1, bimm), "shufpd xmm0,xmm1,0x12", "66 0f c6 c1 12"),
				//  Shufps
				test(new Instruction(Opcode.SHUFPS, XMM0, XMM1, bimm), "shufps xmm0,xmm1,0x12", "0f c6 c1 12"),
				//  Pxor
				test(new Instruction(Opcode.PXOR, XMM1, XMM15), "pxor xmm1,xmm15", "66 41 0f ef cf"),
				test(new Instruction(Opcode.PXOR, MM1, MM7), "pxor mm1,mm7", "0f ef cf"),
				test(
						new Instruction(
								Opcode.PXOR,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"pxor xmm4,XMMWORD PTR [rax+r11*4+0x12345678]",
						"66 42 0f ef a4 98 78 56 34 12"),
				test(new Instruction(Opcode.PXOR, XMM7, XMM7), "pxor xmm7,xmm7", "66 0f ef ff"),
				//  Por
				test(new Instruction(Opcode.POR, XMM1, XMM15), "por xmm1,xmm15", "66 41 0f eb cf"),
				test(
						new Instruction(
								Opcode.POR,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"por xmm4,XMMWORD PTR [rax+r11*4+0x12345678]",
						"66 42 0f eb a4 98 78 56 34 12"),
				test(new Instruction(Opcode.POR, XMM7, XMM7), "por xmm7,xmm7", "66 0f eb ff"),
				//  Pand
				test(new Instruction(Opcode.PAND, XMM1, XMM15), "pand xmm1,xmm15", "66 41 0f db cf"),
				test(
						new Instruction(
								Opcode.PAND,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"pand xmm4,XMMWORD PTR [rax+r11*4+0x12345678]",
						"66 42 0f db a4 98 78 56 34 12"),
				test(new Instruction(Opcode.PAND, XMM7, XMM7), "pand xmm7,xmm7", "66 0f db ff"),
				//  Paddq
				test(new Instruction(Opcode.PADDQ, XMM1, XMM15), "paddq xmm1,xmm15", "66 41 0f d4 cf"),
				test(
						new Instruction(
								Opcode.PADDQ,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"paddq xmm4,XMMWORD PTR [rax+r11*4+0x12345678]",
						"66 42 0f d4 a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PADDQ,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RDI)
										.build()),
						"paddq xmm4,XMMWORD PTR [rdi]",
						"66 0f d4 27"),
				test(new Instruction(Opcode.PADDQ, XMM7, XMM7), "paddq xmm7,xmm7", "66 0f d4 ff"),
				//  Psubq
				test(new Instruction(Opcode.PSUBQ, XMM1, XMM15), "psubq xmm1,xmm15", "66 41 0f fb cf"),
				test(
						new Instruction(
								Opcode.PSUBQ,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RAX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"psubq xmm4,XMMWORD PTR [rax+r11*4+0x12345678]",
						"66 42 0f fb a4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PSUBQ,
								XMM4,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RDI)
										.build()),
						"psubq xmm4,XMMWORD PTR [rdi]",
						"66 0f fb 27"),
				test(new Instruction(Opcode.PSUBQ, XMM7, XMM7), "psubq xmm7,xmm7", "66 0f fb ff"),
				// Psubb
				test(new Instruction(Opcode.PSUBB, XMM3, XMM14), "psubb xmm3,xmm14", "66 41 0f f8 de"),
				// Psubw
				test(new Instruction(Opcode.PSUBW, XMM3, XMM14), "psubw xmm3,xmm14", "66 41 0f f9 de"),
				// Psubd
				test(new Instruction(Opcode.PSUBD, XMM3, XMM14), "psubd xmm3,xmm14", "66 41 0f fa de"),
				//  Cvtsi2sd
				test(new Instruction(Opcode.CVTSI2SD, XMM2, RDI), "cvtsi2sd xmm2,rdi", "f2 48 0f 2a d7"),
				test(new Instruction(Opcode.CVTSI2SD, XMM8, EAX), "cvtsi2sd xmm8,eax", "f2 44 0f 2a c0"),
				//  Divsd
				test(new Instruction(Opcode.DIVSD, XMM0, XMM0), "divsd xmm0,xmm0", "f2 0f 5e c0"),
				test(new Instruction(Opcode.DIVSD, XMM8, XMM11), "divsd xmm8,xmm11", "f2 45 0f 5e c3"),
				//  Addsd
				test(new Instruction(Opcode.ADDSD, XMM0, XMM0), "addsd xmm0,xmm0", "f2 0f 58 c0"),
				test(new Instruction(Opcode.ADDSD, XMM8, XMM11), "addsd xmm8,xmm11", "f2 45 0f 58 c3"),
				//  Xorps
				test(new Instruction(Opcode.XORPS, XMM0, XMM0), "xorps xmm0,xmm0", "0f 57 c0"),
				test(new Instruction(Opcode.XORPS, XMM8, XMM11), "xorps xmm8,xmm11", "45 0f 57 c3"),
				//  Ucomisd
				test(
						new Instruction(
								Opcode.UCOMISD,
								XMM13,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RIP)
										.displacement(0x12345678)
										.build()),
						"ucomisd xmm13,QWORD PTR [rip+0x12345678]",
						"66 44 0f 2e 2d 78 56 34 12"),
				//  Ucomiss
				test(
						new Instruction(
								Opcode.UCOMISS,
								XMM13,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RIP)
										.displacement(0x12345678)
										.build()),
						"ucomiss xmm13,DWORD PTR [rip+0x12345678]",
						"44 0f 2e 2d 78 56 34 12"),
				//  BTx
				test(new Instruction(Opcode.BT, EDX, bimm), "bt edx,0x12", "0f ba e2 12"),
				test(new Instruction(Opcode.BT, EDX, ESI), "bt edx,esi", "0f a3 f2"),
				test(new Instruction(Opcode.BT, RDX, bimm), "bt rdx,0x12", "48 0f ba e2 12"),
				test(new Instruction(Opcode.BT, RDX, RDI), "bt rdx,rdi", "48 0f a3 fa"),
				test(new Instruction(Opcode.BTC, ECX, bimm), "btc ecx,0x12", "0f ba f9 12"),
				test(new Instruction(Opcode.BTC, ECX, R9D), "btc ecx,r9d", "44 0f bb c9"),
				test(new Instruction(Opcode.BTC, RCX, bimm), "btc rcx,0x12", "48 0f ba f9 12"),
				test(new Instruction(Opcode.BTC, RCX, R10), "btc rcx,r10", "4c 0f bb d1"),
				test(new Instruction(Opcode.BTR, EBX, bimm), "btr ebx,0x12", "0f ba f3 12"),
				test(new Instruction(Opcode.BTR, EBX, R11D), "btr ebx,r11d", "44 0f b3 db"),
				test(new Instruction(Opcode.BTR, RBX, bimm), "btr rbx,0x12", "48 0f ba f3 12"),
				test(new Instruction(Opcode.BTR, RBX, R12), "btr rbx,r12", "4c 0f b3 e3"),
				test(new Instruction(Opcode.BTS, EAX, bimm), "bts eax,0x12", "0f ba e8 12"),
				test(new Instruction(Opcode.BTS, EAX, R13D), "bts eax,r13d", "44 0f ab e8"),
				test(new Instruction(Opcode.BTS, RAX, bimm), "bts rax,0x12", "48 0f ba e8 12"),
				test(new Instruction(Opcode.BTS, RAX, R14), "bts rax,r14", "4c 0f ab f0"),
				//  Xgetbv
				test(new Instruction(Opcode.XGETBV), "xgetbv", "0f 01 d0"),
				//  Xchg
				test(new Instruction(Opcode.XCHG, AL, CL), "xchg al,cl", "86 c8"),
				test(new Instruction(Opcode.XCHG, BH, CL), "xchg bh,cl", "86 cf"),
				test(new Instruction(Opcode.XCHG, DI, AX), "xchg di,ax", "66 97"),
				test(new Instruction(Opcode.XCHG, EBP, EAX), "xchg ebp,eax", "95"),
				test(new Instruction(Opcode.XCHG, EBX, EAX), "xchg ebx,eax", "93"),
				test(new Instruction(Opcode.XCHG, EBX, R9D), "xchg ebx,r9d", "44 87 cb"),
				test(new Instruction(Opcode.XCHG, ECX, EAX), "xchg ecx,eax", "91"),
				test(new Instruction(Opcode.XCHG, EDI, EAX), "xchg edi,eax", "97"),
				test(new Instruction(Opcode.XCHG, EDX, EAX), "xchg edx,eax", "92"),
				test(new Instruction(Opcode.XCHG, ESI, EAX), "xchg esi,eax", "96"),
				test(new Instruction(Opcode.XCHG, ESP, EAX), "xchg esp,eax", "94"),
				test(new Instruction(Opcode.XCHG, R10, RAX), "xchg r10,rax", "49 92"),
				test(new Instruction(Opcode.XCHG, R10D, EAX), "xchg r10d,eax", "41 92"),
				test(new Instruction(Opcode.XCHG, R11, RAX), "xchg r11,rax", "49 93"),
				test(new Instruction(Opcode.XCHG, R11D, EAX), "xchg r11d,eax", "41 93"),
				test(new Instruction(Opcode.XCHG, R12, RAX), "xchg r12,rax", "49 94"),
				test(new Instruction(Opcode.XCHG, R12D, EAX), "xchg r12d,eax", "41 94"),
				test(new Instruction(Opcode.XCHG, R13, RAX), "xchg r13,rax", "49 95"),
				test(new Instruction(Opcode.XCHG, R13D, EAX), "xchg r13d,eax", "41 95"),
				test(new Instruction(Opcode.XCHG, R14, RAX), "xchg r14,rax", "49 96"),
				test(new Instruction(Opcode.XCHG, R14D, EAX), "xchg r14d,eax", "41 96"),
				test(new Instruction(Opcode.XCHG, R15, RAX), "xchg r15,rax", "49 97"),
				test(new Instruction(Opcode.XCHG, R15D, EAX), "xchg r15d,eax", "41 97"),
				test(new Instruction(Opcode.XCHG, R8, RAX), "xchg r8,rax", "49 90"),
				test(new Instruction(Opcode.XCHG, R8D, EAX), "xchg r8d,eax", "41 90"),
				test(new Instruction(Opcode.XCHG, R9, RAX), "xchg r9,rax", "49 91"),
				test(new Instruction(Opcode.XCHG, R9D, EAX), "xchg r9d,eax", "41 91"),
				test(new Instruction(Opcode.XCHG, RBP, RAX), "xchg rbp,rax", "48 95"),
				test(new Instruction(Opcode.XCHG, RBX, R9), "xchg rbx,r9", "4c 87 cb"),
				test(new Instruction(Opcode.XCHG, RBX, RAX), "xchg rbx,rax", "48 93"),
				test(new Instruction(Opcode.XCHG, RBX, RCX), "xchg rbx,rcx", "48 87 cb"),
				test(new Instruction(Opcode.XCHG, RCX, RAX), "xchg rcx,rax", "48 91"),
				test(new Instruction(Opcode.XCHG, RDI, RAX), "xchg rdi,rax", "48 97"),
				test(new Instruction(Opcode.XCHG, RDX, RAX), "xchg rdx,rax", "48 92"),
				test(new Instruction(Opcode.XCHG, RSI, RAX), "xchg rsi,rax", "48 96"),
				test(new Instruction(Opcode.XCHG, RSP, RAX), "xchg rsp,rax", "48 94"),
				test(new Instruction(Opcode.XCHG, SI, DI), "xchg si,di", "66 87 fe"),
				//
				test(
						new Instruction(
								Opcode.XCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(2)
										.displacement(0x12345678)
										.build(),
								AH),
						"xchg BYTE PTR [rax+rbx*2+0x12345678],ah",
						"86 a4 58 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(2)
										.displacement(0x12345678)
										.build(),
								AL),
						"xchg BYTE PTR [rax+rbx*2+0x12345678],al",
						"86 84 58 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XCHG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(2)
										.displacement(0x12345678)
										.build(),
								EAX),
						"xchg DWORD PTR [rax+rbx*2+0x12345678],eax",
						"87 84 58 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XCHG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(2)
										.displacement(0x12345678)
										.build(),
								RAX),
						"xchg QWORD PTR [rax+rbx*2+0x12345678],rax",
						"48 87 84 58 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XCHG,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(2)
										.displacement(0x12345678)
										.build(),
								AX),
						"xchg WORD PTR [rax+rbx*2+0x12345678],ax",
						"66 87 84 58 78 56 34 12"),
				//  Bswap
				test(new Instruction(Opcode.BSWAP, EAX), "bswap eax", "0f c8"),
				test(new Instruction(Opcode.BSWAP, EBP), "bswap ebp", "0f cd"),
				test(new Instruction(Opcode.BSWAP, EBX), "bswap ebx", "0f cb"),
				test(new Instruction(Opcode.BSWAP, ECX), "bswap ecx", "0f c9"),
				test(new Instruction(Opcode.BSWAP, EDI), "bswap edi", "0f cf"),
				test(new Instruction(Opcode.BSWAP, EDX), "bswap edx", "0f ca"),
				test(new Instruction(Opcode.BSWAP, ESI), "bswap esi", "0f ce"),
				test(new Instruction(Opcode.BSWAP, ESP), "bswap esp", "0f cc"),
				test(new Instruction(Opcode.BSWAP, R10), "bswap r10", "49 0f ca"),
				test(new Instruction(Opcode.BSWAP, R10D), "bswap r10d", "41 0f ca"),
				test(new Instruction(Opcode.BSWAP, R11), "bswap r11", "49 0f cb"),
				test(new Instruction(Opcode.BSWAP, R11D), "bswap r11d", "41 0f cb"),
				test(new Instruction(Opcode.BSWAP, R12), "bswap r12", "49 0f cc"),
				test(new Instruction(Opcode.BSWAP, R12D), "bswap r12d", "41 0f cc"),
				test(new Instruction(Opcode.BSWAP, R13), "bswap r13", "49 0f cd"),
				test(new Instruction(Opcode.BSWAP, R13D), "bswap r13d", "41 0f cd"),
				test(new Instruction(Opcode.BSWAP, R14), "bswap r14", "49 0f ce"),
				test(new Instruction(Opcode.BSWAP, R14D), "bswap r14d", "41 0f ce"),
				test(new Instruction(Opcode.BSWAP, R15), "bswap r15", "49 0f cf"),
				test(new Instruction(Opcode.BSWAP, R15D), "bswap r15d", "41 0f cf"),
				test(new Instruction(Opcode.BSWAP, R8), "bswap r8", "49 0f c8"),
				test(new Instruction(Opcode.BSWAP, R8D), "bswap r8d", "41 0f c8"),
				test(new Instruction(Opcode.BSWAP, R9), "bswap r9", "49 0f c9"),
				test(new Instruction(Opcode.BSWAP, R9D), "bswap r9d", "41 0f c9"),
				test(new Instruction(Opcode.BSWAP, RAX), "bswap rax", "48 0f c8"),
				test(new Instruction(Opcode.BSWAP, RBP), "bswap rbp", "48 0f cd"),
				test(new Instruction(Opcode.BSWAP, RBX), "bswap rbx", "48 0f cb"),
				test(new Instruction(Opcode.BSWAP, RCX), "bswap rcx", "48 0f c9"),
				test(new Instruction(Opcode.BSWAP, RDI), "bswap rdi", "48 0f cf"),
				test(new Instruction(Opcode.BSWAP, RDX), "bswap rdx", "48 0f ca"),
				test(new Instruction(Opcode.BSWAP, RSI), "bswap rsi", "48 0f ce"),
				test(new Instruction(Opcode.BSWAP, RSP), "bswap rsp", "48 0f cc"),
				//  Prefetch
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build()),
						"prefetchnta BYTE PTR [eax]",
						"67 0f 18 00"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EBX)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [ebx+r11d*4+0x12345678]",
						"67 42 0f 18 84 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [r9+r11*4+0x12345678]",
						"43 0f 18 84 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [r9+rcx*4+0x12345678]",
						"41 0f 18 84 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [r9d+ecx*4+0x12345678]",
						"67 41 0f 18 84 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [r9d+r11d*4+0x12345678]",
						"67 43 0f 18 84 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"prefetchnta BYTE PTR [rax]",
						"0f 18 00"),
				test(
						new Instruction(
								Opcode.PREFETCHNTA,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetchnta BYTE PTR [rbx+r11*4+0x12345678]",
						"42 0f 18 84 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build()),
						"prefetcht0 BYTE PTR [eax]",
						"67 0f 18 08"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EBX)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [ebx+r11d*4+0x12345678]",
						"67 42 0f 18 8c 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [r9+r11*4+0x12345678]",
						"43 0f 18 8c 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [r9+rcx*4+0x12345678]",
						"41 0f 18 8c 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [r9d+ecx*4+0x12345678]",
						"67 41 0f 18 8c 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [r9d+r11d*4+0x12345678]",
						"67 43 0f 18 8c 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"prefetcht0 BYTE PTR [rax]",
						"0f 18 08"),
				test(
						new Instruction(
								Opcode.PREFETCHT0,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht0 BYTE PTR [rbx+r11*4+0x12345678]",
						"42 0f 18 8c 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build()),
						"prefetcht1 BYTE PTR [eax]",
						"67 0f 18 10"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EBX)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [ebx+r11d*4+0x12345678]",
						"67 42 0f 18 94 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [r9+r11*4+0x12345678]",
						"43 0f 18 94 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [r9+rcx*4+0x12345678]",
						"41 0f 18 94 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [r9d+ecx*4+0x12345678]",
						"67 41 0f 18 94 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [r9d+r11d*4+0x12345678]",
						"67 43 0f 18 94 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"prefetcht1 BYTE PTR [rax]",
						"0f 18 10"),
				test(
						new Instruction(
								Opcode.PREFETCHT1,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht1 BYTE PTR [rbx+r11*4+0x12345678]",
						"42 0f 18 94 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EAX)
										.build()),
						"prefetcht2 BYTE PTR [eax]",
						"67 0f 18 18"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(EBX)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [ebx+r11d*4+0x12345678]",
						"67 42 0f 18 9c 9b 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [r9+r11*4+0x12345678]",
						"43 0f 18 9c 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9)
										.index(RCX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [r9+rcx*4+0x12345678]",
						"41 0f 18 9c 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(ECX)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [r9d+ecx*4+0x12345678]",
						"67 41 0f 18 9c 89 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(R9D)
										.index(R11D)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [r9d+r11d*4+0x12345678]",
						"67 43 0f 18 9c 99 78 56 34 12"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.build()),
						"prefetcht2 BYTE PTR [rax]",
						"0f 18 18"),
				test(
						new Instruction(
								Opcode.PREFETCHT2,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RBX)
										.index(R11)
										.scale(4)
										.displacement(0x12345678)
										.build()),
						"prefetcht2 BYTE PTR [rbx+r11*4+0x12345678]",
						"42 0f 18 9c 9b 78 56 34 12"),
				//  Cmpxchg
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								DH),
						"cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh",
						"0f b0 b4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSI)
										.build(),
								BPL),
						"cmpxchg BYTE PTR [rsi],bpl",
						"40 0f b0 2e"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R10D),
						"cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d",
						"44 0f b1 94 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build(),
								ECX),
						"cmpxchg DWORD PTR [rsi],ecx",
						"0f b1 0e"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RDI),
						"cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi",
						"48 0f b1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build(),
								R9),
						"cmpxchg QWORD PTR [rsi],r9",
						"4c 0f b1 0e"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R15W),
						"cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w",
						"66 44 0f b1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSI)
										.build(),
								DX),
						"cmpxchg WORD PTR [rsi],dx",
						"66 0f b1 16"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								DH),
						"lock cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh",
						"f0 0f b0 b4 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSI)
										.build(),
								BPL),
						"lock cmpxchg BYTE PTR [rsi],bpl",
						"f0 40 0f b0 2e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R10D),
						"lock cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d",
						"f0 44 0f b1 94 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build(),
								ECX),
						"lock cmpxchg DWORD PTR [rsi],ecx",
						"f0 0f b1 0e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RDI),
						"lock cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi",
						"f0 48 0f b1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build(),
								R9),
						"lock cmpxchg QWORD PTR [rsi],r9",
						"f0 4c 0f b1 0e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R15W),
						"lock cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w",
						"66 f0 44 0f b1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.CMPXCHG,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSI)
										.build(),
								DX),
						"lock cmpxchg WORD PTR [rsi],dx",
						"66 f0 0f b1 16"),
				//  Xadd
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								DH),
						"lock xadd BYTE PTR [rax+rbx*4+0x12345678],dh",
						"f0 0f c0 b4 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSI)
										.build(),
								BPL),
						"lock xadd BYTE PTR [rsi],bpl",
						"f0 40 0f c0 2e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R10D),
						"lock xadd DWORD PTR [rax+rbx*4+0x12345678],r10d",
						"f0 44 0f c1 94 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build(),
								ECX),
						"lock xadd DWORD PTR [rsi],ecx",
						"f0 0f c1 0e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RDI),
						"lock xadd QWORD PTR [rax+rbx*4+0x12345678],rdi",
						"f0 48 0f c1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build(),
								R9),
						"lock xadd QWORD PTR [rsi],r9",
						"f0 4c 0f c1 0e"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R15W),
						"lock xadd WORD PTR [rax+rbx*4+0x12345678],r15w",
						"66 f0 44 0f c1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								InstructionPrefix.LOCK,
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSI)
										.build(),
								DX),
						"lock xadd WORD PTR [rsi],dx",
						"66 f0 0f c1 16"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								DH),
						"xadd BYTE PTR [rax+rbx*4+0x12345678],dh",
						"0f c0 b4 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(BYTE_PTR)
										.base(RSI)
										.build(),
								BPL),
						"xadd BYTE PTR [rsi],bpl",
						"40 0f c0 2e"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R10D),
						"xadd DWORD PTR [rax+rbx*4+0x12345678],r10d",
						"44 0f c1 94 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RSI)
										.build(),
								ECX),
						"xadd DWORD PTR [rsi],ecx",
						"0f c1 0e"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								RDI),
						"xadd QWORD PTR [rax+rbx*4+0x12345678],rdi",
						"48 0f c1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.build(),
								R9),
						"xadd QWORD PTR [rsi],r9",
						"4c 0f c1 0e"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(4)
										.displacement(0x12345678)
										.build(),
								R15W),
						"xadd WORD PTR [rax+rbx*4+0x12345678],r15w",
						"66 44 0f c1 bc 98 78 56 34 12"),
				test(
						new Instruction(
								Opcode.XADD,
								IndirectOperand.builder()
										.pointer(WORD_PTR)
										.base(RSI)
										.build(),
								DX),
						"xadd WORD PTR [rsi],dx",
						"66 0f c1 16"),
				// Pcmpeqb
				test(new Instruction(Opcode.PCMPEQB, XMM3, XMM11), "pcmpeqb xmm3,xmm11", "66 41 0f 74 db"),
				test(new Instruction(Opcode.PCMPEQB, MM4, MM6), "pcmpeqb mm4,mm6", "0f 74 e6"),
				// Pcmpeqw
				test(new Instruction(Opcode.PCMPEQW, XMM7, XMM12), "pcmpeqw xmm7,xmm12", "66 41 0f 75 fc"),
				test(new Instruction(Opcode.PCMPEQW, MM3, MM5), "pcmpeqw mm3,mm5", "0f 75 dd"),
				//  Pcmpeqd
				test(new Instruction(Opcode.PCMPEQD, XMM0, XMM0), "pcmpeqd xmm0,xmm0", "66 0f 76 c0"),
				test(new Instruction(Opcode.PCMPEQD, XMM3, XMM11), "pcmpeqd xmm3,xmm11", "66 41 0f 76 db"),
				test(new Instruction(Opcode.PCMPEQD, MM1, MM2), "pcmpeqd mm1,mm2", "0f 76 ca"),
				//  Rdrand
				test(new Instruction(Opcode.RDRAND, AX), "rdrand ax", "66 0f c7 f0"),
				test(new Instruction(Opcode.RDRAND, EAX), "rdrand eax", "0f c7 f0"),
				test(new Instruction(Opcode.RDRAND, R11), "rdrand r11", "49 0f c7 f3"),
				test(new Instruction(Opcode.RDRAND, R12D), "rdrand r12d", "41 0f c7 f4"),
				test(new Instruction(Opcode.RDRAND, R13W), "rdrand r13w", "66 41 0f c7 f5"),
				test(new Instruction(Opcode.RDRAND, RAX), "rdrand rax", "48 0f c7 f0"),
				//  Rdseed
				test(new Instruction(Opcode.RDSEED, AX), "rdseed ax", "66 0f c7 f8"),
				test(new Instruction(Opcode.RDSEED, EAX), "rdseed eax", "0f c7 f8"),
				test(new Instruction(Opcode.RDSEED, R11), "rdseed r11", "49 0f c7 fb"),
				test(new Instruction(Opcode.RDSEED, R12D), "rdseed r12d", "41 0f c7 fc"),
				test(new Instruction(Opcode.RDSEED, R13W), "rdseed r13w", "66 41 0f c7 fd"),
				test(new Instruction(Opcode.RDSEED, RAX), "rdseed rax", "48 0f c7 f8"),
				//  Rdsspq
				test(new Instruction(Opcode.RDSSPQ, R11), "rdsspq r11", "f3 49 0f 1e cb"),
				test(new Instruction(Opcode.RDSSPQ, RAX), "rdsspq rax", "f3 48 0f 1e c8"),
				//  Incsspq
				test(new Instruction(Opcode.INCSSPQ, R11), "incsspq r11", "f3 49 0f ae eb"),
				test(new Instruction(Opcode.INCSSPQ, RAX), "incsspq rax", "f3 48 0f ae e8"),
				//  Lahf
				test(new Instruction(Opcode.LAHF), "lahf", "9f"),
				//  Sahf
				test(new Instruction(Opcode.SAHF), "sahf", "9e"),
				// Syscall
				test(new Instruction(Opcode.SYSCALL), "syscall", "0f 05"),
				// Bsr
				test(
						new Instruction(
								Opcode.BSR,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RAX)
										.build()),
						"bsr eax,DWORD PTR [rax]",
						"0f bd 00"),
				// Bsf
				test(new Instruction(Opcode.BSF, RDX, RDX), "bsf rdx,rdx", "48 0f bc d2"),
				test(new Instruction(Opcode.BSF, ECX, EDX), "bsf ecx,edx", "0f bc ca"),
				// Ror
				test(new Instruction(Opcode.ROR, EDI, new Immediate((byte) 0)), "ror edi,0x00", "c1 cf 00"),
				test(new Instruction(Opcode.ROR, R15, new Immediate((byte) 0x11)), "ror r15,0x11", "49 c1 cf 11"),
				// Rol
				test(new Instruction(Opcode.ROL, EDI, new Immediate((byte) 0)), "rol edi,0x00", "c1 c7 00"),
				test(new Instruction(Opcode.ROL, RDX, new Immediate((byte) 0x11)), "rol rdx,0x11", "48 c1 c2 11"),
				// Rcr
				test(new Instruction(Opcode.RCR, EDI, new Immediate((byte) 0)), "rcr edi,0x00", "c1 df 00"),
				// Rcl
				test(new Instruction(Opcode.RCL, EDI, new Immediate((byte) 0)), "rcl edi,0x00", "c1 d7 00"),
				// Pmovmskb
				test(new Instruction(Opcode.PMOVMSKB, EDI, XMM6), "pmovmskb edi,xmm6", "66 0f d7 fe"),
				// Pslldq
				test(
						new Instruction(Opcode.PSLLDQ, XMM2, new Immediate((byte) 0x0f)),
						"pslldq xmm2,0x0f",
						"66 0f 73 fa 0f"),
				// Psrldq
				test(
						new Instruction(Opcode.PSRLDQ, XMM3, new Immediate((byte) 0x1)),
						"psrldq xmm3,0x01",
						"66 0f 73 db 01"),
				// Pminub
				test(new Instruction(Opcode.PMINUB, XMM0, XMM1), "pminub xmm0,xmm1", "66 0f da c1"),
				// Palignr
				test(
						new Instruction(Opcode.PALIGNR, XMM2, XMM3, new Immediate((byte) 0x1)),
						"palignr xmm2,xmm3,0x01",
						"66 0f 3a 0f d3 01"),
				test(
						new Instruction(
								Opcode.PALIGNR,
								XMM0,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSI)
										.displacement((byte) 0x20)
										.build(),
								new Immediate((byte) 0x0f)),
						"palignr xmm0,XMMWORD PTR [rsi+0x20],0x0f",
						"66 0f 3a 0f 46 20 0f"),
				// Pcmpeqb
				test(
						new Instruction(
								Opcode.PCMPEQB,
								XMM0,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RDI)
										.build()),
						"pcmpeqb xmm0,XMMWORD PTR [rdi]",
						"66 0f 74 07"),
				// Vpxor
				test(new Instruction(Opcode.VPXOR, XMM5, XMM6, XMM7), "vpxor xmm5,xmm6,xmm7", "c5 c9 ef ef"),
				test(new Instruction(Opcode.VPXOR, XMM12, XMM6, XMM7), "vpxor xmm12,xmm6,xmm7", "c5 49 ef e7"),
				test(new Instruction(Opcode.VPXOR, XMM2, XMM13, XMM7), "vpxor xmm2,xmm13,xmm7", "c5 91 ef d7"),
				// Pextrw
				test(
						new Instruction(Opcode.PEXTRW, EDI, MM6, new Immediate((byte) 0x6f)),
						"pextrw edi,mm6,0x6f",
						"0f c5 fe 6f"),
				// Vmovdqu
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM1,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RDI)
										.build()),
						"vmovdqu ymm1,YMMWORD PTR [rdi]",
						"c5 fe 6f 0f"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM11,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RDI)
										.build()),
						"vmovdqu ymm11,YMMWORD PTR [rdi]",
						"c5 7e 6f 1f"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM3,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RDI)
										.build()),
						"vmovdqu ymm3,YMMWORD PTR [rdi]",
						"c5 fe 6f 1f"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM2,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RAX)
										.index(R10)
										.scale(1)
										.build()),
						"vmovdqu ymm2,YMMWORD PTR [rax+r10*1]",
						"c4 a1 7e 6f 14 10"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM2,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(R9)
										.index(RBX)
										.scale(1)
										.build()),
						"vmovdqu ymm2,YMMWORD PTR [r9+rbx*1]",
						"c4 c1 7e 6f 14 19"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM11,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RAX)
										.index(RBX)
										.scale(1)
										.build()),
						"vmovdqu ymm11,YMMWORD PTR [rax+rbx*1]",
						"c5 7e 6f 1c 18"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								YMM3,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RAX)
										.index(R10)
										.scale(1)
										.displacement((byte) 0x20)
										.build()),
						"vmovdqu ymm3,YMMWORD PTR [rax+r10*1+0x20]",
						"c4 a1 7e 6f 5c 10 20"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RDI)
										.build(),
								YMM0),
						"vmovdqu YMMWORD PTR [rdi],ymm0",
						"c5 fe 7f 07"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RCX)
										.displacement((byte) -0x40)
										.build(),
								YMM2),
						"vmovdqu YMMWORD PTR [rcx-0x40],ymm2",
						"c5 fe 7f 51 c0"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(R9)
										.displacement((byte) -0x40)
										.build(),
								YMM2),
						"vmovdqu YMMWORD PTR [r9-0x40],ymm2",
						"c4 c1 7e 7f 51 c0"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(R9)
										.displacement((byte) -0x40)
										.build(),
								YMM10),
						"vmovdqu YMMWORD PTR [r9-0x40],ymm10",
						"c4 41 7e 7f 51 c0"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RCX)
										.index(R10)
										.scale(1)
										.displacement((byte) -0x40)
										.build(),
								YMM2),
						"vmovdqu YMMWORD PTR [rcx+r10*1-0x40],ymm2",
						"c4 a1 7e 7f 54 11 c0"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RCX)
										.index(RDX)
										.scale(1)
										.displacement((byte) -0x40)
										.build(),
								YMM10),
						"vmovdqu YMMWORD PTR [rcx+rdx*1-0x40],ymm10",
						"c5 fe 7f 54 11 c0"),
				test(
						new Instruction(
								Opcode.VMOVDQU,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RCX)
										.index(R10)
										.scale(1)
										.displacement((byte) -0x40)
										.build(),
								YMM10),
						"vmovdqu YMMWORD PTR [rcx+r10*1-0x40],ymm10",
						"c4 21 7e 7f 54 11 c0"),
				// Vpminub
				test(new Instruction(Opcode.VPMINUB, YMM0, YMM0, YMM1), "vpminub ymm0,ymm0,ymm1", "c5 fd da c1"),
				// Vpmovmskb
				test(new Instruction(Opcode.VPMOVMSKB, ECX, YMM0), "vpmovmskb ecx,ymm0", "c5 fd d7 c8"),
				// Vpcmpeqb
				test(
						new Instruction(
								Opcode.VPCMPEQB,
								YMM3,
								YMM6,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RSI)
										.displacement((byte) 0x20)
										.build()),
						"vpcmpeqb ymm3,ymm6,YMMWORD PTR [rsi+0x20]",
						"c5 cd 74 5e 20"),
				test(
						new Instruction(
								Opcode.VPCMPEQB,
								YMM1,
								YMM3,
								IndirectOperand.builder()
										.pointer(YMMWORD_PTR)
										.base(RDX)
										.index(R10)
										.scale(1)
										.displacement((byte) 0x20)
										.build()),
						"vpcmpeqb ymm1,ymm3,YMMWORD PTR [rdx+r10*1+0x20]",
						"c4 a1 65 74 4c 12 20"),
				// Vzeroall
				test(new Instruction(Opcode.VZEROALL), "vzeroall", "c5 fc 77"),
				// Vmovq
				test(
						new Instruction(
								Opcode.VMOVQ,
								XMM0,
								IndirectOperand.builder()
										.pointer(QWORD_PTR)
										.base(RSI)
										.index(RDX)
										.scale(1)
										.build()),
						"vmovq xmm0,QWORD PTR [rsi+rdx*1]",
						"c5 fa 7e 04 16"),
				// Vmovd
				test(
						new Instruction(
								Opcode.VMOVD,
								XMM1,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.index(RDX)
										.scale(1)
										.build()),
						"vmovd xmm1,DWORD PTR [rdi+rdx*1]",
						"c5 f9 6e 0c 17"),
				// Pcmpistri
				test(
						new Instruction(Opcode.PCMPISTRI, XMM0, XMM1, new Immediate((byte) 0x1a)),
						"pcmpistri xmm0,xmm1,0x1a",
						"66 0f 3a 63 c1 1a"),
				test(
						new Instruction(
								Opcode.PCMPISTRI,
								XMM0,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSI)
										.index(RDX)
										.scale(1)
										.build(),
								new Immediate((byte) 0x1a)),
						"pcmpistri xmm0,XMMWORD PTR [rsi+rdx*1],0x1a",
						"66 0f 3a 63 04 16 1a"),
				// Punpcklbw
				test(new Instruction(Opcode.PUNPCKLBW, XMM1, XMM1), "punpcklbw xmm1,xmm1", "66 0f 60 c9"),
				// Pmaxub
				test(new Instruction(Opcode.PMAXUB, XMM3, XMM0), "pmaxub xmm3,xmm0", "66 0f de d8"),
				// Vpbroadcastb
				test(new Instruction(Opcode.VPBROADCASTB, YMM2, XMM1), "vpbroadcastb ymm2,xmm1", "c4 e2 7d 78 d1"),
				test(new Instruction(Opcode.VPBROADCASTB, YMM2, XMM9), "vpbroadcastb ymm2,xmm9", "c4 c2 7d 78 d1"),
				test(new Instruction(Opcode.VPBROADCASTB, YMM2, XMM8), "vpbroadcastb ymm2,xmm8", "c4 c2 7d 78 d0"),
				test(new Instruction(Opcode.VPBROADCASTB, YMM10, XMM8), "vpbroadcastb ymm10,xmm8", "c4 42 7d 78 d0"),
				test(new Instruction(Opcode.VPBROADCASTB, YMM0, XMM4), "vpbroadcastb ymm0,xmm4", "c4 e2 7d 78 c4"),
				test(new Instruction(Opcode.VPBROADCASTB, YMM8, XMM4), "vpbroadcastb ymm8,xmm4", "c4 62 7d 78 c4"),
				// Sarx
				test(new Instruction(Opcode.SARX, EAX, EAX, ECX), "sarx eax,eax,ecx", "c4 e2 72 f7 c0"),
				// Vpor
				test(new Instruction(Opcode.VPOR, YMM5, YMM2, YMM1), "vpor ymm5,ymm2,ymm1", "c5 ed eb e9"),
				// Vpand
				test(new Instruction(Opcode.VPAND, YMM5, YMM2, YMM1), "vpand ymm5,ymm2,ymm1", "c5 ed db e9"),
				// Bzhi
				test(new Instruction(Opcode.BZHI, EDX, EAX, EDX), "bzhi edx,eax,edx", "c4 e2 68 f5 d0"),
				// Movbe
				test(
						new Instruction(
								Opcode.MOVBE,
								EAX,
								IndirectOperand.builder()
										.pointer(DWORD_PTR)
										.base(RDI)
										.build()),
						"movbe eax,DWORD PTR [rdi]",
						"0f 38 f0 07"),
				// Movntdq
				test(
						new Instruction(
								Opcode.MOVNTDQ,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RDI)
										.build(),
								XMM1),
						"movntdq XMMWORD PTR [rdi],xmm1",
						"66 0f e7 0f"),
				// Sfence
				test(new Instruction(Opcode.SFENCE), "sfence", "0f ae f8"),
				// Lddqu
				test(
						new Instruction(
								Opcode.LDDQU,
								XMM0,
								IndirectOperand.builder()
										.pointer(XMMWORD_PTR)
										.base(RSI)
										.displacement((byte) -0x80)
										.build()),
						"lddqu xmm0,[rsi-0x80]",
						"f2 0f f0 46 80"),
				// Vmovups
				test(
						new Instruction(
								Opcode.VMOVUPS,
								ZMM0,
								IndirectOperand.builder()
										.pointer(ZMMWORD_PTR)
										.base(RSI)
										.build()),
						"vmovups zmm0,ZMMWORD PTR [rsi]",
						"62 f1 7c 48 10 06"),
				test(
						new Instruction(
								Opcode.VMOVUPS,
								IndirectOperand.builder()
										.pointer(ZMMWORD_PTR)
										.base(RDI)
										.build(),
								ZMM0),
						"vmovups ZMMWORD PTR [rdi],zmm0",
						"62 f1 7c 48 11 07"));
	}

	//
	//  To have a reference which is a bit more usable than the Intel
	//  Software Developer Manual, you can use this:
	//  https://defuse.ca/online-x86-assembler.htm
	//
	protected static final List<X64EncodingTestCase> X64_ENCODINGS = Stream.of(
					nop(), mov(), movsxd(), cmp(), call(), jump(), cmov(), lea(), movzx(), movsx(), push(), pop(),
					others())
			.flatMap(Collection::stream)
			.toList();

	private static String asString(final byte[] v) {
		return IntStream.range(0, v.length)
				.mapToObj(i -> String.format("0x%02x", v[i]))
				.collect(Collectors.joining(" "));
	}

	static {
		// Check that there are no duplicates
		final Set<Instruction> inst = new HashSet<>();
		final Set<String> is = new HashSet<>();
		final Set<List<Byte>> hex = new HashSet<>();
		for (final X64EncodingTestCase t : X64_ENCODINGS) {
			if (inst.contains(t.instruction())) {
				throw new IllegalArgumentException(
						String.format("Duplicate instruction in test cases: '%s'.", t.instruction()));
			}
			inst.add(t.instruction());

			if (is.contains(t.intelSyntax())) {
				throw new IllegalArgumentException(
						String.format("Duplicate intel syntax in test cases: '%s'.", t.intelSyntax()));
			}
			is.add(t.intelSyntax());

			final int n = t.hex().length;
			final List<Byte> b = IntStream.range(0, n).mapToObj(i -> t.hex()[i]).toList();
			if (hex.contains(b)) {
				throw new IllegalArgumentException(
						String.format("Duplicate hex representation in test cases: '%s'.", asString(t.hex())));
			}
			hex.add(b);
		}
	}
}
