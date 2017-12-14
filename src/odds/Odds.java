package odds;

import java.util.Date;

public abstract class Odds {
	public String bookmaker;
	public Date date;
	public boolean isOpening;
	public boolean isClosing;

	public abstract float getMargin();

	public abstract Odds getTrueOddsMarginal();

	public String getBookmaker() {
		return bookmaker;
	}

	public void setBookmaker(String bookmaker) {
		this.bookmaker = bookmaker;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmaker == null) ? 0 : bookmaker.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

}
