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

}
