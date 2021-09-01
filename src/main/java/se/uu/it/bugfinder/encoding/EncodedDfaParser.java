package se.uu.it.bugfinder.encoding;

import java.io.FileNotFoundException;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.pfg666.dotparser.fsm.dfa.AbstractDFAProcessor;
import com.pfg666.dotparser.fsm.dfa.DFADotParser;

import net.automatalib.automata.fsa.impl.FastDFA;
import se.uu.it.bugfinder.encoding.javacc.LabelParserFacade;
import se.uu.it.bugfinder.encoding.javacc.ParseException;
import se.uu.it.bugfinder.encoding.javacc.TokenMgrError;

public class EncodedDfaParser {
	private static final Logger LOGGER = LogManager.getLogger(EncodedDfaParser.class.getName());

	private DFADotParser<Label> dotParser;

	public EncodedDfaParser(ParsingContext context) {
		this.dotParser = new DFADotParser<Label>( new LabelProcessor(context));
	}
	
	public FastDFA<Label> parseEncodedDfa(Reader specSource) throws FileNotFoundException, ParseException, com.alexmerz.graphviz.ParseException {
		return dotParser.parseAutomaton(specSource).get(0);
	}
	
	static class LabelProcessor extends AbstractDFAProcessor<Label>{
		private ParsingContext context;

		public LabelProcessor(ParsingContext context) {
			this.context = context;
		}

		@Override
		protected Label processDFALabel(String labelString) {
			labelString = labelString.replace("\\\\", "\\");
			try {
				Label label = LabelParserFacade.parseLabelString(labelString, context);
				return label;
			} catch (ParseException | TokenMgrError e) {
				throw new InvalidLabelException(labelString, e);
			}
		}
	}
}
