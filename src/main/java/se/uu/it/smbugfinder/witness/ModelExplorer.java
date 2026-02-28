package se.uu.it.smbugfinder.witness;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import se.uu.it.smbugfinder.utils.AutomatonUtils;
import se.uu.it.smbugfinder.utils.AutomatonUtils.PredMap;
import se.uu.it.smbugfinder.utils.AutomatonUtils.PredStruct;

/**
 * Generates iterables for exploring ways to get from the initial state to target states.
 *
 * @param <S>  the type of states
 * @param <I>  the type of inputs
 */
public class ModelExplorer<S, I> {
    private PredMap<S, I> predMap;
    private UniversalDeterministicAutomaton<S, I, ?, ?, ?>  model;
    private Predicate<S> startStateFilter = null;
    private Mapping<S, Word<I>> stateMapping;
    private Collection<I> inputs;

    public ModelExplorer(UniversalDeterministicAutomaton<S, I, ?, ?, ?> model, Collection<I> inputs) {
        this.model = model;
        this.inputs = inputs;
        this.predMap = AutomatonUtils.generatePredecessorMap(model, inputs);
    }

    public void setStartStateFilter(Predicate<S> filter) {
        startStateFilter = filter;
        if (stateMapping == null) {
            MutableMapping<S, Word<I>> stateMapping = model.createDynamicStateMapping();
            Covers.stateCoverIterator(model, inputs).forEachRemaining(seq -> stateMapping.put(model.getState(seq), seq));
            this.stateMapping = stateMapping;
        }
    }

    public Iterable<Word<I>> wordsToTargetStates(Collection<S> targetStates, SearchConfig options) {
        return new Iterable<Word<I>>() {
            @Override
            public Iterator<Word<I>> iterator() {
                return new BFSPathToStateIterator(targetStates, options);
            }
        };
    }

    private class BFSPathToStateIterator implements Iterator<Word<I>> {
        private Queue<SearchState> toVisit;
        private Word<I> nextWord;
        private SearchState searchState;
        private Iterator<PredStruct<S, I>> visitingIter;
        private int stateVisit;
        private boolean visitTargetStates;
        private final AtomicLong idGenerator = new AtomicLong();
        private Set<S> targetStates;
        private int queueBound;

        private BFSPathToStateIterator(Collection<S> targetStates, SearchConfig options) {
            this.stateVisit = options.getStateVisitBound();
            this.visitTargetStates = options.isVisitTargetStates();
            this.queueBound = options.getQueueBound();
            SearchOrder order = options.getOrder();

            toVisit = switch(order) {
                case MIN_VISIT ->
                    new PriorityQueue<SearchState>(new Comparator<SearchState>(){
                        @Override
                        public int compare(SearchState s1, SearchState s2) {
                            // we prioritize states based on the max visited value and on the order in which they were created.
                            int compMax = Integer.compare(s1.maxVisited(), s2.maxVisited());
                            if (compMax == 0) {
                                return Long.compare(s1.getId(), s2.getId());
                            }
                            return compMax;
                        }
                    });
                case MIN_STATE ->
                    new PriorityQueue<SearchState>(new Comparator<SearchState>(){
                        @Override
                        public int compare(SearchState s1, SearchState s2) {
                            // we prioritize states based on the max visited value and on the order in which they were created.
                            int compStates = Integer.compare(s1.distinctStatesVisited(), s2.distinctStatesVisited());
                            if (compStates == 0) {
                                return Long.compare(s1.getId(), s2.getId());
                            }
                            return compStates;
                        }
                    });
                case MIN_STATE_MIN_VISIT ->
                    new PriorityQueue<SearchState>(new Comparator<SearchState>(){
                        @Override
                        public int compare(SearchState s1, SearchState s2) {
                            // we prioritize states based on the max visited value and on the order in which they were created.
                            int compStates = Integer.compare(s1.distinctStatesVisited(), s2.distinctStatesVisited());
                            if (compStates == 0) {
                                compStates = Integer.compare(s1.maxVisited(), s2.maxVisited());
                                if (compStates == 0) {
                                    return Long.compare(s1.getId(), s2.getId());
                                }
                            }
                            return compStates;
                        }
                    });
                case MIN_VISIT_MIN_STATE ->
                    new PriorityQueue<SearchState>(new Comparator<SearchState>(){
                        @Override
                        public int compare(SearchState s1, SearchState s2) {
                            // we prioritize states based on the max visited value and on the order in which they were created.
                            int compStates = Integer.compare(s1.maxVisited(), s2.maxVisited());
                            if (compStates == 0) {
                                compStates = Integer.compare(s1.distinctStatesVisited(), s2.distinctStatesVisited());
                                if (compStates == 0) {
                                    return Long.compare(s1.getId(), s2.getId());
                                }
                            }
                            return compStates;
                        }
                    });
                case MAX_STATE ->
                    new PriorityQueue<SearchState>(new Comparator<SearchState>(){
                        @Override
                        public int compare(SearchState s1, SearchState s2) {
                            // we prioritize states based on the max visited value and on the order in which they were created.
                            int compStates = Integer.compare(s2.distinctStatesVisited(), s1.distinctStatesVisited());
                            if (compStates == 0) {
                                return Long.compare(s1.getId(), s2.getId());
                            }
                            return compStates;
                        }
                    });
                default ->
                    new ArrayDeque<>();
                };

            for (S targetState : targetStates) {
                toVisit.add(new SearchState(targetState));
            }
            this.targetStates = new HashSet<>(targetStates);
            visitingIter = Collections.emptyListIterator();
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

        /*
         * We need use iterators to compensate for the lack of "yield" which would make life a lot easier.
         */
        private Word<I> computeNextWord() {
            if (nextWord != null) {
                return nextWord;
            } else {
                while (!toVisit.isEmpty() || visitingIter.hasNext()) {

                    while (!visitingIter.hasNext() && !toVisit.isEmpty()) {
                        searchState = toVisit.poll();

                        if (predMap.containsKey(searchState.getState())) {
                            visitingIter = predMap.get(searchState.getState()).iterator();
                        }

                        if (model.getInitialState().equals(searchState.getState())) {
                            nextWord = searchState.getSuffix();
                            return nextWord;
                        } else {
                            if (startStateFilter != null && startStateFilter.test(searchState.getState())) {
                                nextWord = searchState.getSuffix();
                                nextWord = stateMapping.get(searchState.getState()).concat(nextWord);
                                return nextWord;
                            }
                        }
                    }

                    while (visitingIter.hasNext()) {
                        PredStruct<S, I> predStruct = visitingIter.next();
                        SearchState potentialState = new SearchState(predStruct.getState(), predStruct.getInput(), searchState);

                        if (potentialState.maxVisited() <= stateVisit && (!visitTargetStates || !targetStates.contains(predStruct.getState()))) {
                            if (toVisit.size() < queueBound) {
                                toVisit.add(potentialState);
                            }
                            else {
                            }
                        }
                    }
                }
            }

            return nextWord;
        }

        private class SearchState {
            private I input;
            private S state;
            private long id;
            private SearchState parent;
            private int distinctVisited = -1;
            private int maxVisited = -1;

            SearchState(S endState) {
                state = endState;
                id = idGenerator.incrementAndGet();
            }

            SearchState(S state, I input, SearchState parent) {
                super();
                this.id = idGenerator.incrementAndGet();
                this.state = state;
                this.input = input;
                this.parent = parent;
            }

            Word<I> getSuffix() {
                if (parent == null) {
                    return Word.epsilon();
                } else {
                    return parent.getSuffix().prepend(input);
                }
            }

            S getState() {
                return state;
            }

            long getId() {
                return id;
            }

            SearchState getParent() {
                return parent;
            }

            int distinctStatesVisited() {
                if (distinctVisited == -1) {
                    Set<S> visited = new HashSet<>();
                    SearchState crtExplState = this;
                    while (crtExplState != null) {
                        S crtState = crtExplState.getState();
                        visited.add(crtState);
                        crtExplState = crtExplState.getParent();
                    }
                    distinctVisited = visited.size();
                }

                return distinctVisited;
            }

            int maxVisited() {
                if (maxVisited == -1) {
                    SearchState crtExplState = this;
                    Map<S,Integer> timesVisited = new HashMap<>();
                    while (crtExplState != null) {
                        S crtState = crtExplState.getState();
                        if (timesVisited.containsKey(crtState)) {
                            timesVisited.put(crtState, timesVisited.get(crtState) + 1);
                        } else {
                            timesVisited.put(crtState, 1);
                        }
                        crtExplState = crtExplState.getParent();
                    }
                    maxVisited = timesVisited.values().stream().max(Comparator.naturalOrder()).orElse(0);
                }
                return maxVisited;
            }
        }
    }
}
