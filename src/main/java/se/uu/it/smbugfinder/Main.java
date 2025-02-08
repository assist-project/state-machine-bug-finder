package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.exception.FormatException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Console application for running the bug finder to test network protocol implementations.
 * For validation, it assumes a test harness, with which it communicates over TCP sockets.
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) throws FileNotFoundException, IOException, FormatException {
        if (args.length > 0 && !args[0].startsWith("@")  && new File(args[0]).exists()) {
            LOGGER.info("Noticed that the first argument is a file. Processing it as an argument file.");
            args[0] = "@" + args[0];
        }

        StateMachineBugFinderConfig config = new StateMachineBugFinderConfig();
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
                StateMachineBugFinder bugFinder = new StateMachineBugFinder(config);
                bugFinder.launch();
            } catch (ParameterException exception) {
                LOGGER.error(exception.getMessage());
                commander.usage();
            }
        }
    }
}
