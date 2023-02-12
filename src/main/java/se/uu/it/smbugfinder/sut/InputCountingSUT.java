package se.uu.it.smbugfinder.sut;

import net.automatalib.words.Word;

public class InputCountingSUT<I,O> implements SUT<I,O> {

	private SUT<I, O> sut;
	private Counter counter;

	public InputCountingSUT(SUT<I,O> sut, Counter counter) {
		this.sut = sut;
		this.counter = counter;
	}

	@Override
	public Word<O> execute(Word<I> inputWord) {
		counter.add(inputWord.length());
		return sut.execute(inputWord);
	}

	public Counter getCounter() {
		return counter;
	}
}
