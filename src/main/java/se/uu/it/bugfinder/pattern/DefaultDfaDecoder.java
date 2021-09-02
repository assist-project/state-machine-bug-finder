package se.uu.it.bugfinder.pattern;

import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.encoding.DecodingTS;
import se.uu.it.bugfinder.encoding.DefaultTokenMatcher;
import se.uu.it.bugfinder.encoding.EncodedDfaHolder;
import se.uu.it.bugfinder.encoding.Label;
import se.uu.it.bugfinder.encoding.TokenMatcher;

public class DefaultDfaDecoder implements DfaDecoder {
	private TokenMatcher tokenMatcher = new DefaultTokenMatcher();
	
	public DefaultDfaDecoder() {
		
	}
	
	public void setTokenMatcher(TokenMatcher tokenMatcher) {
		this.tokenMatcher = tokenMatcher;
	}
	
	public DfaAdapter decode(EncodedDfaHolder encodedDfaHolder, 
			Collection<Symbol> symbols) {
		FastDFA<Symbol> unfoldedSpec = unfold(encodedDfaHolder.getEncodedDfa(), encodedDfaHolder.getLabels(), symbols);
		FastDFA<Symbol> inputCompleteSpec = new FastDFA<Symbol>(new ListAlphabet<Symbol>(new ArrayList<>(symbols)));
		DFAs.complete(unfoldedSpec, inputCompleteSpec.getInputAlphabet(), inputCompleteSpec);
		DfaAdapter unfoldedDfa = new DfaAdapter(unfoldedSpec, unfoldedSpec.getInputAlphabet());
		return unfoldedDfa.minimize();
	}
	
	private <S> FastDFA<Symbol> unfold(DFA<S, Label> encodedDfa, Collection<Label> labels, Collection<Symbol> symbols) {
		Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
		FastDFA<Symbol> unfoldedSpecification = new FastDFA<>(alphabet);
		DecodingTS<S> specificationTS = new DecodingTS<S>(encodedDfa, labels);
		specificationTS.setTokenMatcher(tokenMatcher);
		TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, specificationTS, -1, symbols, unfoldedSpecification);
		return unfoldedSpecification;
	}
}
