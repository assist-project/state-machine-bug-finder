package se.uu.it.smbugfinder.encoding.javacc;

import se.uu.it.smbugfinder.encoding.Label;
import se.uu.it.smbugfinder.encoding.ParsingContext;

public class LabelParserFacade {

	public static Label parseLabelString(String label, ParsingContext context) throws ParseException {
		java.io.StringReader sr = new java.io.StringReader(label);
        LabelParser parser = new LabelParser( sr );
        parser.setParsingContext(context);
        Label result = parser.label();
        return result;
	}

	public static Label parseLabelString(String label) throws ParseException {
        java.io.StringReader sr = new java.io.StringReader(label);
        LabelParser parser = new LabelParser( sr );
        Label result = parser.label();
        return result;
	}
}
