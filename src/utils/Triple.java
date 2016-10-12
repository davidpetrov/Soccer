package utils;

public class Triple {

	public float first;
	public Pair pair;

	public Triple(float first, Pair pair) {
		super();
		this.first = first;
		this.pair = pair;
	}

	public static Triple of(float home, Pair pair) {
		return new Triple(home, pair);
	}

	public String toString() {
		return first + " : " + pair;
	}
}