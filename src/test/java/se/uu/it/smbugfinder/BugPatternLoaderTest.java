package se.uu.it.smbugfinder;

import static se.uu.it.smbugfinder.DtlsResources.*;
import static se.uu.it.smbugfinder.DtlsResources.DtlsClientAlphabet.*;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.I_APPLICATION;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.I_CHANGE_CIPHER_SPEC;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.I_FINISHED;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.O_APPLICATION;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.O_CHANGE_CIPHER_SPEC;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.O_ECDSA_CERTIFICATE;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.O_FINISHED;
import static se.uu.it.smbugfinder.DtlsResources.DtlsJointAlphabet.O_RSA_CERTIFICATE;
import static se.uu.it.smbugfinder.DtlsResources.DtlsServerAlphabet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.word.Word;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.CustomParsingContext;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.encoding.DefaultEncodedDFAParser;
import se.uu.it.smbugfinder.encoding.MappingTokenMatcher;
import se.uu.it.smbugfinder.encoding.MappingTokenMatcher.MappingTokenMatcherBuilder;
import se.uu.it.smbugfinder.encoding.OcamlValues;
import se.uu.it.smbugfinder.encoding.SymbolToken;
import se.uu.it.smbugfinder.pattern.BugPattern;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;


public class BugPatternLoaderTest {
    public static Logger LOGGER = LoggerFactory.getLogger(BugPatternLoaderTest.class);

    @Test
    public void loadNonParametricBugPatternTest() throws IOException {
        BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());

        //symbols contains the entire alphabet to be used for the expansion of bug-pattern. normally symbols are taken from the Mealy SUT
        List<Symbol> symbols = Arrays.asList(I_APPLICATION, O_APPLICATION, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC, I_PSK_CLIENT_HELLO, I_PSK_CLIENT_KEY_EXCHANGE, I_FINISHED, O_FINISHED, O_SERVER_HELLO, O_SERVER_HELLO_DONE, O_CERTIFICATE_REQUEST, I_CERTIFICATE, O_HELLO_VERIFY_REQUEST);
        BugPatterns patterns = loader.loadPatterns(DTLS_SERVER_BUG_PATTERNS, symbols); //Here bug patterns are returned expanded with symbols as defined above and not from the Mealy SUT

        BugPattern earlyFinishedPattern = patterns.getBugPattern(EARLY_FINISHED);
        checkPattern(earlyFinishedPattern, symbols, 3, // init, bug and sink states
                new TestCase(Word.fromSymbols(I_FINISHED, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_FINISHED, O_CHANGE_CIPHER_SPEC), false));
        BugPattern certlessAuthPattern = patterns.getBugPattern(CERTLESS_AUTH);
        checkPattern(certlessAuthPattern, symbols, 4,
                new TestCase(Word.fromSymbols(O_CERTIFICATE_REQUEST, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(O_CERTIFICATE_REQUEST, O_APPLICATION, O_HELLO_VERIFY_REQUEST, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(O_CERTIFICATE_REQUEST, O_APPLICATION, O_HELLO_VERIFY_REQUEST, I_CERTIFICATE, O_CHANGE_CIPHER_SPEC), false),
                new TestCase(Word.fromSymbols(O_CERTIFICATE_REQUEST, O_SERVER_HELLO), false));
        BugPattern multCCSPattern = patterns.getBugPattern(MULT_CCS);
        checkPattern(multCCSPattern, symbols, 5, // init, bug and sink states
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, O_SERVER_HELLO), false),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_APPLICATION, O_APPLICATION, I_FINISHED), false),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_APPLICATION, O_APPLICATION, I_FINISHED, O_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC), false),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, O_SERVER_HELLO, I_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_SERVER_HELLO, I_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC), true),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC, I_APPLICATION, I_PSK_CLIENT_HELLO), true),
                new TestCase(Word.fromSymbols(I_CHANGE_CIPHER_SPEC, I_FINISHED, O_CHANGE_CIPHER_SPEC), false));
    }

    @Test
    public void loadClientParametricBugPatternTest() {
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
        // builder now contains a mapping from SymbolToken to Symbol list

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols);
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS, symbols);
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();

        BugPattern switchingCS = patterns.get(0);
        checkPattern(switchingCS, symbols, 7,
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_PSK_SERVER_HELLO, O_APPLICATION), true),
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_RSA_SERVER_HELLO, O_APPLICATION), false));

        BugPattern wrongCertType = patterns.get(1);
        checkPattern(wrongCertType, symbols, 5,
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_ECDSA_CERTIFICATE), true),
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_RSA_CERTIFICATE), false));
    }

    @Test
    public void loadClientParametricBugPatternTestFromFile() {

        String fullPath = ResourceManager.getResourceAsAbsolutePathString(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS);
        OcamlValues parameters = new OcamlValues(fullPath);


        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new CustomParsingContext(parameters));
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);

        MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();

        List<SymbolToken> symbolTokens = new ArrayList<SymbolToken>();
        Collections.addAll(symbolTokens,
            new SymbolToken(true, "APPLICATION"),
            new SymbolToken(false, "APPLICATION"),
            new SymbolToken(false, "CHANGE_CIPHER_SPEC"),
            new SymbolToken(true, "HELLO_REQUEST")
        );
        // we add symbols from the language file (parameters) + from the symbolTokens list (in order to achieve the same result as loadClientParametricBugPatternTest)
        // this has been simplified so that Input & Output Symbols are automatically generated from SymbolTokens when only one variant exists
        // SymbolTokens are capitalized as the parametric bug patterns will have capitalized messages (as in plain bug patterns)
        builder.addSymbolTokens(parameters, symbolTokens);

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols); //symbols now has all the symbols from above (I_APPLICATION etc)
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS, symbols); //in test directory, we have the same patterns as above but words are capitalised
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();

        BugPattern switchingCS = patterns.get(0);
        checkPattern(switchingCS, symbols, 7,
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_PSK_SERVER_HELLO, O_APPLICATION), true),
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_RSA_SERVER_HELLO, O_APPLICATION), false));

        BugPattern wrongCertType = patterns.get(1);
        checkPattern(wrongCertType, symbols, 5,
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_ECDSA_CERTIFICATE), true),
                new TestCase(Word.fromSymbols(I_RSA_SIGN_CERTIFICATE_REQUEST, O_RSA_CERTIFICATE), false));
    }

    @Test
    public void loadServerParametricBugPatternTest() {
        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new DtlsParsingContext());
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);

        MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();
        builder
        .map(new SymbolToken(true, "ClientHello"), I_ECDH_CLIENT_HELLO, I_DH_CLIENT_HELLO, I_PSK_CLIENT_HELLO, I_RSA_CLIENT_HELLO)
        .map(new SymbolToken(false, "HelloVerifyRequest"), O_HELLO_VERIFY_REQUEST)
        .map(new SymbolToken(false, "ServerHello"), O_SERVER_HELLO)
        .map(new SymbolToken(false, "ServerHelloDone"), O_SERVER_HELLO_DONE)
        .map(new SymbolToken(false, "CertificateRequest"), O_CERTIFICATE_REQUEST);
        // builder now contains a mapping from SymbolToken to Symbol list

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols);
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DTLS_SERVER_PARAMETRIC_BUG_PATTERNS, symbols);
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();

        BugPattern nonConformingCookie = patterns.get(0);
        checkPattern(nonConformingCookie, symbols, 12,
                new TestCase(Word.fromSymbols(I_PSK_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_RSA_CLIENT_HELLO, O_SERVER_HELLO), true),
                new TestCase(Word.fromSymbols(I_PSK_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_PSK_CLIENT_HELLO, O_SERVER_HELLO), false),
                new TestCase(Word.fromSymbols(I_RSA_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_RSA_CLIENT_HELLO, O_SERVER_HELLO), false));
    }

    @Test
    public void loadServerParametricBugPatternTestFromFile() {
        String fullPath = ResourceManager.getResourceAsAbsolutePathString(DTLS_SERVER_PATTERN_LANGUAGE);
        OcamlValues parameters = new OcamlValues(fullPath);

        DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new CustomParsingContext(parameters));
        DefaultDFADecoder decoder = new DefaultDFADecoder(parser);

        MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();

        List<SymbolToken> symbolTokens = new ArrayList<SymbolToken>();
        Collections.addAll(symbolTokens,
            new SymbolToken(false, "HELLO_VERIFY_REQUEST"),
            new SymbolToken(false, "SERVER_HELLO"),
            new SymbolToken(false, "SERVER_HELLO_DONE"),
            new SymbolToken(false, "CERTIFICATE_REQUEST")
        );

        builder.addSymbolTokens(parameters, symbolTokens);

        MappingTokenMatcher matcher = builder.build();
        List<Symbol> symbols = new ArrayList<>();
        matcher.collectSymbols(symbols);
        decoder.setTokenMatcher(matcher);

        BugPatternLoader loader = new BugPatternLoader(decoder);
        BugPatterns bugCatalogue = loader.loadPatterns(DTLS_SERVER_PARAMETRIC_BUG_PATTERNS, symbols);
        List<BugPattern> patterns = bugCatalogue.getBugPatterns();

        BugPattern nonConformingCookie = patterns.get(0);
        checkPattern(nonConformingCookie, symbols, 12,
                new TestCase(Word.fromSymbols(I_PSK_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_RSA_CLIENT_HELLO, O_SERVER_HELLO), true),
                new TestCase(Word.fromSymbols(I_PSK_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_PSK_CLIENT_HELLO, O_SERVER_HELLO), false),
                new TestCase(Word.fromSymbols(I_RSA_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, I_RSA_CLIENT_HELLO, O_SERVER_HELLO), false));
    }

    // helpers
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
