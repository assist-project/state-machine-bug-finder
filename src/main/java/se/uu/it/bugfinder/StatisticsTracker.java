package se.uu.it.bugfinder;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import se.uu.it.bugfinder.bug.BugValidationStatus;
import se.uu.it.bugfinder.bug.ModelBug;
import se.uu.it.bugfinder.pattern.AbstractBugPattern;
import se.uu.it.bugfinder.pattern.BugPatterns;


public class StatisticsTracker {
	
	private AtomicLong inputCounter;
	private AtomicLong resetCounter;
	private long startTime;
	private long totalTime;
	private long totalBugs;

	private long validationInputCount;
	private long validationResetCount;
	
	private Set<AbstractBugPattern> loadedBugPatterns;
	private Set<AbstractBugPattern> foundBugPatterns;
	private Set<AbstractBugPattern> validatedBugPatterns;
	
	private Map<AbstractBugPattern, Long> bugPatternValidationInputCount;
	private Map<AbstractBugPattern, Long> bugPatternValidationResetCount;
	
	private ModelBugFinderConfig config;
	
	public StatisticsTracker(ModelBugFinderConfig config, BugPatterns bugPatterns) {
		this.config = config;
		loadedBugPatterns = new TreeSet<>(bpComp());
		loadedBugPatterns.addAll(bugPatterns.getBugPatterns());
		foundBugPatterns = new TreeSet<>(bpComp());
		validatedBugPatterns = new TreeSet<>(bpComp());
		bugPatternValidationInputCount = new TreeMap<>(bpComp());
		bugPatternValidationResetCount = new TreeMap<>(bpComp());
	}
	
	private Comparator<AbstractBugPattern> bpComp() {
		return (bp1, bp2) -> bp1.getId().compareTo(bp2.getId());
	}
	

	public void setSutTracking(AtomicLong inputCounter, AtomicLong resetCounter) {
		this.inputCounter = inputCounter;
		this.resetCounter = resetCounter;
	}
	
	
	public void startModelBugFinding() {
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Gets the current time of the experiment
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis() - startTime;
	}
	
	public void finishModelBugFinding(List<ModelBug> bugs) {
		totalTime = System.currentTimeMillis() - startTime; 
		for (ModelBug modelBug : bugs) {
			modelBug.getBugPatterns().forEach(bp -> {
				foundBugPatterns.add(bp);
			});
			
			if (modelBug.getStatus() == BugValidationStatus.VALIDATION_SUCCESSFUL) {
				validatedBugPatterns.addAll(modelBug.getBugPatterns());
			}
		} 
		totalBugs = bugs.size();
	}
	
	public void startValidation(AbstractBugPattern bugPattern) {
		validationInputCount = inputCounter.get();
		validationResetCount = resetCounter.get();
	}
	
	public void endValidation(AbstractBugPattern bugPattern) {
		bugPatternValidationInputCount.put(bugPattern, inputCounter.get() - validationInputCount);
		bugPatternValidationResetCount.put(bugPattern, resetCounter.get() - validationResetCount);
	}
	
	public Statistics generateStatistics() {
		Statistics statistics = new Statistics(config);
		
		if (inputCounter != null) {
			statistics.setInputs(inputCounter.get());
			statistics.setResets(resetCounter.get());
		}
		statistics.setTotalBugs(totalBugs);
		statistics.setLoadedBugPatterns(loadedBugPatterns);
		statistics.setFoundBugPatterns(foundBugPatterns);
		statistics.setValidationBugPatterns(validatedBugPatterns);
		statistics.setBugPatternValidationCounts(bugPatternValidationInputCount, bugPatternValidationResetCount);
		statistics.setTotalTime(totalTime);
		
		return statistics;
	}
}
