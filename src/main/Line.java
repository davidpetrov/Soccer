package main;

public class Line {

	public float line;
	public float home;
	public float away;
	public String bookmaker;

	public Line(float line, float home, float away, String bookmaker) {
		super();
		this.line = line;
		this.home = home;
		this.away = away;
		this.bookmaker = bookmaker;
	}

	public String toString() {
		return "line " + line + " " + home + " " + away + "\n";
	}

}
