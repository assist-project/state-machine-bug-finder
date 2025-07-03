package se.uu.it.smbugfinder.bugfinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import net.automatalib.exception.FormatException;
import se.uu.it.smbugfinder.BugFinderResult;
import se.uu.it.smbugfinder.StateMachineBugFinder;
import se.uu.it.smbugfinder.StateMachineBugFinderConfig;
import se.uu.it.smbugfinder.bug.StateMachineBug;

public class TcpBugFindingTest extends BugFindingTest {
    private static final String TCP_SERVER_MODELS_FOLDER = "/models/tcp/server/";
    private static final String TCP_SERVER_BUG_PATTERNS = "/patterns/tcp/server/patterns.xml";
    private static final String TCP_CLIENT_MODELS_FOLDER = "/models/tcp/client/";
    private static final String TCP_CLIENT_BUG_PATTERNS = "/patterns/tcp/client/patterns.xml";
    private static final String TCP_EMPTY_OUTPUT = "TIMEOUT";

    private String tcpServerModel(String sut) {
        return TCP_SERVER_MODELS_FOLDER + sut;
    }

    private String tcpClientModel(String sut) {
        return TCP_CLIENT_MODELS_FOLDER + sut;
    }

    @Test
    public void testFreeBSDClient() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderClient(tcpClientModel("freebsd_2016011170941_client.dot"));
        assertFoundSpecificBugPatterns(bugs, "invalid_response_to_syn_segment(time_wait)");
    }


    @Test
    public void testFreeBSDServer() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderServer(tcpServerModel("freebsd_2016011170941_server.dot"));
        assertFoundSpecificBugPatterns(bugs,  "invalid_response_to_syn_segment(time_wait)");
    }

    @Test
    public void testUbuntuClient() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderClient(tcpClientModel("ubuntu_201601281006_client.dot"));
        assertFoundSpecificBugPatterns(bugs, "invalid_response_to_syn_segment(fin_wait)",
        "invalid_response_to_syn_segment(established)", "invalid_response_to_syn_segment(last_ack)",
        "invalid_response_to_syn_segment(close_wait)", "invalid_response_to_syn_segment(time_wait)");
    }

    @Test
    public void testUbuntuServer() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderServer(tcpServerModel("ubuntu_201601281006_server.dot"));
        assertFoundSpecificBugPatterns(bugs, "invalid_response_to_syn_segment(close_wait)",
                "invalid_response_to_syn_segment(established)", "invalid_response_to_syn_segment(fin_wait)",
                "invalid_response_to_syn_segment(last_ack)", "invalid_response_to_syn_segment(time_wait)");
    }

    @Test
    public void testWindowsClient() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderClient(tcpClientModel("windows8_201601271000_client.dot"));
        assertFoundSpecificBugPatterns(bugs, "unexpected_rst", "invalid_response_to_syn_segment(time_wait)",
        "rst_absence_on_close_state");
    }

    @Test
    public void testWindowsServer() throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinderServer(tcpServerModel("windows8_201601271000_server.dot"));
        assertFoundSpecificBugPatterns(bugs, "unexpected_rst", "rst_absence_on_close_state",
        "rst_absence_on_listen_state", "invalid_response_to_syn_segment(time_wait)");
    }

    private List<StateMachineBug<String, String>> runBugFinderServer(String model) throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(model, TCP_SERVER_BUG_PATTERNS, new StateMachineBugFinderConfig());
        return bugs;
    }


    private List<StateMachineBug<String, String>> runBugFinderClient(String model) throws FileNotFoundException, IOException, FormatException {
        List<StateMachineBug<String, String>> bugs = runBugFinder(model, TCP_CLIENT_BUG_PATTERNS, new StateMachineBugFinderConfig());
        return bugs;
    }

    private List<StateMachineBug<String, String>> runBugFinder(String model, String patterns,  StateMachineBugFinderConfig config) throws FileNotFoundException, IOException, FormatException {
        config.setModel(model);
        config.setEmptyOutput(TCP_EMPTY_OUTPUT);
        config.setPatterns(patterns);
        BugFinderResult<String, String> result = new StateMachineBugFinder(config).launch(null);
        return result.getBugs();
    }
}
