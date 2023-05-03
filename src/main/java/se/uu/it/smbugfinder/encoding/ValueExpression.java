package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public interface ValueExpression {
    Value eval (Symbol symbol, Valuation valuation);
    boolean equals(Object object);
    public int hashCode();
}
