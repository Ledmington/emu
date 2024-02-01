package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestIncompleteInstruction extends X86Test {

    static Stream<Arguments> incompleteInstructions() {
        return instructions()
                .flatMap(arg -> {
                    final String hexCode = (String) arg.get()[1];
                    final String[] splitted = hexCode.split(" ");
                    if (splitted.length == 1) {
                        return Stream.of();
                    }
                    final List<String> ll = new ArrayList<>();
                    for (int i = 0; i < splitted.length; i++) {
                        ll.add(String.join(" ", Arrays.copyOfRange(splitted, 0, i + 1)));
                    }
                    return ll.stream();
                })
                .distinct()
                .sorted()
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("incompleteInstructions")
    void incorrectDecoding(final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        final InstructionDecoder id = new InstructionDecoder();

        // here we expect an ArrayIndexOutOfBoundsException to be thrown because,
        // like CPUs which break when requesting a new byte and not finding it,
        // the InstructionDecoder will ask for more bytes than are available and
        // the ByteBuffer will throw this exception.
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> id.decode(code));
    }
}
