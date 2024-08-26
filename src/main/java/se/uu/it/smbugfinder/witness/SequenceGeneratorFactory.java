package se.uu.it.smbugfinder.witness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;
import se.uu.it.smbugfinder.utils.DFAUtils;

public class SequenceGeneratorFactory {


    public static <I> SequenceGenerator<I> buildGenerator(GenerationStrategy generationStrategy, @Nullable SearchConfig config, @Nullable DFA<?,I> specification) {
        switch(generationStrategy) {
        case SHORTEST:
            return new ShortestSequenceGenerator<I>();
        case BFS:
            if (config == null) {
                throw new InternalError("Config is needed for BFS Sequence Generator");
            }
            return new BFSSequenceGenerator<I>(config);
        case DEVIANT:
            if (specification == null) {
                throw new InternalError("Specification is needed for Deviant Transition Sequence Generator");
            }
            return new DeviantTransitionSequenceGenerator<I>(specification);
        default:
            throw new NotImplementedException(String.format("Generation strategy %s is not supported.", generationStrategy));
        }
    }

    static class ShortestSequenceGenerator<I> implements SequenceGenerator<I> {

        @Override
        public <S> Iterable<Word<I>> generateSequences(DFA<S, I> bugLanguage, Collection<I> alphabet) {
            Word<I> shortest = DFAUtils.findShortestAcceptingWord(bugLanguage, alphabet);
            if (shortest != null) {
                return Arrays.asList(shortest);
            } else {
                return Collections.emptyList();
            }
        }
    }

    static class BFSSequenceGenerator<I> implements SequenceGenerator<I> {
        private SearchConfig config;

        BFSSequenceGenerator(SearchConfig config) {
            this.config = config;
        }

       @Override
        public <S> Iterable<Word<I>> generateSequences(DFA<S, I> bugLanguage, Collection<I> alphabet) {
            Set<S> acceptingStates = bugLanguage.getStates().stream().filter(s -> bugLanguage.isAccepting(s)).collect(Collectors.toSet());
            ModelExplorer<S,I> explorer = new ModelExplorer<>(bugLanguage, alphabet);
            return explorer.wordsToTargetStates(acceptingStates, config);
        }
    }

}
