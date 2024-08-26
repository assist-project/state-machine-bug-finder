package se.uu.it.smbugfinder.sut;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

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
