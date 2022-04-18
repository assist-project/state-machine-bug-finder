package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

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
		return (input ? "I_" : "O_") + symbolString;
	}

}
