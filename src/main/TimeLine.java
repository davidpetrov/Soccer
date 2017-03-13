package main;

import java.util.Date;

public class TimeLine extends Line {

	public Date time;

	public TimeLine(float line, float home, float away, Date time, String bookmaker) {
		super(line, home, away, bookmaker);
		this.time = time;
	}
	
	public String toString() {
		return "line " + line + " " + home + " " + away + " " + "\n";
	}

}
