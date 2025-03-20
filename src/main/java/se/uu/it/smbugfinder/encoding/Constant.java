package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public class Constant extends Value {

    private String name;

    public Constant(String name, Object storedValue) {
        super(storedValue);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Value eval(Symbol symbol, Valuation valuation) {
        return new Value(getStoredValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (! (obj instanceof Constant other))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
