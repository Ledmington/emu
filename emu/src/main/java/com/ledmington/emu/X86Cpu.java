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
package com.ledmington.emu;

import java.util.Objects;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionDecoderV1;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register16;
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

	/** The state of the CPU. */
	protected enum State {

		/** The default state of the CPU, meaning that it is able to execute instructions. */
		RUNNING,

		/** The state in which the CPU has completed execution. */
		HALTED
	}

	private final RegisterFile rf = new X86RegisterFile();
	private final MemoryController mem;
	private final InstructionFetcher instFetch;
	private final InstructionDecoder dec;

	/**
	 * The current state of the CPU. Children classes can modify this field before executing instructions or to forcibly
	 * terminate execution.
	 */
	protected State state = State.RUNNING;

	/**
	 * Creates a new x86 CPU with the given memory controller.
	 *
	 * @param mem The object to be used to access the memory.
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "At the moment we need this object as it is.")
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

	private void assertIsRunning() {
		if (state != State.RUNNING) {
			throw new IllegalStateException("Cannot execute instruction if the state is not RUNNING.");
		}
	}

	@Override
	public void executeOne() {
		assertIsRunning();
		executeOne(dec.decode());
	}

	@Override
	public void executeOne(final Instruction inst) {
		assertIsRunning();
		logger.debug(inst.toIntelSyntax());
		switch (inst.opcode()) {
			case SUB -> {
				switch (inst.firstOperand()) {
					case Register8 op1 -> {
						final byte r1 = rf.get(op1);
						final byte r2 = rf.get((Register8) inst.secondOperand());
						final byte result = BitUtils.asByte(r1 - r2);
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register16 op1 -> {
						final short r1 = rf.get(op1);
						final short r2 = rf.get((Register16) inst.secondOperand());
						final short result = BitUtils.asShort(r1 - r2);
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register32 op1 -> {
						final int r1 = rf.get(op1);
						final int r2 = rf.get((Register32) inst.secondOperand());
						final int result = r1 - r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register64 op1 -> {
						final long r1 = rf.get(op1);
						final long r2 =
								switch (inst.secondOperand()) {
									case Register64 op2 -> rf.get(op2);
									case Immediate imm -> imm.asLong();
									default -> throw new IllegalArgumentException(
											String.format("Unknown second argument type %s", inst.secondOperand()));
								};
						final long result = r1 - r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0L);
					}
					case IndirectOperand iop -> {
						final long address = computeIndirectOperand(rf, iop);
						final Register8 op2 = (Register8) inst.secondOperand();
						final byte r1 = mem.read(address);
						final byte r2 = rf.get(op2);
						final byte result = BitUtils.asByte(r1 + r2);
						mem.write(address, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					default -> throw new IllegalArgumentException(
							String.format("Don't know what to do with ADD and %s.", inst.firstOperand()));
				}
			}
			case ADD -> {
				switch (inst.firstOperand()) {
					case Register8 op1 -> {
						final byte r1 = rf.get(op1);
						final byte r2 = rf.get((Register8) inst.secondOperand());
						final byte result = BitUtils.asByte(r1 + r2);
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register16 op1 -> {
						final short r1 = rf.get(op1);
						final short r2 = rf.get((Register16) inst.secondOperand());
						final short result = BitUtils.asShort(r1 + r2);
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register32 op1 -> {
						final int r1 = rf.get(op1);
						final int r2 = rf.get((Register32) inst.secondOperand());
						final int result = r1 + r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					case Register64 op1 -> {
						final long r1 = rf.get(op1);
						final long r2 =
								switch (inst.secondOperand()) {
									case Register64 op2 -> rf.get(op2);
									case Immediate imm -> imm.asLong();
									default -> throw new IllegalArgumentException(
											String.format("Unknown second argument type %s", inst.secondOperand()));
								};
						final long result = r1 + r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0L);
					}
					case IndirectOperand iop -> {
						final long address = computeIndirectOperand(rf, iop);
						final Register8 op2 = (Register8) inst.secondOperand();
						final byte r1 = mem.read(address);
						final byte r2 = rf.get(op2);
						final byte result = BitUtils.asByte(r1 + r2);
						mem.write(address, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					default -> throw new IllegalArgumentException(
							String.format("Don't know what to do with ADD and %s.", inst.firstOperand()));
				}
			}
			case SHR -> {
				if (inst.firstOperand() instanceof Register64) {
					final long r1 = rf.get((Register64) inst.firstOperand());
					final byte imm = ((Immediate) inst.secondOperand()).asByte();
					final long result = r1 >>> imm;
					rf.set((Register64) inst.firstOperand(), result);
					rf.resetFlags();
					rf.set(RFlags.ZERO, result == 0L);
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
					rf.resetFlags();
					rf.set(RFlags.ZERO, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SAR has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case SHL -> {
				if (inst.firstOperand() instanceof Register64 op1) {
					final long r1 = rf.get(op1);
					final byte imm = rf.get((Register8) inst.secondOperand());
					final long result = r1 << imm;
					rf.set(op1, result);
					rf.resetFlags();
					rf.set(RFlags.ZERO, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SHL has %,d bits", ((Register) inst.firstOperand()).bits()));
				}
			}
			case XOR -> {
				if (inst.firstOperand() instanceof Register8 r1 && inst.secondOperand() instanceof Register8 r2) {
					final byte result = BitUtils.xor(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register16 r1
						&& inst.secondOperand() instanceof Register16 r2) {
					final short result = BitUtils.xor(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register32 r1
						&& inst.secondOperand() instanceof Register32 r2) {
					final int result = rf.get(r1) ^ rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register64 r1
						&& inst.secondOperand() instanceof Register64 r2) {
					final long result = rf.get(r1) ^ rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format("Don't know what to do with %s.", inst));
				}
			}
			case AND -> {
				if (inst.firstOperand() instanceof Register8 r1 && inst.secondOperand() instanceof Register8 r2) {
					final byte result = BitUtils.and(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register16 r1
						&& inst.secondOperand() instanceof Register16 r2) {
					final short result = BitUtils.and(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register32 r1
						&& inst.secondOperand() instanceof Register32 r2) {
					final int result = rf.get(r1) & rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof Register64 r1
						&& inst.secondOperand() instanceof Register64 r2) {
					final long result = rf.get(r1) & rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0L);
				} else if (inst.firstOperand() instanceof Register64 r1
						&& inst.secondOperand() instanceof Immediate imm) {
					rf.set(r1, rf.get(r1) & imm.asLong());
				} else {
					throw new IllegalArgumentException(String.format("Don't know what to do with %s.", inst));
				}
			}
			case CMP -> {
				final long address = computeIndirectOperand(rf, (IndirectOperand) inst.firstOperand());
				final long a = mem.read8(address);
				final long b = ((Immediate) inst.secondOperand()).asLong();
				final long result = a - b;
				rf.resetFlags();
				rf.set(RFlags.ZERO, result == 0L);
				rf.set(RFlags.SIGN, result < 0L);
			}
			case TEST -> {
				final long r1 = rf.get((Register64) inst.firstOperand());
				final long r2 = rf.get((Register64) inst.secondOperand());
				rf.resetFlags();
				rf.set(RFlags.ZERO, (r1 & r2) == 0L);
			}
			case JMP -> {
				final long offset = (inst.firstOperand() instanceof Register64)
						? rf.get((Register64) inst.firstOperand())
						: ((RelativeOffset) inst.firstOperand()).getValue();
				instFetch.setPosition(instFetch.getPosition() + offset);
			}
			case JE -> {
				if (rf.isSet(RFlags.ZERO)) {
					instFetch.setPosition(instFetch.getPosition() + ((RelativeOffset) inst.firstOperand()).getValue());
				}
			}
			case JNE -> {
				if (!rf.isSet(RFlags.ZERO)) {
					instFetch.setPosition(instFetch.getPosition() + ((RelativeOffset) inst.firstOperand()).getValue());
				}
			}
			case MOV -> {
				if (inst.firstOperand() instanceof Register64 op1 && inst.secondOperand() instanceof Register64 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof Register32 op1
						&& inst.secondOperand() instanceof Register32 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof Register16 op1
						&& inst.secondOperand() instanceof Register16 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof Register8 op1
						&& inst.secondOperand() instanceof Register8 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof Register64 op1
						&& inst.secondOperand() instanceof Immediate imm) {
					rf.set(op1, imm.bits() == 32 ? BitUtils.asLong(imm.asInt()) : imm.asLong());
				} else if (inst.firstOperand() instanceof Register64 op1
						&& inst.secondOperand() instanceof IndirectOperand iop) {
					final long address = computeIndirectOperand(rf, iop);
					rf.set(op1, mem.read8(address));
				} else if (inst.firstOperand() instanceof IndirectOperand iop
						&& inst.secondOperand() instanceof Register64 op2) {
					final long address = computeIndirectOperand(rf, iop);
					mem.write(address, rf.get(op2));
				} else {
					throw new IllegalArgumentException(
							String.format("Unknown argument type '%s'", inst.secondOperand()));
				}
			}
			case MOVSXD -> {
				final int x = rf.get((Register32) inst.secondOperand());
				rf.set((Register64) inst.firstOperand(), x);
			}
			case PUSH -> {
				final long value =
						switch (inst.firstOperand()) {
							case Register64 r64 -> rf.get(r64);
							case Immediate imm -> imm.asLong();
							default -> throw new IllegalArgumentException(
									String.format("Unexpected argument %s", inst.firstOperand()));
						};

				final long rsp = rf.get(Register64.RSP);
				// the stack "grows downward"
				final long newRSP = rsp - 8L;
				rf.set(Register64.RSP, newRSP);
				mem.write(newRSP, value);
			}
			case POP -> {
				final Register64 dest = (Register64) inst.firstOperand();
				final long rsp = rf.get(Register64.RSP);
				rf.set(dest, mem.read8(rsp));
				// the stack "grows downward"
				rf.set(Register64.RSP, rsp + 8L);
			}
			case LEA -> {
				final IndirectOperand src = (IndirectOperand) inst.secondOperand();
				if (inst.firstOperand() instanceof Register64 dest) {
					final long address = computeIndirectOperand(rf, src);
					rf.set(dest, address);
				} else if (inst.firstOperand() instanceof Register32 dest) {
					final int address = BitUtils.asInt(computeIndirectOperand(rf, src));
					rf.set(dest, address);
				} else {
					final Register16 dest = (Register16) inst.firstOperand();
					final short address = BitUtils.asShort(computeIndirectOperand(rf, src));
					rf.set(dest, address);
				}
			}
			case CALL -> {
				// This points to the instruction right next to 'CALL ...'
				final long rip = rf.get(Register64.RIP);

				// Push the return address (the position of the next instruction) on top of the stack.
				// This little subroutine may be replaced entirely by a PUSH instruction and a MOV
				final long oldStackPointer = rf.get(Register64.RSP);
				final long newStackPointer = oldStackPointer - 8L;
				rf.set(Register64.RSP, newStackPointer);
				mem.write(newStackPointer, rip);

				// TODO: check this (should modify stack pointers)
				final long relativeAddress = ((RelativeOffset) inst.firstOperand()).getValue();

				rf.set(Register64.RIP, rip + relativeAddress);
			}
				/*case RET -> {
					// TODO: check this
					final long prev = rf.get(Register64.RSP) + 8L;

					// If we read 0x0, we have exhausted the stack
					final long zero = 0L;
					if (mem.read8(prev) == zero) {
						state = State.HALTED;
					} else {
						rf.set(Register64.RSP, prev);
						rf.set(Register64.RIP, mem.read8(prev));
					}
				}*/
			case ENDBR64 -> logger.warning("ENDBR64 not implemented");
			case HLT -> state = State.HALTED;
			case NOP -> {}
			default -> throw new IllegalArgumentException(
					String.format("Unknown instruction %s", inst.toIntelSyntax()));
		}
	}

	public static long computeIndirectOperand(final RegisterFile rf, final IndirectOperand io) {
		return ((io.base() == null)
						? 0L
						: io.base() instanceof Register64
								? rf.get((Register64) io.base())
								: rf.get((Register32) io.base()))
				+ ((io.index() == null)
								? 0L
								: io.index() instanceof Register64
										? rf.get((Register64) io.index())
										: rf.get((Register32) io.index()))
						* io.scale()
				+ io.getDisplacement();
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "We know that this object is immutable.")
	public ImmutableRegisterFile getRegisters() {
		return rf;
	}
}
