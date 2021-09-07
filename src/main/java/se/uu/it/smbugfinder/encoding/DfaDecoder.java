package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;
import java.util.Collection;

import se.uu.it.smbugfinder.dfa.DfaAdapter;
import se.uu.it.smbugfinder.dfa.Symbol;

public interface DfaDecoder {
	public DfaAdapter decode(InputStream encodedDfaStream, Collection<Symbol> symbols) throws Exception;
}
