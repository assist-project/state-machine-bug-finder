package se.uu.it.smbugfinder.execution;

import java.util.Objects;

import com.google.common.base.Ascii;

import se.uu.it.smbugfinder.bug.BugSeverity;

public class OutputBug {
  @Override
  public int hashCode() {
    return Objects.hash(bugPattern, severity, trace, inputs);
  }

  @Override
  public boolean equals(Object o) {
    // if (getClass() != o.getClass()) return false;
    if (!(o instanceof OutputBug)) return false;
    OutputBug bug = (OutputBug) o;
    return this.bugPattern.equals(bug.bugPattern)
        && this.severity.equals(bug.severity)
        && this.trace.equals(bug.trace)
        && this.inputs.equals(bug.inputs);
  }

  // @Override
  // public String toString() {
  //   return "\n{" + bugPattern +
  //           "   inputs='" + inputs + '\'' +
  //           "   severity='" + getSeverity() + '\'' +
  //           "   trace='" + trace + '\'' +
  //           "   inputs='" + inputs + '\'' +
  //           '}';
  // }

  private String bugPattern;
  private BugSeverity severity;
  private String trace;
  private String inputs;

  public OutputBug(String pattern, String severity, String trace, String inputs) {
    this.bugPattern = pattern;
    this.severity = mapStringtoEnum(severity);
    this.trace = trace;
    this.inputs = inputs;
  }

  private static BugSeverity mapStringtoEnum(String s) {
    switch (Ascii.toUpperCase(s)) {
      case "LOW":
        return BugSeverity.LOW;
      case "MEDIUM":
        return BugSeverity.MEDIUM;
      case "HIGH":
        return BugSeverity.HIGH;
      default:
        return BugSeverity.UNKNOWN;
    }
  }
}
