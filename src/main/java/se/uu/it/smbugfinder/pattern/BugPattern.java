package se.uu.it.smbugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.dfa.DfaAdapter;

public class BugPattern extends AbstractBugPattern {
	BugPattern() {
		super();
	}
	
	

	@XmlElement(name="bugLanguage", required = true)
	private String bugLanguagePath;
	
	@XmlTransient
	private DfaAdapter bugLanguage;
	
	@Override
	DfaAdapter doGenerateBugLanguage() {
		return bugLanguage;
	}
	
	public String getBugLanguagePath() {
		return bugLanguagePath;
	}
	
	void setBugLanguage(DfaAdapter bugLanguage) {
		this.bugLanguage = bugLanguage;
	}

	DfaAdapter getBugLanguage() {
		return bugLanguage;
	}
	
	public boolean isGeneral() {
		return false;
	}
	
	@Override
	public String toString() {
		return "BugPattern [name=" + name + "]";
	}
}
