package main;

public class FinalEntry implements Comparable<FinalEntry> {
	public Fixture fixture;
	public Float prediction;
	String alg;
	public Result result;
	float odds;
	public float threshold;
	public float upper;
	public float lower;

	public FinalEntry(Fixture fixture, float prediction, String alg, Result result, float threshold,float lower,float upper) {
		this.fixture = fixture;
		this.prediction = prediction;
		this.alg = alg;
		this.result = result;
		this.threshold = threshold;
		this.upper = upper;
		this.lower = lower;
	}

	@Override
	public String toString() {

		return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeamName + " : "
				+ fixture.awayTeamName + " " + success() + " " + alg + "\n";
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