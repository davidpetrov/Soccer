package main;

import java.util.Date;

public class FullFixture extends ExtendedFixture {

	public AsianLines asianLines;
	public GoalLines goalLines;

	public FullFixture(Date date, String homeTeam, String awayTeam, Result result, String competition) {
		super(date, homeTeam, awayTeam, result, competition);
	}

	public FullFixture withLines(AsianLines asianLines, GoalLines goalLines) {
		this.asianLines = asianLines;
		this.goalLines = goalLines;
		return this;
	}
	
	public FullFixture withAsianLines(AsianLines asianLines) {
		this.asianLines = asianLines;
		return this;
	}
	
	public FullFixture withGoalLines(GoalLines goalLines) {
		this.goalLines = goalLines;
		return this;
	}

}
