package casting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class CastingSolution {

	private List<Actor> actors;
	private List<Role> roles;
	private HardSoftScore score;

	public CastingSolution(Collection<Actor> actors, Collection<Role> roles) {
		this.actors = new ArrayList<Actor>(actors);
		this.roles = new ArrayList<Role>(roles);
	}

	public CastingSolution() {

	}

	@ValueRangeProvider(id = "actorRange")
	public List<Actor> getActors() {
		return actors;
	}

	@PlanningEntityCollectionProperty
	public List<Role> getRoles() {
		return roles;
	}

	@PlanningScore
	public HardSoftScore getScore() {
		return score;
	}

	public void setScore(HardSoftScore score) {
		this.score = score;
	}

	public Map<Actor, List<Role>> getCastMap() {
		Map<Actor, List<Role>> cast = new HashMap<Actor, List<Role>>();
		for (Role r : roles) {
			Actor a = r.getActor();
			if (!cast.containsKey(a)) {
				cast.put(a, new ArrayList<Role>());
			}
			cast.get(a).add(r);
		}
		for (Actor a : cast.keySet()) {
			Collections.sort(cast.get(a));
			Collections.reverse(cast.get(a));
		}
		return cast;
	}

	public Map<Actor, Integer> getLineCounts() {
		Map<Actor, Integer> lines = new HashMap<Actor, Integer>();
		for (Role r : roles) {
			Actor a = r.getActor();
			if (!lines.containsKey(a))
				lines.put(a, r.getNLines());
			else
				lines.put(a, lines.get(a) + r.getNLines());
		}
		return lines;
	}

}
