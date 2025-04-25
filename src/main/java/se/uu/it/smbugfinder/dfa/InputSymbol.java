package se.uu.it.smbugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.it.smbugfinder.encoding.SymbolToken;

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
        return "I_" + getName();
    }

    @Override
    public SymbolToken toSymbolToken() {
        return new SymbolToken(true, getName());
    }

    public static List<Symbol> generateInputSymbols(String mes, Collection<String> keys) {
        List<Symbol> inputSymbols = new ArrayList<Symbol>();
        for (String key : keys) {
            inputSymbols.add(new InputSymbol(key + "_" + mes));
        }
        return inputSymbols;
    }
}
