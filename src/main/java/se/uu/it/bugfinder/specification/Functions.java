package se.uu.it.bugfinder.specification;

import java.util.LinkedHashMap;


public class Functions extends  LinkedHashMap<String, Function> {
	public Functions(Function ... functions) {
		for (Function function : functions) {
			put(function.getName(), function);
		} 
	}

}
