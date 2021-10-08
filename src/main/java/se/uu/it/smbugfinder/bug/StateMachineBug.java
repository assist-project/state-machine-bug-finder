package se.uu.it.smbugfinder.bug;

import se.uu.it.smbugfinder.dfa.Trace;
import se.uu.it.smbugfinder.pattern.AbstractBugPattern;

public class StateMachineBug<I,O> extends Bug<I,O>{
	
	private AbstractBugPattern bugPattern;
	private Trace<I,O> counterexample;
	
	public StateMachineBug(Trace<I,O>  trace, AbstractBugPattern bugPattern) {
		super(trace);
		if (trace == null) {
			throw new InternalError("Trace cannot be null");
		}
		this.bugPattern = bugPattern;
		setSeverity(bugPattern.getSeverity());
	}
	
	public AbstractBugPattern getBugPattern() {
		return bugPattern;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bug Pattern: ").append(bugPattern.getName())
			.append(System.lineSeparator())
			.append("Severity: ").append(bugPattern.getSeverity())
			.append(System.lineSeparator())
			.append("Description: ").append(bugPattern.getDescription())
			.append(System.lineSeparator());
		sb.append(getTrace().toCompactString())
		.append(System.lineSeparator())
		.append("Inputs: ").append(getTrace().getInputWord())
		.append(System.lineSeparator());
		sb.append("Validation Status: ")
		.append(getStatus()).append(System.lineSeparator());
		if (getStatus() == BugValidationStatus.VALIDATION_FAILED) {
			sb.append("Counterexample: ").append(counterexample.toCompactString());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	
	public void validationFailed(Trace<I,O> counterexample) {
		setStatus(BugValidationStatus.VALIDATION_FAILED);
		this.counterexample = counterexample;
	}
	
	public void validationSuccessful() {
		setStatus(BugValidationStatus.VALIDATION_SUCCESSFUL);
	}
	
	@Override
	public BugSeverity getDefaultSeverity() {
		return BugSeverity.UNKNOWN;
	}
	
	@Override
	public BugValidationStatus getDefaultStatus() {
		return BugValidationStatus.NOT_VALIDATED;
	}
}