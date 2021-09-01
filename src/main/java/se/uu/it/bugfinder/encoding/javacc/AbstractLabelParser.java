package se.uu.it.bugfinder.encoding.javacc;

import se.uu.it.bugfinder.encoding.Constant;
import se.uu.it.bugfinder.encoding.Field;
import se.uu.it.bugfinder.encoding.Function;
import se.uu.it.bugfinder.encoding.ParsingContext;
import se.uu.it.bugfinder.encoding.RuntimeSpecificationException;
import se.uu.it.bugfinder.encoding.ValueExpression;
import se.uu.it.bugfinder.encoding.Variable;

public abstract class AbstractLabelParser {
	private ParsingContext context = new ParsingContext();

	public void setParsingContext(ParsingContext context) {
		this.context = context;
	}
	
	public Function resolveFunction(String identifier) {
		Function function = this.context.getFunctions().get(identifier);
		if (function == null) {
			throw new RuntimeSpecificationException("Function " + identifier + " undefined. Defined functions: " + context.getFunctions().keySet());
		}
		return function;
	} 
	
	public Field resolveField(String identifier) {
		Field field = this.context.getFields().get(identifier);
		if (field == null) {
			throw new RuntimeSpecificationException("Field " + identifier + " undefined. Defined fields: " + context.getFields().keySet());
		}
		return field;
	}
	
	public Constant resolveConstant(String identifier) {
		Constant cst = this.context.getConstants().get(identifier);
		if (cst == null) {
			throw new RuntimeSpecificationException("Constant " + identifier + " undefined. Defined constants: " + context.getConstants().keySet());
		}
		return cst;
	}
	
	public Variable resolveVariable(String identifier) {
		if (context.getVariables().containsKey(identifier)) {
			return context.getVariables().get(identifier);
		}
		Variable newVar = new Variable(identifier);
		context.getVariables().put(newVar.getName(), newVar);
		return newVar;
	}
	
	public ValueExpression resolveValueExpression(String identifier) {
		if (context.getFields().containsKey(identifier)) {
			return context.getFields().get(identifier);
		}
		
		if (context.getConstants().containsKey(identifier)) {
			return context.getConstants().get(identifier);
		}
		
		if (context.getVariables().containsKey(identifier)) {
			return context.getVariables().get(identifier);
		}
		
		Variable newVar = new Variable(identifier);
		context.getVariables().put(newVar.getName(), newVar);
		
		return newVar;
	}
}
