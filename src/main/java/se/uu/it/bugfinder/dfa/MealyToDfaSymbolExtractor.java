package se.uu.it.bugfinder.dfa;

import java.util.Collection;

import net.automatalib.automata.transducers.MealyMachine;

public class MealyToDfaSymbolExtractor {
	
	public static <S, I, O> void extractSymbols(MealyMachine<S, I, ?, O> mealy,
			Collection<I> inputs, SymbolMapping<I,O> mapping) {
		
	}

}
