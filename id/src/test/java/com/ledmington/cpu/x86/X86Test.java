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

import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.MiniLogger.LoggingLevel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

public class X86Test {

    private static final String testInputFileName = "x86.test.asm";
    private static List<Arguments> args;

    @BeforeAll
    static void setup() {
        MiniLogger.setMinimumLevel(LoggingLevel.DEBUG);

        args = new ArrayList<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classloader.getResourceAsStream(testInputFileName)) {
            final InputStreamReader reader = new InputStreamReader(
                    Objects.requireNonNull(
                            is, () -> String.format("The InputStream for file '%s' was null", testInputFileName)),
                    StandardCharsets.UTF_8);
            final BufferedReader br = new BufferedReader(reader);
            int i = 0;
            for (String line = br.readLine(); line != null; line = br.readLine(), i++) {
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

            br.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void teardown() {
        args.clear();
    }

    static Stream<Arguments> instructions() {
        return args.stream();
    }
}
