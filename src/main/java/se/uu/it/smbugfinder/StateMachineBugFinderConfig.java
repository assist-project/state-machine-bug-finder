package se.uu.it.smbugfinder;

import java.time.Duration;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import se.uu.it.smbugfinder.witness.GenerationStrategy;
import se.uu.it.smbugfinder.witness.SearchConfig;

public class StateMachineBugFinderConfig {

    @Parameter(names = {"-gs", "-generationStrategy"}, required = false,
               description = "Witness generation strategy.")
    private GenerationStrategy witnessGenerationStrategy = GenerationStrategy.SHORTEST;

    @ParametersDelegate
    private SearchConfig searchConfig;

    @Parameter(names = {"-vb", "-validateBugs"}, required = false,
               description = "Validate the bugs found. Validation requires either an online test harness or a Mealy machine which simulates the SUT.")
    private boolean validate = false;

    @Parameter(names = { "-vtl", "-validationTimeLimit" }, required = false,
               description = "Bound on the time spent validating.", converter = DurationConverter.class)
    private Duration validationTimeLimit;

    @Parameter(names = {"-ub", "-uncategorizedBound"}, required = false,
               description = "Bound on the number sequences generated for a general bug pattern that have not been identified by any of the specific bug patterns.")
    private int uncategorizedBugBound = 10;

    @Parameter(names = {"-ncb", "-nonConformingBound"}, required = false,
               description = "Bound on the number non-conforming sequences generated for a correctness specification.")
    private int nonConformingSequenceBound = 10000;

    @Parameter(names = {"-tb", "-testBound"}, required = false,
               description = "Bound on the number of tests executed to validate bugs.")
    private int bound = 100;

    @Parameter(names = {"-d", "-debug"}, required = false,
               description = "Enables a specific debug mode. "
                 + "EVALUATE_SPECIFIC_BUG_PATTERNS generates and validates witnesses for each general bug pattern until validated witnesses cover all specific bug patterns. "
                 + "COUNT_GENERATED_WITNESSES counts the number of witnesses that would be generated for each specific bug pattern.")
    private DebugMode debugMode;

    @Parameter(names = { "-dwb", "-debugWitnessBound" }, required = false,
               description = "Bound on the number of witnesses generated/counted in debug modes EVALUATE_SPECIFIC_BUG_PATTERNS/COUNT_GENERATED_WITNESSES")
    private int debugWitnessBound = 100000;

    @Parameter(names = { "-dtl", "-debugTimeLimit" }, required = false,
               description = "Time bound for the debug mode EVALUATE_SPECIFIC_BUG_PATTERNS.", converter = DurationConverter.class)
    private Duration debugTimeLimit = Duration.ofDays(1);

    @Parameter(names = { "-sbp", "-selectBugPatterns" }, required = false,
               description = "Only uses the following bug patterns from the catalogue. To be used, these should be enabled in the patterns file.")
    private List<String> selectedBugPatterns;

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

    public DebugMode getDebugMode() {
        return debugMode;
    }

    public int getDebugWitnessBound() {
        return debugWitnessBound;
    }

    public Duration getDebugTimeLimit() {
        return debugTimeLimit;
    }

    public boolean isValidate() {
        return validate;
    }

    public Duration getValidationTimeLimit() {
        return validationTimeLimit;
    }

    public int getUncategorizedBugBound() {
        return uncategorizedBugBound;
    }

    public int getNonConformingSequenceBound() {
        return nonConformingSequenceBound;
    }

    public List<String> getSelectedBugPatterns() {
        return selectedBugPatterns;
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

    public void setNonConformingSequenceBound(int nonConformingSequenceBound) {
        this.nonConformingSequenceBound = nonConformingSequenceBound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }

    public void setDebugMode(DebugMode debugMode) {
        this.debugMode = debugMode;
    }

}
