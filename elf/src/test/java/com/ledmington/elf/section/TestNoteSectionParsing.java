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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.elf.section.note.NoteSection;
import com.ledmington.elf.section.note.NoteSectionEntry;
import com.ledmington.elf.section.note.NoteSectionEntryType;
import com.ledmington.utils.ReadOnlyByteBufferV1;

final class TestNoteSectionParsing {

    private static byte[] convertHexStringToByteArray(final String hexString) {
        return new BigInteger(hexString, 16).toByteArray();
    }

    private static Stream<Arguments> exampleNoteSections() {
        return Stream.of(
                Arguments.of(
                        convertHexStringToByteArray(
                                // real dump of the .note.gnu.property section of a gcc 11.4.0 executable, obtained with
                                // 'readelf -x .note.gnu.property /usr/bin/gcc'
                                "040000002000000005000000474e5500020000c0040000000300000000000000028000c0040000000100000000000000"),
                        false,
                        new NoteSectionEntry[] {
                            new NoteSectionEntry(
                                    "GNU",
                                    new byte[] {
                                        (byte) 0x02,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0xc0,
                                        (byte) 0x04,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x03,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x02,
                                        (byte) 0x80,
                                        (byte) 0x00,
                                        (byte) 0xc0,
                                        (byte) 0x04,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x01,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                    },
                                    NoteSectionEntryType.NT_GNU_PROPERTY_TYPE_0,
                                    false)
                        }),
                Arguments.of(
                        convertHexStringToByteArray(
                                // real dump of the .note.gnu.build-id section of a gcc 11.4.0 executable, obtained with
                                // 'readelf -x .note.gnu.build-id /usr/bin/gcc'
                                "040000001400000003000000474e5500bee27145fd189a47a04c578e204051498e609ed2"),
                        false,
                        new NoteSectionEntry[] {
                            new NoteSectionEntry(
                                    "GNU",
                                    new byte[] {
                                        (byte) 0xbe,
                                        (byte) 0xe2,
                                        (byte) 0x71,
                                        (byte) 0x45,
                                        (byte) 0xfd,
                                        (byte) 0x18,
                                        (byte) 0x9a,
                                        (byte) 0x47,
                                        (byte) 0xa0,
                                        (byte) 0x4c,
                                        (byte) 0x57,
                                        (byte) 0x8e,
                                        (byte) 0x20,
                                        (byte) 0x40,
                                        (byte) 0x51,
                                        (byte) 0x49,
                                        (byte) 0x8e,
                                        (byte) 0x60,
                                        (byte) 0x9e,
                                        (byte) 0xd2,
                                    },
                                    NoteSectionEntryType.NT_GNU_BUILD_ID,
                                    false)
                        }),
                Arguments.of(
                        convertHexStringToByteArray(
                                // real dump of the .note.ABI-tag section of a gcc 11.4.0 executable, obtained with
                                // 'readelf -x .note.ABI-tag /usr/bin/gcc'
                                "040000001000000001000000474e550000000000030000000200000000000000"),
                        false,
                        new NoteSectionEntry[] {
                            new NoteSectionEntry(
                                    "GNU",
                                    new byte[] {
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x03,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x02,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00,
                                    },
                                    NoteSectionEntryType.NT_GNU_ABI_TAG,
                                    false)
                        }));
    }

    @ParameterizedTest
    @MethodSource("exampleNoteSections")
    void correctParsing(final byte[] content, final boolean is32Bit, final NoteSectionEntry[] expected) {
        final NoteSectionEntry[] parsed =
                NoteSection.loadNoteSectionEntries(is32Bit, new ReadOnlyByteBufferV1(content, true), content.length);
        assertArrayEquals(
                expected,
                parsed,
                () -> String.format(
                        "Expected to parse %s but was %s", Arrays.toString(expected), Arrays.toString(parsed)));
    }
}
