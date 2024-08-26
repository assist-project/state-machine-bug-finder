package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public interface ValueExpression {
    Value eval (Symbol symbol, Valuation valuation);
    @Override
    boolean equals(Object object);
    @Override
    public int hashCode();
}
