package se.uu.it.smbugfinder.encoding;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.ts.copy.TSCopy;

public class DFAEncoding {
	/**
	 * We use a DFA representation (instead of a graph) in order to leverage existing functionality for traversing transition systems which we use to decode the DFA.
	 * e.g.  {@link TSCopy}
	 */
	private DFA<?, Label> encodedDfa;
	private Collection<Label> labels;

	public DFAEncoding(DFA<?, Label> encodedDfa, Collection<Label> labels) {
		super();
		this.encodedDfa = encodedDfa;
		this.labels = labels;
	}

	public DFA<?, Label> getEncodedDfa() {
		return encodedDfa;
	}

	public Collection<Label> getLabels() {
		return labels;
	}

}
