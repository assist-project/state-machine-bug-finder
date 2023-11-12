package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public class Assignment {

    private Field field;
    private Variable variable;

    public Assignment(Variable variable, Field field) {
        super();
        this.field = field;
        this.variable = variable;
    }

    public Valuation update(Symbol label, Valuation valuation) {
        Value val = field.eval(label, valuation);
        return valuation.update(variable, val);
    }

    public Field getField() {
        return field;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return variable + " := " + field;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((variable == null) ? 0 : variable.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (! (obj instanceof Assignment))
            return false;
        Assignment other = (Assignment) obj;
        if (field == null) {
            if (other.field != null)
                return false;
        } else if (!field.equals(other.field))
            return false;
        if (variable == null) {
            if (other.variable != null)
                return false;
        } else if (!variable.equals(other.variable))
            return false;
        return true;
    }
}
