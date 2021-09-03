package se.uu.it.bugfinder.encoding;

import java.io.Reader;
import java.util.Collection;

import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.Symbol;

public interface DfaDecoder {
	public DfaAdapter decode(Reader encodedDfaSource, Collection<Symbol> symbols) throws Exception;
}
