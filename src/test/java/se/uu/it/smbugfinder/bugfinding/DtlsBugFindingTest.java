package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import se.uu.it.smbugfinder.Main;
import se.uu.it.smbugfinder.StateMachineBugFinderToolConfig;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class DtlsBugFindingTest extends BugFindingTest {
    public static final String DTLS_CLIENT_MODEL = "/models/dtls/client/MbedTLS-2.26.0_client_psk_reneg.dot";
    public static final String DTLS_CLIENT_BUG_PATTERNS = "/patterns/dtls/client/";
    public static final String DTLS_SERVER_MODEL = "/models/dtls/server/MbedTLS-2.26.0_server_all_cert_req.dot";
    public static final String DTLS_SERVER_BUG_PATTERNS = "/patterns/dtls/server/";

    @Test
    public void testDtlsClient() throws FileNotFoundException, IOException {
        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setModel(DTLS_CLIENT_MODEL);
        config.setPatterns(DTLS_CLIENT_BUG_PATTERNS);
        config.setSeparator("|");
        List<StateMachineBug<String, String>> bugs = new ArrayList<>();
        Main.launchBugFinder(config, bugs, null);
        assertFoundSpecificBugPatterns(bugs, "Premature HelloRequest");
    }

    @Test
    public void testDtlsServer()  throws FileNotFoundException, IOException {
        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setModel(DTLS_SERVER_MODEL);
        config.setPatterns(DTLS_SERVER_BUG_PATTERNS);
        config.setSeparator("|");
        List<StateMachineBug<String, String>> bugs = new ArrayList<>();
        Main.launchBugFinder(config, bugs, null);
        assertFoundSpecificBugPatterns(bugs, "Non-conforming Cookie");
    }
}