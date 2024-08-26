package se.uu.it.smbugfinder.sut;

import net.automatalib.word.Word;

public interface SUT <I,O> {
    Word<O> execute(Word<I> inputWord);
}
