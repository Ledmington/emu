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

import java.util.Objects;

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
import com.ledmington.utils.SuppressFBWarnings;

/** Emulator of an x86 CPU. */
public class X86Cpu implements X86Emulator {

	private static final MiniLogger logger = MiniLogger.getLogger("x86-emu");

	protected enum State {
		RUNNING,
		HALTED
	}

	private final RegisterFile rf = new X86RegisterFile();
	private final MemoryController mem;
	private final InstructionFetcher instFetch;
	private final InstructionDecoder dec;
	protected State state = State.RUNNING;

	/**
	 * Creates a new x86 CPU with the given memory controller.
	 *
	 * @param mem The object to be used to access the memory.
	 */
	public X86Cpu(final MemoryController mem) {
		this.mem = Objects.requireNonNull(mem);
		this.instFetch = new InstructionFetcher(mem, rf);
		this.dec = new InstructionDecoderV1(instFetch);
	}

	@Override
	public void execute() {
		state = State.RUNNING;
		while (state != State.HALTED) {
			executeOne();
		}
	}

	@Override
	public void executeOne() {
		executeOne(dec.decode());
	}

	@Override
	public void executeOne(final Instruction inst) {
		logger.debug(inst.toIntelSyntax());
		switch (inst.opcode()) {
			case SUB -> {
				if (inst.firstOperand() instanceof Register64 op1) {
					final long r1 = rf.get(op1);
					final long r2 =
							switch (inst.secondOperand()) {
								case Register64 op2 -> rf.get(op2);
								case Immediate imm -> imm.asLong();
								default -> throw new IllegalArgumentException();
							};
					rf.set(op1, r1 - r2);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SUB has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case ADD -> {
				final Register64 op1 = (Register64) inst.firstOperand();
				final long r1 = rf.get(op1);
				final long r2 =
						switch (inst.secondOperand()) {
							case Register64 op2 -> rf.get(op2);
							case Immediate imm -> imm.asLong();
							default -> throw new IllegalArgumentException(
									String.format("Unknown second argument type %s", inst.secondOperand()));
						};
				rf.set(op1, r1 + r2);
			}
			case SHR -> {
				if (inst.firstOperand() instanceof Register64) {
					final long r1 = rf.get((Register64) inst.firstOperand());
					final byte imm = ((Immediate) inst.secondOperand()).asByte();
					rf.set((Register64) inst.firstOperand(), r1 >>> imm);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SHR has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case SAR -> {
				if (inst.firstOperand() instanceof Register64 op1) {
					final long r1 = rf.get(op1);
					final byte imm = ((Immediate) inst.secondOperand()).asByte();
					final long result = r1 >> imm;
					rf.set(op1, result);
					rf.set(RFlags.Zero, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SAR has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case SHL -> {
				if (inst.firstOperand() instanceof Register64) {
					final long r1 = rf.get((Register64) inst.firstOperand());
					final byte imm = rf.get((Register8) inst.secondOperand());
					rf.set((Register64) inst.firstOperand(), r1 << imm);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SHL has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case XOR -> {
				if (inst.firstOperand() instanceof Register op1 && inst.secondOperand() instanceof Register op2) {
					switch (op1.bits()) {
						case 8 -> {
							final byte r1 = rf.get((Register8) op1);
							final byte r2 = rf.get((Register8) op2);
							rf.set((Register8) op1, BitUtils.xor(r1, r2));
						}
						case 32 -> {
							final int r1 = rf.get((Register32) op1);
							final int r2 = rf.get((Register32) op2);
							rf.set((Register32) op1, r1 ^ r2);
						}
						default -> throw new IllegalArgumentException(
								String.format("Don't know what to do when XOR has %,d bits", op1.bits()));
					}
				}
			}
			case AND -> {
				if (inst.firstOperand() instanceof Register64 r64) {
					final long imm64 = ((Immediate) inst.secondOperand()).asLong();
					rf.set(r64, rf.get(r64) & imm64);
				} else {
					throw new IllegalArgumentException(
							String.format("Unknown type of first operand '%s'", inst.firstOperand()));
				}
			}
			case TEST -> {
				final long r1 = rf.get((Register64) inst.firstOperand());
				final long r2 = rf.get((Register64) inst.secondOperand());
				rf.set(RFlags.Zero, (r1 & r2) == 0L);
			}
			case JMP -> {
				final long offset = (inst.firstOperand() instanceof Register64)
						? rf.get((Register64) inst.firstOperand())
						: ((RelativeOffset) inst.firstOperand()).getValue();
				instFetch.setPosition(instFetch.getPosition() + offset);
			}
			case JE -> {
				if (rf.isSet(RFlags.Zero)) {
					instFetch.setPosition(instFetch.getPosition() + ((RelativeOffset) inst.firstOperand()).getValue());
				}
			}
			case MOV -> {
				final Register64 dest = (Register64) inst.firstOperand();
				switch (inst.secondOperand()) {
					case Register64 src -> rf.set(dest, rf.get(src));
					case IndirectOperand io -> rf.set(dest, mem.read8(computeIndirectOperand(rf, io)));
					case Immediate imm -> rf.set(dest, imm.asLong());
					default -> throw new IllegalArgumentException(
							String.format("Unknown argument type '%s'", inst.secondOperand()));
				}
			}
			case PUSH -> {
				final long value =
						switch (inst.firstOperand()) {
							case Register64 r64 -> rf.get(r64);
							case Immediate imm -> imm.asLong();
							default -> throw new IllegalArgumentException("Unexpected value: " + inst.firstOperand());
						};

				final long rsp = rf.get(Register64.RSP);
				mem.write(rsp, value);
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
				final long result;
				if (inst.firstOperand() instanceof IndirectOperand iop) {
					result = computeIndirectOperand(rf, iop);
				} else if (inst.firstOperand() instanceof Register64 r64) {
					result = rf.get(r64);
				} else {
					throw new IllegalArgumentException();
				}
				rf.set(Register64.RIP, result);
			}
			case RET -> {
				// TODO: check this
				final long prev = rf.get(Register64.RSP) + 8L;

				// If we read 0x0, we have exhausted the stack
				if (mem.read8(prev) == 0L) {
					state = State.HALTED;
				} else {
					rf.set(Register64.RSP, prev);
					rf.set(Register64.RIP, mem.read8(prev));
				}
			}
			case ENDBR64 -> logger.warning("ENDBR64 not implemented");
			case HLT -> state = State.HALTED;
			default -> throw new IllegalStateException(String.format("Unknwon instruction %s", inst.toIntelSyntax()));
		}
	}

	private long computeIndirectOperand(final RegisterFile rf, final IndirectOperand io) {
		return ((io.base() == null) ? 0L : rf.get((Register64) io.base()))
				+ ((io.index() == null) ? 0L : rf.get((Register64) io.index())) * io.scale()
				+ io.getDisplacement();
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "We know that this object is immutable.")
	public ImmutableRegisterFile getRegisters() {
		return rf;
	}
}
