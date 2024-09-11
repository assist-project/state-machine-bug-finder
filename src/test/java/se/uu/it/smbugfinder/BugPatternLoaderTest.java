package se.uu.it.smbugfinder;

import static se.uu.it.smbugfinder.DtlsResources.*;
import static se.uu.it.smbugfinder.DtlsResources.DtlsClientAlphabet.*;
import static se.uu.it.smbugfinder.DtlsResources.DtlsServerAlphabet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.word.Word;
import se.uu.it.smbugfinder.MappingTokenMatcher.MappingTokenMatcherBuilder;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.encoding.DefaultEncodedDFAParser;
import se.uu.it.smbugfinder.encoding.SymbolToken;
import se.uu.it.smbugfinder.pattern.BugPattern;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;

public class BugPatternLoaderTest {
    public static Logger LOGGER = LoggerFactory.getLogger(BugPatternLoaderTest.class);

    @Test
    public void loadNonParametricBugPatternTest() throws IOException {
        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser();
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);
        BugPatternLoader loader = new BugPatternLoader(decoder);
        List<Symbol> symbols = Arrays.asList(I_APPLICATION, O_APPLICATION, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC, I_PSK_CLIENT_HELLO, I_PSK_CLIENT_KEY_EXCHANGE, I_FINISHED, O_FINISHED, O_SERVER_HELLO, O_SERVER_HELLO_DONE);
        BugPatterns patterns = loader.loadPatterns(DTLS_SERVER_BUG_PATTERNS, symbols);
        BugPattern bugPattern = patterns.getBugPattern(EARLY_FINISHED);
        checkPattern(bugPattern, symbols, 3, // init, bug and sink states
                new TestCase(Word.fromSymbols(I_FINISHED, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_FINISHED, O_CHANGE_CIPHER_SPEC), false));
    }

    @Test
    public void loadParametricBugPatternTest() {
        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new DtlsParsingContext());
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);
        MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();
        builder
        .map(new SymbolToken(true, "Application"), I_APPLICATION)
        .map(new SymbolToken(false, "Application"), O_APPLICATION)
        .map(new SymbolToken(true, "CertificateRequest"), I_RSA_SIGN_CERTIFICATE_REQUEST, I_ECDSA_SIGN_CERTIFICATE_REQUEST)
        .map(new SymbolToken(false, "Certificate"), O_RSA_CERTIFICATE, O_ECDSA_CERTIFICATE)
        .map(new SymbolToken(false, "ChangeCipherSpec"), O_CHANGE_CIPHER_SPEC)
        .map(new SymbolToken(true, "HelloRequest"), I_HELLO_REQUEST)
        .map(new SymbolToken(true, "ServerHello"), I_PSK_SERVER_HELLO, I_RSA_SERVER_HELLO);

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols);
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS, symbols);
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();
        BugPattern switchingCS = patterns.get(0);

        checkPattern(switchingCS, symbols, 7,
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_PSK_SERVER_HELLO, O_APPLICATION), Boolean.TRUE),
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_RSA_SERVER_HELLO, O_APPLICATION), Boolean.FALSE));
        BugPattern wrongCertType = patterns.get(1);
        checkPattern(wrongCertType, symbols, 5,
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_ECDSA_CERTIFICATE), Boolean.TRUE),
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_RSA_CERTIFICATE), Boolean.FALSE));
    }

    public void checkPattern(BugPattern bp, Collection<Symbol> expectedSymbols, int expectedSize, TestCase ...testCases) {
        Assert.assertEquals(new LinkedHashSet<>(expectedSymbols), new LinkedHashSet<>(bp.generateBugLanguage().getSymbols()));
        Assert.assertEquals(expectedSize, bp.generateBugLanguage().getDfa().getStates().size());
        for (TestCase testCase : testCases) {
            testCase.check(bp);
        }
        java.io.StringWriter sw = new java.io.StringWriter();
        try {
            bp.generateBugLanguage().export(sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.debug(sw.toString());
    }

    static record TestCase(Word<Symbol> test, Boolean outcome) {
        public void check(BugPattern bp) {
            Assert.assertEquals(bp.generateBugLanguage().getDfa().computeOutput(test), outcome);
        }
    }
}
