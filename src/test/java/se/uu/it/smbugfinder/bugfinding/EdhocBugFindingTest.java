package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;

public class EdhocBugFindingTest extends BugFindingTest {
    private static final String EDHOC_CLIENT_MODELS = "/models/edhoc/client/";
    private static final String EDHOC_SERVER_MODELS = "/models/edhoc/server/";
    private static final String EDHOC_CLIENT_M3_BUG_PATTERNS = "/patterns/edhoc/client/I_m3/patterns.xml";
    private static final String EDHOC_CLIENT_M4_BUG_PATTERNS = "/patterns/edhoc/client/I_m4/patterns.xml";
    private static final String EDHOC_SERVER_M4_BUG_PATTERNS = "/patterns/edhoc/server/R_m4/patterns.xml";

    private String edhocClientModel(String sut) {
        return EDHOC_CLIENT_MODELS + sut;
    }

    private String edhocServerModel(String sut) {
        return EDHOC_SERVER_MODELS + sut;
    }

    private BugFinderResult<String, String> runBugFinder(String model, String patterns) throws FileNotFoundException, IOException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(model);
        config.setPatterns(patterns);
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        return result;
    }


    /* CLIENTS */

    @Test
    public void testLakersClient() throws FileNotFoundException, IOException {
        String model = edhocClientModel("lakers-v0.5.0_client_initiator.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
    }

    @Test
    public void testRise_9bdb756_client() throws FileNotFoundException, IOException {
        String model = edhocClientModel("rise-9bdb756_client_initiator_m4_app.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
    }

    @Test
    public void testRise_f994359_client() throws FileNotFoundException, IOException {
        String model = edhocClientModel("rise-f994359_client_initiator_m4_app.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Client Alive after sending EDHOC ERR", "Initiator completes EDHOC without M4 or APPo");
    }

    @Test
    public void testSifisClient() throws FileNotFoundException, IOException {
        String model = edhocClientModel("sifis-home-3d1b68b_phase_1_client_initiator.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Unsupported sink state");
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Initiator completes EDHOC without M4 or APPo", "Unsupported sink state");
    }

    @Test
    public void testUoscoreClient() throws FileNotFoundException, IOException {
        String model = edhocClientModel("uoscore-uedhoc-v3.0.3_linux_edhoc_oscore_client_initiator.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Initiator completes EDHOC without M4 or APPo");
    }


    /* SERVERS */

    @Test
    public void testLakersServer() throws FileNotFoundException, IOException {
        String model = edhocServerModel("lakers-v0.5.0_server_responder.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_SERVER_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
    }

    @Test
    public void testRise_9bdb756_server() throws FileNotFoundException, IOException {
        String model = edhocServerModel("rise-9bdb756_server_responder_m4_app.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_SERVER_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Server Respond to CoAP app messages", "EDHOC Error after exchange deletes derived OSCORE context");
    }

    @Test
    public void testSifisServer() throws FileNotFoundException, IOException {
        String model = edhocServerModel("sifis-home-3d1b68b_phase_1_server_responder.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_SERVER_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "EDHOC Error after exchange deletes derived OSCORE context");
    }

    @Test
    public void testUoscoreServer() throws FileNotFoundException, IOException {
        String model = edhocServerModel("uoscore-uedhoc-v3.0.3_linux_edhoc_oscore_server_responder.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_SERVER_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Server Respond to CoAP app messages");
    }

}
