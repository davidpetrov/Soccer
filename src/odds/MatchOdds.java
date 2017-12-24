package odds;

import java.util.Date;

public class MatchOdds extends Odds {

	public float homeOdds;
	public float drawOdds;
	public float awayOdds;

	public MatchOdds(String bookmaker, Date time, float homeOdds, float drawOdds, float awayOdds) {
		super();
		this.time = time;
		this.bookmaker = bookmaker;
		this.homeOdds = homeOdds;
		this.drawOdds = drawOdds;
		this.awayOdds = awayOdds;
	}

	@Override
	public float getMargin() {
		return 1f / homeOdds + 1f / drawOdds + 1f / awayOdds;
	}

	@Override
	public String toString() {
		return "MatchOdds [homeOdds=" + homeOdds + ", drawOdds=" + drawOdds + ", awayOdds=" + awayOdds + ", bookmaker="
				+ bookmaker + ", time=" + time + "]";
	}

	public float getHomeOdds() {
		return homeOdds;
	}

	public float getDrawOdds() {
		return drawOdds;
	}

	public void setDrawOdds(float drawOdds) {
		this.drawOdds = drawOdds;
	}

	public float getAwayOdds() {
		return awayOdds;
	}

	public void setAwayOdds(float awayOdds) {
		this.awayOdds = awayOdds;
	}

	public void setHomeOdds(float homeOdds) {
		this.homeOdds = homeOdds;
	}

	@Override
	public Odds getTrueOddsMarginal() {
		float margin = getMargin() - 1f;
		float truehomeOdds = 3 * homeOdds / (3f - margin * homeOdds);
		float truedrawOdds = 3 * drawOdds / (3f - margin * drawOdds);
		float trueawayOdds = 3 * awayOdds / (3f - margin * awayOdds);
		return new MatchOdds(bookmaker, time, truehomeOdds, truedrawOdds, trueawayOdds);
	}

	public MatchOdds withIsClosing() {
		this.isClosing = true;
		return this;
	}

	public MatchOdds withIsOpening() {
		this.isOpening = true;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(awayOdds);
		result = prime * result + Float.floatToIntBits(drawOdds);
		result = prime * result + Float.floatToIntBits(homeOdds);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof MatchOdds))
			return false;
		MatchOdds other = (MatchOdds) obj;
		if (Float.floatToIntBits(awayOdds) != Float.floatToIntBits(other.awayOdds))
			return false;
		if (Float.floatToIntBits(drawOdds) != Float.floatToIntBits(other.drawOdds))
			return false;
		if (Float.floatToIntBits(homeOdds) != Float.floatToIntBits(other.homeOdds))
			return false;
		return true;
	}

	public MatchOdds withFixtureFields(Date date, String homeTeam, String awayTeam) {
		this.fixtureDate = date;
		this.homeTeamName = homeTeam;
		this.awayTeamName = awayTeam;
		return this;
	}

}
