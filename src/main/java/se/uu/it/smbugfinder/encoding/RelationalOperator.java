package se.uu.it.smbugfinder.encoding;

public enum RelationalOperator {
    EQUAL("=="),
    NOT_EQUAL("!="),
    IN("in"),
    NOT_IN("!in");

    private final String sign;

    private RelationalOperator(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }

}
