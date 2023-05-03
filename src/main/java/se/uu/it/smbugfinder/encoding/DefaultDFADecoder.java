package se.uu.it.smbugfinder.encoding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.Symbol;

public class DefaultDFADecoder implements DFADecoder {
    private TokenMatcher tokenMatcher = new DefaultTokenMatcher();
    private DFAEncodingParser parser;

    public DefaultDFADecoder(DFAEncodingParser parser) {
        this.parser = parser;
    }

    public DefaultDFADecoder() {
        this(new DefaultDFAEncodingParser());
    }

    public void setTokenMatcher(TokenMatcher tokenMatcher) {
        this.tokenMatcher = tokenMatcher;
    }

    public DFAAdapter decode(InputStream dfaEncodingStream, Collection<Symbol> symbols) throws Exception  {
        DFAEncoding dfaEncoding = parser.parse(dfaEncodingStream);
        DFAAdapter decodedDfa = decode(dfaEncoding, symbols);
        return decodedDfa;
    }

    DFAAdapter decode(DFAEncoding dfaEncoding,
            Collection<Symbol> symbols) {
        FastDFA<Symbol> decodedDfa = decode(dfaEncoding.getEncodedDfa(), dfaEncoding.getLabels(), symbols);
        FastDFA<Symbol> inputCompleteDfa = new FastDFA<Symbol>(new ListAlphabet<Symbol>(new ArrayList<>(symbols)));
        DFAs.complete(decodedDfa, inputCompleteDfa.getInputAlphabet(), inputCompleteDfa);
        DFAAdapter dfaAdapter = new DFAAdapter(decodedDfa, decodedDfa.getInputAlphabet());
        return dfaAdapter.minimize();
    }

    private <S> FastDFA<Symbol> decode(DFA<S, Label> encodedDfa, Collection<Label> labels, Collection<Symbol> symbols) {
        Alphabet<Symbol> alphabet = new ListAlphabet<>(new ArrayList<>(symbols));
        FastDFA<Symbol> decodedDfa = new FastDFA<>(alphabet);
        DecodingTS<S> decodingTS = new DecodingTS<S>(encodedDfa, labels);
        decodingTS.setTokenMatcher(tokenMatcher);
        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, decodingTS, -1, symbols, decodedDfa);
        return decodedDfa;
    }
}
