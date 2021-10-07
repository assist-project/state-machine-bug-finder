package se.uu.it.smbugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.dfa.DfaAdapter;

/**
 * A pattern of capturing (some aspect of) conformance to the specification.
 */
public class ConformancePattern extends Pattern {
	@XmlElement(name="conformanceLanguage", required = true)
	private String conformanceLanguagePath;
	
	@XmlTransient
	private DfaAdapter conformanceLanguage;
	
	@XmlElement(name="uncategorizedSequenceBound", required = false)
	private int uncategorizedSequenceBound;
	
	@XmlElement(name="generatedSequenceBound", required = false)
	private int generatedSequenceBound;
	
	public String getConformanceLanguagePath() {
		return conformanceLanguagePath;
	}

	public DfaAdapter getConformanceLanguage() {
		return conformanceLanguage;
	}

	void setConformanceLanguage(DfaAdapter conformanceLanguage) {
		this.conformanceLanguage = conformanceLanguage;
	}
	
	public int getUncategorizedSequenceBound() {
		return uncategorizedSequenceBound;
	}

	public int getGeneratedSequenceBound() {
		return generatedSequenceBound;
	}
}