package se.uu.it.smbugfinder.pattern;

import javax.xml.bind.annotation.XmlElement;

public class Pattern {
	public Pattern() {
	}

	protected Pattern(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@XmlElement(name="name", required = true)
	protected String name;
	@XmlElement(name = "description", required = true)
	protected String description;
	@XmlElement(name = "enabled", required = false)
	private Boolean enabled;

	public String getName() {
		return name;
	}

	public String getShortenedName() {
		return name.replaceAll("\\s", "");
	}

	public String getDescription() {
		return description;
	}

	Boolean getEnabled() {
		return enabled;
	}

	void setEnabled(Boolean enabled) {
		this.enabled =enabled;
	}

	public boolean isEnabled() {
		return Boolean.TRUE.equals(enabled);
	}
}
