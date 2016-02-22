package main;

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

	@Override
	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
				+ fixture.awayTeam + " " + totalGoals + " " + out + " " + success() + "\n";
	}

	public boolean success() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		if (totalGoals > 2.5d) {
			return prediction >= upper ? true : false;
		} else {
			return prediction >= lower ? false : true;
		}

	}

	@Override
	public int compareTo(FinalEntry o) {
		return prediction.compareTo(o.prediction);
	}
}