package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashMap;

public class Constants extends LinkedHashMap<String, Constant> {
    public Constants(Constant ...constants) {
        for (Constant constant : constants) {
            put(constant.getName(), constant);
        }
    }
}
