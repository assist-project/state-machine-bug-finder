package se.uu.it.smbugfinder;

/**
 * Used to run SMBugFinder in debug modes, triggering specific functionalities.
 */
public enum DebugMode {
	/* debug option used assess the effectiveness of specialized bug patterns in finding bugs covered by the general bug patterns*/
	EVALUATE_SPECIFIC_BUG_PATTERNS,
	/* counts the number of witnesses that would be generated for each bug pattern */ 
	COUNT_GENERATED_WITNESSES;
	
	public boolean isEnabled(StateMachineBugFinderConfig config) {
		return this.equals(config.getDebugMode());
	}
}
