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
import se.uu.it.smbugfinder.pattern.AbstractBugPattern;

public class DtlsBugFindingTest {
    public static final String DTLS_CLIENT_MODEL = "/models/dtls/client/MbedTLS.dot";
    public static final String DTLS_CLIENT_BUG_PATTERNS = "/patterns/dtls/client/";

    @Test
    public void testDtlsClient() throws FileNotFoundException, IOException {

        StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
        config.setModel(DTLS_CLIENT_MODEL);
        config.setPatterns(DTLS_CLIENT_BUG_PATTERNS);
        List<StateMachineBug<String, String>> bugs = new ArrayList<>();
        Main.launchBugFinder(config, bugs, null);
        Assert.assertEquals(bugs.size(), 1);
        AbstractBugPattern pattern = bugs.get(0).getBugPattern();
        Assert.assertEquals(pattern.getName(), "Premature HelloRequest");
    }
}
