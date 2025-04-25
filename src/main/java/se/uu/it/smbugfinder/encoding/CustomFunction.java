package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import se.uu.it.smbugfinder.dfa.Symbol;

public class CustomFunction extends Function {

    private enum Type {
        Map;
    }
    private final Type type;
    private final Map<String, String> mapping;

    public static CustomFunction create(String name, String type, Map<String, String> mapping) {
        Type funcType = stringToFuncType(type);
        return new CustomFunction(name, funcType, mapping);
    }

    private CustomFunction(String name, Type type, Map<String, String> mapping) {
        super(name, 1);
        this.type = type;
        this.mapping = mapping;
    }

    @Override
    protected Value doInvoke(Symbol symbol, Value... arguments) {
        if (type.equals(Type.Map)) {
            String key = arguments[0].getStoredValue().toString();
            Set<String> values = new LinkedHashSet<String>();

            if (mapping.containsKey(key)) {
                values.add(mapping.get(key));
            } else {
                throw new NotImplementedException("Unsupported key type " + key);
            }

            return new Value(values);
        } else {
            throw new RuntimeException("Found undefined function type");
        }
    }

    public static Type stringToFuncType (String s) {
        switch (s) {
            case "Map":
                return Type.Map;

            default:
                throw new IllegalArgumentException("Undefined function type in parsing context: " + s);
        }
    }
}
