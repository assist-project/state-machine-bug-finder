package se.uu.it.smbugfinder.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;

import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.fsa.DFAs;
import se.uu.it.smbugfinder.pattern.BugPattern;

public class TestUtils {
    /**
     * Returns the DOT representation of the automaton as a String.
     */
    public static <S,I,T> String getAutomataString(Automaton<S, I, T> automaton, Collection<? extends I> inputAlphabet) {
        StringWriter sw = new StringWriter();
        try {
            GraphDOT.write(automaton, inputAlphabet, sw);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return sw.toString();
    }

    /**
     * Returns the DOT representation of the bug pattern as a String.
     */
    public static String getBugPatternString(BugPattern bp) {
        return getAutomataString(bp.generateBugLanguage().getDfa(), bp.generateBugLanguage().getSymbols());
    }

    /**
     * Returns a DFA in which all unspecified transitions are directed to a sync state.
     */
    public static <I> DFA<?, I> completeDFA(DFA<?, I> dfa, Collection<I> alphabet) {
        ListAlphabet<I> simpleAlphabet = new ListAlphabet<I>(new ArrayList<>(alphabet));
        FastDFA<I> complementModel = new FastDFA<>(simpleAlphabet);
        DFAs.complete(dfa, simpleAlphabet, complementModel);
        return complementModel;

    }
}
