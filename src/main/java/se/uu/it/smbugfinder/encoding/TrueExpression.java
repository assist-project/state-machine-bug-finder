package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public class TrueExpression implements BooleanExpression {
    private static TrueExpression INSTANCE;

    public static final synchronized TrueExpression getInstance() {
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

    @Override
    public String toString() {
        return "true";
    }
}
