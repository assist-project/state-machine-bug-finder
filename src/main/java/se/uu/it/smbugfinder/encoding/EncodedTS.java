package se.uu.it.smbugfinder.encoding;

import java.util.Collection;

import net.automatalib.ts.acceptor.DeterministicAcceptorTS;

/**
 * Interface which class representing encoded transition systems.
 */
public interface EncodedTS {
    /**
     * Returns a TS where transitions are labels compactly capturing transitions/states in the encoded TS.
     */
    DeterministicAcceptorTS<?, Label> getEncodedTS();
    /**
     * Returns all the labels in the TS.
     */
    public Collection<Label> getLabels();
}
