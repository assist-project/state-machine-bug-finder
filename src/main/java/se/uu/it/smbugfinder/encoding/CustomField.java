package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.uu.it.smbugfinder.dfa.Symbol;

public class CustomField extends Field {

    public enum Type {
        Single, Set
    }

    private Pattern messagePattern;
    private Type messageType;

    public static CustomField create(List<String> spec, String message) {
        Type type = toType(spec.get(0));
        return new CustomField(spec.get(1), type, message);
    }

    public CustomField(String name, Type type, String message) {
        // String name = spec.get(1);
        super(name);
        messageType = type;
        messagePattern = Pattern.compile("(?<" + name.replace("_", "") + ">[A-Za-z_]*)_" + message);
    }

    @Override
    protected Value getValue(Symbol symbol) {
        Matcher matcher = messagePattern.matcher(symbol.getName());
        if (matcher.matches()) {
            String field = matcher.group(name.replace("_", ""));
            if (messageType.equals(Type.Set)) {
                Set<String> fieldSet = new LinkedHashSet<>();
                fieldSet.add(field);
                return new Value(fieldSet);
            } else {
                return new Value(field);
            }
        }
        return undefined(symbol);
    }

    private static Type toType(String s) {
        return switch (s) {
            case "Single" -> Type.Single;
            case "Set"    -> Type.Set;
            default       -> throw new IllegalArgumentException("Undefined field type in parsing context: " + s);
        };
    }
}
