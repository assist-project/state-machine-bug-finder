package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ArrayAlphabet;
import net.automatalib.words.impl.ListAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsLanguageAdapter;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsSymbolMapping;
import se.uu.it.dtlsfuzzer.specification.dtls.TlsFlow;
import se.uu.it.dtlsfuzzer.sut.Symbol;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.utils.DFAUtils;

public class DfaAdapter {
	private DFA<?, Symbol> dfa;
	private List<Symbol> symbols;

	private DfaAdapter(DFA<?, Symbol> dfa, Collection<Symbol> symbols) {
		this.dfa = dfa;
		this.symbols = new ArrayList<>(symbols);
	}
	
	public DFA<?, Symbol> getDfa() {
		return dfa;
	}
	
	private FastDFA<Symbol>  newDfa() {
		return new FastDFA<>(new ListAlphabet<Symbol>(symbols));
	}

	/**
	 * Returns an adapter containing the complement of the underlying dfa.
	 */
	public DfaAdapter complement() {
		FastDFA<Symbol> complementModel = newDfa();
		DFAs.complement(dfa, symbols, complementModel);
		return new DfaAdapter(complementModel, symbols);
	}
	
	/**
	 * Returns an adapter containing the intersection between the underlying dfa and the dfa of another adapter.
	 * It is advisable that this model is reduced/minimized, since it may have many unreachable states.
	 */
	public DfaAdapter intersect(DfaAdapter adapter) {
		FastDFA<Symbol> intersectionModel = newDfa();
		DFAs.combine(adapter.getDfa(), dfa, symbols, intersectionModel, AcceptanceCombiner.AND);
		return new DfaAdapter(intersectionModel, intersectionModel.getInputAlphabet());
	}
	
	
	public DfaAdapter union(DfaAdapter adapter) {
		FastDFA<Symbol> unionModel = newDfa();
		DFAs.combine(adapter.getDfa(), dfa, symbols, unionModel, AcceptanceCombiner.OR);
		return new DfaAdapter(unionModel, unionModel.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing a reduced copy of the underlying model in which unreachable states are removed.
	 */
	public DfaAdapter reduce() {
		List<FastDFAState> reachableStates = new ArrayList<>();
		FastDFA<Symbol> reducedModel = newDfa(); 
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, symbols, reducedModel);
		DFAUtils.reachableStates(reducedModel, symbols, reachableStates);
		List<FastDFAState> statesToRemove = new LinkedList<>(reducedModel.getStates());
		statesToRemove.removeAll(reachableStates);
		statesToRemove.forEach(s -> reducedModel.removeState(s));
		return new DfaAdapter(reducedModel, reducedModel.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing a reduced copy in which the last transition corresponding to this flow is directed to a rejecting sink state. 
	 */
	public DtlsLanguageAdapter eliminateTransition(TlsFlow prefix, TlsInput input) {
		FastDFA<Symbol> reducedModel = new FastDFA<>(dfaModel.getInputAlphabet());
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfaModel, dfaModel.getInputAlphabet(), reducedModel);
		Word<Symbol> labels = mapping.toLabelWord(prefix);
		Symbol transitionInput = mapping.labelForInput(input);
		FastDFAState state = reducedModel.getState(labels);
		FastDFAState sink = reducedModel.addState(false);
		reducedModel.setTransition(state, transitionInput, sink);
		for (Symbol label : reducedModel.getInputAlphabet()) {
			reducedModel.setTransition(sink, label, sink);
		}
		return new DtlsLanguageAdapter(reducedModel, mapping);
	}
	
	
	/**
	 * Returns an adapter containing a reduced copy which excludes the given inputs from the alphabet.
	 */
	public DtlsLanguageAdapter eliminateInputs(Collection<Symbol> inputs) {
		List<Symbol> currentInputs = new ArrayList<>(dfaModel.getInputAlphabet());
		currentInputs.removeAll(inputs);
		SimpleAlphabet<Symbol> alphabet = new SimpleAlphabet<>(inputs);
		FastDFA<Symbol> reducedModel = new FastDFA<>(alphabet);
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfaModel, alphabet, reducedModel);
		return new DtlsLanguageAdapter(reducedModel, mapping);
	}
	
	public DtlsLanguageAdapter eliminateInputs(Collection<TlsInput> inputs, DtlsSymbolMapping mapping) {
		List<Symbol> labels = inputs.stream().map(i -> mapping.labelForInput(i)).collect(Collectors.toList());
		return eliminateInputs(labels);
	}
	
	/**
	 * Minimizes using Hopcroft's minimization algorithm.
	 * Returns a copy containing the minimized model.
	 */
	public DfaAdapter minimize() {
		CompactDFA<Symbol> result = DFAs.minimize(dfa, new ListAlphabet<>(symbols));
		FastDFA<Symbol> minimizedCopy = newDfa();
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, result, result.getInputAlphabet(), minimizedCopy);
		return new DfaAdapter(minimizedCopy, minimizedCopy.getInputAlphabet());
	}
}
