package se.uu.it.bugfinder.dfa;

public final class InputSymbol extends Symbol {

	InputSymbol(String name) {
		super(name);
	}

	@Override
	public final boolean isInput() {
		return true;
	}
	
	public String toString() {
		return "I_" + name();
	}
}
