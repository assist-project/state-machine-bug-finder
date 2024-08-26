package se.uu.it.smbugfinder.pattern;

public class BugPatternLoadingException extends RuntimeException{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BugPatternLoadingException(Exception exception) {
        super(exception);
    }

    public BugPatternLoadingException(String message) {
        super(message);
    }

    public BugPatternLoadingException(String message, Exception exception) {
        super(message, exception);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        return sb.append("Failed to load bug patterns.")
        .append(System.lineSeparator())
        .append(super.getMessage())
        .toString();
    }
}
