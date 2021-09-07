package se.uu.it.smbugfinder.encoding;

public class Label {
	private DescriptionToken description;
	private Update update;
	private Guard guard;
	
	public Label(DescriptionToken description) {
		guard = Guard.trueGuard();
		update = Update.emptyUpdate();
		this.description = description;
	}
	
	
	public Label(DescriptionToken description, Guard guard, Update update) {
		this.guard = guard;
		this.update = update;
		this.description = description;
	}
	
	public DescriptionToken getDescription() {
		return description;
	}
	
	public Update getUpdate() {
		return update;
	}
	public Guard getGuard() {
		return guard;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((guard == null) ? 0 : guard.hashCode());
		result = prime * result + ((update == null) ? 0 : update.hashCode());
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
		Label other = (Label) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (guard == null) {
			if (other.guard != null)
				return false;
		} else if (!guard.equals(other.guard))
			return false;
		if (update == null) {
			if (other.update != null)
				return false;
		} else if (!update.equals(other.update))
			return false;
		return true;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(description.toString());
		if (update.getAssignments().length > 0) {
			builder.append("[")
			.append(update.toString());
			builder.append("]");
		}
		if (!guard.equals(Guard.trueGuard())) {
			builder.append(" where ")
			.append(guard.toString());
		}
		
		return builder.toString();
	}
	
}
