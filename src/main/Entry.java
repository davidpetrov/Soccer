package main;

public class Entry implements Comparable<Entry> {
	Fixture fixture;
	Float result;
	String alg;

	public Entry(Fixture fixture, float result, String alg) {
		this.fixture = fixture;
		this.result = result;
		this.alg = alg;
	}

	@Override
	public String toString() {

		return String.format("%.2f", result * 100) + " " + fixture.date + " " + fixture.homeTeamName + " : "
				+ fixture.awayTeamName + " " + alg + "\n";
	}

	@Override
	public int compareTo(Entry o) {
		return result.compareTo(o.result);
	}
}
