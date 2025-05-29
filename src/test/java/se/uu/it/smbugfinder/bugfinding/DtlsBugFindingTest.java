package se.uu.it.smbugfinder.bugfinding;

import static se.uu.it.smbugfinder.DtlsResources.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import net.automatalib.exception.FormatException;
import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;
import se.uu.it.smbugfinder.pattern.BugPattern;

public class DtlsBugFindingTest extends BugFindingTest {

    @Test
    public void testDtlsClient() throws FileNotFoundException, IOException, FormatException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(DTLS_CLIENT_MODEL);
        config.setPatterns(DTLS_CLIENT_BUG_PATTERNS);
        config.setSeparator("|");
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        assertFoundSpecificBugPatterns(result.getBugs(), "Premature HelloRequest");

        // Check if bug patterns are expanded correctly
        BugPattern earlyFinished = result.getBugPatterns().getBugPattern(EARLY_FINISHED);
        Assert.assertEquals(4, earlyFinished.generateBugLanguage().getDfa().size());
        BugPattern close_alert = result.getBugPatterns().getBugPattern(CONTINUE_AFTER_CLOSE);
        Assert.assertEquals(4, close_alert.generateBugLanguage().getDfa().size());
        BugPattern flight_restart = result.getBugPatterns().getBugPattern(SH_FLIGHT_RESTART);
        Assert.assertEquals(5, flight_restart.generateBugLanguage().getDfa().size());
    }

    @Test
    public void testParametricDtlsClient() throws IOException, FormatException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS);
        config.setPatterns(DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS);
        config.setSeparator("|");
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        assertFoundSpecificBugPatterns(result.getBugs(), "Switching Cipher Suite", "Wrong Certificate Type");
    }

    @Test
    public void testDtlsServer() throws FileNotFoundException, IOException, FormatException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(DTLS_SERVER_MODEL);
        config.setPatterns(DTLS_SERVER_BUG_PATTERNS);
        config.setSeparator("|");
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        assertFoundSpecificBugPatterns(result.getBugs(), "Non-conforming Cookie");

        // Check if bug patterns are expanded correctly
        BugPattern earlyFinished = result.getBugPatterns().getBugPattern(EARLY_FINISHED);
        Assert.assertEquals(3, earlyFinished.generateBugLanguage().getDfa().size());
        BugPattern fatalError = result.getBugPatterns().getBugPattern(FATAL_ERROR);
        Assert.assertEquals(3, fatalError.generateBugLanguage().getDfa().size());
        BugPattern invalid_fin = result.getBugPatterns().getBugPattern(INVALID_FIN_AS_RETRANSMISSION);
        Assert.assertEquals(6, invalid_fin.generateBugLanguage().getDfa().size());
        BugPattern ccs_before_certver = result.getBugPatterns().getBugPattern(CCS_BEFORE_CERTVER);
        Assert.assertEquals(5, ccs_before_certver.generateBugLanguage().getDfa().size());
        BugPattern certificateless_auth = result.getBugPatterns().getBugPattern(CERTLESS_AUTH);
        Assert.assertEquals(4, certificateless_auth.generateBugLanguage().getDfa().size());
        BugPattern cke_after_certver = result.getBugPatterns().getBugPattern(CKE_AFTER_CERTVER);
        Assert.assertEquals(5, cke_after_certver.generateBugLanguage().getDfa().size());
    }
}
