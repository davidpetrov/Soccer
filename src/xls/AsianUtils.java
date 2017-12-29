package xls;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import entries.AsianEntry;
import main.Fixture;
import main.SQLiteJDBC;
import settings.Settings;
import settings.SettingsAsian;
import utils.FixtureUtils;
import utils.Lines;
import utils.Pair;
import utils.Utils;

public class AsianUtils {

	public static Pair poissonAsianHome(Fixture f, HSSFSheet sheet) throws ParseException {

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

	public static Pair poissonAsianLine(Fixture f, HSSFSheet sheet, float line, float home, float away)
			throws ParseException {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonAsianHome(lambda, mu, line, home, away);
	}

	public static Pair basic(Fixture f, HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 50, f.date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 50, f.date);

		float home = AsianUtils.beatTheLine(f, f.homeTeam, lastHomeHomeTeam, f.line);
		float away = AsianUtils.beatTheLine(f, f.awayTeam, lastAwayAwayTeam, f.line);

		return Pair.of(home, away);
	}

	public static Pair estimate(Fixture f, HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 50, f.date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 50, f.date);

		float home = AsianUtils.estimateTheLine(f, f.homeTeam, lastHomeHomeTeam, f.line, true);
		float away = AsianUtils.estimateTheLine(f, f.awayTeam, lastAwayAwayTeam, f.line, false);

		return Pair.of(home, away);
	}

	public static Pair estimateBoth(Fixture f, HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date);

		float home = AsianUtils.estimateTheLineBoth(f, f.homeTeam, lastHomeHomeTeam, f.line, true);
		float away = AsianUtils.estimateTheLineBoth(f, f.awayTeam, lastAwayAwayTeam, f.line, false);

		return Pair.of(home, away);
	}

	private static float estimateTheLineBoth(Fixture f, String team, ArrayList<Fixture> lastHomeTeam,
			float line, boolean home) {
		if (lastHomeTeam.size() == 0)
			return 0;
		ArrayList<String> results = new ArrayList<>();
		for (Fixture i : lastHomeTeam) {
			boolean prediction = i.homeTeam.equals(team);
			AsianEntry ae = new AsianEntry(i, prediction, i.line, i.asianHome, i.asianAway, 0f);
			results.add(ae.success());
		}

		float coeff = f.homeTeam.equals(team) ? f.asianHome : f.asianAway;
		return outcomes(results, coeff);
	}

	private static float estimateTheLine(Fixture f, String team, ArrayList<Fixture> lastHomeTeam,
			float line, boolean home) {
		if (lastHomeTeam.size() == 0)
			return 0;
		ArrayList<String> results = new ArrayList<>();
		for (Fixture i : lastHomeTeam) {
			boolean prediction = home;
			AsianEntry ae = new AsianEntry(i, prediction, i.line, i.asianHome, i.asianAway, 0f);
			results.add(ae.success());
		}

		float coeff = f.homeTeam.equals(team) ? f.asianHome : f.asianAway;
		return outcomes(results, coeff);
	}

	public static Pair shotsHomeWin(Fixture f, HSSFSheet sheet) throws ParseException {
		// float avgTotal = selectAvgShotsTotal(sheet, f.date);

		float avgHome = XlSUtils.selectAvgShotsHome(sheet, f.date);
		float avgAway = XlSUtils.selectAvgShotsAway(sheet, f.date);
		float homeShotsFor = XlSUtils.selectAvgHomeShotsFor(sheet, f.homeTeam, f.date);
		float homeShotsAgainst = XlSUtils.selectAvgHomeShotsAgainst(sheet, f.homeTeam, f.date);
		float awayShotsFor = XlSUtils.selectAvgAwayShotsFor(sheet, f.awayTeam, f.date);
		float awayShotsAgainst = XlSUtils.selectAvgAwayShotsAgainst(sheet, f.awayTeam, f.date);

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		float diff = mu - lambda;

		// float homeAvgFor = selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		// float awayAvgFor = selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);

		// float homeRatio = homeAvgFor / homeShotsFor;
		// float awayRatio = awayAvgFor / awayShotsFor;

		// return Utils.poissonOver(homeRatio * lambda, awayRatio * mu);
		// float avgShotsUnder = AvgShotsWhenUnder(sheet, f.date);
		float avgDiff = avgShotsDiffHomeWin(sheet, f.date);

		float ratio = diff / avgDiff;

		if (ratio < 0f) {
			return Pair.of(f.asianHome, 0f);
		} else if (ratio >= 2f) {
			return Pair.of(0f, f.asianAway);
		} else {
			return Pair.of(f.asianHome * (1 - ratio / 2), f.asianAway * ratio / 2);
		}

		// if (diff >= avgDiff)
		// return Pair.of(f.asianHome, 0f);
		// else
		// return Pair.of(0f, f.asianAway);

	}

	private static float avgShotsDiffHomeWin(HSSFSheet sheet, Date date) throws ParseException {
		int totalHome = 0;
		int totalAway = 0;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(XlSUtils.getColumnIndex(sheet, "Date"));
			if (row.getCell(XlSUtils.getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(XlSUtils.getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(XlSUtils.getColumnIndex(sheet, "Date")).getDateCellValue();
			int homegoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awaygoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTAG")).getNumericCellValue();
			if (row.getCell(XlSUtils.getColumnIndex(sheet, "HST")) != null
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")) != null && dateCell != null
					&& fdate.before(date) && row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getCellType() == 0 && homegoal < awaygoal) {
				totalHome += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getNumericCellValue();
				totalAway += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getNumericCellValue();

				count++;
			}
		}
		return count == 0 ? 0 : (float) (totalAway - totalHome) / count;
	}

	// needs to be checked
	private static float getShotDiff(HSSFSheet sheet, Date date, Fixture ae) {
		int totalHome = 0;
		int totalAway = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(XlSUtils.getColumnIndex(sheet, "Date"));
			float line = (float) row.getCell(XlSUtils.getColumnIndex(sheet, "BbAHh")).getNumericCellValue();
			if (row.getCell(XlSUtils.getColumnIndex(sheet, "HST")) != null
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")) != null && dateCell != null
					&& dateCell.getDateCellValue().before(date) && line == ae.line) {

				totalHome += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getNumericCellValue();
				totalAway += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getNumericCellValue();

				count++;
			}
		}
		return count == 0 ? 0 : (totalHome - totalAway) / count;
	}

	private static float beatTheLine(Fixture f, String team, ArrayList<Fixture> lastHomeTeam,
			float line) {
		if (lastHomeTeam.size() == 0)
			return 0;
		ArrayList<String> results = new ArrayList<>();
		for (Fixture i : lastHomeTeam) {
			boolean prediction = i.homeTeam.equals(team);
			AsianEntry ae = new AsianEntry(i, prediction, line, f.asianHome, f.asianAway, 0f);
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
		/* + ((float) draws / results.size()) */ - ((float) halflosses / results.size()) / 2
				- ((float) losses / results.size());
	}

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<AsianEntry> analysis = new ArrayList<>();
		ArrayList<Fixture> all = XlSUtils.selectAll(sheet, 1);
		// all = onlyHome(all);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			// Utils.fairValue(current);
			// ArrayList<Fixture> data = Utils.getBeforeMatchday(all,
			// i);

			// SettingsAsian temp = runForLeagueWithOdds(sheet, data, year);
			// ArrayList<AsianEntry> finals = runWithSettingsList(sheet, data,
			// temp);
			// current = onlyHome(current);
			// data = onlyHome(data);

			ArrayList<AsianEntry> bets = new ArrayList<>();
			// for (Fixture f : data) {
			// AsianEntry ae = better(f, shotsHomeWin(f, sheet), f.line,
			// f.asianHome, f.asianAway);
			// if (ae.prediction)
			// bets.add(ae);
			// }
			//
			// float bestExp = bestExpectancy(finals);
			//
			// bets = new ArrayList<>();
			for (Fixture f : current) {
				if (f.asianAway <= 1f && f.asianHome <= 1f)
					continue;

				AsianEntry ae = worse(f, estimateBoth(f, sheet), f.line, f.asianHome, f.asianAway);
				// AsianEntry bas = better(f, basic(f, sheet), f.line,
				// f.asianHome, f.asianAway);
				// AsianEntry shotEntry = better(f, shotsHomeWin(f, sheet),
				// f.line, f.asianHome, f.asianAway);
				// AsianEntry poissonEntry = better(f, poissonAsianLine(f,
				// sheet, f.line, f.asianHome, f.asianAway),
				// f.line, f.asianHome, f.asianAway);
				// if (!ae.prediction)
				// if (!ae.prediction)
				bets.add(ae);

			}

			// ArrayList<AsianEntry> bets = runWithSettingsList(sheet,
			// current,
			// temp);

			// bets = restrict(bets, 1f);
			// bets = homePredictions(bets);

			profit += getProfit(bets);
			System.out.println(bets);
			played += bets.size();
			analysis.addAll(bets);

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		// analysis(analysis);
		return profit;
	}

	private static ArrayList<Fixture> onlyHome(ArrayList<Fixture> all) {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture i : all) {
			if (i.line == 0.5f) {
				result.add(i);
			}
		}
		return result;
	}

	private static ArrayList<AsianEntry> homePredictions(ArrayList<AsianEntry> all) {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (AsianEntry i : all) {
			if (i.prediction) {
				result.add(i);
			}
		}
		return result;
	}

	public static float realisticAllLines(HSSFSheet sheet, int year)
			throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<AsianEntry> analysis = new ArrayList<>();
		ArrayList<Fixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 30; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);

			ArrayList<AsianEntry> finals = runAllLines(sheet, data);

			float bestExp = bestExpectancy(finals);

			ArrayList<AsianEntry> bets = runAllLines(sheet, current);

			bets = restrict(bets, bestExp);

			// System.out.println(bets);

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

	private static ArrayList<AsianEntry> runAllLines(HSSFSheet sheet, ArrayList<Fixture> data)
			throws ParseException {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (Fixture f : data) {
			// TO DO REMOVE this constraint
			if (f.asianHome < 1.76 || f.asianHome > 2.17)
				continue;
			AsianEntry best = null;
			float bestExp = Float.NEGATIVE_INFINITY;

			Lines l = SQLiteJDBC.closestLine(f);
			for (int i = 0; i < 5; i++) {
				float line = l.getLine(i, f.line);
				float home = l.getHome(i);
				float away = l.getAway(i);
				AsianEntry entry = better(f, poissonAsianLine(f, sheet, line, home, away), line, home, away);
				if (entry.expectancy > bestExp) {
					bestExp = entry.expectancy;
					best = entry;
				}
			}

			result.add(best);

		}

		return result;
	}

	static ArrayList<AsianEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<Fixture> data,
			SettingsAsian temp) throws ParseException {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (Fixture f : data) {
			AsianEntry basic = better(f, basic(f, sheet), f.line, f.asianHome, f.asianAway);
			AsianEntry poisson = better(f, poissonAsianHome(f, sheet), f.line, f.asianHome, f.asianAway);
			if (basic.prediction == poisson.prediction) {
				float finalScore = temp.basic * better(f, basic(f, sheet), f.line, f.asianHome, f.asianAway).expectancy
						+ temp.poisson
								* better(f, poissonAsianHome(f, sheet), f.line, f.asianHome, f.asianAway).expectancy;
				result.add(new AsianEntry(f, basic.prediction, f.line, f.asianHome, f.asianAway, finalScore));
			}
		}
		return result;
	}

	private static AsianEntry better(Fixture f, Pair pair, float line, float home2, float away2) {
		AsianEntry home = new AsianEntry(f, true, line, home2, away2, pair.home);
		AsianEntry away = new AsianEntry(f, false, line, home2, away2, pair.away);
		if (home.expectancy >= away.expectancy)
			return home;
		else
			return away;
	}

	private static AsianEntry worse(Fixture f, Pair pair, float line, float home2, float away2) {
		AsianEntry home = new AsianEntry(f, true, line, home2, away2, pair.home);
		AsianEntry away = new AsianEntry(f, false, line, home2, away2, pair.away);
		if (home.expectancy < away.expectancy)
			return home;
		else
			return away;
	}

	static SettingsAsian runForLeagueWithOdds(HSSFSheet sheet, ArrayList<Fixture> all, int year)
			throws ParseException {

		float bestProfit = Float.NEGATIVE_INFINITY;
		SettingsAsian best = null;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		boolean[] predictions = new boolean[all.size()];

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
			AsianEntry basic = better(f, basic(f, sheet), f.line, f.asianHome, f.asianAway);
			AsianEntry poisson = better(f, poissonAsianHome(f, sheet), f.line, f.asianHome, f.asianAway);
			if (basic.prediction == poisson.prediction) {
				basics[i] = better(f, basic(f, sheet), f.line, f.asianHome, f.asianAway).expectancy;
				poissons[i] = better(f, poissonAsianHome(f, sheet), f.line, f.asianHome, f.asianAway).expectancy;
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
				Fixture f = all.get(i);
				if (basics[i] != -1f) {
					float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];
					AsianEntry ae = new AsianEntry(f, predictions[i], f.line, f.asianHome, f.asianAway, finalScore);
					finals.add(ae);
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
				AsianEntry notLoss = new AsianEntry(i.fixture, i.prediction, 0f, i.fixture.asianHome,
						i.fixture.asianAway, 0f);
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

	static float bestExpectancy(ArrayList<AsianEntry> finals) {
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
			throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<AsianEntry> analysis = new ArrayList<>();
		ArrayList<Fixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);

			ArrayList<AsianEntry> finals = new ArrayList<>();
			ArrayList<AsianEntry> homes = new ArrayList<>();
			ArrayList<AsianEntry> aways = new ArrayList<>();
			for (Fixture f : data) {
				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, f.asianHome, f.asianAway, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, f.asianHome, f.asianAway, pair.away);
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
			for (Fixture f : current) {

				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, f.asianHome, f.asianAway, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, f.asianHome, f.asianAway, pair.away);
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

	public static ArrayList<AsianEntry> makePrediction(HSSFSheet sheet, HSSFSheet league, Fixture f,
			SettingsAsian temp) throws ParseException {
		ArrayList<Fixture> current = new ArrayList<>();
		current.add(f);
		ArrayList<AsianEntry> bets = runWithSettingsList(sheet, current, temp);

		bets = restrict(bets, temp.expectancy);
		return bets;
	}

	public static Float predictions(HSSFSheet sheet, int year)
			throws IOException, InterruptedException, ParseException {

		float profit = 0.0f;
		int played = 0;
		ArrayList<Fixture> all = XlSUtils.selectAll(sheet, 1);
		float th = 0.55f;
		Settings temp = new Settings(sheet.getSheetName(), 0f, 0f, 0f, th, th, th, 0.5f, 0f).withShots(1f);

		ArrayList<AsianEntry> pending = new ArrayList<>();

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 10; i <= maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<AsianEntry> bets = new ArrayList<>();
			for (Fixture f : current) {
				if (f.asianAway <= 1f && f.asianHome <= 1f)
					continue;

				AsianEntry ae = worse(f, estimateBoth(f, sheet), f.line, f.asianHome, f.asianAway);
				// if (!ae.prediction)
				bets.add(ae);

			}

			float trprofit = 0f;
			for (AsianEntry f : bets) {
				if (f.fixture.result.goalsHomeTeam == -1)
					pending.add(f);
				if (f.fixture.result.goalsHomeTeam != -1) {
					played++;
					trprofit += f.getProfit();
				}
			}

			profit += trprofit;

		}

		// pending.sort(new Comparator<FinalEntry>() {
		//
		// @Override
		// public int compare(FinalEntry o1, FinalEntry o2) {
		// return ((Float) o1.prediction).compareTo((Float) o2.prediction);
		// }
		// });

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));

		ArrayList<AsianEntry> todayGames = new ArrayList<>();
		for (AsianEntry p : pending) {
			 if (Utils.isToday(p.fixture.date))
			todayGames.add(p);
		}
		System.out.println(todayGames);

		return profit;

	}
}
