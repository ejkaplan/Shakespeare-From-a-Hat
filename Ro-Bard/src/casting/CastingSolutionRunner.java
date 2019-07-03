package casting;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import com.opencsv.CSVReader;

public class CastingSolutionRunner implements Runnable {

	private List<Role> roles;
	private List<Actor> actors;
	private Solver<CastingSolution> solver;
	private CastingSolution solved;

	public static void main(String[] args) {
		CastingSolutionRunner runner = new CastingSolutionRunner("roles.csv", "actors.csv");
		Thread thr = new Thread(runner);
		thr.start();
		try {
			thr.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public CastingSolutionRunner(String roleFilename, String actorFilename) {
		try {
			loadData(roleFilename, actorFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Random rand = new Random();
		for (Role r : roles) {
			r.setActor(actors.get(rand.nextInt(actors.size())));
		}
	}

	public void loadData(String roleFilename, String actorFilename) throws IOException {
		// Read the csv of roles and create the role objects
		CSVReader reader = new CSVReader(new FileReader("roles.csv"));
		List<String[]> allRows = reader.readAll();
		reader.close();
		HashMap<String, Role> roleMap = new HashMap<String, Role>();
		roles = new ArrayList<Role>();
		for (String[] row : allRows) {
			String name = row[0];
			int nLines = Integer.parseInt(row[1]);
			Role r = new Role(name, nLines);
			roleMap.put(name, r);
			roles.add(r);
		}
		Collections.shuffle(roles);
		// Make sure that the program knows which roles are incompatible
		for (String[] row : allRows) {
			Role r = roleMap.get(row[0]);
			for (int i = 2; i < row.length; i++) {
				r.setIncompatible(roleMap.get(row[i]));
			}
		}
		// Read the csv of actors and create their objects
		reader = new CSVReader(new FileReader("actors.csv"));
		allRows = reader.readAll();
		reader.close();
		actors = new ArrayList<Actor>();
		for (String[] row : allRows) {
			String name = row[0];
			if (name.length() == 0)
				continue;
			List<Role> prohibited = new ArrayList<Role>();
			for (int i = 1; i < row.length; i++) {
				String roleName = row[i];
				if (roleName.length() > 0)
					prohibited.add(roleMap.get(roleName));
			}
			actors.add(new Actor(name, prohibited));
		}
		Collections.shuffle(actors);
	}

	@Override
	public void run() {
		SolverFactory<CastingSolution> factory = SolverFactory.createFromXmlResource("solverconfig.xml");
		solver = factory.buildSolver();
		CastingSolution unsolved = new CastingSolution(actors, roles);
		solved = solver.solve(unsolved);
		printSolution();
	}

	public void printSolution() {
		if (solved == null)
			return;
		Map<Actor, List<Role>> cast = solved.getCastMap();
		Map<Actor, Integer> lines = solved.getLineCounts();
		for (Actor a : cast.keySet()) {
			System.out.println(a + ": " + cast.get(a) + " (" + lines.get(a) + ")");
		}
	}

	public boolean isSolving() {
		if (solver == null)
			return false;
		return solver.isSolving();
	}

	public HardSoftScore getBestScore() {
		return (HardSoftScore) solver.getBestScore();
	}

	public boolean isDone() {
		return solver != null && !isSolving();
	}

	public CastingSolution getBestSolution() {
		return solver.getBestSolution();
	}

}
