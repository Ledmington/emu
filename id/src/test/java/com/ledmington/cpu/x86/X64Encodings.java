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
import static com.ledmington.cpu.x86.Register16.AX;
import static com.ledmington.cpu.x86.Register16.CS;
import static com.ledmington.cpu.x86.Register16.CX;
import static com.ledmington.cpu.x86.Register16.DX;
import static com.ledmington.cpu.x86.Register16.R10W;
import static com.ledmington.cpu.x86.Register16.R11W;
import static com.ledmington.cpu.x86.Register16.R13W;
import static com.ledmington.cpu.x86.Register16.R14W;
import static com.ledmington.cpu.x86.Register16.R8W;
import static com.ledmington.cpu.x86.Register16.SP;
import static com.ledmington.cpu.x86.Register32.EAX;
import static com.ledmington.cpu.x86.Register32.EBP;
import static com.ledmington.cpu.x86.Register32.EBX;
import static com.ledmington.cpu.x86.Register32.ECX;
import static com.ledmington.cpu.x86.Register32.EDI;
import static com.ledmington.cpu.x86.Register32.EDX;
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
import static com.ledmington.cpu.x86.Register64.RSI;
import static com.ledmington.cpu.x86.Register64.RSP;
import static com.ledmington.cpu.x86.Register8.AL;
import static com.ledmington.cpu.x86.Register8.DH;
import static com.ledmington.cpu.x86.Register8.R8B;
import static com.ledmington.cpu.x86.Register8.R9B;

import java.util.List;

import com.ledmington.utils.BitUtils;

public sealed class X64Encodings permits TestDecoding, TestDecodeIncompleteInstruction {

	protected record X64EncodingTestCase(Instruction instruction, String intelSyntax, byte[] hex) {}

	private static X64EncodingTestCase test(final Instruction instruction, final String intelSyntax, final String hex) {
		final String[] splitted = hex.strip().split(" ");
		final byte[] code = new byte[splitted.length];
		for (int i = 0; i < splitted.length; i++) {
			code[i] = BitUtils.asByte(Integer.parseInt(splitted[i], 16));
		}
		return new X64EncodingTestCase(instruction, intelSyntax, code);
	}

	protected static final List<X64EncodingTestCase> X64_ENCODINGS = List.of(
			//
			//  To have a reference which is a bit more usable than the Intel
			//  Software Developer Manual, you can use this:
			//  https://defuse.ca/online-x86-assembler.htm
			//
			//  No-op
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
									.index(EAX)
									.build()),
					"nop DWORD PTR [eax]",
					"67 0f 1f 00"),
			test(
					new Instruction(
							Opcode.NOP,
							IndirectOperand.builder()
									.pointer(DWORD_PTR)
									.index(RAX)
									.build()),
					"nop DWORD PTR [rax]",
					"0f 1f 00"),
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
									.index(EAX)
									.build()),
					"nop QWORD PTR [eax]",
					"67 48 0f 1f 00"),
			test(
					new Instruction(
							Opcode.NOP,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(RAX)
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
									.index(EAX)
									.build()),
					"nop WORD PTR [eax]",
					"67 66 0f 1f 00"),
			test(
					new Instruction(
							Opcode.NOP,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(RAX)
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
									.base(RBX)
									.index(new SegmentRegister(CS, R12))
									.scale(4)
									.displacement(0x12345678)
									.build()),
					"nop WORD PTR cs:[rbx+r12*4+0x12345678]",
					"2e 66 42 0f 1f 84 a3 78 56 34 12"),
			//  Call
			//  The output of these instructions is different from what you can see from other tools such as objdump
			//  because here we keep the addition to the instruction pointer implicit.
			//  In reality, it would look like 'call rip+0x....'
			test(new Instruction(Opcode.CALL, new Immediate(0x12345678)), "call 0x12345678", "e8 78 56 34 12"),
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
									.index(EBX)
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
									.index(RSP)
									.build()),
					"call DWORD PTR [rsp]",
					"66 ff 1c 24"),
			test(
					new Instruction(
							Opcode.CALL,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(EAX)
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
									.index(RDX)
									.build()),
					"call QWORD PTR [rdx]",
					"ff 12"),
			test(
					new Instruction(
							Opcode.CALL,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(ECX)
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
									.index(RSI)
									.build()),
					"call WORD PTR [rsi]",
					"66 ff 16"),
			//  Cdq
			test(new Instruction(Opcode.CDQ), "cdq", "99"),
			//  Cwde
			test(new Instruction(Opcode.CWDE), "cwde", "98"),
			//  Cdqe
			test(new Instruction(Opcode.CDQE), "cdqe", "48 98"),
			//
			// ## Jumps
			//  The output of these instructions is different from what you can see from other tools such as objdump
			//  because here we keep the addition to the instruction pointer implicit.
			//  In reality, it would look like 'jXX rip+0x....'
			//  Ja
			test(new Instruction(Opcode.JA, new Immediate((byte) 0x12)), "ja 0x12", "77 12"),
			test(new Instruction(Opcode.JA, new Immediate(0x12345678)), "ja 0x12345678", "0f 87 78 56 34 12"),
			//  Jae
			test(new Instruction(Opcode.JAE, new Immediate((byte) 0x12)), "jae 0x12", "73 12"),
			test(new Instruction(Opcode.JAE, new Immediate(0x12345678)), "jae 0x12345678", "0f 83 78 56 34 12"),
			//  Jb
			test(new Instruction(Opcode.JB, new Immediate((byte) 0x12)), "jb 0x12", "72 12"),
			test(new Instruction(Opcode.JB, new Immediate(0x12345678)), "jb 0x12345678", "0f 82 78 56 34 12"),
			//  Jbe
			test(new Instruction(Opcode.JBE, new Immediate((byte) 0x12)), "jbe 0x12", "76 12"),
			test(new Instruction(Opcode.JBE, new Immediate(0x12345678)), "jbe 0x12345678", "0f 86 78 56 34 12"),
			//  Jg
			test(new Instruction(Opcode.JG, new Immediate((byte) 0x12)), "jg 0x12", "7f 12"),
			test(new Instruction(Opcode.JG, new Immediate(0x12345678)), "jg 0x12345678", "0f 8f 78 56 34 12"),
			//  Je
			test(new Instruction(Opcode.JE, new Immediate((byte) 0x12)), "je 0x12", "74 12"),
			test(new Instruction(Opcode.JE, new Immediate(0x12345678)), "je 0x12345678", "0f 84 78 56 34 12"),
			//  Jl
			test(new Instruction(Opcode.JL, new Immediate((byte) 0x12)), "jl 0x12", "7c 12"),
			test(new Instruction(Opcode.JL, new Immediate(0x12345678)), "jl 0x12345678", "0f 8c 78 56 34 12"),
			//  Jle
			test(new Instruction(Opcode.JLE, new Immediate((byte) 0x12)), "jle 0x12", "7e 12"),
			test(new Instruction(Opcode.JLE, new Immediate(0x12345678)), "jle 0x12345678", "0f 8e 78 56 34 12"),
			//  Jge
			test(new Instruction(Opcode.JGE, new Immediate((byte) 0x12)), "jge 0x12", "7d 12"),
			test(new Instruction(Opcode.JGE, new Immediate(0x12345678)), "jge 0x12345678", "0f 8d 78 56 34 12"),
			//  Jne
			test(new Instruction(Opcode.JNE, new Immediate((byte) 0x12)), "jne 0x12", "75 12"),
			test(new Instruction(Opcode.JNE, new Immediate(0x12345678)), "jne 0x12345678", "0f 85 78 56 34 12"),
			//  Jns
			test(new Instruction(Opcode.JNS, new Immediate((byte) 0x12)), "jns 0x12", "79 12"),
			test(new Instruction(Opcode.JNS, new Immediate(0x12345678)), "jns 0x12345678", "0f 89 78 56 34 12"),
			//  Js
			test(new Instruction(Opcode.JS, new Immediate((byte) 0x12)), "js 0x12", "78 12"),
			test(new Instruction(Opcode.JS, new Immediate(0x12345678)), "js 0x12345678", "0f 88 78 56 34 12"),
			//  Jp
			test(new Instruction(Opcode.JP, new Immediate((byte) 0x12)), "jp 0x12", "7a 12"),
			test(new Instruction(Opcode.JP, new Immediate(0x12345678)), "jp 0x12345678", "0f 8a 78 56 34 12"),
			//  Jmp
			test(new Instruction(Opcode.JMP, new Immediate((byte) 0x12)), "jmp 0x12", "eb 12"),
			test(new Instruction(Opcode.JMP, new Immediate(0x12345678)), "jmp 0x12345678", "e9 78 56 34 12"),
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
									.index(R11)
									.build()),
					"jmp DWORD PTR [r11]",
					"66 41 ff 2b"),
			test(
					new Instruction(
							Opcode.JMP,
							IndirectOperand.builder()
									.pointer(DWORD_PTR)
									.index(R11D)
									.build()),
					"jmp DWORD PTR [r11d]",
					"67 66 41 ff 2b"),
			test(
					new Instruction(
							Opcode.JMP,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(R11)
									.build()),
					"jmp QWORD PTR [r11]",
					"41 ff 23"),
			test(
					new Instruction(
							Opcode.JMP,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(R11D)
									.build()),
					"jmp QWORD PTR [r11d]",
					"67 41 ff 23"),
			test(
					new Instruction(
							Opcode.JMP,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(R11)
									.build()),
					"jmp WORD PTR [r11]",
					"66 41 ff 23"),
			test(
					new Instruction(
							Opcode.JMP,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(R11D)
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
					"66 ff a4 88 78 56 34 12"),
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
			test(new Instruction(Opcode.CMOVE, R15, RCX), "cmove r15,rcx", "4c 0f 44 f9"),
			test(new Instruction(Opcode.CMOVE, RCX, R15), "cmove rcx,r15", "49 0f 44 cf"),
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
			test(new Instruction(Opcode.CMOVB, R15, RCX), "cmovb r15,rcx", "4c 0f 42 f9"),
			test(new Instruction(Opcode.CMOVB, RCX, R15), "cmovb rcx,r15", "49 0f 42 cf"),
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
			test(new Instruction(Opcode.CMOVNE, R15, RDX), "cmovne r15,rdx", "4c 0f 45 fa"),
			test(new Instruction(Opcode.CMOVNE, RDX, R15), "cmovne rdx,r15", "49 0f 45 d7"),
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
			test(new Instruction(Opcode.CMOVG, R15, RDX), "cmovg r15,rdx", "4c 0f 4f fa"),
			test(new Instruction(Opcode.CMOVG, RDX, R15), "cmovg rdx,r15", "49 0f 4f d7"),
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
			//  Cmp
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(BYTE_PTR)
									.index(EAX)
									.build(),
							DH),
					"cmp BYTE PTR [eax],dh",
					"67 38 30"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(BYTE_PTR)
									.index(EDI)
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
									.index(RDI)
									.build(),
							new Immediate((byte) 0x77)),
					"cmp BYTE PTR [rdi],0x77",
					"80 3f 77"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(DWORD_PTR)
									.index(EBP)
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
									.index(EDI)
									.build(),
							new Immediate(0x12345678)),
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
									.index(RBP)
									.displacement(-0xe8)
									.build(),
							R15D),
					"cmp DWORD PTR [rbp-0xe8],r15d",
					"44 39 bd 18 ff ff ff"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(DWORD_PTR)
									.index(RDI)
									.build(),
							new Immediate(0x12345678)),
					"cmp DWORD PTR [rdi],0x12345678",
					"81 3f 78 56 34 12"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(EDI)
									.build(),
							new Immediate(0x12345678L)),
					"cmp QWORD PTR [edi],0x0000000012345678",
					"67 48 81 3f 78 56 34 12"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(RDI)
									.build(),
							new Immediate(0x12345678L)),
					"cmp QWORD PTR [rdi],0x0000000012345678",
					"48 81 3f 78 56 34 12"),
			test(
					new Instruction(
							Opcode.CMP,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(EDI)
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
							new Immediate((short) 0x0077)),
					"cmp WORD PTR [r13+rcx*2+0x12],0x0077",
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
									.index(RDI)
									.build(),
							new Immediate((short) 0x7788)),
					"cmp WORD PTR [rdi],0x7788",
					"66 81 3f 88 77"),
			//
			test(
					new Instruction(
							Opcode.CMP,
							DH,
							IndirectOperand.builder()
									.pointer(BYTE_PTR)
									.index(EAX)
									.build()),
					"cmp dh,BYTE PTR [eax]",
					"67 3a 30"),
			test(
					new Instruction(
							Opcode.CMP,
							DH,
							IndirectOperand.builder()
									.pointer(BYTE_PTR)
									.index(RAX)
									.build()),
					"cmp dh,BYTE PTR [rax]",
					"3a 30"),
			test(
					new Instruction(
							Opcode.CMP,
							DX,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(EAX)
									.build()),
					"cmp dx,WORD PTR [eax]",
					"67 66 3b 10"),
			test(
					new Instruction(
							Opcode.CMP,
							DX,
							IndirectOperand.builder()
									.pointer(WORD_PTR)
									.index(RAX)
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
									.index(EAX)
									.build()),
					"cmp edx,DWORD PTR [eax]",
					"67 3b 10"),
			test(
					new Instruction(
							Opcode.CMP,
							EDX,
							IndirectOperand.builder()
									.pointer(DWORD_PTR)
									.index(RAX)
									.build()),
					"cmp edx,DWORD PTR [rax]",
					"3b 10"),
			test(
					new Instruction(
							Opcode.CMP,
							RDX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(EAX)
									.build()),
					"cmp rdx,QWORD PTR [eax]",
					"67 48 3b 10"),
			test(
					new Instruction(
							Opcode.CMP,
							RDX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(RAX)
									.build()),
					"cmp rdx,QWORD PTR [rax]",
					"48 3b 10"),
			//
			test(new Instruction(Opcode.CMP, AL, new Immediate((byte) 0x99)), "cmp al,0x99", "3c 99"),
			test(new Instruction(Opcode.CMP, AL, DH), "cmp al,dh", "38 f0"),
			test(new Instruction(Opcode.CMP, CX, new Immediate((short) 0x1234)), "cmp cx,0x1234", "66 81 f9 34 12"),
			test(new Instruction(Opcode.CMP, DH, new Immediate((byte) 0x99)), "cmp dh,0x99", "80 fe 99"),
			test(new Instruction(Opcode.CMP, EAX, new Immediate(0x12345678)), "cmp eax,0x12345678", "3d 78 56 34 12"),
			test(
					new Instruction(Opcode.CMP, EDI, new Immediate(0x12345678)),
					"cmp edi,0x12345678",
					"81 ff 78 56 34 12"),
			test(new Instruction(Opcode.CMP, ESP, R13D), "cmp esp,r13d", "44 39 ec"),
			test(new Instruction(Opcode.CMP, R8B, new Immediate((byte) 0x12)), "cmp r8b,0x12", "41 80 f8 12"),
			test(new Instruction(Opcode.CMP, R8W, DX), "cmp r8w,dx", "66 41 39 d0"),
			test(
					new Instruction(Opcode.CMP, RAX, new Immediate(0x12345678L)),
					"cmp rax,0x0000000012345678",
					"48 3d 78 56 34 12"),
			test(
					new Instruction(Opcode.CMP, RDI, new Immediate(0x12345678L)),
					"cmp rdi,0x0000000012345678",
					"48 81 ff 78 56 34 12"),
			test(new Instruction(Opcode.CMP, RSP, R8), "cmp rsp,r8", "4c 39 c4"),
			test(new Instruction(Opcode.CMP, SP, R13W), "cmp sp,r13w", "66 44 39 ec"),
			//  Lea
			test(
					new Instruction(
							Opcode.LEA,
							AX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
									.index(EBX)
									.build()),
					"lea eax,[ebx]",
					"67 8d 03"),
			test(
					new Instruction(
							Opcode.LEA,
							EAX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(RBX)
									.build()),
					"lea eax,[rbx]",
					"8d 03"),
			test(
					new Instruction(
							Opcode.LEA,
							ECX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.base(RDX)
									.index(RBP)
									.scale(2)
									.build()),
					"lea ecx,[rdx+rbp*2+0x0]",
					"8d 0c 6a"),
			test(
					new Instruction(
							Opcode.LEA,
							ESI,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
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
									.pointer(QWORD_PTR)
									.base(EDX)
									.index(EBP)
									.scale(2)
									.build()),
					"lea r9d,[edx+ebp*2+0x0]",
					"67 44 8d 0c 6a"),
			test(
					new Instruction(
							Opcode.LEA,
							RAX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(EBX)
									.build()),
					"lea rax,[ebx]",
					"67 48 8d 03"),
			test(
					new Instruction(
							Opcode.LEA,
							RAX,
							IndirectOperand.builder()
									.pointer(QWORD_PTR)
									.index(RBX)
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
					"lea rcx,[edx+ebp*2+0x0]",
					"67 48 8d 0c 6a"),
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
					"lea rcx,[rdx+rbp*2+0x0]",
					"48 8d 0c 6a"),
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
					"4a 8d b4 87 78 56 34 12"),
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
			test(null, "mov BYTE PTR [r11+r8*4+0x12345678],0x99", "43 c6 84 83 78 56 34 12 99"),
			test(null, "mov BYTE PTR [rdi],bl", "88 1f"),
			test(null, "mov BYTE PTR [rsp+rcx*4+0x12345678],bh", "88 bc 8c 78 56 34 12"),
			test(null, "mov BYTE PTR [rsp+rcx*4+0x12345678],cl", "88 8c 8c 78 56 34 12"),
			test(null, "mov BYTE PTR [rsp+rcx*4+0x12345678],dil", "40 88 bc 8c 78 56 34 12"),
			test(null, "mov BYTE PTR [rsp+rcx*4+0x12345678],r9b", "44 88 8c 8c 78 56 34 12"),
			test(null, "mov DWORD PTR [r11+r8*4+0x12345678],0xdeadbeef", "43 c7 84 83 78 56 34 12 ef be ad de"),
			test(null, "mov DWORD PTR [rbp+0x7eadbeef],0x12345678", "c7 85 ef be ad 7e 78 56 34 12"),
			test(null, "mov QWORD PTR [rbp+0x7eadbeef],0x12345678", "48 c7 85 ef be ad 7e 78 56 34 12"),
			test(null, "mov QWORD PTR [rbp+r9*4+0x12345678],rsi", "4a 89 b4 8d 78 56 34 12"),
			test(null, "mov WORD PTR [r11+r8*4+0x12345678],0xbeef", "66 43 c7 84 83 78 56 34 12 ef be"),
			test(null, "mov al,BYTE PTR [rax+rbx*8+0x12345678]", "8a 84 d8 78 56 34 12"),
			test(null, "mov esi,0x12345678", "be 78 56 34 12"),
			test(null, "mov r11b,0x12", "41 b3 12"),
			test(null, "mov r8w,0x1234", "66 41 b8 34 12"),
			test(null, "mov r9,0x12345678", "49 c7 c1 78 56 34 12"),
			test(null, "mov rsi,QWORD PTR [rbp+r9*4+0x12345678]", "4a 8b b4 8d 78 56 34 12"),
			//  Movzx
			test(null, "movzx esi,bl", "0f b6 f3"),
			test(null, "movzx r9d,BYTE PTR [rcx]", "44 0f b6 09"),
			test(null, "movzx r9d,BYTE PTR [rdx+rax*4+0x12345678]", "44 0f b6 8c 82 78 56 34 12"),
			test(null, "movzx r9d,WORD PTR [rcx]", "44 0f b7 09"),
			test(null, "movzx r9d,WORD PTR [rdx+rax*4+0x12345678]", "44 0f b7 8c 82 78 56 34 12"),
			test(null, "movzx rsi,bl", "48 0f b6 f3"),
			test(null, "movzx rsi,di", "48 0f b7 f7"),
			//  Movsx
			test(null, "movsx edi,BYTE PTR [rax+rbx*4+0x12345678]", "0f be bc 98 78 56 34 12"),
			test(null, "movsx edi,WORD PTR [rax+rbx*4+0x12345678]", "0f bf bc 98 78 56 34 12"),
			test(null, "movsx esi,bl", "0f be f3"),
			test(null, "movsx rdi,BYTE PTR [rax+rbx*4+0x12345678]", "48 0f be bc 98 78 56 34 12"),
			test(null, "movsx rdi,WORD PTR [rax+rbx*4+0x12345678]", "48 0f bf bc 98 78 56 34 12"),
			test(null, "movsx rsi,bl", "48 0f be f3"),
			test(null, "movsx rsi,di", "48 0f bf f7"),
			//  Movsxd
			test(null, "movsxd r10,eax", "4c 63 d0"),
			test(null, "movsxd r10,ebp", "4c 63 d5"),
			test(null, "movsxd r10,ebx", "4c 63 d3"),
			test(null, "movsxd r10,ecx", "4c 63 d1"),
			test(null, "movsxd r10,edi", "4c 63 d7"),
			test(null, "movsxd r10,edx", "4c 63 d2"),
			test(null, "movsxd r10,esi", "4c 63 d6"),
			test(null, "movsxd r10,esp", "4c 63 d4"),
			test(null, "movsxd r10,r10d", "4d 63 d2"),
			test(null, "movsxd r10,r11d", "4d 63 d3"),
			test(null, "movsxd r10,r12d", "4d 63 d4"),
			test(null, "movsxd r10,r13d", "4d 63 d5"),
			test(null, "movsxd r10,r14d", "4d 63 d6"),
			test(null, "movsxd r10,r15d", "4d 63 d7"),
			test(null, "movsxd r10,r8d", "4d 63 d0"),
			test(null, "movsxd r10,r9d", "4d 63 d1"),
			test(null, "movsxd r11,eax", "4c 63 d8"),
			test(null, "movsxd r11,ebp", "4c 63 dd"),
			test(null, "movsxd r11,ebx", "4c 63 db"),
			test(null, "movsxd r11,ecx", "4c 63 d9"),
			test(null, "movsxd r11,edi", "4c 63 df"),
			test(null, "movsxd r11,edx", "4c 63 da"),
			test(null, "movsxd r11,esi", "4c 63 de"),
			test(null, "movsxd r11,esp", "4c 63 dc"),
			test(null, "movsxd r11,r10d", "4d 63 da"),
			test(null, "movsxd r11,r11d", "4d 63 db"),
			test(null, "movsxd r11,r12d", "4d 63 dc"),
			test(null, "movsxd r11,r13d", "4d 63 dd"),
			test(null, "movsxd r11,r14d", "4d 63 de"),
			test(null, "movsxd r11,r15d", "4d 63 df"),
			test(null, "movsxd r11,r8d", "4d 63 d8"),
			test(null, "movsxd r11,r9d", "4d 63 d9"),
			test(null, "movsxd r12,eax", "4c 63 e0"),
			test(null, "movsxd r12,ebp", "4c 63 e5"),
			test(null, "movsxd r12,ebx", "4c 63 e3"),
			test(null, "movsxd r12,ecx", "4c 63 e1"),
			test(null, "movsxd r12,edi", "4c 63 e7"),
			test(null, "movsxd r12,edx", "4c 63 e2"),
			test(null, "movsxd r12,esi", "4c 63 e6"),
			test(null, "movsxd r12,esp", "4c 63 e4"),
			test(null, "movsxd r12,r10d", "4d 63 e2"),
			test(null, "movsxd r12,r11d", "4d 63 e3"),
			test(null, "movsxd r12,r12d", "4d 63 e4"),
			test(null, "movsxd r12,r13d", "4d 63 e5"),
			test(null, "movsxd r12,r14d", "4d 63 e6"),
			test(null, "movsxd r12,r15d", "4d 63 e7"),
			test(null, "movsxd r12,r8d", "4d 63 e0"),
			test(null, "movsxd r12,r9d", "4d 63 e1"),
			test(null, "movsxd r13,eax", "4c 63 e8"),
			test(null, "movsxd r13,ebp", "4c 63 ed"),
			test(null, "movsxd r13,ebx", "4c 63 eb"),
			test(null, "movsxd r13,ecx", "4c 63 e9"),
			test(null, "movsxd r13,edi", "4c 63 ef"),
			test(null, "movsxd r13,edx", "4c 63 ea"),
			test(null, "movsxd r13,esi", "4c 63 ee"),
			test(null, "movsxd r13,esp", "4c 63 ec"),
			test(null, "movsxd r13,r10d", "4d 63 ea"),
			test(null, "movsxd r13,r11d", "4d 63 eb"),
			test(null, "movsxd r13,r12d", "4d 63 ec"),
			test(null, "movsxd r13,r13d", "4d 63 ed"),
			test(null, "movsxd r13,r14d", "4d 63 ee"),
			test(null, "movsxd r13,r15d", "4d 63 ef"),
			test(null, "movsxd r13,r8d", "4d 63 e8"),
			test(null, "movsxd r13,r9d", "4d 63 e9"),
			test(null, "movsxd r14,eax", "4c 63 f0"),
			test(null, "movsxd r14,ebp", "4c 63 f5"),
			test(null, "movsxd r14,ebx", "4c 63 f3"),
			test(null, "movsxd r14,ecx", "4c 63 f1"),
			test(null, "movsxd r14,edi", "4c 63 f7"),
			test(null, "movsxd r14,edx", "4c 63 f2"),
			test(null, "movsxd r14,esi", "4c 63 f6"),
			test(null, "movsxd r14,esp", "4c 63 f4"),
			test(null, "movsxd r14,r10d", "4d 63 f2"),
			test(null, "movsxd r14,r11d", "4d 63 f3"),
			test(null, "movsxd r14,r12d", "4d 63 f4"),
			test(null, "movsxd r14,r13d", "4d 63 f5"),
			test(null, "movsxd r14,r14d", "4d 63 f6"),
			test(null, "movsxd r14,r15d", "4d 63 f7"),
			test(null, "movsxd r14,r8d", "4d 63 f0"),
			test(null, "movsxd r14,r9d", "4d 63 f1"),
			test(null, "movsxd r15,eax", "4c 63 f8"),
			test(null, "movsxd r15,ebp", "4c 63 fd"),
			test(null, "movsxd r15,ebx", "4c 63 fb"),
			test(null, "movsxd r15,ecx", "4c 63 f9"),
			test(null, "movsxd r15,edi", "4c 63 ff"),
			test(null, "movsxd r15,edx", "4c 63 fa"),
			test(null, "movsxd r15,esi", "4c 63 fe"),
			test(null, "movsxd r15,esp", "4c 63 fc"),
			test(null, "movsxd r15,r10d", "4d 63 fa"),
			test(null, "movsxd r15,r11d", "4d 63 fb"),
			test(null, "movsxd r15,r12d", "4d 63 fc"),
			test(null, "movsxd r15,r13d", "4d 63 fd"),
			test(null, "movsxd r15,r14d", "4d 63 fe"),
			test(null, "movsxd r15,r15d", "4d 63 ff"),
			test(null, "movsxd r15,r8d", "4d 63 f8"),
			test(null, "movsxd r15,r9d", "4d 63 f9"),
			test(null, "movsxd r8,eax", "4c 63 c0"),
			test(null, "movsxd r8,ebp", "4c 63 c5"),
			test(null, "movsxd r8,ebx", "4c 63 c3"),
			test(null, "movsxd r8,ecx", "4c 63 c1"),
			test(null, "movsxd r8,edi", "4c 63 c7"),
			test(null, "movsxd r8,edx", "4c 63 c2"),
			test(null, "movsxd r8,esi", "4c 63 c6"),
			test(null, "movsxd r8,esp", "4c 63 c4"),
			test(null, "movsxd r8,r10d", "4d 63 c2"),
			test(null, "movsxd r8,r11d", "4d 63 c3"),
			test(null, "movsxd r8,r12d", "4d 63 c4"),
			test(null, "movsxd r8,r13d", "4d 63 c5"),
			test(null, "movsxd r8,r14d", "4d 63 c6"),
			test(null, "movsxd r8,r15d", "4d 63 c7"),
			test(null, "movsxd r8,r8d", "4d 63 c0"),
			test(null, "movsxd r8,r9d", "4d 63 c1"),
			test(null, "movsxd r9,eax", "4c 63 c8"),
			test(null, "movsxd r9,ebp", "4c 63 cd"),
			test(null, "movsxd r9,ebx", "4c 63 cb"),
			test(null, "movsxd r9,ecx", "4c 63 c9"),
			test(null, "movsxd r9,edi", "4c 63 cf"),
			test(null, "movsxd r9,edx", "4c 63 ca"),
			test(null, "movsxd r9,esi", "4c 63 ce"),
			test(null, "movsxd r9,esp", "4c 63 cc"),
			test(null, "movsxd r9,r10d", "4d 63 ca"),
			test(null, "movsxd r9,r11d", "4d 63 cb"),
			test(null, "movsxd r9,r12d", "4d 63 cc"),
			test(null, "movsxd r9,r13d", "4d 63 cd"),
			test(null, "movsxd r9,r14d", "4d 63 ce"),
			test(null, "movsxd r9,r15d", "4d 63 cf"),
			test(null, "movsxd r9,r8d", "4d 63 c8"),
			test(null, "movsxd r9,r9d", "4d 63 c9"),
			test(null, "movsxd rax,eax", "48 63 c0"),
			test(null, "movsxd rax,ebp", "48 63 c5"),
			test(null, "movsxd rax,ebx", "48 63 c3"),
			test(null, "movsxd rax,ecx", "48 63 c1"),
			test(null, "movsxd rax,edi", "48 63 c7"),
			test(null, "movsxd rax,edx", "48 63 c2"),
			test(null, "movsxd rax,esi", "48 63 c6"),
			test(null, "movsxd rax,esp", "48 63 c4"),
			test(null, "movsxd rax,r10d", "49 63 c2"),
			test(null, "movsxd rax,r11d", "49 63 c3"),
			test(null, "movsxd rax,r12d", "49 63 c4"),
			test(null, "movsxd rax,r13d", "49 63 c5"),
			test(null, "movsxd rax,r14d", "49 63 c6"),
			test(null, "movsxd rax,r15d", "49 63 c7"),
			test(null, "movsxd rax,r8d", "49 63 c0"),
			test(null, "movsxd rax,r9d", "49 63 c1"),
			test(null, "movsxd rbp,eax", "48 63 e8"),
			test(null, "movsxd rbp,ebp", "48 63 ed"),
			test(null, "movsxd rbp,ebx", "48 63 eb"),
			test(null, "movsxd rbp,ecx", "48 63 e9"),
			test(null, "movsxd rbp,edi", "48 63 ef"),
			test(null, "movsxd rbp,edx", "48 63 ea"),
			test(null, "movsxd rbp,esi", "48 63 ee"),
			test(null, "movsxd rbp,esp", "48 63 ec"),
			test(null, "movsxd rbp,r10d", "49 63 ea"),
			test(null, "movsxd rbp,r11d", "49 63 eb"),
			test(null, "movsxd rbp,r12d", "49 63 ec"),
			test(null, "movsxd rbp,r13d", "49 63 ed"),
			test(null, "movsxd rbp,r14d", "49 63 ee"),
			test(null, "movsxd rbp,r15d", "49 63 ef"),
			test(null, "movsxd rbp,r8d", "49 63 e8"),
			test(null, "movsxd rbp,r9d", "49 63 e9"),
			test(null, "movsxd rbx,eax", "48 63 d8"),
			test(null, "movsxd rbx,ebp", "48 63 dd"),
			test(null, "movsxd rbx,ebx", "48 63 db"),
			test(null, "movsxd rbx,ecx", "48 63 d9"),
			test(null, "movsxd rbx,edi", "48 63 df"),
			test(null, "movsxd rbx,edx", "48 63 da"),
			test(null, "movsxd rbx,esi", "48 63 de"),
			test(null, "movsxd rbx,esp", "48 63 dc"),
			test(null, "movsxd rbx,r10d", "49 63 da"),
			test(null, "movsxd rbx,r11d", "49 63 db"),
			test(null, "movsxd rbx,r12d", "49 63 dc"),
			test(null, "movsxd rbx,r13d", "49 63 dd"),
			test(null, "movsxd rbx,r14d", "49 63 de"),
			test(null, "movsxd rbx,r15d", "49 63 df"),
			test(null, "movsxd rbx,r8d", "49 63 d8"),
			test(null, "movsxd rbx,r9d", "49 63 d9"),
			test(null, "movsxd rcx,eax", "48 63 c8"),
			test(null, "movsxd rcx,ebp", "48 63 cd"),
			test(null, "movsxd rcx,ebx", "48 63 cb"),
			test(null, "movsxd rcx,ecx", "48 63 c9"),
			test(null, "movsxd rcx,edi", "48 63 cf"),
			test(null, "movsxd rcx,edx", "48 63 ca"),
			test(null, "movsxd rcx,esi", "48 63 ce"),
			test(null, "movsxd rcx,esp", "48 63 cc"),
			test(null, "movsxd rcx,r10d", "49 63 ca"),
			test(null, "movsxd rcx,r11d", "49 63 cb"),
			test(null, "movsxd rcx,r12d", "49 63 cc"),
			test(null, "movsxd rcx,r13d", "49 63 cd"),
			test(null, "movsxd rcx,r14d", "49 63 ce"),
			test(null, "movsxd rcx,r15d", "49 63 cf"),
			test(null, "movsxd rcx,r8d", "49 63 c8"),
			test(null, "movsxd rcx,r9d", "49 63 c9"),
			test(null, "movsxd rdi,eax", "48 63 f8"),
			test(null, "movsxd rdi,ebp", "48 63 fd"),
			test(null, "movsxd rdi,ebx", "48 63 fb"),
			test(null, "movsxd rdi,ecx", "48 63 f9"),
			test(null, "movsxd rdi,edi", "48 63 ff"),
			test(null, "movsxd rdi,edx", "48 63 fa"),
			test(null, "movsxd rdi,esi", "48 63 fe"),
			test(null, "movsxd rdi,esp", "48 63 fc"),
			test(null, "movsxd rdi,r10d", "49 63 fa"),
			test(null, "movsxd rdi,r11d", "49 63 fb"),
			test(null, "movsxd rdi,r12d", "49 63 fc"),
			test(null, "movsxd rdi,r13d", "49 63 fd"),
			test(null, "movsxd rdi,r14d", "49 63 fe"),
			test(null, "movsxd rdi,r15d", "49 63 ff"),
			test(null, "movsxd rdi,r8d", "49 63 f8"),
			test(null, "movsxd rdi,r9d", "49 63 f9"),
			test(null, "movsxd rdx,eax", "48 63 d0"),
			test(null, "movsxd rdx,ebp", "48 63 d5"),
			test(null, "movsxd rdx,ebx", "48 63 d3"),
			test(null, "movsxd rdx,ecx", "48 63 d1"),
			test(null, "movsxd rdx,edi", "48 63 d7"),
			test(null, "movsxd rdx,edx", "48 63 d2"),
			test(null, "movsxd rdx,esi", "48 63 d6"),
			test(null, "movsxd rdx,esp", "48 63 d4"),
			test(null, "movsxd rdx,r10d", "49 63 d2"),
			test(null, "movsxd rdx,r11d", "49 63 d3"),
			test(null, "movsxd rdx,r12d", "49 63 d4"),
			test(null, "movsxd rdx,r13d", "49 63 d5"),
			test(null, "movsxd rdx,r14d", "49 63 d6"),
			test(null, "movsxd rdx,r15d", "49 63 d7"),
			test(null, "movsxd rdx,r8d", "49 63 d0"),
			test(null, "movsxd rdx,r9d", "49 63 d1"),
			test(null, "movsxd rsi,eax", "48 63 f0"),
			test(null, "movsxd rsi,ebp", "48 63 f5"),
			test(null, "movsxd rsi,ebx", "48 63 f3"),
			test(null, "movsxd rsi,ecx", "48 63 f1"),
			test(null, "movsxd rsi,edi", "48 63 f7"),
			test(null, "movsxd rsi,edx", "48 63 f2"),
			test(null, "movsxd rsi,esi", "48 63 f6"),
			test(null, "movsxd rsi,esp", "48 63 f4"),
			test(null, "movsxd rsi,r10d", "49 63 f2"),
			test(null, "movsxd rsi,r11d", "49 63 f3"),
			test(null, "movsxd rsi,r12d", "49 63 f4"),
			test(null, "movsxd rsi,r13d", "49 63 f5"),
			test(null, "movsxd rsi,r14d", "49 63 f6"),
			test(null, "movsxd rsi,r15d", "49 63 f7"),
			test(null, "movsxd rsi,r8d", "49 63 f0"),
			test(null, "movsxd rsi,r9d", "49 63 f1"),
			test(null, "movsxd rsp,eax", "48 63 e0"),
			test(null, "movsxd rsp,ebp", "48 63 e5"),
			test(null, "movsxd rsp,ebx", "48 63 e3"),
			test(null, "movsxd rsp,ecx", "48 63 e1"),
			test(null, "movsxd rsp,edi", "48 63 e7"),
			test(null, "movsxd rsp,edx", "48 63 e2"),
			test(null, "movsxd rsp,esi", "48 63 e6"),
			test(null, "movsxd rsp,esp", "48 63 e4"),
			test(null, "movsxd rsp,r10d", "49 63 e2"),
			test(null, "movsxd rsp,r11d", "49 63 e3"),
			test(null, "movsxd rsp,r12d", "49 63 e4"),
			test(null, "movsxd rsp,r13d", "49 63 e5"),
			test(null, "movsxd rsp,r14d", "49 63 e6"),
			test(null, "movsxd rsp,r15d", "49 63 e7"),
			test(null, "movsxd rsp,r8d", "49 63 e0"),
			test(null, "movsxd rsp,r9d", "49 63 e1"),
			//
			test(null, "movsxd r10,DWORD PTR [r10]", "4d 63 12"),
			test(null, "movsxd r10,DWORD PTR [r11]", "4d 63 13"),
			test(null, "movsxd r10,DWORD PTR [r12]", "4d 63 14 24"),
			test(null, "movsxd r10,DWORD PTR [r13]", "4d 63 55 00"),
			test(null, "movsxd r10,DWORD PTR [r14]", "4d 63 16"),
			test(null, "movsxd r10,DWORD PTR [r15]", "4d 63 17"),
			test(null, "movsxd r10,DWORD PTR [r8]", "4d 63 10"),
			test(null, "movsxd r10,DWORD PTR [r9]", "4d 63 11"),
			test(null, "movsxd r10,DWORD PTR [rax]", "4c 63 10"),
			test(null, "movsxd r10,DWORD PTR [rbp]", "4c 63 55 00"),
			test(null, "movsxd r10,DWORD PTR [rbx]", "4c 63 13"),
			test(null, "movsxd r10,DWORD PTR [rcx]", "4c 63 11"),
			test(null, "movsxd r10,DWORD PTR [rdi]", "4c 63 17"),
			test(null, "movsxd r10,DWORD PTR [rdx]", "4c 63 12"),
			test(null, "movsxd r10,DWORD PTR [rsi]", "4c 63 16"),
			test(null, "movsxd r10,DWORD PTR [rsp]", "4c 63 14 24"),
			test(null, "movsxd r11,DWORD PTR [r10]", "4d 63 1a"),
			test(null, "movsxd r11,DWORD PTR [r11]", "4d 63 1b"),
			test(null, "movsxd r11,DWORD PTR [r12]", "4d 63 1c 24"),
			test(null, "movsxd r11,DWORD PTR [r13]", "4d 63 5d 00"),
			test(null, "movsxd r11,DWORD PTR [r14]", "4d 63 1e"),
			test(null, "movsxd r11,DWORD PTR [r15]", "4d 63 1f"),
			test(null, "movsxd r11,DWORD PTR [r8]", "4d 63 18"),
			test(null, "movsxd r11,DWORD PTR [r9]", "4d 63 19"),
			test(null, "movsxd r11,DWORD PTR [rax]", "4c 63 18"),
			test(null, "movsxd r11,DWORD PTR [rbp]", "4c 63 5d 00"),
			test(null, "movsxd r11,DWORD PTR [rbx]", "4c 63 1b"),
			test(null, "movsxd r11,DWORD PTR [rcx]", "4c 63 19"),
			test(null, "movsxd r11,DWORD PTR [rdi]", "4c 63 1f"),
			test(null, "movsxd r11,DWORD PTR [rdx]", "4c 63 1a"),
			test(null, "movsxd r11,DWORD PTR [rsi]", "4c 63 1e"),
			test(null, "movsxd r11,DWORD PTR [rsp]", "4c 63 1c 24"),
			test(null, "movsxd r12,DWORD PTR [r10]", "4d 63 22"),
			test(null, "movsxd r12,DWORD PTR [r11]", "4d 63 23"),
			test(null, "movsxd r12,DWORD PTR [r12]", "4d 63 24 24"),
			test(null, "movsxd r12,DWORD PTR [r13]", "4d 63 65 00"),
			test(null, "movsxd r12,DWORD PTR [r14]", "4d 63 26"),
			test(null, "movsxd r12,DWORD PTR [r15]", "4d 63 27"),
			test(null, "movsxd r12,DWORD PTR [r8]", "4d 63 20"),
			test(null, "movsxd r12,DWORD PTR [r9]", "4d 63 21"),
			test(null, "movsxd r12,DWORD PTR [rax]", "4c 63 20"),
			test(null, "movsxd r12,DWORD PTR [rbp]", "4c 63 65 00"),
			test(null, "movsxd r12,DWORD PTR [rbx]", "4c 63 23"),
			test(null, "movsxd r12,DWORD PTR [rcx]", "4c 63 21"),
			test(null, "movsxd r12,DWORD PTR [rdi]", "4c 63 27"),
			test(null, "movsxd r12,DWORD PTR [rdx]", "4c 63 22"),
			test(null, "movsxd r12,DWORD PTR [rsi]", "4c 63 26"),
			test(null, "movsxd r12,DWORD PTR [rsp]", "4c 63 24 24"),
			test(null, "movsxd r13,DWORD PTR [r10]", "4d 63 2a"),
			test(null, "movsxd r13,DWORD PTR [r11]", "4d 63 2b"),
			test(null, "movsxd r13,DWORD PTR [r12]", "4d 63 2c 24"),
			test(null, "movsxd r13,DWORD PTR [r13]", "4d 63 6d 00"),
			test(null, "movsxd r13,DWORD PTR [r14]", "4d 63 2e"),
			test(null, "movsxd r13,DWORD PTR [r15]", "4d 63 2f"),
			test(null, "movsxd r13,DWORD PTR [r8]", "4d 63 28"),
			test(null, "movsxd r13,DWORD PTR [r9]", "4d 63 29"),
			test(null, "movsxd r13,DWORD PTR [rax]", "4c 63 28"),
			test(null, "movsxd r13,DWORD PTR [rbp]", "4c 63 6d 00"),
			test(null, "movsxd r13,DWORD PTR [rbx]", "4c 63 2b"),
			test(null, "movsxd r13,DWORD PTR [rcx]", "4c 63 29"),
			test(null, "movsxd r13,DWORD PTR [rdi]", "4c 63 2f"),
			test(null, "movsxd r13,DWORD PTR [rdx]", "4c 63 2a"),
			test(null, "movsxd r13,DWORD PTR [rsi]", "4c 63 2e"),
			test(null, "movsxd r13,DWORD PTR [rsp]", "4c 63 2c 24"),
			test(null, "movsxd r14,DWORD PTR [r10]", "4d 63 32"),
			test(null, "movsxd r14,DWORD PTR [r11]", "4d 63 33"),
			test(null, "movsxd r14,DWORD PTR [r12]", "4d 63 34 24"),
			test(null, "movsxd r14,DWORD PTR [r13]", "4d 63 75 00"),
			test(null, "movsxd r14,DWORD PTR [r14]", "4d 63 36"),
			test(null, "movsxd r14,DWORD PTR [r15]", "4d 63 37"),
			test(null, "movsxd r14,DWORD PTR [r8]", "4d 63 30"),
			test(null, "movsxd r14,DWORD PTR [r9]", "4d 63 31"),
			test(null, "movsxd r14,DWORD PTR [rax]", "4c 63 30"),
			test(null, "movsxd r14,DWORD PTR [rbp]", "4c 63 75 00"),
			test(null, "movsxd r14,DWORD PTR [rbx]", "4c 63 33"),
			test(null, "movsxd r14,DWORD PTR [rcx]", "4c 63 31"),
			test(null, "movsxd r14,DWORD PTR [rdi]", "4c 63 37"),
			test(null, "movsxd r14,DWORD PTR [rdx]", "4c 63 32"),
			test(null, "movsxd r14,DWORD PTR [rsi]", "4c 63 36"),
			test(null, "movsxd r14,DWORD PTR [rsp]", "4c 63 34 24"),
			test(null, "movsxd r15,DWORD PTR [r10]", "4d 63 3a"),
			test(null, "movsxd r15,DWORD PTR [r11]", "4d 63 3b"),
			test(null, "movsxd r15,DWORD PTR [r12]", "4d 63 3c 24"),
			test(null, "movsxd r15,DWORD PTR [r13]", "4d 63 7d 00"),
			test(null, "movsxd r15,DWORD PTR [r14]", "4d 63 3e"),
			test(null, "movsxd r15,DWORD PTR [r15]", "4d 63 3f"),
			test(null, "movsxd r15,DWORD PTR [r8]", "4d 63 38"),
			test(null, "movsxd r15,DWORD PTR [r9]", "4d 63 39"),
			test(null, "movsxd r15,DWORD PTR [rax]", "4c 63 38"),
			test(null, "movsxd r15,DWORD PTR [rbp]", "4c 63 7d 00"),
			test(null, "movsxd r15,DWORD PTR [rbx]", "4c 63 3b"),
			test(null, "movsxd r15,DWORD PTR [rcx]", "4c 63 39"),
			test(null, "movsxd r15,DWORD PTR [rdi]", "4c 63 3f"),
			test(null, "movsxd r15,DWORD PTR [rdx]", "4c 63 3a"),
			test(null, "movsxd r15,DWORD PTR [rsi]", "4c 63 3e"),
			test(null, "movsxd r15,DWORD PTR [rsp]", "4c 63 3c 24"),
			test(null, "movsxd r8,DWORD PTR [r10]", "4d 63 02"),
			test(null, "movsxd r8,DWORD PTR [r11]", "4d 63 03"),
			test(null, "movsxd r8,DWORD PTR [r12]", "4d 63 04 24"),
			test(null, "movsxd r8,DWORD PTR [r13]", "4d 63 45 00"),
			test(null, "movsxd r8,DWORD PTR [r14]", "4d 63 06"),
			test(null, "movsxd r8,DWORD PTR [r15]", "4d 63 07"),
			test(null, "movsxd r8,DWORD PTR [r8]", "4d 63 00"),
			test(null, "movsxd r8,DWORD PTR [r9]", "4d 63 01"),
			test(null, "movsxd r8,DWORD PTR [rax]", "4c 63 00"),
			test(null, "movsxd r8,DWORD PTR [rbp]", "4c 63 45 00"),
			test(null, "movsxd r8,DWORD PTR [rbx]", "4c 63 03"),
			test(null, "movsxd r8,DWORD PTR [rcx]", "4c 63 01"),
			test(null, "movsxd r8,DWORD PTR [rdi]", "4c 63 07"),
			test(null, "movsxd r8,DWORD PTR [rdx]", "4c 63 02"),
			test(null, "movsxd r8,DWORD PTR [rsi]", "4c 63 06"),
			test(null, "movsxd r8,DWORD PTR [rsp]", "4c 63 04 24"),
			test(null, "movsxd r9,DWORD PTR [r10]", "4d 63 0a"),
			test(null, "movsxd r9,DWORD PTR [r11]", "4d 63 0b"),
			test(null, "movsxd r9,DWORD PTR [r12]", "4d 63 0c 24"),
			test(null, "movsxd r9,DWORD PTR [r13]", "4d 63 4d 00"),
			test(null, "movsxd r9,DWORD PTR [r14]", "4d 63 0e"),
			test(null, "movsxd r9,DWORD PTR [r15]", "4d 63 0f"),
			test(null, "movsxd r9,DWORD PTR [r8]", "4d 63 08"),
			test(null, "movsxd r9,DWORD PTR [r9]", "4d 63 09"),
			test(null, "movsxd r9,DWORD PTR [rax]", "4c 63 08"),
			test(null, "movsxd r9,DWORD PTR [rbp]", "4c 63 4d 00"),
			test(null, "movsxd r9,DWORD PTR [rbx]", "4c 63 0b"),
			test(null, "movsxd r9,DWORD PTR [rcx]", "4c 63 09"),
			test(null, "movsxd r9,DWORD PTR [rdi]", "4c 63 0f"),
			test(null, "movsxd r9,DWORD PTR [rdx]", "4c 63 0a"),
			test(null, "movsxd r9,DWORD PTR [rsi]", "4c 63 0e"),
			test(null, "movsxd r9,DWORD PTR [rsp]", "4c 63 0c 24"),
			test(null, "movsxd rax,DWORD PTR [r10]", "49 63 02"),
			test(null, "movsxd rax,DWORD PTR [r11]", "49 63 03"),
			test(null, "movsxd rax,DWORD PTR [r12]", "49 63 04 24"),
			test(null, "movsxd rax,DWORD PTR [r13]", "49 63 45 00"),
			test(null, "movsxd rax,DWORD PTR [r14]", "49 63 06"),
			test(null, "movsxd rax,DWORD PTR [r15]", "49 63 07"),
			test(null, "movsxd rax,DWORD PTR [r8]", "49 63 00"),
			test(null, "movsxd rax,DWORD PTR [r9]", "49 63 01"),
			test(null, "movsxd rax,DWORD PTR [rax]", "48 63 00"),
			test(null, "movsxd rax,DWORD PTR [rbp]", "48 63 45 00"),
			test(null, "movsxd rax,DWORD PTR [rbx]", "48 63 03"),
			test(null, "movsxd rax,DWORD PTR [rcx]", "48 63 01"),
			test(null, "movsxd rax,DWORD PTR [rdi]", "48 63 07"),
			test(null, "movsxd rax,DWORD PTR [rdx]", "48 63 02"),
			test(null, "movsxd rax,DWORD PTR [rsi]", "48 63 06"),
			test(null, "movsxd rax,DWORD PTR [rsp]", "48 63 04 24"),
			test(null, "movsxd rbp,DWORD PTR [r10]", "49 63 2a"),
			test(null, "movsxd rbp,DWORD PTR [r11]", "49 63 2b"),
			test(null, "movsxd rbp,DWORD PTR [r12]", "49 63 2c 24"),
			test(null, "movsxd rbp,DWORD PTR [r13]", "49 63 6d 00"),
			test(null, "movsxd rbp,DWORD PTR [r14]", "49 63 2e"),
			test(null, "movsxd rbp,DWORD PTR [r15]", "49 63 2f"),
			test(null, "movsxd rbp,DWORD PTR [r8]", "49 63 28"),
			test(null, "movsxd rbp,DWORD PTR [r9]", "49 63 29"),
			test(null, "movsxd rbp,DWORD PTR [rax]", "48 63 28"),
			test(null, "movsxd rbp,DWORD PTR [rbp]", "48 63 6d 00"),
			test(null, "movsxd rbp,DWORD PTR [rbx]", "48 63 2b"),
			test(null, "movsxd rbp,DWORD PTR [rcx]", "48 63 29"),
			test(null, "movsxd rbp,DWORD PTR [rdi]", "48 63 2f"),
			test(null, "movsxd rbp,DWORD PTR [rdx]", "48 63 2a"),
			test(null, "movsxd rbp,DWORD PTR [rsi]", "48 63 2e"),
			test(null, "movsxd rbp,DWORD PTR [rsp]", "48 63 2c 24"),
			test(null, "movsxd rbx,DWORD PTR [r10]", "49 63 1a"),
			test(null, "movsxd rbx,DWORD PTR [r11]", "49 63 1b"),
			test(null, "movsxd rbx,DWORD PTR [r12]", "49 63 1c 24"),
			test(null, "movsxd rbx,DWORD PTR [r13]", "49 63 5d 00"),
			test(null, "movsxd rbx,DWORD PTR [r14]", "49 63 1e"),
			test(null, "movsxd rbx,DWORD PTR [r15]", "49 63 1f"),
			test(null, "movsxd rbx,DWORD PTR [r8]", "49 63 18"),
			test(null, "movsxd rbx,DWORD PTR [r9]", "49 63 19"),
			test(null, "movsxd rbx,DWORD PTR [rax]", "48 63 18"),
			test(null, "movsxd rbx,DWORD PTR [rbp]", "48 63 5d 00"),
			test(null, "movsxd rbx,DWORD PTR [rbx]", "48 63 1b"),
			test(null, "movsxd rbx,DWORD PTR [rcx]", "48 63 19"),
			test(null, "movsxd rbx,DWORD PTR [rdi]", "48 63 1f"),
			test(null, "movsxd rbx,DWORD PTR [rdx]", "48 63 1a"),
			test(null, "movsxd rbx,DWORD PTR [rsi]", "48 63 1e"),
			test(null, "movsxd rbx,DWORD PTR [rsp]", "48 63 1c 24"),
			test(null, "movsxd rcx,DWORD PTR [r10]", "49 63 0a"),
			test(null, "movsxd rcx,DWORD PTR [r11]", "49 63 0b"),
			test(null, "movsxd rcx,DWORD PTR [r12]", "49 63 0c 24"),
			test(null, "movsxd rcx,DWORD PTR [r13]", "49 63 4d 00"),
			test(null, "movsxd rcx,DWORD PTR [r14]", "49 63 0e"),
			test(null, "movsxd rcx,DWORD PTR [r15]", "49 63 0f"),
			test(null, "movsxd rcx,DWORD PTR [r8]", "49 63 08"),
			test(null, "movsxd rcx,DWORD PTR [r9]", "49 63 09"),
			test(null, "movsxd rcx,DWORD PTR [rax]", "48 63 08"),
			test(null, "movsxd rcx,DWORD PTR [rbp]", "48 63 4d 00"),
			test(null, "movsxd rcx,DWORD PTR [rbx]", "48 63 0b"),
			test(null, "movsxd rcx,DWORD PTR [rcx]", "48 63 09"),
			test(null, "movsxd rcx,DWORD PTR [rdi]", "48 63 0f"),
			test(null, "movsxd rcx,DWORD PTR [rdx]", "48 63 0a"),
			test(null, "movsxd rcx,DWORD PTR [rsi]", "48 63 0e"),
			test(null, "movsxd rcx,DWORD PTR [rsp]", "48 63 0c 24"),
			test(null, "movsxd rdi,DWORD PTR [r10]", "49 63 3a"),
			test(null, "movsxd rdi,DWORD PTR [r11]", "49 63 3b"),
			test(null, "movsxd rdi,DWORD PTR [r12]", "49 63 3c 24"),
			test(null, "movsxd rdi,DWORD PTR [r13]", "49 63 7d 00"),
			test(null, "movsxd rdi,DWORD PTR [r14]", "49 63 3e"),
			test(null, "movsxd rdi,DWORD PTR [r15]", "49 63 3f"),
			test(null, "movsxd rdi,DWORD PTR [r8]", "49 63 38"),
			test(null, "movsxd rdi,DWORD PTR [r9]", "49 63 39"),
			test(null, "movsxd rdi,DWORD PTR [rax]", "48 63 38"),
			test(null, "movsxd rdi,DWORD PTR [rbp]", "48 63 7d 00"),
			test(null, "movsxd rdi,DWORD PTR [rbx]", "48 63 3b"),
			test(null, "movsxd rdi,DWORD PTR [rcx]", "48 63 39"),
			test(null, "movsxd rdi,DWORD PTR [rdi]", "48 63 3f"),
			test(null, "movsxd rdi,DWORD PTR [rdx]", "48 63 3a"),
			test(null, "movsxd rdi,DWORD PTR [rsi]", "48 63 3e"),
			test(null, "movsxd rdi,DWORD PTR [rsp]", "48 63 3c 24"),
			test(null, "movsxd rdx,DWORD PTR [r10]", "49 63 12"),
			test(null, "movsxd rdx,DWORD PTR [r11]", "49 63 13"),
			test(null, "movsxd rdx,DWORD PTR [r12]", "49 63 14 24"),
			test(null, "movsxd rdx,DWORD PTR [r13]", "49 63 55 00"),
			test(null, "movsxd rdx,DWORD PTR [r14]", "49 63 16"),
			test(null, "movsxd rdx,DWORD PTR [r15]", "49 63 17"),
			test(null, "movsxd rdx,DWORD PTR [r8]", "49 63 10"),
			test(null, "movsxd rdx,DWORD PTR [r9]", "49 63 11"),
			test(null, "movsxd rdx,DWORD PTR [rax]", "48 63 10"),
			test(null, "movsxd rdx,DWORD PTR [rbp]", "48 63 55 00"),
			test(null, "movsxd rdx,DWORD PTR [rbx]", "48 63 13"),
			test(null, "movsxd rdx,DWORD PTR [rcx]", "48 63 11"),
			test(null, "movsxd rdx,DWORD PTR [rdi]", "48 63 17"),
			test(null, "movsxd rdx,DWORD PTR [rdx]", "48 63 12"),
			test(null, "movsxd rdx,DWORD PTR [rsi]", "48 63 16"),
			test(null, "movsxd rdx,DWORD PTR [rsp]", "48 63 14 24"),
			test(null, "movsxd rsi,DWORD PTR [r10]", "49 63 32"),
			test(null, "movsxd rsi,DWORD PTR [r11]", "49 63 33"),
			test(null, "movsxd rsi,DWORD PTR [r12]", "49 63 34 24"),
			test(null, "movsxd rsi,DWORD PTR [r13]", "49 63 75 00"),
			test(null, "movsxd rsi,DWORD PTR [r14]", "49 63 36"),
			test(null, "movsxd rsi,DWORD PTR [r15]", "49 63 37"),
			test(null, "movsxd rsi,DWORD PTR [r8]", "49 63 30"),
			test(null, "movsxd rsi,DWORD PTR [r9]", "49 63 31"),
			test(null, "movsxd rsi,DWORD PTR [rax]", "48 63 30"),
			test(null, "movsxd rsi,DWORD PTR [rbp]", "48 63 75 00"),
			test(null, "movsxd rsi,DWORD PTR [rbx]", "48 63 33"),
			test(null, "movsxd rsi,DWORD PTR [rcx]", "48 63 31"),
			test(null, "movsxd rsi,DWORD PTR [rdi]", "48 63 37"),
			test(null, "movsxd rsi,DWORD PTR [rdx]", "48 63 32"),
			test(null, "movsxd rsi,DWORD PTR [rsi]", "48 63 36"),
			test(null, "movsxd rsi,DWORD PTR [rsp]", "48 63 34 24"),
			test(null, "movsxd rsp,DWORD PTR [r10]", "49 63 22"),
			test(null, "movsxd rsp,DWORD PTR [r11]", "49 63 23"),
			test(null, "movsxd rsp,DWORD PTR [r12]", "49 63 24 24"),
			test(null, "movsxd rsp,DWORD PTR [r13]", "49 63 65 00"),
			test(null, "movsxd rsp,DWORD PTR [r14]", "49 63 26"),
			test(null, "movsxd rsp,DWORD PTR [r15]", "49 63 27"),
			test(null, "movsxd rsp,DWORD PTR [r8]", "49 63 20"),
			test(null, "movsxd rsp,DWORD PTR [r9]", "49 63 21"),
			test(null, "movsxd rsp,DWORD PTR [rax]", "48 63 20"),
			test(null, "movsxd rsp,DWORD PTR [rbp]", "48 63 65 00"),
			test(null, "movsxd rsp,DWORD PTR [rbx]", "48 63 23"),
			test(null, "movsxd rsp,DWORD PTR [rcx]", "48 63 21"),
			test(null, "movsxd rsp,DWORD PTR [rdi]", "48 63 27"),
			test(null, "movsxd rsp,DWORD PTR [rdx]", "48 63 22"),
			test(null, "movsxd rsp,DWORD PTR [rsi]", "48 63 26"),
			test(null, "movsxd rsp,DWORD PTR [rsp]", "48 63 24 24"),
			//
			test(null, "movsxd rdx,DWORD PTR [r11+r15*4+0x12345678]", "4b 63 94 bb 78 56 34 12"),
			//  Push
			test(null, "push 0x12345678", "68 78 56 34 12"),
			test(null, "push ax", "66 50"),
			test(null, "push bp", "66 55"),
			test(null, "push bx", "66 53"),
			test(null, "push cx", "66 51"),
			test(null, "push di", "66 57"),
			test(null, "push dx", "66 52"),
			test(null, "push r10", "41 52"),
			test(null, "push r10w", "66 41 52"),
			test(null, "push r11", "41 53"),
			test(null, "push r11w", "66 41 53"),
			test(null, "push r12", "41 54"),
			test(null, "push r12w", "66 41 54"),
			test(null, "push r13", "41 55"),
			test(null, "push r13w", "66 41 55"),
			test(null, "push r14", "41 56"),
			test(null, "push r14w", "66 41 56"),
			test(null, "push r15", "41 57"),
			test(null, "push r15w", "66 41 57"),
			test(null, "push r8", "41 50"),
			test(null, "push r8w", "66 41 50"),
			test(null, "push r9", "41 51"),
			test(null, "push r9w", "66 41 51"),
			test(null, "push rax", "50"),
			test(null, "push rbp", "55"),
			test(null, "push rbx", "53"),
			test(null, "push rcx", "51"),
			test(null, "push rdi", "57"),
			test(null, "push rdx", "52"),
			test(null, "push rsi", "56"),
			test(null, "push rsp", "54"),
			test(null, "push si", "66 56"),
			test(null, "push sp", "66 54"),
			//
			test(null, "push 0x12", "6a 12"),
			test(null, "push QWORD PTR [edx]", "67 ff 32"),
			test(null, "push QWORD PTR [r11+rsi*8+0x12345678]", "41 ff b4 f3 78 56 34 12"),
			test(null, "push QWORD PTR [r11d+edi*8+0x12345678]", "67 41 ff b4 fb 78 56 34 12"),
			test(null, "push QWORD PTR [rdx]", "ff 32"),
			//  Pop
			test(null, "pop ax", "66 58"),
			test(null, "pop bp", "66 5d"),
			test(null, "pop bx", "66 5b"),
			test(null, "pop cx", "66 59"),
			test(null, "pop di", "66 5f"),
			test(null, "pop dx", "66 5a"),
			test(null, "pop r10", "41 5a"),
			test(null, "pop r10w", "66 41 5a"),
			test(null, "pop r11", "41 5b"),
			test(null, "pop r11w", "66 41 5b"),
			test(null, "pop r12", "41 5c"),
			test(null, "pop r12w", "66 41 5c"),
			test(null, "pop r13", "41 5d"),
			test(null, "pop r13w", "66 41 5d"),
			test(null, "pop r14", "41 5e"),
			test(null, "pop r14w", "66 41 5e"),
			test(null, "pop r15", "41 5f"),
			test(null, "pop r15w", "66 41 5f"),
			test(null, "pop r8", "41 58"),
			test(null, "pop r8w", "66 41 58"),
			test(null, "pop r9", "41 59"),
			test(null, "pop r9w", "66 41 59"),
			test(null, "pop rax", "58"),
			test(null, "pop rbp", "5d"),
			test(null, "pop rbx", "5b"),
			test(null, "pop rcx", "59"),
			test(null, "pop rdi", "5f"),
			test(null, "pop rdx", "5a"),
			test(null, "pop rsi", "5e"),
			test(null, "pop rsp", "5c"),
			test(null, "pop si", "66 5e"),
			test(null, "pop sp", "66 5c"),
			//  Leave
			test(null, "leave", "c9"),
			//  Ret
			test(null, "ret", "c3"),
			//  Cpuid
			test(null, "cpuid", "0f a2"),
			//  Hlt
			test(null, "hlt", "f4"),
			//  Add
			test(null, "add DWORD PTR [eax+ebx*4+0x12345678],r8d", "67 44 01 84 98 78 56 34 12"),
			test(null, "add DWORD PTR [rax+rbx*4+0x12345678],r8d", "44 01 84 98 78 56 34 12"),
			test(null, "add QWORD PTR [rax+rbx*4+0x12345678],r9", "4c 01 8c 98 78 56 34 12"),
			test(null, "add QWORD PTR [rax+rbx*4+0x12345678],rsp", "48 01 a4 98 78 56 34 12"),
			test(null, "add QWORD PTR [rsp+rbp*4+0x7eadbeef],0x0000000000000012", "48 83 84 ac ef be ad 7e 12"),
			test(null, "add WORD PTR [rax+rbx*4+0x12345678],r8w", "66 44 01 84 98 78 56 34 12"),
			test(null, "add al,0x99", "04 99"),
			test(null, "add ax,0x1234", "66 05 34 12"),
			test(null, "add cx,0x1234", "66 81 c1 34 12"),
			test(null, "add eax,0x00000018", "83 c0 18"),
			test(null, "add eax,0x12345678", "05 78 56 34 12"),
			test(null, "add esp,DWORD PTR [rax+rbx*4+0x12345678]", "03 a4 98 78 56 34 12"),
			test(null, "add r11d,DWORD PTR [rax+rbx*4+0x12345678]", "44 03 9c 98 78 56 34 12"),
			test(null, "add r8,0x0000000000000001", "49 83 c0 01"),
			test(null, "add r8,r9", "4d 01 c8"),
			test(null, "add r9,0x0000000000000012", "49 83 c1 12"),
			test(null, "add r9,QWORD PTR [rax+rbx*4+0x12345678]", "4c 03 8c 98 78 56 34 12"),
			test(null, "add rax,0x0000000000000001", "48 83 c0 01"),
			test(null, "add rax,0x0000000000000012", "48 83 c0 12"),
			test(null, "add rax,0x0000000012345678", "48 05 78 56 34 12"),
			test(null, "add rsp,0x0000000012345678", "48 81 c4 78 56 34 12"),
			test(null, "add rsp,QWORD PTR [rax+rbx*4+0x12345678]", "48 03 a4 98 78 56 34 12"),
			//  Adc
			test(null, "adc cx,0x1234", "66 81 d1 34 12"),
			//  And
			test(null, "and al,0x12", "24 12"),
			test(null, "and al,BYTE PTR [rax+rbx*4+0x12345678]", "22 84 98 78 56 34 12"),
			test(null, "and ax,WORD PTR [rax+rbx*4+0x12345678]", "66 23 84 98 78 56 34 12"),
			test(null, "and cx,0x1234", "66 81 e1 34 12"),
			test(null, "and di,0x00f0", "66 81 e7 f0 00"),
			test(null, "and di,0xfff0", "66 83 e7 f0"),
			test(null, "and dx,WORD PTR [r10]", "66 41 23 12"),
			test(null, "and eax,0x00000012", "83 e0 12"),
			test(null, "and eax,0x12345678", "25 78 56 34 12"),
			test(null, "and eax,DWORD PTR [rax+rbx*4+0x12345678]", "23 84 98 78 56 34 12"),
			test(null, "and ecx,DWORD PTR [r10]", "41 23 0a"),
			test(null, "and edi,0x00000012", "83 e7 12"),
			test(null, "and edi,0x12345678", "81 e7 78 56 34 12"),
			test(null, "and edi,0xfffffff0", "83 e7 f0"),
			test(null, "and r12,r13", "4d 21 ec"),
			test(null, "and r15d,0x0000001f", "41 83 e7 1f"),
			test(null, "and rax,0x0000000000000012", "48 83 e0 12"),
			test(null, "and rax,0x0000000012345678", "48 25 78 56 34 12"),
			test(null, "and rax,QWORD PTR [rax+rbx*4+0x12345678]", "48 23 84 98 78 56 34 12"),
			test(null, "and rcx,QWORD PTR [r10]", "49 23 0a"),
			test(null, "and rdi,0x0000000000000012", "48 83 e7 12"),
			test(null, "and rdi,0x0000000012345678", "48 81 e7 78 56 34 12"),
			test(null, "and rdi,0xfffffffffedcba98", "48 81 e7 98 ba dc fe"),
			test(null, "and rdi,0xfffffffffffffff0", "48 83 e7 f0"),
			test(null, "and rdi,0xfffffffffffffffe", "48 83 e7 fe"),
			test(null, "and spl,BYTE PTR [r10]", "41 22 22"),
			//  Sub
			test(null, "sub DWORD PTR [eax+ebx*4+0x12345678],r8d", "67 44 29 84 98 78 56 34 12"),
			test(null, "sub DWORD PTR [rax+rbx*4+0x12345678],r8d", "44 29 84 98 78 56 34 12"),
			test(null, "sub QWORD PTR [rax+rbx*4+0x12345678],r9", "4c 29 8c 98 78 56 34 12"),
			test(null, "sub QWORD PTR [rax+rbx*4+0x12345678],rsp", "48 29 a4 98 78 56 34 12"),
			test(null, "sub WORD PTR [rax+rbx*4+0x12345678],r8w", "66 44 29 84 98 78 56 34 12"),
			test(null, "sub cx,0x1234", "66 81 e9 34 12"),
			test(null, "sub esi,0x00000012", "83 ee 12"),
			test(null, "sub esp,DWORD PTR [rax+rbx*4+0x12345678]", "2b a4 98 78 56 34 12"),
			test(null, "sub r11d,DWORD PTR [rax+rbx*4+0x12345678]", "44 2b 9c 98 78 56 34 12"),
			test(null, "sub r8,r9", "4d 29 c8"),
			test(null, "sub r9,QWORD PTR [rax+rbx*4+0x12345678]", "4c 2b 8c 98 78 56 34 12"),
			test(null, "sub rdi,0x0000000000000012", "48 83 ef 12"),
			test(null, "sub rsp,0x0000000012345678", "48 81 ec 78 56 34 12"),
			test(null, "sub rsp,QWORD PTR [rax+rbx*4+0x12345678]", "48 2b a4 98 78 56 34 12"),
			//  Sbb
			test(null, "sbb al,0x12", "1c 12"),
			test(null, "sbb ax,0x1234", "66 1d 34 12"),
			test(null, "sbb cx,0x1234", "66 81 d9 34 12"),
			test(null, "sbb esi,esi", "19 f6"),
			test(null, "sbb r12d,r12d", "45 19 e4"),
			test(null, "sbb rax,rax", "48 19 c0"),
			//  Shr
			test(null, "shr bpl,0x01", "40 d0 ed"),
			test(null, "shr bx,0x12", "66 c1 eb 12"),
			test(null, "shr di,0x01", "66 d1 ef"),
			test(null, "shr eax,cl", "d3 e8"),
			test(null, "shr ecx,0x12", "c1 e9 12"),
			test(null, "shr edx,0x01", "d1 ea"),
			test(null, "shr r11b,cl", "41 d2 eb"),
			test(null, "shr r9,0x01", "49 d1 e9"),
			test(null, "shr rcx,cl", "48 d3 e9"),
			test(null, "shr rdx,0x12", "48 c1 ea 12"),
			test(null, "shr si,cl", "66 d3 ee"),
			test(null, "shr sil,0x01", "40 d0 ee"),
			test(null, "shr spl,0x01", "40 d0 ec"),
			//  Sar
			test(null, "sar bpl,0x01", "40 d0 fd"),
			test(null, "sar bx,0x12", "66 c1 fb 12"),
			test(null, "sar di,0x01", "66 d1 ff"),
			test(null, "sar eax,cl", "d3 f8"),
			test(null, "sar ecx,0x12", "c1 f9 12"),
			test(null, "sar edx,0x01", "d1 fa"),
			test(null, "sar r11b,cl", "41 d2 fb"),
			test(null, "sar r9,0x01", "49 d1 f9"),
			test(null, "sar rcx,cl", "48 d3 f9"),
			test(null, "sar rdx,0x12", "48 c1 fa 12"),
			test(null, "sar si,cl", "66 d3 fe"),
			test(null, "sar sil,0x01", "40 d0 fe"),
			test(null, "sar spl,0x01", "40 d0 fc"),
			//  Shl
			test(null, "shl bpl,0x01", "40 d0 e5"),
			test(null, "shl bx,0x12", "66 c1 e3 12"),
			test(null, "shl di,0x01", "66 d1 e7"),
			test(null, "shl eax,cl", "d3 e0"),
			test(null, "shl ecx,0x12", "c1 e1 12"),
			test(null, "shl edx,0x01", "d1 e2"),
			test(null, "shl r11b,cl", "41 d2 e3"),
			test(null, "shl r9,0x01", "49 d1 e1"),
			test(null, "shl rcx,cl", "48 d3 e1"),
			test(null, "shl rdx,0x12", "48 c1 e2 12"),
			test(null, "shl si,cl", "66 d3 e6"),
			test(null, "shl sil,0x01", "40 d0 e6"),
			test(null, "shl spl,0x01", "40 d0 e4"),
			//  Imul
			test(null, "imul eax,ebx,0x12", "6b c3 12"),
			test(null, "imul edi,DWORD PTR [rax+r12*8+0x12345678]", "42 0f af bc e0 78 56 34 12"),
			test(null, "imul r9,QWORD PTR [r11+r12*4+0x12345678],0x7eadbeef", "4f 69 8c a3 78 56 34 12 ef be ad 7e"),
			test(null, "imul r9,QWORD PTR [rax],0x7eadbeef", "4c 69 08 ef be ad 7e"),
			test(null, "imul rbx,rbp", "48 0f af dd"),
			test(null, "imul rdx,r9,0x58", "49 6b d1 58"),
			//  Idiv
			test(null, "idiv eax", "f7 f8"),
			test(null, "idiv esi", "f7 fe"),
			test(null, "idiv r11", "49 f7 fb"),
			test(null, "idiv r9d", "41 f7 f9"),
			test(null, "idiv rax", "48 f7 f8"),
			test(null, "idiv rsi", "48 f7 fe"),
			//  Div
			test(null, "div ah", "f6 f4"),
			test(null, "div al", "f6 f0"),
			test(null, "div ax", "66 f7 f0"),
			test(null, "div bh", "f6 f7"),
			test(null, "div bl", "f6 f3"),
			test(null, "div bp", "66 f7 f5"),
			test(null, "div bpl", "40 f6 f5"),
			test(null, "div bx", "66 f7 f3"),
			test(null, "div ch", "f6 f5"),
			test(null, "div cl", "f6 f1"),
			test(null, "div cx", "66 f7 f1"),
			test(null, "div dh", "f6 f6"),
			test(null, "div di", "66 f7 f7"),
			test(null, "div dil", "40 f6 f7"),
			test(null, "div dl", "f6 f2"),
			test(null, "div dx", "66 f7 f2"),
			test(null, "div eax", "f7 f0"),
			test(null, "div ebp", "f7 f5"),
			test(null, "div ebx", "f7 f3"),
			test(null, "div ecx", "f7 f1"),
			test(null, "div edi", "f7 f7"),
			test(null, "div edx", "f7 f2"),
			test(null, "div esi", "f7 f6"),
			test(null, "div esp", "f7 f4"),
			test(null, "div r10", "49 f7 f2"),
			test(null, "div r10b", "41 f6 f2"),
			test(null, "div r10d", "41 f7 f2"),
			test(null, "div r10w", "66 41 f7 f2"),
			test(null, "div r11", "49 f7 f3"),
			test(null, "div r11b", "41 f6 f3"),
			test(null, "div r11d", "41 f7 f3"),
			test(null, "div r11w", "66 41 f7 f3"),
			test(null, "div r12", "49 f7 f4"),
			test(null, "div r12b", "41 f6 f4"),
			test(null, "div r12d", "41 f7 f4"),
			test(null, "div r12w", "66 41 f7 f4"),
			test(null, "div r13", "49 f7 f5"),
			test(null, "div r13b", "41 f6 f5"),
			test(null, "div r13d", "41 f7 f5"),
			test(null, "div r13w", "66 41 f7 f5"),
			test(null, "div r14", "49 f7 f6"),
			test(null, "div r14b", "41 f6 f6"),
			test(null, "div r14d", "41 f7 f6"),
			test(null, "div r14w", "66 41 f7 f6"),
			test(null, "div r15", "49 f7 f7"),
			test(null, "div r15b", "41 f6 f7"),
			test(null, "div r15d", "41 f7 f7"),
			test(null, "div r15w", "66 41 f7 f7"),
			test(null, "div r8", "49 f7 f0"),
			test(null, "div r8b", "41 f6 f0"),
			test(null, "div r8d", "41 f7 f0"),
			test(null, "div r8w", "66 41 f7 f0"),
			test(null, "div r9", "49 f7 f1"),
			test(null, "div r9b", "41 f6 f1"),
			test(null, "div r9d", "41 f7 f1"),
			test(null, "div r9w", "66 41 f7 f1"),
			test(null, "div rax", "48 f7 f0"),
			test(null, "div rbp", "48 f7 f5"),
			test(null, "div rbx", "48 f7 f3"),
			test(null, "div rcx", "48 f7 f1"),
			test(null, "div rdi", "48 f7 f7"),
			test(null, "div rdx", "48 f7 f2"),
			test(null, "div rsi", "48 f7 f6"),
			test(null, "div rsp", "48 f7 f4"),
			test(null, "div si", "66 f7 f6"),
			test(null, "div sil", "40 f6 f6"),
			test(null, "div sp", "66 f7 f4"),
			test(null, "div spl", "40 f6 f4"),
			//
			test(null, "div BYTE PTR [rax]", "f6 30"),
			test(null, "div BYTE PTR [rbx+r11*8+0x12345678]", "42 f6 b4 db 78 56 34 12"),
			test(null, "div DWORD PTR [rax]", "f7 30"),
			test(null, "div DWORD PTR [rbx+r11*8+0x12345678]", "42 f7 b4 db 78 56 34 12"),
			test(null, "div QWORD PTR [rax]", "48 f7 30"),
			test(null, "div QWORD PTR [rbx+r11*8+0x12345678]", "4a f7 b4 db 78 56 34 12"),
			test(null, "div WORD PTR [rax]", "66 f7 30"),
			test(null, "div WORD PTR [rbx+r11*8+0x12345678]", "66 42 f7 b4 db 78 56 34 12"),
			//  Mul
			test(null, "mul ah", "f6 e4"),
			test(null, "mul al", "f6 e0"),
			test(null, "mul ax", "66 f7 e0"),
			test(null, "mul bh", "f6 e7"),
			test(null, "mul bl", "f6 e3"),
			test(null, "mul bp", "66 f7 e5"),
			test(null, "mul bpl", "40 f6 e5"),
			test(null, "mul bx", "66 f7 e3"),
			test(null, "mul ch", "f6 e5"),
			test(null, "mul cl", "f6 e1"),
			test(null, "mul cx", "66 f7 e1"),
			test(null, "mul dh", "f6 e6"),
			test(null, "mul di", "66 f7 e7"),
			test(null, "mul dil", "40 f6 e7"),
			test(null, "mul dl", "f6 e2"),
			test(null, "mul dx", "66 f7 e2"),
			test(null, "mul eax", "f7 e0"),
			test(null, "mul ebp", "f7 e5"),
			test(null, "mul ebx", "f7 e3"),
			test(null, "mul ecx", "f7 e1"),
			test(null, "mul edi", "f7 e7"),
			test(null, "mul edx", "f7 e2"),
			test(null, "mul esi", "f7 e6"),
			test(null, "mul esp", "f7 e4"),
			test(null, "mul r10", "49 f7 e2"),
			test(null, "mul r10b", "41 f6 e2"),
			test(null, "mul r10d", "41 f7 e2"),
			test(null, "mul r10w", "66 41 f7 e2"),
			test(null, "mul r11", "49 f7 e3"),
			test(null, "mul r11b", "41 f6 e3"),
			test(null, "mul r11d", "41 f7 e3"),
			test(null, "mul r11w", "66 41 f7 e3"),
			test(null, "mul r12", "49 f7 e4"),
			test(null, "mul r12b", "41 f6 e4"),
			test(null, "mul r12d", "41 f7 e4"),
			test(null, "mul r12w", "66 41 f7 e4"),
			test(null, "mul r13", "49 f7 e5"),
			test(null, "mul r13b", "41 f6 e5"),
			test(null, "mul r13d", "41 f7 e5"),
			test(null, "mul r13w", "66 41 f7 e5"),
			test(null, "mul r14", "49 f7 e6"),
			test(null, "mul r14b", "41 f6 e6"),
			test(null, "mul r14d", "41 f7 e6"),
			test(null, "mul r14w", "66 41 f7 e6"),
			test(null, "mul r15", "49 f7 e7"),
			test(null, "mul r15b", "41 f6 e7"),
			test(null, "mul r15d", "41 f7 e7"),
			test(null, "mul r15w", "66 41 f7 e7"),
			test(null, "mul r8", "49 f7 e0"),
			test(null, "mul r8b", "41 f6 e0"),
			test(null, "mul r8d", "41 f7 e0"),
			test(null, "mul r8w", "66 41 f7 e0"),
			test(null, "mul r9", "49 f7 e1"),
			test(null, "mul r9b", "41 f6 e1"),
			test(null, "mul r9d", "41 f7 e1"),
			test(null, "mul r9w", "66 41 f7 e1"),
			test(null, "mul rax", "48 f7 e0"),
			test(null, "mul rbp", "48 f7 e5"),
			test(null, "mul rbx", "48 f7 e3"),
			test(null, "mul rcx", "48 f7 e1"),
			test(null, "mul rdi", "48 f7 e7"),
			test(null, "mul rdx", "48 f7 e2"),
			test(null, "mul rsi", "48 f7 e6"),
			test(null, "mul rsp", "48 f7 e4"),
			test(null, "mul si", "66 f7 e6"),
			test(null, "mul sil", "40 f6 e6"),
			test(null, "mul sp", "66 f7 e4"),
			test(null, "mul spl", "40 f6 e4"),
			//  Or
			test(null, "or BYTE PTR [r11+r9*4+0x12345678],0x99", "43 80 8c 8b 78 56 34 12 99"),
			test(null, "or BYTE PTR [rbx+r9*4+0x12345678],r9b", "46 08 8c 8b 78 56 34 12"),
			test(null, "or DWORD PTR [r11+r9*4+0x12345678],0xdeadbeef", "43 81 8c 8b 78 56 34 12 ef be ad de"),
			test(null, "or QWORD PTR [r8],rdx", "49 09 10"),
			test(null, "or QWORD PTR [r9+rcx*4+0x12345678],rsi", "49 09 b4 89 78 56 34 12"),
			test(null, "or WORD PTR [r11+r9*4+0x12345678],0xbeef", "66 43 81 8c 8b 78 56 34 12 ef be"),
			test(null, "or al,0x12", "0c 12"),
			test(null, "or cl,0x12", "80 c9 12"),
			test(null, "or cx,0x1234", "66 81 c9 34 12"),
			test(null, "or eax,0x00000012", "83 c8 12"),
			test(null, "or eax,0x12345678", "0d 78 56 34 12"),
			test(null, "or eax,DWORD PTR [rax+rbx*4+0x12345678]", "0b 84 98 78 56 34 12"),
			test(null, "or ecx,DWORD PTR [r10]", "41 0b 0a"),
			test(null, "or edi,0x00000012", "83 cf 12"),
			test(null, "or rax,0x0000000000000012", "48 83 c8 12"),
			test(null, "or rax,0x0000000012345678", "48 0d 78 56 34 12"),
			test(null, "or rax,0xfffffffffffffffe", "48 83 c8 fe"),
			test(null, "or rax,QWORD PTR [rax+rbx*4+0x12345678]", "48 0b 84 98 78 56 34 12"),
			test(null, "or rcx,QWORD PTR [r10]", "49 0b 0a"),
			test(null, "or rdi,0x0000000000000012", "48 83 cf 12"),
			//  Xor
			test(null, "xor cx,0x1234", "66 81 f1 34 12"),
			test(null, "xor eax,0x00000012", "83 f0 12"),
			test(null, "xor eax,0x12345678", "35 78 56 34 12"),
			test(null, "xor ebx,0x12345678", "81 f3 78 56 34 12"),
			test(null, "xor r8,0x0000000000000012", "49 83 f0 12"),
			test(null, "xor r8,0x0000000012345678", "49 81 f0 78 56 34 12"),
			//  Not
			test(null, "not eax", "f7 d0"),
			test(null, "not ebp", "f7 d5"),
			test(null, "not ebx", "f7 d3"),
			test(null, "not ecx", "f7 d1"),
			test(null, "not edi", "f7 d7"),
			test(null, "not edx", "f7 d2"),
			test(null, "not esi", "f7 d6"),
			test(null, "not esp", "f7 d4"),
			test(null, "not r10", "49 f7 d2"),
			test(null, "not r10d", "41 f7 d2"),
			test(null, "not r11", "49 f7 d3"),
			test(null, "not r11d", "41 f7 d3"),
			test(null, "not r12", "49 f7 d4"),
			test(null, "not r12d", "41 f7 d4"),
			test(null, "not r13", "49 f7 d5"),
			test(null, "not r13d", "41 f7 d5"),
			test(null, "not r14", "49 f7 d6"),
			test(null, "not r14d", "41 f7 d6"),
			test(null, "not r15", "49 f7 d7"),
			test(null, "not r15d", "41 f7 d7"),
			test(null, "not r8", "49 f7 d0"),
			test(null, "not r8d", "41 f7 d0"),
			test(null, "not r9", "49 f7 d1"),
			test(null, "not r9d", "41 f7 d1"),
			test(null, "not rax", "48 f7 d0"),
			test(null, "not rbp", "48 f7 d5"),
			test(null, "not rbx", "48 f7 d3"),
			test(null, "not rcx", "48 f7 d1"),
			test(null, "not rdi", "48 f7 d7"),
			test(null, "not rdx", "48 f7 d2"),
			test(null, "not rsi", "48 f7 d6"),
			test(null, "not rsp", "48 f7 d4"),
			//  Neg
			test(null, "neg DWORD PTR [r8+r9*4+0x12345678]", "43 f7 9c 88 78 56 34 12"),
			test(null, "neg QWORD PTR [r8+r9*4+0x12345678]", "4b f7 9c 88 78 56 34 12"),
			test(null, "neg eax", "f7 d8"),
			test(null, "neg rbx", "48 f7 db"),
			//  Test
			test(null, "test BYTE PTR [r11+rdx*4+0x12345678],0x12", "41 f6 84 93 78 56 34 12 12"),
			test(null, "test BYTE PTR [r11+rdx*4+0x12345678],0x99", "41 f6 84 93 78 56 34 12 99"),
			test(null, "test BYTE PTR [r11+rdx*4+0x12345678],r13b", "45 84 ac 93 78 56 34 12"),
			test(null, "test BYTE PTR [r11d+edx*4+0x12345678],0x99", "67 41 f6 84 93 78 56 34 12 99"),
			test(null, "test BYTE PTR [r15+0x40],0x08", "41 f6 47 40 08"),
			test(null, "test DWORD PTR [r11+rdx*4+0x12345678],0xdeadbeef", "41 f7 84 93 78 56 34 12 ef be ad de"),
			test(null, "test DWORD PTR [r11+rdx*4+0x12345678],ebx", "41 85 9c 93 78 56 34 12"),
			test(null, "test DWORD PTR [r11d+edx*4+0x12345678],0xdeadbeef", "67 41 f7 84 93 78 56 34 12 ef be ad de"),
			test(null, "test QWORD PTR [r11+rdx*4+0x12345678],rax", "49 85 84 93 78 56 34 12"),
			test(null, "test WORD PTR [r11+rdx*4+0x12345678],0x1234", "66 41 f7 84 93 78 56 34 12 34 12"),
			test(null, "test WORD PTR [r11+rdx*4+0x12345678],r12w", "66 45 85 a4 93 78 56 34 12"),
			test(null, "test al,0x12", "a8 12"),
			test(null, "test ax,0x1234", "66 a9 34 12"),
			test(null, "test eax,0x12345678", "a9 78 56 34 12"),
			test(null, "test r9b,0x12", "41 f6 c1 12"),
			test(null, "test r9b,r9b", "45 84 c9"),
			test(null, "test r9d,r9d", "45 85 c9"),
			test(null, "test r9w,0x1234", "66 41 f7 c1 34 12"),
			test(null, "test r9w,r9w", "66 45 85 c9"),
			test(null, "test rax,0x12345678", "48 a9 78 56 34 12"),
			test(null, "test rbx,rbx", "48 85 db"),
			//  Ud2
			test(null, "ud2", "0f 0b"),
			//  Rep/repnz movs
			test(null, "movs BYTE PTR es:[rdi],BYTE PTR ds:[rsi]", "a4"),
			test(null, "movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]", "a5"),
			test(null, "movs WORD PTR es:[edi],WORD PTR ds:[esi]", "67 66 a5"),
			test(null, "rep movs BYTE PTR es:[edi],BYTE PTR ds:[esi]", "67 f3 a4"),
			test(null, "rep movs DWORD PTR es:[edi],DWORD PTR ds:[esi]", "67 f3 a5"),
			test(null, "rep movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]", "f3 a5"),
			test(null, "rep movs WORD PTR es:[edi],WORD PTR ds:[esi]", "67 66 f3 a5"),
			test(null, "rep movs WORD PTR es:[rdi],WORD PTR ds:[rsi]", "66 f3 a5"),
			test(null, "repnz movs BYTE PTR es:[edi],BYTE PTR ds:[esi]", "67 f2 a4"),
			test(null, "repnz movs DWORD PTR es:[edi],DWORD PTR ds:[esi]", "67 f2 a5"),
			test(null, "repnz movs WORD PTR es:[edi],WORD PTR ds:[esi]", "67 66 f2 a5"),
			//  Rep stos
			test(null, "rep stos BYTE PTR es:[edi],al", "67 f3 aa"),
			test(null, "rep stos BYTE PTR es:[rdi],al", "f3 aa"),
			test(null, "rep stos DWORD PTR es:[rdi],eax", "f3 ab"),
			test(null, "rep stos QWORD PTR es:[edi],rax", "67 f3 48 ab"),
			test(null, "rep stos QWORD PTR es:[rdi],rax", "f3 48 ab"),
			test(null, "stos BYTE PTR es:[edi],al", "67 aa"),
			test(null, "stos QWORD PTR es:[edi],rax", "67 48 ab"),
			//  Movdqa
			test(null, "movdqa xmm2,XMMWORD PTR [rsp+r9*4+0x12345678]", "66 42 0f 6f 94 8c 78 56 34 12"),
			//  Movaps
			test(null, "movaps XMMWORD PTR [rip+0x12345678],xmm6", "0f 29 35 78 56 34 12"),
			test(null, "movaps XMMWORD PTR [rsp+r11*4+0x12345678],xmm7", "42 0f 29 bc 9c 78 56 34 12"),
			test(null, "movaps xmm0,xmm0", "0f 28 c0"),
			test(null, "movaps xmm6,XMMWORD PTR [rip+0x12345678]", "0f 28 35 78 56 34 12"),
			test(null, "movaps xmm7,XMMWORD PTR [rsp+r11*4+0x12345678]", "42 0f 28 bc 9c 78 56 34 12"),
			test(null, "movaps xmm7,xmm5", "0f 28 fd"),
			//  Movapd
			test(null, "movapd XMMWORD PTR [rip+0x12345678],xmm6", "66 0f 29 35 78 56 34 12"),
			test(null, "movapd XMMWORD PTR [rsp+r11*4+0x12345678],xmm7", "66 42 0f 29 bc 9c 78 56 34 12"),
			test(null, "movapd xmm0,xmm0", "66 0f 28 c0"),
			test(null, "movapd xmm7,xmm5", "66 0f 28 fd"),
			//  Movq
			test(null, "movq QWORD PTR [rbp+rsi*4+0x12345678],xmm3", "66 0f d6 9c b5 78 56 34 12"),
			test(null, "movq QWORD PTR [rsi+0x12345678],xmm3", "66 0f d6 9e 78 56 34 12"),
			test(null, "movq QWORD PTR [rsi],xmm3", "66 0f d6 1e"),
			test(null, "movq mm0,r9", "49 0f 6e c1"),
			test(null, "movq mm3,rsi", "48 0f 6e de"),
			test(null, "movq xmm0,r9", "66 49 0f 6e c1"),
			test(null, "movq xmm2,rax", "66 48 0f 6e d0"),
			test(null, "movq xmm3,QWORD PTR [rbp+rsi*4+0x12345678]", "f3 0f 7e 9c b5 78 56 34 12"),
			test(null, "movq xmm6,QWORD PTR [rsi+0x12345678]", "f3 0f 7e b6 78 56 34 12"),
			test(null, "movq xmm6,QWORD PTR [rsi]", "f3 0f 7e 36"),
			//  Movhps
			test(null, "movhps xmm3,QWORD PTR [eax]", "67 0f 16 18"),
			test(null, "movhps xmm3,QWORD PTR [rax]", "0f 16 18"),
			test(null, "movhps xmm3,QWORD PTR [rbp+rsi*4+0x12345678]", "0f 16 9c b5 78 56 34 12"),
			//  Movhlps
			test(null, "movhlps xmm0,xmm0", "0f 12 c0"),
			test(null, "movhlps xmm3,xmm7", "0f 12 df"),
			//  Punpcklqdq
			test(null, "punpcklqdq xmm0,xmm0", "66 0f 6c c0"),
			test(null, "punpcklqdq xmm3,xmm9", "66 41 0f 6c d9"),
			//  Punpckhqdq
			test(null, "punpckhqdq xmm0,xmm0", "66 0f 6d c0"),
			test(null, "punpckhqdq xmm3,xmm9", "66 41 0f 6d d9"),
			//  Punpckldq
			test(null, "punpckldq xmm0,xmm0", "66 0f 62 c0"),
			test(null, "punpckldq xmm3,xmm9", "66 41 0f 62 d9"),
			//  Setae
			test(null, "setae BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 93 84 4a 78 56 34 12"),
			test(null, "setae al", "0f 93 c0"),
			test(null, "setae r8b", "41 0f 93 c0"),
			//  Setne
			test(null, "setne BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 95 84 4a 78 56 34 12"),
			test(null, "setne al", "0f 95 c0"),
			test(null, "setne r8b", "41 0f 95 c0"),
			//  Setb
			test(null, "setb BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 92 84 4a 78 56 34 12"),
			test(null, "setb al", "0f 92 c0"),
			test(null, "setb r8b", "41 0f 92 c0"),
			//  Sete
			test(null, "sete BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 94 84 4a 78 56 34 12"),
			test(null, "sete al", "0f 94 c0"),
			test(null, "sete r8b", "41 0f 94 c0"),
			//  Seta
			test(null, "seta BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 97 84 4a 78 56 34 12"),
			test(null, "seta al", "0f 97 c0"),
			test(null, "seta r8b", "41 0f 97 c0"),
			//  Setle
			test(null, "setle BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 9e 84 4a 78 56 34 12"),
			test(null, "setle al", "0f 9e c0"),
			test(null, "setle r8b", "41 0f 9e c0"),
			//  Setbe
			test(null, "setbe BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 96 84 4a 78 56 34 12"),
			test(null, "setbe al", "0f 96 c0"),
			test(null, "setbe r8b", "41 0f 96 c0"),
			//  Setl
			test(null, "setl BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 9c 84 4a 78 56 34 12"),
			test(null, "setl al", "0f 9c c0"),
			test(null, "setl r8b", "41 0f 9c c0"),
			//  Setg
			test(null, "setg BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 9f 84 4a 78 56 34 12"),
			test(null, "setg al", "0f 9f c0"),
			test(null, "setg r8b", "41 0f 9f c0"),
			//  Setge
			test(null, "setge BYTE PTR [rdx+r9*2+0x12345678]", "42 0f 9d 84 4a 78 56 34 12"),
			test(null, "setge al", "0f 9d c0"),
			test(null, "setge r8b", "41 0f 9d c0"),
			//  Movabs
			test(null, "movabs rcx,0xdeadbeef00000000", "48 b9 00 00 00 00 ef be ad de"),
			test(null, "movabs rdx,0xdeadbeefcafebabe", "48 ba be ba fe ca ef be ad de"),
			//  Movups
			test(null, "movups XMMWORD PTR [ebx+edi*8+0x12345678],xmm14", "67 44 0f 11 b4 fb 78 56 34 12"),
			test(null, "movups XMMWORD PTR [r8],xmm0", "41 0f 11 00"),
			test(null, "movups XMMWORD PTR [rbx+rdi*8+0x12345678],xmm14", "44 0f 11 b4 fb 78 56 34 12"),
			test(null, "movups xmm0,XMMWORD PTR [rbx]", "0f 10 03"),
			//  Movsd
			test(null, "movsd xmm0,QWORD PTR [r8]", "f2 41 0f 10 00"),
			test(null, "movsd xmm0,QWORD PTR [rbx]", "f2 0f 10 03"),
			test(null, "movsd xmm14,QWORD PTR [ebx+edi*8+0x12345678]", "67 f2 44 0f 10 b4 fb 78 56 34 12"),
			test(null, "movsd xmm14,QWORD PTR [rbx+rdi*8+0x12345678]", "f2 44 0f 10 b4 fb 78 56 34 12"),
			//  Endbr64
			test(null, "endbr64", "f3 0f 1e fa"),
			//  Inc
			test(null, "inc ah", "fe c4"),
			test(null, "inc al", "fe c0"),
			test(null, "inc bh", "fe c7"),
			test(null, "inc bl", "fe c3"),
			test(null, "inc bpl", "40 fe c5"),
			test(null, "inc ch", "fe c5"),
			test(null, "inc cl", "fe c1"),
			test(null, "inc dh", "fe c6"),
			test(null, "inc dil", "40 fe c7"),
			test(null, "inc dl", "fe c2"),
			test(null, "inc eax", "ff c0"),
			test(null, "inc ebp", "ff c5"),
			test(null, "inc ebx", "ff c3"),
			test(null, "inc ecx", "ff c1"),
			test(null, "inc edi", "ff c7"),
			test(null, "inc edx", "ff c2"),
			test(null, "inc esi", "ff c6"),
			test(null, "inc esp", "ff c4"),
			test(null, "inc r10", "49 ff c2"),
			test(null, "inc r10b", "41 fe c2"),
			test(null, "inc r10d", "41 ff c2"),
			test(null, "inc r11", "49 ff c3"),
			test(null, "inc r11b", "41 fe c3"),
			test(null, "inc r11d", "41 ff c3"),
			test(null, "inc r12", "49 ff c4"),
			test(null, "inc r12b", "41 fe c4"),
			test(null, "inc r12d", "41 ff c4"),
			test(null, "inc r13", "49 ff c5"),
			test(null, "inc r13b", "41 fe c5"),
			test(null, "inc r13d", "41 ff c5"),
			test(null, "inc r14", "49 ff c6"),
			test(null, "inc r14b", "41 fe c6"),
			test(null, "inc r14d", "41 ff c6"),
			test(null, "inc r15", "49 ff c7"),
			test(null, "inc r15b", "41 fe c7"),
			test(null, "inc r15d", "41 ff c7"),
			test(null, "inc r8", "49 ff c0"),
			test(null, "inc r8b", "41 fe c0"),
			test(null, "inc r8d", "41 ff c0"),
			test(null, "inc r9", "49 ff c1"),
			test(null, "inc r9b", "41 fe c1"),
			test(null, "inc r9d", "41 ff c1"),
			test(null, "inc rax", "48 ff c0"),
			test(null, "inc rbp", "48 ff c5"),
			test(null, "inc rbx", "48 ff c3"),
			test(null, "inc rcx", "48 ff c1"),
			test(null, "inc rdi", "48 ff c7"),
			test(null, "inc rdx", "48 ff c2"),
			test(null, "inc rsi", "48 ff c6"),
			test(null, "inc rsp", "48 ff c4"),
			test(null, "inc sil", "40 fe c6"),
			test(null, "inc spl", "40 fe c4"),
			//
			test(null, "inc BYTE PTR [rax+0x12345678]", "fe 80 78 56 34 12"),
			test(null, "inc BYTE PTR [rax]", "fe 00"),
			test(null, "inc BYTE PTR [rbx+rcx*2+0x12345678]", "fe 84 4b 78 56 34 12"),
			test(null, "inc DWORD PTR [rax+0x12345678]", "ff 80 78 56 34 12"),
			test(null, "inc DWORD PTR [rax]", "ff 00"),
			test(null, "inc DWORD PTR [rbp+0x12345678]", "ff 85 78 56 34 12"),
			test(null, "inc DWORD PTR [rbp+rsi*2+0x12345678]", "ff 84 75 78 56 34 12"),
			test(null, "inc DWORD PTR [rbx+0x12345678]", "ff 83 78 56 34 12"),
			test(null, "inc DWORD PTR [rcx+0x12345678]", "ff 81 78 56 34 12"),
			test(null, "inc DWORD PTR [rdi+0x12345678]", "ff 87 78 56 34 12"),
			test(null, "inc DWORD PTR [rdx+0x12345678]", "ff 82 78 56 34 12"),
			test(null, "inc DWORD PTR [rsi+0x12345678]", "ff 86 78 56 34 12"),
			test(null, "inc DWORD PTR [rsp+0x12345678]", "ff 84 24 78 56 34 12"),
			test(null, "inc QWORD PTR [r8+rdi*2+0x12345678]", "49 ff 84 78 78 56 34 12"),
			test(null, "inc QWORD PTR [rax]", "48 ff 00"),
			test(null, "inc QWORD PTR [rcx+0x12345678]", "48 ff 81 78 56 34 12"),
			test(null, "inc WORD PTR [rax+0x12345678]", "66 ff 80 78 56 34 12"),
			test(null, "inc WORD PTR [rax]", "66 ff 00"),
			test(null, "inc WORD PTR [rdx+rdi*2+0x12345678]", "66 ff 84 7a 78 56 34 12"),
			//  Dec
			test(null, "dec ah", "fe cc"),
			test(null, "dec al", "fe c8"),
			test(null, "dec bh", "fe cf"),
			test(null, "dec bl", "fe cb"),
			test(null, "dec bpl", "40 fe cd"),
			test(null, "dec ch", "fe cd"),
			test(null, "dec cl", "fe c9"),
			test(null, "dec dh", "fe ce"),
			test(null, "dec dil", "40 fe cf"),
			test(null, "dec dl", "fe ca"),
			test(null, "dec eax", "ff c8"),
			test(null, "dec ebp", "ff cd"),
			test(null, "dec ebx", "ff cb"),
			test(null, "dec ecx", "ff c9"),
			test(null, "dec edi", "ff cf"),
			test(null, "dec edx", "ff ca"),
			test(null, "dec esi", "ff ce"),
			test(null, "dec esp", "ff cc"),
			test(null, "dec r10", "49 ff ca"),
			test(null, "dec r10b", "41 fe ca"),
			test(null, "dec r10d", "41 ff ca"),
			test(null, "dec r11", "49 ff cb"),
			test(null, "dec r11b", "41 fe cb"),
			test(null, "dec r11d", "41 ff cb"),
			test(null, "dec r12", "49 ff cc"),
			test(null, "dec r12b", "41 fe cc"),
			test(null, "dec r12d", "41 ff cc"),
			test(null, "dec r13", "49 ff cd"),
			test(null, "dec r13b", "41 fe cd"),
			test(null, "dec r13d", "41 ff cd"),
			test(null, "dec r14", "49 ff ce"),
			test(null, "dec r14b", "41 fe ce"),
			test(null, "dec r14d", "41 ff ce"),
			test(null, "dec r15", "49 ff cf"),
			test(null, "dec r15b", "41 fe cf"),
			test(null, "dec r15d", "41 ff cf"),
			test(null, "dec r8", "49 ff c8"),
			test(null, "dec r8b", "41 fe c8"),
			test(null, "dec r8d", "41 ff c8"),
			test(null, "dec r9", "49 ff c9"),
			test(null, "dec r9b", "41 fe c9"),
			test(null, "dec r9d", "41 ff c9"),
			test(null, "dec rax", "48 ff c8"),
			test(null, "dec rbp", "48 ff cd"),
			test(null, "dec rbx", "48 ff cb"),
			test(null, "dec rcx", "48 ff c9"),
			test(null, "dec rdi", "48 ff cf"),
			test(null, "dec rdx", "48 ff ca"),
			test(null, "dec rsi", "48 ff ce"),
			test(null, "dec rsp", "48 ff cc"),
			test(null, "dec sil", "40 fe ce"),
			test(null, "dec spl", "40 fe cc"),
			//
			test(null, "dec BYTE PTR [rax+0x12345678]", "fe 88 78 56 34 12"),
			test(null, "dec BYTE PTR [rax]", "fe 08"),
			test(null, "dec BYTE PTR [rbx+rcx*2+0x12345678]", "fe 8c 4b 78 56 34 12"),
			test(null, "dec DWORD PTR [rbp+rsi*2+0x12345678]", "ff 8c 75 78 56 34 12"),
			test(null, "dec DWORD PTR [rsp+0x12345678]", "ff 8c 24 78 56 34 12"),
			test(null, "dec DWORD PTR [rsp]", "ff 0c 24"),
			test(null, "dec QWORD PTR [r8+rdi*2+0x12345678]", "49 ff 8c 78 78 56 34 12"),
			test(null, "dec QWORD PTR [rax]", "48 ff 08"),
			test(null, "dec QWORD PTR [rcx+0x12345678]", "48 ff 89 78 56 34 12"),
			test(null, "dec WORD PTR [rax*2+0x0]", "66 ff 0c 45 00 00 00 00"),
			test(null, "dec WORD PTR [rax+0x12345678]", "66 ff 88 78 56 34 12"),
			test(null, "dec WORD PTR [rax+rbx*1+0x0]", "66 ff 0c 18"),
			test(null, "dec WORD PTR [rax]", "66 ff 08"),
			test(null, "dec WORD PTR [rdx+rdi*2+0x12345678]", "66 ff 8c 7a 78 56 34 12"),
			//  Pshufd
			test(null, "pshufd xmm0,xmm1,0x12", "66 0f 70 c1 12"),
			//  Pshufw
			test(null, "pshufw mm0,mm1,0x12", "0f 70 c1 12"),
			//  Shufpd
			test(null, "shufpd xmm0,xmm1,0x12", "66 0f c6 c1 12"),
			//  Shufps
			test(null, "shufps xmm0,xmm1,0x12", "0f c6 c1 12"),
			//  Pxor
			test(null, "pxor xmm1,xmm15", "66 41 0f ef cf"),
			test(null, "pxor xmm4,XMMWORD PTR [rax+r11*4+0x12345678]", "66 42 0f ef a4 98 78 56 34 12"),
			test(null, "pxor xmm7,xmm7", "66 0f ef ff"),
			//  Por
			test(null, "por xmm1,xmm15", "66 41 0f eb cf"),
			test(null, "por xmm4,XMMWORD PTR [rax+r11*4+0x12345678]", "66 42 0f eb a4 98 78 56 34 12"),
			test(null, "por xmm7,xmm7", "66 0f eb ff"),
			//  Pand
			test(null, "pand xmm1,xmm15", "66 41 0f db cf"),
			test(null, "pand xmm4,XMMWORD PTR [rax+r11*4+0x12345678]", "66 42 0f db a4 98 78 56 34 12"),
			test(null, "pand xmm7,xmm7", "66 0f db ff"),
			//  Paddq
			test(null, "paddq xmm1,xmm15", "66 41 0f d4 cf"),
			test(null, "paddq xmm4,XMMWORD PTR [rax+r11*4+0x12345678]", "66 42 0f d4 a4 98 78 56 34 12"),
			test(null, "paddq xmm4,XMMWORD PTR [rdi]", "66 0f d4 27"),
			test(null, "paddq xmm7,xmm7", "66 0f d4 ff"),
			//  Psubq
			test(null, "psubq xmm1,xmm15", "66 41 0f fb cf"),
			test(null, "psubq xmm4,XMMWORD PTR [rax+r11*4+0x12345678]", "66 42 0f fb a4 98 78 56 34 12"),
			test(null, "psubq xmm4,XMMWORD PTR [rdi]", "66 0f fb 27"),
			test(null, "psubq xmm7,xmm7", "66 0f fb ff"),
			//  Cvtsi2sd
			test(null, "cvtsi2sd xmm2,rdi", "f2 48 0f 2a d7"),
			test(null, "cvtsi2sd xmm8,eax", "f2 44 0f 2a c0"),
			//  Divsd
			test(null, "divsd xmm0,xmm0", "f2 0f 5e c0"),
			test(null, "divsd xmm8,xmm11", "f2 45 0f 5e c3"),
			//  Addsd
			test(null, "addsd xmm0,xmm0", "f2 0f 58 c0"),
			test(null, "addsd xmm8,xmm11", "f2 45 0f 58 c3"),
			//  Xorps
			test(null, "xorps xmm0,xmm0", "0f 57 c0"),
			test(null, "xorps xmm8,xmm11", "45 0f 57 c3"),
			//  Ucomisd
			test(null, "ucomisd xmm13,QWORD PTR [rip+0x12345678]", "66 44 0f 2e 2d 78 56 34 12"),
			//  Ucomiss
			test(null, "ucomiss xmm13,DWORD PTR [rip+0x12345678]", "44 0f 2e 2d 78 56 34 12"),
			//  BTx
			test(null, "bt edx,0x12", "0f ba e2 12"),
			test(null, "bt edx,esi", "0f a3 f2"),
			test(null, "bt rdx,0x12", "48 0f ba e2 12"),
			test(null, "bt rdx,rdi", "48 0f a3 fa"),
			test(null, "btc ecx,0x12", "0f ba f9 12"),
			test(null, "btc ecx,r9d", "44 0f bb c9"),
			test(null, "btc rcx,0x12", "48 0f ba f9 12"),
			test(null, "btc rcx,r10", "4c 0f bb d1"),
			test(null, "btr ebx,0x12", "0f ba f3 12"),
			test(null, "btr ebx,r11d", "44 0f b3 db"),
			test(null, "btr rbx,0x12", "48 0f ba f3 12"),
			test(null, "btr rbx,r12", "4c 0f b3 e3"),
			test(null, "bts eax,0x12", "0f ba e8 12"),
			test(null, "bts eax,r13d", "44 0f ab e8"),
			test(null, "bts rax,0x12", "48 0f ba e8 12"),
			test(null, "bts rax,r14", "4c 0f ab f0"),
			//  Xgetbv
			test(null, "xgetbv", "0f 01 d0"),
			//  Xchg
			test(null, "xchg al,cl", "86 c8"),
			test(null, "xchg bh,cl", "86 cf"),
			test(null, "xchg di,ax", "66 97"),
			test(null, "xchg ebp,eax", "95"),
			test(null, "xchg ebx,eax", "93"),
			test(null, "xchg ebx,r9d", "44 87 cb"),
			test(null, "xchg ecx,eax", "91"),
			test(null, "xchg edi,eax", "97"),
			test(null, "xchg edx,eax", "92"),
			test(null, "xchg esi,eax", "96"),
			test(null, "xchg esp,eax", "94"),
			test(null, "xchg r10,rax", "49 92"),
			test(null, "xchg r10d,eax", "41 92"),
			test(null, "xchg r11,rax", "49 93"),
			test(null, "xchg r11d,eax", "41 93"),
			test(null, "xchg r12,rax", "49 94"),
			test(null, "xchg r12d,eax", "41 94"),
			test(null, "xchg r13,rax", "49 95"),
			test(null, "xchg r13d,eax", "41 95"),
			test(null, "xchg r14,rax", "49 96"),
			test(null, "xchg r14d,eax", "41 96"),
			test(null, "xchg r15,rax", "49 97"),
			test(null, "xchg r15d,eax", "41 97"),
			test(null, "xchg r8,rax", "49 90"),
			test(null, "xchg r8d,eax", "41 90"),
			test(null, "xchg r9,rax", "49 91"),
			test(null, "xchg r9d,eax", "41 91"),
			test(null, "xchg rbp,rax", "48 95"),
			test(null, "xchg rbx,r9", "4c 87 cb"),
			test(null, "xchg rbx,rax", "48 93"),
			test(null, "xchg rbx,rcx", "48 87 cb"),
			test(null, "xchg rcx,rax", "48 91"),
			test(null, "xchg rdi,rax", "48 97"),
			test(null, "xchg rdx,rax", "48 92"),
			test(null, "xchg rsi,rax", "48 96"),
			test(null, "xchg rsp,rax", "48 94"),
			test(null, "xchg si,di", "66 87 fe"),
			//
			test(null, "xchg BYTE PTR [rax+rbx*2+0x12345678],ah", "86 a4 58 78 56 34 12"),
			test(null, "xchg BYTE PTR [rax+rbx*2+0x12345678],al", "86 84 58 78 56 34 12"),
			test(null, "xchg DWORD PTR [rax+rbx*2+0x12345678],eax", "87 84 58 78 56 34 12"),
			test(null, "xchg QWORD PTR [rax+rbx*2+0x12345678],rax", "48 87 84 58 78 56 34 12"),
			test(null, "xchg WORD PTR [rax+rbx*2+0x12345678],ax", "66 87 84 58 78 56 34 12"),
			//  Bswap
			test(null, "bswap eax", "0f c8"),
			test(null, "bswap ebp", "0f cd"),
			test(null, "bswap ebx", "0f cb"),
			test(null, "bswap ecx", "0f c9"),
			test(null, "bswap edi", "0f cf"),
			test(null, "bswap edx", "0f ca"),
			test(null, "bswap esi", "0f ce"),
			test(null, "bswap esp", "0f cc"),
			test(null, "bswap r10", "49 0f ca"),
			test(null, "bswap r10d", "41 0f ca"),
			test(null, "bswap r11", "49 0f cb"),
			test(null, "bswap r11d", "41 0f cb"),
			test(null, "bswap r12", "49 0f cc"),
			test(null, "bswap r12d", "41 0f cc"),
			test(null, "bswap r13", "49 0f cd"),
			test(null, "bswap r13d", "41 0f cd"),
			test(null, "bswap r14", "49 0f ce"),
			test(null, "bswap r14d", "41 0f ce"),
			test(null, "bswap r15", "49 0f cf"),
			test(null, "bswap r15d", "41 0f cf"),
			test(null, "bswap r8", "49 0f c8"),
			test(null, "bswap r8d", "41 0f c8"),
			test(null, "bswap r9", "49 0f c9"),
			test(null, "bswap r9d", "41 0f c9"),
			test(null, "bswap rax", "48 0f c8"),
			test(null, "bswap rbp", "48 0f cd"),
			test(null, "bswap rbx", "48 0f cb"),
			test(null, "bswap rcx", "48 0f c9"),
			test(null, "bswap rdi", "48 0f cf"),
			test(null, "bswap rdx", "48 0f ca"),
			test(null, "bswap rsi", "48 0f ce"),
			test(null, "bswap rsp", "48 0f cc"),
			//  Prefetch
			test(null, "prefetchnta BYTE PTR [eax]", "67 0f 18 00"),
			test(null, "prefetchnta BYTE PTR [ebx+r11d*4+0x12345678]", "67 42 0f 18 84 9b 78 56 34 12"),
			test(null, "prefetchnta BYTE PTR [r9+r11*4+0x12345678]", "43 0f 18 84 99 78 56 34 12"),
			test(null, "prefetchnta BYTE PTR [r9+rcx*4+0x12345678]", "41 0f 18 84 89 78 56 34 12"),
			test(null, "prefetchnta BYTE PTR [r9d+ecx*4+0x12345678]", "67 41 0f 18 84 89 78 56 34 12"),
			test(null, "prefetchnta BYTE PTR [r9d+r11d*4+0x12345678]", "67 43 0f 18 84 99 78 56 34 12"),
			test(null, "prefetchnta BYTE PTR [rax]", "0f 18 00"),
			test(null, "prefetchnta BYTE PTR [rbx+r11*4+0x12345678]", "42 0f 18 84 9b 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [eax]", "67 0f 18 08"),
			test(null, "prefetcht0 BYTE PTR [ebx+r11d*4+0x12345678]", "67 42 0f 18 8c 9b 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [r9+r11*4+0x12345678]", "43 0f 18 8c 99 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [r9+rcx*4+0x12345678]", "41 0f 18 8c 89 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [r9d+ecx*4+0x12345678]", "67 41 0f 18 8c 89 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [r9d+r11d*4+0x12345678]", "67 43 0f 18 8c 99 78 56 34 12"),
			test(null, "prefetcht0 BYTE PTR [rax]", "0f 18 08"),
			test(null, "prefetcht0 BYTE PTR [rbx+r11*4+0x12345678]", "42 0f 18 8c 9b 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [eax]", "67 0f 18 10"),
			test(null, "prefetcht1 BYTE PTR [ebx+r11d*4+0x12345678]", "67 42 0f 18 94 9b 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [r9+r11*4+0x12345678]", "43 0f 18 94 99 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [r9+rcx*4+0x12345678]", "41 0f 18 94 89 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [r9d+ecx*4+0x12345678]", "67 41 0f 18 94 89 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [r9d+r11d*4+0x12345678]", "67 43 0f 18 94 99 78 56 34 12"),
			test(null, "prefetcht1 BYTE PTR [rax]", "0f 18 10"),
			test(null, "prefetcht1 BYTE PTR [rbx+r11*4+0x12345678]", "42 0f 18 94 9b 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [eax]", "67 0f 18 18"),
			test(null, "prefetcht2 BYTE PTR [ebx+r11d*4+0x12345678]", "67 42 0f 18 9c 9b 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [r9+r11*4+0x12345678]", "43 0f 18 9c 99 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [r9+rcx*4+0x12345678]", "41 0f 18 9c 89 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [r9d+ecx*4+0x12345678]", "67 41 0f 18 9c 89 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [r9d+r11d*4+0x12345678]", "67 43 0f 18 9c 99 78 56 34 12"),
			test(null, "prefetcht2 BYTE PTR [rax]", "0f 18 18"),
			test(null, "prefetcht2 BYTE PTR [rbx+r11*4+0x12345678]", "42 0f 18 9c 9b 78 56 34 12"),
			//  Cmpxchg
			test(null, "cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh", "0f b0 b4 98 78 56 34 12"),
			test(null, "cmpxchg BYTE PTR [rsi],bpl", "40 0f b0 2e"),
			test(null, "cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d", "44 0f b1 94 98 78 56 34 12"),
			test(null, "cmpxchg DWORD PTR [rsi],ecx", "0f b1 0e"),
			test(null, "cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi", "48 0f b1 bc 98 78 56 34 12"),
			test(null, "cmpxchg QWORD PTR [rsi],r9", "4c 0f b1 0e"),
			test(null, "cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w", "66 44 0f b1 bc 98 78 56 34 12"),
			test(null, "cmpxchg WORD PTR [rsi],dx", "66 0f b1 16"),
			test(null, "lock cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh", "f0 0f b0 b4 98 78 56 34 12"),
			test(null, "lock cmpxchg BYTE PTR [rsi],bpl", "f0 40 0f b0 2e"),
			test(null, "lock cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d", "f0 44 0f b1 94 98 78 56 34 12"),
			test(null, "lock cmpxchg DWORD PTR [rsi],ecx", "f0 0f b1 0e"),
			test(null, "lock cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi", "f0 48 0f b1 bc 98 78 56 34 12"),
			test(null, "lock cmpxchg QWORD PTR [rsi],r9", "f0 4c 0f b1 0e"),
			test(null, "lock cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w", "66 f0 44 0f b1 bc 98 78 56 34 12"),
			test(null, "lock cmpxchg WORD PTR [rsi],dx", "66 f0 0f b1 16"),
			//  Xadd
			test(null, "lock xadd BYTE PTR [rax+rbx*4+0x12345678],dh", "f0 0f c0 b4 98 78 56 34 12"),
			test(null, "lock xadd BYTE PTR [rsi],bpl", "f0 40 0f c0 2e"),
			test(null, "lock xadd DWORD PTR [rax+rbx*4+0x12345678],r10d", "f0 44 0f c1 94 98 78 56 34 12"),
			test(null, "lock xadd DWORD PTR [rsi],ecx", "f0 0f c1 0e"),
			test(null, "lock xadd QWORD PTR [rax+rbx*4+0x12345678],rdi", "f0 48 0f c1 bc 98 78 56 34 12"),
			test(null, "lock xadd QWORD PTR [rsi],r9", "f0 4c 0f c1 0e"),
			test(null, "lock xadd WORD PTR [rax+rbx*4+0x12345678],r15w", "66 f0 44 0f c1 bc 98 78 56 34 12"),
			test(null, "lock xadd WORD PTR [rsi],dx", "66 f0 0f c1 16"),
			test(null, "xadd BYTE PTR [rax+rbx*4+0x12345678],dh", "0f c0 b4 98 78 56 34 12"),
			test(null, "xadd BYTE PTR [rsi],bpl", "40 0f c0 2e"),
			test(null, "xadd DWORD PTR [rax+rbx*4+0x12345678],r10d", "44 0f c1 94 98 78 56 34 12"),
			test(null, "xadd DWORD PTR [rsi],ecx", "0f c1 0e"),
			test(null, "xadd QWORD PTR [rax+rbx*4+0x12345678],rdi", "48 0f c1 bc 98 78 56 34 12"),
			test(null, "xadd QWORD PTR [rsi],r9", "4c 0f c1 0e"),
			test(null, "xadd WORD PTR [rax+rbx*4+0x12345678],r15w", "66 44 0f c1 bc 98 78 56 34 12"),
			test(null, "xadd WORD PTR [rsi],dx", "66 0f c1 16"),
			//  Pcmpeqd
			test(null, "pcmpeqd xmm0,xmm0", "66 0f 76 c0"),
			test(null, "pcmpeqd xmm3,xmm11", "66 41 0f 76 db"),
			//  Rdrand
			test(null, "rdrand ax", "66 0f c7 f0"),
			test(null, "rdrand eax", "0f c7 f0"),
			test(null, "rdrand r11", "49 0f c7 f3"),
			test(null, "rdrand r12d", "41 0f c7 f4"),
			test(null, "rdrand r13w", "66 41 0f c7 f5"),
			test(null, "rdrand rax", "48 0f c7 f0"),
			//  Rdseed
			test(null, "rdseed ax", "66 0f c7 f8"),
			test(null, "rdseed eax", "0f c7 f8"),
			test(null, "rdseed r11", "49 0f c7 fb"),
			test(null, "rdseed r12d", "41 0f c7 fc"),
			test(null, "rdseed r13w", "66 41 0f c7 fd"),
			test(null, "rdseed rax", "48 0f c7 f8"),
			//  Rdsspq
			test(null, "rdsspq r11", "f3 49 0f 1e cb"),
			test(null, "rdsspq rax", "f3 48 0f 1e c8"),
			//  Incsspq
			test(null, "incsspq r11", "f3 49 0f ae eb"),
			test(null, "incsspq rax", "f3 48 0f ae e8"),
			//  Lahf
			test(null, "lahf", "9f"),
			//  Sahf
			test(null, "sahf", "9e"));
}
