package se.uu.it.smbugfinder;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import se.uu.it.smbugfinder.bug.BugValidationStatus;
import se.uu.it.smbugfinder.bug.StateMachineBug;
import se.uu.it.smbugfinder.pattern.AbstractBugPattern;
import se.uu.it.smbugfinder.pattern.BugPatterns;
import se.uu.it.smbugfinder.pattern.GeneralBugPattern;
import se.uu.it.smbugfinder.sut.Counter;


public class StatisticsTracker {

    private Counter inputCounter;
    private Counter resetCounter;
    private long startTime;
    private long totalTime;
    private long totalBugs;

    private long validationInputCount;
    private long validationResetCount;

    private Set<AbstractBugPattern> loadedBugPatterns;
    private Set<AbstractBugPattern> foundBugPatterns;
    private Set<AbstractBugPattern> validatedBugPatterns;

    private Map<AbstractBugPattern, Long> validatedTSBugPatterns;

    private Map<AbstractBugPattern, Long> bugPatternValidationInputCount;
    private Map<AbstractBugPattern, Long> bugPatternValidationResetCount;

    private Map<GeneralBugPattern, Long> generalBugPatternSequenceCount;
    private Map<GeneralBugPattern, Long> generalBugPatternUncategorizedSequenceCount;
    private Map<GeneralBugPattern, Long> generalBugPatternValidatedSequenceCount;
    private Map<GeneralBugPattern, Long> generalBugPatternValidatedUncategorizedSequenceCount;
    private boolean timeout;


    private StateMachineBugFinderConfig config;
    private Collection<?> inputAlphabet;

    public StatisticsTracker(StateMachineBugFinderConfig config, BugPatterns bugPatterns) {
        this.config = config;
        loadedBugPatterns = new TreeSet<>(bpComp());
        loadedBugPatterns.addAll(bugPatterns.getBugPatterns());
        foundBugPatterns = new TreeSet<>(bpComp());
        validatedBugPatterns = new TreeSet<>(bpComp());
        validatedTSBugPatterns = new LinkedHashMap<>();
        bugPatternValidationInputCount = new TreeMap<>(bpComp());
        bugPatternValidationResetCount = new TreeMap<>(bpComp());
        generalBugPatternSequenceCount = new TreeMap<>(bpComp());
        generalBugPatternUncategorizedSequenceCount = new TreeMap<>(bpComp());
        generalBugPatternValidatedSequenceCount = new TreeMap<>(bpComp());
        generalBugPatternValidatedUncategorizedSequenceCount = new TreeMap<>(bpComp());
    }

    private Comparator<AbstractBugPattern> bpComp() {
        return (bp1, bp2) -> bp1.getId().compareTo(bp2.getId());
    }


    public void setSutTracking(Counter inputCounter, Counter resetCounter) {
        this.inputCounter = inputCounter;
        this.resetCounter = resetCounter;
    }


    public void startStateMachineBugFinding(Collection<?> inputAlphabet) {
        startTime = System.currentTimeMillis();
        this.inputAlphabet = inputAlphabet;

    }

    /**
     * Gets the current time of the experiment
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    public <I,O> void finishStateMachineBugFinding(List<StateMachineBug<I,O>> bugs) {
        totalTime = System.currentTimeMillis() - startTime;
        for (StateMachineBug<I,O> modelBug : bugs) {
            foundBugPatterns.add(modelBug.getBugPattern());

            if (modelBug.getStatus() == BugValidationStatus.VALIDATION_SUCCESSFUL) {
                validatedBugPatterns.add(modelBug.getBugPattern());
            }
        }
        totalBugs = bugs.size();
    }

    public void startValidation(AbstractBugPattern bugPattern) {
        validationInputCount = inputCounter.get();
        validationResetCount = resetCounter.get();
    }

    public void found(AbstractBugPattern bugPattern) {
        foundBugPatterns.add(bugPattern);
    }

    public void timeout() {
        timeout = true;
    }

    public void validated(AbstractBugPattern bugPattern) {
        validatedBugPatterns.add(bugPattern);
        if (!validatedTSBugPatterns.containsKey(bugPattern)) {
            validatedTSBugPatterns.put(bugPattern, System.currentTimeMillis() - startTime);
        }
    }

    public void endValidation(AbstractBugPattern bugPattern) {
        bugPatternValidationInputCount.put(bugPattern, inputCounter.get() - validationInputCount);
        bugPatternValidationResetCount.put(bugPattern, resetCounter.get() - validationResetCount);
    }

    public void handleGeneralBugPattern(GeneralBugPattern bugPattern, long sequences, long uncategorizedSequences) {
        generalBugPatternUncategorizedSequenceCount.put(bugPattern, uncategorizedSequences);
        generalBugPatternSequenceCount.put(bugPattern, sequences);
    }

    public void handleValidatedGeneralBugPattern(GeneralBugPattern bugPattern, long validatedSequences, long validatedUncategorizedSequences) {
        generalBugPatternValidatedUncategorizedSequenceCount.put(bugPattern, validatedUncategorizedSequences);
        generalBugPatternValidatedSequenceCount.put(bugPattern, validatedSequences);
    }

    public Statistics generateStatistics() {
        Statistics statistics = new Statistics(config);
        statistics.setInputAlphabet(inputAlphabet);

        if (inputCounter != null) {
            statistics.setInputs(inputCounter.get());
            statistics.setResets(resetCounter.get());
        }
        statistics.setTimeout(timeout);
        statistics.setTotalBugs(totalBugs);
        statistics.setLoadedBugPatterns(loadedBugPatterns);
        statistics.setFoundBugPatterns(foundBugPatterns);
        statistics.setValidationBugPatterns(validatedBugPatterns);
        statistics.setValidationTSBugPatterns(validatedTSBugPatterns);
        statistics.setBugPatternValidationCounts(bugPatternValidationInputCount, bugPatternValidationResetCount);
        statistics.setGeneralBugPatternSequenceCounts(generalBugPatternSequenceCount, generalBugPatternUncategorizedSequenceCount);
        statistics.setGeneralBugPatternValidatedSequenceCounts(generalBugPatternValidatedSequenceCount, generalBugPatternValidatedUncategorizedSequenceCount);
        statistics.setTotalTime(totalTime);

        return statistics;
    }
}
