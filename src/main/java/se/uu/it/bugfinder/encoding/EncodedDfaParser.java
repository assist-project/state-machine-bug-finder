package se.uu.it.bugfinder.encoding;

import java.io.Reader;

public interface EncodedDfaParser {
	public EncodedDfaHolder parseEncodedDfa(Reader specSource) throws Exception;
}
