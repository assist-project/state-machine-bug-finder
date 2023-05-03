package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public interface TokenMatcher {
    public boolean match(Symbol symbol, DescriptionToken token);

    /**
     * Returns the matching atomic token or null in case there is no match.
     */
    public DescriptionToken matchingAtomicToken(Symbol symbol, DescriptionToken token);
}
