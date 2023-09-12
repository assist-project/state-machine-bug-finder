package se.uu.it.smbugfinder.encoding;

import java.io.Serial;
import java.util.LinkedHashMap;

public class Constants extends LinkedHashMap<String, Constant> {

    @Serial
    private static final long serialVersionUID = 1L;

    public Constants(Constant ...constants) {
        for (Constant constant : constants) {
            put(constant.getName(), constant);
        }
    }
}
