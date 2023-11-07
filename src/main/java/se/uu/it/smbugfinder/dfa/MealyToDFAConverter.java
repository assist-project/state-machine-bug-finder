package se.uu.it.smbugfinder.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ListAlphabet;
import net.automatalib.automaton.fsa.FastDFA;
import net.automatalib.automaton.fsa.FastDFAState;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import se.uu.it.smbugfinder.utils.DFAUtils;
import se.uu.it.smbugfinder.utils.MealyUtils;

public class MealyToDFAConverter<I,O> {

    private DFAPostProcessor<I,O> dfaPostProcessor;

    public MealyToDFAConverter(DFAPostProcessor<I,O> dfaPostProcessor) {
        this.dfaPostProcessor = dfaPostProcessor;
    }

    public MealyToDFAConverter() {
        this.dfaPostProcessor = new DFAPostProcessor<I, O>() {
            @Override
            public <DS, S> void process(MutableDFA<DS, Symbol> dfa, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs,
                    SymbolMapping<I, O> mapping) {
            }
        };
    }

    public <S> DFAAdapter convert(MealyMachine<S, I, ?, O> mealy,
            Collection<I> inputs, SymbolMapping<I,O> mapping) {
        Set<O> outputs = new LinkedHashSet<>();
        MealyUtils.reachableOutputs(mealy, inputs, outputs);

        Mapping<I, Symbol> inputMapping = i -> mapping.fromInput(i);

        Mapping<Pair<S, O>, List<Symbol>> outputMapping = pair -> {
            return new ArrayList<Symbol>(mapping.fromOutput(pair.getSecond()));
        };

        Set<Symbol> symbols = new LinkedHashSet<>();
        mapping.fromInputs(inputs, symbols);
        mapping.fromOutputs(outputs, symbols);
        Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
        FastDFA<Symbol> dfa = new FastDFA<>(alphabet);

        DFAUtils.convertMealyToDFA(mealy, inputs, symbols, inputMapping, outputMapping, new LinkedHashMap<>(), dfa);

        List<FastDFAState> rejecting = dfa.getStates().stream().filter(s -> !s.isAccepting()).collect(Collectors.toList());
        if (rejecting.size() != 1) {
            throw new InternalError("The DFA generated from the learned model is expected to have only one rejecting state");
        }

        dfaPostProcessor.process(dfa, mealy, inputs, mapping);

        return new DFAAdapter(dfa, symbols).complete().minimize();
    }

    public static interface DFAPostProcessor<I,O> {
        <DS, S> void process(MutableDFA<DS, Symbol> dfa, MealyMachine<S, I, ?, O> mealy,
                Collection<I> inputs, SymbolMapping<I,O> mapping);
    }
}
