package se.uu.it.bugfinder.specification;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EnumerationToken extends DescriptionToken {
	private List<? extends DescriptionToken> descriptionToken;
	
	public EnumerationToken(List<? extends DescriptionToken> descriptionToken) {
		this.descriptionToken = descriptionToken;
	}
	
	@Override
	public Set<DescriptionToken> getSubTokens() {
		return new LinkedHashSet<>(descriptionToken);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		descriptionToken.forEach(t -> builder.append(t.toString()).append(", "));
		builder.delete(builder.length()-2, builder.length());
		return builder.append("}").toString();
	}

	@Override
	public DescriptionType getType() {
		return DescriptionType.ENUMERATION;
	}
}
