package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;

public class EdhocBugFindingTest extends BugFindingTest {
    private static final String EDHOC_CLIENT_MODELS = "/models/edhoc/client/";
    private static final String EDHOC_CLIENT_M3_BUG_PATTERNS = "/patterns/edhoc/client/I_m3/patterns.xml";
    private static final String EDHOC_CLIENT_M4_BUG_PATTERNS = "/patterns/edhoc/client/I_m4/patterns.xml";

    private String edhocModel(String sut) {
        return EDHOC_CLIENT_MODELS + sut;
    }

    @Test
    public void testLakersClient() throws FileNotFoundException, IOException {
        String model = edhocModel("lakers-v0.5.0_client_initiator.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
    }

    @Test
    public void testRise_m4_appClient() throws FileNotFoundException, IOException {
        String model = edhocModel("rise-9bdb756_client_initiator_m4_app.dot");//"rise_m4_app.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
    }

    @Test
    public void testRiseClient() throws FileNotFoundException, IOException {
        String model = edhocModel("rise-f994359_client_initiator_m4_app.dot");//"rise.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Client Alive after sending EDHOC error message");
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Client Alive after sending EDHOC ERR", "Initiator completes EDHOC without M4 or APPo");
    }

    @Test
    public void testSifis_homeClient() throws FileNotFoundException, IOException {
        String model = edhocModel("sifis-home-3d1b68b_phase_1_client_initiator.dot");//"sifis-home_phase_1.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Unsupported sink state");
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Initiator completes EDHOC without M4 or APPo", "Unsupported sink state");
    }

    // @Test
    // public void testUoscore1Client() throws FileNotFoundException, IOException {
    //     String model = edhocModel("uoscore-uedhoc_linux_edhoc_oscore.dot");
    //     BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
    //     assertFoundSpecificBugPatterns(bugs.getBugs());
    //     bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
    //     assertFoundSpecificBugPatterns(bugs.getBugs(), "Initiator completes EDHOC without message 4");
    //}

    @Test
    public void testUoscore2Client() throws FileNotFoundException, IOException {
        String model = edhocModel("uoscore-uedhoc-v3.0.3_linux_edhoc_oscore_client_initiator.dot");//"uoscore-uedhoc_linux_edhoc.dot");
        BugFinderResult<String, String> bugs = runBugFinder(model, EDHOC_CLIENT_M3_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs());
        bugs = runBugFinder(model, EDHOC_CLIENT_M4_BUG_PATTERNS);
        assertFoundSpecificBugPatterns(bugs.getBugs(), "Initiator completes EDHOC without M4 or APPo");
    }

    private BugFinderResult<String, String> runBugFinder(String model, String patterns) throws FileNotFoundException, IOException {
        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
        config.setModel(model);
        config.setPatterns(patterns);
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null); //Main.launchBugFinder(config, null);
        return result;
    }
}
