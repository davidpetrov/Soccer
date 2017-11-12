package odds;

import java.util.Date;

public abstract class Odds {
	public String bookmaker;
	public Date date;
	public boolean isOpening;

	public abstract float getMargin();

	public abstract Odds getTrueOddsMarginal();

}
