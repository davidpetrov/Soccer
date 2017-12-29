package entries;

import main.Fixture;
import main.Fixture;
import main.Result;

public class FinalEntry implements Comparable<FinalEntry> {
	public Fixture fixture;
	public Float prediction;
	public Result result;
	public float threshold;
	public float upper;
	public float lower;
	public float value;

	public FinalEntry(Fixture f, float prediction, Result result, float threshold, float lower, float upper) {
		this.fixture = f;
		this.prediction = prediction;
		this.result = result;
		this.threshold = threshold;
		this.upper = upper;
		this.lower = lower;
		this.value = 0.9f;
	}

	/**
	 * Copy constructor (doesn't copy the fixture field, not necessary for now)
	 * 
	 * @param i
	 */
	public FinalEntry(FinalEntry i) {
		this.fixture = i.fixture;
		this.prediction = i.prediction;
		this.result = new Result(i.result);
		this.threshold = i.threshold;
		this.upper = i.upper;
		this.lower = i.lower;
		this.value = i.value;
	}

	public float getPrediction() {
		return prediction;
	}

	public float getCertainty() {
		return prediction > threshold ? prediction : (1f - prediction);
	}

	public float getCOT() {
		return prediction > threshold ? (prediction - threshold) : (threshold - prediction);
	}

	public float getValue() {
		float gain = prediction > threshold ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		return getCertainty() * gain;
	}

	@Override
	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		float coeff = prediction >= upper ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
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
		if (fixture.getTotalGoals() < 0)
			return 0f;
		float coeff = prediction >= upper ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		if (success())
			return coeff - 1f;
		else
			return -1f;

	}

	public float getNormalizedProfit() {
		if (fixture.getTotalGoals() < 0)
			return 0f;
		float coeff = prediction >= upper ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		float betUnit = 1f / (coeff - 1);
		if (success())
			return 1f;
		else
			return -betUnit;
	}

	@Override
	public int compareTo(FinalEntry o) {
		return prediction.compareTo(o.prediction);
	}

}