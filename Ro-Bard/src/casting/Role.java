package casting;

import java.util.HashSet;
import java.util.Set;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(difficultyComparatorClass = RoleDifficultyComparator.class)
public class Role implements Comparable<Role> {

	private String name;
	private Set<Role> incompatible;
	private Actor actor;
	private int nLines;

	public Role(String name, int nLines) {
		this.name = name;
		incompatible = new HashSet<Role>();
		this.nLines = nLines;
	}

	public Role() {
	}

	public void setIncompatible(Role r) {
		if (r != null && r != this) {
			incompatible.add(r);
			r.incompatible.add(this);
		}
	}

	@PlanningVariable(valueRangeProviderRefs = { "actorRange" })
	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public Set<Role> getIncompatible() {
		return incompatible;
	}

	public String getName() {
		return name;
	}

	public int getNLines() {
		return nLines;
	}

	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nLines;
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
		Role other = (Role) obj;
		if (nLines != other.nLines)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(Role other) {
		return getNLines() - other.getNLines();
	}

}
