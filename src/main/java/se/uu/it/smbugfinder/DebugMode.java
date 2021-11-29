package se.uu.it.smbugfinder;

public enum DebugMode {
	/* debug option used assess the effectiveness of specialized bug patterns in finding bugs covered by the general bug patterns*/
	EVALUATE_SPECIFIC_BUG_PATTERNS;
	
	public boolean isEnabled(StateMachineBugFinderConfig config) {
		return this.equals(config.getDebugMode());
	}
}
