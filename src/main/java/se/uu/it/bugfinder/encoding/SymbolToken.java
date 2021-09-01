package se.uu.it.bugfinder.encoding;

import se.uu.it.bugfinder.dfa.Symbol;

public class SymbolToken extends DescriptionToken {
	
	private boolean input;
	private String symbolString;
	
	public SymbolToken(boolean input, String symbolString) {
		this.input = input;
		this.symbolString = symbolString;
	}
	
	public SymbolToken(Symbol symbol) {
		this.input = symbol.isInput();
		this.symbolString = symbol.name();
	}
	
	@Override
	public DescriptionType getType() {
		return DescriptionType.SYMBOL;
	}
	
	public boolean isInput() {
		return input;
	}
	
	public String getSymbolString() {
		return symbolString;
	}

	@Override
	public String toString() {
		return (input ? "I_" : "O_") + symbolString;
	}

}
