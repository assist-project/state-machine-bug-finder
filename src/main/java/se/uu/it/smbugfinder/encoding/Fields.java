package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashMap;

public class Fields extends LinkedHashMap<String, Field> {
	
	public Fields(Field ... fields) {
		super();
		for (Field field : fields) {
			put(field.getName(), field);
		}
	}
}