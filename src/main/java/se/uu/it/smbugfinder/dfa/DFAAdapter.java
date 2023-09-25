package se.uu.it.smbugfinder.dfa;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.smbugfinder.utils.DFAUtils;

/**
 * DFA adapter class that facilitates performing fundamental DFA operations such as complementing, intersection etc.
 */
public class DFAAdapter {
    private DFA<?, Symbol> dfa;
    private List<Symbol> symbols;

    public DFAAdapter(DFA<?, Symbol> dfa, Collection<Symbol> symbols) {
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
    public DFAAdapter complement() {
        FastDFA<Symbol> complementDfa = newDfa();
        DFAs.complement(dfa, symbols, complementDfa);
        return new DFAAdapter(complementDfa, symbols);
    }

    /**
     * Returns an adapter containing the intersection between the underlying DFA and the DFA of another adapter.
     * It is advisable that this model is reduced/minimized, since it may contain many unreachable states.
     */
    public DFAAdapter intersect(DFAAdapter adapter) {
        FastDFA<Symbol> intersectionDfa = newDfa();
        DFAs.combine(adapter.getDfa(), dfa, symbols, intersectionDfa, AcceptanceCombiner.AND);
        return new DFAAdapter(intersectionDfa, intersectionDfa.getInputAlphabet());
    }

    /**
     * Returns an adapter containing the union between the underlying DFA and the DFA of another adapter.
     * It is advisable that this model is reduced/minimized, since it may contain many unreachable states.
     */
    public DFAAdapter union(DFAAdapter adapter) {
        FastDFA<Symbol> unionDfa = newDfa();
        DFAs.combine(adapter.getDfa(), dfa, symbols, unionDfa, AcceptanceCombiner.OR);
        return new DFAAdapter(unionDfa, unionDfa.getInputAlphabet());
    }

    /**
     * Returns an adapter containing a reduced copy of the underlying DFA in which unreachable states are removed.
     */
    public DFAAdapter reduce() {
        List<FastDFAState> reachableStates = new ArrayList<>();
        FastDFA<Symbol> reducedDfa = newDfa();
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, symbols, reducedDfa);
        DFAUtils.reachableStates(reducedDfa, symbols, reachableStates);
        List<FastDFAState> statesToRemove = new ArrayList<>(reducedDfa.getStates());
        statesToRemove.removeAll(reachableStates);
        statesToRemove.forEach(s -> reducedDfa.removeState(s));
        return new DFAAdapter(reducedDfa, reducedDfa.getInputAlphabet());
    }

    /**
     * Returns an adapter containing the DFA resulting from minimizing the underlying DFA using Hopcroft's minimization algorithm.
     */
    public DFAAdapter minimize() {
        CompactDFA<Symbol> result = DFAs.minimize(dfa, new ListAlphabet<>(symbols));
        FastDFA<Symbol> minimizedCopy = newDfa();
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, result, result.getInputAlphabet(), minimizedCopy);
        return new DFAAdapter(minimizedCopy, minimizedCopy.getInputAlphabet());
    }

    /**
     * Returns an adapter containing the a fully specified copy of the underlying DFA.
     */
    public DFAAdapter complete() {
        FastDFA<Symbol> fullySpecifiedDfa = newDfa();
        DFAs.complete(dfa, symbols, fullySpecifiedDfa);
        return new DFAAdapter(fullySpecifiedDfa, fullySpecifiedDfa.getInputAlphabet());
    }

    public boolean accepts(Word<Symbol> sequence) {
        return symbols.containsAll(sequence.asList()) && dfa.accepts(sequence);
    }

    public boolean acceptsPrefix(Word<Symbol> sequence) {
        if (symbols.containsAll(sequence.asList())) {
            return sequence.prefixes(true).stream().anyMatch( p -> dfa.accepts(p));
        }
        return false;
    }

    /**
     * True if the specification is equivalent to the empty language.
     */
    public boolean isEmpty() {
        return DFAs.acceptsEmptyLanguage(dfa);
    }

    /**
     * Generates a shortest accepting sequence.
     */
    public Word<Symbol> getShortestAcceptingSequence() {
        return DFAUtils.findShortestAcceptingWord(dfa, symbols);
    }

    public String path(Word<Symbol> sequence) {
        StringBuilder builder = new StringBuilder();
        Word<Symbol> prefix = Word.epsilon();
        Object state = dfa.getState(prefix);
        for (Symbol input : sequence) {
            builder.append(state);
            prefix = prefix.append(input);
            builder.append(" ").append(input).append(" ");
            state = dfa.getState(prefix);
            if (state == null) {
                break;
            }
        }
        if (state != null) {
            builder.append(state);
        }
        return builder.toString();
    }

    public void export(Writer writer) throws IOException {
        GraphDOT.write(dfa, symbols, writer);
    }
}
