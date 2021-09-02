package se.uu.it.bugfinder.encoding;

import java.io.FileNotFoundException;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pfg666.dotparser.fsm.dfa.AbstractDFAProcessor;
import com.pfg666.dotparser.fsm.dfa.DFADotParser;

import net.automatalib.automata.fsa.impl.FastDFA;
import se.uu.it.bugfinder.encoding.javacc.LabelParserFacade;
import se.uu.it.bugfinder.encoding.javacc.TokenMgrError;

public class DefaultEncodedDfaParser implements EncodedDfaParser {
	private static final Logger LOGGER = LogManager.getLogger(DefaultEncodedDfaParser.class.getName());

	private ParsingContextFactory factory;

	public DefaultEncodedDfaParser(ParsingContextFactory factory) {
		this.factory = factory;
	}
	
	public EncodedDfaHolder parseEncodedDfa(Reader specSource) throws FileNotFoundException, com.alexmerz.graphviz.ParseException {
		ParsingContext context = factory.newContext();
		DFADotParser<Label> dotParser = new DFADotParser<Label>( new LabelProcessor(context));
		FastDFA<Label> parsedAut = dotParser.parseAutomaton(specSource).get(0);
		return new EncodedDfaHolder(parsedAut, parsedAut.getInputAlphabet());
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
			} catch (se.uu.it.bugfinder.encoding.javacc.ParseException | TokenMgrError e) {
				throw new InvalidLabelException(labelString, e);
			} 
		}
	}
}
