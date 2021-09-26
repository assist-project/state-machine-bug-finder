package se.uu.it.smbugfinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;

public class StringSymbolMapper implements SymbolMapping<String,String> {
	
	private String emptyOutput;
	private String sep;
//	private boolean emptyLab

	public StringSymbolMapper(String emptyOutput, String sep) {
		this.emptyOutput = emptyOutput;
		this.sep = sep;
	}
	
	@Override
	public String toInput(InputSymbol symbol) {
		return symbol.name();
	}

	@Override
	public String toOutput(OutputSymbol symbol) {
		return symbol.name();
	}

	@Override
	public String toOutput(Collection<OutputSymbol> symbols) {
		StringBuilder builder = new StringBuilder();
		for (OutputSymbol symbol : symbols) {
			builder.append(symbol.name() + sep);
		}
		return builder.substring(0, builder.length() - sep.length()).toString();
	}

	@Override
	public InputSymbol fromInput(String input) {
		return new InputSymbol(input);
	}

	@Override
	public List<OutputSymbol> fromOutput(String output) {
		return Arrays.stream(output.split(Pattern.quote(sep))).map(s -> new OutputSymbol(s)).collect(Collectors.toList());
	}

	@Override
	public String emptyOutput() {
		return emptyOutput;
	}
}
