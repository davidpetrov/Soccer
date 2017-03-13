package entries;

import main.ExtendedFixture;
import main.Result;

public class FinalEntry implements Comparable<FinalEntry> {
	public ExtendedFixture fixture;
	public Float prediction;
	public Result result;
	public float threshold;
	public float upper;
	public float lower;
	public float value;

	public FinalEntry(ExtendedFixture fixture, float prediction, Result result, float threshold, float lower,
			float upper) {
		this.fixture = fixture;
		this.prediction = prediction;
		this.result = result;
		this.threshold = threshold;
		this.upper = upper;
		this.lower = lower;
		this.value = 0.9f;
	}

	public float getCertainty() {
		return prediction > threshold ? prediction : (1f - prediction);
	}

	public float getCOT() {
		return prediction > threshold ? (prediction - threshold) : (threshold - prediction);
	}

	@Override
	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		float coeff = prediction >= upper ? fixture.maxOver : fixture.maxUnder;
		if (fixture.result.goalsHomeTeam == -1)
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + out + " " + String.format("%.2f", coeff) + "\n";
		else
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + totalGoals + " " + out + " " + success() + " "
					+ String.format("%.2f", getProfit()) + "\n";
	}

	public boolean isOver() {
		return prediction >= upper;
	}

	public boolean isUnder() {
		return prediction < lower;
	}

	public boolean success() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		if (totalGoals > 2.5d) {
			return isOver();
		} else {
			return isUnder();
		}

	}

	public float getProfit() {
		if(fixture.getTotalGoals()<0)
			return 0f;
		float coeff = prediction >= upper ? fixture.maxOver : fixture.maxUnder;
		if (success())
			return coeff - 1f;
		else
			return -1f;

	}

	@Override
	public int compareTo(FinalEntry o) {
		return prediction.compareTo(o.prediction);
	}
}