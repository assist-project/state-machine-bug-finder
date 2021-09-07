package se.uu.it.smbugfinder.encoding;

public interface ParsingContextFactory {
	public static final ParsingContextFactory EMPTY = () -> new ParsingContext();
	
	public ParsingContext newContext();
}
