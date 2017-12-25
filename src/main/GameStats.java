package main;

import utils.Pair;

public class GameStats {

	// check for better presentation, i.e not with pair
	Pair shots;
	Pair shotsWide;
	Pair corners;
	Pair fouls;
	Pair offsides;

	// for shots from all euro data
	Pair AllEuroShots;

	int posssessionHome;

	public GameStats(Pair shots, Pair shotsWide, Pair corners, Pair fouls, Pair offsides) {
		super();
		this.shots = shots;
		this.shotsWide = shotsWide;
		this.corners = corners;
		this.fouls = fouls;
		this.offsides = offsides;
		this.AllEuroShots = Pair.of(-1, -1);// distinct default value
	}

}
