package se.uu.it.smbugfinder.dfa;

import java.util.List;

import net.automatalib.word.Word;

public class SymbolTrace extends Trace<InputSymbol,List<OutputSymbol>>{

    public SymbolTrace(Word<InputSymbol> inputs, Word<List<OutputSymbol>> outputs) {
        super(inputs, outputs);
    }

    public <I,O> Trace<I,O> asIOTrace(SymbolMapping<I,O> mapping) {
        Word<I> inputs = getInputWord().transform(s -> mapping.toInput(s));
        Word<O> outputs = getOutputWord().transform(outSyms -> mapping.toOutput(outSyms));
        return new Trace<I,O>(inputs, outputs);
    }
}
