package main;

import utils.Pair;

public class GameStats {

	// check for better presentation, i.e not with pair
	Pair shots;
	Pair shotsWide;
	Pair corners;
	Pair fouls;
	Pair offsides;

	public GameStats(Pair shots, Pair shotsWide, Pair corners, Pair fouls, Pair offsides) {
		super();
		this.shots = shots;
		this.shotsWide = shotsWide;
		this.corners = corners;
		this.fouls = fouls;
		this.offsides = offsides;
	}

}
