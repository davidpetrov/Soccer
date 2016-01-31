package settings;

public class Settings {

	public String league;
	public int year;
	public float basic;
	public float poisson;
	public float weightedPoisson;
	public float htCombo;
	public float halfTimeOverOne;
	public float threshold;
	public float upperBound;
	public float lowerBound;
	public float minOdds;
	public float successRate;
	public float profit;
	public float maxOdds;
	public float value;

	public Settings(String league, float basic, float poisson, float weightedPoisson, float threshold, float upperBound,
			float lowerBound, float minOdds, float maxOdds, float successRate, float profit) {
		this.league = league;
		this.basic = basic;
		this.poisson = poisson;
		this.weightedPoisson = weightedPoisson;
		this.threshold = threshold;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.minOdds = minOdds;
		this.successRate = successRate;
		this.profit = profit;
		this.maxOdds = maxOdds;
	}

	public Settings(Settings other) {
		this.league = other.league;
		this.basic = other.basic;
		this.poisson = other.poisson;
		this.weightedPoisson = other.weightedPoisson;
		this.threshold = other.threshold;
		this.upperBound = other.upperBound;
		this.lowerBound = other.lowerBound;
		this.minOdds = other.minOdds;
		this.successRate = other.successRate;
		this.profit = other.profit;
		this.maxOdds = other.maxOdds;
	}

	public Settings withYear(int year) {
		this.year = year;
		return this;
	}

	public Settings withHT(float overOne, float htCombo) {
		this.halfTimeOverOne = overOne;
		this.htCombo = htCombo;
		return this;
	}

	public Settings withValue(float value) {
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		return league + " bas*" + format(basic) + " poi*" + format(poisson) + " wei*" + format(weightedPoisson) + " ht*"
				+ format(htCombo) + " min " + format(minOdds) + " max " + format(maxOdds) + " thold "
				+ format(threshold) + " lower " + format(lowerBound) + " upper " + format(upperBound)
				+ String.format(" %.2f%% ", successRate * 100) + format(profit);

	}

	private String format(float d) {
		return String.format("%.2f", d);
	}
}
