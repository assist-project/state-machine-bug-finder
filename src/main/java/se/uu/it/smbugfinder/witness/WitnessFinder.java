package se.uu.it.smbugfinder.witness;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import se.uu.it.smbugfinder.dfa.DfaAdapter;
import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.dfa.Trace;
import se.uu.it.smbugfinder.sut.SUT;

public class WitnessFinder {
	
	private int bound;
	private SequenceGenerator<Symbol> generator;

	public WitnessFinder(SequenceGenerator<Symbol> generator, int bound) {
		this.generator = generator;
		this.bound = bound;
	}
	
	public <I,O>  Trace<I,O> findWitness(SUT<I,O> sut, SymbolMapping<I,O> mapping, DfaAdapter sutBugLanguage, DfaAdapter bugLanguage) {
		return findWitness(sut, mapping, sutBugLanguage, bugLanguage,  true);
	}
	
	public <I,O>  Trace<I,O> findCounterexample(SUT<I,O> sut, SymbolMapping<I,O> mapping, DfaAdapter sutBugLanguage, DfaAdapter bugLanguage) {
		return findWitness(sut, mapping, sutBugLanguage, bugLanguage, false);
	}
	
	private <I,O> Trace<I,O> findWitness(SUT<I,O> sut, SymbolMapping<I,O> mapping, DfaAdapter sutBugLanguage, DfaAdapter bugLanguage, boolean desiredValidationOutcome) {
		int count = 0;
		for (Word<Symbol> sequence : generator.generateSequences(sutBugLanguage.getDfa(), sutBugLanguage.getSymbols())) {
			WordBuilder<I> inputWordBuilder = new WordBuilder<I>();
			sequence.stream().filter(s -> s instanceof InputSymbol).forEach(s -> inputWordBuilder.add( mapping.toInput((InputSymbol) s)));
			Word<I> inputWord = inputWordBuilder.toWord();
			Word<O> outputWord = sut.execute(inputWord);
			Trace<I,O> trace = new Trace<I,O> (inputWord, outputWord);
			Word<Symbol> actualSequence= mapping.fromExecutionTrace(trace);
			boolean exhibitsBug = bugLanguage.acceptsPrefix(actualSequence);
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
