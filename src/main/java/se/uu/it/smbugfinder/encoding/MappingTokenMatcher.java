package se.uu.it.smbugfinder.encoding;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;

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

    static public class MappingTokenMatcherBuilder {
        private Map<Key, Set<Symbol>> map = new LinkedHashMap<>();

        public MappingTokenMatcherBuilder map(SymbolToken token, Symbol ... symbols) {
            return map(token, Arrays.asList(symbols));
        }

        public MappingTokenMatcherBuilder map(SymbolToken token, Collection<Symbol> symbols) {
            map.put(new Key(token), new LinkedHashSet<>(symbols));
            return this;
        }

        public MappingTokenMatcher build() {
            return new MappingTokenMatcher(map);
        }

        public MappingTokenMatcherBuilder addSymbolTokens(OcamlValues params, Collection<SymbolToken> symbolTokens) {
            MappingTokenMatcherBuilder builder = null;
            for (SymbolToken st : symbolTokens) {
                builder = map(st, st.getSymbol());
            }

            Map<String, List<String>> fields = params.getFieldsMap();
            Map<String, List<String>> messageMap = params.getMessageMap();

            // for each message we find in the lang file we generate both input and output symbols
            for (Map.Entry<String, List<String>> mesKey : messageMap.entrySet()) {
                String mes = mesKey.getKey();
                String key = mesKey.getValue().get(1);

                Collection<String> values = fields.get(key);

                builder = map(new SymbolToken(true, mes), InputSymbol.generateInputSymbols(mes, values));
                builder = map(new SymbolToken(false, mes), OutputSymbol.generateOutputSymbols(mes, values));
            }
            return builder;
        }

        public MappingTokenMatcherBuilder addMapFromSymbols(OcamlValues params, Collection<Symbol> symbols) {
            MappingTokenMatcherBuilder builder = null;

            Map<String, List<String>> fields = params.getFieldsMap();
            Map<String, List<String>> messageMap = params.getMessageMap();

            // for each message we find in lang file we generate both input and output symbols
            for (Map.Entry<String, List<String>> mesKey : messageMap.entrySet()) {
                String mes = mesKey.getKey();
                String key = mesKey.getValue().get(1);

                Collection<String> values = fields.get(key);

                Set<Symbol> parametricSymbols = symbols.stream()
                    .filter(s -> values.stream().anyMatch(value -> (value +"_" +  mes).equals(s.getName())))
                    .collect(Collectors.toSet());

                Set<Symbol> inputSymbols = parametricSymbols.stream()
                    .filter(s -> s instanceof InputSymbol)
                    .collect(Collectors.toSet());

                Set<Symbol> outputSymbols = parametricSymbols.stream()
                    .filter(s -> s instanceof OutputSymbol)
                    .collect(Collectors.toSet());

                builder = map(new SymbolToken(true, mes), inputSymbols);
                builder = map(new SymbolToken(false, mes), outputSymbols);

                symbols.removeAll(parametricSymbols);
            }

            for (Symbol s : symbols) {
                builder = map(s.toSymbolToken(), s);
            }

            return builder;
        }
    }
}
