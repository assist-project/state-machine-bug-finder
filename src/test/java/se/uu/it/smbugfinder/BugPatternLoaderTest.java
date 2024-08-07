package se.uu.it.smbugfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.uu.it.smbugfinder.MappingTokenMatcher.MappingTokenMatcherBuilder;
import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.encoding.DefaultEncodedDFAParser;
import se.uu.it.smbugfinder.encoding.SymbolToken;
import se.uu.it.smbugfinder.pattern.BugPattern;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;

public class BugPatternLoaderTest {

    @Test
    public void loadParametricBugPatternTest() {
//    	Pattern SERVER_HELLO_PATTERN = Pattern.compile("(?<ciphersuite>[A-Za-z]*)_SERVER_HELLO");
        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new DtlsParsingContext());
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);
        MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();
        builder
        .map(new SymbolToken(true, "Application"), new InputSymbol("APPLICATION"))
        .map(new SymbolToken(false, "Application"), new OutputSymbol("APPLICATION"))
        .map(new SymbolToken(true, "CertificateRequest"), new InputSymbol("RSA_SIGN_CERTIFICATE_REQUEST"), new InputSymbol("ECDSA_SIGN_CERTIFICATE_REQUEST"))
        .map(new SymbolToken(false, "Certificate"), new OutputSymbol("RSA_CERTIFICATE"), new OutputSymbol("ECDSA_CERTIFICATE"))
        .map(new SymbolToken(false, "ChangeCipherSpec"), new OutputSymbol("CHANGE_CIPHER_SPEC"))
        .map(new SymbolToken(true, "HelloRequest"), new InputSymbol("HELLO_REQUEST"))
        .map(new SymbolToken(true, "ServerHello"), new InputSymbol("PSK_SERVER_HELLO"), new InputSymbol("RSA_SERVER_HELLO"));

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols);
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DtlsResources.DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS, symbols);
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();
        Assert.assertEquals(2, patterns.size());
        BugPattern switchingCS = patterns.get(0);
        checkPattern(switchingCS, symbols, 7);
        BugPattern wrongCertType = patterns.get(1);
        checkPattern(wrongCertType, symbols, 5);
    }

    public void checkPattern(BugPattern bp, Collection<Symbol> expectedSymbols, int expectedSize) {
        Assert.assertEquals(new LinkedHashSet<>(expectedSymbols), new LinkedHashSet<>(bp.generateBugLanguage().getSymbols()));
        Assert.assertEquals(expectedSize, bp.generateBugLanguage().getDfa().getStates().size());
    }
}
