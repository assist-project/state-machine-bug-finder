package se.uu.it.smbugfinder.sut;

import java.time.Duration;

import net.automatalib.words.Word;

public class TimedSUT <I,O> implements SUT<I, O> {
    
    private Duration duration;
    private SUT<I, O> sut;
    private long startTime;

    public TimedSUT (SUT<I,O> sut, Duration duration) {
        startTime = System.currentTimeMillis();
        this.sut = sut;
        this.duration = duration;
    } 

    @Override
    public Word<O> execute(Word<I> inputWord) {
        Word<O> result = sut.execute(inputWord);
        if (System.currentTimeMillis() - startTime > duration.toMillis()) {
            throw new TimeoutException();
        }
        return result;
    }

}
