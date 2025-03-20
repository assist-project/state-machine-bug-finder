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
        boolean leftTrue = left.eval(label, valuation);
        boolean rightTrue = right.eval(label, valuation);

        return switch(op) {
            case AND -> leftTrue && rightTrue;
            case OR -> leftTrue || rightTrue;
            default -> {
                throw new RuntimeDecodingException(String.format("Operator %s not supported", op.name()));
            }
        };
    }

    @Override
    public String toString() {
        return left + op.getSign() + right;
    }

}
