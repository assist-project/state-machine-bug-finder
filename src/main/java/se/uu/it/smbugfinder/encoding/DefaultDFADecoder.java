package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ListAlphabet;
import net.automatalib.automaton.fsa.FastDFA;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.Symbol;

public class DefaultDFADecoder implements DFADecoder {
    private TokenMatcher tokenMatcher = new DefaultTokenMatcher();
    private EncodedTSParser parser;

    public DefaultDFADecoder(EncodedTSParser parser) {
        this.parser = parser;
    }

    public DefaultDFADecoder() {
        this(new DefaultEncodedDFAParser());
    }

    public void setTokenMatcher(TokenMatcher tokenMatcher) {
        this.tokenMatcher = tokenMatcher;
    }

    @Override
    public DFAAdapter decode(InputStream encodedTsStream, Collection<Symbol> symbols) throws Exception  {
        EncodedTS encodedTs = parser.parse(encodedTsStream);
        DFAAdapter decodedDfa = decode(encodedTs, symbols);
        return decodedDfa;
    }

    DFAAdapter decode(EncodedTS encodedTs, Collection<Symbol> symbols) {
        FastDFA<Symbol> decodedDfa = decode(encodedTs.getEncodedTS(), encodedTs.getLabels(), symbols);
        FastDFA<Symbol> inputCompleteDfa = new FastDFA<Symbol>(new ListAlphabet<Symbol>(new ArrayList<>(symbols)));
        DFAs.complete(decodedDfa, inputCompleteDfa.getInputAlphabet(), inputCompleteDfa);
        DFAAdapter dfaAdapter = new DFAAdapter(decodedDfa, decodedDfa.getInputAlphabet());
        return dfaAdapter.minimize();
    }

    private <S> FastDFA<Symbol> decode(DeterministicAcceptorTS<S, Label> encodedTs, Collection<Label> labels, Collection<Symbol> symbols) {
        Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
        FastDFA<Symbol> decodedDfa = new FastDFA<>(alphabet);
        DecodingTS<S> decodingTS = new DecodingTS<S>(encodedTs, labels);
        decodingTS.setTokenMatcher(tokenMatcher);
        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, decodingTS, -1, symbols, decodedDfa);
        return decodedDfa;
    }
}
