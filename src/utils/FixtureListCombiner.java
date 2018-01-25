package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import entries.FinalEntry;
import main.Fixture;

public class FixtureListCombiner {
	ArrayList<? extends Fixture> odds;
	ArrayList<? extends Fixture> gameStats;
	String competition;

	public FixtureListCombiner(ArrayList<? extends Fixture> odds, ArrayList<? extends Fixture> gameStats,
			String competition) {
		super();
		this.odds = odds;
		this.gameStats = gameStats;
		this.competition = competition;
	}

	public HashMap<String, String> deduceDictionary() {

		if (odds.size() != gameStats.size())
			System.out.println("Deducing dictionary with different sizes for: " + competition + " " + odds.size() + " "
					+ gameStats.size());
		HashMap<String, String> dictionary = new HashMap<>();

		ArrayList<String> teamsOdds = getTeamsList(odds);
		ArrayList<String> teamsgameStats = getTeamsList(gameStats);

		ArrayList<String> matchedOdds = new ArrayList<>();
		ArrayList<String> matchedgameStats = new ArrayList<>();

		// find direct matchesss
		for (String i : teamsOdds) {
			for (String j : teamsgameStats) {
				if (i.equals(j)) {
					matchedOdds.add(i);
					matchedgameStats.add(j);
					dictionary.put(i, i);
				}
			}
		}

		for (String team : teamsOdds) {
			if (matchedOdds.contains(team))
				continue;

			ArrayList<String> possibleCandidates = getPossibleCandidates(team, teamsgameStats, matchedgameStats);

			if (possibleCandidates.isEmpty())
				System.out.println("No possible candidates for:  " + team);

			String bestMatch = null;
			double bestSimilarity = -1d;
			for (String pos : possibleCandidates) {
				double similarity = Utils.similarity(team, pos);
				if (similarity > bestSimilarity) {
					bestSimilarity = similarity;
					bestMatch = pos;
				}
			}

			matchedOdds.add(team);
			if (bestMatch != null) {
				matchedgameStats.add(bestMatch);
				dictionary.put(team, bestMatch);
			} else {
				System.out.println();
			}

		}

		if (matchedOdds.size() != teamsOdds.size())
			System.err.println("Deducing dictionary failed");

		return dictionary;

	}

	private ArrayList<String> getPossibleCandidates(String team, ArrayList<String> teamsgameStats,
			ArrayList<String> matchedgameStats) {
		ArrayList<String> possibleCandidates = new ArrayList<>();
		ArrayList<? extends Fixture> fixtures = getFixturesList(team, odds);
		for (String tgameStats : teamsgameStats) {
			if (!matchedgameStats.contains(tgameStats)) {
				ArrayList<? extends Fixture> fwa = getFixturesList(tgameStats, gameStats);
				if (matchesFixtureLists(team, fixtures, fwa)) {
					possibleCandidates.add(tgameStats);
				}
			}
		}

		return possibleCandidates;
	}

	public ArrayList<Fixture> combineWithDictionary() {
		HashMap<String, String> dictionary = deduceDictionary();

		ArrayList<Fixture> combined = new ArrayList<>();
		for (Fixture i : odds) {
			Fixture matchedGS = findCorresponding(i, gameStats, dictionary);
			if (matchedGS == null)
				System.out.println("No match found for: \n" + i);
			else
				combined.add(matchedGS);
		}

		return combined;

	}

	private Fixture findCorresponding(Fixture i2, ArrayList<? extends Fixture> gameStats,
			HashMap<String, String> dictionary) {

		HashMap<String, String> reverseDictionary = (HashMap<String, String>) dictionary.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		for (Fixture i : gameStats) {
			if (i2.getHomeTeam().equals(reverseDictionary.get(i.getHomeTeam()))
					&& i2.getAwayTeam().equals(reverseDictionary.get(i.getAwayTeam()))
					&& (Math.abs(i.getDate().getTime() - i2.getDate().getTime()) <= 24 * 60 * 60 * 1000)) {

				Fixture ef = i2.withGameStats(i.getGameStats());
				return ef;
			}
		}

		return null;
	}

	/**
	 * Checks if two lists of fixtures are the same, but different team names
	 * 
	 * @param team
	 * @param fixtures
	 * @param fwa
	 * @return
	 */
	private boolean matchesFixtureLists(String team, ArrayList<? extends Fixture> fixtures,
			ArrayList<? extends Fixture> fwa) {
		for (Fixture i : fixtures) {
			boolean foundMatch = false;
			for (Fixture j : fwa) {
				if (Math.abs(i.getDate().getTime() - j.getDate().getTime()) <= 24 * 60 * 60 * 1000
						&& i.getResult().equals(j.getResult()) /* && i.HTresult.equals(j.HTresult) */) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				return false;

		}

		return true;
	}

	private ArrayList<String> getTeamsList(ArrayList<? extends Fixture> odds) {
		ArrayList<String> result = new ArrayList<>();
		for (Fixture i : odds) {
			if (!result.contains(i.getHomeTeam()))
				result.add(i.getHomeTeam());
			if (!result.contains(i.getAwayTeam()))
				result.add(i.getAwayTeam());
		}
		return result;
	}

	/**
	 * 
	 * @param team
	 * @param fixtures
	 * @return list of the fixtures for the given team
	 */
	private ArrayList<? extends Fixture> getFixturesList(String team, ArrayList<? extends Fixture> fixtures) {
		return fixtures.stream().filter(i -> i.getHomeTeam().equals(team) || i.getAwayTeam().equals(team))
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
