package se.uu.it.smbugfinder.pattern;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.dfa.DFAAdapter;

/**
 * Encodes a protocol violation as a DFA.
 */
public class BugPattern extends AbstractBugPattern {
    BugPattern() {
        super();
    }

    @XmlElement(name="bugLanguage", required = true)
    private String bugLanguagePath;

    @XmlTransient
    private DFAAdapter bugLanguage;

    @Override
    DFAAdapter doGenerateBugLanguage() {
        return bugLanguage;
    }

    public String getBugLanguagePath() {
        return bugLanguagePath;
    }

    void setBugLanguage(DFAAdapter bugLanguage) {
        this.bugLanguage = bugLanguage;
    }

    DFAAdapter getBugLanguage() {
        return bugLanguage;
    }

    @Override
    public boolean isGeneral() {
        return false;
    }

    @Override
    public String toString() {
        return "BugPattern [name=" + name + "]";
    }
}
