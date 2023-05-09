package se.uu.it.smbugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;

/**
 * Captures general bugs, used to unearth a wide range of violations.
 */
public class GeneralBugPattern extends BugPattern {

    @XmlElement(name="uncategorizedSequenceBound", required = false)
    private int uncategorizedSequenceBound;
    @XmlElement(name="generatedSequenceBound", required = false)
    private int generatedSequenceBound;

    public GeneralBugPattern() {
        uncategorizedSequenceBound = 100;
        generatedSequenceBound = 10000;
    }

    public int generatedSequenceBound() {
        return generatedSequenceBound;
    }

    public int uncategorizedSequenceBound() {
        return uncategorizedSequenceBound;
    }

    public boolean isGeneral() {
        return true;
    }

}
