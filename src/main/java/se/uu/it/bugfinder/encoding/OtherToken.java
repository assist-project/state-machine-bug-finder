package se.uu.it.bugfinder.encoding;

public class OtherToken extends DescriptionToken {
	
	private OtherTokenType otherTokenType;
	
	public OtherToken() {
		this(OtherTokenType.ALL);
	}

	public OtherToken(OtherTokenType type) {
		this.otherTokenType = type;
	}

	@Override
	public DescriptionType getType() {
		return DescriptionType.OTHER;
	}
	
	public OtherTokenType getOtherTokenType() {
		return otherTokenType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("other");
		switch(otherTokenType) {
		case ALL: break;
		case INPUT: sb.append("_input"); break;
		case OUTPUT: sb.append("_output"); break;
		}
		return sb.toString();
	}

}
