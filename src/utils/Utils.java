package utils;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.cyberneko.html.HTMLScanner.CurrentEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import charts.LineChart;
import constants.Constants;
import entries.AsianEntry;
import entries.FinalEntry;
import entries.FullEntry;
import entries.HTEntry;
import main.Fixture;
import main.Fixture;
import main.GoalLines;
import main.Line;
import main.Player;
import main.PlayerFixture;
import main.Result;
import main.SQLiteJDBC;
import main.Test.DataType;
import odds.Odds;
import odds.OverUnderOdds;
import results.Results;
import scraper.Names;
import scraper.Scraper;
import settings.Settings;
import tables.Position;
import tables.Table;
import xls.AsianUtils;
import xls.XlSUtils;
import xls.XlSUtils.MaximizingBy;

public class Utils {
	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static long start = System.currentTimeMillis();

	public static ArrayList<Fixture> getLastFixtures(ArrayList<Fixture> fixtures, int n) {
		ArrayList<Fixture> last = new ArrayList<>();
		int returnedSize = fixtures.size() >= n ? n : fixtures.size();
		fixtures.sort(Comparator.comparing(Fixture::getDate).reversed());
		for (int i = 0; i < returnedSize; i++) {
			last.add(fixtures.get(i));
		}
		return last;
	}

	public static ArrayList<Fixture> getHomeFixtures(Fixture f, ArrayList<Fixture> fixtures) {
		ArrayList<Fixture> home = new ArrayList<>();
		for (Fixture i : fixtures) {
			if (f.homeTeam.equals(i.homeTeam))
				home.add(i);
		}
		return home;
	}

	public static ArrayList<Fixture> getAwayFixtures(Fixture f, ArrayList<Fixture> fixtures) {
		ArrayList<Fixture> away = new ArrayList<>();
		for (Fixture i : fixtures) {
			if (f.awayTeam.equals(i.awayTeam))
				away.add(i);
		}
		return away;
	}

	public static float countOverGamesPercent(ArrayList<Fixture> fixtures) {
		int count = 0;
		for (Fixture f : fixtures) {
			if (f.getTotalGoals() > 2.5d)
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static float countBTSPercent(ArrayList<Fixture> fixtures) {
		int count = 0;
		for (Fixture f : fixtures) {
			if (f.bothTeamScore())
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static float findAvg(ArrayList<Fixture> all) {
		float total = 0;
		for (Fixture f : all) {
			total += f.getTotalGoals();
		}
		return total / all.size();
	}

	public static float poisson(float lambda, int goal) {
		return (float) (Math.pow(lambda, goal) * Math.exp(-lambda) / CombinatoricsUtils.factorial(goal));
	}

	public static float poissonOver(float lambda, float mu) {
		float home[] = new float[3];
		float away[] = new float[3];
		for (int i = 0; i < 3; i++) {
			home[i] = poisson(lambda, i);
			away[i] = poisson(mu, i);
		}
		float totalUnder = 0;
		totalUnder += home[0] * away[0] + home[0] * away[1] + home[0] * away[2] + home[1] * away[0] + home[2] * away[0]
				+ home[1] * away[1];
		return 1.0f - totalUnder;
	}

	public static float poissonHome(float lambda, float mu, int offset) {
		float home[] = new float[10];
		float away[] = new float[10];
		for (int i = 0; i < 10; i++) {
			home[i] = poisson(lambda, i);
			away[i] = poisson(mu, i);
		}

		float totalHome = 0f;
		if (offset >= 0) {
			for (int i = offset + 1; i < 10; i++) {
				for (int j = 0; j < i - offset; j++) {
					// System.out.println(i + " : " + j);
					totalHome += home[i] * away[j];
				}
			}
		} else {
			for (int i = 0; i < 10 + offset; i++) {
				for (int j = 0; j < i - offset; j++) {
					// System.out.println(i + " : " + j);
					totalHome += home[i] * away[j];
				}
			}
		}

		return totalHome;
	}

	public static float poissonExact(float lambda, float mu, int offset) {
		float home[] = new float[10];
		float away[] = new float[10];
		for (int i = 0; i < 10; i++) {
			home[i] = poisson(lambda, i);
			away[i] = poisson(mu, i);
		}

		float totalHome = 0f;
		if (offset <= 0) {
			for (int i = 0; i < 10 + offset; i++) {
				totalHome += home[i] * away[i - offset];
				// System.out.println(i + " : " + (i - offset));
			}
		} else {
			for (int i = offset; i < 10; i++) {
				totalHome += home[i] * away[i - offset];
				// System.out.println(i + " : " + (i - offset));
			}
		}

		return totalHome;
	}

	public static float poissonAway(float lambda, float mu, int offset) {
		float home[] = new float[10];
		float away[] = new float[10];
		for (int i = 0; i < 10; i++) {
			home[i] = poisson(lambda, i);
			away[i] = poisson(mu, i);
		}

		float totalHome = 0f;
		for (int i = offset + 1; i < 10; i++) {
			for (int j = 0; j < i - offset; j++) {
				totalHome += home[j] * away[i];
			}
		}

		return totalHome;
	}

	public static Pair poissonAsianHome(float lambda, float mu, float line, float asianHome, float asianAway) {

		// System.out.println(poissonAway(lambda, mu, 0) + poissonDraw(lambda,
		// mu) + poissonHome(lambda, mu, 0));

		float fraction = line % 1;
		int whole = (int) (line - fraction);

		if (fraction == 0f) {
			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);

			// System.out.println("draws");
			float drawChance = poissonExact(lambda, mu, -whole);

			float home = winChance * asianHome + drawChance - (1f - winChance - drawChance);
			float away = (1f - winChance - drawChance) * asianAway + drawChance - winChance;
			return Pair.of(home, away);

		} else if (fraction == -0.5f) {
			line = whole - 1;
			whole = (int) (line - fraction);
			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);

			float home = winChance * asianHome - (1f - winChance);
			float away = (1f - winChance) * asianAway - winChance;
			return Pair.of(home, away);

		} else if (fraction == 0.5f) {
			line = (float) Math.ceil(line);
			fraction = line % 1;
			whole = (int) (line - fraction);

			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);

			float home = winChance * asianHome - (1f - winChance);
			float away = (1f - winChance) * asianAway - winChance;
			return Pair.of(home, away);

		} else if (fraction == -0.25f) {
			line = whole - 1;
			whole = (int) (line - fraction);
			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);
			// System.out.println("half loses");
			float drawChance = poissonExact(lambda, mu, -whole);

			float home = winChance * asianHome - drawChance / 2 - (1f - winChance - drawChance);
			float away = (1f - winChance - drawChance) * asianAway + drawChance * (1 + (asianAway - 1) / 2) - winChance;
			return Pair.of(home, away);

		} else if (fraction == 0.25f) {
			line = (float) Math.floor(line);
			fraction = line % 1;
			whole = (int) (line - fraction);

			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);
			// System.out.println("half wins");
			float drawChance = poissonExact(lambda, mu, -whole);

			float home = winChance * asianHome + drawChance * (1 + (asianHome - 1) / 2) - (1f - winChance - drawChance);
			float away = (1f - winChance - drawChance) * asianAway - drawChance / 2 - winChance;
			return Pair.of(home, away);

		} else if (fraction == -0.75f) {
			line = whole - 1;
			fraction = line % 1;
			whole = (int) (line - fraction);
			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);
			// System.out.println("half wins");
			float drawChance = poissonExact(lambda, mu, -whole);

			float home = winChance * asianHome + drawChance * (1 + (asianHome - 1) / 2) - (1f - winChance - drawChance);
			float away = (1f - winChance - drawChance) * asianAway - drawChance / 2 - winChance;
			return Pair.of(home, away);

		} else if (fraction == 0.75f) {
			line = (float) Math.ceil(line);
			fraction = line % 1;
			whole = (int) (line - fraction);
			// System.out.println("wins");
			float winChance = poissonHome(lambda, mu, -whole);
			// System.out.println("half loss");
			float drawChance = poissonExact(lambda, mu, -whole);

			float home = winChance * asianHome - drawChance / 2 - (1f - winChance - drawChance);
			float away = (1f - winChance - drawChance) * asianAway + (1 + (asianAway - 1) / 2) * drawChance - winChance;
			return Pair.of(home, away);
		} else {
			return Pair.of(-1, -1);
		}

	}

	public static float poissonDraw(float lambda, float mu, int offset) {
		float home[] = new float[5];
		float away[] = new float[5];
		for (int i = 0; i < 5; i++) {
			home[i] = poisson(lambda, i);
			away[i] = poisson(mu, i);
		}
		float totalUnder = 0;
		totalUnder += home[0] * away[0 + offset] + home[1] * away[1 + offset] + home[2] * away[2 + offset]
				+ home[3] * away[3 + offset] + home[4] * away[4 + offset];
		return totalUnder;
	}

	public static float avgFor(String team, ArrayList<Fixture> fixtures) {
		float total = 0;
		for (Fixture f : fixtures) {
			if (f.homeTeam.equals(team))
				total += f.result.goalsHomeTeam;
			if (f.awayTeam.equals(team))
				total += f.result.goalsAwayTeam;
		}
		return fixtures.size() == 0 ? 0 : total / fixtures.size();
	}

	public static float getSuccessRate(ArrayList<FinalEntry> list) {
		int success = 0;
		for (FinalEntry fe : list) {
			if (fe.success())
				success++;
		}
		return (float) success / list.size();
	}

	/**
	 * Filter by min and max odds
	 * 
	 * @param finals
	 * @param minOdds
	 * @param maxOdds
	 * @param threshold
	 * @return
	 */
	public static ArrayList<FinalEntry> filterByOdds(ArrayList<FinalEntry> finals, float minOdds, float maxOdds) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float coeff = fe.isOver() ? fe.fixture.getMaxClosingOverOdds() : fe.fixture.getMaxClosingUnderOdds();
			if (coeff > minOdds && coeff <= maxOdds)
				filtered.add(fe);
		}
		return filtered;
	}

	public static ArrayList<Fixture> onlyFixtures(ArrayList<FinalEntry> finals) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (FinalEntry fe : finals)
			result.add(fe.fixture);
		return result;
	}

	public static ArrayList<FinalEntry> underPredictions(ArrayList<FinalEntry> finals, Settings set) {
		ArrayList<FinalEntry> unders = new ArrayList<>(finals);
		for (FinalEntry fe : finals) {
			if (fe.prediction >= set.lowerBound)
				unders.remove(fe);
		}
		return unders;
	}

	public static ArrayList<FinalEntry> filterTrust(ArrayList<FinalEntry> finals, Settings trset) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			if (fe.prediction > trset.lowerBound && fe.prediction < trset.upperBound)
				continue;
			filtered.add(fe);
		}
		return filtered;
	}

	public static Settings getSettings(ArrayList<Settings> setts, int year) {
		for (Settings i : setts) {
			if (i.year == year)
				return i;
		}
		return null;
	}

	public static void overUnderStats(ArrayList<FinalEntry> finals) {
		int overCnt = 0, underCnt = 0;
		float overProfit = 0f, underProfit = 0f;
		for (FinalEntry i : finals) {
			if (i.prediction > i.upper) {
				overCnt++;
				overProfit += i.success() ? (i.fixture.getMaxClosingOverOdds() - 1f) : -1f;
			}
			if (i.prediction < i.lower) {
				underCnt++;
				underProfit += i.success() ? (i.fixture.getMaxClosingUnderOdds() - 1f) : -1f;
			}
		}

		System.out.println(overCnt + " overs with profit: " + overProfit);
		System.out.println(underCnt + " unders with profit: " + underProfit);
	}

	public static float countOverHalfTime(ArrayList<Fixture> fixtures, int i) {

		int count = 0;
		for (Fixture f : fixtures) {
			if (f.getHalfTimeGoals() >= i)
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static float countHalfTimeGoalAvgExact(ArrayList<Fixture> fixtures, int i) {

		int count = 0;
		for (Fixture f : fixtures) {
			if (f.getHalfTimeGoals() == i)
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	// public static ArrayList<Fixture>
	// filterByOdds(ArrayList<Fixture> data, float min, float max) {
	// ArrayList<Fixture> filtered = new ArrayList<>();
	// for (Fixture i : data) {
	// if (i.getMaxClosingOverOdds() <= max && i.getMaxClosingOverOdds() >= min)
	// filtered.add(i);
	// }
	// return filtered;
	// }

	public static float countOversWhenDraw(ArrayList<Fixture> all) {
		int count = 0;
		for (Fixture i : all) {
			if (i.result.goalsHomeTeam == i.result.goalsAwayTeam && i.getTotalGoals() > 2.5f)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static float countOversWhenNotDraw(ArrayList<Fixture> all) {
		int count = 0;
		for (Fixture i : all) {
			if (i.result.goalsHomeTeam != i.result.goalsAwayTeam && i.getTotalGoals() > 2.5f)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static float countDraws(ArrayList<Fixture> all) {
		int count = 0;
		for (Fixture i : all) {
			if (i.result.goalsHomeTeam == i.result.goalsAwayTeam)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static void byWeekDay(ArrayList<Fixture> all) {
		int[] days = new int[8];
		int[] overs = new int[8];
		String[] literals = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

		for (Fixture i : all) {
			Calendar c = Calendar.getInstance();
			c.setTime(i.date);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			days[dayOfWeek]++;
			if (i.getTotalGoals() > 2.5f)
				overs[dayOfWeek]++;
		}

		float[] raitios = new float[8];
		for (int i = 1; i < 8; i++) {
			raitios[i] = ((float) overs[i]) / days[i];
			System.out.println(literals[i - 1] + " " + raitios[i] + " from " + days[i]);
		}

	}

	public static ArrayList<FinalEntry> intersect(ArrayList<FinalEntry> finalsBasic,
			ArrayList<FinalEntry> finalsPoisson) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (int i = 0; i < finalsBasic.size(); i++) {
			if (samePrediction(finalsBasic.get(i), finalsPoisson.get(i)))
				result.add(finalsBasic.get(i));
		}
		return result;
	}

	@SafeVarargs
	public static ArrayList<FinalEntry> intersectMany(ArrayList<FinalEntry>... lists) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (int i = 0; i < lists[0].size(); i++) {
			if (samePrediction(lists, i))
				result.add(lists[0].get(i));
		}
		return result;
	}

	@SafeVarargs
	public static ArrayList<FinalEntry> intersectVotes(ArrayList<FinalEntry>... lists) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (int i = 0; i < lists[0].size(); i++) {
			int overs = 0;
			int unders = 0;
			for (ArrayList<FinalEntry> list : lists) {
				if (list.get(i).prediction >= list.get(i).upper)
					overs++;
				else
					unders++;
			}

			FinalEntry curr = lists[0].get(i);
			curr.prediction = overs > unders ? 1f : 0f;
			result.add(curr);
		}
		return result;
	}

	private static boolean samePrediction(ArrayList<FinalEntry>[] lists, int index) {

		boolean flag = true;
		for (ArrayList<FinalEntry> i : lists) {
			for (ArrayList<FinalEntry> j : lists) {
				if (!samePrediction(i.get(index), j.get(index)))
					flag = false;
			}
		}
		return flag;
	}

	public static boolean samePrediction(FinalEntry f1, FinalEntry f2) {
		return (f1.prediction >= f1.upper && f2.prediction >= f2.upper)
				|| (f1.prediction <= f1.lower && f2.prediction <= f2.lower);
	}

	public static ArrayList<FinalEntry> intersectDiff(ArrayList<FinalEntry> finals1, ArrayList<FinalEntry> finals2) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry fe : finals1) {
			FinalEntry other = Utils.getFE(finals2, fe);
			if (other != null) {
				if (samePrediction(fe, other))
					result.add(fe);
			}
		}
		return result;
	}

	/**
	 * Combines two list of finals whit ratio weighted predictions
	 * 
	 * @param finals1
	 * @param finals2
	 * @param ratio
	 * @return
	 */
	public static ArrayList<FinalEntry> combineDiff(ArrayList<FinalEntry> finals1, ArrayList<FinalEntry> finals2,
			float ratio) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry fe : finals1) {
			FinalEntry other = Utils.getFE(finals2, fe);
			if (other != null) {
				FinalEntry combined = new FinalEntry(fe);
				fe.prediction = ratio * fe.prediction + (1f - ratio) * other.prediction;
				result.add(new FinalEntry(combined));
			}
		}
		return result;
	}

	private static FinalEntry getFE(ArrayList<FinalEntry> finals2, FinalEntry fe) {
		for (FinalEntry i : finals2) {
			if (i.fixture.equals(fe.fixture))
				return i;
		}
		return null;
	}

	public static float bestNperWeek(ArrayList<FinalEntry> all, int n) {
		String[] literals = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : all) {
			Calendar c = Calendar.getInstance();
			c.setTime(fe.fixture.date);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			if (literals[dayOfWeek - 1].equals("SAT") || literals[dayOfWeek - 1].equals("SUN")) {
				filtered.add(fe);
			}

		}
		filtered.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {
				return o1.fixture.date.compareTo(o2.fixture.date);
			}

		});

		float profit = 0f;
		int winBets = 0;
		int loseBets = 0;
		ArrayList<FinalEntry> curr = new ArrayList<>();
		Date currDate = filtered.get(0).fixture.date;
		for (int i = 0; i < filtered.size(); i++) {
			Date date = filtered.get(i).fixture.date;
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			Date next = cal.getTime();
			if (date.equals(currDate) || date.equals(next)) {
				curr.add(filtered.get(i));
			} else {
				if (i + 1 < filtered.size()) {
					currDate = filtered.get(i + 1).fixture.date;

					curr.sort(new Comparator<FinalEntry>() {

						@Override
						public int compare(FinalEntry o1, FinalEntry o2) {
							Float certainty1 = o1.getCOT();
							Float certainty2 = o2.getCOT();
							return certainty2.compareTo(certainty1);
						}
					});

					boolean flag = true;
					float coeff = 1f;
					int successes = 0;
					int notlosses = 0;
					if (curr.size() >= n) {
						// System.out.println(curr);
						for (int j = 0; j < n; j++) {
							if (curr.get(j).success()) {
								coeff *= curr.get(j).prediction >= curr.get(j).upper
										? curr.get(j).fixture.getMaxClosingOverOdds()
										: curr.get(j).fixture.getMaxClosingUnderOdds();
								successes++;
								notlosses++;
							} else if ((curr.get(j).prediction >= curr.get(j).upper
									&& curr.get(j).fixture.getTotalGoals() == 2)
									|| (curr.get(j).prediction <= curr.get(j).lower
											&& curr.get(j).fixture.getTotalGoals() == 3)) {
								notlosses++;
								coeff = -1f;
								break;
							} else {
								coeff = -1f;
								break;
							}
						}
						// System.out.println(curr.get(0).fixture.date + " " + "
						// " + successes + " not loss: " + notlosses
						// + " pr: " + (coeff != -1f ? (coeff - 1f) : coeff));
						if (coeff != -1f)
							winBets++;
						else
							loseBets++;

						profit += (coeff != -1f ? (coeff - 1f) : coeff);
					}
					curr = new ArrayList<>();
				} else {
					break;
				}
			}
		}
		System.out.println("Total from " + n + "s: " + profit + " " + winBets + "W " + loseBets + "L");

		return profit;

	}

	public static void fullAnalysys(ArrayList<FinalEntry> all, String description) {
		analysys(all, description, true);

		// Settings initial = new Settings("", 0f, 0f, 0f, 0.55f, 0.55f, 0.55f,
		// 0.5f, 0f).withShots(1f);
		//
		// initial = XlSUtils.findValueByEvaluation(all, initial);
		// System.out.println("=======================================================================");
		// System.out.println("Optimal value is " + initial.value);
		// ArrayList<FinalEntry> values = XlSUtils.restrict(all, initial);
		// analysys(values, year);
		//
		// initial = XlSUtils.findThreshold(all, initial, MaximizingBy.UNDERS);
		// ArrayList<FinalEntry> withTH = XlSUtils.restrict(all, initial);
		// System.out.println("=======================================================================");
		// System.out.println("Optimal th is " + initial.threshold);
		// analysys(onlyUnders(withTH), year, true);
		// LineChart.draw(Utils.createProfitMovementData(onlyUnders(withTH)),
		// 3000);
		//
		// initial = XlSUtils.findValueByEvaluation(withTH, initial);
		// System.out.println("=======================================================================");
		// System.out.println("Optimal value is " + initial.value + " for found
		// optimal threshold=" + initial.threshold);
		// ArrayList<FinalEntry> values2 = XlSUtils.restrict(withTH, initial);
		// analysys(values2, year);

	}

	public static void analysys(ArrayList<FinalEntry> all, String description, boolean verbose) {
		ArrayList<Stats> stats = new ArrayList<>();

		Utils.removeMarginProportional(all);
		ArrayList<FinalEntry> noEquilibriums = Utils.noequilibriums(all);
		ArrayList<FinalEntry> equilibriums = Utils.equilibriums(all);

		Stats equilibriumsAsUnders = new Stats(allUnders(onlyFixtures(equilibriums)), "Equilibriums as unders");
		Stats equilibriumsAsOvers = new Stats(allOvers(onlyFixtures(equilibriums)), "Equilibriums as overs");
		stats.add(equilibriumsAsOvers);
		stats.add(equilibriumsAsUnders);
		// if (verbose) {
		System.out.println(equilibriumsAsUnders);
		System.out.println(equilibriumsAsOvers);
		// }

		if (verbose)
			System.out.println("Avg return: " + avgReturn(onlyFixtures(noEquilibriums)));
		Stats allStats = new Stats(noEquilibriums, "all");
		stats.add(allStats);
		if (verbose)
			System.out.println(allStats);

		if (verbose)
			System.out.println(thresholdsByLeague(all));
		if (verbose)
			LineChart.draw(Utils.createProfitMovementData(Utils.noequilibriums(all)), description);

		// Settings initial = new Settings("", 0f, 0f, 0f, 0.55f, 0.55f, 0.55f,
		// 0.5f, 0f).withShots(1f);

		// initial = XlSUtils.findThreshold(all, initial);
		// System.out.println("Optimal th is " + initial.threshold);
		// printStats(all, "all");

		// initial = XlSUtils.findValueByEvaluation(all, initial);
		// System.out.println("Optimal value is " + initial.value);
		// all = XlSUtils.restrict(all, initial);
		// printStats(all, "all");

		ArrayList<FinalEntry> overs = Utils.onlyOvers(noEquilibriums);
		ArrayList<FinalEntry> unders = Utils.onlyUnders(noEquilibriums);

		if (verbose) {
			System.err.println(description);
			System.out.println();
		}

		if (verbose)
			System.out.println();
		ArrayList<Stats> byCertaintyandCOT = byCertaintyandCOT(noEquilibriums, "", verbose);
		stats.addAll(byCertaintyandCOT);

		if (verbose)
			System.out.println();
		ArrayList<Stats> byOdds = byOdds(noEquilibriums, "", verbose);
		stats.addAll(byOdds);

		if (verbose)
			System.out.println();
		ArrayList<Stats> byValue = byValue(noEquilibriums, "", verbose);
		stats.addAll(byValue);

		Stats underStats = new Stats(unders, "unders");
		if (verbose)
			System.out.println(underStats);
		stats.add(underStats);

		if (verbose)
			System.out.println();
		ArrayList<Stats> byCertaintyandCOTUnders = byCertaintyandCOT(unders, "unders", verbose);
		stats.addAll(byCertaintyandCOTUnders);

		if (verbose)
			System.out.println();
		ArrayList<Stats> byOddsUnders = byOdds(unders, "unders", verbose);
		stats.addAll(byOddsUnders);

		Stats overStats = new Stats(overs, "overs");
		if (verbose)
			System.out.println(overStats);
		stats.add(overStats);

		System.out.println();
		ArrayList<Stats> byCertaintyandCOTover = byCertaintyandCOT(overs, "overs", verbose);
		stats.addAll(byCertaintyandCOTover);

		System.out.println();
		ArrayList<Stats> byOddsOvers = byOdds(overs, "overs", verbose);
		stats.addAll(byOddsOvers);

		System.out.println();
		Utils.byYear(onlyUnders(noEquilibriums), "all");

		System.out.println();
		Utils.byCompetition(onlyUnders(noEquilibriums), "all");

		Stats allOvers = new Stats(allOvers(Utils.onlyFixtures(noEquilibriums)), "all Overs");
		stats.add(allOvers);
		if (verbose) {
			System.out.println();
			System.out.println(allOvers);
		}

		Stats allUnders = new Stats(allUnders(Utils.onlyFixtures(noEquilibriums)), "all Unders");
		if (verbose)
			System.out.println(allUnders);
		stats.add(allUnders);

		Stats higherOdds = new Stats(higherOdds(Utils.onlyFixtures(noEquilibriums)), "higher Odds");
		Stats lowerOdds = new Stats(lowerOdds(Utils.onlyFixtures(noEquilibriums)), "lower Odds");
		stats.add(higherOdds);
		stats.add(lowerOdds);
		if (verbose) {
			System.out.println();
			System.out.println(higherOdds);
			System.out.println(lowerOdds);
		}

		System.out.println();
		int wins = 0;
		float draws = 0f;
		int certs = 0;
		for (FinalEntry fe : noEquilibriums) {
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			if (certainty >= 0f) {
				certs++;
				if (fe.success()) {
					wins++;
				} else if ((fe.prediction >= fe.upper && fe.fixture.getTotalGoals() == 2)
						|| (fe.prediction <= fe.lower && fe.fixture.getTotalGoals() == 3)) {
					draws++;
				}
			}
		}

		if (verbose)
			System.out.println("Soft lines wins: " + format((float) wins / certs) + " draws: "
					+ format((float) draws / certs) + " not losses: " + format((float) (wins + draws) / certs));

		ArrayList<Stats> normalizedStats = new ArrayList<>();
		for (Stats st : stats)
			if (st.getPvalueOdds() > 4 && !st.all.isEmpty())
				normalizedStats.add(new NormalizedStats(st.all, "norm " + st.description));
		stats.addAll(normalizedStats);

		System.out.println();
		stats.sort(Comparator.comparing(Stats::getPvalueOdds).reversed());
		stats.stream().filter(v -> verbose ? true : (v.getPvalueOdds() > 4 && !v.all.isEmpty()))
				.forEach(System.out::println);
	}

	private static void removeMarginProportional(ArrayList<FinalEntry> all) {
		all.stream().forEach(fe -> fe.fixture.overUnderOdds.stream().forEach(OverUnderOdds::removeMarginProportional));
		
	}

	private static HashMap<String, Float> thresholdsByLeague(ArrayList<FinalEntry> all) {
		HashMap<String, Float> result = new HashMap<>();

		for (FinalEntry i : all)
			if (result.containsKey(i.fixture.competition))
				continue;
			else
				result.put(i.fixture.competition, i.threshold);
		return result;
	}

	private static ArrayList<Stats> byCertaintyandCOT(ArrayList<FinalEntry> all, String prefix, boolean verbose) {
		ArrayList<Stats> result = new ArrayList<>();
		ArrayList<FinalEntry> cot5 = new ArrayList<>();
		ArrayList<FinalEntry> cot10 = new ArrayList<>();
		ArrayList<FinalEntry> cot15 = new ArrayList<>();
		ArrayList<FinalEntry> cot20 = new ArrayList<>();
		ArrayList<FinalEntry> cot25 = new ArrayList<>();
		ArrayList<FinalEntry> cer80 = new ArrayList<>();
		ArrayList<FinalEntry> cer70 = new ArrayList<>();
		ArrayList<FinalEntry> cer60 = new ArrayList<>();
		ArrayList<FinalEntry> cer50 = new ArrayList<>();
		ArrayList<FinalEntry> cer40 = new ArrayList<>();
		for (FinalEntry fe : all) {
			float certainty = fe.getCertainty();
			if (certainty >= 0.8f)
				cer80.add(fe);
			else if (certainty >= 0.7f) {
				cer70.add(fe);
			} else if (certainty >= 0.6f) {
				cer60.add(fe);
			} else if (certainty >= 0.5f) {
				cer50.add(fe);
			} else {
				cer40.add(fe);
			}

			float cot = fe.prediction > fe.threshold ? (fe.prediction - fe.threshold) : (fe.threshold - fe.prediction);
			if (cot >= 0.25f) {
				cot25.add(fe);
			} else if (cot >= 0.2f) {
				cot20.add(fe);
			} else if (cot >= 0.15f) {
				cot15.add(fe);
			} else if (cot >= 0.10f) {
				cot10.add(fe);
			} else if (cot >= 0.05f) {
				cot5.add(fe);
			}

		}

		if (verbose) {
			System.out.println(new Stats(cer80, prefix + " " + "cer80"));
			System.out.println(new Stats(cer70, prefix + " " + "cer70"));
			System.out.println(new Stats(cer60, prefix + " " + "cer60"));
			System.out.println(new Stats(cer50, prefix + " " + "cer50"));
			System.out.println(new Stats(cer40, prefix + " " + "cer40"));
		}
		result.add(new Stats(cer80, prefix + " " + "cer80"));
		result.add(new Stats(cer70, prefix + " " + "cer70"));
		result.add(new Stats(cer60, prefix + " " + "cer60"));
		result.add(new Stats(cer50, prefix + " " + "cer50"));
		result.add(new Stats(cer40, prefix + " " + "cer40"));

		if (verbose) {
			System.out.println();
			System.out.println(new Stats(cot25, prefix + " " + "cot25"));
			System.out.println(new Stats(cot20, prefix + " " + "cot20"));
			System.out.println(new Stats(cot15, prefix + " " + "cot15"));
			System.out.println(new Stats(cot10, prefix + " " + "cot10"));
			System.out.println(new Stats(cot5, prefix + " " + "cot5"));
		}
		result.add(new Stats(cot25, prefix + " " + "cot25"));
		result.add(new Stats(cot20, prefix + " " + "cot20"));
		result.add(new Stats(cot15, prefix + " " + "cot15"));
		result.add(new Stats(cot10, prefix + " " + "cot10"));
		result.add(new Stats(cot5, prefix + " " + "cot5"));

		return result;

	}

	public static void printStats(ArrayList<FinalEntry> all, String name) {
		float profit = Utils.getProfit(all);
		System.out.println(all.size() + " " + name + " with rate: " + format(100 * Utils.getSuccessRate(all))
				+ " profit: " + format(profit) + " yield: " + String.format("%.2f%%", 100 * profit / all.size())
				+ ((profit >= 0f && !all.isEmpty()) ? (" 1 in " + format(evaluateRecord(all))) : ""));
	}

	private static ArrayList<Stats> byOdds(ArrayList<FinalEntry> all, String prefix, boolean verbose) {
		ArrayList<Stats> result = new ArrayList<>();
		ArrayList<FinalEntry> under14 = new ArrayList<>();
		ArrayList<FinalEntry> under18 = new ArrayList<>();
		ArrayList<FinalEntry> under22 = new ArrayList<>();
		ArrayList<FinalEntry> over22 = new ArrayList<>();

		for (FinalEntry i : all) {
			float odds = i.isOver() ? i.fixture.getMaxClosingOverOdds() : i.fixture.getMaxClosingUnderOdds();
			if (odds <= 1.4f) {
				under14.add(i);
			} else if (odds <= 1.8f) {
				under18.add(i);
			} else if (odds <= 2.2f) {
				under22.add(i);
			} else {
				over22.add(i);
			}
		}

		if (verbose) {
			System.out.println(new Stats(under14, prefix + " " + "1.00 - 1.40"));
			System.out.println(new Stats(under18, prefix + " " + "1.41 - 1.80"));
			System.out.println(new Stats(under22, prefix + " " + "1.81 - 2.20"));
			System.out.println(new Stats(over22, prefix + " " + " > 2.21"));
		}
		result.add(new Stats(under14, prefix + " " + "1.00 - 1.40"));
		result.add(new Stats(under18, prefix + " " + "1.41 - 1.80"));
		result.add(new Stats(under22, prefix + " " + "1.81 - 2.20"));
		result.add(new Stats(over22, prefix + " " + " > 2.21"));

		return result;

	}

	private static ArrayList<Stats> byValue(ArrayList<FinalEntry> all, String prefix, boolean verbose) {
		ArrayList<Stats> result = new ArrayList<>();
		ArrayList<FinalEntry> under09 = new ArrayList<>();
		ArrayList<FinalEntry> under1 = new ArrayList<>();
		ArrayList<FinalEntry> under110 = new ArrayList<>();
		ArrayList<FinalEntry> under120 = new ArrayList<>();
		ArrayList<FinalEntry> under130 = new ArrayList<>();
		ArrayList<FinalEntry> under140 = new ArrayList<>();
		ArrayList<FinalEntry> under150 = new ArrayList<>();
		ArrayList<FinalEntry> under160 = new ArrayList<>();
		ArrayList<FinalEntry> over160 = new ArrayList<>();

		for (FinalEntry i : all) {
			float value = i.getValue();
			if (value <= 0.9f) {
				under09.add(i);
			} else if (value <= 1f) {
				under1.add(i);
			} else if (value <= 1.10f) {
				under110.add(i);
			} else if (value <= 1.20f) {
				under120.add(i);
			} else if (value <= 1.30f) {
				under130.add(i);
			} else if (value <= 1.40f) {
				under140.add(i);
			} else if (value <= 1.50f) {
				under150.add(i);
			} else if (value <= 1.60f) {
				under160.add(i);
			} else {
				over160.add(i);
			}
		}

		if (verbose) {
			System.out.println(new Stats(under09, prefix + " " + "< 0.9"));
			System.out.println(new Stats(under1, prefix + " " + "0.9 - 1.0"));
			System.out.println(new Stats(under110, prefix + " " + "1.00 - 1.10"));
			System.out.println(new Stats(under120, prefix + " " + "1.10 - 1.2"));
			System.out.println(new Stats(under130, prefix + " " + "1.2 - 1.3"));
			System.out.println(new Stats(under140, prefix + " " + "1.3 - 1.4"));
			System.out.println(new Stats(under150, prefix + " " + "1.4 - 1.5"));
			System.out.println(new Stats(under160, prefix + " " + "1.5 - 1.6"));
			System.out.println(new Stats(over160, prefix + " " + " > 1.6"));
		}

		result.add(new Stats(under09, prefix + " " + "< 0.9"));
		result.add(new Stats(under1, prefix + " " + "0.9 - 1.0"));
		result.add(new Stats(under110, prefix + " " + "1.0 - 1.10"));
		result.add(new Stats(under120, prefix + " " + "1.1 - 1.2"));
		result.add(new Stats(under130, prefix + " " + "1.2 - 1.3"));
		result.add(new Stats(under140, prefix + " " + "1.3 - 1.4"));
		result.add(new Stats(under150, prefix + " " + "1.4 - 1.5"));
		result.add(new Stats(under160, prefix + " " + "1.5 - 1.6"));
		result.add(new Stats(over160, prefix + " " + "> 1.6"));

		return result;

	}

	public static String format(float d) {
		return String.format("%.2f", d);
	}

	public static void triples(ArrayList<FinalEntry> all, int year) {
		int failtimes = 0;
		int losses = 0;
		int testCount = 1_000_000;
		double total = 0D;

		for (int trials = 0; trials < testCount; trials++) {
			Collections.shuffle(all);

			float bankroll = 1000f;
			float unit = 6f;
			int yes = 0;
			boolean flag = false;
			for (int i = 0; i < all.size() - all.size() % 3; i += 3) {
				if (bankroll < 0) {
					flag = true;
					break;
				}
				if (all.get(i).success() && all.get(i + 1).success() && all.get(i + 2).success()) {
					float c1 = all.get(i).prediction > all.get(i).upper ? all.get(i).fixture.getMaxClosingOverOdds()
							: all.get(i).fixture.getMaxClosingUnderOdds();
					float c2 = all.get(i + 1).prediction > all.get(i + 1).upper
							? all.get(i + 1).fixture.getMaxClosingOverOdds()
							: all.get(i + 1).fixture.getMaxClosingUnderOdds();
					float c3 = all.get(i + 2).prediction > all.get(i + 2).upper
							? all.get(i + 2).fixture.getMaxClosingOverOdds()
							: all.get(i + 2).fixture.getMaxClosingUnderOdds();
					bankroll += unit * (c1 * c2 * c3 - 1f);
					yes++;
				} else {
					bankroll -= unit;
				}
			}

			// System.out.println(
			// flag ? "bankrupt" : "bankroll: " + bankroll + " successrate: " +
			// (float) yes / (all.size() / 3));
			if (flag) {
				failtimes++;
			} else {
				total += bankroll;
				if (bankroll < 1000f)
					losses++;
			}
		}

		System.out.println(year + " Out of " + testCount + " fails: " + failtimes + " losses " + losses + " successes: "
				+ (testCount - failtimes - losses) + " with AVG: " + total / (testCount - failtimes));
	}

	public static void hyperReal(ArrayList<FinalEntry> all, int year, float bankroll, float percent) {
		System.err.println(year);
		float bank = bankroll;
		float previous = bank;
		int succ = 0;
		int alls = 0;
		all.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {
				return o1.fixture.date.compareTo(o2.fixture.date);
			}

		});

		float betSize = percent * bankroll;
		Calendar cal = Calendar.getInstance();
		cal.setTime(all.get(0).fixture.date);
		int month = cal.get(Calendar.MONTH);
		for (FinalEntry i : all) {
			cal.setTime(i.fixture.date);
			if (cal.get(Calendar.MONTH) == month) {
				float gain = i.prediction >= i.upper ? i.fixture.getMaxClosingOverOdds()
						: i.fixture.getMaxClosingUnderOdds();
				bank += betSize * (i.success() ? (gain - 1f) : -1f);
				succ += i.success() ? 1 : 0;
				alls++;
			} else {
				System.out.println("Bank after month: " + (month + 1) + " is: " + bank + " unit: " + betSize
						+ " profit: " + (bank - previous) + " in units: " + (bank - previous) / betSize + " rate: "
						+ (float) succ / alls + "%");
				previous = bank;
				betSize = bank * percent;
				month = cal.get(Calendar.MONTH);
				float gain = i.prediction >= i.upper ? i.fixture.getMaxClosingOverOdds()
						: i.fixture.getMaxClosingUnderOdds();
				bank += betSize * (i.success() ? (gain - 1f) : -1f);
				alls = 1;
				succ = i.success() ? 1 : 0;
			}
		}
		System.out.println("Bank after month: " + (month + 1) + " is: " + bank + " unit: " + betSize + " profit: "
				+ (bank - previous) + " in units: " + (bank - previous) / betSize + " rate: " + (float) succ / alls
				+ "%");
	}

	public static float correlation(Integer[] arr1, Integer[] arr2) {
		if (arr1.length != arr2.length)
			return -1;
		float avg1 = 0f;
		float avg2 = 0f;
		for (int i = 0; i < arr1.length; i++) {
			avg1 += arr1[i];
			avg2 += arr2[i];
		}

		avg1 /= arr1.length;
		avg2 /= arr2.length;

		float sumXY = 0f;
		float sumX2 = 0f;
		float sumY2 = 0f;

		for (int i = 0; i < arr1.length; i++) {
			sumXY += (arr1[i] - avg1) * (arr2[i] - avg2);
			sumX2 += Math.pow(arr1[i] - avg1, 2.0f);
			sumY2 += Math.pow(arr2[i] - avg2, 2.0f);
		}
		return (float) (sumXY / (Math.sqrt(sumX2) * Math.sqrt(sumY2)));
	}

	public static boolean oddsInRange(float gain, float finalScore, Settings settings) {
		boolean over = finalScore > settings.threshold;

		if (over)
			return (gain >= settings.minOver && gain <= settings.maxOver);
		else
			return (gain >= settings.minUnder && gain <= settings.maxUnder);
	}

	public static ArrayList<FinalEntry> onlyUnders(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.prediction < i.threshold)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<FinalEntry> onlyOvers(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.prediction >= i.threshold)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<FinalEntry> ratioRestrict(ArrayList<FinalEntry> finals, Map<String, Integer> played,
			Map<String, Integer> success) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (!played.containsKey(i.fixture.homeTeam) || !played.containsKey(i.fixture.awayTeam)
					|| played.get(i.fixture.homeTeam) + played.get(i.fixture.awayTeam) < 8)
				result.add(i);
			else {
				float homeRate = success.get(i.fixture.homeTeam) == 0 ? 0f
						: ((float) success.get(i.fixture.homeTeam) / played.get(i.fixture.homeTeam));
				float awayRate = success.get(i.fixture.awayTeam) == 0 ? 0f
						: ((float) success.get(i.fixture.awayTeam) / played.get(i.fixture.awayTeam));
				float avgRate = (homeRate + awayRate) / 2;
				if (avgRate >= 0.4)
					result.add(i);
			}

		}
		return result;
	}

	public static ArrayList<FinalEntry> certaintyRestrict(ArrayList<FinalEntry> finals, float cert) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			float certainty = i.prediction > i.threshold ? i.prediction : (1f - i.prediction);
			if (certainty >= cert)
				result.add(i);
		}

		return result;
	}

	public static ArrayList<FinalEntry> cotRestrict(ArrayList<FinalEntry> finals, float f) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float cot = fe.prediction > fe.threshold ? (fe.prediction - fe.threshold) : (fe.threshold - fe.prediction);
			if (cot >= f)
				result.add(fe);
		}

		return result;
	}

	public static ArrayList<FinalEntry> cotRestrictOU(ArrayList<FinalEntry> finals, Pair pair) {
		ArrayList<FinalEntry> result = new ArrayList<>();

		for (FinalEntry fe : Utils.onlyOvers(finals)) {
			float cot = fe.prediction > fe.threshold ? (fe.prediction - fe.threshold) : (fe.threshold - fe.prediction);
			if (cot >= pair.home)
				result.add(fe);
		}

		for (FinalEntry fe : Utils.onlyUnders(finals)) {
			float cot = fe.prediction > fe.threshold ? (fe.prediction - fe.threshold) : (fe.threshold - fe.prediction);
			if (cot >= pair.away)
				result.add(fe);
		}

		return result;
	}

	public static void drawAnalysis(ArrayList<FinalEntry> all) {
		int drawUnder = 0;
		int drawOver = 0;
		int under = 0;
		int over = 0;

		float profitOver = 0f;
		float profitUnder = 0f;

		for (FinalEntry i : all) {
			if (i.prediction <= i.lower) {
				under++;
				if (i.fixture.result.goalsHomeTeam == i.fixture.result.goalsAwayTeam) {
					drawUnder++;
					profitUnder += i.fixture.getMaxClosingDrawOdds();
				}

			} else if (i.prediction >= i.upper) {
				over++;
				if (i.fixture.result.goalsHomeTeam == i.fixture.result.goalsAwayTeam) {
					drawOver++;
					profitOver += i.fixture.getMaxClosingDrawOdds();
				}
			}
		}

		System.out.println("Draws when under pr: " + (profitUnder - under) + " from " + under + " "
				+ Results.format((float) (profitUnder - under) * 100 / under) + "%");
		System.out.println("Draws when over pr: " + (profitOver - over) + " from " + over + " "
				+ Results.format((float) (profitOver - over) * 100 / over) + "%");
	}

	public static Table createTable(ArrayList<Fixture> data, String sheetName, int year, int i) {
		HashMap<String, Position> teams = getTeams(data);

		Table table = new Table(sheetName, year, i);

		for (String team : teams.keySet()) {
			Fixture f = new Fixture(null, team, team, null, null);
			ArrayList<Fixture> all = Utils.getHomeFixtures(f, data);
			all.addAll(Utils.getAwayFixtures(f, data));
			Position pos = createPosition(team, all);
			table.positions.add(pos);
		}

		table.sort();
		return table;
	}

	private static Position createPosition(String team, ArrayList<Fixture> all) {
		Position pos = new Position();
		pos.team = team;

		for (Fixture i : all) {
			if (i.homeTeam.equals(team)) {
				pos.played++;
				pos.homeplayed++;

				if (i.isHomeWin()) {
					pos.wins++;
					pos.homewins++;
					pos.points += 3;
					pos.homepoints += 3;
				} else if (i.isAwayWin()) {
					pos.losses++;
					pos.homelosses++;
				} else {
					pos.draws++;
					pos.homedraws++;
					pos.points++;
					pos.homepoints++;
				}

				pos.scored += i.result.goalsHomeTeam;
				pos.conceded += i.result.goalsAwayTeam;

				pos.homescored += i.result.goalsHomeTeam;
				pos.homeconceded += i.result.goalsAwayTeam;
			} else {
				pos.played++;
				pos.awayplayed++;

				if (i.isHomeWin()) {
					pos.losses++;
					pos.awaylosses++;
				} else if (i.isAwayWin()) {
					pos.wins++;
					pos.awaywins++;
					pos.points += 3;
					pos.awaypoints += 3;
				} else {
					pos.draws++;
					pos.awaydraws++;
					pos.points++;
					pos.awaypoints++;
				}

				pos.scored += i.result.goalsAwayTeam;
				pos.conceded += i.result.goalsHomeTeam;

				pos.awayscored += i.result.goalsAwayTeam;
				pos.awayconceded += i.result.goalsHomeTeam;
			}
		}

		pos.diff = pos.scored - pos.conceded;
		pos.homediff = pos.homescored - pos.homeconceded;
		pos.awaydiff = pos.awayscored - pos.awayconceded;
		pos.team = team;
		return pos;
	}

	private static HashMap<String, Position> getTeams(ArrayList<Fixture> data) {
		HashMap<String, Position> teams = new HashMap<>();

		for (Fixture i : data) {
			if (!teams.containsKey(i.homeTeam)) {
				Position pos = new Position();
				pos.team = i.homeTeam;
				teams.put(i.homeTeam, pos);
			}
			if (!teams.containsKey(i.awayTeam)) {
				Position pos = new Position();
				pos.team = i.awayTeam;
				teams.put(i.awayTeam, pos);
			}
		}

		return teams;
	}

	public static ArrayList<FinalEntry> shotsRestrict(ArrayList<FinalEntry> finals, HSSFSheet sheet)
			throws ParseException {
		ArrayList<FinalEntry> shotBased = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float shotsScore = XlSUtils.shots(fe.fixture, sheet);
			if (fe.prediction >= fe.upper && shotsScore == 1f) {
				shotBased.add(fe);
			} else if (fe.prediction <= fe.lower && shotsScore == 0f) {
				shotBased.add(fe);
			}
		}
		return shotBased;
	}

	public static Pair positionLimits(ArrayList<FinalEntry> finals, Table table, String type) {

		float bestProfit = getProfit(finals);
		int bestLow = 0;
		int bestHigh = 23;

		for (int i = 1; i < 11; i++) {
			ArrayList<FinalEntry> diffPos = positionRestrict(finals, table, i, 23, type);

			float curr = Utils.getProfit(diffPos);
			if (curr > bestProfit) {
				bestProfit = curr;
				bestLow = i;
			}

		}

		for (int i = bestLow; i < 23; i++) {
			ArrayList<FinalEntry> diffPos = positionRestrict(finals, table, bestLow, i, type);

			float curr = Utils.getProfit(diffPos);
			if (curr > bestProfit) {
				bestProfit = curr;
				bestHigh = i;
			}
		}

		return Pair.of(bestLow, bestHigh);
	}

	public static ArrayList<FinalEntry> positionRestrict(ArrayList<FinalEntry> finals, Table table, int i, int j,
			String type) {
		ArrayList<FinalEntry> diffPos = new ArrayList<>();
		for (FinalEntry fe : finals) {
			int diff = Math.abs(table.getPositionDiff(fe.fixture));

			if (diff <= i && fe.prediction <= fe.lower)
				diffPos.add(fe);
			else if (diff >= j && fe.prediction >= fe.upper)
				diffPos.add(fe);
			else if (diff > i && diff < j)
				diffPos.add(fe);
		}
		return diffPos;

	}

	public static ArrayList<FinalEntry> similarityRestrict(HSSFSheet sheet, ArrayList<FinalEntry> finals, Table table)
			throws ParseException {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			float basicSimilar = Utils.basicSimilar(i.fixture, sheet, table);
			if (i.prediction >= i.upper && basicSimilar >= i.threshold)
				result.add(i);
			else if (i.prediction <= i.lower && basicSimilar <= i.threshold)
				result.add(i);
			// else
			// System.out.println(i + " " + i.prediction + " " + basicSimilar);
		}

		return result;
	}

	public static float basicSimilar(Fixture f, HSSFSheet sheet, Table table) throws ParseException {
		ArrayList<String> filterHome = table.getSimilarTeams(f.awayTeam);
		ArrayList<String> filterAway = table.getSimilarTeams(f.homeTeam);

		ArrayList<Fixture> lastHomeTeam = filter(f.homeTeam, XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date),
				filterHome);
		ArrayList<Fixture> lastAwayTeam = filter(f.awayTeam, XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date),
				filterAway);

		ArrayList<Fixture> lastHomeHomeTeam = filter(f.homeTeam, XlSUtils.selectLastHome(sheet, f.homeTeam, 25, f.date),
				filterHome);
		ArrayList<Fixture> lastAwayAwayTeam = filter(f.awayTeam, XlSUtils.selectLastAway(sheet, f.awayTeam, 25, f.date),
				filterAway);

		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return 0.6f * allGamesAVG + 0.3f * homeAwayAVG + 0.1f * BTSAVG;
	}

	public static float similarPoisson(Fixture f, HSSFSheet sheet, Table table) throws ParseException {
		ArrayList<String> filterHome = table.getSimilarTeams(f.awayTeam);
		ArrayList<String> filterAway = table.getSimilarTeams(f.homeTeam);

		ArrayList<Fixture> lastHomeTeam = filter(f.homeTeam, XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date),
				filterHome);
		ArrayList<Fixture> lastAwayTeam = filter(f.awayTeam, XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date),
				filterAway);

		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	private static ArrayList<Fixture> filter(String team, ArrayList<Fixture> selectLastAll,
			ArrayList<String> filterHome) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : selectLastAll) {
			if (i.homeTeam.equals(team) && filterHome.contains(i.awayTeam))
				result.add(i);
			if (i.awayTeam.equals(team) && filterHome.contains(i.homeTeam))
				result.add(i);

		}
		return result;
	}

	public static float bestCot(ArrayList<FinalEntry> finals) {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		float bestCot = 0f;
		float bestProfit = getProfit(finals);

		for (int j = 1; j <= 12; j++) {
			ArrayList<FinalEntry> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			filtered.addAll(Utils.cotRestrict(finals, cot));

			float currProfit = 0f;
			currProfit += Utils.getProfit(filtered);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestCot = cot;
			}

		}

		return 5 * bestCot / 6;
	}

	public static ArrayList<FinalEntry> allOvers(ArrayList<Fixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (Fixture i : current) {
			FinalEntry n = new FinalEntry(i, 1f, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static ArrayList<FinalEntry> allUnders(ArrayList<Fixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (Fixture i : current) {
			FinalEntry n = new FinalEntry(i, 0f, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static ArrayList<FinalEntry> higherOdds(ArrayList<Fixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (Fixture i : current) {
			float prediction = i.getMaxClosingOverOdds() >= i.getMaxClosingUnderOdds() ? 1f : 0f;
			FinalEntry n = new FinalEntry(i, prediction, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static ArrayList<FinalEntry> lowerOdds(ArrayList<Fixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (Fixture i : current) {
			float prediction = i.getMaxClosingOverOdds() >= i.getMaxClosingUnderOdds() ? 0f : 1f;
			FinalEntry n = new FinalEntry(i, prediction, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static float avgShotsDiffHomeWin(HSSFSheet sheet, Date date) {
		int totalHome = 0;
		int totalAway = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(XlSUtils.getColumnIndex(sheet, "Date"));
			int homegoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awaygoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTAG")).getNumericCellValue();
			if (row.getCell(XlSUtils.getColumnIndex(sheet, "HST")) != null
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")) != null && dateCell != null
					&& dateCell.getDateCellValue().before(date)
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getCellType() == 0 && homegoal > awaygoal) {
				totalHome += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getNumericCellValue();
				totalAway += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getNumericCellValue();

				count++;
			}
		}
		return count == 0 ? 0 : (float) (totalHome - totalAway) / count;
	}

	public static String replaceNonAsciiWhitespace(String s) {

		String resultString = s.replaceAll("[^\\p{ASCII}]", "");

		return resultString;
	}

	public static Date getYesterday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date oneDayBefore = cal.getTime();
		return oneDayBefore;

	}

	public static Object getTommorow(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date oneDayBefore = cal.getTime();
		return oneDayBefore;
	}

	public static boolean isToday(Date date) {
		Date today = new Date();
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date);
		cal2.setTime(today);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}

	public static ArrayList<FinalEntry> equilibriums(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.prediction == 0.5f)
				result.add(i);
		}

		return result;
	}

	public static ArrayList<FinalEntry> noequilibriums(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.prediction != 0.5f)
				result.add(i);
		}

		return result;
	}

	public static float getProfit(ArrayList<FinalEntry> finals) {
		float profit = 0f;
		for (FinalEntry i : finals) {
			profit += i.getProfit();
		}
		return profit;
	}

	public static float getNormalizedProfit(ArrayList<FinalEntry> all) {
		float sum = 0f;
		for (FinalEntry i : all)
			sum += i.getNormalizedProfit();

		return sum / (getNormalizedStakeSum(all) / all.size());
	}

	public static float getAvgOdds(ArrayList<FinalEntry> finals) {
		float total = 0f;
		for (FinalEntry i : finals) {
			float coeff = i.prediction >= i.upper ? i.fixture.getMaxClosingOverOdds()
					: i.fixture.getMaxClosingUnderOdds();
			total += coeff;
		}
		return finals.size() == 0 ? 0 : total / finals.size();
	}

	public static float pValueCalculator(int count, float yield, float avgOdds) {
		if (count < 5)
			return -1f;
		double standardDeviation = Math.pow((1 + yield) * (avgOdds - 1 - yield), 0.5);
		double tStatistic = yield * Math.pow(count, 0.5) / standardDeviation;
		TDistribution td = new TDistribution(count - 1);
		double pValue = 1 - td.cumulativeProbability(tStatistic);
		return (float) (1 / pValue);
	}

	public static float evaluateRecord(ArrayList<FinalEntry> all) {
		return pValueCalculator(all.size(), Utils.getYield(all), Utils.getAvgOdds(all));
	}

	public static float evaluateRecordNormalized(ArrayList<FinalEntry> all) {
		return pValueCalculator(all.size(), Utils.getNormalizedYield(all), Utils.getAvgOdds(all));
	}

	public static float getYield(ArrayList<FinalEntry> all) {
		return getProfit(all) / all.size();
	}

	public static float getNormalizedYield(ArrayList<FinalEntry> all) {

		return getNormalizedProfit(all) / all.size();
	}

	private static float getNormalizedStakeSum(ArrayList<FinalEntry> all) {
		float stakeSum = 0f;
		for (FinalEntry i : all) {
			float coeff = i.prediction >= i.upper ? i.fixture.getMaxClosingOverOdds()
					: i.fixture.getMaxClosingUnderOdds();
			float betUnit = 1f / (coeff - 1);
			stakeSum += betUnit;
		}
		return stakeSum;
	}

	public static float[] createProfitMovementData(ArrayList<FinalEntry> all) {
		all.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {

				return o1.fixture.date.compareTo(o2.fixture.date);
			}
		});
		float profit = 0;
		float[] result = new float[all.size() + 1];
		result[0] = 0f;
		for (int i = 0; i < all.size(); i++) {
			profit += all.get(i).getProfit();
			result[i + 1] = profit;
		}
		return result;
	}

	/**
	 * 
	 * @param all
	 * @return the avg return of the picks a.k.a the avg vigorish
	 */
	public static float avgReturn(ArrayList<Fixture> all) {
		float total = 0f;
		for (Fixture i : all) {
			total += 1f / i.getMaxClosingOverOdds() + 1f / i.getMaxClosingUnderOdds();
		}
		return all.size() == 0 ? 0 : total / all.size();
	}

	public static ArrayList<FinalEntry> runRandom(ArrayList<Fixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		Random random = new Random();
		for (Fixture i : current) {
			boolean next = random.nextBoolean();
			float prediction = next ? 1f : 0f;
			FinalEntry n = new FinalEntry(i, prediction, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	// for update of results from soccerway
	public static Date findLastPendingFixture(ArrayList<Fixture> all) {

		all.sort(Comparator.comparing(Fixture::getDate));
		boolean noPendings = true;
		for (Fixture i : all)
			if (i.result.goalsHomeTeam == -1) {
				noPendings = false;
				break;
			}

		Date last;
		if (all.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Scraper.CURRENT_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, 1);
			last = cal.getTime();
		} else {
			last = all.get(all.size() - 1).date;
		}
		Date lastNotPending = last;

		for (int i = all.size() - 1; i >= 0; i--) {
			if (all.get(i).result.goalsHomeTeam == -1 && all.get(i).result.goalsAwayTeam == -1)
				lastNotPending = all.get(i).date;
		}

		return /* noPendings ? new Date() : */ lastNotPending;

	}

	// TODO
	// public static ArrayList<FinalEntry>
	// bestValueByDistibution(ArrayList<FinalEntry> finals,
	// HashMap<Fixture, Fixture> map, ArrayList<Fixture> all, HSSFSheet sheet) {
	// ArrayList<FinalEntry> fulls = new ArrayList<>();
	// for (FinalEntry f : finals) {
	//
	// int[] distributionHome = getGoalDistribution(f.fixture, all,
	// f.fixture.homeTeam);
	// int[] distributionAway = getGoalDistribution(f.fixture, all,
	// f.fixture.awayTeam);
	// FullEntry best = findBestLineFullEntry(f, distributionHome,
	// distributionAway, map);
	//
	// fulls.add(best);
	// }
	//
	// return fulls;
	// }

	// private static FullEntry findBestLineFullEntry(FinalEntry f, int[]
	// distributionHome, int[] distributionAway,
	// HashMap<Fixture, Fixture> map) {
	// FullEntry best = new FullEntry(f.fixture, f.prediction, f.result,
	// f.threshold, f.lower, f.upper,
	// map.get(f.fixture).goalLines.main);
	// Fixture full = map.get(f.fixture);
	// Result originalResult = new Result(full.result.goalsHomeTeam,
	// full.result.goalsAwayTeam);
	// float bestValue = Float.NEGATIVE_INFINITY;
	//
	// for (Line i : map.get(f.fixture).goalLines.getArrayLines()) {
	// float valueHome = 0;
	// int homeSize = 0;
	// for (int s : distributionHome)
	// homeSize += s;
	//
	// for (int goals = 0; goals < distributionHome.length; goals++) {
	// full.result = new Result(goals, 0);
	// valueHome += distributionHome[goals]
	// * new FullEntry(full, f.prediction, new Result(goals, 0), f.threshold,
	// f.lower, f.upper, i)
	// .getProfit();
	// }
	//
	// valueHome /= homeSize;
	//
	// float valueAway = 0;
	// int awaySize = 0;
	// for (int s : distributionAway)
	// awaySize += s;
	//
	// for (int goals = 0; goals < distributionAway.length; goals++) {
	// full.result = new Result(goals, 0);
	// valueAway += distributionAway[goals]
	// * new FullEntry(full, f.prediction, new Result(goals, 0), f.threshold,
	// f.lower, f.upper, i)
	// .getProfit();
	// }
	//
	// valueAway /= awaySize;
	//
	// float finalValue = (valueAway + valueHome) / 2;
	//
	// if (finalValue > bestValue) {
	// bestValue = finalValue;
	// best.line = i;
	// }
	// }
	//
	// best.result = originalResult;
	// best.fixture.result = originalResult;
	// return best;
	//
	// }

	private static int[] getGoalDistribution(Fixture fixture, ArrayList<Fixture> all, String team) {
		int[] distribution = new int[20];
		for (Fixture i : all) {
			if ((i.homeTeam.equals(team) || i.awayTeam.equals(team)) && i.date.before(fixture.date)) {
				int totalGoals = i.getTotalGoals();
				distribution[totalGoals]++;
			}
		}
		return distribution;

	}

	public static ArrayList<String> getTeamsList(ArrayList<Fixture> odds) {
		ArrayList<String> result = new ArrayList<>();
		for (Fixture i : odds) {
			if (!result.contains(i.homeTeam))
				result.add(i.homeTeam);
			if (!result.contains(i.awayTeam))
				result.add(i.awayTeam);
		}
		return result;
	}

	/**
	 * 
	 * @param team
	 * @param fixtures
	 * @return list of the fixtures for the given team
	 */
	public static ArrayList<Fixture> getFixturesList(String team, ArrayList<Fixture> fixtures) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : fixtures)
			if (i.homeTeam.equals(team) || i.awayTeam.equals(team))
				result.add(i);

		return result;
	}

	/**
	 * Method for verifying two list of fixtures are the same (only difference
	 * in naming the clubs)
	 * 
	 * @param teamFor
	 * 
	 * @param fixtures
	 * @param fwa
	 * @return
	 */
	public static boolean matchesFixtureLists(String teamFor, ArrayList<Fixture> fixtures, ArrayList<Fixture> fwa) {
		// System.out.println(fixtures);
		// System.out.println(fwa);
		for (Fixture i : fixtures) {
			boolean foundMatch = false;
			boolean isHomeSide = teamFor.equals(i.homeTeam);
			for (Fixture j : fwa) {
				if (Math.abs(i.date.getTime() - j.date.getTime()) <= 24 * 60 * 60 * 1000 && i.result.equals(j.result)) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				return false;

		}

		return true;
	}
	// TODO
	// public static ArrayList<FinalEntry> specificLine(ArrayList<FinalEntry>
	// finals, HashMap<Fixture, Fixture> map,
	// float line) {
	// ArrayList<FinalEntry> fulls = new ArrayList<>();
	//
	// for (FinalEntry f : finals) {
	// // boolean isOver = f.prediction > f.threshold;
	// FullEntry full = new FullEntry(f.fixture, f.prediction, f.result,
	// f.threshold, f.lower, f.upper, null);
	// for (Line l : map.get(f.fixture).goalLines.getArrayLines()) {
	// if (l.line == line) {
	// full.line = l;
	// fulls.add(full);
	// continue;
	// }
	// }
	//
	// }
	//
	// return fulls;
	// }

	/**
	 * Calculates the similarity (a number within 0 and 1) between two strings.
	 */
	public static double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) { // longer should always have greater
											// length
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0;
			/* both strings are zero length */ }
		/*
		 * // If you have StringUtils, you can use it to calculate the edit
		 * distance: return (longerLength -
		 * StringUtils.getLevenshteinDistance(longer, shorter)) / (double)
		 * longerLength;
		 */
		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

	}

	// Example implementation of the Levenshtein Edit Distance
	// See http://rosettacode.org/wiki/Levenshtein_distance#Java
	public static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	public static float outcomes(ArrayList<String> results, float coeff) {
		int wins = 0;
		int halfwins = 0;
		int draws = 0;
		int halflosses = 0;
		int losses = 0;
		for (String i : results) {
			if (i.equals("W"))
				wins++;
			else if (i.equals("HW")) {
				halfwins++;
			} else if (i.equals("D")) {
				draws++;
			} else if (i.equals("HL")) {
				halflosses++;
			} else {
				losses++;
			}

		}

		return ((float) wins / results.size()) * coeff + ((float) halfwins / results.size()) * (1 + (coeff - 1) / 2)
		/* + ((float) draws / results.size()) */ - ((float) halflosses / results.size()) / 2
				- ((float) losses / results.size());
	}

	public static LinearRegression getRegression(String homeTeam, ArrayList<Fixture> lastHome) {
		ArrayList<Double> homeGoals = new ArrayList<>();
		ArrayList<Double> homeShots = new ArrayList<>();
		for (Fixture i : lastHome) {
			if (i.homeTeam.equals(homeTeam)) {
				homeGoals.add((double) i.result.goalsHomeTeam);
				homeShots.add((double) i.gameStats.getShotsHome());
			} else {
				homeGoals.add((double) i.result.goalsAwayTeam);
				homeShots.add((double) i.gameStats.getShotsAway());
			}
		}

		double[] xhome = new double[homeGoals.size()];
		double[] yhome = new double[homeGoals.size()];

		for (int i = 0; i < homeGoals.size(); i++) {
			xhome[i] = homeShots.get(i);
			yhome[i] = homeGoals.get(i);
		}

		return new LinearRegression(xhome, yhome);
	}

	public static ArrayList<FinalEntry> removePending(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.fixture.getTotalGoals() >= 0)
				result.add(i);
		}
		return result;
	}

	public static float predictionCorrelation(ArrayList<FinalEntry> all) {
		Integer[] totalGoals = new Integer[all.size()];
		Integer[] predictions = new Integer[all.size()];
		for (int i = 0; i < all.size(); i++) {
			totalGoals[i] = all.get(i).fixture.getTotalGoals();
			predictions[i] = (int) (all.get(i).prediction * 1000);
		}

		System.out.println("Correlation is: " + Utils.correlation(totalGoals, predictions));
		return Utils.correlation(totalGoals, predictions);
	}

	/**
	 * zero based months
	 * 
	 * @param current
	 */

	public static ArrayList<Fixture> inMonth(ArrayList<Fixture> current, int i, int j) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture c : current) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(c.date);
			int month = cal.get(Calendar.MONTH);
			if (month >= i && month <= j)
				result.add(c);
		}
		return result;
	}

	public static ArrayList<FinalEntry> similarRanking(ArrayList<FinalEntry> finals, Table table) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry c : finals) {
			if ((table.getMiddleTeams().contains((c.fixture.homeTeam))
					&& table.getMiddleTeams().contains((c.fixture.awayTeam)))
			/*
			 * || (table.getBottomTeams().contains((c.fixture.homeTeam)) &&
			 * table.getTopTeams().contains((c.fixture.awayTeam)))
			 */)
				result.add(c);
		}
		return result;
	}

	public static ArrayList<FinalEntry> runWithPlayersData(ArrayList<Fixture> current, ArrayList<PlayerFixture> pfs,
			HashMap<String, String> dictionary, ArrayList<Fixture> all, float th) throws ParseException {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (Fixture i : current) {
			float eval = evaluatePlayers(i, pfs, dictionary, all);
			FinalEntry fe = new FinalEntry(i, eval/* >=0.55f ? 0f : 1f */, i.result, th, th, th);
			// fe.prediction = fe.isOver() ? 0f : 1f;
			if (fe.getValue() > 1.05f)
				result.add(fe);
		}
		return result;
	}

	public static float evaluatePlayers(Fixture ef, ArrayList<PlayerFixture> pfs, HashMap<String, String> dictionary,
			ArrayList<Fixture> all) throws ParseException {
		// The shots data from soccerway(opta) does not add the goals as shots,
		// must be added for more accurate predictions and equivalancy with
		// alleurodata
		boolean manual = Arrays.asList(Constants.MANUAL).contains(ef.competition);
		float goalsWeight = 1f;

		float homeEstimate = estimateGoalFromPlayerStats(ef, pfs, dictionary, true, all);
		float awayEstimate = estimateGoalFromPlayerStats(ef, pfs, dictionary, false, all);

		// -----------------------------------------------
		// shots adjusted with pfs team expectancy)
		Pair avgShotsGeneral = FixtureUtils.selectAvgShots(all, ef.date, manual, goalsWeight);
		float avgHome = avgShotsGeneral.home;
		float avgAway = avgShotsGeneral.away;
		Pair avgShotsHomeTeam = FixtureUtils.selectAvgShotsHome(all, ef.homeTeam, ef.date, manual, goalsWeight);
		float homeShotsFor = avgShotsHomeTeam.home;
		float homeShotsAgainst = avgShotsHomeTeam.away;
		Pair avgShotsAwayTeam = FixtureUtils.selectAvgShotsAway(all, ef.awayTeam, ef.date, manual, goalsWeight);
		float awayShotsFor = avgShotsAwayTeam.home;
		float awayShotsAgainst = avgShotsAwayTeam.away;

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		Pair avgShotsByType = FixtureUtils.selectAvgShotsByType(all, ef.date, manual, goalsWeight);
		float avgShotsUnder = avgShotsByType.home;
		float avgShotsOver = avgShotsByType.away;
		float expected = homeEstimate * lambda + awayEstimate * mu;

		float dist = avgShotsOver - avgShotsUnder;
		// System.out.println(dist);

		if (avgShotsUnder > avgShotsOver) {
			return 0.5f;
		}
		if (expected >= avgShotsOver && expected > avgShotsUnder) {
			float score = 0.5f + 0.5f * (expected - avgShotsOver) / dist;
			return (score >= 0 && score <= 1f) ? score : 1f;
		} else if (expected <= avgShotsUnder && expected < avgShotsOver) {
			float score = 0.5f - 0.5f * (-expected + avgShotsUnder) / dist;
			return (score >= 0 && score <= 1f) ? score : 0f;
		} else {
			// System.out.println(f);
			return 0.5f;
		}

		// System.out.println(homeEstimate + " : " + awayEstimate);
		// return /* Utils.poissonOver(homeEstimate, awayEstimate)
		// */totalEstimate / 2;

	}

	private static float estimateGoalFromPlayerStats(Fixture ef, ArrayList<PlayerFixture> pfs,
			HashMap<String, String> dictionary, boolean home, ArrayList<Fixture> all) throws ParseException {
		ArrayList<PlayerFixture> homePlayers = getPlayers(ef, home, pfs, dictionary);
		if (!Utils.validatePlayers(homePlayers))
			System.out.println("Not a valid squad for " + ef);
		// printPlayers(homePlayers);
		ArrayList<Player> playerStatsHome = createStatistics(ef, home, pfs, dictionary);
		HashMap<String, Player> homeHash = (HashMap<String, Player>) playerStatsHome.stream()
				.collect(Collectors.toMap(Player::getName, Function.identity()));

		float avgGoalsHome = FixtureUtils.selectAvgHomeTeam(all, ef.homeTeam, ef.date).home;
		float avgGoalsAway = FixtureUtils.selectAvgAwayTeam(all, ef.awayTeam, ef.date).home;

		float homeAvgFor = home ? avgGoalsHome : avgGoalsAway;
		// float homeAvgFor = XlSUtils.selectAvgFor(sheet, home ? ef.homeTeam :
		// ef.awayTeam, ef.date);
		ArrayList<Player> keyAttackingPlayers = new ArrayList<>();
		// int totalGoals = 0, totalAssists = 0;
		// for (Player i : playerStatsHome) {
		// totalGoals += i.goals;
		// totalAssists += i.assists;
		// }

		// for (Player i : playerStatsHome) {
		// if (((float) i.goals / totalGoals) > 0.4f)
		// keyAttackingPlayers.add(i);
		// }

		// System.out.println(playerStatsHome);

		float goalRatio = 1f;
		float assistRatio = 1f - goalRatio;
		float homeEstimate = 0f;
		for (PlayerFixture i : homePlayers) {
			if (i.lineup) {
				if (homeHash.containsKey(i.name)) {
					homeEstimate += goalRatio * homeHash.get(i.name).getGoalAvg() * 90
							+ assistRatio * homeHash.get(i.name).getAssistAvg() * 90;
				}
			}
		}

		// for (Player i : keyAttackingPlayers) {
		// if (!homePlayers.contains(i))
		// return 0f;
		// }

		return homeEstimate / homeAvgFor;
	}

	private static boolean validatePlayers(ArrayList<PlayerFixture> players) {
		int lineups = 0;
		int subs = 0;

		for (PlayerFixture i : players) {
			if (i.lineup)
				lineups++;
			if (i.substitute)
				subs++;
		}

		boolean result = lineups == 11 && subs <= 3;
		if (!result)
			System.out.println("Not a valid squad for " + (players.isEmpty() ? " " : players.get(0).team));

		return result;
	}

	private static ArrayList<Player> createStatistics(Fixture i, boolean home, ArrayList<PlayerFixture> pfs,
			HashMap<String, String> dictionary) {
		HashMap<String, String> reverseDictionary = (HashMap<String, String>) dictionary.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		String team = home ? i.homeTeam : i.awayTeam;

		HashMap<String, ArrayList<PlayerFixture>> players = new HashMap<>();
		for (PlayerFixture pf : pfs) {
			if ((reverseDictionary.get(pf.team).equals(team) || reverseDictionary.get(pf.team).equals(team))
					&& pf.fixture.date.before(i.date)) {

				if (!players.containsKey(pf.name))
					players.put(pf.name, new ArrayList<>());

				players.get(pf.name).add(pf);
			}
		}

		ArrayList<Player> result = new ArrayList<>();
		for (Entry<String, ArrayList<PlayerFixture>> entry : players.entrySet()) {
			Player player = new Player(team, entry.getKey(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
			for (PlayerFixture pf : entry.getValue()) {
				player.minutesPlayed += pf.minutesPlayed;
				player.goals += pf.goals;
				player.assists += pf.assists;
				if (pf.lineup)
					player.lineups++;
				else if (pf.substitute)
					player.substitutes++;
				else
					player.subsWOP++;

				if (team.equals(pf.fixture.homeTeam)) {
					player.homeMinutesPlayed += pf.minutesPlayed;
					player.homeGoals += pf.goals;
					player.homeAssists += pf.assists;
					if (pf.lineup)
						player.homeLineups++;
					else if (pf.substitute)
						player.homeSubstitutes++;
					else
						player.homeSubsWOP++;
				} else {
					player.awayMinutesPlayed += pf.minutesPlayed;
					player.awayGoals += pf.goals;
					player.awayAssists += pf.assists;
					if (pf.lineup)
						player.awayLineups++;
					else if (pf.substitute)
						player.awaySubstitutes++;
					else
						player.awaySubsWOP++;
				}
			}
			result.add(player);
		}

		// Sort - goalscorers first
		result.sort(new Comparator<Player>() {

			@Override
			public int compare(Player o1, Player o2) {
				return ((Integer) o2.goals).compareTo((Integer) o1.goals);
			}
		});

		return result;
	}

	// TODO remove later
	public static void printPlayers(ArrayList<PlayerFixture> homePlayers) {
		if (homePlayers.size() > 0)
			System.out.println(homePlayers.get(0).team);
		for (PlayerFixture i : homePlayers) {
			System.out.println(i.name + " " + i.lineup + " " + i.minutesPlayed + "' " + i.goals + " " + i.assists);
		}
	}

	private static ArrayList<PlayerFixture> getPlayers(Fixture i, boolean home, ArrayList<PlayerFixture> pfs,
			HashMap<String, String> dictionary) {
		HashMap<String, String> reverseDictionary = (HashMap<String, String>) dictionary.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		ArrayList<PlayerFixture> result = new ArrayList<>();
		for (PlayerFixture pf : pfs) {
			if (reverseDictionary.get(pf.fixture.homeTeam).equals(i.homeTeam)
					&& reverseDictionary.get(pf.fixture.awayTeam).equals(i.awayTeam)
					&& (pf.fixture.date.equals(i.date) || pf.fixture.date.equals(Utils.getYesterday(i.date))
							|| pf.fixture.date.equals(Utils.getTommorow(i.date)))
					&& reverseDictionary.get(pf.team).equals(home ? i.homeTeam : i.awayTeam)) {
				result.add(pf);
			}
		}
		result.sort(new Comparator<PlayerFixture>() {

			@Override
			public int compare(PlayerFixture o1, PlayerFixture o2) {
				return ((Integer) o2.minutesPlayed).compareTo((Integer) o1.minutesPlayed);
			}
		});
		return result;
	}

	public static ArrayList<Fixture> getFixtures(ArrayList<PlayerFixture> pfs) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (PlayerFixture i : pfs) {
			if (!result.contains(i.fixture))
				result.add(i.fixture);
		}
		return result;
	}

	public static ArrayList<Fixture> notPending(ArrayList<Fixture> all) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : all) {
			if (i.getTotalGoals() >= 0)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<Fixture> pending(ArrayList<Fixture> all) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : all) {
			if (i.getTotalGoals() < 0)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<PlayerFixture> removeRepeats(ArrayList<PlayerFixture> all) {
		ArrayList<PlayerFixture> result = new ArrayList<>();

		for (PlayerFixture i : all) {
			boolean repeat = false;
			for (PlayerFixture j : result) {
				if (i.fixture.equals(j.fixture) && i.team.equals(j.team) && i.name.equals(j.name))
					repeat = true;
				break;
			}
			if (!repeat)
				result.add(i);
		}
		return result;
	}

	public static HashMap<String, ArrayList<FinalEntry>> byLeague(ArrayList<FinalEntry> all) {
		HashMap<String, ArrayList<FinalEntry>> leagues = new HashMap<>();
		for (FinalEntry i : all) {
			if (!leagues.containsKey(i.fixture.competition))
				leagues.put(i.fixture.competition, new ArrayList<>());

			leagues.get(i.fixture.competition).add(i);
		}
		return leagues;
	}

	/**
	 * Running precomputed finals with best TH from previous offset seasons
	 * 
	 * @param byLeagueYear
	 *            - hash map of finals by competition and year
	 * @param offset
	 *            - number of previous seasons based on which data the best th
	 *            will be computed
	 * @return
	 */
	public static ArrayList<FinalEntry> withBestThreshold(
			HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear, int offset, MaximizingBy maxBy) {
		ArrayList<FinalEntry> withTH = new ArrayList<>();

		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;

		for (java.util.Map.Entry<String, HashMap<Integer, ArrayList<FinalEntry>>> league : byLeagueYear.entrySet()) {
			start = Math.min(start, league.getValue().keySet().stream().min(Integer::compareTo).get());
			end = Math.max(start, league.getValue().keySet().stream().max(Integer::compareTo).get());
		}

		for (int i = start + offset; i <= end; i++) {
			for (java.util.Map.Entry<String, HashMap<Integer, ArrayList<FinalEntry>>> league : byLeagueYear
					.entrySet()) {
				ArrayList<FinalEntry> current = Utils.deepCopy(league.getValue().get(i));

				ArrayList<FinalEntry> data = new ArrayList<>();
				for (int j = i - offset; j < i; j++)
					if (league.getValue().containsKey(j))
						data.addAll(league.getValue().get(j));

				Settings initial = new Settings("", 0f, 0f, 0f, 0.55f, 0.55f, 0.55f, 0.5f, 0f).withShots(1f);
				initial = XlSUtils.findThreshold(data, initial, maxBy);
				ArrayList<FinalEntry> toAdd = XlSUtils.restrict(current, initial);

				if (maxBy.equals(MaximizingBy.UNDERS))
					toAdd = onlyUnders(toAdd);
				else if (maxBy.equals(MaximizingBy.OVERS))
					toAdd = onlyOvers(toAdd);

				withTH.addAll(toAdd);

			}
		}
		return withTH;
	}

	public static ArrayList<FinalEntry> withBestSettings(
			HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear, int offset) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;

		for (java.util.Map.Entry<String, HashMap<Integer, ArrayList<FinalEntry>>> league : byLeagueYear.entrySet()) {
			start = Math.min(start, league.getValue().keySet().stream().min(Integer::compareTo).get());
			end = Math.max(start, league.getValue().keySet().stream().max(Integer::compareTo).get());
		}

		float optimalOneIn = evaluateForBestSettingsWithParameters(start, end, offset, byLeagueYear, 2f, 200f, 0.5f);
		System.out.println("Optimal 1 in is: " + optimalOneIn);
		for (int i = start + offset; i <= end; i++) {
			for (java.util.Map.Entry<String, HashMap<Integer, ArrayList<FinalEntry>>> league : byLeagueYear
					.entrySet()) {
				ArrayList<FinalEntry> current = Utils.deepCopy(league.getValue().get(i));

				ArrayList<FinalEntry> data = new ArrayList<>();
				for (int j = i - offset; j < i; j++)
					if (league.getValue().containsKey(j))
						data.addAll(league.getValue().get(j));

				// analysys(data, -1, false);
				result.addAll(filterByPastResults(current, data, optimalOneIn));

				// Settings initial = new Settings("", 0f, 0f, 0f, 0.55f, 0.55f,
				// 0.55f, 0.5f, 0f).withShots(1f);
				// initial = XlSUtils.findThreshold(data, initial, maxBy);
				// ArrayList<FinalEntry> toAdd = XlSUtils.restrict(current,
				// initial);

				// if (maxBy.equals(MaximizingBy.UNDERS))
				// toAdd = onlyUnders(toAdd);
				// else if (maxBy.equals(MaximizingBy.OVERS))
				// toAdd = onlyOvers(toAdd);

				// withTH.addAll(toAdd);

			}
		}

		return result;
	}

	/**
	 * Helper function for finding optimal value for withBestSettings method
	 * 
	 * @param start
	 * @param end
	 * @param offset
	 * @param byLeagueYear
	 * @param lowerValue
	 * @param upperValue
	 */
	private static float evaluateForBestSettingsWithParameters(int start, int end, int offset,
			HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear, float lowerValue, float upperValue,
			float step) {
		float optimalValue = Float.NEGATIVE_INFINITY;
		float optimalResult = Float.NEGATIVE_INFINITY;

		for (float curr = lowerValue; curr <= upperValue; curr += step) {
			ArrayList<FinalEntry> result = new ArrayList<>();

			for (int i = start + offset; i <= end; i++) {
				for (java.util.Map.Entry<String, HashMap<Integer, ArrayList<FinalEntry>>> league : byLeagueYear
						.entrySet()) {
					ArrayList<FinalEntry> current = Utils.deepCopy(league.getValue().get(i));

					ArrayList<FinalEntry> data = new ArrayList<>();
					for (int j = i - offset; j < i; j++)
						if (league.getValue().containsKey(j))
							data.addAll(league.getValue().get(j));

					// analysys(data, -1, false);
					result.addAll(filterByPastResults(current, data, curr));

					// Settings initial = new Settings("", 0f, 0f, 0f, 0.55f,
					// 0.55f,
					// 0.55f, 0.5f, 0f).withShots(1f);
					// initial = XlSUtils.findThreshold(data, initial, maxBy);
					// ArrayList<FinalEntry> toAdd = XlSUtils.restrict(current,
					// initial);

					// if (maxBy.equals(MaximizingBy.UNDERS))
					// toAdd = onlyUnders(toAdd);
					// else if (maxBy.equals(MaximizingBy.OVERS))
					// toAdd = onlyOvers(toAdd);

					// withTH.addAll(toAdd);

				}
			}

			float currentEvaluation = evaluateRecord(result);
			if (currentEvaluation > optimalResult) {
				optimalResult = currentEvaluation;
				optimalValue = curr;
			}
		}

		return optimalValue;
	}

	// just all unders or overs for now
	private static Collection<? extends FinalEntry> filterByPastResults(ArrayList<FinalEntry> current,
			ArrayList<FinalEntry> data, float onein) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		boolean both = evaluateRecord(data) > onein;
		boolean onlyUnders = evaluateRecord(onlyUnders(data)) > onein;
		boolean onlyOvers = evaluateRecord(onlyOvers(data)) > onein;

		// if (both)
		// result.addAll(current);
		// if (onlyUnders)
		// result.addAll(onlyUnders(current));
		if (onlyOvers)
			result.addAll(onlyOvers(current));

		return result;
	}

	/**
	 * Deep copy list of finals
	 * 
	 * @param arrayList
	 * @return
	 */
	private static ArrayList<FinalEntry> deepCopy(ArrayList<FinalEntry> finals) {
		return (ArrayList<FinalEntry>) finals.stream().map(i -> new FinalEntry(i)).collect(Collectors.toList());
	}

	public static ArrayList<FinalEntry> notPendingFinals(ArrayList<FinalEntry> all) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : all) {
			if (i.fixture.getTotalGoals() >= 0)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<FinalEntry> pendingFinals(ArrayList<FinalEntry> all) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : all) {
			if (i.fixture.getTotalGoals() < 0)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<FinalEntry> todayGames(ArrayList<FinalEntry> value) {
		return (ArrayList<FinalEntry>) value.stream().filter(i -> isToday(i.fixture.date)).collect(Collectors.toList());
	}

	public static void byYear(ArrayList<FinalEntry> all, String description) {
		System.out.println(description);
		Map<Object, List<FinalEntry>> map = all.stream().collect(Collectors.groupingBy(p -> (Integer) p.fixture.year,
				Collectors.mapping(Function.identity(), Collectors.toList())));

		for (Entry<Object, List<FinalEntry>> i : map.entrySet()) {
			System.out.println(new Stats((ArrayList<FinalEntry>) i.getValue(), ((Integer) i.getKey()).toString()));
		}

		System.out.println(new Stats(all, description));
	}

	public static void byCompetition(ArrayList<FinalEntry> all, String description) {
		System.out.println(description);
		Map<Object, List<FinalEntry>> map = all.stream().collect(Collectors.groupingBy(p -> p.fixture.competition,
				Collectors.mapping(Function.identity(), Collectors.toList())));

		for (Entry<Object, List<FinalEntry>> i : map.entrySet()) {
			System.out.println(new Stats((ArrayList<FinalEntry>) i.getValue(), (i.getKey()).toString()));
		}

		System.out.println(new Stats(all, description));
	}

	/**
	 * Mutably changes the predictions of a list of finals to (prediction +
	 * x*impliedProb)/(x+1) where x is the weight of the implied probability of
	 * the odds
	 * 
	 * @param all
	 * @param oddsImpliedProbabilityWeight
	 */
	public static void weightedPredictions(ArrayList<FinalEntry> all, float oddsImpliedProbabilityWeight) {
		for (FinalEntry i : all) {
			float gain = i.prediction > i.threshold ? i.fixture.getMaxClosingOverOdds()
					: i.fixture.getMaxClosingUnderOdds();
			i.prediction = (i.prediction + oddsImpliedProbabilityWeight / gain) / (oddsImpliedProbabilityWeight + 1f);
		}
	}

	/**
	 * Finds the best half time evaluatuan representing linear combination of
	 * the average frequencies of 0,1,2 and more half time goals averages for
	 * both teams The data is selected from database
	 * 
	 * @param start
	 * @param end
	 * @param dataType
	 * @throws InterruptedException
	 */
	public static void optimalHTSettings(int start, int end, DataType dataType, MaximizingBy maxBy)
			throws InterruptedException {
		ArrayList<HTEntry> all = new ArrayList<>();

		for (int i = start; i <= end; i++) {
			ArrayList<HTEntry> finals = new ArrayList<>();
			for (String comp : Arrays.asList(Constants.SHOTS)) {
				finals.addAll(SQLiteJDBC.selectHTData(comp, i, "ht"));
			}

			// HashMap<String, ArrayList<FinalEntry>> byLeague =
			// Utils.byLeague(finals);
			// for (java.util.Map.Entry<String, ArrayList<FinalEntry>> league :
			// byLeague.entrySet()) {
			// if (!byLeagueYear.containsKey(league.getKey()))
			// byLeagueYear.put(league.getKey(), new HashMap<>());
			//
			// byLeagueYear.get(league.getKey()).put(i, league.getValue());

			// }

			all.addAll(finals);
		}

		float step = 0.1f;

		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestWinRatio = 0f;
		String bestDescription = null;
		float bestx, besty, bestz, bestw;
		bestx = besty = bestz = bestw = 0f;
		float bestTH = 0.3f;
		float bestEval = 1f;

		for (int i = 0; i <= 0; i++) {
			float currentTH = 0.22f + i * 0.01f;
			System.out.println(currentTH);
			for (HTEntry hte : all) {
				hte.fe.threshold = currentTH;
				hte.fe.lower = currentTH;
				hte.fe.upper = currentTH;
			}

			int xmax = (int) (1f / step);
			for (int x = 0; x <= xmax; x++) {
				int ymax = xmax - x;
				for (int y = 0; y <= ymax; y++) {
					int zmax = ymax - y;
					for (int z = 0; z <= zmax; z++) {
						int w = zmax - z;
						System.out.println(x * step + " " + y * step + " " + z * step + " " + w * step);

						for (HTEntry hte : all) {
							hte.fe.prediction = x * step * hte.zero + y * step * hte.one + z * step * hte.two
									+ w * step * hte.more;
						}

						float currentProfit, currentWinRate = 0f;
						float currEval = 1f;
						if (maxBy.equals(MaximizingBy.BOTH)) {
							if (all.size() < 100)
								continue;
							currentProfit = getProfitHT(all);
							currEval = evaluateRecord(getFinals(all));
							// currentWinRate = getSuccessRate(getFinals(all));
						} else if (maxBy.equals(MaximizingBy.UNDERS)) {
							if (onlyUnders(getFinals(all)).size() < 100)
								continue;
							currentProfit = getProfitHT(onlyUndersHT(all));
							currEval = evaluateRecord(onlyUnders(getFinals(all)));
							// currentWinRate
							// =getSuccessRate(onlyUnders(getFinals(all)));

						} else if (maxBy.equals(MaximizingBy.OVERS)) {
							if (onlyOvers(getFinals(all)).size() < 100)
								continue;
							currentProfit = getProfitHT(onlyOversHT(all));
							currEval = evaluateRecord(onlyOvers(getFinals(all)));
							// currentWinRate
							// =getSuccessRate(onlyOvers(getFinals(all)));
						} else {
							currentProfit = Float.NEGATIVE_INFINITY;
						}

						System.out.println(currentProfit);
						System.out.println("1 in " + currEval);

						if (/* currentProfit > bestProfit */ currEval > bestEval/*
																				 * currentWinRate
																				 * >
																				 * bestWinRatio
																				 */) {
							bestProfit = currentProfit;
							bestEval = currEval;
							bestWinRatio = currentWinRate;
							bestx = step * x;
							besty = step * y;
							bestz = step * z;
							bestw = step * w;
							bestTH = currentTH;
							bestDescription = x * step + "*zero + " + y * step + "*one + " + z * step + " *two+ "
									+ w * step + " *>=3";
							// System.out.println(bestProfit);
							// System.out.println("1 in " + bestEval);
						}

					}
				}
			}
		}

		for (HTEntry hte : all) {
			hte.fe.prediction = bestx * hte.zero + besty * hte.one + bestz * hte.two + bestw * hte.more;
			hte.fe.threshold = bestTH;
			hte.fe.lower = bestTH;
			hte.fe.upper = bestTH;
		}

		if (maxBy.equals(MaximizingBy.UNDERS))
			all = onlyUndersHT(all);
		else if (maxBy.equals(MaximizingBy.OVERS))
			all = onlyOversHT(all);

		System.out.println(bestProfit);
		System.out.println(bestTH);
		System.out.println("1 in " + bestEval);
		System.out.println(new Stats(getFinals(all), bestDescription));

	}

	private static ArrayList<HTEntry> onlyOversHT(ArrayList<HTEntry> finals) {
		ArrayList<HTEntry> result = new ArrayList<>();
		for (HTEntry i : finals) {
			if (i.fe.prediction >= i.fe.threshold)
				result.add(i);
		}
		return result;
	}

	private static ArrayList<HTEntry> onlyUndersHT(ArrayList<HTEntry> finals) {
		ArrayList<HTEntry> result = new ArrayList<>();
		for (HTEntry i : finals) {
			if (i.fe.prediction < i.fe.threshold)
				result.add(i);
		}
		return result;
	}

	/**
	 * Returns list of final entries from list of ht entries
	 * 
	 * @param all
	 * @return
	 */
	private static ArrayList<FinalEntry> getFinals(ArrayList<HTEntry> all) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (HTEntry i : all) {
			result.add(i.fe);
		}

		return result;
	}

	/**
	 * Returns profit from a list of half time entries
	 * 
	 * @param all
	 * @return
	 */
	private static float getProfitHT(ArrayList<HTEntry> all) {
		float profit = 0f;
		for (HTEntry i : all) {
			profit += i.fe.getProfit();
		}
		return profit;
	}

	public static void fastSearch(int start, int end, DataType dataType, MaximizingBy maxBy)
			throws InterruptedException {
		ArrayList<HTEntry> all = new ArrayList<>();

		for (int i = start; i <= end; i++) {
			ArrayList<HTEntry> finals = new ArrayList<>();
			for (String comp : Arrays.asList(Constants.SHOTS)) {
				finals.addAll(SQLiteJDBC.selectHTData(comp, i, "ht"));
			}
			all.addAll(finals);
		}

		float step = 0.0005f;

		float bestProfit = Float.NEGATIVE_INFINITY;
		String bestDescription = null;
		float bestx, besty = 0, bestz, bestw;
		bestx = 0f;
		float bestTH = 0.3f;
		float bestEval = 1f;

		int xmax = (int) (1f / step);
		for (int x = 0; x <= xmax; x++) {
			int y = xmax - x;

			for (HTEntry hte : all) {
				hte.fe.prediction = x * step * hte.zero + y * step * hte.one;
			}

			float currentProfit, currentWinRate = 0f;
			float currEval = 1f;
			if (maxBy.equals(MaximizingBy.BOTH)) {
				if (all.size() < 100)
					continue;
				currentProfit = getProfitHT(all);
				currEval = evaluateRecord(getFinals(all));
				// currentWinRate = getSuccessRate(getFinals(all));
			} else if (maxBy.equals(MaximizingBy.UNDERS)) {
				if (onlyUnders(getFinals(all)).size() < 100)
					continue;
				currentProfit = getProfitHT(onlyUndersHT(all));
				currEval = evaluateRecord(onlyUnders(getFinals(all)));
				// currentWinRate
				// =getSuccessRate(onlyUnders(getFinals(all)));

			} else if (maxBy.equals(MaximizingBy.OVERS)) {
				if (onlyOvers(getFinals(all)).size() < 100)
					continue;
				currentProfit = getProfitHT(onlyOversHT(all));
				currEval = evaluateRecord(onlyOvers(getFinals(all)));
				// currentWinRate
				// =getSuccessRate(onlyOvers(getFinals(all)));
			} else {
				currentProfit = Float.NEGATIVE_INFINITY;
			}

			System.out.println(x * step + " " + y * step);
			System.out.println(currentProfit);
			System.out.println("1 in " + currEval);

			if (/* currentProfit > bestProfit */ currEval > bestEval/*
																	 * currentWinRate
																	 * >
																	 * bestWinRatio
																	 */) {
				bestProfit = currentProfit;
				bestEval = currEval;
				bestx = step * x;
				besty = step * y;
				bestDescription = x * step + "*zero + " + y * step + "*one + ";
				// System.out.println(bestProfit);
				// System.out.println("1 in " + bestEval);

			}
		}

		for (HTEntry hte : all) {
			hte.fe.prediction = bestx * hte.zero + besty * hte.one;
			// hte.fe.threshold = bestTH;
			// hte.fe.lower = bestTH;
			// hte.fe.upper = bestTH;
		}

		if (maxBy.equals(MaximizingBy.UNDERS))
			all = onlyUndersHT(all);
		else if (maxBy.equals(MaximizingBy.OVERS))
			all = onlyOversHT(all);

		System.out.println(bestProfit);
		System.out.println(bestTH);
		System.out.println("1 in " + bestEval);
		System.out.println(new Stats(getFinals(all), bestDescription));

	}

	/**
	 * Return exactly the oposite prediction for all finals (For testing of
	 * significantly bad results) Mutator
	 * 
	 * @param finals
	 * @return
	 */
	public static void theOposite(ArrayList<FinalEntry> finals) {
		for (FinalEntry i : finals) {
			i.prediction = i.prediction >= i.threshold ? 0f : 1f;
		}
	}

	public static ArrayList<FinalEntry> gamesForDay(ArrayList<FinalEntry> pending, LocalDate date) {
		return (ArrayList<FinalEntry>) pending.stream()
				.filter(i -> date.equals(i.fixture.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))
				.collect(Collectors.toList());
	}

	public static ArrayList<FinalEntry> setTrueOddsProportional(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			FullEntry full = (FullEntry) i;
			Odds odds = new OverUnderOdds(full.line.bookmaker, full.fixture.date, full.line.line, full.line.home,
					full.line.away);
			OverUnderOdds trueOdds = (OverUnderOdds) odds.getTrueOddsMarginal();
			full.line.home = trueOdds.overOdds;
			full.line.away = trueOdds.underOdds;
			result.add(full);
		}

		return result;
	}

}
