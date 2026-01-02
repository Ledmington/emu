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

import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

public final class TooltipFactory {

	private TooltipFactory() {}

	public static Tooltip getDefaultTooltip(final String text) {
		final Tooltip lbl = new Tooltip(text);
		lbl.setWrapText(false);
		lbl.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		return lbl;
	}
}
