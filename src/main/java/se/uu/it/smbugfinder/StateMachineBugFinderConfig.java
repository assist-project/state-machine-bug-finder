package se.uu.it.smbugfinder;

import com.beust.jcommander.Parameter;

import se.uu.it.smbugfinder.witness.GenerationStrategy;
import se.uu.it.smbugfinder.witness.SearchConfig;

public class StateMachineBugFinderConfig {
	
	@Parameter(names = {"-gs", "-generationStrategy"}, required=false, description = "Witness generation strategy.")
	private GenerationStrategy witnessGenerationStrategy = GenerationStrategy.SHORTEST;
	
	private SearchConfig searchConfig;
	
	@Parameter(names = {"-vb", "-validateBugs"}, required=false, description = "Validate the bugs found. Validation requires either an online test harness or a Mealy machine.")
	private boolean validate = false;

	@Parameter(names = {"-ub", "-uncategorizedBound"}, required=false, description = "Bound on the number non-conforming sequences generated for a (correctness) specification.")
	private int uncategorizedBugBound = 10;
	
	@Parameter(names = {"-ncb", "-nonConformingBound"}, required=false, description = "Bound on the number non-conforming sequences generated for a (correctness) specification.")
	private int nonConformingSequenceBound = 10000;
	
	@Parameter(names = {"-tb", "-testBound"}, required=false, description = "Bound on the number of tests executed to validate bugs.")
	private int bound = 100;
	
	@Parameter(names = {"-d", "-debug"}, required=false, description = "Runs a specific debug mode.")
	private DebugMode debugMode;
	
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

	public boolean isValidate() {
		return validate;
	}

	public int getUncategorizedBugBound() {
		return uncategorizedBugBound;
	}

	public int getNonConformingSequenceBound() {
		return nonConformingSequenceBound;
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
	
}
