package se.uu.it.smbugfinder.encoding;

public class RegexToken extends DescriptionToken {

    private String regexFilter;

    public RegexToken(String regexFilter) {
        this.regexFilter = regexFilter;
    }

    public String getRegexFilter() {
        return regexFilter;
    }

    public String toString() {
        return "F_" + regexFilter;
    }

    @Override
    public DescriptionType getType() {
        return DescriptionType.FILTER;
    }

}
