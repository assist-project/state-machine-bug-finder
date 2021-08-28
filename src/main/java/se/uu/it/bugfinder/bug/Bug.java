package se.uu.it.bugfinder.bug;

import se.uu.it.bugfinder.dfa.SymbolTrace;

public abstract class Bug {
	private static int BUG_COUNT;
	
	private static Integer getFreshBugId() {
		return ++BUG_COUNT;
	}
	
	private SymbolTrace trace;
	private Integer id;
	private BugSeverity severity;
	private BugValidationStatus status;
	
	public Bug(SymbolTrace trace) {
		this.trace = trace;
		id = getFreshBugId();
		severity = getDefaultSeverity();
		status = getDefaultStatus();
	}
	
	public abstract String getDescription();
	
	public final BugSeverity getSeverity() {
		return severity;
	}
	
	protected void setSeverity(BugSeverity severity) {
		this.severity = severity;
	}
	
	public BugValidationStatus getStatus() {
		return status;
	}

	protected void setStatus(BugValidationStatus status) {
		this.status = status;
	}
	
	public abstract BugSeverity getDefaultSeverity();
	
	public abstract BugValidationStatus getDefaultStatus();
	
	public final Integer getId() {
		return id;
	}
	
	protected SymbolTrace getTrace() {
		return trace;
	}
	
}
