package se.uu.it.bugfinder.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.dfa.BasicStringDFAProcessor;
import com.pfg666.dotparser.fsm.dfa.DFADotParser;
import com.pfg666.dotparser.fsm.dfa.DFAProcessor;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.impl.ListAlphabet;

public class TestUtils {
	public static <S,I,T> String getAutomataString(Automaton<S, I, T> automaton, Collection<? extends I> inputAlphabet) {
		StringWriter sw = new StringWriter();
		try {
			GraphDOT.write(automaton, inputAlphabet, sw);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		return sw.toString();
	}
	
	/*
	 * Returns a DFA in which all unspecified transitions are directed to a sync state.
	 */
	public static <I> DFA<?, I> completeDFA(DFA<?, I> dfa, Collection<I> alphabet) {
		ListAlphabet<I> simpleAlphabet = new ListAlphabet<I>(new ArrayList<>(alphabet));
		FastDFA<I> complementModel = new FastDFA<>(simpleAlphabet);
		DFAs.complete(dfa, simpleAlphabet, complementModel);
		return complementModel;
		
	}
	
	public static <I> DFA<?, I> loadDFA(String dfaString, DFAProcessor<I> processor) {
		DFADotParser<I> parser = new DFADotParser<I>(processor);
		try {
			return parser.parseAutomaton(new StringReader("")).get(0);
		} catch (FileNotFoundException | ParseException e) {
			throw new RuntimeException("Failed to load DFA", e);
		}
	}
	
	public static DFA<?, String> loadDFA(String dfaString) {
		return loadDFA(dfaString, new BasicStringDFAProcessor());
	}
}
