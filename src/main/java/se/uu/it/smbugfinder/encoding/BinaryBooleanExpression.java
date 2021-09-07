package se.uu.it.smbugfinder.encoding;

import se.uu.it.smbugfinder.dfa.Symbol;

public class BinaryBooleanExpression implements BooleanExpression {
	
	private BooleanExpression left;
	private LogicalOperator op;
	private BooleanExpression right;

	public BinaryBooleanExpression(BooleanExpression left, LogicalOperator op, BooleanExpression right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public boolean eval(Symbol label, Valuation valuation) {
		boolean result = false;
		boolean leftTrue = left.eval(label, valuation);
		boolean rightTrue = right.eval(label, valuation);
		switch(op) {
		case AND:
			result = leftTrue && rightTrue;
			break;
		case OR:
			result = leftTrue || rightTrue;
			break;
		default:
			throw new RuntimeDecodingException(String.format("Operator %s not supported", op.name()));
		}
		
		return result;
	}

	@Override
	public String toString() {
		return left +  op.getSign() + right;
	}

}
