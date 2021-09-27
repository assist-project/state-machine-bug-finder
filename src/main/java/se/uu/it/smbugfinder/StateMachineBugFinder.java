package se.uu.it.smbugfinder;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import se.uu.it.smbugfinder.bug.StateMachineBug;
import se.uu.it.smbugfinder.dfa.DfaAdapter;
import se.uu.it.smbugfinder.dfa.MealyToDfaConverter;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.dfa.Trace;
import se.uu.it.smbugfinder.pattern.AbstractBugPattern;
import se.uu.it.smbugfinder.pattern.BugPattern;
import se.uu.it.smbugfinder.pattern.BugPatterns;
import se.uu.it.smbugfinder.pattern.GeneralBugPattern;
import se.uu.it.smbugfinder.sut.Counter;
import se.uu.it.smbugfinder.sut.InputCountingSUT;
import se.uu.it.smbugfinder.sut.ResetCountingSUT;
import se.uu.it.smbugfinder.sut.SUT;
import se.uu.it.smbugfinder.witness.ModelExplorer;
import se.uu.it.smbugfinder.witness.SearchConfig;
import se.uu.it.smbugfinder.witness.SearchOrder;
import se.uu.it.smbugfinder.witness.SequenceGenerator;
import se.uu.it.smbugfinder.witness.SequenceGeneratorFactory;
import se.uu.it.smbugfinder.witness.WitnessFinder;

public class StateMachineBugFinder<I,O> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineBugFinder.class);
	private MealyToDfaConverter<I,O> coverter;
	private boolean validate;
	private StateMachineBugFinderConfig config;
	private StatisticsTracker tracker;
	private DfaExporter exporter;
	
	public StateMachineBugFinder(StateMachineBugFinderConfig config) {
		this.validate = config.isValidate();
		this.config = config;
		this.coverter = new MealyToDfaConverter<>();
		this.exporter = (dfa, name) -> {};
	}
	
	/**
	 * @param converter
	 */
	public void setConverter(MealyToDfaConverter<I,O> converter) {
		this.coverter = converter;
	}
	
	/**
	 * @param exporter
	 */
	public void setExporter(DfaExporter exporter) {
		this.exporter = exporter;
	}
	
	public Statistics findBugs(BugPatterns patterns, MealyMachine<?,I,?,O> mealy, Collection<I> inputs, SymbolMapping<I,O> mapping, @Nullable SUT<I,O> sut, List<StateMachineBug<I,O>> bugs) {
		tracker = new StatisticsTracker(config, patterns);
		tracker.startStateMachineBugFinding(inputs);
		if (validate) {
			ResetCountingSUT<I, O> resetCountingSut = new ResetCountingSUT<I, O>(sut, new Counter("resets"));
			InputCountingSUT<I, O> inputCountingSut = new InputCountingSUT<I, O>(resetCountingSut, new Counter("inputs"));
			sut = inputCountingSut;
			tracker.setSutTracking(inputCountingSut.getCounter(), resetCountingSut.getCounter());
		}
		DfaAdapter sutLanguage = coverter.convert(mealy, inputs, mapping);
		exporter.exportDfa(sutLanguage, "sutLanguage.dot");
		List<BugPattern> detectedPatterns = new LinkedList<>(); 

		// match against each loaded bug pattern 
		for (BugPattern bugPattern : patterns.getSpecificBugPatterns()) {
			LOGGER.info("Checking bug pattern {}", bugPattern.getShortenedName());
			DfaAdapter bugLanguage = bugPattern.generateBugLanguage();

			exporter.exportDfa(bugLanguage, bugPattern.getShortenedName() + "Language.dot");
			if (bugLanguage.isEmpty()) {
				LOGGER.info("The bug pattern {} is an empty language when considering only the input/output labels from the SUT model. ", bugPattern.getName());
				continue;
			}
			
			DfaAdapter sutBugLanguage = bugPattern.generateBugLanguage()
					.intersect(sutLanguage)
					.minimize(); 
			
			if (!sutBugLanguage.isEmpty()) {
				detectedPatterns.add(bugPattern);
				LOGGER.info("sutBugLanguage not empty, finding witness");
				exporter.exportDfa(sutBugLanguage, "sut" + bugPattern.getShortenedName() + "Language.dot");
				if (validate) {
					SequenceGenerator<Symbol> sequenceGenerator = SequenceGeneratorFactory.buildGenerator(config.getWitnessGenerationStrategy(), config.getSearchConfig(), null);
					WitnessFinder witnessFinder = new WitnessFinder(sequenceGenerator, config.getBound()); 
					tracker.startValidation(bugPattern);
					Trace<I,O> witness = witnessFinder.findWitness(sut, mapping, sutBugLanguage);
					tracker.endValidation(bugPattern);
					if (witness != null) {
						StateMachineBug<I,O> bug = new StateMachineBug<>(witness, bugPattern);
						bug.validationSuccessful();
						bugs.add(bug);
						LOGGER.info("Found valid witness {}", witness.toCompactString());
					} else {
						// could not validate bug
						Trace<I,O> counterexample = witnessFinder.findCounterexample(sut, mapping, sutBugLanguage);
						Word<O> mealyOutput = mealy.computeOutput(counterexample.getInputWord());
						Trace<I,O> falseAlarm = new Trace<I,O> (counterexample.getInputWord(), mealyOutput);
						StateMachineBug<I,O> bug = new StateMachineBug<I,O>(falseAlarm, bugPattern);
						bug.validationFailed(counterexample);
						bugs.add(bug);
						LOGGER.info("Could not find valid witness, giving counterexample {}", counterexample.toCompactString());
					}
				} else {
					Word<Symbol> acceptingSequence = sutBugLanguage.getShortestAcceptingSequence();
					Trace<I,O> trace = mapping.toExecutionTrace(acceptingSequence);
					StateMachineBug<I,O> bug = new StateMachineBug<I,O>(trace, bugPattern);
					bugs.add(bug);
					LOGGER.info("Found witness {}", trace.toCompactString());
				}
			}
		}

		for (GeneralBugPattern bugPattern :  patterns.getGeneralBugPatterns()) {
			handleGeneralBugPatterns(bugPattern, sutLanguage, detectedPatterns, mapping, bugs);
		}
		
		DfaAdapter spec = patterns.getSpecificationLanguage();
		if (spec != null) {
			// check for the existence of unidentified specification bugs
			handleUncategorizedSpecificationBugs(spec, sutLanguage, detectedPatterns, mealy, mapping, bugs);
		}
		tracker.finishStateMachineBugFinding(bugs);
		return tracker.generateStatistics();
	}
	
	private void handleGeneralBugPatterns(GeneralBugPattern generalBugPattern, DfaAdapter sutLanguage, Collection<BugPattern> specificBugPatterns, SymbolMapping<I,O> mapping, List<StateMachineBug<I,O>> bugs) {
		LOGGER.info("Checking bug pattern {}", generalBugPattern.getShortenedName());
		DfaAdapter bugLanguage = generalBugPattern.generateBugLanguage().minimize();
		exporter.exportDfa(bugLanguage, generalBugPattern.getShortenedName() + "Language.dot");
		DfaAdapter sutBugLanguage = sutLanguage.intersect(bugLanguage).minimize();
		exporter.exportDfa(sutBugLanguage, "sut" + generalBugPattern.getShortenedName() + "Language.dot");
		Set<String> categorizingBp = new LinkedHashSet<>();
		
		SearchConfig search = new SearchConfig();
		search.setOrder(SearchOrder.INSERTION);
		search.setStateVisitBound(1);
		search.setVisitTargetStates(false);
		
		int uncategorizedSequences = 0, generatedSequences = 0;

		for (Word<Symbol> sequence : wordsToAcceptingStates(sutBugLanguage.getDfa(), sutBugLanguage.getSymbols(), search)) {
			generatedSequences ++;
			List<BugPattern> capturingBps = specificBugPatterns.stream().filter(bp -> bp.generateBugLanguage().accepts(sequence)).collect(Collectors.toList());
			capturingBps.forEach(bp -> categorizingBp.add(bp.getName()));
			if (capturingBps.isEmpty()) {

				Trace<I,O> trace = mapping.toExecutionTrace(sequence);
				
				StateMachineBug<I,O> bug = new StateMachineBug<>(trace, generalBugPattern);
				bugs.add(bug);
				uncategorizedSequences ++;
				if (uncategorizedSequences >= generalBugPattern.uncategorizedSequenceBound()) {
					break;
				} 				
			}
			if (generatedSequences > generalBugPattern.generatedSequenceBound()) {
				break;
			}
		}
		
		LOGGER.info("Sequences generated: {}", generatedSequences);
		LOGGER.info("Uncategorized sequences generated: {}", uncategorizedSequences);
		LOGGER.info("Categorizing bug patterns ({}): {}", categorizingBp.size(), categorizingBp.toString());
	}

	private void handleUncategorizedSpecificationBugs(DfaAdapter specLanguage, DfaAdapter sutLanguage, Collection<BugPattern> bugPatterns, 
			MealyMachine<?,I, ?, O> mealy, SymbolMapping<I,O> mapping, List<StateMachineBug<I,O>> bugs) {
		exporter.exportDfa(specLanguage, "specificationLanguage.dot");
		LOGGER.info("Generating specification-violating sequences and checking there are bug patterns capturing them");
		DfaAdapter specBugLanguage = specLanguage.complement().minimize();
		exporter.exportDfa(specBugLanguage, "specificationBugLanguage.dot");
		DfaAdapter sutSpecBugLanguage = sutLanguage.intersect(specBugLanguage).minimize();
		exporter.exportDfa(sutSpecBugLanguage, "sutSpecificationBugLanguage.dot");
		
		Set<Object> deviantTransitions = new LinkedHashSet<>();
		Set<String> categorizingBp = new LinkedHashSet<>();
		SearchConfig search = new SearchConfig();
		search.setOrder(SearchOrder.INSERTION);
		search.setStateVisitBound(1);
		search.setVisitTargetStates(false);
		
		int uncategorizedFlows = 0, allFlows = 0, deviantTransitionSkips = 0;
		for (Word<Symbol> sequence : wordsToAcceptingStates(sutSpecBugLanguage.getDfa(), sutSpecBugLanguage.getSymbols(), search)) {
			allFlows ++;
			List<AbstractBugPattern> capturingBps = bugPatterns.stream().filter(bp -> bp.generateBugLanguage().accepts(sequence)).collect(Collectors.toList());
			capturingBps.forEach(bp -> categorizingBp.add(bp.getName()));
			Object deviantTransition = getDeviantTransition(sequence, specBugLanguage, mealy, mapping);
			if (deviantTransition != null) {
				if (deviantTransitions.contains(deviantTransition)) {
					deviantTransitionSkips ++;
					continue;
				} 
				deviantTransitions.add(deviantTransition);
			} 
			if (capturingBps.isEmpty()) {
				if (specLanguage.accepts(sequence)) {
					throw new InternalError("Accepting sequence in uncategorized bug language accepted by specification");
				}
				
				Trace<I,O> trace = mapping.toExecutionTrace(sequence);
				StateMachineBug<I,O> bug = new StateMachineBug<>(trace, AbstractBugPattern.uncategorized());
				bugs.add(bug);
				uncategorizedFlows ++;
				if (uncategorizedFlows >= config.getUncategorizedBugBound()) {
					break;
				} 				
			}
			if (allFlows > config.getNonConformingSequenceBound()) {
				break;
			}
		}
		
		LOGGER.info("Non-conforming sequences generated: {}", allFlows);
		LOGGER.info("Uncategorized sequences generated: {}", uncategorizedFlows);
		LOGGER.info("Deviant transition skips: {}", deviantTransitionSkips);
		LOGGER.info("Categorizing bug patterns ({}): {}", categorizingBp.size(), categorizingBp.toString());
	}
	
	private <S>  Iterable<Word<Symbol>> wordsToAcceptingStates(DFA<S, Symbol> dfa, Collection<Symbol> symbols, SearchConfig search) {
		ModelExplorer<S, Symbol> explorer = new ModelExplorer<>(dfa, symbols);
		List<S> acceptingStates = dfa.getStates().stream().filter(s -> dfa.isAccepting(s)).collect(Collectors.toList());
		return explorer.wordsToTargetStates(acceptingStates, search);
	}
	
	private <MS> Object getDeviantTransition(Word<Symbol> sequence, DfaAdapter spec, MealyMachine<MS,I,?,O> mealy,  SymbolMapping<I,O> mapping) {
		assert(!spec.accepts(sequence));
		Set<Symbol> suffixSymbols = new LinkedHashSet<>();
		Word<Symbol> acceptedPrefix = null;
		Symbol deviatingSymbol = null;

		for (int i=1; i<=sequence.length(); i++) {
			Word<Symbol> prefix = sequence.prefix(sequence.length() - i);
			suffixSymbols.add(sequence.getSymbol(sequence.length() - i));
			if (spec.accepts(prefix)) {
				Set<Symbol> allowedSymbols = new HashSet<>();
				allowedSymbols(spec.getDfa(), prefix, suffixSymbols, allowedSymbols);
				if (allowedSymbols.containsAll(suffixSymbols)) {
					acceptedPrefix = prefix;
					deviatingSymbol = sequence.getSymbol(sequence.length() - i);
					break;
				}
			}
		}
		
		if (deviatingSymbol != null) {
			Word<Symbol> deviatingSequence = acceptedPrefix.append(deviatingSymbol);
			Trace<I, O> trace = mapping.toExecutionTrace(deviatingSequence);
			Word<I> inputWord = trace.getInputWord();
			return mealy.getTransition(mealy.getState(inputWord.prefix(-1)), inputWord.lastSymbol());
		}
		return null;
	}
	
	private <S> void allowedSymbols(DFA<S, Symbol> specDfa, Word<Symbol> fromPrefix, Collection<Symbol> usedSymbols,  Collection<Symbol> allowedSymbols) {
		S state = specDfa.getState(fromPrefix);
		if (specDfa.isAccepting(state)) {
			Set<S> visited = new HashSet<>();
			Deque<S> toVisit = new ArrayDeque<>();
			toVisit.add(state);
			while (!toVisit.isEmpty()) {
				S visiting = toVisit.pop();
				visited.add(visiting);
				for (Symbol symbol : usedSymbols) {
					S nextState = specDfa.getTransition(visiting, symbol);
					if (nextState != null && specDfa.isAccepting(nextState) ) {
						if (!visited.contains(nextState)) {
							toVisit.add(nextState);
						}
						
						allowedSymbols.add(symbol);
					}
				}
			}
		}
	}
}
