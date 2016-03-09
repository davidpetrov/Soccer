package utils;

public class Pair {

	public float home;
	public float away;

	public Pair(float home, float away) {
		super();
		this.home = home;
		this.away = away;
	}

	public static Pair of(float home, float away) {
		return new Pair(home, away);
	}
}
