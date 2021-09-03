package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.bugfinder.utils.DFAUtils;
import se.uu.it.bugfinder.utils.MealyUtils;

public class MealyToDfaConverter {
	
	public MealyToDfaConverter() {
		
	}
	
	public <S, I, O> DfaAdapter convert(MealyMachine<S, I, ?, O> mealy,
			Collection<I> inputs, SymbolMapping<I,O> mapping) {
		Set<O> outputs = new LinkedHashSet<>();
		MealyUtils.reachableOutputs(mealy, inputs, outputs);
		
		Mapping<I, Symbol> inputMapping = i -> mapping.fromInput(i);
		
		Mapping<Pair<S, O>, List<Symbol>> outputMapping = pair -> {
			return new ArrayList<Symbol>(mapping.fromOutput(pair.getSecond()));
		};
		
		Set<Symbol> symbols = new LinkedHashSet<>();
		mapping.fromInputs(inputs, symbols);
		mapping.fromOutputs(outputs, symbols);
		
		Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
		FastDFA<Symbol> dfa = new FastDFA<>(alphabet);
		
		DFAUtils.convertMealyToDFA(mealy, inputs, symbols, inputMapping, outputMapping, new LinkedHashMap<>(), dfa);
		
		List<FastDFAState> rejecting = dfa.getStates().stream().filter(s -> !s.isAccepting()).collect(Collectors.toList());
		if (rejecting.size() != 1) {
			throw new InternalError("The DFA generated from the learned model is expected to have only one rejecting state");
		}
		
		dfaPostProcessor(dfa, mealy, inputs, mapping);
		
		return new DfaAdapter(dfa, symbols).minimize();
	}
	
	protected <DS, DI, MS, I, O> void dfaPostProcessor(MutableDFA<DS, DI> dfa, MealyMachine<MS, I, ?, O> mealy,
			Collection<I> inputs, SymbolMapping<I,O> mapping) {
	}
}
