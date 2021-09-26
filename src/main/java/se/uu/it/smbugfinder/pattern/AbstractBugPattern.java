package se.uu.it.smbugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import se.uu.it.smbugfinder.bug.BugSeverity;
import se.uu.it.smbugfinder.dfa.DfaAdapter;

public abstract class AbstractBugPattern extends Pattern {
	private static int CUR_ID = 1;
	private static AbstractBugPattern UNCATEGORIZED;
	public static AbstractBugPattern uncategorized() {
		if (UNCATEGORIZED == null) {
			AbstractBugPattern bp = new AbstractBugPattern() {
				@Override
				DfaAdapter doGenerateBugLanguage() {
					throw new RuntimeException("Uncategorized bug pattern does not provide a bug language");
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
			UNCATEGORIZED = bp;
		}
		return UNCATEGORIZED;
	}
	
	public AbstractBugPattern() {
		id = CUR_ID ++;
	}
	
	@XmlTransient
	private int id;
	
	@XmlElement(name = "severity", required = false)
	private BugSeverity severity;
	
	@XmlTransient
	private DfaAdapter bugLanguage;
	
	/**
	 * Generates the bug language described by this bug pattern. 
	 * On first call, it stores the generated language to a variable, allowing for subsequent calls to simply return the variable.
	 */
	public DfaAdapter generateBugLanguage() {
		if (bugLanguage == null) {
			bugLanguage = doGenerateBugLanguage();
		}
		return bugLanguage;
	}
	
	abstract DfaAdapter doGenerateBugLanguage();
	
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
