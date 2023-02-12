package se.uu.it.smbugfinder.encoding;

import java.util.Arrays;

import se.uu.it.smbugfinder.dfa.Symbol;

public abstract class Function {

	private String name;
	private int numArgs;

	public Function(String name, int numArgs) {
		this.name = name;
		this.numArgs = numArgs;
	}

	public Value invoke(Symbol symbol, Value ... arguments) {
		if (arguments.length != numArgs) {
			throw new RuntimeDecodingException(String.format("Function %s invoked with an invalid number of arguments (%d instead of %d)", name, arguments.length, numArgs));
		}

		return doInvoke(symbol, arguments);
	}

	public int getNumArgs() {
		return numArgs;
	}

	public String getName() {
		return name;
	}

	protected abstract Value doInvoke(Symbol symbol, Value ... arguments);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numArgs;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Function other = (Function) obj;
		if (numArgs != other.numArgs)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	protected void invalidInvocation(Value ... arguments) {
		throw new RuntimeDecodingException(String.format("Function %s was invoked with invalid arguments %s", this.getName(), Arrays.toString(arguments)));
	}

}
