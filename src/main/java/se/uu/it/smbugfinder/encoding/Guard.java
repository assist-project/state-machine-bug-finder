package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public class Guard {
	private static Guard TRUE;

	public static final Guard trueGuard() {
		if (TRUE == null) {
			TRUE = new Guard(TrueExpression.getInstance());
		}
		return TRUE;
	}

	private BooleanExpression booleanExpression;

	public Guard(BooleanExpression booleanExpression) {
		this.booleanExpression = booleanExpression;
	}

	public boolean eval(Symbol symbol, Valuation valuation) {
		return booleanExpression.eval(symbol, valuation);
	}

	public String toString() {
		return booleanExpression.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((booleanExpression == null) ? 0 : booleanExpression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Guard other = (Guard) obj;
		if (booleanExpression == null) {
			if (other.booleanExpression != null)
				return false;
		} else if (!booleanExpression.equals(other.booleanExpression))
			return false;
		return true;
	}

}
