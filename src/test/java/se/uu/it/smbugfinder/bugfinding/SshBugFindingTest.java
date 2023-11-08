package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.uu.it.smbugfinder.Main;
import se.uu.it.smbugfinder.StateMachineBugFinderToolConfig;
import se.uu.it.smbugfinder.bug.StateMachineBug;

/**
 * Tests involve using the bug finder to reproduce the results for SSH servers reported in the bug detection publication (accepted at NDSS 2023).
 * @see <a href="https://www.ndss-symposium.org/wp-content/uploads/2023/02/ndss2023_s68_paper.pdf">Bug detection publication</a>
 */
public class SshBugFindingTest {
    private static final String SSH_SERVER_MODEL_FOLDER = "/models/ssh/server/";
    private static final String SSH_SERVER_BUG_PATTERNS = "/patterns/ssh/server/";

    private String sshServerModel(String sut) {
        return SSH_SERVER_MODEL_FOLDER + sut;
    }

    @Test
    public void testBitViseServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("BitVise-8.49.dot"));
        // Unresponsive State was not reported in the paper (it was reported in Patrick's MSc thesis on fuzzing SSH servers)
        assertFoundSpecificBugPatterns(bugs, "Rekey Failure Post-Authentication", "Invalid SR_AUTH Response",
                "Unignored Authentication Request", "Missing NEWKEYS", "Unresponsive State");
    }

    @Test
    public void testDropbearServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("Dropbear-v2020.81.dot"));
        assertFoundSpecificBugPatterns(bugs, "Invalid Closure Response", "Missing SR_AUTH");
    }

    @Test
    public void testOpenSSHServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("OpenSSH-8.8p1.dot"));
        assertFoundSpecificBugPatterns(bugs, "Rekey Failure Pre-Authentication", "Invalid SR_AUTH Response",
                "Unignored Authentication Request", "Invalid Closure Response", "Missing NEWKEYS");
    }


    private List<StateMachineBug<String, String>> runBugFinder(String model) throws FileNotFoundException, IOException {
        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setModel(model);
        config.setPatterns(SSH_SERVER_BUG_PATTERNS);
        List<StateMachineBug<String, String>> bugs = new ArrayList<>();
        Main.launchBugFinder(config, bugs, null);
        return bugs;
    }

    private void assertFoundSpecificBugPatterns(List<StateMachineBug<String,String>> bugs, String ... expectedBugPatterns) {
        String [] foundBugPatterns = bugs.stream()
                .filter(b -> !b.getBugPattern().isGeneral())
                .map(b -> b.getBugPattern().getName())
                .toArray(String []::new);
        Assert.assertArrayEquals( "Bug patterns detected different:\\n   expected: %s \\n   found: %s"
                .formatted(Arrays.toString(expectedBugPatterns), Arrays.toString(foundBugPatterns)),
                expectedBugPatterns, foundBugPatterns);
    }
}
