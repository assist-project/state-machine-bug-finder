package se.uu.it.smbugfinder.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.bug.BugSeverity;
import se.uu.it.smbugfinder.dfa.DFAAdapter;

@XmlRootElement(name = "bugPatterns")
public class BugPatterns {

    @XmlElement(name = "specificationLanguage", required = false)
    private String specificationLanguagePath;

    @XmlTransient
    private DFAAdapter specificationLanguage;

    @XmlElement(name = "patternLanguage", required = false)
    private String patternLanguagePath;

    @XmlElements(value = {
            @XmlElement(type = BugPattern.class, name = "bugPattern"),
            @XmlElement(type = GeneralBugPattern.class, name = "generalBugPattern")
            })
    private List<BugPattern> bugPatterns;

    @XmlElement(name = "defaultBugSeverity")
    private BugSeverity defaultBugSeverity;

    @XmlElement(name = "defaultEnabled")
    private boolean defaultEnabled;

    BugPatterns() {
        bugPatterns = new ArrayList<>();
        defaultBugSeverity = BugSeverity.UNKNOWN;
        defaultEnabled = true;
    }


    /**
     * Initializes bug patterns according to the default values of this container and removes disabled bug patterns.
     * Should be called before bug patterns are accessed.
     */
    void prepare() {
        updateDefaults();
        removeDisabled();
    }


    /**
     * Adjusts the contained bug patterns according to the default values of this container.
     */
    private void updateDefaults() {
        for (BugPattern bugPattern : bugPatterns) {
            if (bugPattern.getEnabled() == null) {
                bugPattern.setEnabled(defaultEnabled);
            }
            if (bugPattern.getSeverity() == null) {
                bugPattern.setSeverity(defaultBugSeverity);
            }
        }
    }


    /**
     * Removes bug patterns that have not been enabled
     */
    private void removeDisabled() {
        List<BugPattern> bugPatterns = new ArrayList<BugPattern>(this.bugPatterns);
        bugPatterns.removeIf(bp -> !bp.isEnabled());
        this.bugPatterns = bugPatterns;
    }

    /**
     * Removes bug patterns for which the selector returns false.
     */
    public void applySelector(Predicate<BugPattern> selector) {
        List<BugPattern> bugPatterns = new ArrayList<BugPattern>(this.bugPatterns);
        bugPatterns.removeIf(bp -> !selector.test(bp));
        this.bugPatterns = bugPatterns;
    }

    /**
     * Returns the bug pattern with a matching name, or null if no such bug pattern could be found.
     */
    public BugPattern getBugPattern(String name) {
        return bugPatterns.stream().filter(bp -> bp.getName().equals(name)).findAny().orElse(null);
    }

    public String getSpecificationLanguagePath() {
        return specificationLanguagePath;
    }

    public DFAAdapter getSpecificationLanguage() {
        return specificationLanguage;
    }

    void setSpecificationLanguage(DFAAdapter specificationLanguage) {
        this.specificationLanguage = specificationLanguage;
    }

    public String getPatternLanguagePath() {
        return this.patternLanguagePath;
    }
    public void setPatternLanguage(String patternLanguagePath) {
        this.patternLanguagePath = patternLanguagePath;
    }

    public List<BugPattern> getBugPatterns() {
        return bugPatterns;
    }

    public List<BugPattern> getSpecificBugPatterns() {
        return bugPatterns.stream().filter(bp -> !bp.isGeneral()).collect(Collectors.toList());
    }

    public List<GeneralBugPattern> getGeneralBugPatterns() {
        return bugPatterns.stream().filter(bp -> bp.isGeneral()).map(bp -> (GeneralBugPattern) bp).collect(Collectors.toList());
    }

    public List<BugPattern> getBugPatterns(Predicate<BugPattern> filter) {
        return bugPatterns.stream().filter(filter).collect(Collectors.toList());
    }

    public BugSeverity getDefaultBugSeverity() {
        return defaultBugSeverity;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
