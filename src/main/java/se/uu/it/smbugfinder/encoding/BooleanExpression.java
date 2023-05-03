package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public interface BooleanExpression {

    public boolean eval(Symbol label, Valuation valuation);
}
