package se.uu.it.bugfinder.witness;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

public interface SequenceGenerator<I> {
	/**
	 * Generates sequences accepted by {@code bugLanguage} using the supplied inputs.
	 */
	<S>  Iterable<Word<I>> generateSequences(DFA<S, I> bugLanguage, Collection<I> inputs);
	
}
