package main;

import settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
import constants.MinMaxOdds;
import utils.Api;
import utils.Utils;
import xls.XlSUtils;

public class Test {

	public static void main(String[] args) throws JSONException, IOException {

		long start = System.currentTimeMillis();

		// simplePredictions();

		float total = 0f;
		try {
			for (int year = 2005; year <= 2015; year++)
				total += simulation(year);
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Avg profit is " + (total / 11));

		// makePredictions();

		// aggregateInterval();

		// stats();

		// try {
		// optimals();
		// } catch (InterruptedException | ExecutionException e) {
		// e.printStackTrace();
		// }

		// optimalsbyCompetition();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");

	}

	public static final void aggregateInterval() throws IOException {
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + 2014 + "-" + 2015 + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			System.out.println(XlSUtils.aggregateInterval(2012, 2014, sh.getSheetName()));
		}
		workbook.close();
		file.close();
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
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(8);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (dont.contains(sh.getSheetName()))
				continue;
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

	public static void optimals() throws IOException, InterruptedException, ExecutionException {
		String basePath = new File("").getAbsolutePath();
		float totalTotal = 0f;

		for (int year = 2010; year <= 2015; year++) {
			float total = 0f;
			ExecutorService pool = Executors.newFixedThreadPool(8);
			ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				threadArray.add(pool.submit(new RunnerOptimals(i, year)));
			}

			for (Future<Float> fd : threadArray) {
				total += fd.get();
			}

			System.out.println("Total profit for " + year + " is: " + total);

			totalTotal += total;
			workbookdata.close();
			pool.shutdown();
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

		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		for (int year = 2006; year <= 2015; year++) {
			float total = 0f;
			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				if (dont.contains(i.getSheetName()))
					continue;
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
