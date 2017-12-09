package odds;

import java.util.Date;

public class OverUnderOdds extends Odds {

	public float line;
	public float overOdds;
	public float underOdds;

	public OverUnderOdds(String bookmaker, Date date, float line, float overOdds, float underOdds) {
		super();
		this.date = date;
		this.bookmaker = bookmaker;
		this.line = line;
		this.overOdds = overOdds;
		this.underOdds = underOdds;
	}

	@Override
	public float getMargin() {
		return 1f / overOdds + 1f / underOdds;
	}

	public String toString() {
		return bookmaker + " " + date + "  " + line + "  " + overOdds + "  " + underOdds;
	}

	@Override
	public Odds getTrueOddsMarginal() {
		float margin = 1f / overOdds + 1f / underOdds - 1f;
		float trueOverOdds = 2 * overOdds / (2f - margin * overOdds);
		float trueUnderOdds = 2 * underOdds / (2f - margin * underOdds);
		return new OverUnderOdds(bookmaker, date, line, trueOverOdds, trueUnderOdds);
	}
}
