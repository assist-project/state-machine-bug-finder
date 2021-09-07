package se.uu.it.smbugfinder.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uu.it.smbugfinder.dfa.DfaAdapter;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.encoding.DfaDecoder;

public class BugPatternLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugPatternLoader.class);
	private static JAXBContext context;
	
	private static final String PATTERNS_FILE = "patterns.xml";

	private static synchronized JAXBContext getJAXBContext()
			throws JAXBException, IOException {
		if (context == null) {
			context = JAXBContext.newInstance(BugPatterns.class,
					AbstractBugPattern.class);
		}
		return context;
	}
	
	private DfaDecoder dfaDecoder;
	
	public BugPatternLoader(DfaDecoder dfaDecoder) {
		this.dfaDecoder = dfaDecoder;
	}
	
	public BugPatterns loadPatterns(String patternsDirectory, Collection<Symbol> symbols) throws BugPatternLoadingException {
		BugPatterns bugPatterns = null;
		LOGGER.info("Loading bug patterns");
		InputStream patternsStream = null;
		URI patternsURI = URI.create(patternsDirectory);
		String patternsFile = patternsURI.resolve(PATTERNS_FILE).getPath();
		patternsStream = getResourceAsStream(patternsFile);
		try {
			bugPatterns = loadPatterns(patternsStream);
		} catch (Exception e) {
			throw new BugPatternLoadingException("Failed to load patterns from patterns XML file from file " + patternsDirectory, e);
		}
		
		preparePatterns(bugPatterns, patternsURI, symbols);
		LOGGER.info("Successfully loaded {} bug patterns from file {}", bugPatterns.getBugPatterns().size(), patternsDirectory);
		return bugPatterns;
	}
	
	private BugPatterns loadPatterns(InputStream inputStream) throws JAXBException, IOException, XMLStreamException {
		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(
				inputStream));
		BugPatterns bugPatterns = (BugPatterns) unmarshaller.unmarshal(xsr);
		bugPatterns.prepare();
		
		return bugPatterns;

	}

	private void preparePatterns(BugPatterns bugPatterns, URI location, Collection<Symbol> symbols) {
		Function<String, DfaAdapter> loadSpecification = p -> loadDfa(p, location, symbols);
		
		if (bugPatterns.getSpecificationLanguagePath() != null) {
			DfaAdapter conformanceLanguage = loadSpecification.apply(bugPatterns.getSpecificationLanguagePath());
			bugPatterns.setSpecificationLanguage(conformanceLanguage);
		}
		
		for (BugPattern bugPattern : bugPatterns.getBugPatterns()) {
			DfaAdapter bugLanguage = loadSpecification.apply(bugPattern.getBugLanguagePath());
			bugPattern.setBugLanguage(bugLanguage);
		}
	}
	
	private DfaAdapter loadDfa(String encodedDfaPath, URI location, Collection<Symbol> symbols){
		LOGGER.info("Loading DFA at path: {}", encodedDfaPath);
		URI encodedDfaLocation = location.resolve(encodedDfaPath);
		InputStream encodedDfaStream = getResourceAsStream(encodedDfaLocation.getPath()); 
		try {
			DfaAdapter dfaAdapter = dfaDecoder.decode(encodedDfaStream, symbols);
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
