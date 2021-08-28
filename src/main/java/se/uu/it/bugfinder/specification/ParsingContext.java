package se.uu.it.bugfinder.specification;

public class ParsingContext {
	
	private Constants constants;
	private Fields fields;
	private Functions functions;
	private Variables variables;
	
	public ParsingContext() {
		constants = new Constants();
		fields = new Fields();
		functions = new Functions();
		variables = new Variables();
	}

	public ParsingContext(Constants constants, Fields fields, Functions functions) {
		this.constants = constants;
		this.fields = fields;
		this.functions = functions;
		variables = new Variables();
	}

	public Constants getConstants() {
		return constants;
	}

	public Fields getFields() {
		return fields;
	}

	public Functions getFunctions() {
		return functions;
	}
	
	public Variables getVariables() {
		return variables;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		return builder.append("Constants: ").append(constants.keySet()).append(System.lineSeparator())
		.append("Fields: ").append(fields.keySet()).append(System.lineSeparator())
		.append("Functions: ").append(functions.keySet()).append(System.lineSeparator())
		.append("Variables: ").append(variables.keySet()).toString();
		
	}
}
