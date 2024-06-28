package se.uu.it.smbugfinder.encoding;

import java.util.Collection;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.util.ts.copy.TSCopy;


public class EncodedDFA implements EncodedTS {
    /**
     * We use a DFA representation (instead of a graph) in order to leverage existing functionality for traversing transition systems which we use to decode the DFA.
     * e.g.  {@link TSCopy}
     */
    private DFA<?, Label> encodedDfa;
    private Collection<Label> labels;

    public EncodedDFA(DFA<?, Label> encodedDfa, Collection<Label> labels) {
        super();
        this.encodedDfa = encodedDfa;
        this.labels = labels;
    }

    @Override
    public DFA<?, Label> getEncodedTS() {
        return encodedDfa;
    }

    @Override
    public Collection<Label> getLabels() {
        return labels;
    }

}
