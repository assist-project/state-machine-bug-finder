package se.uu.it.bugfinder;

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
import se.uu.it.bugfinder.bug.ModelBug;
import se.uu.it.bugfinder.dfa.InputSymbol;
import se.uu.it.bugfinder.dfa.OutputSymbol;
import se.uu.it.bugfinder.dfa.Symbol;
import se.uu.it.bugfinder.dfa.SymbolMapping;
import se.uu.it.bugfinder.encoding.DefaultDfaDecoder;
import se.uu.it.bugfinder.pattern.BugPatternLoader;
import se.uu.it.bugfinder.pattern.BugPatterns;
import se.uu.it.bugfinder.utils.MealyUtils;

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
		out.println(msg);
		if (!commands.isEmpty()) {
			return commands.remove();
		}
		String newCommands; 
		while ( (newCommands =  in.readLine().trim()).isEmpty());
		String[] commandSplit = newCommands.split("\\s");
		if (commandSplit.length > 1) {
			Arrays.stream(commandSplit, 1, commandSplit.length).forEach(cmd -> commands.add(cmd));
		}
		return commandSplit[0];
	}
	
	private String askOrDefault(String msg, String def) throws IOException {
		String input = ask(msg);
		if (input.length() == 0) {
			input = def;
			out.println("Using (default): " + def);
		} 
		return input;
	}
	
	
	private void displayIntro() {
		System.out.println("Welcome to the bug-finder demo. ");
		System.out.println("The purpose is to showcase how the bug-finder works on user-supplied models/bug patterns.");
	}
	
	public void run() throws IOException {
		displayIntro();
		String sutModel = ask("SUT Model Path: ");
		String patterns = ask("Bug Patterns .xml Path: ");
		String sep = askOrDefault("Mealy output separator: ", ",");
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> modelData = mealyParser.readModel(new File(sutModel));
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
		symbolMapping.fromInputs(modelData.alphabet, allSymbols);
		List<String> outputs = new ArrayList<>();
		MealyUtils.reachableOutputs(modelData.model, modelData.alphabet, outputs);
		symbolMapping.fromOutputs(outputs, allSymbols);
		BugPatterns bp = loader.loadPatterns(patterns, allSymbols);
		
		ModelBugFinderConfig config = new ModelBugFinderConfig();
		config.setValidate(false);
		ModelBugFinder<String, String> modelBugFinder = new ModelBugFinder<String, String>(config);
		List<ModelBug> modelBugs = new ArrayList<>();
		Statistics stats = modelBugFinder.findBugs(bp, modelData.model, modelData.alphabet, symbolMapping, null, modelBugs);
		stats.doExport(new PrintWriter(new OutputStreamWriter(System.out)));
	}
	
	

	public static void main(String args []) throws IOException {
		App app = new App();
		app.run();
	}
}
