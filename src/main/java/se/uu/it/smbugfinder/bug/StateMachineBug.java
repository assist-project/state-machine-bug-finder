package se.uu.it.smbugfinder.bug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.uu.it.smbugfinder.dfa.Trace;
import se.uu.it.smbugfinder.pattern.AbstractBugPattern;

public class StateMachineBug extends Bug{
	
	private List<AbstractBugPattern> bugPatterns;
	private Trace<?,?> counterexample;
	
	public StateMachineBug(Trace<?,?>  trace, List<AbstractBugPattern> bugPatterns) {
		super(trace);
		if (bugPatterns.isEmpty()) {
			throw new InternalError("There should be at least one pattern");
		}
		if (trace == null) {
			throw new InternalError("Trace cannot be null");
		}
		this.bugPatterns = new ArrayList<>(bugPatterns);
		Collections.sort(bugPatterns, (b1, b2) -> b1.getShortenedName().compareTo(b2.getShortenedName()));
		BugSeverity maxSeverity = bugPatterns.stream().map(bp -> bp.getSeverity()).max((s1, s2) -> Integer.compare(s1.ordinal(), s2.ordinal())).get();
		setSeverity(maxSeverity);
	}

	public StateMachineBug(Trace<?,?>  trace, AbstractBugPattern pattern) {
		super(trace);
		this.bugPatterns = Arrays.asList(pattern);
		setSeverity(pattern.getSeverity());
	}
	
	public List<AbstractBugPattern> getBugPatterns() {
		return bugPatterns;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Detected the following bug patterns in flow");
		sb.append(System.lineSeparator());
		for (AbstractBugPattern pattern : bugPatterns) {
			sb.append("Pattern: ").append(pattern.getName())
			.append(System.lineSeparator())
			.append("Severity: ").append(pattern.getSeverity())
			.append(System.lineSeparator())
			.append("Description: ").append(pattern.getDescription())
			.append(System.lineSeparator());
		}
		sb.append(getTrace().toCompactString())
		.append(System.lineSeparator());
		sb.append("Validation Status: ")
		.append(getStatus()).append(System.lineSeparator());
		if (getStatus() == BugValidationStatus.VALIDATION_FAILED) {
			sb.append("Counterexample: ").append(counterexample.toCompactString());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	
	public void validationFailed(Trace<?,?> counterexample) {
		setStatus(BugValidationStatus.VALIDATION_FAILED);
		this.counterexample = counterexample;
	}
	
	public void validationSuccessful() {
		setStatus(BugValidationStatus.VALIDATION_SUCCESSFUL);
	}
	
	public String getSubType() {
		return String.join(",", bugPatterns.stream()
				.map(bp -> bp.getShortenedName())
				.toArray(String []::new));
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