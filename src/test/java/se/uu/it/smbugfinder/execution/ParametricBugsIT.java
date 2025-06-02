package se.uu.it.smbugfinder.execution;


import static  se.uu.it.smbugfinder.DtlsResources.*;

import java.io.IOException;

import org.junit.Test;

import net.automatalib.exception.FormatException;

public class ParametricBugsIT extends CommandsExec {

    @Test
    public void testParamPatternsDtlsServer() throws IOException, FormatException, InterruptedException {
        Output expectedOutput = new Output();
        expectedOutput.addBug(new OutputBug("Non-conforming Cookie", "LOW",
                "PSK_CLIENT_HELLO/HELLO_VERIFY_REQUEST RSA_CLIENT_HELLO/SERVER_HELLO",
                "PSK_CLIENT_HELLO RSA_CLIENT_HELLO"));
        Output output = CommandsExec.runCommand(DTLS_SERVER_MODEL, DTLS_SERVER_PARAMETRIC_BUG_PATTERNS, "|");
        assertCorrectOutput(expectedOutput, output);
    }
}
