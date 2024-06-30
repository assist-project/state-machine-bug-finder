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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.DFAExporter.DirectoryDFAExporter;
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
 * StateMachineBugFinder or SMBugFinder in short, is a library for automating the detection of state machine bugs in SUTs.
 * It takes as input the Mealy machine model of the SUT and a catalog of bugs expressed as DFAs.
 * It then automatically detects the DFA-encoded bugs on the Mealy machine, generating witnesses exposing the violations.
 * The witnesses can optionally be validated by executing the corresponding inputs on the SUT.
 * This last step requires a test harness.
 *
 * @author Paul
 *
 */
public class StateMachineBugFinder {
    private static final String BUG_REPORT = "bug_report.tx";
    private static final String WITNESS_FOLDER = "bug_witnesses";

    private final StateMachineBugFinderConfig config;

    /**
     * @param config configuration containing the bug finder config, plus other options relevant when running the bug finder from the console.
     */
    public StateMachineBugFinder(StateMachineBugFinderConfig config) {
        this.config = config;
    }


    /**
     * Launches the bug finder, creating an output directory containing the results (statistics, bugs, generated bug patterns, statistics, executable witnesses).
     * @throws FileNotFoundException if the directory to save files cannot be found
     * @throws IOException           if there a problen when writing files
     */
    public BugFinderResult<String, String>  launch() throws IOException {
        Files.createDirectories(Paths.get(config.getOutputDir()));
        DirectoryDFAExporter exporter = new DFAExporter.DirectoryDFAExporter(config.getOutputDir());
        BugFinderResult<String, String> result = launch(exporter);
        export(result, config.getOutputDir());
        return result;
    }

    /**
     * Launches the bug finder using a custom {@link DFAExporter}.
     * @param exporter - if not null, will receive for export the bugs detected by the bug finder.
     * @return result containing the bugs found plus statistics of the bug detection experiment.
     */
    public BugFinderResult<String, String> launch(@Nullable DFAExporter exporter) throws IOException {
        InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
        InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutModelData = mealyParser.readModel(getResource(config.getModel()));

        BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());

        SymbolMapping<String, String> symbolMapping = new StringSymbolMapper(config.getEmptyOutput(), config.getSeparator());
        List<Symbol> allSymbols = new ArrayList<>();
        SUT<String,String> sut = null;
        MealySymbolExtractor.extractSymbols(sutModelData.model, sutModelData.alphabet, symbolMapping, allSymbols);
        BugPatterns bp = loader.loadPatterns(config.getPatterns(), allSymbols);
        StateMachineBugFinderCoreConfig finderConfig = config.getSmBugFinderConfig();
        if (finderConfig.isValidate()) {
            if (config.getHarnessAddress() != null) {
                String[] hostPort = config.getHarnessAddress().split("\\:", -1);
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                Socket socket = new Socket(host, port);
                sut = new SocketSUT(socket, config.getResetMessage(), config.getResetConfirmationMessage());
            } else if (config.getValidationModel() != null) {
                InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> validationModelPath = mealyParser.readModel(getResource(config.getValidationModel()));
                sut = new SimulatedMealySUT<String, String>(validationModelPath.model);
            } else {
                throw new ConfigurationException("Unable to validate since neither the address of a test harness nor a validation model were provided");
            }
        }

        StateMachineBugFinderCore<String, String> modelBugFinder = new StateMachineBugFinderCore<>(finderConfig);
        if (exporter != null) {
            modelBugFinder.setExporter(exporter);
        }
        BugFinderResult<String, String> result  = modelBugFinder.findBugs(bp, sutModelData.model, sutModelData.alphabet, symbolMapping, sut);
        return result;
    }

    private void export(BugFinderResult<String, String> result, String outputDirectory) throws IOException {
        result.doExport(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
        result.doExport(new PrintWriter(new OutputStreamWriter(new FileOutputStream(Paths.get(outputDirectory, BUG_REPORT).toFile()), StandardCharsets.UTF_8), true));
        File witnessFolder = Paths.get(outputDirectory, WITNESS_FOLDER).toFile();
        if (witnessFolder.exists()) {
            if (!witnessFolder.isDirectory()) {
                throw new RuntimeException("File %s already exists and is not a folder".formatted(witnessFolder.toString()));
            }
        } else {
            if (!witnessFolder.mkdirs()) {
                throw new RuntimeException("Failed to create witness folder");
            }
        }
        result.generateExecutableWitnesses(witnessFolder);
    }

    private InputStream getResource(String path) throws FileNotFoundException {
        InputStream resource = StateMachineBugFinder.class.getResourceAsStream(path);
        if (resource == null) {
            resource = new FileInputStream(path);
        }
        return resource;
    }
}
