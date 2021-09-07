package se.uu.it.smbugfinder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import se.uu.it.smbugfinder.pattern.AbstractBugPattern;

public class Statistics extends ExportableResult {
	private long totalTime;
	private long inputs;
	private long resets;
	private boolean validationEnabled;
	private long totalBugs;
	
	// Model bug finder
	private Collection<AbstractBugPattern> foundBugPatterns;
	private Collection<AbstractBugPattern> loadedBugPatterns;
	private Collection<AbstractBugPattern> verifiedBugPatterns;
	
	private Map<AbstractBugPattern, Long> bpInputCount;
	private Map<AbstractBugPattern, Long> bpResetCount;
	private StateMachineBugFinderConfig config;
	private Collection<?> inputAlphabet;
	
	public Statistics(StateMachineBugFinderConfig config) {
		this.config = config;
		foundBugPatterns = Collections.emptyList();
		loadedBugPatterns = Collections.emptyList();
		verifiedBugPatterns = Collections.emptyList();
	}
	
	
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		export(sw);
		String statsString = sw.toString();
		return statsString;
	}

	public void doExport(PrintWriter out) {
		title("Run Description", out);
		generateRunDescription(out, config, inputAlphabet);
		title("Statistics", out);

		section("General", out);
		if (validationEnabled) {
			out.println("Number of inputs: " + inputs);
			out.println("Number of resets: " + resets);
		}
		out.println("Number of bugs: " + totalBugs);
		out.println("Time bug-checking took (ms): " + totalTime);
		
		section("State Machine Bug Finder", out);
		printCollection("Bug patterns loaded", loadedBugPatterns, AbstractBugPattern::getName, out);
		printCollection("Bug patterns found", foundBugPatterns, AbstractBugPattern::getName, out);
		printCollection("Bug patterns validated successfully", verifiedBugPatterns, AbstractBugPattern::getName, out);
		if (config.isValidate() && bpInputCount != null) {
			printCounterMap("Validation Inputs per Bug Pattern", bpInputCount, AbstractBugPattern::getName, out);
			printCounterMap("Validation Resets per Bug Pattern", bpResetCount, AbstractBugPattern::getName, out);
		}
		
		out.close();
	}
	
	private <T,MT> void printCounterMap(String name, Map<T, Long> map, Function<T,MT> mapping, PrintWriter out) {
		out.println(name);
		map.forEach((k,v) -> out.println("   " + mapping.apply(k) + " : " + v));
		out.println();
	}
	
	private <T,MT> void printCollection(String name, Collection<T> collection, Function<T,MT> mapping, PrintWriter out) {
		out.format(name + " (%d): %s", collection.size(), collection.stream().map(mapping).collect(Collectors.toList()).toString());
		out.println();
	}
	
	public void generateRunDescription(PrintWriter out, StateMachineBugFinderConfig config, Collection<?> alphabet) {
		section("State Machine Bug Finding Parameters", out);
		out.println("Alphabet: " + alphabet);
		
		out.println(String.format("Loaded Bug Patterns (%d): %s", loadedBugPatterns.size(), 
					loadedBugPatterns.stream()
					.map(bp -> bp.getName()).collect(Collectors.toList()).toString()));
		out.println("Bug Validation Enabled: " + config.isValidate());
		out.println("Uncategorized Bug Bound: " + config.getUncategorizedBugBound());
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public long getInputs() {
		return inputs;
	}
	public void setInputs(long inputs) {
		this.inputs = inputs;
	}
	public long getResets() {
		return resets;
	}
	public void setResets(long resets) {
		this.resets = resets;
	}
	
	public void setTotalBugs(long totalBugs) {
		this.totalBugs = totalBugs;
	}

	public void setFoundBugPatterns(Collection<AbstractBugPattern> modelBugPatternsFound) {
		this.foundBugPatterns = modelBugPatternsFound;
	}

	public void setLoadedBugPatterns(Collection<AbstractBugPattern> loadedBugPatterns) {
		this.loadedBugPatterns = loadedBugPatterns;
	}
	
	public void setValidationBugPatterns(Collection<AbstractBugPattern> verifiedBugPatterns) {
		this.verifiedBugPatterns = verifiedBugPatterns;
	}
	
	public void setBugPatternValidationCounts(Map<AbstractBugPattern, Long> bpInputCount, Map<AbstractBugPattern, Long> bpResetCount) {
		this.bpInputCount = bpInputCount;
		this.bpResetCount = bpResetCount;
	}
	
	public void setInputAlphabet(Collection<?> inputAlphabet) {
		this.inputAlphabet = inputAlphabet;
	}
	
}
