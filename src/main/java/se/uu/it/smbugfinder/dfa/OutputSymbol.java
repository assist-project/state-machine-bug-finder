package se.uu.it.smbugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.it.smbugfinder.encoding.SymbolToken;

public final class OutputSymbol extends Symbol {

    public OutputSymbol(String name) {
        super(name);
    }

    @Override
    public final boolean isInput() {
        return false;
    }

    @Override
    public String toString() {
        return "O_" + getName();
    }

    @Override
    public SymbolToken toSymbolToken() {
        return new SymbolToken(false, getName());
    }

    public static List<Symbol> generateOutputSymbols(String mes, Collection<String> keys) {
        List<Symbol> outputSymbols = new ArrayList<Symbol>();
        for (String key : keys) {
            outputSymbols.add(new OutputSymbol(key + "_" + mes));
        }
        return outputSymbols;
    }
}
