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
package com.ledmington.emu;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionDecoderV1;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RelativeOffset;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

/** Emulator of an x86 CPU. */
public final class X86Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("x86-emu");

    private X86Emulator() {}

    /**
     * Automatically fetches instruction from the emulated memory and executes them.
     *
     * @param mem The emulated memory to read instructions and data from.
     * @param entryPointVirtualAddress The virtual address where to start execution.
     * @param highestAddress The highest address of the allocated memory.
     * @param stackSize The size in bytes of the stack.
     */
    public static void run(
            final MemoryController mem,
            final long entryPointVirtualAddress,
            final long highestAddress,
            final long stackSize) {
        final X86RegisterFile rf = new X86RegisterFile();
        final InstructionFetcher instructionFetcher = new InstructionFetcher(mem, rf);
        final InstructionDecoder dec = new InstructionDecoderV1(instructionFetcher);

        instructionFetcher.setPosition(entryPointVirtualAddress);
        logger.debug("Entry point virtual address : 0x%x", entryPointVirtualAddress);

        // we make RSP point at the last 8 bytes of allocated memory
        rf.set(Register64.RSP, highestAddress + stackSize - 8L);

        while (true) {
            final Instruction inst = dec.decode();

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    switch (((Register) inst.firstOperand()).bits()) {
                        case 8 -> {
                            final byte r1 = rf.get((Register8) inst.firstOperand());
                            final byte r2 = rf.get((Register8) inst.secondOperand());
                            rf.set((Register8) inst.firstOperand(), BitUtils.xor(r1, r2));
                        }
                        case 32 -> {
                            final int r1 = rf.get((Register32) inst.firstOperand());
                            final int r2 = rf.get((Register32) inst.secondOperand());
                            rf.set((Register32) inst.firstOperand(), r1 ^ r2);
                        }
                        default -> throw new IllegalArgumentException(String.format(
                                "Don't know what to do when XOR has %,d bits",
                                ((Register) inst.firstOperand()).bits()));
                    }
                }
                case AND -> {
                    switch (inst.firstOperand()) {
                        case IndirectOperand io -> {
                            // final long result = computeIndirectOperand(io);
                            throw new Error("Not implemented");
                        }
                        case Register r -> {
                            if (r instanceof Register64 r64) {
                                final long imm64 = ((Immediate) inst.secondOperand()).asLong();
                                rf.set(r64, rf.get(r64) & imm64);
                            } else {
                                throw new IllegalArgumentException(
                                        String.format("Don't know what to do when AND has %,d bits", r.bits()));
                            }
                        }
                        default -> throw new IllegalArgumentException(
                                String.format("Unknown type of first operand '%s'", inst.firstOperand()));
                    }
                }
                case JMP -> instructionFetcher.setPosition(
                        instructionFetcher.getPosition() + ((RelativeOffset) inst.firstOperand()).getValue());
                case MOV -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final Register64 src = (Register64) inst.secondOperand();
                    rf.set(dest, rf.get(src));
                }
                case PUSH -> {
                    final Register64 src = (Register64) inst.firstOperand();
                    final long rsp = rf.get(Register64.RSP);
                    mem.write(rsp, rf.get(src));
                    // the stack "grows downward"
                    rf.set(Register64.RSP, rsp - 8L);
                }
                case POP -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final long rsp = rf.get(Register64.RSP);
                    rf.set(dest, mem.read8(rsp));
                    // the stack "grows downward"
                    rf.set(Register64.RSP, rsp + 8L);
                }
                case LEA -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final IndirectOperand src = (IndirectOperand) inst.secondOperand();
                    final long result = computeIndirectOperand(rf, src);
                    rf.set(dest, result);
                }
                case CALL -> {
                    // TODO: check this
                    final IndirectOperand src = (IndirectOperand) inst.firstOperand();
                    final long result = computeIndirectOperand(rf, src);
                    rf.set(Register64.RIP, result);
                }
                case ENDBR64 -> logger.warning("ENDBR64 not implemented");
                case HLT -> {
                    return;
                }
                default -> throw new IllegalStateException(
                        String.format("Unknwon instruction %s", inst.toIntelSyntax()));
            }
        }
    }

    private static long computeIndirectOperand(final X86RegisterFile rf, final IndirectOperand io) {
        return ((io.base() == null) ? 0L : rf.get((Register64) io.base()))
                + ((io.index() == null) ? 0L : rf.get((Register64) io.index())) * io.scale()
                + io.getDisplacement();
    }
}
