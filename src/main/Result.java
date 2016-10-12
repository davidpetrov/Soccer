package main;

public class Result {
	public int goalsHomeTeam;
	public int goalsAwayTeam;

	public Result(int goalsHomeTeam, int goalsAwayTeam) {
		this.goalsHomeTeam = goalsHomeTeam;
		this.goalsAwayTeam = goalsAwayTeam;
	}

	public String toString() {
		return goalsHomeTeam + " : " + goalsAwayTeam;
	}
}
