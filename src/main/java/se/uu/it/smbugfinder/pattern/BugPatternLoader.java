package se.uu.it.smbugfinder.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.MealySymbolExtractor;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.encoding.DFADecoder;

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

    public static <I,O> BugPatterns loadPatterns(String patternsDirectory, DFADecoder decoder, MealyMachine<?, I, ?, O> mealy, Collection<I> inputs, SymbolMapping<I,O> mapping) {
        Set<Symbol> symbols = new LinkedHashSet<>();
        MealySymbolExtractor.extractSymbols(mealy, inputs, mapping, symbols);
        BugPatternLoader loader = new BugPatternLoader(decoder);
        return loader.loadPatterns(patternsDirectory, symbols);
    }

    private DFADecoder dfaDecoder;

    public BugPatternLoader(DFADecoder dfaDecoder) {
        this.dfaDecoder = dfaDecoder;
    }

    public BugPatterns loadPatterns(String patternsFile, Collection<Symbol> symbols) throws BugPatternLoadingException {
        BugPatterns bugPatterns = null;
        LOGGER.info("Loading bug patterns");
        String patternsFileName = Path.of(patternsFile).getFileName().toString();
        URI parentFolderURI = URI.create(
                patternsFile.substring(0,
                        patternsFile.length() - patternsFileName.length()));
        InputStream patternsStream = getResourceAsStream(patternsFile);
        try {
            bugPatterns = loadPatterns(patternsStream);
        } catch (Exception e) {
            throw new BugPatternLoadingException("Failed to load patterns from catalogue " + patternsFile, e);
        }

        preparePatterns(bugPatterns, parentFolderURI, symbols);
        LOGGER.info("Successfully loaded {} bug patterns from catalogue {}", bugPatterns.getBugPatterns().size(), patternsFile);
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

    private DFAAdapter loadDfa(String encodedDfaPath, URI location, Collection<Symbol> symbols){
        LOGGER.info("Loading DFA at path: {}", encodedDfaPath);
        URI encodedDfaLocation = location.resolve(encodedDfaPath);
        InputStream encodedDfaStream = getResourceAsStream(encodedDfaLocation.getPath());
        try {
            DFAAdapter dfaAdapter = dfaDecoder.decode(encodedDfaStream, symbols);
            return dfaAdapter;
        } catch (Exception e) {
            throw new BugPatternLoadingException("Error handling encoded dfa at path " + encodedDfaLocation.getPath(), e);
        }
    }

    private InputStream getResourceAsStream(String resourcePath) {
        InputStream encodedDfaStream = BugPatternLoader.class.getResourceAsStream(resourcePath);
        if (encodedDfaStream == null) {
            File file = new File(resourcePath);
            if (file.exists()) {
                try {
                    encodedDfaStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new BugPatternLoadingException("Failed to load resource at path " + resourcePath, e);
                }
            }
        }
        if (encodedDfaStream == null) {
            throw new BugPatternLoadingException("Could not find resource at path " + resourcePath);
        }

        return encodedDfaStream;
    }
}
