package se.uu.it.smbugfinder;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTInputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

/**
 * Generates a Mealy machine from a DOT specification.
 */
public class MealyDOTParser {
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> InputModelData<I, A> parse(AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor <I, O> processor) throws IOException, FormatException {
        DOTInputModelDeserializer<S, I, A> parser = DOTParsers.mealy(creator, (map)
                -> {
                    Pair<String, String> ioStringPair = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(map);
                    Pair<I, O> ioPair = processor.processMealyInputOutput(ioStringPair.getFirst(), ioStringPair.getSecond());
                    return ioPair;
                });
        return parser.readModel(inputStream);
    }

    public static InputModelData<String, CompactMealy<String, String>> parse(InputStream inputStream) throws IOException, FormatException {
        return parse(new CompactMealy.Creator<String, String>(), inputStream, (i,o) -> Pair.of(i.toString(), o.toString()));
    }

    public static interface MealyInputOutputProcessor <I, O> {
        Pair<I, O> processMealyInputOutput(String inputName, String outputName);
    }
}
