package com.ledmington.cpu.x86;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public abstract class X86Test {

    private static final String testInputFileName = "x86.test.asm";
    private static List<Arguments> args = null;

    static Stream<Arguments> instructions() {
        if (args == null) {
            args = new ArrayList<>();
            final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            try (final InputStream is = classloader.getResourceAsStream(testInputFileName)) {
                final InputStreamReader reader = new InputStreamReader(
                        Objects.requireNonNull(
                                is, () -> String.format("The InputStream for file '%s' was null", testInputFileName)),
                        StandardCharsets.UTF_8);
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
        }
        return args.stream();
    }
}
