package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import se.uu.it.smbugfinder.dfa.Symbol;

public class CustomFunction extends Function {

    private final Map<String, String> mapping;

    public static CustomFunction create(String name, Map<String, String> mapping) {
        return new CustomFunction(name, mapping);
    }

    private CustomFunction(String name, Map<String, String> mapping) {
        super(name, 1);
        this.mapping = mapping;
    }

    @Override
    protected Value doInvoke(Symbol symbol, Value... arguments) {
            String key = arguments[0].getStoredValue().toString();
            Set<String> values = new LinkedHashSet<String>();

            if (mapping.containsKey(key)) {
                values.add(mapping.get(key));
            } else {
                throw new NotImplementedException("Unsupported key type " + key);
            }

            return new Value(values);
    }
}
