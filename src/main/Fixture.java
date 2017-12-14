package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;

public class Fixture {

	public Date date;
	public int year;
	public String competition;
	public int matchday;
	public String homeTeam;
	public String awayTeam;
	public Result result;
	public Result HTresult;
	public String status;

	// stats data
	public int shotsHome;
	public int shotsAway;
	// public int shotsWideHome;
	// public int shotsWideAway;
	// public int cornersHome;
	// public int cornersAway;
	// public int foulsHome;
	// public int foulsAway;
	// public int offsidesHome;
	// public int offsidesAway;
	// public int possessionHome;
	// public int redCardsHome;
	// public int redCardsAway;
	// public int yellowCardsHome;
	// public int yellowCardsAway;

	ArrayList<MatchOdds> matchOdds;
	ArrayList<AsianOdds> asianOdds;
	ArrayList<OverUnderOdds> overUnderOdds;

	public Fixture(Date date, int year, String competition, String homeTeam, String awayTeam, Result result) {
		super();
		this.date = date;
		this.year = year;
		this.competition = competition;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.result = result;
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
		if (!(obj instanceof Fixture))
			return false;
		Fixture other = (Fixture) obj;
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

	public Fixture withHTResult(Result htResult) {
		this.HTresult = htResult;
		return this;
	}

	public Fixture withOUodds(HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overUnderOdds) {
		this.overUnderOdds = new ArrayList<>();
		for (HashMap<String, ArrayList<OverUnderOdds>> i : overUnderOdds.values())
			for (ArrayList<OverUnderOdds> j : i.values())
				this.overUnderOdds.addAll(j);
		return this;
	}

	public Fixture withAsianOdds(HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> asianOdds) {
		this.asianOdds = new ArrayList<>();
		for (HashMap<String, ArrayList<AsianOdds>> i : asianOdds.values())
			for (Collection<AsianOdds> j : i.values())
				this.asianOdds.addAll(j);
		return this;
	}

	public Fixture withMatchOdds(HashMap<String, ArrayList<MatchOdds>> matchOdds) {
		this.matchOdds =  new ArrayList<>();
		for (ArrayList<MatchOdds> i : matchOdds.values())
			this.matchOdds.addAll(i);
		return this;
	}

}
