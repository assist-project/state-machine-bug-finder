package se.uu.it.smbugfinder.dfa;

public final class OutputSymbol extends Symbol {

    public OutputSymbol(String name) {
        super(name);
    }

    @Override
    public final boolean isInput() {
        return false;
    }

    public String toString() {
        return "O_" + name();
    }
}
