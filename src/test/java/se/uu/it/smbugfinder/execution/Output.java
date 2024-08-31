package se.uu.it.smbugfinder.execution;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Output {
  @Override
  public int hashCode() {
      return Objects.hash(bugs);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Output)) return false;
    Output output = (Output) o;
    return this.getSet().equals(output.getSet());
  }

  @Override
  public String toString() {
    return "{Set='" + bugs + '\'' + "}";
  }

  private Set<OutputBug> bugs;

  public Output() {
    this.bugs = new HashSet<OutputBug>();
  }

  public Output(Set<OutputBug> bugs) {
    this.bugs = bugs;
  }

  public Set<OutputBug> getSet() {
    return this.bugs;
  }

  public void addBug(OutputBug bug) {
    this.bugs.add(bug);
  }
}
