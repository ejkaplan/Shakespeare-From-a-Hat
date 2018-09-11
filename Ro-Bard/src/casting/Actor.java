package casting;

import java.util.List;

public class Actor {

	private List<Role> prohibited;
	private String name;

	public Actor(String name, List<Role> prohibited) {
		this.name = name;
		this.prohibited = prohibited;
	}

	public List<Role> getProhibited() {
		return prohibited;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Actor other = (Actor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}