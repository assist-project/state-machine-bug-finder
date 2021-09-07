package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uu.it.smbugfinder.dfa.DfaAdapter;

public interface DfaExporter {
	static final Logger LOGGER = LoggerFactory.getLogger(DfaExporter.class);
	
	public void exportDfa(DfaAdapter spec, String name);
	
	public static class DirectoryDfaExporter implements DfaExporter {
		private String outputDir;

		public DirectoryDfaExporter(String outputDir) {
			this.outputDir = outputDir;
		}
		
		public void exportDfa(DfaAdapter spec, String name) {
			try {
				spec.export(new FileWriter(new File(outputDir, name)));
			} catch (IOException e) {
				LOGGER.error("Could not export {}", name);
				LOGGER.error(e.getLocalizedMessage());
			}
		}
	} 
	
	public static class StreamDfaExporter implements DfaExporter {
		private PrintWriter writer;

		public StreamDfaExporter(OutputStream out) {
			this.writer = new PrintWriter(new OutputStreamWriter(out));
		}

		@Override
		public void exportDfa(DfaAdapter spec, String name) {
			
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
