package se.uu.it.bugfinder.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.EnumMap;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.automata.fsa.impl.FastDFA;
import se.uu.it.bugfinder.dfa.DfaAdapter;
import se.uu.it.dtlsfuzzer.ClientCertAuth;
import se.uu.it.dtlsfuzzer.config.BugPatternSpecificationConfig;
import se.uu.it.dtlsfuzzer.config.BugPatternSpecificationServerConfig;
import se.uu.it.dtlsfuzzer.specification.SpecificationLabel;
import se.uu.it.dtlsfuzzer.specification.SpecificationLanguageParser;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsSymbolMapping;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsLanguageAdapter;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsLanguageAdapterBuilder;
import se.uu.it.dtlsfuzzer.specification.dtls.DtlsParsingContext;

public class BugPatternLoader {
	private static final Logger LOGGER = LogManager.getLogger(BugPatternLoader.class);
	
	public static final String CLIENT_PATTERNS = "/bugpatterns/clients/";
	private static final EnumMap<ClientCertAuth, String> SERVER_PATTERNS = new EnumMap<ClientCertAuth, String>(ClientCertAuth.class);
	static {
		SERVER_PATTERNS.put(ClientCertAuth.REQUIRED, "/bugpatterns/servers/required/");
	}
	
	public static String PATTERN_FILE = "patterns.xml";
	
	private static JAXBContext context;

	private static synchronized JAXBContext getJAXBContext()
			throws JAXBException, IOException {
		if (context == null) {
			context = JAXBContext.newInstance(BugPatterns.class,
					AbstractBugPattern.class, ClientCertAuth.class);
		}
		return context;
	}
	
	public static BugPatterns loadPatternsFromFile(String patternsFile, DtlsSymbolMapping mapping) throws BugPatternLoadingException {
		BugPatterns bugPatterns = null;
		LOGGER.info("Loading bug patterns");
		URI patternsURI; 
		InputStream patternsStream;
		patternsURI = new File(patternsFile).getParentFile().toURI();
		bugPatterns = loadPatterns(patternsStream);
		
		
		
		if (config.getPatterns() == null) {
			
			if (config.isClient()) {
				patternsURI = URI.create(CLIENT_PATTERNS);
			} else {
				String serverLoc = SERVER_PATTERNS.get(((BugPatternSpecificationServerConfig)config).getClientCertAuth());
				if (serverLoc == null) {
					throw new NotImplementedException("Bug patterns not available for " + ((BugPatternSpecificationServerConfig)config).getClientCertAuth() + " cert");
				}
				patternsURI = URI.create(serverLoc); 
			}
			String patternsXmlPath = patternsURI.resolve(PATTERN_FILE).getPath();
			patternsStream = BugPatternLoader.class.getResourceAsStream(patternsXmlPath);
			if (patternsStream == null) {
				throw new BugPatternLoadingException("Could not find patterns XML at path " + patternsStream);
			}
			try {
				bugPatterns = loadPatterns(patternsStream);
			} catch (Exception e) {
				throw new BugPatternLoadingException("Failed to load patterns from patterns XML file at path " + patternsXmlPath, e);
			}
		} else {
			try {
				patternsStream = new FileInputStream(config.getPatterns());
			} catch (FileNotFoundException e) {
				throw new BugPatternLoadingException("Failed to load patterns from patterns XML file at path " + config.getPatterns(), e);
			}
			patternsURI = new File(config.getPatterns()).getParentFile().toURI();
			try {
				bugPatterns = loadPatterns(patternsStream);
			} catch (Exception e) {
				throw new BugPatternLoadingException("Failed to load patterns from patterns XML file at path " + config.getPatterns(), e);
			}
		}
		
		preparePatterns(bugPatterns, patternsURI, config, mapping);
		LOGGER.info("Successfully loaded {} bug patterns", bugPatterns.getBugPatterns().size());
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

	private static void preparePatterns(BugPatterns bugPatterns, URI location, BugPatternSpecificationConfig config, DtlsSymbolMapping mapping) {
		SpecificationLanguageParser specParser = new SpecificationLanguageParser(new DtlsParsingContext());
		Function<String, DtlsLanguageAdapter> loadLanguage = p -> loadLanguage(p, location, specParser, mapping, config.isClient());
		
		DtlsLanguageAdapter validHandshakeLanguage = loadLanguage.apply(bugPatterns.getSpecificationLanguagePath());
		bugPatterns.setSpecificationLanguage(validHandshakeLanguage);
		
		for (BugPattern bugPattern : bugPatterns.getBugPatterns()) {
			DtlsLanguageAdapter bugLanguage = loadLanguage.apply(bugPattern.getBugLanguagePath());
			bugPattern.setBugLanguage(bugLanguage);
		}
	}
	
	private static DfaAdapter loadLanguage(String languagePath, URI location, SpecificationLanguageParser specParser, DtlsSymbolMapping mapping, boolean client) {
		LOGGER.info("Loading language at path: {}", languagePath);
		URI languageLocation = location.resolve(languagePath);
		InputStream languageStream = BugPatternLoader.class.getResourceAsStream(languageLocation.getPath());
		if (languageStream == null) {
			throw new BugPatternLoadingException("Could not find language at path " + languageLocation.getPath());
		}
		FastDFA<SpecificationLabel> bugSpec;
		try {
			bugSpec = specParser.parseDfaModel(new InputStreamReader(languageStream));
			DfaAdapter specAdapter = DfaBuilder.fromSpecification(bugSpec, mapping);
			return specAdapter;
		} catch (FileNotFoundException e) {
			throw new BugPatternLoadingException("Could not find language at path " + languageLocation.getPath(), e);
		} catch (ParseException | se.uu.it.dtlsfuzzer.specification.javacc.ParseException e) {
			throw new BugPatternLoadingException("Failed to parse language at path " + languageLocation.getPath(), e);
		} 
	}
}
