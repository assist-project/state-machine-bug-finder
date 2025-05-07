package se.uu.it.smbugfinder.bug;

import com.google.common.base.Ascii;

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

    public static BugSeverity ofString(String s) {
        return switch (Ascii.toUpperCase(s)) {
            case "LOW" -> BugSeverity.LOW;
            case "MEDIUM" -> BugSeverity.MEDIUM;
            case "HIGH" -> BugSeverity.HIGH;
            default -> BugSeverity.UNKNOWN;
        };
    }
}
