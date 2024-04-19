package se.uu.it.smbugfinder.pattern;

import se.uu.it.smbugfinder.bug.BugSeverity;
import se.uu.it.smbugfinder.dfa.DFAAdapter;

public class UncategorizedBugPattern extends AbstractBugPattern {
    private static UncategorizedBugPattern INSTANCE;

    public static final synchronized UncategorizedBugPattern getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UncategorizedBugPattern();
        }
        return INSTANCE;
    }

    private UncategorizedBugPattern() {
        super("Uncategorized", "Uncategorized behavior which does not conform to the specification.",
                BugSeverity.UNKNOWN);
    }

    @Override
    DFAAdapter doGenerateBugLanguage() {
        throw new RuntimeException("Uncategorized bug pattern does not provide a bug language.");
    }

    @Override
    public boolean isGeneral() {
        return true;
    }

}
