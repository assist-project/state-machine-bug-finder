package se.uu.it.smbugfinder;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uu.it.smbugfinder.encoding.OcamlValues;

public class PatternLanguageTest {
    public static Logger LOGGER = LoggerFactory.getLogger(PatternLanguageTest.class);

    // Tests that all .lang files in resources pass the semantic check
    @Test
    public void testLangFiles() {
        Path start = Paths.get("src/main/resources");

        try (Stream<Path> stream = Files.walk(start)) {
            List<Path> langFiles = stream.
                filter(path -> path.toString().endsWith(".lang"))
                .map(path -> path.toAbsolutePath())
                .collect(Collectors.toList());

            for (Path path : langFiles) {
                new OcamlValues(path.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
