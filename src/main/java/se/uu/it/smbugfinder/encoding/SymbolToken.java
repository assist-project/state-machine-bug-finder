package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;

public class SymbolToken extends DescriptionToken {

    private Boolean input;
    private String symbolString;

    public SymbolToken(Boolean input, String symbolString) {
        this.input = input;
        this.symbolString = symbolString;
    }

    public SymbolToken(String symbolString) {
        this.symbolString = symbolString;
        this.input = null;
    }

    public SymbolToken(Symbol symbol) {
        this.input = symbol.isInput();
        this.symbolString = symbol.getName();
    }

    @Override
    public DescriptionType getType() {
        return DescriptionType.SYMBOL;
    }

    /**
     * @return true if describes a message that should be sent to the SUT, false if it describes a message that should be received by the SUT, null if it can be both.
     */
    public Boolean isInput() {
        return input;
    }

    public String getSymbolString() {
        return symbolString;
    }

    @Override
    public String toString() {
        return (input != null ? (input.booleanValue() == true ? "I_" : "O_") : "") + symbolString;
    }

    public Symbol getSymbol() {
        return (isInput() ?
            new InputSymbol(symbolString) :
            new OutputSymbol(symbolString));
    }

}
