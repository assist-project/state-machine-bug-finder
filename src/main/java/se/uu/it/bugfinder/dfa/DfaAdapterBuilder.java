package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.bugfinder.encoding.DecodingTS;
import se.uu.it.bugfinder.encoding.DefaultTokenMatcher;
import se.uu.it.bugfinder.encoding.Label;
import se.uu.it.bugfinder.encoding.TokenMatcher;
import se.uu.it.bugfinder.utils.DFAUtils;
import se.uu.it.bugfinder.utils.MealyUtils;

/**
 * Implements two backbone operations which both result in DFAs.
 * First is conversion of a Mealy machine to a DFA.
 * Second is generating a DFA from a condensed specification of a bug pattern. 
 */
public class DfaAdapterBuilder {
	private Consumer<MutableDFA<?,Symbol>> systemDfaPostProcessor = a -> {};
	private TokenMatcher tokenMatcher = new DefaultTokenMatcher();
	
	
	public void setSystemDfaPostProcessor(Consumer<MutableDFA<?,Symbol>> systemDfaPostProcessor) {
		this.systemDfaPostProcessor = systemDfaPostProcessor;
	}
	
	public void setTokenMatcher(TokenMatcher tokenMatcher) {
		this.tokenMatcher = tokenMatcher;
	}
	
	public <S, I, O> DfaAdapter fromSystemModel(MealyMachine<S, I, ?, O> mealy,
			Collection<I> inputs, SymbolMapping<I,O> mapping) {
		Set<O> outputs = new LinkedHashSet<>();
		MealyUtils.reachableOutputs(mealy, inputs, outputs);
		
		Mapping<I, Symbol> inputMapping = i -> mapping.fromInput(i);
		
		Mapping<Pair<S, O>, List<Symbol>> outputMapping = pair -> {
			return new ArrayList<Symbol>(mapping.fromOutput(pair.getSecond()));
		};
		
		List<InputSymbol> inputSymbols = mapping.fromInputs(inputs);
		List<OutputSymbol> outputSymbols = mapping.fromOutputs(outputs);
		
		List<Symbol> symbols = new ArrayList<Symbol>(inputs.size() + outputSymbols.size());
		symbols.addAll(inputSymbols);
		symbols.addAll(outputSymbols);
		
		Alphabet<Symbol> alphabet = new ListAlphabet<>(symbols);
		FastDFA<Symbol> dfa = new FastDFA<>(alphabet);
		
		DFAUtils.convertMealyToDFA(mealy, inputs, symbols, inputMapping, outputMapping, new LinkedHashMap<>(), dfa);
		
		List<FastDFAState> rejecting = dfa.getStates().stream().filter(s -> !s.isAccepting()).collect(Collectors.toList());
		if (rejecting.size() != 1) {
			throw new InternalError("The DFA generated from the learned model is expected to have only one rejecting state");
		}
		
		systemDfaPostProcessor.accept(dfa);
		
		return new DfaAdapter(dfa, symbols).minimize();
	}
	
	public DfaAdapter fromSpecification(DFA<?, Label> specification, Collection<Label> labels, 
			Collection<Symbol> symbols) {
		FastDFA<Symbol> unfoldedSpec = unfold(specification, labels, symbols);
		FastDFA<Symbol> inputCompleteSpec = new FastDFA<Symbol>(new ListAlphabet<Symbol>(new ArrayList<>(symbols)));
		DFAs.complete(unfoldedSpec, inputCompleteSpec.getInputAlphabet(), inputCompleteSpec);
		DfaAdapter unfoldedDfa = new DfaAdapter(unfoldedSpec, unfoldedSpec.getInputAlphabet());
		return unfoldedDfa.minimize();
	}
	
	private <S> FastDFA<Symbol> unfold(DFA<S, Label> specification, Collection<Label> labels, Collection<Symbol> symbols) {
		Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
		FastDFA<Symbol> unfoldedSpecification = new FastDFA<>(alphabet);
		DecodingTS<S> specificationTS = new DecodingTS<S>(specification, labels);
		specificationTS.setTokenMatcher(tokenMatcher);
		TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, specificationTS, -1, symbols, unfoldedSpecification);
		return unfoldedSpecification;
	}
}
