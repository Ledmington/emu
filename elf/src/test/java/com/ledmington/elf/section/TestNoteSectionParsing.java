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
package com.ledmington.elf.section;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.utils.ReadOnlyByteBufferV1;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

final class TestNoteSectionParsing {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void correctParsing(final boolean is32Bit) {
        final NoteSectionEntry[] expected = new NoteSectionEntry[] {
            new NoteSectionEntry("test-name", "12345", 0x12345678, is32Bit),
            new NoteSectionEntry("long-test-name", "Another test description", 99, is32Bit),
            new NoteSectionEntry("really-long-test-name", "This is a test description", -99, is32Bit)
        };

        final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1();
        int runningLength = 0;
        for (int i = 0; i < expected.length; i++) {
            wb.write(expected[i].name().length());
            wb.write(expected[i].description().length());
            wb.write(expected[i].type());
            wb.write(expected[i].name().getBytes(StandardCharsets.UTF_8));
            wb.write(expected[i].description().getBytes(StandardCharsets.UTF_8));
            if (i < expected.length - 1) {
                wb.setPosition(runningLength + expected[i].getAlignedSize());
            }
            runningLength += expected[i].getAlignedSize();
        }
        final byte[] encoded = wb.array();
        final NoteSectionEntry[] parsed =
                NoteSection.loadNoteSectionEntries(is32Bit, new ReadOnlyByteBufferV1(encoded), encoded.length);
        assertArrayEquals(
                expected,
                parsed,
                () -> String.format(
                        "Expected to parse %s but was %s", Arrays.toString(expected), Arrays.toString(parsed)));
    }
}
