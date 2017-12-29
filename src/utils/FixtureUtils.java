package utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import entries.FinalEntry;
import main.Fixture;
import main.Fixture;
import main.Result;
import settings.Settings;

public class FixtureUtils {

	/**
	 * Adds match day for each fixture (It's guessed based on number of previous
	 * games played, it is possible to be different than the real one)
	 * 
	 * @param all
	 * @return the max match day i.e. the last one played
	 */
	public static int addMatchDay(ArrayList<Fixture> all) {
		int max = -1;
		for (Fixture f : all) {
			if (f.matchday == -1 || f.matchday == 0) {
				f.matchday = selectLastAll(all, f.homeTeam, 50, f.date).size() + 1;
				if (f.matchday > max)
					max = f.matchday;
			}
		}

		return max;
	}

	/**
	 * Select the last count fixtures (both home and away) for the given team
	 * from all fixtures list
	 * 
	 * @param all
	 * @param team
	 * @param count
	 * @param date
	 * @return
	 */
	public static ArrayList<Fixture> selectLastAll(ArrayList<Fixture> all, String team, int count, Date date) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : all) {
			if ((i.homeTeam.equals(team) || i.awayTeam.equals(team)) && i.date.before(date)) {
				result.add(i);
			}
		}

		return getLastFixtures(result, count);
	}

	/**
	 * Returns the last n fixtures based on data from a list of fixtures for a
	 * given team
	 * 
	 * @param fixtures
	 * @param n
	 * @return
	 */

	public static ArrayList<Fixture> getLastFixtures(ArrayList<Fixture> fixtures, int n) {
		int returnedSize = fixtures.size() >= n ? n : fixtures.size();
		fixtures.sort(Comparator.comparing(Fixture::getDate).reversed());

		return new ArrayList<>(fixtures.subList(0, returnedSize));
	}

	/**
	 * Return list the fixtures for the given match day from a list (the current
	 * fixtures)
	 * 
	 * @param all
	 * @param i
	 * @return
	 */
	public static ArrayList<Fixture> getByMatchday(ArrayList<Fixture> all, int i) {
		ArrayList<Fixture> filtered = new ArrayList<>();
		for (Fixture f : all) {
			if (f.matchday == i)
				filtered.add(f);
		}
		return filtered;
	}

	/**
	 * Return list the fixtures for before given match day from a list (the past
	 * fixtures)
	 * 
	 * @param all
	 * @param i
	 * @return
	 */
	public static ArrayList<Fixture> getBeforeMatchday(ArrayList<Fixture> all, int i) {
		ArrayList<Fixture> filtered = new ArrayList<>();
		for (Fixture f : all) {
			if (f.matchday < i)
				filtered.add(f);
		}
		return filtered;
	}

	public static ArrayList<FinalEntry> runWithSettingsList(ArrayList<Fixture> all, ArrayList<Fixture> current,
			Settings settings) {
		ArrayList<FinalEntry> finals = calculateScores(all, current, settings);

		return FinalsUtils.restrict(finals, settings);
	}

	/**
	 * Calculate the prediction (the probability for over 2.5 goals) for a
	 * fixture based on settings
	 * 
	 * @param all
	 * @param current
	 * @param settings
	 * @return
	 */
	public static ArrayList<FinalEntry> calculateScores(ArrayList<Fixture> all, ArrayList<Fixture> current,
			Settings settings) {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : current) {

			float finalScore = 0f;

			// if (settings.basic != 0f)
			// finalScore += settings.basic * basic2(f, sheet, 0.6f, 0.3f,
			// 0.1f);
			//
			// if (settings.poisson != 0f)
			// finalScore += settings.poisson * poisson(f, sheet);
			//
			// if (settings.weightedPoisson != 0f)
			// finalScore += settings.weightedPoisson * poissonWeighted(f,
			// sheet);
			//
			if (settings.htCombo != 0f)
				finalScore += settings.htCombo * Classifiers.halfTime(f, all, settings.halfTimeOverOne);

			if (settings.shots != 0f)
				finalScore += settings.shots * Classifiers.shots(f, all);

			FinalEntry fe = new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					settings.threshold, settings.lowerBound, settings.upperBound);
			finals.add(fe);
		}
		return finals;
	}

	/**
	 * Calculates the average number of shots per game for the home team for all
	 * fixtures before the given date
	 * 
	 * @param all
	 * @param date
	 * @return
	 */
	public static float selectAvgShotsHome(ArrayList<Fixture> all, Date date) {
		float sum = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date)) {
				sum += i.getShotsHome();
				count++;
			}

		return all.size() == 0 ? 0f : sum / count;
	}

	/**
	 * Calculates the average number of shots per game for the away team for all
	 * fixtures before the given date
	 * 
	 * @param all
	 * @param date
	 * @return
	 */
	public static float selectAvgShotsAway(ArrayList<Fixture> all, Date date) {
		float sum = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date)) {
				sum += i.gameStats.getShotsAway();
				count++;
			}

		return all.size() == 0 ? 0f : sum / count;
	}

	/**
	 * Calculates the average number of shots per game for the both home and
	 * away team for all fixtures before the given date
	 * 
	 * @param all
	 * @param date
	 * @param manual
	 * @param goalsWeight
	 * @return
	 * 
	 */
	public static Pair selectAvgShots(ArrayList<Fixture> all, Date date, boolean manual, float goalsWeight) {
		float sumHome = 0f;
		float sumAway = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date)) {
				sumHome += i.getShotsHome();
				sumAway += i.getShotsAway();
				if (manual) {
					sumHome += goalsWeight * i.result.goalsHomeTeam;
					sumAway += goalsWeight * i.result.goalsAwayTeam;
				}

				count++;
			}

		float avgHome = all.size() == 0 ? 0f : sumHome / count;
		float avgAway = all.size() == 0 ? 0f : sumAway / count;
		return Pair.of(avgHome, avgAway);
	}

	/**
	 * Calculates the average number of shots per game when the game is under
	 * 2.5 and when it is over for all fixtures before the given date
	 * 
	 * @param all
	 * @param date
	 * @param manual
	 * @param goalsWeight
	 * @return
	 * 
	 */
	public static Pair selectAvgShotsByType(ArrayList<Fixture> all, Date date, boolean manual, float goalsWeight) {
		float sumUnder = 0f;
		float sumOver = 0f;
		int countUnder = 0;
		int countOver = 0;

		for (Fixture i : all)
			if (i.date.before(date))
				if (i.getTotalGoals() < 2.5) {
					sumUnder += i.getShotsTotal();
					if (manual)
						sumUnder += goalsWeight * i.getTotalGoals();
					countUnder++;
				} else {
					sumOver += i.getShotsTotal();
					if (manual)
						sumOver += goalsWeight * i.getTotalGoals();
					countOver++;
				}
		float avgUnder = countUnder == 0 ? 0f : sumUnder / countUnder;
		float avgOver = countOver == 0 ? 0f : sumOver / countOver;
		return Pair.of(avgUnder, avgOver);

	}

	/**
	 * Calculate the averages shots for home team for and against
	 * 
	 * @param all
	 * @param homeTeam
	 * @param date
	 * @param goalsWeight
	 * @return
	 */
	public static Pair selectAvgShotsHome(ArrayList<Fixture> all, String homeTeam, Date date, boolean manual,
			float goalsWeight) {
		float sumFor = 0f;
		float sumAgainst = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date) && i.homeTeam.equals(homeTeam)) {
				sumFor += i.getShotsHome();
				sumAgainst += i.getShotsAway();
				if (manual) {
					sumFor += goalsWeight * i.result.goalsHomeTeam;
					sumAgainst += goalsWeight * i.result.goalsAwayTeam;
				}
				count++;
			}

		float avgHome = all.size() == 0 ? 0f : sumFor / count;
		float avgAway = all.size() == 0 ? 0f : sumAgainst / count;
		return Pair.of(avgHome, avgAway);
	}

	/**
	 * Calculate the averages shots for home team for and against
	 * 
	 * @param all
	 * @param homeTeam
	 * @param date
	 * @param goalsWeight
	 * @return
	 */
	public static Pair selectAvgShotsAway(ArrayList<Fixture> all, String awayTeam, Date date, boolean manual,
			float goalsWeight) {
		float sumFor = 0f;
		float sumAgainst = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date) && i.awayTeam.equals(awayTeam)) {
				sumFor += i.gameStats.getShotsAway();
				sumAgainst += i.gameStats.getShotsHome();
				if (manual) {
					sumFor += goalsWeight * i.result.goalsAwayTeam;
					sumAgainst += goalsWeight * i.result.goalsHomeTeam;
				}
				count++;
			}

		float avgHome = all.size() == 0 ? 0f : sumFor / count;
		float avgAway = all.size() == 0 ? 0f : sumAgainst / count;
		return Pair.of(avgHome, avgAway);
	}

	/**
	 * Calculate the average goal for the homeTeam
	 * 
	 * @param all
	 * @param homeTeam
	 * @param date
	 * @return
	 */
	public static Pair selectAvgHomeTeam(ArrayList<Fixture> all, String homeTeam, Date date) {
		float sumFor = 0f;
		float sumAgainst = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date) && i.homeTeam.equals(homeTeam)) {
				sumFor += i.result.goalsHomeTeam;
				sumAgainst += i.result.goalsAwayTeam;
				count++;
			}

		float forr = count == 0 ? 0f : sumFor / count;
		float against = count == 0 ? 0f : sumAgainst / count;
		return Pair.of(forr, against);

	}

	/**
	 * Calculates the averages for and against goal for the given away team
	 * playing away
	 * 
	 * @param all
	 * @param awayTeam
	 * @param date
	 * @return
	 */
	public static Pair selectAvgAwayTeam(ArrayList<Fixture> all, String awayTeam, Date date) {
		float sumFor = 0f;
		float sumAgainst = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date) && i.awayTeam.equals(awayTeam)) {
				sumFor += i.result.goalsAwayTeam;
				sumAgainst = i.result.goalsHomeTeam;
				count++;
			}

		float forr = count == 0 ? 0f : sumFor / count;
		float against = count == 0 ? 0f : sumAgainst / count;
		return Pair.of(forr, against);
	}

	/**
	 * Calculates the averages goals for home team and away team
	 * 
	 * @param all
	 * @param awayTeam
	 * @param date
	 * @return
	 */
	public static Pair selectAvgHomeAway(ArrayList<Fixture> all, Date date) {
		float sumHome = 0f;
		float sumAway = 0f;
		int count = 0;

		for (Fixture i : all)
			if (i.date.before(date)) {
				sumHome += i.result.goalsHomeTeam;
				sumAway = i.result.goalsAwayTeam;
				count++;
			}

		float forr = count == 0 ? 0f : sumHome / count;
		float against = count == 0 ? 0f : sumAway / count;
		return Pair.of(forr, against);
	}

}
