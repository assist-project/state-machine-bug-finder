package se.uu.it.bugfinder.encoding;

public enum LogicalOperator {
	AND("and"),
	OR("or");
	
	private String sign;

	private LogicalOperator(String sign) {
		this.sign = sign;
	}
	
	public String getSign() {
		return sign;
	}
}
