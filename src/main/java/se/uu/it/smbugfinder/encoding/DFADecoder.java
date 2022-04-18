package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;
import java.util.Collection;

import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.Symbol;

public interface DFADecoder {
	public DFAAdapter decode(InputStream dfaStream, Collection<Symbol> symbols) throws Exception;
}
