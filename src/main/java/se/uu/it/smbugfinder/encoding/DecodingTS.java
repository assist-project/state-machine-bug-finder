package se.uu.it.smbugfinder.encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import se.uu.it.smbugfinder.dfa.Symbol;

public class DecodingTS <S> implements DeterministicAcceptorTS<RegisterState<S>, Symbol> {

        private DeterministicAcceptorTS<S, Label> encodedTs;
        private RegisterState<S> initial;
        private RegisterState<S> sink;
        private TokenMatcher tokenMatcher;
        private Collection<Label> labels;

        public DecodingTS(DeterministicAcceptorTS<S, Label> encodedTs, Collection<Label> labels) {
            this.encodedTs = encodedTs;
            this.initial = new RegisterState<S>(encodedTs.getInitialState(), new Valuation());
            this.sink = new RegisterState<S>(encodedTs.getInitialState(), null);
            this.tokenMatcher = new DefaultTokenMatcher();
            this.labels = labels;
        }

        public void setTokenMatcher(TokenMatcher tokenMatcher) {
            this.tokenMatcher = tokenMatcher;
        }

        @Override
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
                List<Label> otherLabels = new ArrayList<>();

                // variables used to check for non-determinism/inconsistency in the specification
                RegisterState<S> nextState = null, nextStateCandidate = null;
                Label previouslyMatchedLabel = null;

                for (Label label : labels) {
                    if (encodedTs.getSuccessor(dfaState, label) == null) {
                        continue;
                    }

                    // we start by attempting to match the symbol against a non-other label
                    if (requiresOtherComputation(symbol, label)) {
                        otherLabels.add(label);
                    } else {
                        nextStateCandidate = transition(state, symbol, label);
                        if (nextStateCandidate != null) {
                            if (previouslyMatchedLabel != null) {
                                    throw new RuntimeDecodingException(
                                            String.format("Non-determinism in state %s. %n"
                                            + "The concrete label %s can trigger two transitions: %s and %s.",
                                            dfaState, symbol, previouslyMatchedLabel, label) );
                            } else {
                                nextState = nextStateCandidate;
                                previouslyMatchedLabel = label;
                            }
                        }
                    }
                }

                if (nextState == null) {
                    previouslyMatchedLabel = null;
                    // at this point, we know that either a transition was not specified for the symbol, or was done so by an other label
                    for (Label otherLabel : otherLabels) {
                        nextStateCandidate = transition(state, symbol, otherLabel);
                        if (nextStateCandidate != null) {
                            if (previouslyMatchedLabel != null) {
                                throw new RuntimeDecodingException(
                                        String.format("Non-determinism in state %s. %n"
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

        private boolean requiresOtherComputation(Symbol symbol, Label label) {
            DescriptionToken matchingToken = tokenMatcher.matchingAtomicToken(symbol, label.getDescription());
            return matchingToken != null && matchingToken.getType() == DescriptionType.OTHER;
        }

        private RegisterState<S> transition(RegisterState<S> state, Symbol symbol, Label label) {
            S encodedDfaState = state.getState();
            S nextEncodedDfaState = encodedTs.getTransition(encodedDfaState, label);
            RegisterState<S> nextState = null;
            if (nextEncodedDfaState != null) {
                nextState = transition(state, symbol, label, nextEncodedDfaState);
            }
            return nextState;
        }

        private RegisterState<S> transition(RegisterState<S> state, Symbol symbol, Label label, S encodedDfaState ) {
            RegisterState<S>  nextState = null;
            DescriptionToken symbolDescription = label.getDescription();
            Valuation nextValuation;
            if (tokenMatcher.match(symbol, symbolDescription)) {
                nextValuation = label.getUpdate().update(symbol, state.getValuation());
                boolean guard = label.getGuard().eval(symbol, nextValuation);
                if (guard) {
                    nextState = new RegisterState<S>(encodedDfaState, nextValuation);
                }
            }
            return nextState;
        }

        @Override
        public RegisterState<S> getTransition(RegisterState<S> state, Symbol input) {
            RegisterState<S> successor = getSuccessor(state, input);
            return successor;
        }



        @Override
        public Boolean getStateProperty(RegisterState<S> state) {
            return isAccepting(encodedTs, state);
        }

        private boolean isAccepting(DeterministicAcceptorTS<S, Label> encodedDfa, RegisterState<S> state ) {
            return encodedDfa.isAccepting( state.getState());
        }

        @Override
        public Void getTransitionProperty(RegisterState<S> transition) {
            return null;
        }

        @Override
        public boolean isAccepting(RegisterState<S> state) {
            if (state.equals(sink)) {
                return false;
            } else {
                return isAccepting(encodedTs, state);
            }
        }
}
