package se.uu.it.smbugfinder;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import se.uu.it.smbugfinder.witness.GenerationStrategy;
import se.uu.it.smbugfinder.witness.SearchConfig;

public class StateMachineBugFinderConfig {

    @Parameter(names = { "-gs", "-generationStrategy" }, required = false, description = "Witness generation strategy.")
    private GenerationStrategy witnessGenerationStrategy = GenerationStrategy.SHORTEST;

    @ParametersDelegate
    private SearchConfig searchConfig;

    @Parameter(names = { "-vb",
            "-validateBugs" }, required = false, description = "Validate the bugs found. Validation requires either an online test harness or a Mealy machine.")
    private boolean validate = false;

    @Parameter(names = { "-ub",
            "-uncategorizedBound" }, required = false, description = "Bound on the number witnesses generated for an incorrectness specification (general bug pattern) that are not captured by specific bug patterns.")
    private int uncategorizedBugBound = 10;

    @Parameter(names = { "-ncb",
            "-nonConformingBound" }, required = false, description = "Bound on the number witnesses generated for a correctness specification.")
    private int nonConformingBound = 10000;

    @Parameter(names = { "-tb",
            "-testBound" }, required = false, description = "Bound on the number of tests executed to validate each bug pattern.")
    private int bound = 100;

    @Parameter(names = { "-dm", "-debugMode" }, required = false, description = "Runs a specific debug mode.")
    private DebugMode debugMode;

    @Parameter(names = { "-dwb",
            "-debugWitnessBound" }, required = false, description = "Bound on the number of counted witnesses generated assuming debug mode COUNT_GENERATED_WITNESSES is used")
    private int debugWitnessBound = 10000;

    public StateMachineBugFinderConfig() {
        searchConfig = new SearchConfig();
    }

    public GenerationStrategy getWitnessGenerationStrategy() {
        return witnessGenerationStrategy;
    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

    public int getBound() {
        return bound;
    }

    public boolean isValidate() {
        return validate;
    }

    public int getUncategorizedBugBound() {
        return uncategorizedBugBound;
    }

    public int getNonConformingBound() {
        return nonConformingBound;
    }

    public void setWitnessGenerationStrategy(GenerationStrategy witnessGenerationStrategy) {
        this.witnessGenerationStrategy = witnessGenerationStrategy;
    }

    public void setSearchConfig(SearchConfig searchConfig) {
        this.searchConfig = searchConfig;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public void setUncategorizedBugBound(int uncategorizedBugBound) {
        this.uncategorizedBugBound = uncategorizedBugBound;
    }

    public void setNonConformingBound(int nonConformingSequenceBound) {
        this.nonConformingBound = nonConformingSequenceBound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }

    public DebugMode getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(DebugMode debugMode) {
        this.debugMode = debugMode;
    }

    public int getDebugWitnessBound() {
        return debugWitnessBound;
    }

    public void setDebugWitnessBound(int debugWitnessBound) {
        this.debugWitnessBound = debugWitnessBound;
    }
}
