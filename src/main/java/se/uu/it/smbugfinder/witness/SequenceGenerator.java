package se.uu.it.smbugfinder.witness;

import java.util.Collection;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

public interface SequenceGenerator<I> {
    /**
     * Generates sequences accepted by {@code bugLanguage} using the supplied inputs.
     */
    <S>  Iterable<Word<I>> generateSequences(DFA<S, I> bugLanguage, Collection<I> inputs);

}
