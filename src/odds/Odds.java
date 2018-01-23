package odds;

import java.util.Date;

public abstract class Odds {

	// for identification
	public Date fixtureDate;
	public String homeTeamName;
	public String awayTeamName;

	public String bookmaker;
	public Date time;
	public boolean isOpening;
	public boolean isClosing;
	public boolean isActive;

	public abstract float getMargin();

	public abstract Odds getTrueOddsMarginal();

	public String getBookmaker() {
		return bookmaker;
	}

	public void setBookmaker(String bookmaker) {
		this.bookmaker = bookmaker;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmaker == null) ? 0 : bookmaker.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Odds))
			return false;
		Odds other = (Odds) obj;
		if (bookmaker == null) {
			if (other.bookmaker != null)
				return false;
		} else if (!bookmaker.equals(other.bookmaker))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

}
