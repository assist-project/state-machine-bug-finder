package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public interface SymbolMapping <I,O> {
	I toInput(InputSymbol symbol);
	O toOutput(OutputSymbol symbol);
	O toOutput(List<OutputSymbol> symbols);
	InputSymbol fromInput(I input);
	List<OutputSymbol> fromOutput(O output);
	
	default List<OutputSymbol> fromOutputs(Collection<O> outputs) {
		List<OutputSymbol> outputSymbols = new ArrayList<OutputSymbol>();
		fromOutputs(outputs, outputSymbols);
		return outputSymbols;
	}
	
	default void fromOutputs(Collection<O> outputs, Collection<OutputSymbol> outputSymbols) {
		outputs.stream().forEach(o -> outputSymbols.addAll(fromOutput(o)));
	}
	
	default List<InputSymbol> fromInputs(Collection<I> inputs) {
		List<InputSymbol> inputSymbols = new ArrayList<InputSymbol>();
		fromInputs(inputs, inputSymbols);
		return inputSymbols;
	}
	
	default void fromInputs(Collection<I> inputs, Collection<InputSymbol> inputSymbols) {
		inputs.stream().forEach(i -> inputSymbols.add(fromInput(i)));
	}
	
	default void toInputs(Collection<InputSymbol> inputSymbols, Collection<I> inputs) {
		inputSymbols.forEach(sym -> inputs.add(toInput(sym)));
		
	}
	
	default List<I> toInputs(Collection<InputSymbol> inputSymbols) {
		List<I> inputs = new ArrayList<I>(inputSymbols.size());
		toInputs(inputSymbols, inputs);
		return inputs;
	}
	
	default void toOutputs(Collection<OutputSymbol> outputSymbols, Collection<O> outputs) {
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
}
