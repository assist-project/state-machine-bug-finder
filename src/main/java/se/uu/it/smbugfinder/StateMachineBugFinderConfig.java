package se.uu.it.smbugfinder;

import se.uu.it.smbugfinder.witness.GenerationStrategy;
import se.uu.it.smbugfinder.witness.SearchConfig;

public class StateMachineBugFinderConfig {
	private GenerationStrategy witnessGenerationStrategy = GenerationStrategy.SHORTEST;
	
	private SearchConfig searchConfig;
	
	private boolean validate = false;

	private int uncategorizedBugBound = 10;
	
	private int nonConformingSequenceBound = 10000;
	
	private boolean checkSpecification = false;
	
	private int bound = 100;
	
	private String outputDir = null;
	
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

	public int getNonConformingSequenceBound() {
		return nonConformingSequenceBound;
	}

	public boolean isCheckSpecification() {
		return checkSpecification;
	}
	
	public String getOutputDir() {
		return outputDir;
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

	public void setCheckSpecification(boolean checkSpecification) {
		this.checkSpecification = checkSpecification;
	}

	public void setBound(int bound) {
		this.bound = bound;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	
}
