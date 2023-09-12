package se.uu.it.smbugfinder.pattern;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.bug.BugSeverity;
import se.uu.it.smbugfinder.dfa.DFAAdapter;

/**
 * Abstract class for bug patterns encoded as DFAs.
 * Like a pattern, a bug pattern has a name and a description.
 * In addition, it is equipped with a unique index and a severity level.
 */
public abstract class AbstractBugPattern extends Pattern {
    private static int CUR_ID = 1;
    private static AbstractBugPattern UNCATEGORIZED;

    public static AbstractBugPattern uncategorized() {
        if (AbstractBugPattern.UNCATEGORIZED == null) {
            AbstractBugPattern bp = new AbstractBugPattern() {
                @Override
                DFAAdapter doGenerateBugLanguage() {
                    throw new RuntimeException("Uncategorized bug pattern does not provide a bug language.");
                }

                @Override
                public boolean isGeneral() {
                    return true;
                }

            };
            bp.id = 0;
            bp.name = "Uncategorized";
            bp.description = "Uncategorized behavior which does not conform to the specification.";
            bp.severity = BugSeverity.UNKNOWN;
            AbstractBugPattern.UNCATEGORIZED = bp;
        }
        return AbstractBugPattern.UNCATEGORIZED;
    }

    public AbstractBugPattern() {
        id = CUR_ID ++;
    }

    @XmlTransient
    private int id;

    @XmlElement(name = "severity", required = false)
    private BugSeverity severity;

    @XmlTransient
    private DFAAdapter bugLanguage;

    /**
     * Generates the bug language described by this bug pattern.
     * On first call, it stores the generated language to a variable, allowing for subsequent calls to simply return the variable.
     */
    public DFAAdapter generateBugLanguage() {
        if (bugLanguage == null) {
            bugLanguage = doGenerateBugLanguage();
        }
        return bugLanguage;
    }

    abstract DFAAdapter doGenerateBugLanguage();

    public Integer getId() {
        return id;
    }

    public BugSeverity getSeverity() {
        return severity;
    }

    void setSeverity(BugSeverity severity) {
        this.severity = severity;
    }

    public abstract boolean isGeneral();
}
