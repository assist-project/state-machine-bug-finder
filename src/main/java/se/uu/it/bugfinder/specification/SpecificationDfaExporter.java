package se.uu.it.bugfinder.specification;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.serialization.dot.GraphDOT;

public class SpecificationDfaExporter {
	
	public static void export(DFA<?, SpecificationLabel> dfa, Collection<SpecificationLabel> alphabet, Writer writer) throws IOException {
		GraphDOT.write(dfa, alphabet, writer);
	}
}
