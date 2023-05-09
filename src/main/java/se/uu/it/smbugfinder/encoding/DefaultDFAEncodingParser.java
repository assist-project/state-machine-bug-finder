package se.uu.it.smbugfinder.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.encoding.javacc.LabelParserFacade;
import se.uu.it.smbugfinder.encoding.javacc.TokenMgrError;

public class DefaultDFAEncodingParser implements DFAEncodingParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDFAEncodingParser.class.getName());

    private ParsingContextFactory factory;
    private Map<String, Label> cache;

    public DefaultDFAEncodingParser(ParsingContextFactory factory) {
        this.factory = factory;
    }


    public DefaultDFAEncodingParser() {
        this(ParsingContextFactory.EMPTY);
    }

    public DFAEncoding parse(InputStream encodedDfaStream) throws IOException {
        ParsingContext context = factory.newContext();
        cache = new HashMap<>();
        InputModelDeserializer<Label, CompactDFA<Label>>  deserializer = DOTParsers.dfa(
                DOTParsers.DEFAULT_FSA_NODE_PARSER,
                m -> processDFALabel(DOTParsers.DEFAULT_EDGE_PARSER.apply(m), context));
        InputModelData<Label, CompactDFA<Label>> inputModel = deserializer.readModel(encodedDfaStream);
        return new DFAEncoding(inputModel.model, inputModel.alphabet);
    }

    /*
     * We use a cache to ensure that the same label string mapped to the same Label object, which is a requirement for the deserializer.
     */
    protected Label processDFALabel(String labelString, ParsingContext context) {
        labelString = labelString.replace("\\\\", "\\");
        if (cache.containsKey(labelString)) {
            return cache.get(labelString);
        }
        try {
            Label label = LabelParserFacade.parseLabelString(labelString, context);
            cache.put(labelString, label);
            return label;
        } catch (se.uu.it.smbugfinder.encoding.javacc.ParseException | TokenMgrError e) {
            throw new InvalidLabelException(labelString, e);
        }
    }

}
