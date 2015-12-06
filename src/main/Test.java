package main;

import settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONException;

import algorithms.Algorithm;
import algorithms.Basic1;
import dictionaries.EN;
import utils.Api;
import utils.Utils;
import xls.XlSUtils;

public class Test {

	public static void main(String[] args) throws JSONException, IOException, ParseException {
		long start = System.currentTimeMillis();

		// SQLiteJDBC.update(2015);
		// //
		// ArrayList<Fixture> fixtures = Api.findFixtures(1);
		// //
		// ArrayList<Entry> entries = new ArrayList<>();
		// for (Fixture f : fixtures) {
		// Algorithm alg = new Basic1(f);
		// entries.add(new Entry(f, alg.calculate(),
		// alg.getClass().getSimpleName()));
		// }
		// entries.sort(new Comparator<Entry>() {
		// @Override
		// public int compare(Entry o1, Entry o2) {
		// int fc =
		// o1.fixture.links_competition.compareTo(o2.fixture.links_competition);
		// return fc != 0 ? fc : o1.compareTo(o2);
		// }
		// });
		// // Collections.sort(entries, Collections.reverseOrder());
		// System.out.println(entries);

//		float total = 0f;
//		try {
//			for (int year = 2014; year <= 2014; year++)
//				total += simulation(year);
//		} catch (InterruptedException | ExecutionException | IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Avg profit is " + (total / 10));

		 makePredictions();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");

	}

	public static float simulation(int year) throws InterruptedException, ExecutionException, IOException {
		FileInputStream file = new FileInputStream(new File(
				"C:\\Users\\Tereza\\workspace\\Soccer\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(1);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			threadArray.add(pool.submit(new Runner(sh, year)));
		}

		for (Future<Float> fd : threadArray) {
			totalProfit += fd.get();
			// System.out.println("Total profit: " + String.format("%.2f",
			// totalProfit));
		}
		System.out.println("Total profit for season " + year + " is " + String.format("%.2f", totalProfit));
		workbook.close();
		file.close();
		return totalProfit;
	}

	public static void findSettings(int year) throws IOException, ParseException {
		FileInputStream file = new FileInputStream(new File(
				"C:\\Users\\Admin\\workspace\\Soccer\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			Settings sett = /*
							 * XlSUtils.runForLeagueWithOdds(sh,
							 * xls.XlSUtils.selectAll(sh), 1.0d);
							 */

			XlSUtils.findInterval(XlSUtils.selectAll(sh), sh, year);
			Settings stored = SQLiteJDBC.getSettings(sett.league, year);

			if (stored == null) {
				SQLiteJDBC.storeSettings(sett, year);
				System.out.println(sett);
				totalProfit += sett.profit;
			} else if (stored.profit >= sett.profit) {
				System.out.println(stored);
				totalProfit += stored.profit;
			} else {
				System.out.println(sett);
				SQLiteJDBC.deleteSettings(sett.league, year);
				SQLiteJDBC.storeSettings(sett, year);
				System.out.println(sett);
				totalProfit += sett.profit;
			}
		}
		System.out.println("TotalProfit: " + totalProfit);
		workbook.close();
	}

	public static void makePredictions() throws IOException, ParseException {
		String basePath = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File(basePath + "\\data\\fixtures.xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<Fixture> fixtures = XlSUtils.selectForPrediction(sheet);

		FileInputStream filedata = new FileInputStream(new File(basePath + "\\data\\all-euro-data-2015-2016.xls"));
		HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

		HashMap<String, Settings> optimal = new HashMap<>();
		Iterator<Sheet> sh = workbookdata.sheetIterator();
		while (sh.hasNext()) {
			HSSFSheet i = (HSSFSheet) sh.next();
			optimal.put(i.getSheetName(), XlSUtils.predictionSettings(i, 2015));
		}

		for (Fixture f : fixtures) {
			HSSFSheet league = workbookdata.getSheet(f.links_competition);
			XlSUtils.makePrediction(sheet, league, f, optimal.get(league.getSheetName()));
		}
		workbook.close();
		workbookdata.close();
	}

	public static void runForSeason(int year) {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (int i = 2013; i < 2014; i++) {
			// result = SQLiteJDBC.select(i);
			for (Fixture f : SQLiteJDBC.select(i)) {
				if (f.links_competition.equals("CL")) {
					float finalScore = poisson(f, i) * 0.1f + 0.9f * basic2(f, i, 0.6f, 0.3f, 0.1f);

					finals.add(new FinalEntry(f, finalScore, "Basic1",
							new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
				}
			}
		}

		printSuccessRate(finals, "finals50");

		ArrayList<FinalEntry> finals55 = new ArrayList<>();
		ArrayList<FinalEntry> finals60 = new ArrayList<>();
		ArrayList<FinalEntry> finals65 = new ArrayList<>();
		ArrayList<FinalEntry> finals70 = new ArrayList<>();
		ArrayList<FinalEntry> finals75 = new ArrayList<>();
		ArrayList<FinalEntry> finals80 = new ArrayList<>();
		for (FinalEntry fe : finals) {
			if (fe.prediction >= 0.80d || fe.prediction <= 0.20d)
				finals80.add(fe);
			if (fe.prediction >= 0.75d || fe.prediction <= 0.25d)
				finals75.add(fe);
			if (fe.prediction >= 0.70d || fe.prediction <= 0.30d)
				finals70.add(fe);
			if (fe.prediction >= 0.65d || fe.prediction <= 0.35d)
				finals65.add(fe);
			if (fe.prediction >= 0.60d || fe.prediction <= 0.40d)
				finals60.add(fe);
			if (fe.prediction >= 0.55d || fe.prediction <= 0.45d)
				finals55.add(fe);
		}

		// printSuccessRate(finals55, "finals55");
		// printSuccessRate(finals60, "finals60");
		// printSuccessRate(finals65, "finals65");
		// printSuccessRate(finals70, "finals70");
		// printSuccessRate(finals75, "finals75");
		// printSuccessRate(finals80, "finals80");

		ArrayList<FinalEntry> over50 = new ArrayList<>();
		// ArrayList<FinalEntry> over55 = new ArrayList<>();
		// ArrayList<FinalEntry> over60 = new ArrayList<>();
		// ArrayList<FinalEntry> over65 = new ArrayList<>();
		// ArrayList<FinalEntry> over70 = new ArrayList<>();
		// ArrayList<FinalEntry> over75 = new ArrayList<>();
		// ArrayList<FinalEntry> over80 = new ArrayList<>();
		for (FinalEntry fe : finals) {
			// if (fe.prediction >= 0.80d)
			// over80.add(fe);
			// if (fe.prediction >= 0.75d)
			// over75.add(fe);
			// if (fe.prediction >= 0.70d)
			// over70.add(fe);
			// if (fe.prediction >= 0.65d)
			// over65.add(fe);
			// if (fe.prediction >= 0.60d)
			// over60.add(fe);
			// if (fe.prediction >= 0.55d)
			// over55.add(fe);
			if (fe.prediction >= 0.55d)
				over50.add(fe);
		}

		printSuccessRate(over50, "over50");
		// printSuccessRate(over55, "over55");
		// printSuccessRate(over60, "over60");
		// printSuccessRate(over65, "over65");
		// printSuccessRate(over70, "over70");
		// printSuccessRate(over75, "over75");
		// printSuccessRate(over80, "over80");

		ArrayList<FinalEntry> under50 = new ArrayList<>();
		// ArrayList<FinalEntry> under45 = new ArrayList<>();
		// ArrayList<FinalEntry> under40 = new ArrayList<>();
		// ArrayList<FinalEntry> under35 = new ArrayList<>();
		// ArrayList<FinalEntry> under30 = new ArrayList<>();
		// ArrayList<FinalEntry> under25 = new ArrayList<>();
		// ArrayList<FinalEntry> under20 = new ArrayList<>();
		for (FinalEntry fe : finals) {
			// if (fe.prediction <= 0.20d)
			// under20.add(fe);
			// if (fe.prediction <= 0.25d)
			// under25.add(fe);
			// if (fe.prediction <= 0.30d)
			// under30.add(fe);
			// if (fe.prediction <= 0.35d)
			// under35.add(fe);
			// if (fe.prediction <= 0.40d)
			// under40.add(fe);
			// if (fe.prediction <= 0.45d)
			// under45.add(fe);
			if (fe.prediction < 0.45d)
				under50.add(fe);
		}

		printSuccessRate(under50, "under50");
		// printSuccessRate(under45, "under45");
		// printSuccessRate(under40, "under40");
		// printSuccessRate(under35, "under35");
		// printSuccessRate(under30, "under30");
		// printSuccessRate(under25, "under25");
		// printSuccessRate(under20, "under20");
	}

	public static void runForSeasonWithOdds(HSSFSheet sheet, int year) throws IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (int i = 2014; i < 2015; i++) {
			for (Fixture f : SQLiteJDBC.select(i)) {
				if (f.links_competition.equals("PL")) {
					float finalScore = poisson(f, i) * 0.25f + 0.75f * basic2(f, i, 0.6f, 0.3f, 0.1f);

					float gain = finalScore > 0.55d
							? XlSUtils.getOverOdds(sheet, null, EN.getAlias(f.homeTeamName),
									EN.getAlias(f.awayTeamName))
							: XlSUtils.getUnderOdds(sheet, null, EN.getAlias(f.homeTeamName),
									EN.getAlias(f.awayTeamName));
					// if (gain >= 1.7d)
					finals.add(new FinalEntry(f, finalScore, "Basic1",
							new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
				}
			}
		}

		printSucceRateWithOdds(sheet, finals);
	}

	public static void printSucceRateWithOdds(HSSFSheet sheet, ArrayList<FinalEntry> list) {
		int successOver50 = 0, failureOver50 = 0;
		for (FinalEntry fe : list) {
			if (fe.success())
				successOver50++;
			else
				failureOver50++;
		}
		System.out.println("success" + ": " + successOver50 + "failure" + ": " + failureOver50);
		System.out.println("Rate" + ": " + String.format("%.2f", ((float) successOver50 / list.size()) * 100));
		float profit = 0.0f;
		for (FinalEntry fe : list) {
			if (fe.success()) {
				float gain = fe.prediction > 0.55d
						? XlSUtils.getOverOdds(sheet, null, EN.getAlias(fe.fixture.homeTeamName),
								EN.getAlias(fe.fixture.awayTeamName))
						: XlSUtils.getUnderOdds(sheet, null, EN.getAlias(fe.fixture.homeTeamName),
								EN.getAlias(fe.fixture.awayTeamName));
				profit += gain;
				System.out.println(EN.getAlias(fe.fixture.homeTeamName) + " : " + EN.getAlias(fe.fixture.awayTeamName)
						+ " " + fe.result.goalsHomeTeam + "-" + fe.result.goalsAwayTeam + " " + gain
						+ (fe.prediction > 0.55 ? " over" : " udner"));
			}
		}
		System.out.println("Profit" + ": " + String.format("%.2f", profit - list.size()));
		System.out.println("AVG win odds: " + String.format("%.2f", profit / successOver50));

	}

	public static void runForSeasonXYZ(int year) {
		for (int x = 0; x <= 20; x++) {
			int w = 20 - x;
			for (int y = 0; y <= w; y++) {
				int z = w - y;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 2014; i < 2015; i++) {
					for (Fixture f : SQLiteJDBC.select(i)) {
						float finalScore = basic2(f, i, x * 0.05f, y * 0.05f, z * 0.05f);

						finals.add(new FinalEntry(f, finalScore, "Basic1",
								new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
					}
				}

				System.out.println("-----------------------");
				System.out.println("For " + x + " " + y + " " + z);
				printSuccessRate(finals, "finals50");

				ArrayList<FinalEntry> over50 = new ArrayList<>();
				for (FinalEntry fe : finals)
					if (fe.prediction >= 0.50d)
						over50.add(fe);
				printSuccessRate(over50, "over50");
				ArrayList<FinalEntry> under50 = new ArrayList<>();
				for (FinalEntry fe : finals)
					if (fe.prediction <= 0.50d)
						under50.add(fe);
				printSuccessRate(under50, "under50");
			}
		}
	}

	public static void runForSeasonXY(int year) {
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 2014; i < 2015; i++) {
				for (Fixture f : SQLiteJDBC.select(i)) {
					if (f.links_competition.equals("PL")) {
						float finalScore = x * 0.05f * basic2(f, i, 0.6f, 0.3f, 0.1f) + y * 0.05f * poisson(f, i);

						finals.add(new FinalEntry(f, finalScore, "Basic1",
								new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
					}
				}
			}

			System.out.println("-----------------------");
			System.out.println("For " + x + " " + y);
			printSuccessRate(finals, "finals50");

			ArrayList<FinalEntry> over50 = new ArrayList<>();
			for (FinalEntry fe : finals)
				if (fe.prediction >= 0.55d)
					over50.add(fe);
			printSuccessRate(over50, "over50");
			ArrayList<FinalEntry> under50 = new ArrayList<>();
			for (FinalEntry fe : finals)
				if (fe.prediction <= 0.55d)
					under50.add(fe);
			printSuccessRate(under50, "under50");
		}
	}

	public static void runByCompetitionXY(int year) {
		// map float is basic %
		HashMap<String, Float> best = new HashMap<>();

		for (String league : SQLiteJDBC.getLeagues(2013)) {
			float bestPercent = 0;
			float bestValue = 0;
			for (int x = 0; x <= 20; x++) {
				int y = 20 - x;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 2013; i < 2014; i++) {
					for (Fixture f : SQLiteJDBC.select(i)) {
						if (f.links_competition.equals(league)) {
							float finalScore = x * 0.05f * basic2(f, i, 0.6f, 0.3f, 0.1f)
									+ y * 0.05f * poissonWeighted(f, i);

							finals.add(new FinalEntry(f, finalScore, "Basic1",
									new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
						}
					}
				}

				float current = Utils.getSuccessRate(finals);
				if (current > bestPercent) {
					bestPercent = current;
					bestValue = x;
				}

			}

			System.out.println(league + " basic*" + bestValue * 0.05d + " poisson*" + (20 - bestValue) * 0.05d + " = "
					+ bestPercent);
			best.put(league, bestValue);
		}
	}

	public static void printSuccessRate(ArrayList<FinalEntry> list, String listName) {
		int successOver50 = 0, failureOver50 = 0;
		for (FinalEntry fe : list) {
			if (fe.success())
				successOver50++;
			else
				failureOver50++;
		}
		System.out.println("success" + listName + ": " + successOver50 + "failure" + listName + ": " + failureOver50);
		System.out
				.println("Rate" + listName + ": " + String.format("%.2f", ((float) successOver50 / list.size()) * 100));
		System.out.println("Profit" + listName + ": " + String.format("%.2f", successOver50 * 0.9 - failureOver50));
	}

	public static float basic1(Fixture f) {
		ArrayList<Fixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeamName, 5, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeamName, 5, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeamName, 5, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeamName, 5, 2014, f.matchday,
				f.links_competition);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return 0.4f * allGamesAVG + 0.4f * homeAwayAVG + 0.2f * BTSAVG;
	}

	public static float last10only(Fixture f, int n) {
		ArrayList<Fixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeamName, n, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeamName, n, 2014, f.matchday,
				f.links_competition);

		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		return allGamesAVG;
	}

	public static float last5HAonly(Fixture f) {
		ArrayList<Fixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeamName, 5, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeamName, 5, 2014, f.matchday,
				f.links_competition);

		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		return homeAwayAVG;
	}

	public static float last10BTSonly(Fixture f) {
		ArrayList<Fixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeamName, 10, 2014, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeamName, 10, 2014, f.matchday,
				f.links_competition);

		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;
		return BTSAVG;
	}

	public static float basic2(Fixture f, int year, float d, float e, float z) {
		ArrayList<Fixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeamName, 10, year, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeamName, 10, year, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeamName, 5, year, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeamName, 5, year, f.matchday,
				f.links_competition);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG + z * BTSAVG;
	}

	public static float poisson(Fixture f, int year) {
		ArrayList<Fixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeamName, 10, year, f.matchday,
				f.links_competition);
		ArrayList<Fixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeamName, 10, year, f.matchday,
				f.links_competition);
		float lambda = Utils.avgFor(f.homeTeamName, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeamName, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(Fixture f, int year) {
		float leagueAvgHome = SQLiteJDBC.selectAvgLeagueHome(f.links_competition, year, f.matchday);
		float leagueAvgAway = SQLiteJDBC.selectAvgLeagueAway(f.links_competition, year, f.matchday);
		float homeAvgFor = SQLiteJDBC.selectAvgHomeTeamFor(f.links_competition, f.homeTeamName, year, f.matchday);
		float homeAvgAgainst = SQLiteJDBC.selectAvgHomeTeamAgainst(f.links_competition, f.homeTeamName, year,
				f.matchday);
		float awayAvgFor = SQLiteJDBC.selectAvgAwayTeamFor(f.links_competition, f.awayTeamName, year, f.matchday);
		float awayAvgAgainst = SQLiteJDBC.selectAvgAwayTeamAgainst(f.links_competition, f.awayTeamName, year,
				f.matchday);

		float lambda = homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = awayAvgFor * homeAvgAgainst / leagueAvgHome;
		return Utils.poissonOver(lambda, mu);
	}

}
