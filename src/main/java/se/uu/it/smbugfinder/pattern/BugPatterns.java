package se.uu.it.smbugfinder.pattern;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.bug.BugSeverity;
import se.uu.it.smbugfinder.dfa.DfaAdapter;

@XmlRootElement(name = "bugPatterns")
public class BugPatterns {
	
	@XmlElement(name = "specificationLanguage", required = false)
	private String specificationLanguagePath;
	
	@XmlTransient
	private DfaAdapter specificationLanguage;
	
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
	
	/*
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
	
	/*
	 * Removes bug patterns that have not been enabled
	 */
	private void removeDisabled() {
		List<BugPattern> bugPatterns = new LinkedList<BugPattern>(this.bugPatterns);
		bugPatterns.removeIf(bp -> !bp.isEnabled());
		this.bugPatterns = bugPatterns;
	}
	
	public String getSpecificationLanguagePath() {
		return specificationLanguagePath;
	}

	public DfaAdapter getSpecificationLanguage() {
		return specificationLanguage;
	}
	
	void setSpecificationLanguage(DfaAdapter specificationLanguage) {
		this.specificationLanguage = specificationLanguage;
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
