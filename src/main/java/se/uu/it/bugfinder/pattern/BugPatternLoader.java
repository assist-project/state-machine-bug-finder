package se.uu.it.bugfinder.pattern;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.dfa.SymbolMapping;
import se.uu.it.bugfinder.encoding.DfaDecoder;

public class BugPatternLoader {
	private static final Logger LOGGER = LogManager.getLogger(BugPatternLoader.class);
	private static JAXBContext context;

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
	
	public BugPatterns loadPatterns(String patternsFile, Collection<Symbol> symbols) throws BugPatternLoadingException {
		BugPatterns bugPatterns = null;
		LOGGER.info("Loading bug patterns");
		InputStream patternsStream = null;
		URI patternsURI = new File(patternsFile).getParentFile().toURI();
		patternsStream = getResourceAsStream(patternsFile);
		try {
			bugPatterns = loadPatterns(patternsStream);
		} catch (Exception e) {
			throw new BugPatternLoadingException("Failed to load patterns from patterns XML file from file " + patternsFile, e);
		}
		
		preparePatterns(bugPatterns, patternsURI, symbols);
		LOGGER.info("Successfully loaded {} bug patterns from file {}", bugPatterns.getBugPatterns().size(), patternsFile);
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
		
		DfaAdapter validHandshakeLanguage = loadSpecification.apply(bugPatterns.getSpecificationLanguagePath());
		bugPatterns.setSpecificationLanguage(validHandshakeLanguage);
		
		for (BugPattern bugPattern : bugPatterns.getBugPatterns()) {
			DfaAdapter bugLanguage = loadSpecification.apply(bugPattern.getBugLanguagePath());
			bugPattern.setBugLanguage(bugLanguage);
		}
	}
	
	private DfaAdapter loadDfa(String encodedDfaPath, URI location, Collection<Symbol> symbols){
		LOGGER.info("Loading specification at path: {}", encodedDfaPath);
		URI encodedDfaLocation = location.resolve(encodedDfaPath);
		InputStream specificationStream = getResourceAsStream(encodedDfaLocation.getPath()); 
		try {
			DfaAdapter dfaAdapter = dfaDecoder.decode(new InputStreamReader(specificationStream), symbols);
			return dfaAdapter;
		} catch (Exception e) {
			throw new BugPatternLoadingException("Error handling encoded dfa at path " + encodedDfaLocation.getPath(), e);
		}
	}
	
	private InputStream getResourceAsStream(String resourcePath) {
		InputStream specificationStream = BugPatternLoader.class.getResourceAsStream(resourcePath);
		if (specificationStream == null) {
			File file = new File(resourcePath);
			if (file.exists()) {
				try {
					specificationStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new BugPatternLoadingException("Failed to load resource at path " + resourcePath, e);
				}
			}
		}
		if (specificationStream == null) {
			throw new BugPatternLoadingException("Could not find resource at path " + resourcePath);
		}
		
		return specificationStream;
	}
}
