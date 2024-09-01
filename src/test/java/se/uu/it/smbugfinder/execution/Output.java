package se.uu.it.smbugfinder.execution;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class Output {
  private final Set<OutputBug> bugs;

  public Output() {
    this.bugs = new HashSet<OutputBug>();
  }

  public void addBug(OutputBug bug) {
    this.bugs.add(bug);
  }
}
