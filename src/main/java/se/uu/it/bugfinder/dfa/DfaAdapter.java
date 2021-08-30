package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.bugfinder.utils.DFAUtils;

/**
 * DFA adapter class that facilitates performing fundamental DFA operations such as complementing, intersection etc.
 */
public class DfaAdapter {
	private DFA<?, Symbol> dfa;
	private List<Symbol> symbols;

	DfaAdapter(DFA<?, Symbol> dfa, Collection<Symbol> symbols) {
		this.dfa = dfa;
		this.symbols = new ArrayList<>(symbols);
	}
	
	public DFA<?, Symbol> getDfa() {
		return dfa;
	}
	
	public List<Symbol> getSymbols() {
		return symbols;
	}
	
	private FastDFA<Symbol>  newDfa() {
		return new FastDFA<>(new ListAlphabet<Symbol>(symbols));
	}

	/**
	 * Returns an adapter containing the complement of the underlying DFA.
	 */
	public DfaAdapter complement() {
		FastDFA<Symbol> complementDfa = newDfa();
		DFAs.complement(dfa, symbols, complementDfa);
		return new DfaAdapter(complementDfa, symbols);
	}
	
	/**
	 * Returns an adapter containing the intersection between the underlying DFA and the DFA of another adapter.
	 * It is advisable that this model is reduced/minimized, since it may contain many unreachable states.
	 */
	public DfaAdapter intersect(DfaAdapter adapter) {
		FastDFA<Symbol> intersectionDfa = newDfa();
		DFAs.combine(adapter.getDfa(), dfa, symbols, intersectionDfa, AcceptanceCombiner.AND);
		return new DfaAdapter(intersectionDfa, intersectionDfa.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing the union between the underlying DFA and the DFA of another adapter.
	 * It is advisable that this model is reduced/minimized, since it may contain many unreachable states.
	 */
	public DfaAdapter union(DfaAdapter adapter) {
		FastDFA<Symbol> unionDfa = newDfa();
		DFAs.combine(adapter.getDfa(), dfa, symbols, unionDfa, AcceptanceCombiner.OR);
		return new DfaAdapter(unionDfa, unionDfa.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing a reduced copy of the underlying DFA in which unreachable states are removed.
	 */
	public DfaAdapter reduce() {
		List<FastDFAState> reachableStates = new ArrayList<>();
		FastDFA<Symbol> reducedDfa = newDfa(); 
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, symbols, reducedDfa);
		DFAUtils.reachableStates(reducedDfa, symbols, reachableStates);
		List<FastDFAState> statesToRemove = new LinkedList<>(reducedDfa.getStates());
		statesToRemove.removeAll(reachableStates);
		statesToRemove.forEach(s -> reducedDfa.removeState(s));
		return new DfaAdapter(reducedDfa, reducedDfa.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing the DFA resulting from minimizing the underlying DFA using Hopcroft's minimization algorithm.
	 */
	public DfaAdapter minimize() {
		CompactDFA<Symbol> result = DFAs.minimize(dfa, new ListAlphabet<>(symbols));
		FastDFA<Symbol> minimizedCopy = newDfa();
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, result, result.getInputAlphabet(), minimizedCopy);
		return new DfaAdapter(minimizedCopy, minimizedCopy.getInputAlphabet());
	}
	
	/**
	 * Returns an adapter containing the a fully specified copy of the underlying DFA.
	 */
	public DfaAdapter complete() {
		FastDFA<Symbol> fullySpecifiedDfa = newDfa();
		DFAs.complete(dfa, symbols, fullySpecifiedDfa);
		return new DfaAdapter(fullySpecifiedDfa, fullySpecifiedDfa.getInputAlphabet());
	}
}
