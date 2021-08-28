package se.uu.it.bugfinder.specification;

import java.io.FileNotFoundException;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.pfg666.dotparser.fsm.dfa.AbstractDFAProcessor;
import com.pfg666.dotparser.fsm.dfa.DFADotParser;

import net.automatalib.automata.fsa.impl.FastDFA;
import se.uu.it.bugfinder.specification.javacc.LabelParserFacade;
import se.uu.it.bugfinder.specification.javacc.ParseException;
import se.uu.it.bugfinder.specification.javacc.TokenMgrError;

public class SpecificationLanguageParser {
	private static final Logger LOGGER = LogManager.getLogger(SpecificationTS.class.getName());

	private DFADotParser<SpecificationLabel> dotParser;

	public SpecificationLanguageParser(ParsingContext context) {
		this.dotParser = new DFADotParser<SpecificationLabel>( new SpecificationLabelProcessor(context));
	}
	
	public FastDFA<SpecificationLabel> parseDfaModel(Reader specSource) throws FileNotFoundException, ParseException, com.alexmerz.graphviz.ParseException {
		return dotParser.parseAutomaton(specSource).get(0);
	}
	
	static class SpecificationLabelProcessor extends AbstractDFAProcessor<SpecificationLabel>{
		private ParsingContext context;

		public SpecificationLabelProcessor(ParsingContext context) {
			this.context = context;
		}

		@Override
		protected SpecificationLabel processDFALabel(String labelString) {
			labelString = labelString.replace("\\\\", "\\");
			try {
				SpecificationLabel label = LabelParserFacade.parseLabelString(labelString, context);
				return label;
			} catch (ParseException | TokenMgrError e) {
				throw new InvalidLabelException(labelString, e);
			}
		}
	}
}
