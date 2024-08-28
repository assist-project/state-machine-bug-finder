package se.uu.it.smbugfinder;

import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;

public final class DtlsResources {
    public static final String DTLS_CLIENT_MODEL = "/models/dtls/client/MbedTLS-2.26.0_client_psk_reneg.dot";
    public static final String DTLS_CLIENT_BUG_PATTERNS = "/patterns/dtls/client/patterns.xml";
    public static final String DTLS_CLIENT_PARAMETRIC_BUG_PATTERNS = "/patterns/dtls/client/parametric/patterns.xml";
    public static final String DTLS_SERVER_MODEL = "/models/dtls/server/MbedTLS-2.26.0_server_all_cert_req.dot";
    public static final String DTLS_SERVER_BUG_PATTERNS = "/patterns/dtls/server/patterns.xml";

    public static final String DTLS_BUG_PATTERN_EARLY_FINISHED = "Early Finished";

    public static class DtlsJointAlphabet {
        public static final InputSymbol I_APPLICATION = new InputSymbol("APPLICATION");
        public static final InputSymbol I_CHANGE_CIPHER_SPEC = new InputSymbol("CHANGE_CIPHER_SPEC");
        public static final InputSymbol I_FINISHED = new InputSymbol("FINISHED");

        public static final OutputSymbol O_APPLICATION = new OutputSymbol("APPLICATION");
        public static final OutputSymbol O_ECDSA_CERTIFICATE = new OutputSymbol("ECDSA_CERTIFICATE");
        public static final OutputSymbol O_RSA_CERTIFICATE = new OutputSymbol("RSA_CERTIFICATE");
        public static final OutputSymbol O_CHANGE_CIPHER_SPEC = new OutputSymbol("CHANGE_CIPHER_SPEC");
        public static final OutputSymbol O_FINISHED = new OutputSymbol("FINISHED");
        public static final OutputSymbol O_TIMEOUT = new OutputSymbol("TIMEOUT");
    }

    public static class DtlsClientAlphabet extends DtlsJointAlphabet {
        public static final InputSymbol I_RSA_SIGN_CERTIFICATE_REQUEST = new InputSymbol("RSA_SIGN_CERTIFICATE_REQUEST");
        public static final InputSymbol I_ECDSA_SIGN_CERTIFICATE_REQUEST = new InputSymbol("ECDSA_SIGN_CERTIFICATE_REQUEST");
        public static final InputSymbol I_HELLO_REQUEST = new InputSymbol("HELLO_REQUEST");
        public static final InputSymbol I_PSK_SERVER_HELLO = new InputSymbol("PSK_SERVER_HELLO");
        public static final InputSymbol I_RSA_SERVER_HELLO = new InputSymbol("RSA_SERVER_HELLO");

    }

    public static class DtlsServerAlphabet extends DtlsJointAlphabet {
        public static final InputSymbol I_PSK_CLIENT_HELLO = new InputSymbol("PSK_CLIENT_HELLO");
        public static final InputSymbol I_RSA_CLIENT_HELLO = new InputSymbol("RSA_CLIENT_HELLO");
        public static final InputSymbol I_PSK_CLIENT_KEY_EXCHANGE = new InputSymbol("PSK_CLIENT_KEY_EXCHANGE");
        public static final InputSymbol I_RSA_CLIENT_KEY_EXCHANGE = new InputSymbol("RSA_CLIENT_KEY_EXCHANGE");

        public static final OutputSymbol O_CERTIFICATE_REQUEST = new OutputSymbol("CERTIFICATE_REQUEST");
        public static final OutputSymbol O_CERTIFICATE_VERIFY = new OutputSymbol("CERTIFICATE_VERIFY");
        public static final OutputSymbol O_SERVER_HELLO = new OutputSymbol("SERVER_HELLO");
        public static final OutputSymbol O_SERVER_HELLO_DONE = new OutputSymbol("SERVER_HELLO_DONE");
    }
}