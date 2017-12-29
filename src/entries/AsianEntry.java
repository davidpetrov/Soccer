package entries;

import main.Fixture;
import results.Results;
import utils.Utils;

public class AsianEntry {
	public Fixture fixture;
	public boolean prediction;
	public float line;
	public float home;
	public float away;
	public float expectancy;

	public AsianEntry(Fixture fixture, boolean prediction, float line, float home, float away,
			float expectancy) {
		this.fixture = fixture;
		this.prediction = prediction;
		this.line = line;
		this.expectancy = expectancy;
		this.home = home;
		this.away = away;
	}

	@Override
	public String toString() {
		String out = prediction ? "home" : "away";
		float coeff = prediction ? home : away;
		return fixture.date + " " + fixture.homeTeam + " : " + fixture.awayTeam + " " + " " + out + " " + line + " "
				+ coeff + " " + success() + " exp " + Results.format(expectancy) + "\n";
	}

	public String success() {
		int result = fixture.result.goalsHomeTeam - fixture.result.goalsAwayTeam;
		float diff = result + line;

		if (prediction) {
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
		float coeff = prediction ? home : away;
		String success = success();
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

}
