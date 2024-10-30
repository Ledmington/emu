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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.MiniLogger;

// TODO: refactor avoiding inheritance
public final class X86CpuAdapter extends X86Cpu {

	private static final MiniLogger logger = MiniLogger.getLogger("x86-cpu-adapter");

	private CompletableFuture<Void> fut = new CompletableFuture<>();

	public X86CpuAdapter(final X86RegisterFile regFile, final MemoryController mem) {
		super(regFile, mem);
	}

	@Override
	public void executeOne(final Instruction inst) {

		logger.debug("submitted %s", inst.toIntelSyntax());

		// block until the user want to execute
		try {
			fut.get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		logger.debug("actually executing %s", inst.toIntelSyntax());
		// each instruction gets added to a queue
		super.executeOne(inst);
	}

	public void doExecuteOne() {
		fut.complete(null);
	}

	public void doExecute() {
		throw new Error("Not implemented");
	}
}
