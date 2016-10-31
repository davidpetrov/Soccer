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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entries.FinalEntry;
import entries.FullEntry;
import main.ExtendedFixture;
import main.FullFixture;
import main.Line;
import main.Result;
import main.SQLiteJDBC;
import results.Results;
import scraper.Names;
import scraper.Scraper;
import settings.Settings;
import tables.Position;
import tables.Table;
import xls.XlSUtils;

/**
 * @author Tereza
 *
 */
/**
 * @author Tereza
 *
 */
public class Utils {
	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final String TOKEN = "19f6c3cd0bd54c4286322c08734b53bd";
	static int count = 50;
	static long start = System.currentTimeMillis();

	public static String query(String address) throws IOException {
		if (--count == 0)
			try {
				long now = System.currentTimeMillis();
				System.out.println("Sleeping for " + (61 * 1000 - (now - start)) / 1000);
				long time = 61 * 1000 - (now - start);
				Thread.sleep(time < 0 ? 61 : time);
				count = 50;
				start = System.currentTimeMillis();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.addRequestProperty("X-Auth-Token", TOKEN);
		InputStreamReader isr = null;
		isr = new InputStreamReader(conn.getInputStream());
		BufferedReader bfr = new BufferedReader(isr);
		String output;
		StringBuffer sb = new StringBuffer();
		while ((output = bfr.readLine()) != null) {
			sb.append(output);
		}
		return sb.toString();
	}

	@SuppressWarnings("unused")
	public static ArrayList<ExtendedFixture> createFixtureList(JSONArray arr) throws JSONException, ParseException {
		ArrayList<ExtendedFixture> fixtures = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject f = arr.getJSONObject(i);
			String date = f.getString("date");
			String status;
			try {
				status = f.getString("status");
			} catch (Exception e) {
				status = "FINISHED";
			}
			int matchday = f.getInt("matchday");
			String homeTeamName = f.getString("homeTeamName");
			String awayTeamName = f.getString("awayTeamName");
			int goalsHomeTeam = f.getJSONObject("result").getInt("goalsHomeTeam");
			int goalsAwayTeam = f.getJSONObject("result").getInt("goalsAwayTeam");
			String links_homeTeam = f.getJSONObject("_links").getJSONObject("homeTeam").getString("href");
			String links_awayTeam = f.getJSONObject("_links").getJSONObject("awayTeam").getString("href");
			String competition = f.getJSONObject("_links").getJSONObject("soccerseason").getString("href");

			ExtendedFixture ef = new ExtendedFixture(format.parse(date), homeTeamName, awayTeamName,
					new Result(goalsHomeTeam, goalsAwayTeam), competition).withMatchday(matchday);
			fixtures.add(ef);
		}
		return fixtures;
	}

	@SuppressWarnings("unused")
	public static ArrayList<ExtendedFixture> createFixtureList(JSONObject obj) throws JSONException, ParseException {
		ArrayList<ExtendedFixture> fixtures = new ArrayList<>();
		String date = obj.getString("date");
		String status = obj.getString("status");
		int matchday = obj.getInt("matchday");
		String homeTeamName = obj.getString("homeTeamName");
		String awayTeamName = obj.getString("awayTeamName");
		int goalsHomeTeam = obj.getJSONObject("result").getInt("goalsHomeTeam");
		int goalsAwayTeam = obj.getJSONObject("result").getInt("goalsAwayTeam");
		String links_homeTeam = obj.getJSONObject("_links").getJSONObject("homeTeam").getString("href");
		String links_awayTeam = obj.getJSONObject("_links").getJSONObject("homeTeam").getString("href");
		String competition = obj.getJSONObject("_links").getJSONObject("soccerseason").getString("href");

		ExtendedFixture f = new ExtendedFixture(format.parse(date), homeTeamName, awayTeamName,
				new Result(goalsHomeTeam, goalsAwayTeam), competition).withMatchday(matchday);
		fixtures.add(f);
		return fixtures;
	}

	// get last n fixtures from a list
	// assumes the ordering is the from older to newer
	// public static ArrayList<Fixture> getLastFixtures(ArrayList<Fixture>
	// fixtures, int n) {
	// ArrayList<Fixture> last = new ArrayList<>();
	// int returnedSize = fixtures.size() >= n ? n : fixtures.size();
	// Collections.sort(fixtures, Collections.reverseOrder());
	// for (int i = 0; i < returnedSize; i++) {
	// last.add(fixtures.get(i));
	// }
	// return last;
	// }

	public static ArrayList<ExtendedFixture> getLastFixtures(ArrayList<ExtendedFixture> fixtures, int n) {
		ArrayList<ExtendedFixture> last = new ArrayList<>();
		int returnedSize = fixtures.size() >= n ? n : fixtures.size();
		Collections.sort(fixtures, Collections.reverseOrder());
		for (int i = 0; i < returnedSize; i++) {
			last.add(fixtures.get(i));
		}
		return last;
	}

	public static ArrayList<ExtendedFixture> getHomeFixtures(ExtendedFixture f, ArrayList<ExtendedFixture> fixtures) {
		ArrayList<ExtendedFixture> home = new ArrayList<>();
		for (ExtendedFixture i : fixtures) {
			if (f.homeTeam.equals(i.homeTeam))
				home.add(i);
		}
		return home;
	}

	public static ArrayList<ExtendedFixture> getAwayFixtures(ExtendedFixture f, ArrayList<ExtendedFixture> fixtures) {
		ArrayList<ExtendedFixture> away = new ArrayList<>();
		for (ExtendedFixture i : fixtures) {
			if (f.awayTeam.equals(i.awayTeam))
				away.add(i);
		}
		return away;
	}

	// public static ArrayList<Fixture> getForDate(ArrayList<Fixture> fixtures,
	// int day) {
	// ArrayList<Fixture> result = new ArrayList<>();
	// DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	// for (Fixture i : fixtures) {
	// try {
	// if (format.parse(i.date).getDay() == day)
	// result.add(i);
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// }
	// return result;
	// }

	public static float countOverGamesPercent(ArrayList<ExtendedFixture> fixtures) {
		int count = 0;
		for (ExtendedFixture f : fixtures) {
			if (f.getTotalGoals() > 2.5d)
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static float countBTSPercent(ArrayList<ExtendedFixture> fixtures) {
		int count = 0;
		for (ExtendedFixture f : fixtures) {
			if (f.bothTeamScore())
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static float findAvg(ArrayList<ExtendedFixture> lastHomeTeam) {
		float total = 0;
		for (ExtendedFixture f : lastHomeTeam) {
			total += f.getTotalGoals();
		}
		return total / lastHomeTeam.size();
	}

	public static float poisson(float lambda, int goal) {
		return (float) (Math.pow(lambda, goal) * Math.exp(-lambda) / factorial(goal));
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

	private static int factorial(int n) {
		if (n == 0)
			return 1;
		else
			return n * factorial(n - 1);
	}

	public static float avgFor(String team, ArrayList<ExtendedFixture> fixtures) {
		float total = 0;
		for (ExtendedFixture f : fixtures) {
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

	public static float getSuccessRate(ArrayList<FinalEntry> list, float threshold) {
		int success = 0;
		for (FinalEntry fe : list) {
			fe.threshold = threshold;
			if (fe.success())
				success++;
		}
		return (float) success / list.size();
	}

	public static ArrayList<FinalEntry> filterByOdds(ArrayList<FinalEntry> finals, float minOdds, float maxOdds,
			float threshold) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float coeff = fe.prediction > threshold ? fe.fixture.maxOver : fe.fixture.maxUnder;
			if (coeff >= minOdds && coeff <= maxOdds)
				filtered.add(fe);
		}
		return filtered;
	}

	public static ArrayList<ExtendedFixture> getByMatchday(ArrayList<ExtendedFixture> all, int i) {
		ArrayList<ExtendedFixture> filtered = new ArrayList<>();
		for (ExtendedFixture f : all) {
			if (f.matchday == i)
				filtered.add(f);
		}
		return filtered;
	}

	public static ArrayList<ExtendedFixture> getBeforeMatchday(ArrayList<ExtendedFixture> all, int i) {
		ArrayList<ExtendedFixture> filtered = new ArrayList<>();
		for (ExtendedFixture f : all) {
			if (f.matchday < i)
				filtered.add(f);
		}
		return filtered;
	}

	public static float[] getScaledProfit(ArrayList<FinalEntry> finals, float f) {
		float profit = 0.0f;
		float staked = 0f;
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float cot = fe.prediction > fe.threshold ? (fe.prediction - fe.threshold) : (fe.threshold - fe.prediction);
			float betsize = 1;
			float value = certainty * gain;
			if (value > fe.value) {
				staked += betsize;
				if (fe.success()) {
					if (gain != -1.0d) {
						profit += gain * betsize;
					}
				}
			}
		}
		float[] result = new float[2];
		result[0] = profit - staked;
		result[1] = staked;
		return result;
	}

	public static ArrayList<ExtendedFixture> onlyFixtures(ArrayList<FinalEntry> finals) {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
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
				overProfit += i.success() ? (i.fixture.maxOver - 1f) : -1f;
			}
			if (i.prediction < i.lower) {
				underCnt++;
				underProfit += i.success() ? (i.fixture.maxUnder - 1f) : -1f;
			}
		}

		System.out.println(overCnt + " overs with profit: " + overProfit);
		System.out.println(underCnt + " unders with profit: " + underProfit);
	}

	public static float countOverHalfTime(ArrayList<ExtendedFixture> fixtures, int i) {

		int count = 0;
		for (ExtendedFixture f : fixtures) {
			if (f.getHalfTimeGoals() >= i)
				count++;
		}

		return fixtures.size() == 0 ? 0 : ((float) count / fixtures.size());
	}

	public static ArrayList<ExtendedFixture> filterByOdds(ArrayList<ExtendedFixture> data, float min, float max) {
		ArrayList<ExtendedFixture> filtered = new ArrayList<>();
		for (ExtendedFixture i : data) {
			if (i.maxOver <= max && i.maxOver >= min)
				filtered.add(i);
		}
		return filtered;
	}

	public static float countOversWhenDraw(ArrayList<ExtendedFixture> all) {
		int count = 0;
		for (ExtendedFixture i : all) {
			if (i.result.goalsHomeTeam == i.result.goalsAwayTeam && i.getTotalGoals() > 2.5f)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static float countOversWhenNotDraw(ArrayList<ExtendedFixture> all) {
		int count = 0;
		for (ExtendedFixture i : all) {
			if (i.result.goalsHomeTeam != i.result.goalsAwayTeam && i.getTotalGoals() > 2.5f)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static float countDraws(ArrayList<ExtendedFixture> all) {
		int count = 0;
		for (ExtendedFixture i : all) {
			if (i.result.goalsHomeTeam == i.result.goalsAwayTeam)
				count++;
		}
		return all.size() == 0 ? 0 : ((float) count / all.size());
	}

	public static void byWeekDay(ArrayList<ExtendedFixture> all) {
		int[] days = new int[8];
		int[] overs = new int[8];
		String[] literals = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

		for (ExtendedFixture i : all) {
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

	private static FinalEntry getFE(ArrayList<FinalEntry> finals2, FinalEntry fe) {
		for (FinalEntry i : finals2) {
			if (i.fixture.equals(fe.fixture))
				return i;
		}
		return null;
	}

	public static void bestNperWeek(ArrayList<FinalEntry> all, int n) {
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
						for (int j = 0; j < n; j++) {
							if (curr.get(j).success()) {
								coeff *= curr.get(j).prediction >= curr.get(j).upper ? curr.get(j).fixture.maxOver
										: curr.get(j).fixture.maxUnder;
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
						System.out.println(curr.get(0).fixture.date + " " + " " + successes + " not loss: " + notlosses
								+ " pr: " + (coeff != -1f ? (coeff - 1f) : coeff));
						profit += (coeff != -1f ? (coeff - 1f) : coeff);
					}
					curr = new ArrayList<>();
				} else {
					break;
				}
			}
		}
		System.out.println("Total from " + n + "s: " + profit);

	}

	public static void analysys(ArrayList<FinalEntry> all, int year) {
		ArrayList<FinalEntry> overs = new ArrayList<>();
		ArrayList<FinalEntry> unders = new ArrayList<>();

		// System.out.println(all);
		for (FinalEntry fe : all) {

			if (fe.isOver())
				overs.add(fe);
			else
				unders.add(fe);
		}
		System.err.println(year);
		System.out.println(overs.size() + " overs with rate: " + format(100 * Utils.getSuccessRate(overs)) + " profit: "
				+ format(Utils.getProfit(overs)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(overs) / overs.size()));
		System.out.println(unders.size() + " unders with rate: " + format(100 * Utils.getSuccessRate(unders))
				+ " profit: " + format(Utils.getProfit(unders)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(unders) / unders.size()));

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
			}

		}

		System.out.println(cer80.size() + " 80s with rate: " + format(100 * Utils.getSuccessRate(cer80)) + " profit: "
				+ format(Utils.getProfit(cer80)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cer80) / cer80.size()));
		System.out.println(cer70.size() + " 70s with rate: " + format(100 * Utils.getSuccessRate(cer70)) + " profit: "
				+ format(Utils.getProfit(cer70)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cer70) / cer70.size()));
		System.out.println(cer60.size() + " 60s with rate: " + format(100 * Utils.getSuccessRate(cer60)) + " profit: "
				+ format(Utils.getProfit(cer60)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cer60) / cer60.size()));
		System.out.println(cer50.size() + " 50s with rate: " + format(100 * Utils.getSuccessRate(cer50)) + " profit: "
				+ format(Utils.getProfit(cer50)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cer50) / cer50.size()));
		System.out.println(cer40.size() + " under50s with rate: " + format(100 * Utils.getSuccessRate(cer40))
				+ " profit: " + format(Utils.getProfit(cer40)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cer40) / cer40.size()));

		System.out.println(cot25.size() + " cot25s with rate: " + format(100 * Utils.getSuccessRate(cot25))
				+ " profit: " + format(Utils.getProfit(cot25)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cot25) / cot25.size()));
		System.out.println(cot20.size() + " cot20s with rate: " + format(100 * Utils.getSuccessRate(cot20))
				+ " profit: " + format(Utils.getProfit(cot20)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cot20) / cot20.size()));
		System.out.println(cot15.size() + " cot15s with rate: " + format(100 * Utils.getSuccessRate(cot15))
				+ " profit: " + format(Utils.getProfit(cot15)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cot15) / cot15.size()));
		System.out.println(cot10.size() + " cot10s with rate: " + format(100 * Utils.getSuccessRate(cot10))
				+ " profit: " + format(Utils.getProfit(cot10)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(cot10) / cot10.size()));

		// byOdds(all);

		int onlyOvers = 0;
		float onlyOversProfit = 0f;
		for (FinalEntry fe : all) {
			if (fe.fixture.getTotalGoals() > 2.5) {
				onlyOvers++;
				onlyOversProfit += fe.fixture.maxOver;
			}
		}

		System.out.println("Only overs: " + format((float) onlyOvers / all.size()) + " profit: "
				+ format((onlyOversProfit - all.size())));

		int onlyUnders = 0;
		float onlyUndersProfit = 0f;
		for (FinalEntry fe : all) {
			if (fe.fixture.getTotalGoals() < 2.5) {
				onlyUnders++;
				onlyUndersProfit += fe.fixture.maxUnder;
			}
		}

		System.out.println("Only unders: " + format((float) onlyUnders / all.size()) + " profit: "
				+ format((onlyUndersProfit - all.size())));

		int betterOdds = 0;
		float betterOddsProfit = 0f;
		for (FinalEntry fe : all) {
			float biggerOdds = fe.fixture.maxOver >= fe.fixture.maxUnder ? fe.fixture.maxOver : fe.fixture.maxUnder;
			boolean pred = fe.fixture.maxOver >= fe.fixture.maxUnder;
			if ((pred && fe.fixture.getTotalGoals() > 2.5) || (!pred && fe.fixture.getTotalGoals() < 2.5)) {
				betterOdds++;
				betterOddsProfit += biggerOdds;
			}
		}

		System.out.println("Better odds choice: " + format((float) betterOdds / all.size()) + " profit: "
				+ format((betterOddsProfit - all.size())));

		int wins = 0;
		float draws = 0f;
		int certs = 0;
		for (FinalEntry fe : all) {
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

		System.out.println("Soft lines wins: " + format((float) wins / certs) + " draws: "
				+ format((float) draws / certs) + " not losses: " + format((float) (wins + draws) / certs));
	}

	private static void byOdds(ArrayList<FinalEntry> all) {

		ArrayList<FinalEntry> under13 = new ArrayList();
		ArrayList<FinalEntry> under14 = new ArrayList();
		ArrayList<FinalEntry> under15 = new ArrayList();
		ArrayList<FinalEntry> under16 = new ArrayList();
		ArrayList<FinalEntry> under17 = new ArrayList();
		ArrayList<FinalEntry> under18 = new ArrayList();
		ArrayList<FinalEntry> under19 = new ArrayList();
		ArrayList<FinalEntry> under20 = new ArrayList();
		ArrayList<FinalEntry> under21 = new ArrayList();
		ArrayList<FinalEntry> under22 = new ArrayList();
		ArrayList<FinalEntry> over22 = new ArrayList();

		for (FinalEntry i : all) {
			float odds = i.isOver() ? i.fixture.maxOver : i.fixture.maxUnder;
			if (odds < 1.3) {
				under13.add(i);
			} else if (odds < 1.4) {
				under14.add(i);
			} else if (odds < 1.5) {
				under15.add(i);
			} else if (odds < 1.6) {
				under16.add(i);
			} else if (odds < 1.7) {
				under17.add(i);
			} else if (odds < 1.8) {
				under18.add(i);
			} else if (odds < 1.9) {
				under19.add(i);
			} else if (odds < 2) {
				under20.add(i);
			} else if (odds < 21) {
				under21.add(i);
			} else if (odds < 22) {
				under22.add(i);
			} else {
				over22.add(i);
			}

		}
		info("under13", under13);
		info("under14", under14);
		info("under15", under15);
		info("under16", under16);
		info("under17", under17);
		info("under18", under18);
		info("under19", under19);
		info("under20", under20);
		info("under21", under21);
		info("under22", under22);
		info("over22", over22);

	}

	private static void info(String string, ArrayList<FinalEntry> all) {
		System.out.println(all.size() + " " + string + " with rate: " + format(100 * Utils.getSuccessRate(all))
				+ " profit: " + format(Utils.getProfit(all)) + " yield: "
				+ String.format("%.2f%%", 100 * Utils.getProfit(all) / all.size()));

	}

	private static String format(float d) {
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
					float c1 = all.get(i).prediction > all.get(i).upper ? all.get(i).fixture.maxOver
							: all.get(i).fixture.maxUnder;
					float c2 = all.get(i + 1).prediction > all.get(i + 1).upper ? all.get(i + 1).fixture.maxOver
							: all.get(i + 1).fixture.maxUnder;
					float c3 = all.get(i + 2).prediction > all.get(i + 2).upper ? all.get(i + 2).fixture.maxOver
							: all.get(i + 2).fixture.maxUnder;
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
				float gain = i.prediction >= i.upper ? i.fixture.maxOver : i.fixture.maxUnder;
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
				float gain = i.prediction >= i.upper ? i.fixture.maxOver : i.fixture.maxUnder;
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
					profitUnder += i.fixture.drawOdds;
				}

			} else if (i.prediction >= i.upper) {
				over++;
				if (i.fixture.result.goalsHomeTeam == i.fixture.result.goalsAwayTeam) {
					drawOver++;
					profitOver += i.fixture.drawOdds;
				}
			}
		}

		System.out.println("Draws when under pr: " + (profitUnder - under) + " from " + under + " "
				+ Results.format((float) (profitUnder - under) * 100 / under) + "%");
		System.out.println("Draws when over pr: " + (profitOver - over) + " from " + over + " "
				+ Results.format((float) (profitOver - over) * 100 / over) + "%");
	}

	public static Table createTable(ArrayList<ExtendedFixture> data, String sheetName, int year, int i) {
		HashMap<String, Position> teams = getTeams(data);

		Table table = new Table(sheetName, year, i);

		for (String team : teams.keySet()) {
			ExtendedFixture f = new ExtendedFixture(null, team, team, null, null);
			ArrayList<ExtendedFixture> all = Utils.getHomeFixtures(f, data);
			all.addAll(Utils.getAwayFixtures(f, data));
			Position pos = createPosition(team, all);
			table.positions.add(pos);
		}

		table.sort();
		return table;
	}

	private static Position createPosition(String team, ArrayList<ExtendedFixture> all) {
		Position pos = new Position();
		pos.team = team;

		for (ExtendedFixture i : all) {
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

	private static HashMap<String, Position> getTeams(ArrayList<ExtendedFixture> data) {
		HashMap<String, Position> teams = new HashMap<>();

		for (ExtendedFixture i : data) {
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

	public static float basicSimilar(ExtendedFixture f, HSSFSheet sheet, Table table) throws ParseException {
		ArrayList<String> filterHome = table.getSimilarTeams(f.awayTeam);
		ArrayList<String> filterAway = table.getSimilarTeams(f.homeTeam);

		ArrayList<ExtendedFixture> lastHomeTeam = filter(f.homeTeam,
				XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date), filterHome);
		ArrayList<ExtendedFixture> lastAwayTeam = filter(f.awayTeam,
				XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date), filterAway);

		ArrayList<ExtendedFixture> lastHomeHomeTeam = filter(f.homeTeam,
				XlSUtils.selectLastHome(sheet, f.homeTeam, 25, f.date), filterHome);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = filter(f.awayTeam,
				XlSUtils.selectLastAway(sheet, f.awayTeam, 25, f.date), filterAway);

		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return 0.6f * allGamesAVG + 0.3f * homeAwayAVG + 0.1f * BTSAVG;
	}

	public static float similarPoisson(ExtendedFixture f, HSSFSheet sheet, Table table) throws ParseException {
		ArrayList<String> filterHome = table.getSimilarTeams(f.awayTeam);
		ArrayList<String> filterAway = table.getSimilarTeams(f.homeTeam);

		ArrayList<ExtendedFixture> lastHomeTeam = filter(f.homeTeam,
				XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date), filterHome);
		ArrayList<ExtendedFixture> lastAwayTeam = filter(f.awayTeam,
				XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date), filterAway);

		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	private static ArrayList<ExtendedFixture> filter(String team, ArrayList<ExtendedFixture> selectLastAll,
			ArrayList<String> filterHome) {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		for (ExtendedFixture i : selectLastAll) {
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

	public static ArrayList<FinalEntry> allOvers(ArrayList<ExtendedFixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (ExtendedFixture i : current) {
			FinalEntry n = new FinalEntry(i, 1f, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static ArrayList<FinalEntry> allUnders(ArrayList<ExtendedFixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (ExtendedFixture i : current) {
			FinalEntry n = new FinalEntry(i, 0f, i.result, 0.55f, 0.55f, 0.55f);
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

	public static float getAvgOdds(ArrayList<FinalEntry> finals) {
		float total = 0f;
		for (FinalEntry i : finals) {
			float coeff = i.prediction >= i.upper ? i.fixture.maxOver : i.fixture.maxUnder;
			total += coeff;
		}
		return finals.size() == 0 ? 0 : total / finals.size();
	}

	public static ArrayList<FinalEntry> higherOdds(ArrayList<ExtendedFixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (ExtendedFixture i : current) {
			float prediction = i.maxOver >= i.maxUnder ? 1f : 0f;
			FinalEntry n = new FinalEntry(i, prediction, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	public static float pValueCalculator(int count, float yield, float avgOdds) {
		double standardDeviation = Math.pow((1 + yield) * (avgOdds - 1 - yield), 0.5);
		double tStatistic = yield * Math.pow(count, 0.5) / standardDeviation;
		TDistribution td = new TDistribution(count - 1);
		double pValue = 1 - td.cumulativeProbability(tStatistic);
		return (float) (1 / pValue);
	}

	public static void evaluateRecord(ArrayList<FinalEntry> all) {
		float yield = Utils.getYield(all);
		float avgOdds = Utils.getAvgOdds(all);
		System.out.println(format(Utils.getProfit(all)) + " from " + all.size() + " picks "
				+ String.format("%.2f%%", 100 * yield) + " yield avgOdds: " + format(avgOdds));
		float pValue = pValueCalculator(all.size(), Utils.getYield(all), Utils.getAvgOdds(all));
		if (yield >= 0)
			System.out.println("1 in " + format(pValue));

	}

	private static float getYield(ArrayList<FinalEntry> all) {
		return getProfit(all) / all.size();
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

	public static float avgReturn(ArrayList<ExtendedFixture> all) {
		float total = 0f;
		for (ExtendedFixture i : all) {
			total += 1f / i.maxOver + 1f / i.maxUnder;
		}
		return all.size() == 0 ? 0 : total / all.size();
	}

	public static void fairValue(ArrayList<ExtendedFixture> current) {

		for (ExtendedFixture i : current) {
			float sum = 1f / i.maxOver + 1f / i.maxUnder;
			i.maxOver = 1f / ((1f / i.maxOver) / sum);
			i.maxUnder = 1f / ((1f / i.maxUnder) / sum);
		}
	}

	public static ArrayList<FinalEntry> runRandom(ArrayList<ExtendedFixture> current) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		Random random = new Random();
		for (ExtendedFixture i : current) {
			boolean next = random.nextBoolean();
			float prediction = next ? 1f : 0f;
			FinalEntry n = new FinalEntry(i, prediction, i.result, 0.55f, 0.55f, 0.55f);
			result.add(n);
		}
		return result;
	}

	// for update of results from soccerway
	public static Date findLastPendingFixture(ArrayList<ExtendedFixture> all) {

		all.sort(new Comparator<ExtendedFixture>() {

			@Override
			public int compare(ExtendedFixture o1, ExtendedFixture o2) {
				return o1.date.compareTo(o2.date);
			}
		});

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

		return lastNotPending;

	}

	public static ArrayList<FinalEntry> mainGoalLine(ArrayList<FinalEntry> finals,
			HashMap<ExtendedFixture, FullFixture> map) {
		ArrayList<FinalEntry> fulls = new ArrayList<>();
		for (FinalEntry f : finals) {

			fulls.add(new FullEntry(f.fixture, f.prediction, f.result, f.threshold, f.lower, f.upper,
					map.get(f.fixture).goalLines.main));
		}

		return fulls;
	}

	// TO DO
	public static ArrayList<FinalEntry> bestValueByDistibution(ArrayList<FinalEntry> finals,
			HashMap<ExtendedFixture, FullFixture> map, ArrayList<ExtendedFixture> all, HSSFSheet sheet) {
		ArrayList<FinalEntry> fulls = new ArrayList<>();
		for (FinalEntry f : finals) {

			int[] distributionHome = getGoalDistribution(f.fixture, all, f.fixture.homeTeam);
			int[] distributionAway = getGoalDistribution(f.fixture, all, f.fixture.awayTeam);
			FullEntry best = findBestLineFullEntry(f, distributionHome, distributionAway, map);

			fulls.add(best);
		}

		return fulls;
	}

	private static FullEntry findBestLineFullEntry(FinalEntry f, int[] distributionHome, int[] distributionAway,
			HashMap<ExtendedFixture, FullFixture> map) {
		FullEntry best = new FullEntry(f.fixture, f.prediction, f.result, f.threshold, f.lower, f.upper,
				map.get(f.fixture).goalLines.main);
		FullFixture full = map.get(f.fixture);
		Result originalResult = new Result(full.result.goalsHomeTeam, full.result.goalsAwayTeam);
		float bestValue = Float.NEGATIVE_INFINITY;

		for (Line i : map.get(f.fixture).goalLines.getArrayLines()) {
			float valueHome = 0;
			int homeSize = 0;
			for (int s : distributionHome)
				homeSize += s;

			for (int goals = 0; goals < distributionHome.length; goals++) {
				full.result = new Result(goals, 0);
				valueHome += distributionHome[goals]
						* new FullEntry(full, f.prediction, new Result(goals, 0), f.threshold, f.lower, f.upper, i)
								.getProfit();
			}

			valueHome /= homeSize;

			float valueAway = 0;
			int awaySize = 0;
			for (int s : distributionAway)
				awaySize += s;

			for (int goals = 0; goals < distributionAway.length; goals++) {
				full.result = new Result(goals, 0);
				valueAway += distributionAway[goals]
						* new FullEntry(full, f.prediction, new Result(goals, 0), f.threshold, f.lower, f.upper, i)
								.getProfit();
			}

			valueAway /= awaySize;

			float finalValue = (valueAway + valueHome) / 2;

			if (finalValue > bestValue) {
				bestValue = finalValue;
				best.line = i;
			}
		}

		best.result = originalResult;
		best.fixture.result = originalResult;
		return best;

	}

	private static int[] getGoalDistribution(ExtendedFixture fixture, ArrayList<ExtendedFixture> all, String team) {
		int[] distribution = new int[20];
		for (ExtendedFixture i : all) {
			if ((i.homeTeam.equals(team) || i.awayTeam.equals(team)) && i.date.before(fixture.date)) {
				int totalGoals = i.getTotalGoals();
				distribution[totalGoals]++;
			}
		}
		return distribution;

	}

	// offset of main line
	public static ArrayList<FinalEntry> customGoalLine(ArrayList<FinalEntry> finals,
			HashMap<ExtendedFixture, FullFixture> map, float offset) {
		ArrayList<FinalEntry> fulls = new ArrayList<>();

		for (FinalEntry f : finals) {
			boolean isOver = f.prediction > f.threshold;
			FullEntry full = new FullEntry(f.fixture, f.prediction, f.result, f.threshold, f.lower, f.upper, null);
			if (offset == 0.25f) {
				if (isOver)
					full.line = map.get(f.fixture).goalLines.line2;
				else
					full.line = map.get(f.fixture).goalLines.line3;
			} else if (offset == 0.5f) {
				if (isOver)
					full.line = map.get(f.fixture).goalLines.line1;
				else
					full.line = map.get(f.fixture).goalLines.line4;
			} else if (offset == -0.25f) {
				if (isOver)
					full.line = map.get(f.fixture).goalLines.line3;
				else
					full.line = map.get(f.fixture).goalLines.line2;
			} else if (offset == -0.5f) {
				if (isOver)
					full.line = map.get(f.fixture).goalLines.line4;
				else
					full.line = map.get(f.fixture).goalLines.line1;
			}

			fulls.add(full);
		}

		return fulls;
	}

	public static ArrayList<String> getTeamsList(ArrayList<ExtendedFixture> odds) {
		ArrayList<String> result = new ArrayList<>();
		for (ExtendedFixture i : odds) {
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
	public static ArrayList<ExtendedFixture> getFixturesList(String team, ArrayList<ExtendedFixture> fixtures) {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		for (ExtendedFixture i : fixtures)
			if (i.homeTeam.equals(team) || i.awayTeam.equals(team))
				result.add(i);

		return result;
	}

	/**
	 * Method for verifying two list of fixtures are the same (only difference
	 * in naming the clubs)
	 * 
	 * @param fixtures
	 * @param fwa
	 * @return
	 */
	public static boolean matchesFixtureLists(ArrayList<ExtendedFixture> fixtures, ArrayList<ExtendedFixture> fwa) {

		for (ExtendedFixture i : fixtures) {
			boolean foundMatch = false;
			for (ExtendedFixture j : fwa) {
				if ((i.date.equals(i.date) || i.date.equals(Utils.getYesterday(i.date))
						|| i.date.equals(Utils.getTommorow(i.date))) && i.result.equals(j.result)) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				return false;

		}

		return true;
	}

}
