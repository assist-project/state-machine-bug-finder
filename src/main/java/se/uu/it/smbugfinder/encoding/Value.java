package se.uu.it.smbugfinder.encoding;

import java.util.Objects;

import se.uu.it.smbugfinder.dfa.Symbol;

public class Value implements ValueExpression {
    private Object storedValue;

    public Value(Object storedValue) {
        this.storedValue = storedValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((storedValue == null) ? 0 : storedValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (! (obj instanceof Value))
            return false;
        Value other = (Value) obj;
        if (storedValue == null) {
            if (other.storedValue != null)
                return false;
        } else if (!storedValue.equals(other.storedValue))
            return false;
        return true;
    }

    public <T> T getStoredValue(Class<T> cls) {
        if (storedValue == null) {
            return null;
        } else {
            return cls.cast(storedValue);
        }
    }

    public Object getStoredValue() {
        return storedValue;
    }

    @Override
    public Value eval(Symbol symbol, Valuation valuation) {
        return this;
    }

    @Override
    public String toString() {
        return Objects.toString(storedValue);
    }
}
