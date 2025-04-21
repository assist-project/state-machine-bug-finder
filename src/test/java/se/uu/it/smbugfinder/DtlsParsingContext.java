package se.uu.it.smbugfinder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.NotImplementedException;

import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.Constants;
import se.uu.it.smbugfinder.encoding.Field;
import se.uu.it.smbugfinder.encoding.Fields;
import se.uu.it.smbugfinder.encoding.Function;
import se.uu.it.smbugfinder.encoding.Functions;
import se.uu.it.smbugfinder.encoding.ParsingContext;
import se.uu.it.smbugfinder.encoding.Value;

public class DtlsParsingContext extends ParsingContext {
    private static Pattern SERVER_HELLO_PATTERN = Pattern.compile("(?<cipherSuite>[A-Za-z_]*)_SERVER_HELLO");
    private static Pattern CERTIFICATE_REQUEST_PATTERN = Pattern.compile("(?<certType>[A-Za-z_]*)_CERTIFICATE_REQUEST");
    private static Pattern CERTIFICATE_PATTERN = Pattern.compile("(?<keyType>[A-Za-z_]*)_CERTIFICATE");
    private static Pattern CLIENT_HELLO_PATTERN = Pattern.compile("(?<cipherSuite>[A-Za-z_]*)_CLIENT_HELLO");

    public DtlsParsingContext() {
        super(new DtlsConstants(), new DtlsFields(), new DtlsFunctions());
    }

    static class DtlsConstants extends Constants {
        private static final long serialVersionUID = 1L;
        public DtlsConstants() {
            super();
        }
    }

    static class DtlsFields extends Fields {
        private static final long serialVersionUID = 1L;
        public DtlsFields() {
            super();
            initialize(new CertificateTypes(), new CipherSuite(), new CipherSuites(), new KeyType());
        }
    }

    static class DtlsFunctions extends Functions {
        private static final long serialVersionUID = 1L;
        public DtlsFunctions() {
            super();
            initialize(new FunCertType());
        }
    }

    static class CertificateTypes extends Field {
        public CertificateTypes () {
            super("cert_type");
        }

        @Override
        protected Value getValue(Symbol symbol) {
            Matcher matcher = CERTIFICATE_REQUEST_PATTERN.matcher(symbol.name());
            if (matcher.matches()) {
                String certType = matcher.group("certType");
                Set<String> certTypes = new LinkedHashSet<>(); //always create a set with one object (currently there are no patterns that need more than that, but generally this is wrong)
                certTypes.add(certType);
                return new Value(certTypes);
            }
            return undefined(symbol);
        }
    }

    static class CipherSuite extends Field {
        public CipherSuite() {
            super("cipher_suite");
        }

        @Override
        protected Value getValue(Symbol symbol) {
            Matcher matcher = SERVER_HELLO_PATTERN.matcher(symbol.name());
            if (matcher.matches()) {
                String cipherSuite = matcher.group("cipherSuite");
                return new Value(cipherSuite);
            }
            return undefined(symbol);
        }
    }

    static class CipherSuites extends Field {
        public CipherSuites () {
            super("cipher_suites");
        }

        @Override
        protected Value getValue(Symbol symbol) {
            Matcher matcher = CLIENT_HELLO_PATTERN.matcher(symbol.name());
            if (matcher.matches()) {
                String suite = matcher.group("cipherSuite");
                Set<String> suites = new LinkedHashSet<>();
                suites.add(suite);
                return new Value(suites);
            }
            return undefined(symbol);
        }
    }

    static class KeyType extends Field {
        public KeyType () {
            super("key_type");
        }

        @Override
        protected Value getValue(Symbol symbol) {
            Matcher matcher = CERTIFICATE_PATTERN.matcher(symbol.name());
            if (matcher.matches()) {
                String cipherSuite = matcher.group("keyType");
                return new Value(cipherSuite);
            }
            return undefined(symbol);
        }
    }

    static class FunCertType extends Function {
        public FunCertType() {
            super("cert_type", 1);
        }

        @Override
        protected Value doInvoke(Symbol symbol, Value... arguments) {
            String keyType = arguments[0].getStoredValue().toString();
            Set<String> certTypes = new LinkedHashSet<String>();

            switch(keyType) {
                case "RSA" -> certTypes.add("RSA_SIGN");
                case "ECDSA" -> certTypes.add("ECDSA_SIGN");
                default -> {
                    throw new NotImplementedException("Unsupported key type " + keyType);
                }
            };

            return new Value(certTypes);
        }
    }
}
