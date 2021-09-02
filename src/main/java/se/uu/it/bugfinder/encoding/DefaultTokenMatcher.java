package se.uu.it.bugfinder.encoding;

import se.uu.it.bugfinder.dfa.Symbol;

public class DefaultTokenMatcher implements TokenMatcher {

	@Override
	public boolean match(Symbol symbol, DescriptionToken description) {
		boolean matches = false;
		DescriptionToken matchingToken = matchingAtomicToken(symbol, description);
		matches = matchingToken != null;
		return matches;
	}

	/**
	 * In case a symbol matches a token, return the atomic token that it is matched to. 
	 */
	@Override
	public DescriptionToken matchingAtomicToken(Symbol symbol, DescriptionToken description) {
		DescriptionToken matchingToken = null;
		switch(description.getType()) {
		case SYMBOL:
			SymbolToken symbolToken = (SymbolToken) description;
			if ( (symbolToken.isInput() == symbol.isInput()) && symbolToken.getSymbolString().equals(symbol.name()) ) {
				matchingToken = symbolToken;
			}
			break;
		case FILTER:
			RegexToken regexDescription = (RegexToken) description;
			if (symbol.toString().matches(regexDescription.getRegexFilter())) {
				matchingToken = regexDescription;
			}
			break;
		case BINARY_EXPRESSION:
			SetExpressionToken expressionToken = (SetExpressionToken) description;
			SetOperator operation = expressionToken.getOperator();
			DescriptionToken leftMatchingToken = matchingAtomicToken(symbol, expressionToken.getLeft());
			DescriptionToken rightMatchingToken = matchingAtomicToken(symbol, expressionToken.getRight());
			switch (operation) {
			case DIFFERENCE:
				if (leftMatchingToken != null && rightMatchingToken == null) {
					matchingToken = leftMatchingToken;
				}
				break;
			case UNION:
				if ( leftMatchingToken != null) {
					matchingToken = leftMatchingToken;
				} else {
					if (rightMatchingToken != null) {
						matchingToken = rightMatchingToken;
					}
				}
				break;
			default:
				throw new RuntimeDecodingException(String.format("Unsupported binary operation type %s", operation.name()));
			}
			break;
			
		case ENUMERATION:
			EnumerationToken enumerationDescription = (EnumerationToken) description;
			for (DescriptionToken token : enumerationDescription.getSubTokens()) {
				matchingToken = matchingAtomicToken(symbol, token);
				if (matchingToken != null) {
					break;
				}
			}
			break;
		case OTHER:
			OtherToken otherToken = (OtherToken) description;
			switch(otherToken.getOtherTokenType()) {
			case ALL:
				matchingToken = otherToken;
				break;
			case INPUT:
				if ( symbol.isInput() ) {
					matchingToken = otherToken;
				}
				break;
			case OUTPUT:
				if ( !symbol.isInput()) {
					matchingToken = otherToken;
				}
				break;
			default:
				throw new RuntimeDecodingException(String.format("Unsupported other token type %s", otherToken.getOtherTokenType().name()));
			}
			break;
		default:
			throw new RuntimeDecodingException(String.format("Unsupported symbol description type %s", description.getType().name())); 
			
		}
		
		return matchingToken;
	}

}
