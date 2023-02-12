package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.Demo.BugReport;
import se.uu.it.smbugfinder.bug.StateMachineBug;
import se.uu.it.smbugfinder.dfa.MealySymbolExtractor;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;
import se.uu.it.smbugfinder.sut.SUT;
import se.uu.it.smbugfinder.sut.SimulatedMealySUT;
import se.uu.it.smbugfinder.sut.SocketSUT;

/**
 * Utility for using smbugfinder to be used to test generic network protocol implementations using a Mealy machine.
 * For validation, it assumes a test harness with which it communicates over TCP sockets, by exchanging strings representing in
 */
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String args[] ) throws FileNotFoundException, IOException {
		if (args.length > 0 && !args[0].startsWith("@")  && new File(args[0]).exists()) {
			LOGGER.info("Noticed that the first argument is a file. Processing it as an argument file.");
			args[0] = "@" + args[0];
		}

		StateMachineBugFinderToolConfig config = new StateMachineBugFinderToolConfig();
		JCommander commander = JCommander.newBuilder()
				.allowParameterOverwriting(true)
				.programName("state-machine-bug-finder")
				.addObject(config)
				.build();
		if (args.length == 0) {
			commander.usage();
		} else {
			try {
				commander.parse(args);
				launchBugFinder(config);
			} catch(ParameterException exception) {
				LOGGER.error(exception.getMessage());
				commander.usage();
			}
		}
	}

	private static void launchBugFinder(StateMachineBugFinderToolConfig config) throws FileNotFoundException, IOException {
		File dir = new File(config.getOutputDir());
		dir.mkdirs();
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutModelData = mealyParser.readModel(getResource(config.getModel()));

		BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());

		SymbolMapping<String, String> symbolMapping = new StringSymbolMapper(config.getEmptyOutput(), config.getSeparator());
		List<Symbol> allSymbols = new ArrayList<>();
		SUT<String,String> sut = null;
		MealySymbolExtractor.extractSymbols(sutModelData.model, sutModelData.alphabet, symbolMapping, allSymbols);
		BugPatterns bp = loader.loadPatterns(config.getPatterns(), allSymbols);
		StateMachineBugFinderConfig finderConfig = config.getSmBugFinderConfig();
		if (finderConfig.isValidate()) {
			if (config.getHarnessAddress() != null) {
				String[] hostPort = config.getHarnessAddress().split("\\:");
				String host = hostPort[0];
				int port = Integer.valueOf(hostPort[1]);
				Socket socket = new Socket(host, port);
				sut = new SocketSUT(socket, config.getResetMessage(), config.getResetConfirmationMessage());
			} else if (config.getValidationModel() != null) {
				InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> validationModelPath = mealyParser.readModel(getResource(config.getValidationModel()));
				sut = new SimulatedMealySUT<String, String>(validationModelPath.model);
			} else {
				throw new ConfigurationException("Unable to validate since neither the address of a test harness nor a validation model were provided");
			}
		}

		StateMachineBugFinder<String, String> modelBugFinder = new StateMachineBugFinder<String, String>(finderConfig);
		modelBugFinder.setExporter(new DFAExporter.DirectoryDFAExporter(config.getOutputDir()));
		List<StateMachineBug<String,String>> modelBugs = new ArrayList<>();
		Statistics stats = modelBugFinder.findBugs(bp, sutModelData.model, sutModelData.alphabet, symbolMapping, sut, modelBugs);
		export(stats, config.getOutputDir(), "statistics.txt");
		BugReport bugReport = new BugReport(modelBugs);
		export(bugReport, config.getOutputDir(), "bug_report.txt");

	}

	private static void export(ExportableResult result, String outputDirectory, String filename) throws FileNotFoundException {
		result.doExport(new PrintWriter(new OutputStreamWriter(System.out), true));
		result.doExport(new PrintWriter(new OutputStreamWriter(new FileOutputStream(Paths.get(outputDirectory, filename).toFile())), true));
	}

	private static InputStream getResource(String path) throws FileNotFoundException {
		InputStream resource = Main.class.getResourceAsStream(path);
		if (resource == null) {
			resource = new FileInputStream(path);
		}
		return resource;
	}
}
