package se.uu.it.smbugfinder.pattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.automaton.transducer.MealyMachine;
import se.uu.it.smbugfinder.ResourceLoadingException;
import se.uu.it.smbugfinder.ResourceManager;
import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.MealySymbolExtractor;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.encoding.CustomParsingContext;
import se.uu.it.smbugfinder.encoding.DFADecoder;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.encoding.DefaultEncodedDFAParser;
import se.uu.it.smbugfinder.encoding.MappingTokenMatcher;
import se.uu.it.smbugfinder.encoding.MappingTokenMatcher.MappingTokenMatcherBuilder;
import se.uu.it.smbugfinder.encoding.OcamlValues;

/**
 * Loads bug patterns from a supplied directory path.
 */
public class BugPatternLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(BugPatternLoader.class);
    private static JAXBContext context;

    private static synchronized JAXBContext getJAXBContext()
            throws JAXBException, IOException {
        if (context == null) {
            context = JAXBContext.newInstance(BugPatterns.class,
                    AbstractBugPattern.class);
        }
        return context;
    }

    private DFADecoder dfaDecoder;

    public BugPatternLoader(DFADecoder dfaDecoder) {
        this.dfaDecoder = dfaDecoder;
    }

    /**
     * Generic method for loading bug patterns.
     * @param <I> class of SUT inputs
     * @param <O> class of SUT outputs
     * @param patternsFile path to patterns XML file
     * @param decoder transforms DOT files to  to {@link DFAAdapter} instances
     * @param mealy is the SUT state machine
     * @param inputs are the input symbols considered from the SUT Mealy machine
     * @param mapping maps input/output symbols from the SUT Mealy machine to the classes used for modeling symbols that appear in e.g., the DFAs of bug patterns
     * @return a {@link BugPatterns} instance representing the catalogue of bug patterns.
     */
    public static <I,O> BugPatterns loadPatterns(String patternsFile, DFADecoder decoder, MealyMachine<?, I, ?, O> mealy, Collection<I> inputs, SymbolMapping<I,O> mapping) {
        Set<Symbol> symbols = new LinkedHashSet<>();
        MealySymbolExtractor.extractSymbols(mealy, inputs, mapping, symbols);
        BugPatternLoader loader = new BugPatternLoader(decoder);
        return loader.loadPatterns(patternsFile, symbols);
    }

    /**
     * Convenient method for loading bug patterns that works with given symbols.
     * @param patternsFile path to patterns XMl file
     * @param symbols represents the symbols used in the bugpatterns
     * @return a {@link BugPatterns} instance representing the catalogue of bug patterns.
     */
    public static BugPatterns loadPatternsBasic(String patternsFile, Collection<Symbol> symbols) {
        BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());
        return loader.loadPatterns(patternsFile, symbols);
    }

    public BugPatterns loadPatterns(String patternsFile, Collection<Symbol> symbols) throws ResourceLoadingException {
        BugPatterns bugPatterns = null;
        LOGGER.info("Loading bug patterns");
        String patternsFileName = Path.of(patternsFile).getFileName().toString();
        URI parentFolderURI = URI.create(
                patternsFile.substring(0,
                        patternsFile.length() - patternsFileName.length()));
        InputStream patternsStream = ResourceManager.getResourceAsStream(patternsFile);
        try {
            bugPatterns = loadPatterns(patternsStream);
        } catch (Exception e) {
            throw new ResourceLoadingException("Failed to load patterns from catalogue " + patternsFile, e);
        }

        preparePatterns(bugPatterns, parentFolderURI, symbols);
        LOGGER.info("Successfully loaded {} bug pattern(s) from catalogue {}", bugPatterns.getBugPatterns().size(), patternsFile);
        return bugPatterns;
    }

    private BugPatterns loadPatterns(InputStream inputStream) throws JAXBException, IOException, XMLStreamException {
        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
        XMLInputFactory xif = XMLInputFactory.newFactory();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        BugPatterns bugPatterns = (BugPatterns) unmarshaller.unmarshal(xsr);
        bugPatterns.prepare();

        return bugPatterns;

    }

    private void preparePatterns(BugPatterns bugPatterns, URI location, Collection<Symbol> symbols) {
        applyPatternLanguage(bugPatterns, location, symbols);
        Function<String, DFAAdapter> loadSpecification = p -> loadDfa(p, location, symbols);

        if (bugPatterns.getSpecificationLanguagePath() != null) {
            DFAAdapter conformanceLanguage = loadSpecification.apply(bugPatterns.getSpecificationLanguagePath());
            bugPatterns.setSpecificationLanguage(conformanceLanguage);
        }

        for (BugPattern bugPattern : bugPatterns.getBugPatterns()) {
            DFAAdapter bugLanguage = loadSpecification.apply(bugPattern.getBugLanguagePath());
            bugPattern.setBugLanguage(bugLanguage);
        }
    }

    void applyPatternLanguage(BugPatterns patterns, URI location, Collection<Symbol> symbols) {
       if (patterns.getPatternLanguagePath() != null) {
                   URI patternLanguageLocation = location.resolve(patterns.getPatternLanguagePath());
                   String absolutePatternLanguagePath = ResourceManager.getResourceAsAbsolutePathString(patternLanguageLocation.getPath());
                OcamlValues parameters = new OcamlValues(absolutePatternLanguagePath);
                DefaultEncodedDFAParser parser = new DefaultEncodedDFAParser(() -> new CustomParsingContext(parameters));
                DefaultDFADecoder decoder = new DefaultDFADecoder(parser);

                MappingTokenMatcherBuilder builder = new MappingTokenMatcher.MappingTokenMatcherBuilder();
                builder.addMapFromSymbols(parameters, symbols);
                MappingTokenMatcher matcher = builder.build();
                decoder.setTokenMatcher(matcher);
                dfaDecoder = decoder;
            }
    }

    private DFAAdapter loadDfa(String encodedDfaPath, URI location, Collection<Symbol> symbols) {
        LOGGER.info("Loading DFA at path: {}", encodedDfaPath);
        URI encodedDfaLocation = location.resolve(encodedDfaPath);
        InputStream encodedDfaStream = ResourceManager.getResourceAsStream(encodedDfaLocation.getPath());
        try {
            DFAAdapter dfaAdapter = dfaDecoder.decode(encodedDfaStream, symbols);
            return dfaAdapter;
        } catch (Exception e) {
            throw new ResourceLoadingException("Error handling encoded dfa at path " + encodedDfaLocation.getPath(), e);
        }
    }
}
