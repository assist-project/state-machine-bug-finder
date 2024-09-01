package se.uu.it.smbugfinder.execution;


import com.google.common.base.Ascii;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.uu.it.smbugfinder.bug.BugSeverity;

@ToString @EqualsAndHashCode
public class OutputBug {
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
