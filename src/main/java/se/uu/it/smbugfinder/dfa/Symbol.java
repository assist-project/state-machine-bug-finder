package se.uu.it.smbugfinder.dfa;

import se.uu.it.smbugfinder.encoding.SymbolToken;

/**
 * Implements the DFA symbols.
 *
 */
public abstract class Symbol {
    private final String name;
    Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public abstract boolean isInput();

    public abstract SymbolToken toSymbolToken();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("EqualsGetClass")
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        // we are using this method to compare objects
        // of both InputSymbol and OutputSymbol classes
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
