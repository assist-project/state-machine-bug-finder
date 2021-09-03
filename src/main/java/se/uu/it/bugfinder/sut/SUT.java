package se.uu.it.bugfinder.sut;

import net.automatalib.words.Word;

public interface SUT <I,O> {
	Word<O> execute(Word<I> inputWord); 
}
