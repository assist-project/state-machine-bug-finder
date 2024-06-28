package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;

public interface EncodedTSParser {
    public EncodedTS parse(InputStream encodedTsStream) throws Exception;
}
