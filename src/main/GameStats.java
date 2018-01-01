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

	public int getShotsAway() {
		return (int) shots.away;
	}

	public int getShotsHome() {
		return (int) shots.home;
	}

	public static GameStats initial() {
		return new GameStats(Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1), Pair.of(-1, -1));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((AllEuroShots == null) ? 0 : AllEuroShots.hashCode());
		result = prime * result + ((corners == null) ? 0 : corners.hashCode());
		result = prime * result + ((fouls == null) ? 0 : fouls.hashCode());
		result = prime * result + ((offsides == null) ? 0 : offsides.hashCode());
		result = prime * result + possessionHome;
		result = prime * result + ((shots == null) ? 0 : shots.hashCode());
		result = prime * result + ((shotsWide == null) ? 0 : shotsWide.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GameStats))
			return false;
		GameStats other = (GameStats) obj;
		if (AllEuroShots == null) {
			if (other.AllEuroShots != null)
				return false;
		} else if (!AllEuroShots.equals(other.AllEuroShots))
			return false;
		if (corners == null) {
			if (other.corners != null)
				return false;
		} else if (!corners.equals(other.corners))
			return false;
		if (fouls == null) {
			if (other.fouls != null)
				return false;
		} else if (!fouls.equals(other.fouls))
			return false;
		if (offsides == null) {
			if (other.offsides != null)
				return false;
		} else if (!offsides.equals(other.offsides))
			return false;
		if (possessionHome != other.possessionHome)
			return false;
		if (shots == null) {
			if (other.shots != null)
				return false;
		} else if (!shots.equals(other.shots))
			return false;
		if (shotsWide == null) {
			if (other.shotsWide != null)
				return false;
		} else if (!shotsWide.equals(other.shotsWide))
			return false;
		return true;
	}

}
