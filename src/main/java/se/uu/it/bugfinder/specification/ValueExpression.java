package se.uu.it.bugfinder.specification;

import se.uu.it.bugfinder.dfa.Symbol;

public interface ValueExpression {
	Value eval (Symbol symbol, Valuation valuation);
	boolean equals(Object object);
	public int hashCode();
}
