package se.uu.it.bugfinder.specification.javacc;

import se.uu.it.bugfinder.specification.ParsingContext;
import se.uu.it.bugfinder.specification.SpecificationLabel;

public class LabelParserFacade {
	
	public static SpecificationLabel parseLabelString(String label, ParsingContext context) throws ParseException {
		java.io.StringReader sr = new java.io.StringReader(label);
        LabelParser parser = new LabelParser( sr );
        parser.setParsingContext(context);
        SpecificationLabel result = parser.label();
        return result;
	}
	
	public static SpecificationLabel parseLabelString(String label) throws ParseException {
        java.io.StringReader sr = new java.io.StringReader(label);
        LabelParser parser = new LabelParser( sr );
        SpecificationLabel result = parser.label();
        return result;
	}
}
