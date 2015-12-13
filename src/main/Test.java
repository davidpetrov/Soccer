package main;

import settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
import utils.Api;
import utils.Utils;
import xls.XlSUtils;

public class Test {

	public static void main(String[] args) throws JSONException, IOException {

		long start = System.currentTimeMillis();

		// simplePredictions();

		// float total = 0f;
		// try {
		// for (int year = 2015; year <= 2015; year++)
		// total += simulation(year);
		// } catch (InterruptedException | ExecutionException | IOException e) {
		// e.printStackTrace();
		// }
		// System.out.println("Avg profit is " + (total / 11));

//		 makePredictions();

		// stats();

		optimals();

		// optimalsbyCompetition();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");

	}

	public static void stats() throws IOException {
		for (int year = 2015; year <= 2015; year++) {
			String base = new File("").getAbsolutePath();

			FileInputStream file = new FileInputStream(
					new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet("I2");
			ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);
			System.out.println(year + " over: " + Utils.countOverGamesPercent(all) + "% AVG: " + Utils.findAvg(all));
			workbook.close();
			file.close();
		}
	}

	public static float simulation(int year) throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();

		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(6);
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
		pool.shutdown();
		return totalProfit;
	}

	public static void optimals() throws IOException {
		String basePath = new File("").getAbsolutePath();
		float totalTotal = 0f;

		for (int year = 2005; year <= 2015; year++) {
			float total = 0f;

			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				Settings set = XlSUtils.predictionSettings(i, year);
				total += set.profit;
				System.out.println(set);
			}

			System.out.println("Total profit for " + year + " is: " + total);
			totalTotal += total;
			workbookdata.close();
			filedata.close();
		}
		System.out.println("Average is:" + totalTotal / 11);
	}

	public static void optimalsbyCompetition() throws IOException {

		HashMap<String, ArrayList<Settings>> optimals = new HashMap<>();

		String basePath = new File("").getAbsolutePath();

		for (int year = 2005; year <= 2015; year++) {

			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				Settings set = XlSUtils.predictionSettings(i, year);
				if (optimals.get(i.getSheetName()) != null)
					optimals.get(i.getSheetName()).add(set);
				else {
					optimals.put(i.getSheetName(), new ArrayList<>());
					optimals.get(i.getSheetName()).add(set);
				}
				// System.out.println(set);
			}

			workbookdata.close();
			filedata.close();
		}

		// for (java.util.Map.Entry<String, ArrayList<Settings>> league :
		// optimals.entrySet()) {
		// for (Settings setts : league.getValue()) {
		// System.out.println(setts);
		// }
		// System.out.println("===============================================================");
		// }
		float totalPeriod = 0f;

		for (int year = 2006; year <= 2015; year++) {
			float total = 0f;
			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				ArrayList<Settings> setts = optimals.get(i.getSheetName());
				Settings set = Utils.getSettings(setts, year - 1);
				ArrayList<FinalEntry> fes = XlSUtils.runWithSettingsList(i, XlSUtils.selectAllAll(i), set);
				float profit = Utils.getProfit(i, fes, set);
				total += profit;
				// System.out.println("Profit with best sets for " +
				// i.getSheetName() + " : " + profit);
			}
			totalPeriod += total;
			System.out.println("Total for " + year + " : " + total);
			workbookdata.close();
			filedata.close();
		}

		System.out.println("Avg profit per year using last year best setts: " + totalPeriod / 10);

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

	public static void makePredictions() throws IOException {
		String basePath = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File(basePath + "\\data\\fixtures.xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<ExtendedFixture> fixtures = XlSUtils.selectForPrediction(sheet);

		FileInputStream filedata = new FileInputStream(new File(basePath + "\\data\\all-euro-data-2015-2016.xls"));
		HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

		HashMap<String, Settings> optimal = new HashMap<>();
		Iterator<Sheet> sh = workbookdata.sheetIterator();
		while (sh.hasNext()) {
			HSSFSheet i = (HSSFSheet) sh.next();
			optimal.put(i.getSheetName(), XlSUtils.predictionSettings(i, 2015));
		}

		for (ExtendedFixture f : fixtures) {
			HSSFSheet league = workbookdata.getSheet(f.competition);
			XlSUtils.makePrediction(sheet, league, f, optimal.get(league.getSheetName()));
		}
		workbook.close();
		workbookdata.close();
	}

	public static void runForSeasonXYZ(int year) {
		for (int x = 0; x <= 20; x++) {
			int w = 20 - x;
			for (int y = 0; y <= w; y++) {
				int z = w - y;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 2014; i < 2015; i++) {
					for (ExtendedFixture f : SQLiteJDBC.select(i)) {
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
				for (ExtendedFixture f : SQLiteJDBC.select(i)) {
					if (f.competition.equals("PL")) {
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
					for (ExtendedFixture f : SQLiteJDBC.select(i)) {
						if (f.competition.equals(league)) {
							float finalScore = x * 0.05f * basic2(f, i, 0.6f, 0.3f, 0.1f)
									+ y * 0.05f * poissonWeighted(f, i);
							System.out.println(f + " " + poissonWeighted(f, i));

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

	public static float basic1(ExtendedFixture f) {
		ArrayList<ExtendedFixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeam, 5, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeam, 5, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeam, 5, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeam, 5, 2014, f.matchday,
				f.competition);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return 0.4f * allGamesAVG + 0.4f * homeAwayAVG + 0.2f * BTSAVG;
	}

	public static float last10only(ExtendedFixture f, int n) {
		ArrayList<ExtendedFixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeam, n, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeam, n, 2014, f.matchday,
				f.competition);

		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		return allGamesAVG;
	}

	public static float last5HAonly(ExtendedFixture f) {
		ArrayList<ExtendedFixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeam, 5, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeam, 5, 2014, f.matchday,
				f.competition);

		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		return homeAwayAVG;
	}

	public static float last10BTSonly(ExtendedFixture f) {
		ArrayList<ExtendedFixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeam, 10, 2014, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeam, 10, 2014, f.matchday,
				f.competition);

		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;
		return BTSAVG;
	}

	public static float basic2(ExtendedFixture f, int year, float d, float e, float z) {
		ArrayList<ExtendedFixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeam, 10, year, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeam, 10, year, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastHomeHomeTeam = SQLiteJDBC.selectLastHome(f.homeTeam, 5, year, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = SQLiteJDBC.selectLastAway(f.awayTeam, 5, year, f.matchday,
				f.competition);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG + z * BTSAVG;
	}

	public static float poisson(ExtendedFixture f, int year) {
		ArrayList<ExtendedFixture> lastHomeTeam = SQLiteJDBC.selectLastAll(f.homeTeam, 10, year, f.matchday,
				f.competition);
		ArrayList<ExtendedFixture> lastAwayTeam = SQLiteJDBC.selectLastAll(f.awayTeam, 10, year, f.matchday,
				f.competition);
		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(ExtendedFixture f, int year) {
		float leagueAvgHome = SQLiteJDBC.selectAvgLeagueHome(f.competition, year, f.matchday);
		float leagueAvgAway = SQLiteJDBC.selectAvgLeagueAway(f.competition, year, f.matchday);
		float homeAvgFor = SQLiteJDBC.selectAvgHomeTeamFor(f.competition, f.homeTeam, year, f.matchday);
		float homeAvgAgainst = SQLiteJDBC.selectAvgHomeTeamAgainst(f.competition, f.homeTeam, year, f.matchday);
		float awayAvgFor = SQLiteJDBC.selectAvgAwayTeamFor(f.competition, f.awayTeam, year, f.matchday);
		float awayAvgAgainst = SQLiteJDBC.selectAvgAwayTeamAgainst(f.competition, f.awayTeam, year, f.matchday);

		float lambda = homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = awayAvgFor * homeAvgAgainst / leagueAvgHome;
		return Utils.poissonOver(lambda, mu);
	}

	public static void simplePredictions() throws JSONException, IOException, ParseException {
		SQLiteJDBC.update(2015);
		ArrayList<ExtendedFixture> fixtures = Api.findFixtures(1);
		//
		ArrayList<Entry> entries = new ArrayList<>();
		for (ExtendedFixture f : fixtures) {
			Algorithm alg = new Basic1(f);
			entries.add(new Entry(f, alg.calculate(), alg.getClass().getSimpleName()));
		}
		entries.sort(new Comparator<Entry>() {
			@Override
			public int compare(Entry o1, Entry o2) {
				int fc = o1.fixture.competition.compareTo(o2.fixture.competition);
				return fc != 0 ? fc : o1.compareTo(o2);
			}
		});
		// Collections.sort(entries, Collections.reverseOrder());
		System.out.println(entries);
	}
}
