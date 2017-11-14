package odds;

import java.util.Date;

public class MatchOdds extends Odds {

	public float homeOdds;
	public float drawOdds;
	public float awayOdds;

	public MatchOdds(String bookmaker, Date date, float homeOdds, float drawOdds, float awayOdds) {
		super();
		this.date = date;
		this.bookmaker = bookmaker;
		this.homeOdds = homeOdds;
		this.drawOdds = drawOdds;
		this.awayOdds = awayOdds;
	}

	@Override
	public float getMargin() {
		return 1f / homeOdds + 1f / drawOdds + 1f / awayOdds;
	}

	public String toString() {
		return bookmaker + "  " + homeOdds + "  " + drawOdds + "  " + awayOdds;
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
		return new MatchOdds(bookmaker, date, truehomeOdds, truedrawOdds, trueawayOdds);
	}

}
