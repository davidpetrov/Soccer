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

	public OverUnderOdds withIsClosing() {
		this.isClosing = true;
		return this;
	}

	public OverUnderOdds withIsOpening() {
		this.isOpening = true;
		return this;
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

}
