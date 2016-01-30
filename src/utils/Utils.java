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

import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.ExtendedFixture;
import main.FinalEntry;
import main.Result;
import settings.Settings;

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
				float gain = fe.prediction > 0.55d ? fe.fixture.maxOver : fe.fixture.maxUnder;
				if (gain != -1.0d)
					profit += gain;

			}
		}

		return profit - list.size();
	}

	public static ArrayList<FinalEntry> filterFinals(HSSFSheet sheet, ArrayList<FinalEntry> finals, float minOdds) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > 0.55d ? fe.fixture.maxOver : fe.fixture.maxUnder;
			if (gain >= minOdds)
				filtered.add(fe);
		}
		return filtered;
	}

	public static ArrayList<FinalEntry> filterMaxFinals(HSSFSheet sheet, ArrayList<FinalEntry> finals, float maxOdds) {
		ArrayList<FinalEntry> filtered = new ArrayList<>();
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > 0.55d ? fe.fixture.maxOver : fe.fixture.maxUnder;
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

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings set) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			fe.threshold = set.threshold;
			fe.lower = set.lowerBound;
			fe.upper = set.upperBound;
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > set.value) {
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

	public static float getProfit(ArrayList<FinalEntry> finals) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
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
			if (fe.prediction >= fe.upper)
				overs.add(fe);
			else
				unders.add(fe);
		}
		System.err.println(year);
		System.out.println(overs.size() + " overs with rate: " + Utils.getSuccessRate(overs) + " profit: "
				+ Utils.getProfit(overs));
		System.out.println(unders.size() + " unders with rate: " + Utils.getSuccessRate(unders) + " profit: "
				+ Utils.getProfit(unders));

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

		System.out.println(
				cer80.size() + " 80s with rate: " + Utils.getSuccessRate(cer80) + "profit: " + Utils.getProfit(cer80));
		System.out.println(
				cer70.size() + " 70s with rate: " + Utils.getSuccessRate(cer70) + "profit: " + Utils.getProfit(cer70));
		System.out.println(
				cer60.size() + " 60s with rate: " + Utils.getSuccessRate(cer60) + "profit: " + Utils.getProfit(cer60));
		System.out.println(
				cer50.size() + " 50s with rate: " + Utils.getSuccessRate(cer50) + "profit: " + Utils.getProfit(cer50));
		System.out.println(cer40.size() + " under50s with rate: " + Utils.getSuccessRate(cer40) + " profit: "
				+ Utils.getProfit(cer40));

		System.out.println(cot25.size() + " cot25s with rate: " + Utils.getSuccessRate(cot25) + "profit: "
				+ Utils.getProfit(cot25));
		System.out.println(cot20.size() + " cot20s with rate: " + Utils.getSuccessRate(cot20) + "profit: "
				+ Utils.getProfit(cot20));
		System.out.println(cot15.size() + " cot15s with rate: " + Utils.getSuccessRate(cot15) + "profit: "
				+ Utils.getProfit(cot15));

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

	public static float correlation(float[] arr1, float[] arr2) {
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

	public static float getProfit(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings set, float currentValue) {
		float profit = 0.0f;
		int size = 0;
		for (FinalEntry fe : finals) {
			fe.threshold = set.threshold;
			fe.lower = set.lowerBound;
			fe.upper = set.upperBound;
			float gain = fe.prediction > fe.upper ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > currentValue) {
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

}
