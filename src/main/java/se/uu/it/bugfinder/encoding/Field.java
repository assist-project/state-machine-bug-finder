package se.uu.it.bugfinder.encoding;

import se.uu.it.bugfinder.dfa.Symbol;

public abstract class Field implements ValueExpression {
	
	private final String name;
	
	
	public Field(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public final Value eval(Symbol symbol, Valuation valuation) {
		return getValue(symbol);
	}
	
	protected abstract Value getValue(Symbol symbol);
	
	protected Value undefined(Symbol symbol) {
		throw new RuntimeSpecificationException(String.format("Field %s is undefined for symbol %s", name, symbol.name()));
	}

}
