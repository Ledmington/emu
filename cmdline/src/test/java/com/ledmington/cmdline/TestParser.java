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
package com.ledmington.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class TestParser {

	@Test
	void booleanOptionShortName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("test", null, "This is a testing option.", true)
				.build();
		assertTrue(p.parse().get("test").asBoolean(), "Expected to parse '' as true but didn't.");
		assertFalse(p.parse("-test").get("test").asBoolean(), "Expected to parse '-test' as false but didn't.");
	}

	@Test
	void booleanOptionLongName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean(null, "test", "This is a testing option.", true)
				.build();
		assertTrue(p.parse().get("test").asBoolean(), "Expected to parse '' as true but didn't.");
		assertFalse(p.parse("--test").get("test").asBoolean(), "Expected to parse '--test' as false but didn't.");
	}

	@Test
	void stringOptionShortName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addString("test", null, "This is a testing option.", "default")
				.build();
		assertEquals("default", p.parse().get("test").asString(), "Expected to parse '' as 'default' but didn't.");
		assertEquals(
				"hello",
				p.parse("-test", "hello").get("test").asString(),
				"Expected to parse '-test hello' as 'hello' but didn't.");
		assertEquals(
				"hello",
				p.parse("-test=hello").get("test").asString(),
				"Expected to parse '-test=hello' as 'hello' but didn't.");
		assertThrows(
				ArrayIndexOutOfBoundsException.class,
				() -> p.parse("-test"),
				"Expected to not be able to parse '-test' but it did.");
	}

	@Test
	void stringOptionLongName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addString(null, "test", "This is a testing option.", "default")
				.build();
		assertEquals("default", p.parse().get("test").asString(), "Expected to parse '' as 'default' but didn't.");
		assertEquals(
				"hello",
				p.parse("--test", "hello").get("test").asString(),
				"Expected to parse '--test hello' as 'hello' but didn't.");
		assertEquals(
				"hello",
				p.parse("--test=hello").get("test").asString(),
				"Expected to parse '--test=hello' as 'hello' but didn't.");
		assertThrows(
				ArrayIndexOutOfBoundsException.class,
				() -> p.parse("--test"),
				"Expected to not be able to parse '--test' but it did.");
	}

	@Test
	void multipleStringArgumentsDoNotCorruptEachOther() {
		// Regression test: a previous implementation advanced the parsing index once per registered
		// StringArgument candidate instead of once per actual match, corrupting parsing as soon as more than
		// one StringArgument was registered.
		final CommandLineParser p = CommandLineParser.builder()
				.addString("a", null, "First option.", "defaultA")
				.addString("b", null, "Second option.", "defaultB")
				.addString("c", null, "Third option.", "defaultC")
				.build();

		final ParsedArguments parsed = p.parse("-a", "1", "-b", "2", "-c", "3");

		assertEquals("1", parsed.get("a").asString(), "Expected '-a 1' to set 'a' to '1'.");
		assertEquals("2", parsed.get("b").asString(), "Expected '-b 2' to set 'b' to '2'.");
		assertEquals("3", parsed.get("c").asString(), "Expected '-c 3' to set 'c' to '3'.");
	}

	@Test
	void dualAliasBooleanResultsAreMerged() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("h", "help", "Shows help.", false)
				.build();

		assertTrue(
				p.parse("-h").get("help").asBoolean(),
				"Expected '-h' to also be reflected under the long name 'help'.");
		assertTrue(
				p.parse("--help").get("h").asBoolean(),
				"Expected '--help' to also be reflected under the short name 'h'.");
	}

	@Test
	void dualAliasStringResultsAreMerged() {
		final CommandLineParser p = CommandLineParser.builder()
				.addString("i", "input", "Input file.", "default")
				.build();

		assertEquals(
				"foo.txt",
				p.parse("-i", "foo.txt").get("input").asString(),
				"Expected '-i' to also be reflected under the long name 'input'.");
		assertEquals(
				"bar.txt",
				p.parse("--input", "bar.txt").get("i").asString(),
				"Expected '--input' to also be reflected under the short name 'i'.");
	}

	@Test
	void positionalArgumentsAfterOptions() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("v", null, "Verbose.", false)
				.addString(null, "mode", "Mode.", "default")
				.build();

		final ParsedArguments parsed = p.parse("-v", "--mode", "fast", "file.elf", "--not-an-option", "extra");

		assertTrue(parsed.get("v").asBoolean(), "Expected '-v' to be parsed as true.");
		assertEquals("fast", parsed.get("mode").asString(), "Expected '--mode fast' to set 'mode' to 'fast'.");
		assertEquals(
				List.of("file.elf", "--not-an-option", "extra"),
				parsed.positionalArguments(),
				"Expected everything from the first unrecognized token onward to be positional.");
	}

	@Test
	void allPositionalWhenNothingMatches() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("v", null, "Verbose.", false)
				.build();

		final ParsedArguments parsed = p.parse("file.elf", "-v");

		assertFalse(
				parsed.get("v").asBoolean(),
				"Expected '-v' appearing after the first positional argument to not be parsed as an option.");
		assertEquals(
				List.of("file.elf", "-v"),
				parsed.positionalArguments(),
				"Expected every token to be positional once the first one failed to match an option.");
	}

	@Test
	void helpMessageListsEachOptionWithItsDescription() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("h", "help", "Shows the help message and exits.", false)
				.addString(null, "stack-size", "Number of bytes to allocate for the stack.", "")
				.build();

		final String help = p.helpMessage();

		assertTrue(help.contains("-h, --help"), "Expected the help message to list '-h, --help'.");
		assertTrue(
				help.contains("Shows the help message and exits."),
				"Expected the help message to include the description of '--help'.");
		assertTrue(
				help.contains("--stack-size <value>"),
				"Expected the help message to list '--stack-size' with a value placeholder.");
		assertTrue(
				help.contains("Number of bytes to allocate for the stack."),
				"Expected the help message to include the description of '--stack-size'.");
	}

	@Test
	void helpMessageGroupsOptionsUnderTheirHeadings() {
		final CommandLineParser p = CommandLineParser.builder()
				.group("General options")
				.addBoolean("h", "help", "Shows help.", false)
				.group("Memory options")
				.addString(null, "stack-size", "Stack size.", "")
				.build();

		final String help = p.helpMessage();
		final int generalIndex = help.indexOf(" General options:");
		final int memoryIndex = help.indexOf(" Memory options:");
		final int helpFlagIndex = help.indexOf("-h, --help");
		final int stackSizeIndex = help.indexOf("--stack-size");

		assertTrue(generalIndex >= 0, "Expected a 'General options' heading.");
		assertTrue(memoryIndex >= 0, "Expected a 'Memory options' heading.");
		assertTrue(
				generalIndex < helpFlagIndex && helpFlagIndex < memoryIndex,
				"Expected '-h, --help' to be listed under 'General options', before the 'Memory options' heading.");
		assertTrue(memoryIndex < stackSizeIndex, "Expected '--stack-size' to be listed under 'Memory options'.");
	}

	@Test
	void helpMessageIncludesProgramNameDescriptionAndUsage() {
		final CommandLineParser p = CommandLineParser.builder()
				.programName("emu")
				.description("CPU emulator")
				.addBoolean("h", "help", "Shows help.", false)
				.addPositional("FILE", "The ELF executable file to emulate.")
				.build();

		final String help = p.helpMessage();

		assertTrue(help.contains("emu - CPU emulator"), "Expected the title to show 'emu - CPU emulator'.");
		assertTrue(
				help.contains("Usage: emu [OPTIONS] FILE"),
				"Expected the usage line to include the program name and the positional argument name.");
		assertTrue(
				help.contains("Positional arguments:"),
				"Expected a 'Positional arguments' section when positional arguments are documented.");
		assertTrue(
				help.contains("FILE") && help.contains("The ELF executable file to emulate."),
				"Expected the positional argument to be listed with its description.");
	}

	@Test
	void noPositionalArgumentsByDefault() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("v", null, "Verbose.", false)
				.build();

		assertEquals(List.of(), p.parse().positionalArguments(), "Expected no positional arguments for ''.");
		assertEquals(List.of(), p.parse("-v").positionalArguments(), "Expected no positional arguments for '-v'.");
	}
}
