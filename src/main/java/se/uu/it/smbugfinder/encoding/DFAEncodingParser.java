package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;

public interface DFAEncodingParser {
	public DFAEncoding parse(InputStream dfaEncodingStream) throws Exception;
}
