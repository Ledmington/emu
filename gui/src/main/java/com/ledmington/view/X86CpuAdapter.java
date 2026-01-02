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
package com.ledmington.view;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.emu.X86Cpu;
import com.ledmington.mem.MemoryController;

// TODO: refactor avoiding inheritance (and avoiding two queues)
public final class X86CpuAdapter extends X86Cpu {

	private final BlockingQueue<Object> execute = new ArrayBlockingQueue<>(1);
	private final BlockingQueue<Object> executionCompleted = new ArrayBlockingQueue<>(1);

	public X86CpuAdapter(final MemoryController mem) {
		super(mem, true);
	}

	private void waitToStartExecution() {
		try {
			execute.take();
		} catch (final InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private void sendExecutionCompleted() {
		try {
			executionCompleted.put(new Object());
		} catch (final InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private void sendExecutionMessage() {
		try {
			execute.put(new Object());
		} catch (final InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private void waitForExecutionCompleted() {
		try {
			executionCompleted.take();
		} catch (final InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void executeOne(final Instruction inst) {
		// block until the user wants to execute
		waitToStartExecution();

		super.executeOne(inst);

		// signal the user that we are done executing
		sendExecutionCompleted();
	}

	public void doExecuteOne() {
		sendExecutionMessage();

		// block until execution completed
		waitForExecutionCompleted();
	}

	public void doExecute() {
		super.state = State.RUNNING;
		while (state != State.HALTED) {
			this.doExecuteOne();
		}
	}
}
