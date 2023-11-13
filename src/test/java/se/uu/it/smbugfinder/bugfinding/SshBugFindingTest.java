package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.Main;
import se.uu.it.smbugfinder.StateMachineBugFinderToolConfig;
import se.uu.it.smbugfinder.bug.StateMachineBug;

/**
 * Tests that use the bug finder to reproduce the results for SSH servers reported in the NDSS 2023 paper:
 *    Automata-Based Automated Detection of StateMachine Bugs in Protocol Implementations
 * @see <a href="https://www.ndss-symposium.org/wp-content/uploads/2023/02/ndss2023_s68_paper.pdf">PDF of paper</a>
 */
public class SshBugFindingTest extends BugFindingTest {
    private static final String SSH_SERVER_MODEL_FOLDER = "/models/ssh/server/";
    private static final String SSH_SERVER_BUG_PATTERNS = "/patterns/ssh/server/";
    private static final String SSH_EMPTY_OUTPUT = "NO_RESP";

    private String sshServerModel(String sut) {
        return SSH_SERVER_MODEL_FOLDER + sut;
    }

    @Test
    public void testBitViseServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("BitVise-8.49.dot"));
        // Unresponsive State was not reported in the paper (it was reported in Patrick's MSc thesis on fuzzing SSH servers)
        assertFoundSpecificBugPatterns(bugs, "Rekey Fail After Auth", "Invalid SR_AUTH Response",
                "Unignored Authentication Request After UA_SUCCESS", "Missing NEWKEYS", "Unresponsive State");
    }

    @Test
    public void testDropbearServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("Dropbear-v2020.81.dot"));
        assertFoundSpecificBugPatterns(bugs, "Invalid CH_CLOSE Response", "Missing SR_AUTH");
    }

    @Test
    public void testOpenSSHServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("OpenSSH-8.8p1.dot"));
        assertFoundSpecificBugPatterns(bugs, "Rekey Fail Before Auth", "Invalid SR_AUTH Response",
                "Unignored Authentication Request After UA_SUCCESS", "Invalid CH_CLOSE Response", "Missing NEWKEYS");
    }

    @Test
    public void testValidationPass() throws FileNotFoundException, IOException {
        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setValidationModel(sshServerModel("Dropbear-v2020.81.dot"));
        config.getSmBugFinderConfig().setValidate(true);
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("Dropbear-v2020.81.dot"), config);
        assertValidationSuccess(bugs);
    }

    @Test
    public void testValidationFail() throws FileNotFoundException, IOException {
        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setValidationModel(sshServerModel("Dropbear-v2020.81.dot"));
        config.getSmBugFinderConfig().setValidate(true);
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("BitVise-8.49.dot"), config);
        assertValidationFail(bugs);
    }

    private List<StateMachineBug<String, String>> runBugFinder(String model) throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(model, new StateMachineBugFinderToolConfig());
        return bugs;
    }

    private List<StateMachineBug<String, String>> runBugFinder(String model, StateMachineBugFinderToolConfig config) throws FileNotFoundException, IOException {
        config.setModel(model);
        config.setPatterns(SSH_SERVER_BUG_PATTERNS);
        config.setEmptyOutput(SSH_EMPTY_OUTPUT);
        BugFinderResult<String, String> result = Main.launchBugFinder(config, null);
        return result.getBugs();
    }
}
