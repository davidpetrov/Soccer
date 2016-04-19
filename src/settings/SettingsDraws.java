package settings;

public class SettingsDraws {
	public String league;
	public int year;
	public float basic;
	public float poisson;
	public float value;
	public float profit;

	public SettingsDraws(String league, int year, float basic, float poisson, float value, float profit) {
		this.league = league;
		this.year = year;
		this.basic = basic;
		this.poisson = poisson;
		this.value = value;
		this.profit = profit;
	}

	private String format(float d) {
		return String.format("%.2f", d);
	}

	@Override
	public String toString() {
		return league + " bas*" + format(basic) + " poi*" + format(poisson) + format(value) + format(profit);
	}

}
