package se.uu.it.bugfinder.encoding;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;

public class EncodedDfaHolder {
	private DFA<?, Label> encodedDfa;
	private Collection<Label> labels;
	
	public EncodedDfaHolder(DFA<?, Label> encodedDfa, Collection<Label> labels) {
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
