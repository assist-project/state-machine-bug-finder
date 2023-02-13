package se.uu.it.smbugfinder.encoding;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetExpressionToken extends DescriptionToken {

	private DescriptionToken left;
	private SetOperator operator;
	private DescriptionToken right;

	public SetExpressionToken(DescriptionToken left, SetOperator operator, DescriptionToken right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public SetOperator getOperator() {
		return operator;
	}

	public DescriptionToken getLeft() {
		return left;
	}

	public DescriptionToken getRight() {
		return right;
	}

	public Set<DescriptionToken> getSubTokens() {
		Set<DescriptionToken> subTokens = new LinkedHashSet<>();
		subTokens.add(left);
		subTokens.add(right);
		return subTokens;
	}

	@Override
	public DescriptionType getType() {
		return DescriptionType.BINARY_EXPRESSION;
	}

	@Override
	public String toString() {
		return left.toString() + " " + operator.getSign() + " " + right.toString();
	}
}
