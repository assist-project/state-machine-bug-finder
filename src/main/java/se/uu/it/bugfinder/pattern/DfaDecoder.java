package se.uu.it.bugfinder.pattern;

import java.util.Collection;

import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.encoding.EncodedDfaHolder;

public interface DfaDecoder {
	public DfaAdapter decode(EncodedDfaHolder encodedDfa, 
			Collection<Symbol> symbols);
}
