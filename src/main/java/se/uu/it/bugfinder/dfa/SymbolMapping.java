package se.uu.it.bugfinder.dfa;

import java.util.List;

public interface SymbolMapping <I,O> {
	I toInput(InputSymbol symbol);
	O toOutput(OutputSymbol symbol);
	O toOutput(List<OutputSymbol> symbols);
	InputSymbol fromInput(I input);
	List<OutputSymbol> fromOutput(O output);
}
