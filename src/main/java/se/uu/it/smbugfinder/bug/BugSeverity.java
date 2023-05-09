package se.uu.it.smbugfinder.bug;

public enum BugSeverity {
    /**
     * Unknown impact.
     */
    UNKNOWN,
    /**
     * Non-conformance bug with negligible security or negligible to minor functional impact.
     */
    LOW,
    /**
     * Non-conformance bug with significant functional impact or minor security impact.
     */
    MEDIUM,
    /**
     * Non-conformance bug with significant security impact.
     */
    HIGH;
}
