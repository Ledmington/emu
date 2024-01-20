package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestDecoding {

    private static Stream<Arguments> instructions() {
        final List<Arguments> args = new ArrayList<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classloader.getResourceAsStream("x86.test")) {
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

    @ParameterizedTest
    @MethodSource("instructions")
    void parsing(final String expected, final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        final InstructionDecoder id = new InstructionDecoder();
        final List<Instruction> instructions = id.decode(code);
        assertNotNull(instructions, "InstructionDecoder returned a null List");
        assertEquals(
                1,
                instructions.size(),
                String.format("Expected 1 instruction but %,d were found", instructions.size()));
        assertEquals(
                expected,
                instructions.get(0).toString(),
                String.format(
                        "Expected '%s' but '%s' was decoded",
                        expected, instructions.get(0).toString()));
    }
}
