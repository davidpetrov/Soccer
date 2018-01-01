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

	public String toString() {
		return home + " : " + away;
	}

	public static Pair defaultValue() {
		return new Pair(-1, -1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(away);
		result = prime * result + Float.floatToIntBits(home);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		Pair other = (Pair) obj;
		if (Float.floatToIntBits(away) != Float.floatToIntBits(other.away))
			return false;
		if (Float.floatToIntBits(home) != Float.floatToIntBits(other.home))
			return false;
		return true;
	}

}
