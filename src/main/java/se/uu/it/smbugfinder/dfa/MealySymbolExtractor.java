package se.uu.it.smbugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automaton.transducer.MealyMachine;
import se.uu.it.smbugfinder.utils.MealyUtils;

public class MealySymbolExtractor {

    public static <S, I, O> void extractSymbols(MealyMachine<S, I, ?, O> mealy,
            Collection<I> inputs, SymbolMapping<I,O> mapping, Collection<Symbol> symbols) {
        List<O> reachableOutputs = new ArrayList<>();
        MealyUtils.reachableOutputs(mealy, inputs, reachableOutputs);
        Set<Symbol> uniqueSymbols = new LinkedHashSet<Symbol>();
        uniqueSymbols.addAll(mapping.fromInputs(inputs));
        uniqueSymbols.addAll(mapping.fromOutputs(reachableOutputs));
        symbols.addAll(uniqueSymbols);
    }

}
