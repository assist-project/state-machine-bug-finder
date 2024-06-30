package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;

public class DtlsBugFindingTest extends BugFindingTest {
    public static final String DTLS_CLIENT_MODEL = "/models/dtls/client/MbedTLS-2.26.0_client_psk_reneg.dot";
    public static final String DTLS_CLIENT_BUG_PATTERNS = "/patterns/dtls/client/patterns.xml";
    public static final String DTLS_SERVER_MODEL = "/models/dtls/server/MbedTLS-2.26.0_server_all_cert_req.dot";
    public static final String DTLS_SERVER_BUG_PATTERNS = "/patterns/dtls/server/patterns.xml";

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
