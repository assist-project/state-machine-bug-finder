package se.uu.it.smbugfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import se.uu.it.smbugfinder.bug.StateMachineBug;
import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.MealyToDfaSymbolExtractor;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.dfa.SymbolMapping;
import se.uu.it.smbugfinder.encoding.DefaultDfaDecoder;
import se.uu.it.smbugfinder.pattern.BugPatternLoader;
import se.uu.it.smbugfinder.pattern.BugPatterns;
import se.uu.it.smbugfinder.sut.SUT;
import se.uu.it.smbugfinder.sut.SimulatedMealySUT;

public class App {

	private BufferedReader in;
	private PrintStream out;
	private Deque<String> commands;
	
	public App(BufferedReader in, PrintStream out) {
		this.in = in;
		this.out = out;
		this.commands = new ArrayDeque<String>();
	}
	
	public App() {
		this(new BufferedReader(new InputStreamReader(System.in)), System.out);
	}
	
	public void bufferCommands(Collection<String> commands) {
		this.commands.addAll(commands);
	}
	
	private String ask(String msg) throws IOException{
		return ask(msg, true);
	}
	
	private String ask(String msg, boolean required) throws IOException{
		out.println(msg);
		if (!commands.isEmpty()) {
			String command = commands.remove();
			out.println(command);
			return command;
		}
		String newCommands; 
		while ( (newCommands =  in.readLine().trim()).isEmpty() && required);
		String[] commandSplit = newCommands.split("\\s");
		if (commandSplit.length > 1) {
			Arrays.stream(commandSplit, 1, commandSplit.length).forEach(cmd -> commands.add(cmd));
		}
		return commandSplit[0];
	}
	
	private String askOrDefault(String msg, String def) throws IOException {
		String input = ask(msg, false);
		if (input.length() == 0) {
			input = def;
			out.println("Using (default): " + def);
		} 
		return input;
	}
	
	
	private void displayIntro() {
		out.println("Welcome to the bug-finder demo. ");
		out.println("The purpose is to showcase how the bug-finder works on user-supplied models/bug patterns.");
	}
	
	public void run() throws IOException {
		displayIntro();
		String sutModel = ask("SUT Model Path: ");
		String patterns = ask("Bug Patterns .xml Path: ");
		String sep = askOrDefault("Mealy output separator: ", ",");
		String validationModel = askOrDefault("Validation Model Path: ", sutModel);
		
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutModelData = mealyParser.readModel(new File(sutModel));
		
		BugPatternLoader loader = new BugPatternLoader(new DefaultDfaDecoder());
		
		SymbolMapping<String, String> symbolMapping = new SymbolMapping<String, String>() {
			@Override
			public String toInput(InputSymbol symbol) {
				return symbol.name();
			}

			@Override
			public String toOutput(OutputSymbol symbol) {
				return symbol.name();
			}

			@Override
			public String toOutput(Collection<OutputSymbol> symbols) {
				StringBuilder builder = new StringBuilder();
				for (OutputSymbol symbol : symbols) {
					builder.append(symbol.name() + sep);
				}
				return builder.substring(0, builder.length() - sep.length()).toString();
			}

			@Override
			public InputSymbol fromInput(String input) {
				return new InputSymbol(input);
			}

			@Override
			public List<OutputSymbol> fromOutput(String output) {
				return Arrays.stream(output.split(sep)).map(s -> new OutputSymbol(s)).collect(Collectors.toList());
			}
		};
		List<Symbol> allSymbols = new ArrayList<>();
		SUT<String,String> sut = null;
		MealyToDfaSymbolExtractor.extractSymbols(sutModelData.model, sutModelData.alphabet, symbolMapping, allSymbols);
		BugPatterns bp = loader.loadPatterns(patterns, allSymbols);
		StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
		if (validationModel.isEmpty()) {
			config.setValidate(false);
		} else {
			config.setValidate(true);
			InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> validationModelPath = mealyParser.readModel(new File(validationModel));
			sut = new SimulatedMealySUT<String, String>(validationModelPath.model);
		}
		StateMachineBugFinder<String, String> modelBugFinder = new StateMachineBugFinder<String, String>(config);
		modelBugFinder.setExporter(new DfaExporter.StreamDfaExporter(System.out));
		List<StateMachineBug> modelBugs = new ArrayList<>();
		Statistics stats = modelBugFinder.findBugs(bp, sutModelData.model, sutModelData.alphabet, symbolMapping, sut, modelBugs);
		stats.doExport(new PrintWriter(new OutputStreamWriter(System.out)));
	}
	
	

	public static void main(String args []) throws IOException {
		App app = new App();
		app.bufferCommands(Arrays.asList(args));
		app.run();
	}
}
