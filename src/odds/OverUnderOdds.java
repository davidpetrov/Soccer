package odds;

import java.util.Date;

public class OverUnderOdds extends Odds {

	public float line;
	public float overOdds;
	public float underOdds;

	public OverUnderOdds(String bookmaker, Date time, float line, float overOdds, float underOdds) {
		super();
		this.time = time;
		this.bookmaker = bookmaker;
		this.line = line;
		this.overOdds = overOdds;
		this.underOdds = underOdds;
	}

	public float getOverOdds() {
		return overOdds;
	}

	public void setOverOdds(float overOdds) {
		this.overOdds = overOdds;
	}

	public float getUnderOdds() {
		return underOdds;
	}

	public void setUnderOdds(float underOdds) {
		this.underOdds = underOdds;
	}

	@Override
	public float getMargin() {
		return 1f / overOdds + 1f / underOdds;
	}

	public String toString() {
		return bookmaker + " " + time + "  " + line + "  " + overOdds + "  " + underOdds;
	}

	@Override
	public OverUnderOdds getTrueOddsMarginal() {
		float margin = 1f / overOdds + 1f / underOdds - 1f;
		float trueOverOdds = 2 * overOdds / (2f - margin * overOdds);
		float trueUnderOdds = 2 * underOdds / (2f - margin * underOdds);
		return new OverUnderOdds(bookmaker, time, line, trueOverOdds, trueUnderOdds);
	}

	public void removeMarginProportional() {
		OverUnderOdds trueOdds = getTrueOddsMarginal();
		this.overOdds = trueOdds.overOdds;
		this.underOdds = trueOdds.underOdds;
	}

	public OverUnderOdds withIsClosing() {
		this.isClosing = true;
		return this;
	}

	public OverUnderOdds withIsOpening() {
		this.isOpening = true;
		return this;
	}

	public float getOddsDiff() {
		return Math.abs(overOdds - underOdds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(line);
		result = prime * result + Float.floatToIntBits(overOdds);
		result = prime * result + Float.floatToIntBits(underOdds);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof OverUnderOdds))
			return false;
		OverUnderOdds other = (OverUnderOdds) obj;
		if (Float.floatToIntBits(line) != Float.floatToIntBits(other.line))
			return false;
		if (Float.floatToIntBits(overOdds) != Float.floatToIntBits(other.overOdds))
			return false;
		if (Float.floatToIntBits(underOdds) != Float.floatToIntBits(other.underOdds))
			return false;
		return true;
	}

	public OverUnderOdds withFixtureFields(Date date, String homeTeam, String awayTeam) {
		this.fixtureDate = date;
		this.homeTeamName = homeTeam;
		this.awayTeamName = awayTeam;
		return this;
	}

}
