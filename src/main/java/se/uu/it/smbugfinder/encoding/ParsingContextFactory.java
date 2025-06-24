package se.uu.it.smbugfinder.encoding;


/* () -> new ParsingContext() is shorthand for implementing the newContext() method
   in an anonymous implementation of ParsingContextFactory. Therefore, EMPTY
   effectively provides a newContext() method that returns a new instance of ParsingContext.
   Even though EMPTY's definition does not explicitly call newContext(), any code using
   EMPTY.newContext() will trigger the lambda, creating and returning a ParsingContext object.
*/


public interface ParsingContextFactory {
    public static final ParsingContextFactory EMPTY = () -> new ParsingContext();

    public ParsingContext newContext();
}
