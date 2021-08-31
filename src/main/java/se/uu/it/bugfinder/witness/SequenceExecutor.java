package se.uu.it.bugfinder.witness;

import net.automatalib.words.Word;

public interface SequenceExecutor <I,O> {
	Word<O> execute(Word<I> inputs); 
}
