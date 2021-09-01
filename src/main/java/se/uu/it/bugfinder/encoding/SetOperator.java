package se.uu.it.bugfinder.encoding;

public enum SetOperator {
	DIFFERENCE("-"),
	UNION("U");
	
	private String sign;

	private SetOperator(String sign) {
		this.sign = sign;
	}
	
	public String getSign() {
		return sign;
	}
}
