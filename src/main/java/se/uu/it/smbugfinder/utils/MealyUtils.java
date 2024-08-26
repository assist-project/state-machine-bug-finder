package se.uu.it.smbugfinder.utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automaton.transducer.MealyMachine;

public class MealyUtils extends AutomatonUtils {

    /**
     * Determines all the outputs the model can generate in response to the given inputs.
     * @param automaton         the model expressed as automaton
     * @param inputs            the given inputs
     * @param reachableOutputs  the initial reachable outputs
     */
    public static <S,I,O> void reachableOutputs(MealyMachine<S, I, ?, O> automaton, Collection<I> inputs, Collection<? super O> reachableOutputs) {
        Queue<S> reachableStates = new ArrayDeque<S>();
        Set<O> outputs = new HashSet<O>();
        reachableStates(automaton, inputs, reachableStates);
        for (S state : reachableStates) {
            for (I input : inputs) {
                outputs.add(automaton.getOutput(state, input));
            }
        }
        reachableOutputs.addAll(outputs);
    }

}
