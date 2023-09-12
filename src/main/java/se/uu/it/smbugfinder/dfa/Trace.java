package se.uu.it.smbugfinder.dfa;

import java.util.Iterator;
import java.util.stream.Stream;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public class Trace<I,O> implements Iterable<Pair<I,O>> {
    private Word<I> inputWord;
    private Word<O> outputWord;

    public Trace(Word<I> inputs, Word<O> outputs) {
        super();
        this.inputWord = inputs;
        this.outputWord = outputs;
    }

    public Word<I> getInputWord() {
        return inputWord;
    }
    public Word<O> getOutputWord() {
        return outputWord;
    }

    public int getLength() {
        return inputWord.length();
    }

    public Stream<Pair<I,O>> getInputOutputStream() {
        Stream.Builder<Pair<I,O>> builder = Stream.builder();
        for (int i=0; i<getLength(); i++) {
            builder.add(Pair.of(inputWord.getSymbol(i), outputWord.getSymbol(i)));
        }
        return builder.build();
    }

    @Override
    public Iterator<Pair<I,O>> iterator() {
        return getInputOutputStream().iterator();
    }

    public String toCompactString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Trace: ");
        for (int i=0; i<getLength(); i++) {
            builder.append(inputWord.getSymbol(i) + "/" + outputWord.getSymbol(i)).append(' ');
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return String.format("Trace: %n  inputs: %s%n  outputs: %s%n", inputWord, outputWord);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputWord == null) ? 0 : inputWord.hashCode());
        result = prime * result + ((outputWord == null) ? 0 : outputWord.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Trace<?,?> other = (Trace) obj;
        if (inputWord == null) {
            if (other.inputWord != null)
                return false;
        } else if (!inputWord.equals(other.inputWord))
            return false;
        if (outputWord == null) {
            if (other.outputWord != null)
                return false;
        } else if (!outputWord.equals(other.outputWord))
            return false;
        return true;
    }
}
