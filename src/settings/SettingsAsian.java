package settings;

public class SettingsAsian {
	public String league;
	public int year;
	public float basic;
	public float poisson;
	public float expectancy;
	public float profit;

	public SettingsAsian(String league, int year, float basic, float poisson, float expectancy, float profit) {
		super();
		this.league = league;
		this.year = year;
		this.basic = basic;
		this.poisson = poisson;
		this.expectancy = expectancy;
		this.profit = profit;
	}

	private String format(float d) {
		return String.format("%.2f", d);
	}

	@Override
	public String toString() {
		return league + " bas*" + format(basic) + " poi*" + format(poisson) + format(expectancy) + format(profit);
	}

}
