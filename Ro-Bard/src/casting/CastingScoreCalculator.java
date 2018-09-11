package casting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;

public class CastingScoreCalculator implements IncrementalScoreCalculator<CastingSolution> {

	private Map<Actor, List<Role>> actorRoleMap;
	private Map<Actor, Integer> actorLines;
	private int idealLinesPerActor;
	private int hard;
	private int soft;

	private void insert(Role r) {
		Actor actor = r.getActor();
		if (actor.getProhibited().contains(r)) {
			hard -= r.getNLines();
		}
		for (Role other : actorRoleMap.get(actor)) {
			if (r.getIncompatible().contains(other)) {
				hard -= Math.min(r.getNLines(), other.getNLines());
			}
		}
		actorRoleMap.get(actor).add(r);
		int lines = actorLines.get(actor) + r.getNLines();
		int oldDiff = Math.abs(actorLines.get(actor) - idealLinesPerActor);
		int newDiff = Math.abs(lines - idealLinesPerActor);
		soft += oldDiff - newDiff;
		actorLines.put(actor, lines);
	}

	private void retract(Role r) {
		Actor actor = r.getActor();
		actorRoleMap.get(actor).remove(r);
		if (actor.getProhibited().contains(r)) {
			hard += r.getNLines();
		}
		for (Role other : actorRoleMap.get(actor)) {
			if (r.getIncompatible().contains(other)) {
				hard += Math.min(r.getNLines(), other.getNLines());
			}
		}
		int lines = actorLines.get(actor) - r.getNLines();
		int oldDiff = Math.abs(actorLines.get(actor) - idealLinesPerActor);
		int newDiff = Math.abs(lines - idealLinesPerActor);
		soft += oldDiff - newDiff;
		actorLines.put(actor, lines);
	}

	@Override
	public void resetWorkingSolution(CastingSolution sln) {
		actorRoleMap = new HashMap<Actor, List<Role>>();
		actorLines = new HashMap<Actor, Integer>();
		for (Actor a : sln.getActors()) {
			actorRoleMap.put(a, new ArrayList<Role>());
			actorLines.put(a, 0);
		}
		idealLinesPerActor = 0;
		for (Role r : sln.getRoles()) {
			idealLinesPerActor += r.getNLines();
		}
		idealLinesPerActor /= sln.getActors().size();
		hard = 0;
		soft = -idealLinesPerActor * sln.getActors().size();
		for (Role r : sln.getRoles())
			insert(r);
	}

	@Override
	public void beforeEntityAdded(Object entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterEntityAdded(Object entity) {
		insert((Role) entity);
	}

	@Override
	public void beforeVariableChanged(Object entity, String var) {
		retract((Role) entity);
	}

	@Override
	public void afterVariableChanged(Object entity, String var) {
		insert((Role) entity);
	}

	@Override
	public void beforeEntityRemoved(Object entity) {
		retract((Role) entity);
	}

	@Override
	public void afterEntityRemoved(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public HardSoftScore calculateScore() {
		return HardSoftScore.valueOf(hard, soft);
	}

}
