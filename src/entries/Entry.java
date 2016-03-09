package entries;

import main.ExtendedFixture;

public class Entry implements Comparable<Entry> {
	public ExtendedFixture fixture;
	Float result;
	String alg;

	public Entry(ExtendedFixture fixture, float result, String alg) {
		this.fixture = fixture;
		this.result = result;
		this.alg = alg;
	}

	@Override
	public String toString() {

		return String.format("%.2f", result * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
				+ fixture.awayTeam + " " + alg + "\n";
	}

	@Override
	public int compareTo(Entry o) {
		return result.compareTo(o.result);
	}
}
