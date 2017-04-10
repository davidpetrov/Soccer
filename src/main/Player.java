package main;

public class Player {
	public String team;
	public String name;
	public int minutesPlayed;
	public int lineups;
	public int substitutes;
	public int subsWOP;// substitute but didn't participate in the match
	public int goals;
	public int assists;
	public int homeMinutesPlayed;
	public int homeLineups;
	public int homeSubstitutes;
	public int homeSubsWOP;// substitute but didn't participate in the match at
							// home
	public int homeGoals;
	public int homeAssists;
	public int awayMinutesPlayed;
	public int awayLineups;
	public int awaySubstitutes;
	public int awaySubsWOP;// substitute but didn't participate in the match at
							// away
	public int awayGoals;
	public int awayAssists;

	public String getName() {
		return name;
	}

	public Player(String team, String name, int minutesPlayed, int lineups, int substitutes, int subsWOP, int goals,
			int assists, int homeMinutesPlayed, int homeLineups, int homeSubstitutes, int homeSubsWOP, int homeGoals,
			int homeAssists, int awayMinutesPlayed, int awayLineups, int awaySubstitutes, int awaySubsWOP,
			int awayGoals, int awayAssists) {
		super();
		this.team = team;
		this.name = name;
		this.minutesPlayed = minutesPlayed;
		this.lineups = lineups;
		this.substitutes = substitutes;
		this.subsWOP = subsWOP;
		this.goals = goals;
		this.assists = assists;
		this.homeMinutesPlayed = homeMinutesPlayed;
		this.homeLineups = homeLineups;
		this.homeSubstitutes = homeSubstitutes;
		this.homeSubsWOP = homeSubsWOP;
		this.homeGoals = homeGoals;
		this.homeAssists = homeAssists;
		this.awayMinutesPlayed = awayMinutesPlayed;
		this.awayLineups = awayLineups;
		this.awaySubstitutes = awaySubstitutes;
		this.awaySubsWOP = awaySubsWOP;
		this.awayGoals = awayGoals;
		this.awayAssists = awayAssists;
	}

	public float getGoalAvg() {
		return minutesPlayed == 0 ? 0 : ((float) goals) / minutesPlayed;
	}

	public float getAssistAvg() {
		return minutesPlayed == 0 ? 0 : ((float) assists) / minutesPlayed;
	}

	@Override
	public String toString() {
		return "Player [team=" + team + ", name=" + name + ", minutesPlayed=" + minutesPlayed + ", lineups=" + lineups
				+ ", substitutes=" + substitutes + ", subsWOP=" + subsWOP + ", goals=" + goals + ", assists=" + assists
				+ "]";
	}

	public float getGoalAvgHome() {
		return homeMinutesPlayed == 0 ? 0 : ((float) homeGoals) / homeMinutesPlayed;
	}

	public float getAssistAvgHome() {
		return homeMinutesPlayed == 0 ? 0 : ((float) homeAssists) / homeMinutesPlayed;
	}

	public float getGoalAvgAway() {
		return awayMinutesPlayed == 0 ? 0 : ((float) awayGoals) / awayMinutesPlayed;
	}

	public float getAssistAvgAway() {
		return awayMinutesPlayed == 0 ? 0 : ((float) awayAssists) / awayMinutesPlayed;
	}

}
