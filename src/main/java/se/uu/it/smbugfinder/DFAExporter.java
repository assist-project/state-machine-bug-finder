package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uu.it.smbugfinder.dfa.DFAAdapter;

public interface DFAExporter {
    static final Logger LOGGER = LoggerFactory.getLogger(DFAExporter.class);

    public void exportDfa(DFAAdapter spec, String name);

    public static class DirectoryDFAExporter implements DFAExporter {
        private String outputDir;

        public DirectoryDFAExporter(String outputDir) {
            this.outputDir = outputDir;
        }

        @Override
        public void exportDfa(DFAAdapter spec, String name) {
            try {
                spec.export(new FileWriter(new File(outputDir, name), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOGGER.error("Could not export {}", name);
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }

    public static class StreamDFAExporter implements DFAExporter {
        private PrintWriter writer;

        public StreamDFAExporter(OutputStream out) {
            this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        }

        @Override
        public void exportDfa(DFAAdapter spec, String name) {

            writer.println("============");
            writer.println("");
            writer.println("DFA " + name);
            writer.println("");
            writer.println("============");
            try {
                spec.export(writer);
            } catch (IOException e) {
                writer.println("Failure exporting model");
            }
        }
    }
}
