package se.uu.it.smbugfinder.execution;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;

import net.automatalib.exception.FormatException;
import se.uu.it.smbugfinder.Main;

public class CommandsExec {
    protected void assertCorrectOutput(Output expectedOutput, Output output) {
        Assert.assertEquals(expectedOutput.getBugs(), output.getBugs());
    }

    // Parse the output produced by Main.main and store it in an output object
    private static Output getBugs(ByteArrayOutputStream outputStream) throws IOException {
        Output bugs = new Output();
        String pattern = null, severity = null, trace = null, inputs = null;
        BufferedReader reader = new BufferedReader(new StringReader(outputStream.toString(StandardCharsets.UTF_8)));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Bug Pattern:"))
                pattern = line.substring(line.indexOf(":") + 1).trim();

            if (line.startsWith("Severity:"))
                severity = line.substring(line.indexOf(":") + 1).trim();

            if (line.startsWith("Trace:"))
                trace = line.substring(line.indexOf(":") + 1).trim();

            if (line.startsWith("Inputs:"))
                inputs = line.substring(line.indexOf(":") + 1).trim();

            if (Stream.of(pattern, severity, trace, inputs).allMatch(x -> x != null)) {
                bugs.addBug(new OutputBug(pattern, severity, trace, inputs));
                pattern = null;
                severity = null;
                trace = null;
                inputs = null;
            }
        }
        return bugs;
    }

    public static Output runCommand(String model, String patterns, String os)
            throws IOException, FormatException, InterruptedException {
        return runCommand(model, patterns, false, os);
    }

    public static Output runCommand(String model, String patterns, boolean eo)
            throws IOException, FormatException, InterruptedException {
        return runCommand(model, patterns, eo, null);
    }

    private static Output runCommand(String model, String patterns, boolean eo, String os)
            throws IOException, FormatException, InterruptedException {
        List<String> command = new ArrayList<>(Arrays.asList(
            "-m", model,
            "-c", patterns));

        if (eo) {
            command.add("-eo");
            command.add("NO_RESP");
        }

        if (os != null) {
            command.add("-os");
            command.add(os);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        System.setOut(ps);
        Main.main(command.toArray(new String[0]));
        System.setOut(System.out);

        return getBugs(outputStream);
    }
}
