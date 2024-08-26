package se.uu.it.smbugfinder.encoding;

public enum SetOperator {
    DIFFERENCE("-"),
    UNION("U");

    private final String sign;

    private SetOperator(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
