package se.uu.it.smbugfinder.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;

public class CommandsExec {
  protected void assertCorrectOutput(Output expectedOutput, Output output) {
    Assert.assertEquals(expectedOutput.getSet(), output.getSet());
  }

  private static Output getBugs(Process process) throws IOException {
    Output bugs = new Output();
    String pattern = null, severity = null, trace = null, inputs = null;
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
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
        pattern = null; severity = null; trace = null; inputs = null;
      }
    }
    return bugs;
  }

  public static Output runCommand(String model, String patterns, boolean eo) throws IOException, InterruptedException {
    List<String> command = new ArrayList<String>(Arrays.asList(
      "java", "-jar", "target/sm-bug-finder.jar",
      "-m", model,
      "-c", patterns,
      "-eo", "NO_RESP"
    ));

    if (eo) {
      command.add("-eo");
      command.add("NO_RESP");
    }

    Process process = new ProcessBuilder(command).start();
    Output output = getBugs(process);
    process.waitFor();
    return output;
  }
}
