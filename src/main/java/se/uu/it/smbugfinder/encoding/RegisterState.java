package se.uu.it.smbugfinder.encoding;

public class RegisterState<S> {
	private Valuation valuation;
	private S state;
	public RegisterState(S state, Valuation valuation) {
		this.valuation = valuation;
		this.state = state;
	}
	public Valuation getValuation() {
		return valuation;
	}
	public S getState() {
		return state;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((valuation == null) ? 0 : valuation.hashCode());
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
		RegisterState<?> other = (RegisterState<?>) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (valuation == null) {
			if (other.valuation != null)
				return false;
		} else if (!valuation.equals(other.valuation))
			return false;
		return true;
	}
}
