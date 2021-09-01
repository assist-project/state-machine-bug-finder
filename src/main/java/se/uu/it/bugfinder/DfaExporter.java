package se.uu.it.bugfinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.uu.it.bugfinder.dfa.DfaAdapter;

public interface DfaExporter {
	static final Logger LOGGER = LogManager.getLogger(DfaExporter.class);
	
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
}
