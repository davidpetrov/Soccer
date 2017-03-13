package main;

import java.util.ArrayList;
import java.util.Date;

public class TimeFixture extends ExtendedFixture {

	public TimeFixture(Date date, String homeTeam, String awayTeam, Result result, String competition) {
		super(date, homeTeam, awayTeam, result, competition);
	}

	ArrayList<TimeLine> homeOdds;
	ArrayList<TimeLine> drawOdds;
	ArrayList<TimeLine> awayOdds;

	public ArrayList<ArrayList<TimeLine>> asianLines;
	public ArrayList<ArrayList<TimeLine>> goalLines;

	public TimeFixture withOdds(ArrayList<TimeLine> homeOdds, ArrayList<TimeLine> drawOdds,
			ArrayList<TimeLine> awayOdds) {
		this.homeOdds = homeOdds;
		this.awayOdds = awayOdds;
		this.drawOdds = drawOdds;
		return this;
	}

	public TimeFixture withAsianLines(ArrayList<ArrayList<TimeLine>> asianLines) {
		this.asianLines = asianLines;
		return this;
	}

	public TimeFixture withGoalLines(ArrayList<ArrayList<TimeLine>> goalLines) {
		this.goalLines = goalLines;
		return this;
	}

}
