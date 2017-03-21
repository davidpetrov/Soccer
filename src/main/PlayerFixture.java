package main;

public class PlayerFixture {
	public ExtendedFixture fixture;
	public String team;
	public String name;
	public int minutesPlayed;
	public boolean lineup;
	public boolean substitute;
	public int goals;
	public int assists;

	public PlayerFixture(ExtendedFixture fixture, String team, String name, int minutesPlayed, boolean lineup,
			boolean substitute, int goals, int assists) {
		super();
		this.fixture = fixture;
		this.team = team;
		this.name = name;
		this.minutesPlayed = minutesPlayed;
		this.lineup = lineup;
		this.substitute = substitute;
		this.goals = goals;
		this.assists = assists;
	}

	public String toString() {
		return fixture + "/n" + name + " " + lineup + " " + minutesPlayed + "' " + goals + " " + assists;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fixture == null) ? 0 : fixture.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PlayerFixture))
			return false;
		PlayerFixture other = (PlayerFixture) obj;
		if (fixture == null) {
			if (other.fixture != null)
				return false;
		} else if (!fixture.equals(other.fixture))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}
	
	
}
