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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.AvoidInstantiatingObjectsInLoops"})
public final class FormatTestFiles {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, true, StandardCharsets.UTF_8);

	private record TestCase(String mnemonic, String hex) {}

	public static void main(final String[] args) {
		if (args.length > 0) {
			out.println("Command-line arguments were provided but not needed. Ignoring them.");
		}

		final String testInputFileName = "x86.test.asm";
		final String testInputFile =
				new File(String.join(File.separator, "src", "test", "resources", testInputFileName)).getAbsolutePath();

		final List<String> allLines = readAllLines(testInputFile);

		writeAllLines(allLines, testInputFile);
	}

	private static List<String> readAllLines(final String filepath) {
		/*
		 * You may be wondering "Why the hell a List of Sets of Strings?".
		 * I couldn't find a better to solution to encode the fact that:
		 * - comments, empty lines and blank lines need to be saved in order
		 * - everything else is a group and does not need to be saved in order
		 * So, comments, empty lines and blank will be Sets with a single element.
		 */
		final List<Set<String>> lines = new ArrayList<>();

		try (final BufferedReader br = Files.newBufferedReader(Path.of(filepath), StandardCharsets.UTF_8)) {
			int lineIndex = 0;
			boolean isGroupEnded = false;
			for (String line = br.readLine(); line != null; line = br.readLine(), lineIndex++) {
				// empty lines are added as they are
				if (line.isEmpty()) {
					lines.add(Set.of(""));
					isGroupEnded = true;
					continue;
				}
				// blank lines are substituted with empty lines
				if (line.isBlank()) {
					lines.add(Set.of(""));
					isGroupEnded = true;
					continue;
				}
				// comments are stripped before being added
				if (line.startsWith("#")) {
					lines.add(Set.of(line.strip()));
					isGroupEnded = true;
					continue;
				}

				final String[] splitted = line.split("\\|");

				if (splitted.length != 2) {
					throw new IllegalArgumentException(
							String.format("Line %,d: '%s' is not formatted correctly", lineIndex, line));
				}

				if (isGroupEnded) {
					final Set<String> s = new HashSet<>();
					s.add(line.strip());
					lines.add(s);
					isGroupEnded = false;
				} else {
					lines.getLast().add(line.strip());
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		out.printf("Read %,d lines%n", lines.stream().mapToInt(Set::size).sum());

		final List<String> allLines = new ArrayList<>();

		for (final Set<String> ss : lines) {
			if (ss.size() == 1) {
				allLines.add(ss.stream().findFirst().orElseThrow());
				continue;
			}

			final int maxInstructionLength = ss.stream()
					.mapToInt(s -> s.split("\\|")[0].strip().length())
					.max()
					.orElseThrow();
			final String fmt = String.format("%%-%ds", maxInstructionLength);

			final Set<TestCase> tc = new HashSet<>();
			for (final String s : ss) {
				final String[] splitted = s.split("\\|");
				tc.add(new TestCase(splitted[0].strip(), splitted[1].strip()));
			}

			tc.stream()
					.sorted((a, b) -> ((a.mnemonic().equals(b.mnemonic()))
							? (a.hex().compareTo(b.hex()))
							: (a.mnemonic().compareTo(b.mnemonic()))))
					.forEach(e -> allLines.add(String.format(fmt + " | %s", e.mnemonic(), e.hex())));
		}

		// Last empty line
		if (!allLines.getLast().isEmpty()) {
			allLines.add("");
		}

		return allLines;
	}

	private static void writeAllLines(final List<String> lines, final String filePath) {
		try {
			Files.writeString(Path.of(filePath), String.join("%n", lines), StandardOpenOption.WRITE);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
