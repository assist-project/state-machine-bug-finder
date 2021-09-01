package se.uu.it.bugfinder.witness;

import net.automatalib.words.Word;

public interface SUT <I,O> {
	Word<O> execute(Word<I> inputWord); 
}
