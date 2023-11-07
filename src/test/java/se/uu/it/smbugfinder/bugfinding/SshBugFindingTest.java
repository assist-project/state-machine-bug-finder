package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.uu.it.smbugfinder.Main;
import se.uu.it.smbugfinder.StateMachineBugFinderToolConfig;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class SshBugFindingTest {
    public static final String SSH_SERVER_MODEL_FOLDER = "/models/ssh/server/";
    public static final String SSH_SERVER_BUG_PATTERNS = "/patterns/ssh/server/";

    private String sshServerModel(String sut) {
        return SSH_SERVER_MODEL_FOLDER + sut;
    }

    @Test
    public void testDropbearServer() throws FileNotFoundException, IOException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(sshServerModel("Dropbear-v2020.81.dot"));
        assertFoundSpecificBugPatterns(bugs, "Invalid Closure Response", "Missing SR_AUTH");
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
        Assert.assertArrayEquals(expectedBugPatterns, foundBugPatterns);
    }
}
