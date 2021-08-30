package se.uu.it.bugfinder.specification;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import se.uu.it.bugfinder.dfa.Symbol;

public class SpecificationTS<S> implements DeterministicAcceptorTS<RegisterState<S>, Symbol> {
		private static final Logger LOGGER = LogManager.getLogger(SpecificationTS.class.getName());
		
		private Map<String, Pattern> patternCache;
		
		private DFA<S, SpecificationLabel> specification;
		private RegisterState<S> initial;
		private RegisterState<S> sink;
		private TokenMatcher tokenMatcher;
		private Collection<SpecificationLabel> labels;
		

		public SpecificationTS(DFA<S, SpecificationLabel> specification, Collection<SpecificationLabel> labels) {
			this.specification = specification;
			this.initial = new RegisterState<S>(specification.getInitialState(), new Valuation());
			this.sink = new RegisterState<S> (specification.getInitialState(), null);
			this.patternCache = new HashMap<>();
			this.tokenMatcher = new DefaultTokenMatcher();
			this.labels = labels;
		}
		
		public void setTokenMatcher(TokenMatcher tokenMatcher) {
			this.tokenMatcher = tokenMatcher;
		}

	    public RegisterState<S> getInitialState() {
	        return initial;
	    }

		@Override
		public Collection<RegisterState<S>> getTransitions(RegisterState<S> state, Symbol symbol) {
			return Arrays.asList(getTransition(state, symbol));
		}

		@Override
		public Set<RegisterState<S>> getInitialStates() {
			return Collections.singleton(initial); 
		}

		@Override
		public RegisterState<S> getSuccessor(RegisterState<S> state, Symbol symbol) {
			if (state.equals(sink)) {
				return sink;
			} else {
				S dfaState = state.getState();
				List<SpecificationLabel> otherLabels = new LinkedList<>();
				
				// variables used to check for non-determinism/inconsistency in the specification
				RegisterState<S> nextState = null, nextStateCandidate = null;
				SpecificationLabel previouslyMatchedLabel = null;
				
				for (SpecificationLabel specificationLabel : labels) {
					if (specification.getSuccessor(dfaState, specificationLabel) == null) {
						continue;
					}
					
					// we start by attempting to match the symbol against a non-other label 
					if (requiresOtherComputation(symbol, specificationLabel)) {
						otherLabels.add(specificationLabel);
					} else {
						nextStateCandidate = transition(state, symbol, specificationLabel);
						if (nextStateCandidate != null) {
							if (previouslyMatchedLabel != null) {
									throw new RuntimeSpecificationException(
											String.format("Non-determinism in state %s. \n"
											+ "The concrete label %s can trigger two transitions: %s and %s.", 
											dfaState, symbol, previouslyMatchedLabel, specificationLabel) );
							} else {
								nextState = nextStateCandidate;
								previouslyMatchedLabel = specificationLabel;	
							}
						}
					}
				}
				
				if (nextState == null) {
					previouslyMatchedLabel = null;
					// at this point, we know that either a transition was not specified for the symbol, or was done so by an other label 
					for (SpecificationLabel otherLabel : otherLabels) {
						nextStateCandidate = transition(state, symbol, otherLabel);
						if (nextStateCandidate != null) {
							if (previouslyMatchedLabel != null) {
								throw new RuntimeSpecificationException(
										String.format("Non-determinism in state %s. \n"
										+ "The concrete label %s can trigger two transitions: %s and %s.", 
										dfaState, symbol, previouslyMatchedLabel, otherLabel) );
							} else {
								nextState = nextStateCandidate;
								previouslyMatchedLabel = otherLabel;	
							}
						}
					} 
					if (nextState != null) {
						return nextState;
					}
					return sink;
				} else {
					return nextState;
				}
			}
		}
		
		private boolean requiresOtherComputation(Symbol symbol, SpecificationLabel specificationLabel) {
			DescriptionToken matchingToken = tokenMatcher.matchingAtomicToken(symbol, specificationLabel.getDescription());
			return matchingToken != null && matchingToken.getType() == DescriptionType.OTHER;
		}
		
		private RegisterState<S> transition(RegisterState<S> state, Symbol messageLabel, SpecificationLabel specificationLabel) {
			S specificationState = state.getState();
			S nextSpecificationState = specification.getTransition(specificationState, specificationLabel);
			RegisterState<S> nextState = null;
			if (nextSpecificationState != null) {
				nextState = transition(state, messageLabel, specificationLabel, nextSpecificationState);
			}
			return nextState;
		}
		
		private RegisterState<S> transition(RegisterState<S> state, Symbol symbol, SpecificationLabel specificationLabel, S nextSpecificationState ) {
			RegisterState<S>  nextState = null;
			DescriptionToken symbolDescription = specificationLabel.getDescription();
			Valuation nextValuation;
			if (tokenMatcher.match(symbol, symbolDescription)) {
				nextValuation = specificationLabel.getUpdate().update(symbol, state.getValuation());
				boolean guard = specificationLabel.getGuard().eval(symbol, nextValuation);
				if (guard) {
					nextState = new RegisterState<S>(nextSpecificationState, nextValuation);
				}
			}
			return nextState;
		}
		
		private Variable captureGroupVariable(int index) {
			return new Variable("cg" + index);
		}
		
		private String resolveBackRefs(Valuation valuation, String regex) {
			int index = 1;
			Variable var = captureGroupVariable(index);
			while (valuation.containsKey(var)) {
				Value value = valuation.get(var);
				regex = regex.replace("\\" + index, value.getStoredValue().toString());
				var = captureGroupVariable(++index);
			}
			
			return regex;
		}

		@Override
		public RegisterState<S> getTransition(RegisterState<S> state, Symbol input) {
			RegisterState<S> successor = getSuccessor(state, input);
			return successor;
		}



		@Override
		public Boolean getStateProperty(RegisterState<S> state) {
			return specification.getStateProperty(state.getState());
		}

		@Override
		public Void getTransitionProperty(RegisterState<S> transition) {
			return specification.getTransitionProperty(transition.getState());
		}

		@Override
		public boolean isAccepting(RegisterState<S> state) {
			if (state.equals(sink)) {
				return false;
			} else {
				return specification.isAccepting(state.getState());
			}
		}
}
