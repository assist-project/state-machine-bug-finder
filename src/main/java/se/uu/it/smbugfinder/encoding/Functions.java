package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashMap;


public class Functions extends  LinkedHashMap<String, Function> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Functions(Function ... functions) {
        for (Function function : functions) {
            put(function.getName(), function);
        }
    }

}
