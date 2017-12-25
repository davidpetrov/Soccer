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

	private GameStats withGameStats;
	
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

	@Override
	public String toString() {
		return date + " " + homeTeam + " " + result.goalsHomeTeam + " : " + result.goalsAwayTeam + " " + awayTeam;
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
		this.matchOdds = new ArrayList<>();
		for (ArrayList<MatchOdds> i : matchOdds.values())
			this.matchOdds.addAll(i);
		return this;
	}

	// For loading from db
	public Fixture withMatchOddsList(ArrayList<MatchOdds> matchOdds) {
		this.matchOdds = matchOdds;
		return this;
	}

	public Fixture withAsianOddsList(ArrayList<AsianOdds> asianOdds) {
		this.asianOdds = asianOdds;
		return this;
	}

	public Fixture withOverUnderOddsList(ArrayList<OverUnderOdds> overUnderOdds) {
		this.overUnderOdds = overUnderOdds;
		return this;
	}

	public Fixture withGameStats(GameStats gameStats) {
		this.withGameStats = gameStats;
		return this;
	}

}
