package com.ledmington.cpu.x86;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

public abstract class X86Test {

    private static final String testInputFileName = "x86.test.asm";

    @BeforeAll
    static void formatInputFile() {
        final List<String> allLines = new ArrayList<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final URL inputFileURL = classloader.getResource(testInputFileName);
        try (final InputStream is = classloader.getResourceAsStream(testInputFileName)) {
            Objects.requireNonNull(is);
            final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final BufferedReader br = new BufferedReader(reader);
            int i = 0;
            List<String> groupLines = new ArrayList<>();
            for (String line; (line = br.readLine()) != null; i++) {
                if (line.isEmpty() || line.isBlank() || line.startsWith("#")) {
                    if (!groupLines.isEmpty()) {
                        final int maxInstructionLength = groupLines.stream()
                                .mapToInt(s -> s.split("\\|")[0].strip().length())
                                .max()
                                .orElseThrow();
                        final String fmt = String.format("%%-%ds", maxInstructionLength);
                        groupLines.stream().sorted().forEach(gl -> {
                            allLines.add(String.format(
                                    fmt + " | %s", gl.split("\\|")[0].strip(), gl.split("\\|")[1].strip()));
                        });
                    }
                    allLines.add(line);
                    groupLines.clear();
                    groupLines = new ArrayList<>();
                    continue;
                }

                final String[] splitted = line.split("\\|");

                if (splitted.length != 2) {
                    throw new IllegalArgumentException(
                            String.format("Line %,d: '%s' is not formatted correctly", i, line));
                }
                groupLines.add(line);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try (final FileOutputStream fos = new FileOutputStream(inputFileURL.getFile())) {
            fos.write(String.join("\n", allLines).getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static final Stream<Arguments> instructions() {
        final List<Arguments> args = new ArrayList<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classloader.getResourceAsStream(testInputFileName)) {
            Objects.requireNonNull(is);
            final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final BufferedReader br = new BufferedReader(reader);
            int i = 0;
            for (String line; (line = br.readLine()) != null; i++) {
                if (line.isEmpty() || line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                final String[] splitted = line.split("\\|");

                if (splitted.length != 2) {
                    throw new IllegalArgumentException(
                            String.format("Line %,d: '%s' is not formatted correctly", i, line));
                }
                args.add(Arguments.of(splitted[0].strip(), splitted[1].strip()));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return args.stream();
    }
}
