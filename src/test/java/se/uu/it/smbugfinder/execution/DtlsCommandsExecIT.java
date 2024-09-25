package se.uu.it.smbugfinder.execution;

import java.io.IOException;

import org.junit.Test;


public class DtlsCommandsExecIT extends CommandsExec {
  private static final String DTLS_SERVER_MODEL = "src/main/resources/models/dtls/server/MbedTLS-2.26.0_server_all_cert_req.dot";
  private static final String DTLS_SERVER_BUG_PATTERNS = "src/main/resources/patterns/dtls/server/patterns.xml";
  private static final String DTLS_CLIENT_MODEL = "src/main/resources/models/dtls/client/MbedTLS-2.26.0_client_psk_reneg.dot";
  private static final String DTLS_CLIENT_BUG_PATTERNS = "src/main/resources/patterns/dtls/client/patterns.xml";

  @Test
  public void testDtlsServer () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Non-conforming Cookie", "LOW", "PSK_CLIENT_HELLO/HELLO_VERIFY_REQUEST RSA_CLIENT_HELLO/SERVER_HELLO", "PSK_CLIENT_HELLO RSA_CLIENT_HELLO"));
    Output output = CommandsExec.runCommand(DTLS_SERVER_MODEL, DTLS_SERVER_BUG_PATTERNS, false, "|");
    assertCorrectOutput(expectedOutput, output);
  }

  @Test
  public void testDtlsClient () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Premature HelloRequest", "LOW", "HELLO_REQUEST/CLIENT_HELLO", "HELLO_REQUEST"));
    Output output = CommandsExec.runCommand(DTLS_CLIENT_MODEL, DTLS_CLIENT_BUG_PATTERNS, false, "|");
    assertCorrectOutput(expectedOutput, output);
  }
}
