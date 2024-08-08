package se.uu.it.smbugfinder.bugfinding;

import static se.uu.it.smbugfinder.DtlsResources.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;

public class DtlsBugFindingTest extends BugFindingTest {

    @Test
    public void testDtlsClient() throws FileNotFoundException, IOException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(DTLS_CLIENT_MODEL);
        config.setPatterns(DTLS_CLIENT_BUG_PATTERNS);
        config.setSeparator("|");
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        assertFoundSpecificBugPatterns(result.getBugs(), "Premature HelloRequest");
    }

    @Test
    public void testDtlsServer()  throws FileNotFoundException, IOException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(DTLS_SERVER_MODEL);
        config.setPatterns(DTLS_SERVER_BUG_PATTERNS);
        config.setSeparator("|");
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        assertFoundSpecificBugPatterns(result.getBugs(), "Non-conforming Cookie");
    }
}
