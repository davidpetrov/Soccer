package tables;

import java.util.ArrayList;
import java.util.Collections;

import main.ExtendedFixture;

public class Table {

	public String league;
	public int year;
	public int matchday;
	public ArrayList<Position> positions;

	public Table(String league, int year, int matchday) {
		this.league = league;
		this.year = year;
		this.matchday = matchday;
		positions = new ArrayList<>();
	}

	public void sort() {
		Collections.sort(positions, Collections.reverseOrder());
	}

	public int getPosition(String team) {
		for (int i = 0; i < positions.size(); i++) {
			if (positions.get(i).team.equals(team))
				return i + 1;
		}

		return Integer.MIN_VALUE;
	}

	public int getPositionDiff(ExtendedFixture f) {
		return getPosition(f.homeTeam) - getPosition(f.awayTeam);
	}

	public String toString() {
		int num = 1;
		String result = "";
		for (Position i : positions) {
			String curr = String.format("%3d ", num++) + String.format("%-25s", i.team) + String.format("%3d", i.played)
					+ String.format("%3d", i.wins) + String.format("%3d", i.draws) + String.format("%3d", i.losses)
					+ String.format("%4d", i.scored) + " : " + String.format("%-4d", i.conceded)
					+ String.format("%+3d", i.diff) + String.format("%4d", i.points) + "\n";
			result += curr;
		}
		return result;
	}
}
