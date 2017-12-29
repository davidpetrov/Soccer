package main;

import utils.Pair;

public class GameStats {

	// check for better presentation, i.e not with pair
	public Pair shots;
	Pair shotsWide;
	Pair corners;
	Pair fouls;
	Pair offsides;

	// for shots from all euro data
	Pair AllEuroShots;

	int possessionHome;

	public GameStats(Pair shots, Pair shotsWide, Pair corners, Pair fouls, Pair offsides) {
		super();
		this.shots = shots;
		this.shotsWide = shotsWide;
		this.corners = corners;
		this.fouls = fouls;
		this.offsides = offsides;
		this.AllEuroShots = Pair.of(-1, -1);// distinct default value
	}

	public GameStats withAllEuroShots(Pair allEuro) {
		this.AllEuroShots = allEuro;
		return this;
	}

	public GameStats withPossession(int possessionHome) {
		this.possessionHome = possessionHome;
		return this;
	}

	public float getShotsAway() {
		return shots.away;
	}

	public float getShotsHome() {
		return shots.home;
	}

	public static GameStats initial() {
		return new GameStats(Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1));
	}

}
