package se.uu.it.smbugfinder.encoding;

import java.util.Collection;

import se.uu.it.smbugfinder.dfa.Symbol;

public class RelationalExpression  implements BooleanExpression {

	private ValueExpression expr1, expr2;
	private RelationalOperator op;
	
	public RelationalExpression(ValueExpression expr1, RelationalOperator op, ValueExpression expr2) {
		this.expr1 = expr1;
		this.op = op;
		this.expr2 = expr2;
	}
	
	public boolean eval(Symbol symbol, Valuation valuation) {
		Value value1 = expr1.eval(symbol, valuation);
		Value value2 = expr2.eval(symbol, valuation);
		switch(op) {
		case EQUAL:
			return value1.equals(value2);
		case NOT_EQUAL:
			return !value1.equals(value2);
		case IN:
		case NOT_IN:
			Collection<?> collection = value2.getStoredValue(Collection.class);
			boolean contains = false;
			if (value1.getStoredValue() instanceof Collection) {
				contains = collection.containsAll(value1.getStoredValue(Collection.class));
			} else {
				contains = collection.contains(value1.getStoredValue());
			}
			
			if (op == RelationalOperator.IN) {
				return contains;
			} else {
				return !contains;
			}
		default:
			throw new RuntimeDecodingException(String.format("Operation %s not supported", op.name()));
		}
	}
	
	public String toString() {
		return expr1.toString() + " " + op.getSign() + " " + expr2.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr1 == null) ? 0 : expr1.hashCode());
		result = prime * result + ((expr2 == null) ? 0 : expr2.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		RelationalExpression other = (RelationalExpression) obj;
		if (expr1 == null) {
			if (other.expr1 != null)
				return false;
		} else if (!expr1.equals(other.expr1))
			return false;
		if (expr2 == null) {
			if (other.expr2 != null)
				return false;
		} else if (!expr2.equals(other.expr2))
			return false;
		if (op != other.op)
			return false;
		return true;
	}
}
