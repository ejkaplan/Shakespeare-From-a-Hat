package casting;

import java.util.Comparator;

public class RoleDifficultyComparator implements Comparator<Role> {

	@Override
	public int compare(Role r0, Role r1) {
		return r0.getNLines() - r1.getNLines();
	}

}
