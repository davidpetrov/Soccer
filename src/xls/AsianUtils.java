package xls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import entries.AsianEntry;
import entries.FinalEntry;
import main.ExtendedFixture;
import main.Result;
import settings.Settings;
import settings.SettingsAsian;
import utils.Pair;
import utils.Utils;

public class AsianUtils {

	public static Pair poissonAsianHome(ExtendedFixture f, HSSFSheet sheet) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonAsianHome(lambda, mu, f.line, f.asianHome, f.asianAway);
	}

	public static Pair basic(ExtendedFixture f, HSSFSheet sheet) {
		ArrayList<ExtendedFixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 50, f.date);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 50, f.date);

		float home = AsianUtils.beatTheLine(f, f.homeTeam, lastHomeHomeTeam, f.line);
		float away = AsianUtils.beatTheLine(f, f.awayTeam, lastAwayAwayTeam, f.line);

		return Pair.of(home, away);
	}

	private static float beatTheLine(ExtendedFixture f, String team, ArrayList<ExtendedFixture> lastHomeTeam,
			float line) {
		if (lastHomeTeam.size() == 0)
			return 0;
		ArrayList<String> results = new ArrayList<>();
		for (ExtendedFixture i : lastHomeTeam) {
			boolean prediction = i.homeTeam.equals(team);
			AsianEntry ae = new AsianEntry(i, prediction, line, 0f);
			results.add(ae.success());
		}

		float coeff = f.homeTeam.equals(team) ? f.asianHome : f.asianAway;
		return outcomes(results, coeff);
	}

	private static float outcomes(ArrayList<String> results, float coeff) {
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
				+ ((float) draws / results.size()) - ((float) halflosses / results.size()) / 2
				- ((float) losses / results.size());
	}

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<AsianEntry> analysis = new ArrayList<>();
		ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);

			SettingsAsian temp = runForLeagueWithOdds(sheet, data, year);
			ArrayList<AsianEntry> finals = runWithSettingsList(sheet, data, temp);

			float bestExp = bestExpectancy(finals);

			ArrayList<AsianEntry> bets = runWithSettingsList(sheet, current, temp);

			bets = restrict(bets, bestExp);

			profit += getProfit(bets);
			// System.out.println("Curr: "+ profit);
			played += bets.size();
			analysis.addAll(bets);

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		// analysis(analysis);
		return profit;
	}

	private static ArrayList<AsianEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<ExtendedFixture> data,
			SettingsAsian temp) {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (ExtendedFixture f : data) {
			AsianEntry basic = better(f, basic(f, sheet));
			AsianEntry poisson = better(f, poissonAsianHome(f, sheet));
			if (basic.prediction == poisson.prediction) {
				float finalScore = temp.basic * better(f, basic(f, sheet)).expectancy
						+ temp.poisson * better(f, poissonAsianHome(f, sheet)).expectancy;
				result.add(new AsianEntry(f, basic.prediction, f.line, finalScore));
			}
		}
		return result;
	}

	private static AsianEntry better(ExtendedFixture f, Pair pair) {
		AsianEntry home = new AsianEntry(f, true, f.line, pair.home);
		AsianEntry away = new AsianEntry(f, false, f.line, pair.away);
		if (home.expectancy >= away.expectancy)
			return home;
		else
			return away;
	}

	private static SettingsAsian runForLeagueWithOdds(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year) {

		float bestProfit = Float.NEGATIVE_INFINITY;
		SettingsAsian best = null;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		boolean[] predictions = new boolean[all.size()];

		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);
			AsianEntry basic = better(f, basic(f, sheet));
			AsianEntry poisson = better(f, poissonAsianHome(f, sheet));
			if (basic.prediction == poisson.prediction) {
				basics[i] = better(f, basic(f, sheet)).expectancy;
				poissons[i] = better(f, poissonAsianHome(f, sheet)).expectancy;
				predictions[i] = basic.prediction;
			} else {
				basics[i] = -1f;
				poissons[i] = -1f;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<AsianEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				if (basics[i] != -1f) {
					float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];
					AsianEntry ae = new AsianEntry(f, predictions[i], f.line, finalScore);
				}

			}

			SettingsAsian set = new SettingsAsian(sheet.getSheetName(), year, x * 0.05f, y * 0.05f, 1f, 0f);

			float currentProfit = getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}

		}

		best.profit = bestProfit;
		return best;

	}

	public static void analysis(ArrayList<AsianEntry> analysis) {
		float home = 0f;
		float away = 0f;
		float hard = 0f;
		float half = 0f;
		float soft = 0f;
		int homeWins = 0;
		int homeSuccess = 0;
		int notLosses = 0;

		for (AsianEntry i : analysis) {
			float pr = i.getProfit();
			if (i.prediction)
				home += pr;
			else
				away += pr;

			if (i.line == -0.5f) {
				homeWins++;
				if (i.success().equals("W"))
					homeSuccess++;
				AsianEntry notLoss = new AsianEntry(i.fixture, i.prediction, 0f, 0f);
				if (notLoss.success().equals("W") || notLoss.success().equals("D"))
					notLosses++;
			}
			float fraction = Math.abs(i.line % 1);
			if (fraction == 0.5)
				hard += pr;
			else if (fraction == 0.25 || fraction == 0.75)
				half += pr;
			else
				soft += pr;
		}

		System.out.println("Home Success: " + String.format("%.2f", (float) homeSuccess / homeWins) + " not losses:"
				+ String.format("%.2f", (float) notLosses / homeWins));

		// System.out.println("home: " + String.format("%.2f", home) + " away: "
		// + String.format("%.2f", away));
		// System.out.println("hard: " + String.format("%.2f", hard) + " half: "
		// + String.format("%.2f", half) + " soft: "
		// + String.format("%.2f", soft));

	}

	private static ArrayList<AsianEntry> restrict(ArrayList<AsianEntry> bets, float f) {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (AsianEntry i : bets)
			if (i.expectancy > f)
				result.add(i);

		return result;
	}

	private static float getProfit(ArrayList<AsianEntry> bets) {
		float profit = 0f;
		for (AsianEntry i : bets)
			profit += i.getProfit();
		return profit;
	}

	private static float bestExpectancy(ArrayList<AsianEntry> finals) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float best = 0;

		for (int i = 0; i < 30; i++) {
			float current = i * 0.05f;
			float profit = 0f;
			for (AsianEntry f : finals) {
				if (f.expectancy > current)
					profit += f.getProfit();
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = current;
			}

		}
		return best;
	}

	public static ArrayList<AsianEntry> realisticFinals(HSSFSheet sheet, int year)
			throws IOException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<AsianEntry> analysis = new ArrayList<>();
		ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);

			ArrayList<AsianEntry> finals = new ArrayList<>();
			ArrayList<AsianEntry> homes = new ArrayList<>();
			ArrayList<AsianEntry> aways = new ArrayList<>();
			for (ExtendedFixture f : data) {
				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, pair.away);
				if (home.expectancy >= away.expectancy) {
					finals.add(home);
					homes.add(home);
				} else {
					finals.add(away);
					aways.add(away);
				}
			}

			float bestExp = bestExpectancy(finals);

			ArrayList<AsianEntry> bets = new ArrayList<>();
			for (ExtendedFixture f : current) {

				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, pair.away);
				if (home.expectancy >= away.expectancy)
					bets.add(home);
				else
					bets.add(away);

				// System.out.println(f + " " + home.expectancy + " to " +
				// away.expectancy + " " + f.line);
				//// System.out.println(home.expectancy+away.expectancy);
				// System.out.println(home.success() + " " + home.getProfit());
				// System.out.println(away.success() + " " + away.getProfit());
			}

			bets = restrict(bets, bestExp);

			profit += getProfit(bets);
			// System.out.println("Curr: "+ profit);
			played += bets.size();
			analysis.addAll(bets);

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		// analysis(analysis);
		return analysis;
	}
}
