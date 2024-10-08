package se.uu.it.smbugfinder.encoding;

public enum LogicalOperator {
    AND("and"),
    OR("or");

    private final String sign;

    private LogicalOperator(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
