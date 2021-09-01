package se.uu.it.bugfinder.encoding;

import java.util.Arrays;
import java.util.List;

import se.uu.it.bugfinder.dfa.Symbol;

public class FunctionInvocation implements ValueExpression {
	private ValueExpression[] parameters;
	private Function function;

	public FunctionInvocation(Function function, ValueExpression ... parameters) {
		this.function = function;
		this.parameters = parameters;
		if (function.getNumArgs() != parameters.length) {
			throw new RuntimeSpecificationException("Invalid number of parameters in function invokation");
		}
	}
	
	public FunctionInvocation(Function function, List<ValueExpression> parameters) {
		this(function, parameters.toArray(new ValueExpression[parameters.size()]));
	}
	
	@Override
	public Value eval(Symbol symbol, Valuation valuation) {
		Value [] args = new Value [parameters.length];
		
		for (int i=0; i< parameters.length; i++) {
			args[i] = parameters[i].eval(symbol, valuation);
		}
		
		Value result = function.invoke(symbol, args);
		
		return result;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(function.getName());
		builder.append("(");
		if (parameters.length > 0) {
			for (ValueExpression exp : parameters) {
				builder.append(exp.toString()).append(",");
			}
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append(")");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + Arrays.hashCode(parameters);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionInvocation other = (FunctionInvocation) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		return true;
	}
}
