package se.uu.it.bugfinder.specification;

import se.uu.it.bugfinder.dfa.Symbol;

public interface BooleanExpression {
	
	public boolean eval(Symbol label, Valuation valuation);
}
