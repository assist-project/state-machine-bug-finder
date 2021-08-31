package se.uu.it.bugfinder.witness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.bugfinder.utils.DFAUtils;

public class DeviantTransitionSequenceGenerator <I> implements SequenceGenerator<I> {
	
	private DFA<?, I> specification;

	public DeviantTransitionSequenceGenerator(DFA<?, I> specification) {
		this.specification = specification;
	}

	@Override
	public <S> Iterable<Word<I>> generateSequences(DFA<S, I> bugLanguage, Collection<I> alphabet) {
		return new Iterable<Word<I>>() {

			@Override
			public Iterator<Word<I>> iterator() {
				return new DeviantTransitionSequenceIterator<I>(bugLanguage, specification, alphabet);
			}
		};
	}
	
	private static class DeviantTransitionSequenceIterator<I> implements Iterator<Word<I>> {
		
		private DFA<?, I> specification;
		private FastDFA<I> bugLanguage;
		private FastDFAState sink;
		private Word<I> nextWord;
		private Collection<I> alphabet;

		DeviantTransitionSequenceIterator(DFA<?, I> bugLanguage, DFA<?, I> specification, Collection<I> symbols) {
			this.specification = specification;
			this.bugLanguage = new FastDFA<I>(new ListAlphabet<I>(new ArrayList<>(symbols)));
			createCopy(bugLanguage, this.bugLanguage, symbols);
			sink = this.bugLanguage.addState(false);
			symbols.forEach(i -> this.bugLanguage.setTransition(sink, i, sink));
			this.alphabet = symbols;
		}
		
		private <S1,S2> void createCopy(DFA<S1, I> from, MutableDFA<S2, I>  to, Collection<I> alphabet) {
			AutomatonLowLevelCopy.copy(AutomatonCopyMethod.BFS, from, alphabet, to);
		}
		
		@Override
		public boolean hasNext() {
			return computeNextWord() != null;
		}

		@Override
		public Word<I> next() {
			if (hasNext()) {
				Word<I> next = nextWord;
				nextWord = null;
				return next;
			}
			throw new NoSuchElementException();
		}

		private Word<I> computeNextWord() {
			if (nextWord != null) {
				return nextWord;
			} else {
				Word<I> acceptingWord = DFAUtils.findShortestAcceptingWord(bugLanguage, alphabet);
				if (acceptingWord != null) {
					nextWord = acceptingWord;
					Word<I> rejWord = DFAUtils.findShortestNonAcceptingPrefix(specification, acceptingWord);
					if (rejWord == null) {
						throw new InternalError(String.format("Specification does not reject word %s, which is accepted by bug language.", acceptingWord));
					}
					if (rejWord.isEmpty()) {
						throw new InternalError("A specification that rejects the empty word is a bad specification.");
					}
					FastDFAState stateBeforeDeviant = bugLanguage.getState(rejWord.prefix(-1));
					I deviantInput = rejWord.lastSymbol();
					bugLanguage.setTransition(stateBeforeDeviant, deviantInput, sink);
				}
			}
			return nextWord;
		}
	}
}
