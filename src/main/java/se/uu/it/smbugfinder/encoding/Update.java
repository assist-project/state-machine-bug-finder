package se.uu.it.smbugfinder.encoding;

import java.util.Arrays;
import java.util.Collection;

import se.uu.it.smbugfinder.dfa.Symbol;

public class Update {

    private Assignment[] assignments;

    public Update(Assignment... assignments) {
        this.assignments = assignments;
    }

    public Update(Collection<Assignment> assignments) {
        this.assignments = assignments.toArray(new Assignment[assignments.size()]);
    }

    public Assignment [] getAssignments() {
        return assignments;
    }

    public Valuation update(Symbol symbol, Valuation valuation) {
        Valuation currentValuation = valuation;
        for (Assignment assignment : assignments) {
            currentValuation = assignment.update(symbol, currentValuation);
        }
        return currentValuation;
    }

    @Override
    public String toString() {
        return Arrays.toString(assignments);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(assignments);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (! (obj instanceof Update other))
            return false;
        if (!Arrays.equals(assignments, other.assignments))
            return false;
        return true;
    }

}
