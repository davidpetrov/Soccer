package main;

import java.util.Date;

public class ExtendedFixture implements Comparable<ExtendedFixture> {
	public Date date;
	public String homeTeam;
	public String awayTeam;
	public Result result;
	Result HTresult;
	public String status;
	public String competition;
	public int matchday;
	public float overOdds;
	public float underOdds;
	public float maxOver;
	public float maxUnder;
	public float homeOdds;
	public float drawOdds;
	public float awayOdds;

	public ExtendedFixture(Date date, String homeTeam, String awayTeam, Result result, String competition) {
		this.date = date;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.result = result;
		this.competition = competition;
	}

	public ExtendedFixture withOdds(float overOdds, float underOdds, float maxOver, float maxUnder) {
		this.overOdds = overOdds;
		this.underOdds = underOdds;
		this.maxOver = maxOver;
		this.maxUnder = maxUnder;
		return this;
	}

	public ExtendedFixture with1X2Odds(float homeOdds, float drawOdds, float awayOdds) {
		this.homeOdds = homeOdds;
		this.drawOdds = drawOdds;
		this.awayOdds = awayOdds;
		return this;
	}

	public ExtendedFixture withStatus(String status) {
		this.status = status;
		return this;
	}

	public ExtendedFixture withHTResult(Result ht) {
		this.HTresult = ht;
		return this;
	}

	public ExtendedFixture withMatchday(int matchday) {
		this.matchday = matchday;
		return this;
	}

	public int getTotalGoals() {
		return result.goalsHomeTeam + result.goalsAwayTeam;
	}

	public double getHalfTimeGoals() {

		return HTresult.goalsHomeTeam + HTresult.goalsAwayTeam;
	}

	public boolean bothTeamScore() {
		return ((result.goalsAwayTeam > 0) && (result.goalsHomeTeam > 0));
	}

	@Override
	public String toString() {
		return date + " " + status + "\n" + homeTeam + " " + result.goalsHomeTeam + " : " + result.goalsAwayTeam + " "
				+ awayTeam + "\n";
	}

	@Override
	public int compareTo(ExtendedFixture o) {
		return date.compareTo(o.date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((awayTeam == null) ? 0 : awayTeam.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((homeTeam == null) ? 0 : homeTeam.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedFixture other = (ExtendedFixture) obj;
		if (awayTeam == null) {
			if (other.awayTeam != null)
				return false;
		} else if (!awayTeam.equals(other.awayTeam))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (homeTeam == null) {
			if (other.homeTeam != null)
				return false;
		} else if (!homeTeam.equals(other.homeTeam))
			return false;
		return true;
	}

}
