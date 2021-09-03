package se.uu.it.bugfinder.sut;

import net.automatalib.words.Word;

public class ResetCountingSUT<I,O> implements SUT<I,O> {
	
	private SUT<I, O> sut;
	private Counter counter;

	public ResetCountingSUT(SUT<I,O> sut, Counter counter) {
		this.sut = sut;
		this.counter = counter;
	}

	@Override
	public Word<O> execute(Word<I> inputWord) {
		counter.increment();
		return sut.execute(inputWord);
	}
	
	public Counter getCounter() {
		return counter;
	}
	
}
