package entries;

import main.Fixture;

public class DrawEntry {

	public Fixture fixture;
	public boolean prediction;
	public float expectancy;

	public DrawEntry(Fixture fixture, boolean prediction, float expectancy) {
		super();
		this.fixture = fixture;
		this.prediction = prediction;
		this.expectancy = expectancy;
	}

	@Override
	public String toString() {
		String out = prediction ? "draw" : "12";
		float coeff = prediction ? fixture.getMaxClosingDrawOdds() : 1f;
		return fixture.date + " " + fixture.homeTeam + " : " + fixture.awayTeam + " " + " " + out + " " + coeff + " "
				+ success() + "\n";
	}

	public boolean success() {
		return fixture.isDraw() && prediction;
	}

	public float getProfit() {
		if (success())
			return fixture.getMaxClosingDrawOdds() - 1f;
		else
			return -1f;
	}

}
