package se.uu.it.bugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import se.uu.it.bugfinder.dfa.DfaAdapter;

public class BugPattern extends AbstractBugPattern {

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
		// TODO Auto-generated method stub
		return false;
	}
}
