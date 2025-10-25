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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionChecker;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionEncoder;
import com.ledmington.cpu.x86.Operand;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.SuppressFBWarnings;

/** Emulator of an x86 CPU. */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.CouplingBetweenObjects"})
public class X86Cpu implements X86Emulator {

	private static final MiniLogger logger = MiniLogger.getLogger("x86-emu");

	/** The state of the CPU. */
	protected enum State {

		/** The default state of the CPU, meaning that it is able to execute instructions. */
		RUNNING,

		/** The state in which the CPU has completed execution. */
		HALTED
	}

	private final RegisterFile rf;
	private final Memory mem; // TODO: can we remove dependency on mem?
	private final InstructionFetcher instFetch;
	private final boolean checkInstructions;

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
	public X86Cpu(final MemoryController mem, final boolean checkInstructions) {
		this(mem, new X86RegisterFile(), checkInstructions);
	}

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "At the moment we need these objects as they are.")
	public X86Cpu(final MemoryController mem, final RegisterFile rf, final boolean checkInstructions) {
		this.mem = Objects.requireNonNull(mem);
		this.instFetch = new InstructionFetcher(mem, rf);
		this.rf = Objects.requireNonNull(rf);
		this.checkInstructions = checkInstructions;
	}

	@Override
	public void turnOn() {
		state = State.RUNNING;
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
		executeOne(InstructionDecoder.fromHex(this.instFetch));
	}

	@Override
	public void executeOne(final Instruction inst) {
		assertIsRunning();

		if (checkInstructions) {
			InstructionChecker.check(inst);
		}

		logger.debug(InstructionEncoder.toIntelSyntax(inst));
		switch (inst.opcode()) {
			case SUB -> {
				switch (inst.firstOperand()) {
					case Register8 op1 -> op(op1, (Register8) inst.secondOperand(), (a, b) -> BitUtils.asByte(a - b));
					case Register16 op1 ->
						op(op1, (Register16) inst.secondOperand(), (a, b) -> BitUtils.asShort(a - b));
					case Register32 op1 -> op(op1, (Register32) inst.secondOperand(), (a, b) -> a - b);
					case Register64 op1 -> {
						final long r1 = rf.get(op1);
						final long r2 =
								switch (inst.secondOperand()) {
									case Register64 op2 -> rf.get(op2);
									case Immediate imm -> getAsLongSX(imm);
									default ->
										throw new IllegalArgumentException(String.format(
												"Unknown second argument type %s.", inst.secondOperand()));
								};
						final long result = r1 - r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0L);
					}
					case IndirectOperand iop -> {
						final long address = computeIndirectOperand(iop);
						final Register8 op2 = (Register8) inst.secondOperand();
						final byte r1 = mem.read(address);
						final byte r2 = rf.get(op2);
						final byte result = BitUtils.asByte(r1 + r2);
						mem.write(address, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == (byte) 0);
					}
					default ->
						throw new IllegalArgumentException(
								String.format("Don't know what to do with SUB and %s.", inst.firstOperand()));
				}
			}
			case ADD -> {
				switch (inst.firstOperand()) {
					case Register8 op1 -> op(op1, (Register8) inst.secondOperand(), (a, b) -> BitUtils.asByte(a + b));
					case Register16 op1 ->
						op(op1, (Register16) inst.secondOperand(), (a, b) -> BitUtils.asShort(a + b));
					case Register32 op1 -> op(op1, (Register32) inst.secondOperand(), Integer::sum);
					case Register64 op1 -> {
						final long r1 = rf.get(op1);
						final long r2 =
								switch (inst.secondOperand()) {
									case Register64 op2 -> rf.get(op2);
									case Immediate imm -> getAsLongSX(imm);
									default ->
										throw new IllegalArgumentException(String.format(
												"Unknown second argument type %s.", inst.secondOperand()));
								};
						final long result = r1 + r2;
						rf.set(op1, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0L);
					}
					case IndirectOperand iop -> {
						final long address = computeIndirectOperand(iop);
						final Register8 op2 = (Register8) inst.secondOperand();
						final byte r1 = mem.read(address);
						final byte r2 = rf.get(op2);
						final byte result = BitUtils.asByte(r1 + r2);
						mem.write(address, result);
						rf.resetFlags();
						rf.set(RFlags.ZERO, result == 0);
					}
					default ->
						throw new IllegalArgumentException(
								String.format("Don't know what to do with ADD and %s.", inst.firstOperand()));
				}
			}
			case SHR -> {
				if (inst.firstOperand() instanceof final Register64 r1) {
					op(r1, (Immediate) inst.secondOperand(), (r, i) -> r >>> i);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SHR has %,d bits.",
							inst.firstOperand().bits()));
				}
			}
			case SAR -> {
				if (inst.firstOperand() instanceof final Register64 r1) {
					op(r1, (Immediate) inst.secondOperand(), (r, i) -> r >> i);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SAR has %,d bits.",
							inst.firstOperand().bits()));
				}
			}
			case SHL -> {
				if (inst.firstOperand() instanceof final Register64 op1) {
					final long r1 = rf.get(op1);
					final byte imm = rf.get((Register8) inst.secondOperand());
					final long result = r1 << imm;
					rf.set(op1, result);
					rf.resetFlags();
					rf.set(RFlags.ZERO, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format(
							"Don't know what to do when SHL has %,d bits.",
							inst.firstOperand().bits()));
				}
			}
			case XOR -> {
				if (inst.firstOperand() instanceof final Register8 r1
						&& inst.secondOperand() instanceof final Register8 r2) {
					final byte result = BitUtils.xor(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == (byte) 0);
				} else if (inst.firstOperand() instanceof final Register16 r1
						&& inst.secondOperand() instanceof final Register16 r2) {
					final short result = BitUtils.xor(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == (short) 0);
				} else if (inst.firstOperand() instanceof final Register32 r1
						&& inst.secondOperand() instanceof final Register32 r2) {
					final int result = rf.get(r1) ^ rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof final Register64 r1
						&& inst.secondOperand() instanceof final Register64 r2) {
					final long result = rf.get(r1) ^ rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0L);
				} else {
					throw new IllegalArgumentException(String.format("Don't know what to do with %s.", inst));
				}
			}
			case AND -> {
				if (inst.firstOperand() instanceof final Register8 r1
						&& inst.secondOperand() instanceof final Register8 r2) {
					final byte result = BitUtils.and(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == (byte) 0);
				} else if (inst.firstOperand() instanceof final Register16 r1
						&& inst.secondOperand() instanceof final Register16 r2) {
					final short result = BitUtils.and(rf.get(r1), rf.get(r2));
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == (short) 0);
				} else if (inst.firstOperand() instanceof final Register32 r1
						&& inst.secondOperand() instanceof final Register32 r2) {
					final int result = rf.get(r1) & rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0);
				} else if (inst.firstOperand() instanceof final Register64 r1
						&& inst.secondOperand() instanceof final Register64 r2) {
					final long result = rf.get(r1) & rf.get(r2);
					rf.set(r1, result);
					rf.set(RFlags.ZERO, result == 0L);
				} else if (inst.firstOperand() instanceof final Register64 r1
						&& inst.secondOperand() instanceof final Immediate imm) {
					rf.set(r1, rf.get(r1) & getAsLongSX(imm));
				} else {
					throw new IllegalArgumentException(String.format("Don't know what to do with '%s'.", inst));
				}
			}
			case CMP -> {
				final long a = getAsLongSX(inst.firstOperand());
				final long b = getAsLongSX(inst.secondOperand());
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

			// Jumps
			case JMP -> jumpTo(getAsLongSX(inst.firstOperand()));
			case JE -> jumpToIf(getAsLongSX(inst.firstOperand()), rf.isSet(RFlags.ZERO));
			case JNE -> jumpToIf(getAsLongSX(inst.firstOperand()), !rf.isSet(RFlags.ZERO));

			case MOV -> {
				if (inst.firstOperand() instanceof Register64 r) {
					rf.set(r, getAsLongSX(inst.secondOperand()));
				} else if (inst.firstOperand() instanceof final Register8 op1
						&& inst.secondOperand() instanceof final Register8 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof final Register16 op1
						&& inst.secondOperand() instanceof final Register16 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof final Register32 op1
						&& inst.secondOperand() instanceof final Register32 op2) {
					rf.set(op1, rf.get(op2));
				} else if (inst.firstOperand() instanceof final Register32 op1
						&& inst.secondOperand() instanceof final Immediate imm) {
					rf.set(op1, imm.asInt());
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register64 op2) {
					final long address = computeIndirectOperand(io);
					mem.write(address, rf.get(op2));
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					final long address = computeIndirectOperand(io);
					mem.write(address, imm.asInt());
				} else {
					throw new IllegalArgumentException(
							String.format("Unknown argument type '%s'.", inst.secondOperand()));
				}
			}
			case MOVABS -> rf.set((Register64) inst.firstOperand(), ((Immediate) inst.secondOperand()).asLong());
			case MOVSXD -> rf.set((Register64) inst.firstOperand(), getAsLongSX(inst.secondOperand()));
			case PUSH -> {
				final long value =
						switch (inst.firstOperand()) {
							case Register64 r64 -> rf.get(r64);
							case Immediate imm -> getAsLongSX(imm);
							default ->
								throw new IllegalArgumentException(
										String.format("Unexpected argument '%s'.", inst.firstOperand()));
						};

				final long rsp = rf.get(Register64.RSP);
				// the stack "grows downward"
				final long newRSP = rsp - 8L;
				rf.set(Register64.RSP, newRSP);
				mem.write(newRSP, value);
			}
			case POP -> {
				final Register64 dest = (Register64) inst.firstOperand();
				rf.set(dest, pop());
			}
			case LEA -> {
				final IndirectOperand src = (IndirectOperand) inst.secondOperand();
				if (inst.firstOperand() instanceof final Register64 dest) {
					final long address = computeIndirectOperand(src);
					rf.set(dest, address);
				} else if (inst.firstOperand() instanceof final Register32 dest) {
					final int address = BitUtils.asInt(computeIndirectOperand(src));
					rf.set(dest, address);
				} else {
					final Register16 dest = (Register16) inst.firstOperand();
					final short address = BitUtils.asShort(computeIndirectOperand(src));
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

				final long jumpAddress;
				if (inst.firstOperand() instanceof final Immediate imm) {
					// TODO: check this (should modify stack pointers?)
					final long relativeAddress = getAsLongSX(imm);
					jumpAddress = rip + relativeAddress;
				} else if (inst.firstOperand() instanceof final IndirectOperand io) {
					jumpAddress = computeIndirectOperand(io);
				} else {
					throw new IllegalStateException();
				}

				rf.set(Register64.RIP, jumpAddress);
			}
			case RET -> rf.set(Register64.RIP, pop());
			case CMOVNE -> {
				if (rf.isSet(RFlags.ZERO)) {
					return;
				}
				rf.set((Register64) inst.firstOperand(), rf.get((Register64) inst.secondOperand()));
			}
			case SYSCALL -> handleSyscall();
			case ENDBR64 -> logger.warning("ENDBR64 not implemented.");
			case HLT -> state = State.HALTED;
			case NOP -> {}
			case UD2 -> {
				logger.error("Illegal instruction.");
				state = State.HALTED;
			}
			default ->
				throw new IllegalArgumentException(
						String.format("Unknown instruction '%s'.", InstructionEncoder.toIntelSyntax(inst)));
		}
	}

	private void handleSyscall() {
		// Useful reference: https://filippo.io/linux-syscall-table/
		final int sysCallCode = rf.get(Register32.EAX);
		final int sysCallExitCode = 60;
		if (sysCallCode == sysCallExitCode) {
			final long exitCode = rf.get(Register64.RDI);
			logger.info("syscall exit %d encountered", exitCode);
			state = State.HALTED;
		} else {
			throw new IllegalArgumentException(String.format("Unknown syscall code %,d.", sysCallCode));
		}
	}

	private void jumpToIf(final long offset, final boolean condition) {
		if (!condition) {
			return;
		}
		jumpTo(offset);
	}

	private void jumpTo(final long offset) {
		instFetch.setPosition(instFetch.getPosition() + offset);
	}

	private void op(final Register8 op1, final Register8 op2, final BiFunction<Byte, Byte, Byte> task) {
		op(() -> rf.get(op1), () -> rf.get(op2), task, result -> {
			rf.set(op1, result);
			rf.resetFlags();
			rf.set(RFlags.ZERO, result == (byte) 0);
		});
	}

	private void op(final Register16 op1, final Register16 op2, final BiFunction<Short, Short, Short> task) {
		op(() -> rf.get(op1), () -> rf.get(op2), task, result -> {
			rf.set(op1, result);
			rf.resetFlags();
			rf.set(RFlags.ZERO, result == (short) 0);
		});
	}

	private void op(final Register32 op1, final Register32 op2, final BiFunction<Integer, Integer, Integer> task) {
		op(() -> rf.get(op1), () -> rf.get(op2), task, result -> {
			rf.set(op1, result);
			rf.resetFlags();
			rf.set(RFlags.ZERO, result == 0);
		});
	}

	private void op(final Register64 op1, final Immediate imm, final BiFunction<Long, Long, Long> task) {
		op(() -> rf.get(op1), () -> (long) imm.asByte(), task, result -> {
			rf.set(op1, result);
			rf.resetFlags();
			rf.set(RFlags.ZERO, result == 0L);
		});
	}

	// FIXME: shouldn't this be with three types X, Y and Z to be the most general version possible?
	private <X> void op(
			final Supplier<X> readOp1,
			final Supplier<X> readOp2,
			final BiFunction<X, X, X> task,
			final Consumer<X> writeResult) {
		final X op1 = readOp1.get();
		final X op2 = readOp2.get();
		final X result = task.apply(op1, op2);
		writeResult.accept(result);
	}

	private long pop() {
		final long rsp = rf.get(Register64.RSP);
		final long value = mem.read8(rsp);
		// If we read the baseStackValue, we have exhausted the stack
		if (value == EmulatorConstants.getBaseStackValue()) {
			logger.warning("Found the base of the stack (stack underflow), halting execution");
			state = State.HALTED;
		}
		// the stack "grows downward"
		rf.set(Register64.RSP, rsp + 8L);
		return value;
	}

	/** Returns a sign-extended long */
	private long getAsLongSX(final Operand op) {
		return switch (op) {
			case Immediate imm -> getAsLongSX(imm);
			case IndirectOperand io -> getAsLongSX(io);
			case Register32 r -> rf.get(r);
			case Register64 r -> rf.get(r);
			default -> throw new IllegalArgumentException(String.format("Unknown operand '%s'.", op));
		};
	}

	/** Returns a sign-extended long */
	@SuppressWarnings("PMD.UnnecessaryCast")
	private long getAsLongSX(final Immediate imm) {
		return switch (imm.bits()) {
			case 8 -> (long) imm.asByte();
			case 16 -> (long) imm.asShort();
			case 32 -> (long) imm.asInt();
			case 64 -> imm.asLong();
			default -> throw new IllegalArgumentException(String.format("Invalid immediate: '%s'.", imm));
		};
	}

	/** Returns a sign-extended long */
	private long getAsLongSX(final IndirectOperand io) {
		final long address = computeIndirectOperand(io);
		if (io.getPointerSize() == PointerSize.QWORD_PTR) {
			return mem.read8(address);
		} else {
			throw new IllegalArgumentException(String.format("Invalid indirect operand pointer size: '%s'.", io));
		}
	}

	public long computeIndirectOperand(final IndirectOperand io) {
		final long base = io.hasBase()
				? (io.getBase() instanceof Register64
						? rf.get((Register64) io.getBase())
						: BitUtils.asLong(rf.get((Register32) io.getBase())))
				: 0L;
		final long index = io.hasIndex()
				? (io.getIndex() instanceof Register64
						? rf.get((Register64) io.getIndex())
						: BitUtils.asLong(rf.get((Register32) io.getIndex())))
				: 0L;
		final long scale = io.hasScale() ? io.getScale() : 1L;
		final long displacement = io.hasDisplacement() ? io.getDisplacement() : 0L;
		return base + index * scale + displacement;
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "We know that this object is immutable.")
	public ImmutableRegisterFile getRegisters() {
		return rf;
	}

	@Override
	public void setInstructionPointer(final long ip) {
		this.rf.set(Register64.RIP, ip);
	}
}
