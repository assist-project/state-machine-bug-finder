package se.uu.it.smbugfinder.sut;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class SimulatedMealySUT <I,O> implements SUT <I,O> {
	private MealyMachine<?, I, ?, O> mealy;
	
	public SimulatedMealySUT(MealyMachine<?,I,?,O> mealy) {
		this.mealy = mealy;
		
	}
	@Override
	public Word<O> execute(Word<I> inputWord) {
		Word<O> output = doExecute(inputWord, mealy);
		return output;
	}
	
	private <S> Word<O> doExecute(Word<I> inputWord, MealyMachine<S, I, ?, O> mealy) {
		WordBuilder<O> builder = new WordBuilder<>();
		S crtState = mealy.getInitialState();
		for (I input : inputWord) {
			builder.add(mealy.getOutput(crtState, input));
			crtState = mealy.getSuccessor(crtState, input);
		}
		return builder.toWord();
	}
}
