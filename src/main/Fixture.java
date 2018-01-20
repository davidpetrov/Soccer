package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import constants.Constants;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;
import utils.Pair;

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

	public GameStats gameStats;

	public ArrayList<MatchOdds> matchOdds;
	public ArrayList<AsianOdds> asianOdds;
	public ArrayList<OverUnderOdds> overUnderOdds;

	public Fixture(Date date, String competition, String homeTeam, String awayTeam, Result result) {
		super();
		this.date = date;
		this.competition = competition;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.result = result;
		this.matchOdds = new ArrayList<>();
		this.asianOdds = new ArrayList<>();
		this.overUnderOdds = new ArrayList<>();
		this.gameStats = GameStats.initial();
	}

	/**
	 * Copy constructor Does not copy oddsdata
	 */
	public Fixture(Fixture f) {
		this.date = f.date;
		this.competition = f.competition;
		this.homeTeam = f.homeTeam;
		this.awayTeam = f.awayTeam;
		this.result = f.result;
		this.matchOdds = new ArrayList<>();
		this.asianOdds = new ArrayList<>();
		this.overUnderOdds = new ArrayList<>();
		this.gameStats = f.gameStats;
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
		for (HashMap<String, ArrayList<OverUnderOdds>> i : overUnderOdds.values())
			for (ArrayList<OverUnderOdds> j : i.values())
				this.overUnderOdds.addAll(j);
		return this;
	}

	public Fixture withAsianOdds(HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> asianOdds) {
		for (HashMap<String, ArrayList<AsianOdds>> i : asianOdds.values())
			for (Collection<AsianOdds> j : i.values())
				this.asianOdds.addAll(j);
		return this;
	}

	public Fixture withMatchOdds(HashMap<String, ArrayList<MatchOdds>> matchOdds) {
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

	public Fixture withYear(int year) {
		this.year = year;
		return this;
	}

	public Fixture withGameStats(GameStats gameStats) {
		this.gameStats = gameStats;
		return this;
	}

	public String getHomeTeam() {
		return homeTeam;
	}

	public String getAwayTeam() {
		return awayTeam;
	}

	public Date getDate() {
		return date;
	}

	public Result getResult() {
		return result;
	}

	public GameStats getGameStats() {
		return gameStats;
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

	public boolean isHomeWin() {
		return result.goalsHomeTeam > result.goalsAwayTeam;
	}

	public boolean isAwayWin() {
		return result.goalsHomeTeam < result.goalsAwayTeam;
	}

	public boolean isDraw() {
		return result.goalsHomeTeam == result.goalsAwayTeam;
	}

	public int getShotsTotal() {
		return (int) gameStats.shots.home + (int) gameStats.shots.away;
	}

	public int getShotsHome() {
		return (int) gameStats.shots.home;
	}

	public int getShotsAway() {
		return (int) gameStats.shots.away;
	}

	public HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> getOUByLineandBookie() {
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> result = new HashMap<>();
		for (OverUnderOdds ou : overUnderOdds) {
			float line = ou.line;
			String bookmaker = ou.bookmaker;

			if (!result.containsKey(line))
				result.put(line, new HashMap<>());

			if (!result.get(line).containsKey(bookmaker))
				result.get(line).put(bookmaker, new ArrayList<>());

			result.get(line).get(bookmaker).add(ou);
		}

		return result;
	}

	public HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> getAsianByLineandBookie() {
		HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> result = new HashMap<>();
		for (AsianOdds ao : asianOdds) {
			float line = ao.line;
			String bookmaker = ao.bookmaker;

			if (!result.containsKey(line))
				result.put(line, new HashMap<>());

			if (!result.get(line).containsKey(bookmaker))
				result.get(line).put(bookmaker, new ArrayList<>());

			result.get(line).get(bookmaker).add(ao);
		}

		return result;
	}

	float maxClosingOverOdds = -1f;

	// methods for leagacy code (line=2.5 maxOdds) closing
	// TODO test for correctness
	public float getMaxClosingOverOdds() {
		if (maxClosingOverOdds != -1f)
			return maxClosingOverOdds;

		HashMap<String, ArrayList<OverUnderOdds>> baseLineOdds = getOUByLineandBookie().get(2.5f);

		// baseLineOdds.keySet().stream().forEach(bookie ->
		// System.out.println("\"" + bookie + "\","));

		ArrayList<ArrayList<OverUnderOdds>> filteredBookies = baseLineOdds == null ? new ArrayList<>()
				: baseLineOdds.entrySet().stream()
						.filter(map -> !Arrays.asList(Constants.FAKEBOOKS).contains(map.getKey()))
						.map(map -> map.getValue()).collect(Collectors.toCollection(ArrayList::new));

		float maxClosing = -1f;
		for (ArrayList<OverUnderOdds> list : filteredBookies) {
			Optional<OverUnderOdds> closing = list.stream().max(Comparator.comparing(OverUnderOdds::getTime));
			if (closing.isPresent())
				if (closing.get().getOverOdds() > maxClosing)
					maxClosing = closing.get().getOverOdds();
		}

		maxClosingOverOdds = maxClosing;
		return maxClosing;
	}

	float maxClosingUnderOdds = -1f;

	public float getMaxClosingUnderOdds() {
		if (maxClosingUnderOdds != -1f)
			return maxClosingUnderOdds;

		HashMap<String, ArrayList<OverUnderOdds>> baseLineOdds = getOUByLineandBookie().get(2.5f);

		ArrayList<ArrayList<OverUnderOdds>> filteredBookies = baseLineOdds == null ? new ArrayList<>()
				: baseLineOdds.entrySet().stream()
						.filter(map -> !Arrays.asList(Constants.FAKEBOOKS).contains(map.getKey()))
						.map(map -> map.getValue()).collect(Collectors.toCollection(ArrayList::new));

		float maxClosing = -1f;
		for (ArrayList<OverUnderOdds> list : filteredBookies) {
			Optional<OverUnderOdds> closing = list.stream().max(Comparator.comparing(OverUnderOdds::getTime));
			if (closing.isPresent())
				if (closing.get().getUnderOdds() > maxClosing)
					maxClosing = closing.get().getUnderOdds();
		}

		maxClosingUnderOdds = maxClosing;
		return maxClosing;
	}

	public float getMaxClosingHomeOdds() {
		float maxClosing = -1f;
		HashMap<String, ArrayList<MatchOdds>> matchOddsHash = getMatchOddsByBookie();

		for (ArrayList<MatchOdds> list : matchOddsHash.values()) {
			Optional<MatchOdds> closing = list.stream().max(Comparator.comparing(MatchOdds::getTime));
			if (closing.isPresent())
				if (closing.get().getHomeOdds() > maxClosing)
					maxClosing = closing.get().getHomeOdds();
		}

		return maxClosing;
	}

	private HashMap<String, ArrayList<MatchOdds>> getMatchOddsByBookie() {
		HashMap<String, ArrayList<MatchOdds>> result = new HashMap<>();
		for (MatchOdds mo : matchOdds) {
			String bookmaker = mo.bookmaker;

			if (!result.containsKey(bookmaker))
				result.put(bookmaker, new ArrayList<>());

			result.get(bookmaker).add(mo);
		}

		return result;
	}

	public float getMaxClosingDrawOdds() {
		float maxClosing = -1f;
		HashMap<String, ArrayList<MatchOdds>> matchOddsHash = getMatchOddsByBookie();

		for (ArrayList<MatchOdds> list : matchOddsHash.values()) {
			Optional<MatchOdds> closing = list.stream().max(Comparator.comparing(MatchOdds::getTime));
			if (closing.isPresent())
				if (closing.get().getHomeOdds() > maxClosing)
					maxClosing = closing.get().getDrawOdds();
		}

		return maxClosing;
	}

	public float getMaxClosingAwayOdds() {
		float maxClosing = -1f;
		HashMap<String, ArrayList<MatchOdds>> matchOddsHash = getMatchOddsByBookie();

		for (ArrayList<MatchOdds> list : matchOddsHash.values()) {
			Optional<MatchOdds> closing = list.stream().max(Comparator.comparing(MatchOdds::getTime));
			if (closing.isPresent())
				if (closing.get().getHomeOdds() > maxClosing)
					maxClosing = closing.get().getAwayOdds();
		}

		return maxClosing;
	}

	public Fixture withMatchday(int matchd) {
		this.matchday = matchd;
		return this;
	}

	public Fixture withStatus(String status) {
		this.status = status;
		return this;
	}

	public Fixture withShots(int home, int away) {
		this.gameStats.shots = Pair.of(home, away);
		return this;
	}

	// TODO test this
	public float getOptimalAsianLine() {
		HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> byLineAndBookie = getAsianByLineandBookie();
		float minDiff = Float.MAX_VALUE;
		float optimalLine = -0.5f;

		for (Entry<Float, HashMap<String, ArrayList<AsianOdds>>> entry : byLineAndBookie.entrySet()) {
			float line = entry.getKey();
			ArrayList<Optional<AsianOdds>> closing = entry.getValue().values().stream()
					.map(list -> list.stream().max(Comparator.comparing(AsianOdds::getTime)))
					.collect(Collectors.toCollection(ArrayList::new));

			float avgDiff = (float) closing.stream().filter(Optional::isPresent).map(Optional::get)
					.mapToDouble(AsianOdds::getOddsDiff).average().getAsDouble();

			if (avgDiff < minDiff) {
				avgDiff = minDiff;
				optimalLine = line;
			}
		}

		return optimalLine;
	}

	// TODO test that
	public Pair getMaxClosingAsian() {

		float maxHome = 1f;
		float maxAway = 1f;

		for (ArrayList<AsianOdds> list : getAsianByLineandBookie().get(getOptimalAsianLine()).values()) {
			Optional<AsianOdds> closing = list.stream().max(Comparator.comparing(AsianOdds::getTime));
			if (closing.isPresent()) {
				if (closing.get().homeOdds > maxHome)
					maxHome = closing.get().homeOdds;
				if (closing.get().awayOdds > maxAway)
					maxAway = closing.get().awayOdds;
			}
		}

		return Pair.of(maxHome, maxAway);
	}

	// TODO test this
	public float getOptimalOULine() {
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> byLineAndBookie = getOUByLineandBookie();
		float minDiff = Float.MAX_VALUE;
		float optimalLine = -0.50f;

		for (Entry<Float, HashMap<String, ArrayList<OverUnderOdds>>> entry : byLineAndBookie.entrySet()) {
			float line = entry.getKey();

			
			HashMap<String, ArrayList<OverUnderOdds>> filtered = new HashMap<>();
			for (Entry<String, ArrayList<OverUnderOdds>> i : entry.getValue().entrySet()) {
				if (!Arrays.asList(Constants.FAKEBOOKS).contains(i.getKey())) {
					filtered.put(i.getKey(), i.getValue());
				}
			}
			
			if(filtered.isEmpty())
				continue;
			
			ArrayList<Optional<OverUnderOdds>> closing = filtered.values().stream()
					.map(list -> list.stream().max(Comparator.comparing(OverUnderOdds::getTime)))
					.collect(Collectors.toCollection(ArrayList::new));

			float avgDiff = (float) closing.stream().filter(Optional::isPresent).map(Optional::get)
					.mapToDouble(OverUnderOdds::getOddsDiff).average().getAsDouble();

			if (avgDiff < minDiff) {
				minDiff = avgDiff;
				optimalLine = line;
			}
		}

		return optimalLine;
	}

	/**
	 * Cashing of baseOULines
	 */
	ArrayList<Float> baseOULines = new ArrayList<>();

	public ArrayList<Float> getBaseOULines() {
		if (!baseOULines.isEmpty())
			return baseOULines;

		float optimalLine = getOptimalOULine();
		baseOULines.add(optimalLine - 0.5f);
		baseOULines.add(optimalLine - 0.25f);
		baseOULines.add(optimalLine);
		baseOULines.add(optimalLine + 0.25f);
		baseOULines.add(optimalLine + 0.5f);
		return baseOULines;
	}

	HashMap<Float, ArrayList<OverUnderOdds>> maxClosingOUOdds = new HashMap<>();

	/**
	 * Returns list of 2 odds, the first - max closing overOdds, the second - max
	 * closing under Odds for the line Possible better implementation
	 * 
	 * @param line
	 * @return
	 */
	public ArrayList<OverUnderOdds> getMaxClosingOUOddsByLine(float line) {
		if (maxClosingOUOdds.get(line) != null)
			return maxClosingOUOdds.get(line);

		OverUnderOdds maxHome = OverUnderOdds.defaultOdds();
		OverUnderOdds maxAway = OverUnderOdds.defaultOdds();

		if (overUnderOdds == null || getOUByLineandBookie().get(line) == null) {
			ArrayList<OverUnderOdds> defaulList = new ArrayList<>();
			defaulList.add(OverUnderOdds.defaultOdds());
			defaulList.add(OverUnderOdds.defaultOdds());
			return defaulList;
		}

		for (ArrayList<OverUnderOdds> list : getOUByLineandBookie().get(line).values()) {
			if (!list.isEmpty() && Arrays.asList(Constants.FAKEBOOKS).contains(list.get(0).bookmaker))
				continue;
			Optional<OverUnderOdds> closing = list.stream().max(Comparator.comparing(OverUnderOdds::getTime));
			if (closing.isPresent()) {
				if (closing.get().overOdds > maxHome.overOdds)
					maxHome = closing.get();
				if (closing.get().underOdds > maxAway.underOdds)
					maxAway = closing.get();
			}
		}

		ArrayList<OverUnderOdds> result = new ArrayList<>();
		result.add(maxHome);
		result.add(maxAway);
		maxClosingOUOdds.put(line, result);
		return result;
	}

	/**
	 * Cashe for Pinnacle odds
	 */
	HashMap<Float, OverUnderOdds> pinnOdds = new HashMap<>();

	/**
	 * Closing odds for specific line and bookmaker if present
	 * 
	 * @param line
	 * @param bookmaker
	 * @return
	 */
	public OverUnderOdds getMaxCloingByLineAndBookie(float line, String bookmaker) {
		if (bookmaker.equals("Pinnacle") && pinnOdds.containsKey(line))
			return pinnOdds.get(line);

		if(overUnderOdds == null)
			return null;
		HashMap<String, ArrayList<OverUnderOdds>> bookMap = getOUByLineandBookie().get(line);
		if (bookMap == null)
			return null;
		ArrayList<OverUnderOdds> list = bookMap.get(bookmaker);
		if (list == null || list.isEmpty())
			return null;

		Optional<OverUnderOdds> closing = list.stream().max(Comparator.comparing(OverUnderOdds::getTime));
		pinnOdds.put(line, closing.get());
		return closing.get();
	}

}
