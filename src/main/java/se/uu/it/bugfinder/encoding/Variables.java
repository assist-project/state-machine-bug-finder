package se.uu.it.bugfinder.encoding;

import java.util.LinkedHashMap;

public class Variables extends LinkedHashMap<String, Variable>{
	public Variables(Variable ...variables) {
		for (Variable variable : variables) {
			put(variable.getName(), variable);
		}
	}

}
