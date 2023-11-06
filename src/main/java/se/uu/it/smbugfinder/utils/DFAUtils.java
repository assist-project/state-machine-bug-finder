package se.uu.it.smbugfinder.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.ListAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.FastDFA;
import net.automatalib.automaton.fsa.FastDFAState;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.word.Word;

public class DFAUtils extends AutomatonUtils {

    /**
     * Converts a deterministic Mealy machine to an equivalent DFA.
     * Inputs/outputs are mapped to corresponding symbols given the provided input and output mappings.
     * An output can be mapped to zero, one or several symbols (which will be chained one after the other in the model).
     * The end result is an input-complete DFA which is not minimized to preserve resemblance with the original model.
     * Minimization can be achieved via minimize methods in {@link Automata}.
     */
    public static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>> DA convertMealyToDFA(MealyMachine<MS, MI, ?, MO> mealy, Collection<MI> inputs,
            Collection<DI> symbols,
            Mapping<MI,DI> inputMapping,
            Mapping<Pair<MS,MO>,List<DI>> outputMapping,
            Map<MS,DS> stateMapping,
            DA dfa) {
        MS mealyState = mealy.getInitialState();
        Map<MS, DS> inputStateMapping = new HashMap<>();
        DS dfaState = dfa.addInitialState(true);
        inputStateMapping.put(mealyState, dfaState);
        inputStateMapping.put(mealyState, dfaState);
        Set<MS> visited = new HashSet<>();
        convertMealyToDFA(mealyState, dfaState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
        MutableDFAs.complete(dfa, symbols, false, false);
        stateMapping.putAll(inputStateMapping);
        return dfa;
    }

    private static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>>  void convertMealyToDFA(MS mealyState, DS dfaState, MealyMachine<MS, MI, ?, MO> mealy,
            Collection<MI> inputs, Mapping<MI, DI> inputMapping, Mapping<Pair<MS,MO>, List<DI>> outputMapping, Map<MS, DS> inputStateMapping,
            Set<MS> visited, DA dfa) {
        inputStateMapping.put(mealyState, dfaState);
        DS inputState = dfaState;
        visited.add(mealyState);
        DS nextInputState;
        for (MI input : inputs) {
            DI inputSymbol = inputMapping.get(input);
            MO output = mealy.getOutput(mealyState, input);
            MS nextMealyState = mealy.getSuccessor(mealyState, input);

            nextInputState = inputStateMapping.get(nextMealyState);
            if (nextInputState == null) {
                nextInputState = dfa.addState(true);
                inputStateMapping.put(nextMealyState, nextInputState);
            }

            Collection<DI> outputSymbols = outputMapping.get(Pair.of(mealyState, output));
            List<DI> symbols = new ArrayList<>(outputSymbols.size()+1);
            symbols.add(inputSymbol);
            symbols.addAll(outputSymbols);

            DS lastState = inputState, nextState;
            for (int i = 0; i < symbols.size()-1; i++) {
                DI ioSymbol = symbols.get(i);
                nextState = dfa.addState(true);
                dfa.addTransition(lastState, ioSymbol, nextState);
                lastState = nextState;
            }

            dfa.addTransition(lastState, symbols.get(symbols.size()-1), nextInputState);

            if (!visited.contains(nextMealyState)) {
                convertMealyToDFA(nextMealyState, nextInputState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
            }
        }
    }

    public static <I> DFA<?,I> buildRejecting(Collection<I> inputs) {
        FastDFA<I> rejectingModel = new FastDFA<I>(new ListAlphabet<I>(new ArrayList<>(inputs)));
        FastDFAState rej = rejectingModel.addInitialState(false);
        for (I symbol : inputs) {
            rejectingModel.addTransition(rej, symbol, rej);
        }
        return rejectingModel;
    }


    public static <S,I> boolean hasAcceptingPaths(S state, DFA<S, I> automaton, Collection<I> inputs) {
        Set<S> reachableStates = new HashSet<>();
        reachableStates(automaton, inputs, state, reachableStates);
        return reachableStates.stream().anyMatch(s -> automaton.isAccepting(s));
    }

    public static <S,I> Word<I> findShortestAcceptingWord( DFA<S, I> automaton, Collection<I> inputs ) {
        DeterministicEquivalenceTest<I> test = new DeterministicEquivalenceTest<I>(DFAs.complete(automaton, new ListAlphabet<I>(new ArrayList<>(inputs))));
        Word<I> accepting = test.findSeparatingWord(buildRejecting(inputs), inputs);
        return accepting;
    }

    public static <S,I> Word<I> findShortestNonAcceptingPrefix( DFA<S, I> automaton, Word<I> word ) {
        int prefixLen = 0;
        S crtState = automaton.getInitialState();
        for (I input : word) {
            if (crtState == null || !automaton.isAccepting(crtState)) {
                return word.prefix(prefixLen);
            }
            prefixLen ++;
            crtState = automaton.getSuccessor(crtState, input);
        }

        if (crtState == null || !automaton.isAccepting(crtState)) {
            return word.prefix(prefixLen);
        }

        return null;
    }
}
