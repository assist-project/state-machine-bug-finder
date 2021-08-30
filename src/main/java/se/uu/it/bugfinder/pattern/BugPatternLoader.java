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

import net.automatalib.automata.fsa.impl.FastDFA;
import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.bugfinder.dfa.DfaAdapterBuilder;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.specification.ParsingContext;
import se.uu.it.bugfinder.specification.SpecificationDfaParser;
import se.uu.it.bugfinder.specification.SpecificationLabel;
import se.uu.it.bugfinder.specification.javacc.ParseException;

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
	
	public static BugPatterns loadPatterns(String patternsFile, boolean resource, DfaAdapterBuilder builder, Collection<Symbol> symbols) throws BugPatternLoadingException {
		BugPatterns bugPatterns = null;
		LOGGER.info("Loading bug patterns");
		InputStream patternsStream;
		URI patternsURI = new File(patternsFile).getParentFile().toURI(); 
		if (!resource) {
			try {
				patternsStream = new FileInputStream(patternsFile);
			} catch (FileNotFoundException e) {
				throw new BugPatternLoadingException("Failed to load patterns from patterns XML file from file " + patternsFile, e);
			}
		} else {
			patternsStream = BugPatternLoader.class.getResourceAsStream(patternsURI.getPath());
			if (patternsStream == null) {
				throw new BugPatternLoadingException("Could not find patterns XML at path " + patternsStream);
			}
		}
		try {
			bugPatterns = loadPatterns(patternsStream);
		} catch (Exception e) {
			throw new BugPatternLoadingException("Failed to load patterns from patterns XML file from file " + patternsFile, e);
		}
		
		preparePatterns(bugPatterns, patternsURI, builder, symbols);
		LOGGER.info("Successfully loaded {} bug patterns from file {}", bugPatterns.getBugPatterns().size(), patternsFile);
		return bugPatterns;
	}
	
	private static BugPatterns loadPatterns(InputStream inputStream) throws JAXBException, IOException, XMLStreamException {
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

	private static void preparePatterns(BugPatterns bugPatterns, URI location, DfaAdapterBuilder builder, Collection<Symbol> symbols) {
		SpecificationDfaParser specParser = new SpecificationDfaParser(new ParsingContext());
		Function<String, DfaAdapter> loadSpecification = p -> loadDfa(p, location, specParser, builder, symbols);
		
		DfaAdapter validHandshakeLanguage = loadSpecification.apply(bugPatterns.getSpecificationLanguagePath());
		bugPatterns.setSpecificationLanguage(validHandshakeLanguage);
		
		for (BugPattern bugPattern : bugPatterns.getBugPatterns()) {
			DfaAdapter bugLanguage = loadSpecification.apply(bugPattern.getBugLanguagePath());
			bugPattern.setBugLanguage(bugLanguage);
		}
	}
	
	private static DfaAdapter loadDfa(String specificationPath, URI location, SpecificationDfaParser specParser, DfaAdapterBuilder builder, Collection<Symbol> symbols){
		LOGGER.info("Loading specification at path: {}", specificationPath);
		URI specificationLocation = location.resolve(specificationPath);
		InputStream specificationStream = BugPatternLoader.class.getResourceAsStream(specificationLocation.getPath());
		if (specificationStream == null) {
			throw new BugPatternLoadingException("Could not find specification at path " + specificationLocation.getPath());
		}
		FastDFA<SpecificationLabel> bugSpec;
		try {
			bugSpec = specParser.parseDfaModel(new InputStreamReader(specificationStream));
			DfaAdapter specAdapter = builder.fromSpecification(bugSpec, bugSpec.getInputAlphabet(), symbols);
			return specAdapter;
		} catch (FileNotFoundException e) {
			throw new BugPatternLoadingException("Could not find specification at path " + specificationLocation.getPath(), e);
		} catch (ParseException | com.alexmerz.graphviz.ParseException e) {
			throw new BugPatternLoadingException("Error parsing specifcation at path " + specificationLocation.getPath(), e);
		} 
	}
}
