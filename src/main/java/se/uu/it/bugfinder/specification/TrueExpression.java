package se.uu.it.bugfinder.specification;

import se.uu.it.bugfinder.dfa.Symbol;

public class TrueExpression implements BooleanExpression {
	private static TrueExpression INSTANCE;
	
	
	public static final TrueExpression getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TrueExpression();
		}
		return INSTANCE;
	}
	
	private TrueExpression() {
		
	}

	@Override
	public boolean eval(Symbol symbol, Valuation valuation) {
		return true;
	}

	public String toString() {
		return "true";
	}
}
