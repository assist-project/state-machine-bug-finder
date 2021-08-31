package se.uu.it.bugfinder.witness;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.InputSymbol;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.dfa.SymbolMapping;
import se.uu.it.bugfinder.dfa.Trace;

public class WitnessFinder <I,O> {
	
	private SequenceExecutor<I,O> sutOracle;
	private SymbolMapping<I,O> mapping;
	private int bound;
	private SequenceGenerator<Symbol> generator;
	
	public WitnessFinder(SequenceExecutor<I,O> sutOracle, SymbolMapping<I,O> mapping,
			 SequenceGenerator<Symbol> generator) {
		this.sutOracle = sutOracle;
		this.mapping = mapping;
		this.generator = generator;
		this.bound = -1;
	}

	public WitnessFinder(SequenceExecutor<I,O> sutOracle, SymbolMapping<I,O> mapping,
			 SequenceGenerator<Symbol> generator, int bound) {
		this.sutOracle = sutOracle;
		this.mapping = mapping;
		this.generator = generator;
		this.bound = bound;
	}
	
	public Trace<I,O> findWitness(DfaAdapter sutBugLanguage) {
		return findWitness(sutBugLanguage, true);
	}
	
	public Trace<I,O> findCounterexample(DfaAdapter sutBugLanguage) {
		return findWitness(sutBugLanguage, false);
	}
	
	private Trace<I,O> findWitness(DfaAdapter sutBugLanguage, boolean desiredValidationOutcome) {
		int count = 0;
		for (Word<Symbol> sequence : generator.generateSequences(sutBugLanguage.getDfa(), sutBugLanguage.getSymbols())) {
			WordBuilder<I> inputWordBuilder = new WordBuilder<I>();
			sequence.stream().filter(s -> s instanceof InputSymbol).forEach(s -> inputWordBuilder.add( mapping.toInput((InputSymbol) s)));
			Word<I> inputWord = inputWordBuilder.toWord();
			Word<O> outputWord = sutOracle.execute(inputWord);
			Trace<I,O> trace = new Trace<I,O> (inputWord, outputWord);
			Word<Symbol> actualSequence= mapping.fromExecutionTrace(trace);
			boolean exhibitsBug = sutBugLanguage.accepts(actualSequence);
			if ( (desiredValidationOutcome && exhibitsBug) || (!desiredValidationOutcome && !exhibitsBug) ) {
				return trace;
			}
			count ++;
			if (count >= bound && bound != -1) {
				break;
			}
		}
		return null;
	}
}
