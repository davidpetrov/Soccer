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
import java.util.Collections;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.ExtendedFixture;
import main.FinalEntry;
import main.Result;
import settings.Settings;
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
		return total / fixtures.size();
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

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> list) {
		float profit = 0.0f;
		for (FinalEntry fe : list) {
			if (fe.success()) {
				float gain = fe.prediction > 0.55d
						? XlSUtils.getOverOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam)
						: XlSUtils.getUnderOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam);
				if (gain != -1.0d)
					profit += gain;

			}
		}

		return profit - list.size();
	}

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> list, float minOdds) {
		float profit = 0.0f;
		for (FinalEntry fe : list) {
			if (fe.success()) {
				float gain = fe.prediction > 0.55d
						? XlSUtils.getOverOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam)
						: XlSUtils.getUnderOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam);
				if (gain >= minOdds)
					profit += gain;
			}
		}

		return profit - list.size();
	}

	public static ArrayList<FinalEntry> filterFinals(HSSFSheet sheet, ArrayList<FinalEntry> finals, float minOdds) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > 0.55d
					? XlSUtils.getOverOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam)
					: XlSUtils.getUnderOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam);
			if (gain >= minOdds)
				filtered.add(fe);
		}
		return filtered;
	}

	public static ArrayList<FinalEntry> filterMaxFinals(HSSFSheet sheet, ArrayList<FinalEntry> finals, float maxOdds) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > 0.55d
					? XlSUtils.getOverOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam)
					: XlSUtils.getUnderOdds(sheet, null, fe.fixture.homeTeam, fe.fixture.awayTeam);
			if (gain <= maxOdds)
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

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> finals,
			HashMap<ExtendedFixture, Float> underOdds, HashMap<ExtendedFixture, Float> overOdds, Settings set) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			fe.threshold = set.threshold;
			fe.lower = set.lowerBound;
			fe.upper = set.upperBound;
			float gain = fe.prediction > fe.upper ? overOdds.get(fe.fixture) : underOdds.get(fe.fixture);
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > 0.9f) {
				size++;
				if (fe.success()) {
					if (gain != -1.0d) {
						profit += gain;
					}
				}
			}
		}
		return profit - size;
	}

	public static ArrayList<ExtendedFixture> onlyFixtures(ArrayList<FinalEntry> finals) {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		for (FinalEntry fe : finals)
			result.add(fe.fixture);
		return result;
	}

}
