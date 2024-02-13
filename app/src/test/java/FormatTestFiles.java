import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FormatTestFiles {
    public static void main(final String[] args) {
        if (args.length > 0) {
            System.out.println("Command-line arguments were provided but not needed. Ignoring them.");
        }

        final String testInputFileName = "x86.test.asm";
        final String testInputFile =
                new File(String.join(File.separator, "src", "test", "resources", testInputFileName)).getAbsolutePath();

        final List<String> allLines = readAllLines(testInputFile);

        writeAllLines(allLines, testInputFile);
    }

    private static List<String> readAllLines(final String filepath) {
        final List<String> allLines = new ArrayList<>();
        final Map<String, Integer> stats = new HashMap<>();

        try (final InputStream is = new FileInputStream(filepath)) {
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
                        groupLines.stream()
                                .sorted()
                                .forEach(gl -> allLines.add(String.format(
                                        fmt + " | %s", gl.split("\\|")[0].strip(), gl.split("\\|")[1].strip())));
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

                // Collecting statistics
                final String prefix = line.split(" ")[0];
                if (stats.containsKey(prefix)) {
                    stats.replace(prefix, stats.get(prefix) + 1);
                } else {
                    stats.put(prefix, 1);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        System.out.printf("Read %,d lines:\n", allLines.size());
        stats.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> b - a))
                .forEach(e -> System.out.printf("%,6d: '%s'\n", e.getValue(), e.getKey()));

        return allLines;
    }

    private static void writeAllLines(final List<String> lines, final String filePath) {
        try (final FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
