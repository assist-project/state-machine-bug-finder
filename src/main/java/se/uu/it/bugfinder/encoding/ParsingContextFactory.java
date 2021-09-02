package se.uu.it.bugfinder.encoding;

public interface ParsingContextFactory {
	public static final ParsingContextFactory EMPTY = () -> new ParsingContext();
	
	public ParsingContext newContext();
}
