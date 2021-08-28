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
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import se.uu.it.bugfinder.dfa.Symbol;

public class SpecificationTS<S> implements DeterministicAcceptorTS<RegisterState<S>, Symbol> {
		private static final Logger LOGGER = LogManager.getLogger(SpecificationTS.class.getName());
		
		private Map<String, Pattern> patternCache;
		
		private DFA<S, SpecificationLabel> specification;
		private Mapping<S,Collection<SpecificationLabel>> stateLabelsMapping;
		private RegisterState<S> initial;
		private RegisterState<S> sink;
		private TokenMatcher tokenMatcher;
		

		public SpecificationTS(DFA<S, SpecificationLabel> specification, S sink, 
				Mapping<S,Collection<SpecificationLabel>> specificationLabels, 
				Collection<Symbol> symbols) {
			this.specification = specification;
			this.stateLabelsMapping = specificationLabels;
			this.initial = new RegisterState<S>(specification.getInitialState(), new Valuation());
			this.sink = new RegisterState<S> (sink, null);
			this.patternCache = new HashMap<>();
			this.tokenMatcher = new DefaultTokenMatcher();
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
				Collection<SpecificationLabel> localSpecificationLabels = stateLabelsMapping.get(dfaState);
				List<SpecificationLabel> otherLabels = new LinkedList<>();
				
				// variables used to check for non-determinism/inconsistency in the specification
				RegisterState<S> nextState = null, nextStateCandidate = null;
				SpecificationLabel previouslyMatchedLabel = null;
				
				for (SpecificationLabel specificationLabel : localSpecificationLabels) {
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
//		
//		throw new RuntimeSpecificationException(
//				String.format("Multiple other-dependent transitions from state %s, causing ambiguity. \n"
//						+ "Other transitions: %s and %s", dfaState, otherLabel, specificationLabel));
		
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
//			
//			switch(symbolDescription.getType()) {
//			case EL:
//				if (specificationLabel.equals(messageLabel)) {
//					nextState =  new RegisterState<S> (nextSpecificationState, nextValuation);
//				}
//				break;
//				
//			case FILTER:
//				FilterLabel filterLabel = (FilterLabel) specificationLabel;
//				String regex = filterLabel.getRegexFilter();
//				String resolvedRegex = resolveBackRefs(state.getValuation(), regex);
//				Pattern pattern = patternCache.get(resolvedRegex);
//				if (pattern == null) {
//					pattern = Pattern.compile(resolvedRegex);
//					patternCache.put(resolvedRegex, pattern);
//				}
//				Matcher matcher = pattern.matcher(messageLabel.toString());
//				
//				if (matcher.matches()) {
//					for (int i=1; i<=matcher.groupCount(); i++) {
//						nextValuation.put(captureGroupVariable(i), new Value(matcher.group(i)));
//					}
//					nextState = new RegisterState<S> (nextSpecificationState, nextValuation);
//				}
//				break;
//			
//			case ENUMERATION:
//				EnumerationLabel enumerationLabel = (EnumerationLabel) specificationLabel;
//				if (enumerationLabel.containsLabel(messageLabel)) {
//					nextState = new RegisterState<S> (nextSpecificationState, nextValuation);
//				}
//				break;
//				
//			case OTHER:
//				nextState = new RegisterState<S> (nextSpecificationState, nextValuation);
//				break;
//
//			case BINARY_EXPRESSION:
//				BinaryExpressionLabel expressionLabel = (BinaryExpressionLabel) specificationLabel;
//				BinarySetOperation operation = expressionLabel.getOperation();
//				Supplier<RegisterState<S>> nextStateLeft = () -> transition(state, messageLabel, expressionLabel.getLeft(), nextSpecificationState);
//				Supplier<RegisterState<S>> nextStateRight = getElements;
//				switch (operation) {
//				case DIFFERENCE:
//					if (nextStateLeft.get() != null && nextStateRight.get() == null) {
//						nextState = new RegisterState<S> (nextSpecificationState, nextValuation);
//					}
//					break;
//				case UNION:
//					if (nextStateLeft.get() != null || nextStateRight.get() != null) {
//						return nextState = new RegisterState<S> (nextSpecificationState, nextValuation);
//					}
//					break;
//				default:
//					throw new RuntimeSpecificationException(String.format("Unsupported binary operation type %s", specificationLabel.getType().name()));
//				}
//				break;
//			case SPECIFICATION:
//				SpecificationLabel specLabel = (SpecificationLabel) specificationLabel;
//				specLabel.getDescription()
//			default:
//				throw new RuntimeSpecificationException(String.format("Unsupported label type %s", specificationLabel.getType().name())); 
//			}
			return nextState;
		}
		
//		private Valuation storeCaptureGroups() {
//			
//		}
		
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
