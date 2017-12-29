package entries;

import main.Fixture;
import main.Result;

public class FullEntry extends FinalEntry {

	public main.Line line;

	public FullEntry(Fixture fixture, float prediction, Result result, float threshold, float lower,
			float upper, main.Line line) {
		super(fixture, prediction, result, threshold, lower, upper);
		this.line = line;
	}

	public String successFull() {
		int result = fixture.result.goalsHomeTeam + fixture.result.goalsAwayTeam;
		float diff = result - line.line;
		if (line.line == -1f)
			return "missing  data";

		if (prediction >= upper) {
			if (diff >= 0.5f)
				return "W";
			else if (diff == 0.25f) {
				return "HW";
			} else if (diff == 0f) {
				return "D";
			} else if (diff == -0.25f) {
				return "HL";
			} else {
				return "L";
			}
		} else {
			if (diff >= 0.5f)
				return "L";
			else if (diff == 0.25f) {
				return "HL";
			} else if (diff == 0f) {
				return "D";
			} else if (diff == -0.25f) {
				return "HW";
			} else {
				return "W";
			}
		}
	}

	public float getProfit() {
		if (line.line == -1f)
			return 0;
		float coeff = prediction >= upper ? line.home : line.away;
		String success = successFull();
		if (success.equals("W")) {
			return coeff - 1;
		} else if (success.equals("HW")) {
			return (coeff - 1) / 2;
		} else if (success.equals("D")) {
			return 0f;
		} else if (success.equals("HL")) {
			return -0.5f;
		} else {
			return -1;
		}
	}

	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
				+ fixture.awayTeam + " " + totalGoals + " " + out + " " + line.line + " " + successFull() + " "
				+ String.format("%.2f", getProfit()) + "\n";
	}

}
