package se.uu.it.bugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;

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
