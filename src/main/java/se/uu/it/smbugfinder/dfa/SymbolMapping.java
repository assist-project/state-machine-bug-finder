package se.uu.it.smbugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 *
 * A mapping from the (external) Mealy machine symbols used in model learning, to symbols in which our bug patterns are encoded.
 *
 * @param <I> Input symbol implementation.
 * @param <O> Output symbol implementation.
 */
public interface SymbolMapping <I,O> {
    I toInput(InputSymbol symbol);
    O toOutput(Collection<OutputSymbol> symbols);
    InputSymbol fromInput(I input);
    List<OutputSymbol> fromOutput(O output);
    O emptyOutput();

    default O toOutput(OutputSymbol symbol) {
        Set<OutputSymbol> symbols = Collections.singleton(symbol);
        return toOutput(symbols);
    }

    default List<OutputSymbol> fromOutputs(Collection<O> outputs) {
        List<OutputSymbol> outputSymbols = new ArrayList<OutputSymbol>();
        fromOutputs(outputs, outputSymbols);
        return outputSymbols;
    }

    default void fromOutputs(Collection<O> outputs, Collection<? super OutputSymbol> outputSymbols) {
        outputs.stream().forEach(o -> outputSymbols.addAll(fromOutput(o)));
    }

    default List<InputSymbol> fromInputs(Collection<I> inputs) {
        List<InputSymbol> inputSymbols = new ArrayList<InputSymbol>();
        fromInputs(inputs, inputSymbols);
        return inputSymbols;
    }

    default void fromInputs(Collection<I> inputs, Collection<? super InputSymbol> inputSymbols) {
        inputs.stream().forEach(i -> inputSymbols.add(fromInput(i)));
    }

    default void toInputs(Collection<InputSymbol> inputSymbols, Collection<? super I> inputs) {
        inputSymbols.forEach(sym -> inputs.add(toInput(sym)));
    }

    default List<I> toInputs(Collection<InputSymbol> inputSymbols) {
        List<I> inputs = new ArrayList<I>(inputSymbols.size());
        toInputs(inputSymbols, inputs);
        return inputs;
    }

    default void toOutputs(Collection<OutputSymbol> outputSymbols, Collection<? super O> outputs) {
        outputSymbols.forEach(sym -> outputs.add(toOutput(sym)));
    }

    default List<O> toOutputs(Collection<OutputSymbol> outputSymbols) {
        List<O> outputs = new ArrayList<O>(outputSymbols.size());
        toOutputs(outputSymbols, outputs);
        return outputs;
    }

    default Word<Symbol> fromExecutionTrace(Trace<I,O> trace) {
        WordBuilder<Symbol> builder = new WordBuilder<>();
        for (Pair<I,O> io : trace) {
            builder.add(fromInput(io.getFirst()));
            builder.addAll(fromOutput(io.getSecond()));
        }
        return builder.toWord();
    }

    default Trace<I,O> toExecutionTrace(Word<Symbol> symbolWord) {
        WordBuilder<I> inputWordBuilder = new WordBuilder<I>();
        WordBuilder<O> outputWordBuilder = new WordBuilder<O>();
        List<OutputSymbol> outputSymbols = new ArrayList<>();
        boolean first = true;

        for (Symbol symbol : symbolWord) {
            if (symbol instanceof InputSymbol inputSymbol) {
                if (!first) {
                    if (!outputSymbols.isEmpty()) {
                        outputWordBuilder.append(toOutput(outputSymbols));
                        outputSymbols.clear();
                    } else {
                        outputWordBuilder.add(emptyOutput());
                    }
                }
                inputWordBuilder.add(toInput(inputSymbol));
            } else {
                outputSymbols.add((OutputSymbol) symbol);
            }
            first = false;
        }
        outputWordBuilder.append(toOutput(outputSymbols));
        Word<I> inputWord = inputWordBuilder.toWord();
        Word<O> outputWord = outputWordBuilder.toWord();
        Trace<I,O> trace = new Trace<I,O> (inputWord, outputWord);
        return trace;
    }
}
