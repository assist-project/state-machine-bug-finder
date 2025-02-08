package se.uu.it.smbugfinder.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMealyState;
import net.automatalib.automaton.transducer.impl.MealyTransition;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;

public class DFAUtilsTest {

    @Test
    public void testConvertMultiOutputMealyToDFA() throws IOException {
        List<String> inputs = Arrays.asList("a", "b");

        FastMealy<String, String> fm = new FastMealy<String, String>(new ListAlphabet<>(inputs));
        FastMealyState<String> s1 = fm.addInitialState();
        FastMealyState<String> s2 = fm.addState();
        FastMealyState<String> s3 = fm.addState();

        fm.addTransition(s1, "a", new MealyTransition<FastMealyState<String>, String>(s2, "o1"));
        fm.addTransition(s1, "b", new MealyTransition<FastMealyState<String>, String>(s3, "o1"));
        fm.addTransition(s2, "a", new MealyTransition<FastMealyState<String>, String>(s2, "o2"));
        fm.addTransition(s2, "b", new MealyTransition<FastMealyState<String>, String>(s2, "o1"));
        fm.addTransition(s3, "a", new MealyTransition<FastMealyState<String>, String>(s1, "o2"));
        fm.addTransition(s3, "b", new MealyTransition<FastMealyState<String>, String>(s3, "o1,o2"));

        List<String> labels = Arrays.asList( "?a", "?b", "!o1", "!o2" ) ;
        Mapping<String,String> inputMapping = (s) -> "?" + s;
        Mapping<Pair<FastMealyState<String>, String>,List<String>> outputMapping =
                (p) ->
        Arrays.stream(p.getSecond().split("\\,"))
        .map(o -> "!" + o)
        .collect(Collectors.toList());

        FastDFA<String> dfa = new FastDFA<String>(new ListAlphabet<String>(labels));
        Map<FastMealyState<String>, FastDFAState> stateMapping = new LinkedHashMap<>();
        DFAUtils.convertMealyToDFA(fm, inputs, labels, inputMapping, outputMapping, stateMapping, dfa);

        Assert.assertTrue(dfa.accepts(Arrays.asList("?a", "!o1")));
        Assert.assertFalse(dfa.accepts(Arrays.asList("?a", "!o2")));
        Assert.assertTrue(dfa.accepts(Arrays.asList("?b", "!o1", "?b", "!o1", "!o2")));
    }

    @Test
    public void testShortestAcceptingSequence() throws IOException {
        Symbol a = new InputSymbol("a");
        Symbol b = new InputSymbol("b");

        List<Symbol> Symbols = Arrays.asList(a, b);

        FastDFA<Symbol> dfa = new FastDFA<>(new ListAlphabet<>(Symbols));
        FastDFAState s0 = dfa.addInitialState(false);
        FastDFAState s1 = dfa.addState(false);
        FastDFAState s2 = dfa.addState(false);
        FastDFAState s3 = dfa.addState(false);
        FastDFAState s4 = dfa.addState(false);
        FastDFAState s5 = dfa.addState(true);

        dfa.addTransition(s0, a, s1);
        dfa.addTransition(s0, b, s2);
        dfa.addTransition(s1, a, s3);
        dfa.addTransition(s1, b, s4);
        dfa.addTransition(s2, a, s5);
        dfa.addTransition(s2, b, s2);
        dfa.addTransition(s3, a, s5);
        dfa.addTransition(s4, b, s5);

        Word<Symbol> actualShortest = DFAUtils.findShortestAcceptingWord(dfa, Symbols);
        Assert.assertEquals(Word.fromSymbols(b, a), actualShortest);
    }

}
