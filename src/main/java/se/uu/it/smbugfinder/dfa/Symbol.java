package se.uu.it.smbugfinder.dfa;

/**
 * Implements the DFA symbols.
 *
 */
public abstract class Symbol {
    private final String name;
    Symbol(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
    public abstract boolean isInput();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
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
