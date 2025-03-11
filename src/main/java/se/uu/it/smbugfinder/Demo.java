package se.uu.it.smbugfinder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.dfa.MealySymbolExtractor;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.encoding.DefaultDFADecoder;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;
import se.uu.it.smbugfinder.sut.SUT;
import se.uu.it.smbugfinder.sut.SimulatedMealySUT;

/**
 * An interactive demo application showcasing how the state-machine-bug-finder works.
 * A user is requested first a SUT model along, the bug patterns and other inputs relevant for bug checking.
 * Each request may be skipped, in which case, default input is used.
 */
public class Demo {

    private BufferedReader in;
    private PrintStream out;
    private Deque<String> commands;

    public Demo(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
        this.commands = new ArrayDeque<String>();
    }

    public Demo() {
        this(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), System.out);
    }

    public void bufferCommands(Collection<String> commands) {
        this.commands.addAll(commands);
    }

    private String ask(String msg, boolean required) throws IOException{
        out.println(msg);
        if (!commands.isEmpty()) {
            String command = commands.remove();
            out.println(command);
            return command;
        }
        String newCommands;
        while ((newCommands = in.readLine().trim()).isEmpty() && required);
        String[] commandSplit = newCommands.split("\\s", -1);
        if (commandSplit.length > 1) {
            Arrays.stream(commandSplit, 1, commandSplit.length).forEach(cmd -> commands.add(cmd));
        }
        return commandSplit[0];
    }

    private String askOrDefault(String msg, String def) throws IOException {
        String input = ask(msg, false);
        if (input.length() == 0 || input.equals("-")) {
            input = def;
            out.println("Using (default): " + def);
        }
        return input;
    }

    private void displayIntro() {
        out.println("Welcome to the state-machine-bug-finder demo. ");
        out.println("The purpose is to showcase how the bug-finder works on user-supplied models/bug patterns.");
    }

    private InputStream getResource(String path) throws FileNotFoundException {
        InputStream resource = Demo.class.getResourceAsStream(path);
        if (resource == null) {
            resource = new FileInputStream(path);
        }
        return resource;
    }

    public void run() throws IOException, FormatException {
        displayIntro();
        String sutModel = askOrDefault("SUT model path: ", "/models/dtls/server/MbedTLS.dot");
        String patternsDir = askOrDefault("Bug patterns directory: ", "/patterns/dtls/server/");
        String sep = askOrDefault("Mealy output separator: ", "\\|");
        String noResp = askOrDefault("Mealy no response output: ", "TIMEOUT");
        String validationModel = askOrDefault("Path to Mealy machine used in validation: ", null);
        String outputDirectory = askOrDefault("Output directory: ", "output");

        InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
        InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutModelData = mealyParser.readModel(getResource(sutModel));

        BugPatternLoader loader = new BugPatternLoader(new DefaultDFADecoder());

        SymbolMapping<String, String> symbolMapping = new StringSymbolMapper(noResp, sep);
        List<Symbol> allSymbols = new ArrayList<>();
        SUT<String,String> sut = null;
        MealySymbolExtractor.extractSymbols(sutModelData.model, sutModelData.alphabet, symbolMapping, allSymbols);
        BugPatterns bp = loader.loadPatterns(patternsDir, allSymbols);
        StateMachineBugFinderCoreConfig config = new StateMachineBugFinderCoreConfig();
        if (validationModel == null) {
            config.setValidate(false);
        } else {
            config.setValidate(true);
            InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> validationModelPath = mealyParser.readModel(getResource(validationModel));
            sut = new SimulatedMealySUT<String, String>(validationModelPath.model);
        }
        StateMachineBugFinderCore<String, String> modelBugFinder = new StateMachineBugFinderCore<>(config);

        Files.createDirectories(Paths.get(outputDirectory));
        modelBugFinder.setExporter(new DFAExporter.DirectoryDFAExporter(outputDirectory));

        BugFinderResult<String, String> result = modelBugFinder.findBugs(bp, sutModelData.model, sutModelData.alphabet, symbolMapping, sut);
        export(result, outputDirectory, "bug_report.txt");
    }

    public static void main(String args []) throws IOException, FormatException {
        Demo demo = new Demo();
        demo.bufferCommands(Arrays.asList(args));
        demo.run();
    }

    private static void export(ExportableResult result, String outputDirectory, String filename) throws FileNotFoundException {
        result.doExport(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
        result.doExport(new PrintWriter(new OutputStreamWriter(new FileOutputStream(Paths.get(outputDirectory, filename).toFile()), StandardCharsets.UTF_8), true));
    }
}
