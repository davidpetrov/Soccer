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

	public Player(String team, String name, int minutesPlayed, int lineups, int substitutes, int subsWOP, int goals,
			int assists) {
		super();
		this.team = team;
		this.name = name;
		this.minutesPlayed = minutesPlayed;
		this.lineups = lineups;
		this.substitutes = substitutes;
		this.subsWOP = subsWOP;
		this.goals = goals;
		this.assists = assists;
	}
	
	public String getName(){
		return name;
	}

	public float getGoalAvg() {
		return ((float) goals) / minutesPlayed;
	}

	public float getAssistAvg() {
		return ((float) assists) / minutesPlayed;
	}

	@Override
	public String toString() {
		return "Player [team=" + team + ", name=" + name + ", minutesPlayed=" + minutesPlayed + ", lineups=" + lineups
				+ ", substitutes=" + substitutes + ", subsWOP=" + subsWOP + ", goals=" + goals + ", assists=" + assists
				+ "]";
	}

}
