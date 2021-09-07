package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;

public interface EncodedDfaParser {
	public EncodedDfaHolder parseEncodedDfa(InputStream encodedDfaStream) throws Exception;
}
