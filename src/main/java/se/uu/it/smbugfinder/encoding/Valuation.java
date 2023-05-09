package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashMap;

/**
 * A mapping from variables to values.
 */
public class Valuation extends LinkedHashMap<Variable, Value>{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Valuation() {
        super();
    }

    public Valuation(Valuation valuation) {
        super(valuation);
    }

    public Valuation update(Variable variable, Value value) {
        Valuation newValuation = new Valuation();
        newValuation.putAll(this);
        newValuation.put(variable, value);
        return newValuation;
    }

    public Valuation update(Valuation valuation) {
        Valuation newValuation = new Valuation();
        newValuation.putAll(this);
        newValuation.putAll(valuation);
        return newValuation;
    }
}
