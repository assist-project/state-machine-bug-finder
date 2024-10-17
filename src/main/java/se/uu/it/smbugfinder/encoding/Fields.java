package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashMap;

public class Fields extends LinkedHashMap<String, Field> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected void initialize(Field ... fields) {
        for (Field field : fields) {
            put(field.getName(), field);
        }
    }
}
