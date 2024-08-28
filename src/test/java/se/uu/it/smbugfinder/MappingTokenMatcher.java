package se.uu.it.smbugfinder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.DefaultTokenMatcher;
import se.uu.it.smbugfinder.encoding.DescriptionToken;
import se.uu.it.smbugfinder.encoding.DescriptionType;
import se.uu.it.smbugfinder.encoding.RuntimeDecodingException;
import se.uu.it.smbugfinder.encoding.SymbolToken;

public class MappingTokenMatcher extends DefaultTokenMatcher {

    private Map<Key, Set<Symbol>> map;

    private MappingTokenMatcher(Map<Key, Set<Symbol>> map) {
        this.map = map;
    }

    @Override
    public DescriptionToken matchingAtomicToken(Symbol symbol, DescriptionToken description) {
        if (description.getType() == DescriptionType.SYMBOL) {
            Set<Symbol> mappedSymbols = map.get(new Key(((SymbolToken) description)));
            if (mappedSymbols == null) {
                throw new RuntimeDecodingException(String.format("SymbolToken %s has no associated symbols",((SymbolToken) description).getSymbolString()));
            }
            if (mappedSymbols.contains(symbol)) {
                return description;
            } else {
                return null;
            }
        }
        return description;
    }

    public void collectSymbols(Collection<Symbol> symbols) {
        map.values().stream().flatMap(s -> s.stream()).distinct().forEach(s -> symbols.add(s));
    }

    private static class Key {
        private String name;
        private Boolean input;

        private Key(SymbolToken token) {
            this.name = token.getSymbolString();
            this.input = token.isInput();
        }

        @Override
        public int hashCode() {
            return Objects.hash(input, name);
        }

        @Override
        @SuppressWarnings("EqualsGetClass")
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            return Objects.equals(input, other.input) && Objects.equals(name, other.name);
        }
    }

    static class MappingTokenMatcherBuilder {
        private Map<Key, Set<Symbol>> map = new LinkedHashMap<>();

        public MappingTokenMatcherBuilder map(SymbolToken token, Symbol ... symbols) {
            Set<Symbol> symbolSet = new LinkedHashSet<>();
            for (Symbol symbol : symbols) {
                symbolSet.add(symbol);
            }
            map.put(new Key(token), symbolSet);
            return this;
        }

        public MappingTokenMatcher build() {
            return new MappingTokenMatcher(map);
        }
    }
}
