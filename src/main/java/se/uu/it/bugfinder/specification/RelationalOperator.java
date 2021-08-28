package se.uu.it.bugfinder.specification;

public enum RelationalOperator {
	EQUAL("=="),
	NOT_EQUAL("!="),
	IN("in"),
	NOT_IN("!in");
	
	private String sign;

	private RelationalOperator(String sign) {
		this.sign = sign;
	}
	
	public String getSign() {
		return sign;
	}

}
