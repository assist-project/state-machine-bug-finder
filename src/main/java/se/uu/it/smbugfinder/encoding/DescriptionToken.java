package se.uu.it.smbugfinder.encoding;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DescriptionToken {

	public Set<DescriptionToken> getSubTokens() {
		return Collections.emptySet();
	}

	public final Set<DescriptionType> getSubTokenTypes() {
		return getSubTokens().stream().map(t -> t.getType()).collect(Collectors.toSet());
	}

	public abstract DescriptionType getType();
}
