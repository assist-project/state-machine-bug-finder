package se.uu.it.smbugfinder.dfa;

public final class InputSymbol extends Symbol {

    public InputSymbol(String name) {
        super(name);
    }

    @Override
    public final boolean isInput() {
        return true;
    }

    @Override
    public String toString() {
        return "I_" + name();
    }
}
