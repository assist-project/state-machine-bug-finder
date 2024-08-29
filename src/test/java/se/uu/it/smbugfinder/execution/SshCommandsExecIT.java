package se.uu.it.smbugfinder.execution;

import java.io.IOException;

import org.junit.Test;

public class SshCommandsExecIT extends CommandsExec {
  private static final String SSH_SERVER_MODEL_FOLDER = "src/main/resources/models/ssh/server/";
  private static final String SSH_SERVER_BUG_PATTERNS = "src/main/resources/patterns/ssh/server/patterns.xml";
  //private static final String SSH_EMPTY_OUTPUT = "NO_RESP";

  // @Test
  // public void testBitViseServer7_23 () throws IOException, InterruptedException {
  //   Output expectedOutput = new Output();
  //   expectedOutput.addBug(new OutputBug("Multiple UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK KEXINIT KEX30 NEWKEYS UA_PK_OK"));

  //   Output output = Output.runCommand(SSH_SERVER_MODEL_FOLDER + "BitVise-7.23.dot", SSH_SERVER_BUG_PATTERNS);
  //   assertCorrectOutput(expectedOutput, output);
  // }

  // @Test
  // public void testBitViseServer8_49 () throws IOException, InterruptedException {
  //   Output expectedOutput = new Output();
  //   expectedOutput.addBug(new OutputBug("Multiple UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK KEXINIT KEX30 NEWKEYS UA_PK_OK"));

  //   Output output = Output.runCommand(SSH_SERVER_MODEL_FOLDER + "BitVise-8.49.dot", SSH_SERVER_BUG_PATTERNS);
  //   assertCorrectOutput(expectedOutput, output);
  // }










  @Test
  public void testDropbearServer2014 () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Multiple UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK KEXINIT KEX30 NEWKEYS UA_PK_OK"));
    expectedOutput.addBug(new OutputBug("Unignored Authentication Request After UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK KEXINIT KEX30 NEWKEYS UA_PK_OK"));
    expectedOutput.addBug(new OutputBug("Invalid CH_CLOSE Response", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS CH_OPEN/CH_OPEN_SUCCESS CH_CLOSE/CH_EOF", "KEXINIT KEX30 NEWKEYS UA_PK_OK CH_OPEN CH_CLOSE"));
    expectedOutput.addBug(new OutputBug("Channel Open Fail After Rekey", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP CH_OPEN/NO_CONN", "KEXINIT KEX30 NEWKEYS UA_PK_OK KEXINIT KEX30 NEWKEYS CH_OPEN"));
    expectedOutput.addBug(new OutputBug("Missing SR_AUTH", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK"));
    Output output = CommandsExec.runCommand(SSH_SERVER_MODEL_FOLDER + "Dropbear-v2014.65_server.dot", SSH_SERVER_BUG_PATTERNS, true);
    assertCorrectOutput(expectedOutput, output);
  }

  @Test
  public void testDropbearServer2020 () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Invalid CH_CLOSE Response", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS CH_OPEN/CH_OPEN_SUCCESS CH_CLOSE/CH_EOF", "KEXINIT KEX30 NEWKEYS UA_PK_OK CH_OPEN CH_CLOSE"));
    expectedOutput.addBug(new OutputBug("Missing SR_AUTH", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UA_PK_OK/UA_SUCCESS", "KEXINIT KEX30 NEWKEYS UA_PK_OK"));
    Output output = CommandsExec.runCommand(SSH_SERVER_MODEL_FOLDER + "Dropbear-v2020.81_server.dot", SSH_SERVER_BUG_PATTERNS, true);
    assertCorrectOutput(expectedOutput, output);
  }

  @Test
  public void testOpenSSHServer6_9 () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Rekey Fail Before Auth", "LOW", "SR_CONN/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT KEXINIT/UNIMPL", "SR_CONN KEX30 NEWKEYS SR_AUTH KEXINIT"));
    expectedOutput.addBug(new OutputBug("Invalid SR_AUTH Response", "LOW", "SR_CONN/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PW_OK/UA_SUCCESS+GLOBAL_REQUEST SR_AUTH/UNIMPL", "SR_CONN KEX30 NEWKEYS SR_AUTH UA_PW_OK SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Invalid Authentication Rejection Response", "LOW", "SR_CONN/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PW_NOK/UA_FAILURE UA_NONE/DISCONNECT", "SR_CONN KEX30 NEWKEYS SR_AUTH UA_PW_NOK UA_NONE"));
    expectedOutput.addBug(new OutputBug("Unignored Authentication Request After UA_SUCCESS", "MEDIUM", "SR_CONN/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PW_OK/UA_SUCCESS+GLOBAL_REQUEST UA_PK_OK/UNIMPL", "SR_CONN KEX30 NEWKEYS SR_AUTH UA_PW_OK UA_PK_OK"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS UNIMPL/NO_RESP NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 UNIMPL NEWKEYS SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS IGNORE/NO_RESP NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 IGNORE NEWKEYS SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS DEBUG/NO_RESP NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 DEBUG NEWKEYS SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP UNIMPL/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 NEWKEYS UNIMPL SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP IGNORE/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 NEWKEYS IGNORE SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Transport Layer Bug", "UNKNOWN", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP DEBUG/NO_RESP SR_AUTH/SR_ACCEPT", "KEXINIT KEX30 NEWKEYS DEBUG SR_AUTH"));
    Output output = CommandsExec.runCommand(SSH_SERVER_MODEL_FOLDER + "OpenSSH-6.9p1_server.dot", SSH_SERVER_BUG_PATTERNS, true);
    assertCorrectOutput(expectedOutput, output);
  }

  @Test
  public void testOpenSSHServer8_2 () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Rekey Fail Before Auth", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT KEXINIT/UNIMPL", "KEXINIT KEX30 NEWKEYS SR_AUTH KEXINIT"));
    expectedOutput.addBug(new OutputBug("Invalid SR_AUTH Response", "LOW", "KEXINIT/KEXINIT SR_AUTH/UNIMPL", "KEXINIT SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Unignored Authentication Request After UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST UA_PK_OK/UNIMPL", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK UA_PK_OK"));
    expectedOutput.addBug(new OutputBug("Invalid CH_CLOSE Response", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS CH_OPEN/CH_OPEN_SUCCESS CH_CLOSE/NO_RESP SR_CONN/NO_RESP", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK KEXINIT KEX30 CH_OPEN CH_CLOSE SR_CONN"));
    expectedOutput.addBug(new OutputBug("Missing NEWKEYS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS CH_OPEN/CH_OPEN_SUCCESS", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK KEXINIT KEX30 CH_OPEN"));
    Output output = CommandsExec.runCommand(SSH_SERVER_MODEL_FOLDER + "OpenSSH-8.2p1_server.dot", SSH_SERVER_BUG_PATTERNS, true);
    assertCorrectOutput(expectedOutput, output);
  }

  @Test
  public void testOpenSSHServer8_8 () throws IOException, InterruptedException {
    Output expectedOutput = new Output();
    expectedOutput.addBug(new OutputBug("Rekey Fail Before Auth", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT KEXINIT/UNIMPL", "KEXINIT KEX30 NEWKEYS SR_AUTH KEXINIT"));
    expectedOutput.addBug(new OutputBug("Invalid SR_AUTH Response", "LOW", "KEXINIT/KEXINIT SR_AUTH/UNIMPL", "KEXINIT SR_AUTH"));
    expectedOutput.addBug(new OutputBug("Unignored Authentication Request After UA_SUCCESS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST UA_PK_OK/UNIMPL", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK UA_PK_OK"));
    expectedOutput.addBug(new OutputBug("Invalid CH_CLOSE Response", "LOW", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS CH_OPEN/CH_OPEN_SUCCESS CH_CLOSE/NO_RESP SR_CONN/NO_RESP", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK KEXINIT KEX30 CH_OPEN CH_CLOSE SR_CONN"));
    expectedOutput.addBug(new OutputBug("Missing NEWKEYS", "MEDIUM", "KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS NEWKEYS/NO_RESP SR_AUTH/SR_ACCEPT UA_PK_OK/UA_SUCCESS+GLOBAL_REQUEST KEXINIT/KEXINIT KEX30/KEX31+NEWKEYS CH_OPEN/CH_OPEN_SUCCESS", "KEXINIT KEX30 NEWKEYS SR_AUTH UA_PK_OK KEXINIT KEX30 CH_OPEN"));
    Output output = CommandsExec.runCommand(SSH_SERVER_MODEL_FOLDER + "OpenSSH-8.8p1_server.dot", SSH_SERVER_BUG_PATTERNS, true);
    assertCorrectOutput(expectedOutput, output);
  }
}
