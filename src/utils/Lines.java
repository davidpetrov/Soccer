package utils;

public class Lines {

	String type;
	float line;
	float home;
	float away;
	float line1home;
	float line1away;
	float line2home;
	float line2away;
	float line3home;
	float line3away;
	float line4home;
	float line4away;

	public Lines(String type, float line, float home, float away, float line1home, float line1away, float line2home,
			float line2away, float line3home, float line3away, float line4home, float line4away) {
		super();
		this.type = type;
		this.line = line;
		this.home = home;
		this.away = away;
		this.line1home = line1home;
		this.line1away = line1away;
		this.line2home = line2home;
		this.line2away = line2away;
		this.line3home = line3home;
		this.line3away = line3away;
		this.line4home = line4home;
		this.line4away = line4away;
	}

	public float getLine(int n, float line) {
		if (n == 1)
			return line - 0.5f;
		else if (n == 2)
			return line - 0.25f;
		else if (n == 3)
			return line + 0.25f;
		else if (n == 4)
			return line + 0.5f;
		else
			return line;
	}

	public float getHome(int n) {
		if (n == 1)
			return line1home;
		else if (n == 2)
			return line2home;
		else if (n == 3)
			return line3home;
		else if (n == 4)
			return line4home;
		else
			return home;
	}

	public float getAway(int n) {
		if (n == 1)
			return line1away;
		else if (n == 2)
			return line2away;
		else if (n == 3)
			return line3away;
		else if (n == 4)
			return line4away;
		else
			return away;
	}

}
