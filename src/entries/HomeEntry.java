package entries;

import main.Fixture;

public class HomeEntry {

	public Fixture fixture;
	public boolean prediction;
	public float score;

	public HomeEntry(Fixture fixture, boolean prediction, float score) {
		super();
		this.fixture = fixture;
		this.prediction = prediction;
		this.score = score;
	}

	@Override
	public String toString() {
		String out = prediction ? "home" : "X2";
		float coeff = prediction ? fixture.getMaxClosingHomeOdds() : 1f;
		return fixture.date + " " + fixture.homeTeam + " : " + fixture.awayTeam + " " + " " + out + " " + getProfit()
				+ " " + success() + "\n";
	}

	public boolean success() {
		return fixture.isHomeWin() && prediction;
	}

	public float getProfit() {
		if (success())
			return fixture.getMaxClosingHomeOdds() - 1f;
		else
			return -1f;
	}

}
