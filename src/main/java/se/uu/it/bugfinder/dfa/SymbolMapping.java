package se.uu.it.bugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}
