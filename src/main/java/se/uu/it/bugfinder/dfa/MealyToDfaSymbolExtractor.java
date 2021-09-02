package se.uu.it.bugfinder.dfa;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.automatalib.automata.transducers.MealyMachine;
import se.uu.it.bugfinder.utils.MealyUtils;

public class MealyToDfaSymbolExtractor {
	
	public static <S, I, O> void extractSymbols(MealyMachine<S, I, ?, O> mealy,
			Collection<I> inputs, SymbolMapping<I,O> mapping, Collection<Symbol> symbols) {
		List<O> reachableOutputs = new LinkedList<>();
		MealyUtils.reachableOutputs(mealy, inputs, reachableOutputs);
		Set<Symbol> uniqueSymbols = new LinkedHashSet<Symbol>();
		uniqueSymbols.addAll(mapping.fromInputs(inputs));
		uniqueSymbols.addAll(mapping.fromOutputs(reachableOutputs));
		symbols.addAll(uniqueSymbols);
	}

}
