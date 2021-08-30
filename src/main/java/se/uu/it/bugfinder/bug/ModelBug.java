package se.uu.it.bugfinder.bug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.uu.it.bugfinder.dfa.SymbolTrace;
import se.uu.it.bugfinder.pattern.AbstractBugPattern;

public class ModelBug extends Bug{
	
	private List<AbstractBugPattern> bugPatterns;
	private SymbolTrace counterexample;
	
	public ModelBug(SymbolTrace trace, List<AbstractBugPattern> bugPatterns) {
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

	public ModelBug(SymbolTrace flow, AbstractBugPattern pattern) {
		super(flow);
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
	
	
	public void verificationFailed(SymbolTrace counterexample) {
		setStatus(BugValidationStatus.VALIDATION_FAILED);
		this.counterexample = counterexample;
	}
	
	public void verificationSuccessful() {
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