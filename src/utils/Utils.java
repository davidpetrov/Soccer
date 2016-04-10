package utils;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entries.FinalEntry;
import main.ExtendedFixture;
import main.Result;
import results.Results;
import settings.Settings;
import tables.Position;
import tables.Table;
import xls.XlSUtils;

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

	public static float getProfit(ArrayList<FinalEntry> finals, Settings set, String type) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			fe.threshold = set.threshold;
			fe.lower = set.lowerBound;
			fe.upper = set.upperBound;
			fe.value = set.value;
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > set.value) {
				if (type.equals("unders")) {
					if (fe.prediction <= fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else if (type.equals("overs")) {
					if (fe.prediction > fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else {
					size++;
					if (fe.success()) {
						if (gain != -1.0d) {
							profit += gain;
						}
					}
				}
			}
		}
		return profit - size;
	}

	public static float getProfit(ArrayList<FinalEntry> finals, String type) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > fe.value) {
				if (type.equals("unders")) {
					if (fe.prediction <= fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else if (type.equals("overs")) {
					if (fe.prediction > fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else {
					size++;
					if (fe.success()) {
						if (gain != -1.0d) {
							profit += gain;
						}
					}
				}
			}
		}
		return profit - size;
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
							Float certainty1 = o1.prediction > o1.threshold ? o1.prediction : (1f - o1.prediction);
							Float certainty2 = o2.prediction > o2.threshold ? o2.prediction : (1f - o2.prediction);
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
							} else {
								coeff = -1f;
							}
						}
						System.out.println(curr.get(0).fixture.date + " " + " " + successes + " not loss: " + notlosses
								+ " pr: " + (successes * 1.35f - 10 + (notlosses - successes)));
					}
					curr = new ArrayList<>();
				} else {
					break;
				}
			}
		}

	}

	public static void analysys(ArrayList<FinalEntry> all, int year) {
		ArrayList<FinalEntry> overs = new ArrayList<>();
		ArrayList<FinalEntry> unders = new ArrayList<>();

		// System.out.println(all);
		for (FinalEntry fe : all) {
			// float gain = fe.prediction > fe.upper ? fe.fixture.maxOver :
			// fe.fixture.maxUnder;
			// float certainty = fe.prediction > fe.threshold ? fe.prediction :
			// (1f - fe.prediction);
			// float value = certainty * gain;
			// if (value > fe.value)
			if (fe.prediction >= fe.upper)
				overs.add(fe);
			else
				unders.add(fe);
		}
		System.err.println(year);
		System.out.println(overs.size() + " overs with rate: " + Utils.getSuccessRate(overs) + " profit: "
				+ Utils.getProfit(overs, "overs"));
		System.out.println(unders.size() + " unders with rate: " + Utils.getSuccessRate(unders) + " profit: "
				+ Utils.getProfit(unders, "unders"));

		ArrayList<FinalEntry> cot15 = new ArrayList<>();
		ArrayList<FinalEntry> cot20 = new ArrayList<>();
		ArrayList<FinalEntry> cot25 = new ArrayList<>();
		ArrayList<FinalEntry> cer80 = new ArrayList<>();
		ArrayList<FinalEntry> cer70 = new ArrayList<>();
		ArrayList<FinalEntry> cer60 = new ArrayList<>();
		ArrayList<FinalEntry> cer50 = new ArrayList<>();
		ArrayList<FinalEntry> cer40 = new ArrayList<>();
		for (FinalEntry fe : all) {
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
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
			}

		}

		System.out.println(cer80.size() + " 80s with rate: " + Utils.getSuccessRate(cer80) + "profit: "
				+ Utils.getProfit(cer80, "all"));
		System.out.println(cer70.size() + " 70s with rate: " + Utils.getSuccessRate(cer70) + "profit: "
				+ Utils.getProfit(cer70, "all"));
		System.out.println(cer60.size() + " 60s with rate: " + Utils.getSuccessRate(cer60) + "profit: "
				+ Utils.getProfit(cer60, "all"));
		System.out.println(cer50.size() + " 50s with rate: " + Utils.getSuccessRate(cer50) + "profit: "
				+ Utils.getProfit(cer50, "all"));
		System.out.println(cer40.size() + " under50s with rate: " + Utils.getSuccessRate(cer40) + " profit: "
				+ Utils.getProfit(cer40, "all"));

		System.out.println(cot25.size() + " cot25s with rate: " + Utils.getSuccessRate(cot25) + "profit: "
				+ Utils.getProfit(cot25, "all"));
		System.out.println(cot20.size() + " cot20s with rate: " + Utils.getSuccessRate(cot20) + "profit: "
				+ Utils.getProfit(cot20, "all"));
		System.out.println(cot15.size() + " cot15s with rate: " + Utils.getSuccessRate(cot15) + "profit: "
				+ Utils.getProfit(cot15, "all"));

		int onlyOvers = 0;
		float onlyOversProfit = 0f;
		for (FinalEntry fe : all) {
			if (fe.fixture.getTotalGoals() > 2.5) {
				onlyOvers++;
				onlyOversProfit += fe.fixture.maxOver;
			}
		}

		System.out.println(
				"Only overs: " + (float) onlyOvers / all.size() + " profit: " + (onlyOversProfit - all.size()));

		int onlyUnders = 0;
		float onlyUndersProfit = 0f;
		for (FinalEntry fe : all) {
			if (fe.fixture.getTotalGoals() < 2.5) {
				onlyUnders++;
				onlyUndersProfit += fe.fixture.maxUnder;
			}
		}

		System.out.println(
				"Only unders: " + (float) onlyUnders / all.size() + " profit: " + (onlyUndersProfit - all.size()));

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

		System.out.println("Better odds choice: " + (float) betterOdds / all.size() + " profit: "
				+ (betterOddsProfit - all.size()));

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

		System.out.println("Soft lines wins: " + (float) wins / certs + "draws: " + (float) draws / certs
				+ " not losses: " + (float) (wins + draws) / certs);
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
				// betSize = bank * percent;
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

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings set, float currentValue,
			String type) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			fe.threshold = set.threshold;
			fe.lower = set.lowerBound;
			fe.upper = set.upperBound;
			float gain = fe.prediction > fe.threshold ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > currentValue) {
				if (type.equals("unders")) {
					if (fe.prediction <= fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else if (type.equals("overs")) {
					if (fe.prediction > fe.threshold) {
						size++;
						if (fe.success()) {
							if (gain != -1.0d) {
								profit += gain;
							}
						}
					}
				} else {
					size++;
					if (fe.success()) {
						if (gain != -1.0d) {
							profit += gain;
						}
					}
				}
			}
		}
		return profit - size;
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
			if (i.prediction <= i.threshold)
				result.add(i);
		}
		return result;
	}

	public static ArrayList<FinalEntry> onlyOvers(ArrayList<FinalEntry> finals) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			if (i.prediction > i.threshold)
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

	public static ArrayList<FinalEntry> shotsRestrict(ArrayList<FinalEntry> finals, HSSFSheet sheet) {
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

		float bestProfit = getProfit(finals, type);
		int bestLow = 0;
		int bestHigh = 23;

		for (int i = 1; i < 11; i++) {
			ArrayList<FinalEntry> diffPos = positionRestrict(finals, table, i, 23, type);

			float curr = Utils.getProfit(diffPos, type);
			if (curr > bestProfit) {
				bestProfit = curr;
				bestLow = i;
			}

		}

		for (int i = bestLow; i < 23; i++) {
			ArrayList<FinalEntry> diffPos = positionRestrict(finals, table, bestLow, i, type);

			float curr = Utils.getProfit(diffPos, type);
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

	public static ArrayList<FinalEntry> similarityRestrict(HSSFSheet sheet, ArrayList<FinalEntry> finals, Table table) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry i : finals) {
			float basicSimilar = Utils.basicSimilar(i, sheet, table);
			if (i.prediction >= i.upper && basicSimilar >= i.threshold)
				result.add(i);
			else if (i.prediction <= i.lower && basicSimilar <= i.threshold)
				result.add(i);
//			else
//				System.out.println(i + " " + i.prediction + " " + basicSimilar);
		}

		return result;
	}

	private static float basicSimilar(FinalEntry i, HSSFSheet sheet, Table table) {
		ArrayList<String> filterHome = table.getSimilarTeams(i.fixture.awayTeam);
		ArrayList<String> filterAway = table.getSimilarTeams(i.fixture.homeTeam);
		ExtendedFixture f = i.fixture;

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

}
