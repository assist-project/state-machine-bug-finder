package se.uu.it.smbugfinder.bug;

import se.uu.it.smbugfinder.dfa.Trace;

public abstract class Bug<I,O> {
	private static int BUG_COUNT;

	private static Integer getFreshBugId() {
		return ++BUG_COUNT;
	}

	private Trace<I,O>  trace;
	private Integer id;
	private BugSeverity severity;
	private BugValidationStatus status;

	public Bug(Trace<I,O>  trace) {
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

	public Trace<I,O>  getTrace() {
		return trace;
	}

}
