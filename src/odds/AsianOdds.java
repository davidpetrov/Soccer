package odds;

import java.util.Date;

public class AsianOdds extends Odds {

	public float line;
	public float homeOdds;
	public float awayOdds;

	public AsianOdds(String bookmaker, Date date, float line, float homeOdds, float awayOdds) {
		super();
		this.time = time;
		this.bookmaker = bookmaker;
		this.line = line;
		this.homeOdds = homeOdds;
		this.awayOdds = awayOdds;
	}

	/**
	 * Constructor for converting OverUnderOdds into AsianOdds overOdds has the
	 * meaning of homeOdds and underOdds has the meaning of awayOdds
	 * 
	 * @param ou
	 */
	public AsianOdds(OverUnderOdds ou) {
		this.time = ou.time;
		this.bookmaker = ou.bookmaker;
		this.line = ou.line;
		this.homeOdds = ou.overOdds;
		this.awayOdds = ou.underOdds;
		this.isClosing = ou.isClosing;
		this.isOpening = ou.isOpening;
	}

	@Override
	public float getMargin() {
		return 1f / homeOdds + 1f / awayOdds;
	}

	public String toString() {
		return bookmaker + "  " + line + "  " + homeOdds + "  " + awayOdds;
	}

	@Override
	public Odds getTrueOddsMarginal() {
		float margin = 1f / homeOdds + 1f / awayOdds - 1f;
		float truehomeOdds = 2 * homeOdds / (2f - margin * homeOdds);
		float trueawayOdds = 2 * awayOdds / (2f - margin * awayOdds);
		return new AsianOdds(bookmaker, time, line, truehomeOdds, trueawayOdds);
	}

	public AsianOdds withIsClosing() {
		this.isClosing = true;
		return this;
	}

	public AsianOdds withIsOpening() {
		this.isOpening = true;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(awayOdds);
		result = prime * result + Float.floatToIntBits(homeOdds);
		result = prime * result + Float.floatToIntBits(line);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AsianOdds))
			return false;
		AsianOdds other = (AsianOdds) obj;
		if (Float.floatToIntBits(awayOdds) != Float.floatToIntBits(other.awayOdds))
			return false;
		if (Float.floatToIntBits(homeOdds) != Float.floatToIntBits(other.homeOdds))
			return false;
		if (Float.floatToIntBits(line) != Float.floatToIntBits(other.line))
			return false;
		return true;
	}
	
	public AsianOdds withFixtureFields(Date date, String homeTeam, String awayTeam) {
		this.fixtureDate = date;
		this.homeTeamName = homeTeam;
		this.awayTeamName = awayTeam;
		return this;
	}

}
