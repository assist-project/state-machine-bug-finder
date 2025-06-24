package se.uu.it.smbugfinder;

import static se.uu.it.smbugfinder.DtlsResources.CERTLESS_AUTH;
import static se.uu.it.smbugfinder.DtlsResources.DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS;
import static se.uu.it.smbugfinder.DtlsResources.DTLS_SERVER_BUG_PATTERNS;
import static se.uu.it.smbugfinder.DtlsResources.DTLS_SERVER_PARAMETRIC_BUG_PATTERNS;
import static se.uu.it.smbugfinder.DtlsResources.DtlsClientAlphabet.*;
import static se.uu.it.smbugfinder.DtlsResources.DtlsServerAlphabet.*;
import static se.uu.it.smbugfinder.DtlsResources.EARLY_FINISHED;
import static se.uu.it.smbugfinder.DtlsResources.MULT_CCS;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import net.automatalib.word.Word;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.pattern.BugPattern;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;


public class BugPatternLoaderTest {
    public static Logger LOGGER = LoggerFactory.getLogger(BugPatternLoaderTest.class);

    @Test
    public void loadNonParametricBugPatternTest() throws IOException {
        BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());

        // symbols contains the entire alphabet to be used for the expansion of bug-pattern.
        // Normally symbols are taken from the Mealy SUT.
        List<Symbol> symbols = Arrays.asList(I_APPLICATION, O_APPLICATION, I_CHANGE_CIPHER_SPEC, O_CHANGE_CIPHER_SPEC, I_PSK_CLIENT_HELLO, I_PSK_CLIENT_KEY_EXCHANGE, I_FINISHED, O_FINISHED, O_SERVER_HELLO, O_SERVER_HELLO_DONE, O_CERTIFICATE_REQUEST, I_CERTIFICATE, O_HELLO_VERIFY_REQUEST);
        // Here bug patterns are returned expanded with symbols as defined above and not from the Mealy SUT.
        BugPatterns patterns = loader.loadPatterns(DTLS_SERVER_BUG_PATTERNS, symbols);

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
    public void loadDtlsClientParametricBugPatternsTest() {
        List<Symbol> symbols = ImmutableList.of(
                I_PSK_SERVER_HELLO, I_RSA_SERVER_HELLO, O_APPLICATION, I_HELLO_REQUEST, O_CHANGE_CIPHER_SPEC);
        BugPatterns bugPatterns = BugPatternLoader.loadPatternsBasic(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS, symbols);
        BugPattern switchingCS = bugPatterns.getBugPattern("Switching Cipher Suite");
        checkPattern(switchingCS, symbols, 7,
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_PSK_SERVER_HELLO, O_APPLICATION), true),
                new TestCase(Word.fromSymbols(I_RSA_SERVER_HELLO, I_RSA_SERVER_HELLO, O_APPLICATION), false));
    }

    @Test
    public void loadDtlsServerParametricBugPatternsTest() {
        List<Symbol> symbols = ImmutableList.of(
                I_PSK_CLIENT_HELLO, I_RSA_CLIENT_HELLO, O_HELLO_VERIFY_REQUEST, O_SERVER_HELLO);
        BugPatterns bugPatterns = BugPatternLoader.loadPatternsBasic(DTLS_SERVER_PARAMETRIC_BUG_PATTERNS, symbols);
        BugPattern nonConformingCookie = bugPatterns.getBugPattern("Non-conforming Cookie");
        checkPattern(nonConformingCookie, symbols, 8,
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
