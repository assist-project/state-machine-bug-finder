package se.uu.it.smbugfinder.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.encoding.javacc.LabelParserFacade;
import se.uu.it.smbugfinder.encoding.javacc.TokenMgrError;

public class DefaultEncodedDFAParser implements EncodedTSParser {

    private ParsingContextFactory factory;
    private Map<String, Label> cache;

    public DefaultEncodedDFAParser(ParsingContextFactory factory) {
        this.factory = factory;
    }


    public DefaultEncodedDFAParser() {
        this(ParsingContextFactory.EMPTY);
    }

    @Override
    public EncodedDFA parse(InputStream encodedDfaStream) throws IOException, FormatException {
        ParsingContext context = factory.newContext();
        cache = new HashMap<>();
        InputModelDeserializer<Label, CompactDFA<Label>>  deserializer = DOTParsers.dfa(
                DOTParsers.DEFAULT_FSA_NODE_PARSER,
                m -> processDFALabel(DOTParsers.DEFAULT_EDGE_PARSER.apply(m), context));
        InputModelData<Label, CompactDFA<Label>> inputModel = deserializer.readModel(encodedDfaStream);
        return new EncodedDFA(inputModel.model, inputModel.alphabet);
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
