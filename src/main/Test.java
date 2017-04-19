package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
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
import charts.LineChart;
import constants.MinMaxOdds;
import entries.AsianEntry;
import entries.Entry;
import entries.FinalEntry;
import predictions.Predictions;
import results.Results;
import runner.Runner;
import runner.RunnerAggregateInterval;
import runner.RunnerAllLines;
import runner.RunnerAsian;
import runner.RunnerAsianFinals;
import runner.RunnerDraws;
import runner.RunnerFinals;
import runner.RunnerIntersect;
import runner.RunnerOptimals;
import settings.Settings;
import settings.SettingsAsian;
import utils.Api;
import utils.Utils;
import xls.AsianUtils;
import xls.XlSUtils;
import xls.XlSUtils.MaximizingBy;

public class Test {

	public static void main(String[] args) throws JSONException, IOException, InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();

		// simplePredictions();

		// Results.eval("estimateBoth");
		// Results.eval("smooth");
		// Results.eval("test");

		// stored24();

		// makePredictions();
		// asianPredictions();

		// float total = 0f;
		// for (int year = 2005; year <= 2015; year++)
		// total += asian(year);
		// System.out.println("Avg profit is " + (total / 11));

		// System.out.println(Utils.pValueCalculator(11880, 0.04f, 1.8f));
		// makePredictions();

		// float total = 0f;
		// int startY = 2008;
		// int end = 2015;
		// for (int year = startY; year <= end; year++)
		// total += simulation(year, false);
		// System.out.println("Avg profit is " + (total / (end - startY + 1)));

		// for (int i = 2005; i <= 2015; i++)
		// XlSUtils.populateScores(i);

		// accumulators(2015, 2015);

		analysis(2011, 2015, DataType.ODDSPORTAL);

		// aggregateInterval();

		// stats();

		// optimals();
		// for (int year = 2013; year <= 2013; year++)
		// aggregate(year, 5);

		// optimalsbyCompetition();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");

	}

	private static void analysis(int start, int end, DataType type)
			throws InterruptedException, ExecutionException, IOException {
		ArrayList<FinalEntry> all = new ArrayList<>();
		HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear = new HashMap<>();
		for (int i = start; i <= end; i++) {
			ArrayList<FinalEntry> finals = finals(i, type);
			HashMap<String, ArrayList<FinalEntry>> byLeague = Utils.byLeague(finals);
			for (java.util.Map.Entry<String, ArrayList<FinalEntry>> league : byLeague.entrySet()) {
				if (!byLeagueYear.containsKey(league.getKey()))
					byLeagueYear.put(league.getKey(), new HashMap<>());

				byLeagueYear.get(league.getKey()).put(i, league.getValue());

			}

			all.addAll(finals);
		}

		// HashMap<String, ArrayList<FinalEntry>> byLeague =
		// Utils.byLeague(all);
		// for (java.util.Map.Entry<String, ArrayList<FinalEntry>> i :
		// byLeague.entrySet()) {
		// System.out.println(i.getKey());
		// Utils.analysys(i.getValue(), 3000);
		// }
//		ArrayList<FinalEntry> withTHU = Utils.withBestThreshold(byLeagueYear, 3, MaximizingBy.UNDERS);
		
//		ArrayList<FinalEntry> withTH1 = Utils.withBestThreshold(byLeagueYear, 1, MaximizingBy.OVERS);
//		ArrayList<FinalEntry> withTH2 = Utils.withBestThreshold(byLeagueYear, 2, MaximizingBy.OVERS);
//		ArrayList<FinalEntry> withTH3 = Utils.withBestThreshold(byLeagueYear, 3, MaximizingBy.OVERS);
//		ArrayList<FinalEntry> withTH4 = Utils.withBestThreshold(byLeagueYear, 4, MaximizingBy.OVERS);
//		
//		ArrayList<FinalEntry> withTH5 = Utils.withBestThreshold(byLeagueYear, 1, MaximizingBy.BOTH);
//		ArrayList<FinalEntry> withTH6 = Utils.withBestThreshold(byLeagueYear, 2, MaximizingBy.BOTH);
//		ArrayList<FinalEntry> withTH7 = Utils.withBestThreshold(byLeagueYear, 3, MaximizingBy.BOTH);
//		ArrayList<FinalEntry> withTH8 = Utils.withBestThreshold(byLeagueYear, 4, MaximizingBy.BOTH);
		
		

		Utils.fullAnalysys(all, 3000);
//		Utils.fullAnalysys(withTHU, 0);
//		Utils.fullAnalysys(withTH1, 1);
//		Utils.fullAnalysys(withTH2, 2);
//		Utils.fullAnalysys(withTH3, 3);
//		Utils.fullAnalysys(withTH4, 4);
//		Utils.fullAnalysys(withTH5, 5);
//		Utils.fullAnalysys(withTH6, 6);
//		Utils.fullAnalysys(withTH7, 7);
//		Utils.fullAnalysys(withTH8, 8);
	}

	// private static void accumulators(int start, int end) throws
	// InterruptedException, ExecutionException, IOException {
	//
	// float[] profit = new float[8];
	// for (int year = start; year <= end; year++) {
	// float[] curr = finals(year, false);
	// for (int i = 0; i < 8; i++)
	// profit[i] += curr[i];
	// }
	// System.out.println("=========================================================================");
	// for (int i = 0; i < 8; i++)
	// System.out.println("Total from " + (i + 3) + "s: " + profit[i]);
	//
	// }

	public static float simulationAllLines(int year, boolean parsedLeagues)
			throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();
		// ArrayList<String> dont = new
		// ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		FileInputStream file;
		if (!parsedLeagues)
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\fullodds" + year + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (!sh.getSheetName().equals("IT"))
				continue;
			// if (!Arrays.asList(MinMaxOdds.FULL).contains(sh.getSheetName()))
			// continue;

			// if
			// (!Arrays.asList(MinMaxOdds.MANUAL).contains(sh.getSheetName()))
			// continue;

			threadArray.add(pool.submit(new RunnerAllLines(sh, year)));
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

	public static float asian(int year, boolean parsedLeagues)
			throws IOException, InterruptedException, ExecutionException {
		String base = new File("").getAbsolutePath();

		FileInputStream file;
		if (!parsedLeagues)
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\odds" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (!sh.getSheetName().equals("SPA2"))
				continue;
			// if (Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
			// continue;

			threadArray.add(pool.submit(new RunnerAsian(sh, year)));
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

	public static float draws(int year, boolean b) throws IOException, InterruptedException, ExecutionException {
		String base = new File("").getAbsolutePath();

		FileInputStream file;
		if (!b)
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\odds" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (!sh.getSheetName().equals("BRA"))
				continue;
			// if (!Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
			// continue;

			threadArray.add(pool.submit(new RunnerDraws(sh, year)));
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

	public static float asianFinals(int year) throws IOException, InterruptedException, ExecutionException {
		String base = new File("").getAbsolutePath();

		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		ArrayList<AsianEntry> all = new ArrayList<>();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<ArrayList<AsianEntry>>> threadArray = new ArrayList<Future<ArrayList<AsianEntry>>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			// if (!sh.getSheetName().equals("G1"))
			// continue;
			// if(!Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
			// continue;

			threadArray.add(pool.submit(new RunnerAsianFinals(sh, year)));
		}

		for (Future<ArrayList<AsianEntry>> fd : threadArray) {
			// totalProfit += fd.get();
			all.addAll(fd.get());
			// System.out.println("Total profit: " + String.format("%.2f",
			// totalProfit));
		}

		AsianUtils.analysis(all);

		// System.out.println("Total profit for season " + year + " is " +
		// String.format("%.2f", totalProfit));
		workbook.close();
		file.close();
		pool.shutdown();
		return totalProfit;
	}

	public static final void singleMethod() throws IOException, ParseException {

		float totalTotal = 0f;
		for (int year = 2005; year <= 2015; year++) {
			float total = 0f;
			String base = new File("").getAbsolutePath();
			FileInputStream file = new FileInputStream(
					new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			Iterator<Sheet> sheet = workbook.sheetIterator();
			while (sheet.hasNext()) {
				HSSFSheet sh = (HSSFSheet) sheet.next();
				if (!Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
					continue;
				float profit = XlSUtils.singleMethod(sh, XlSUtils.selectAll(sh, 10), year);
				// System.out.println(sh.getSheetName() + " " + year + " " +
				// profit);
				total += profit;
			}
			System.out.println("Total for " + year + ": " + total);
			workbook.close();
			file.close();
			totalTotal += total;
		}
		System.out.println("Avg is: " + totalTotal / 11);
	}

	public static void stored24() throws InterruptedException {
		int bestPeriod = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;

		int period = 3;

		float total = 0f;
		int sizeTotal = 0;
		float totalStake = 0f;

		ArrayList<FinalEntry> all = new ArrayList<>();

		for (int i = 2005 + period; i <= 2014; i++) {
			float curr = 0f;
			int size = 0;
			float staked = 0f;
			for (String league : Results.LEAGUES) {
				if (!Arrays.asList(MinMaxOdds.DONT).contains(league)) {
					ArrayList<FinalEntry> list = XlSUtils.bestCot(league, i, period, "realdouble15");
					// System.out.println("Profit for: " + league + " last: " +
					// i + " is: " + Results.format(pr));

					curr += Utils.getScaledProfit(list, 0f)[0];
					size += list.size();
					staked += Utils.getScaledProfit(list, 0f)[1];
					all.addAll(list);
				}
			}

			System.out.println(
					"For " + i + ": " + curr + "  yield: " + Results.format((curr / staked) * 100) + " from: " + size);
			total += curr;
			sizeTotal += size;
			totalStake += staked;

			if (curr > bestProfit) {
				bestProfit = curr;
				bestPeriod = i;
			}

		}

		System.out.println(
				"Total avg: " + total / (10 - period) + " avg yield: " + Results.format(100 * (total / totalStake)));
				// Utils.drawAnalysis(all);

		// System.out.println("Best period: " + bestPeriod + " with profit: " +
		// bestProfit);
	}

	public static final void aggregateInterval() throws IOException, InterruptedException, ExecutionException {
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + 2014 + "-" + 2015 + ".xls"));

		ExecutorService pool = Executors.newFixedThreadPool(7);
		ArrayList<Future<Settings>> threadArray = new ArrayList<Future<Settings>>();
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (dont.contains(sh.getSheetName()))
				continue;
			threadArray.add(pool.submit(new RunnerAggregateInterval(2005, 2007, sh)));
		}

		for (Future<Settings> fd : threadArray)
			fd.get();

		workbook.close();
		file.close();
		pool.shutdown();
	}

	public static final void aggregate(int year, int n) throws IOException, InterruptedException, ExecutionException {
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Settings>> threadArray = new ArrayList<Future<Settings>>();
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			// if (dont.contains(sh.getSheetName()))
			// continue;
			// if (sh.getSheetName().equals("D1"))
			threadArray.add(pool.submit(new RunnerAggregateInterval(year - n, year - 1, sh)));
		}

		HashMap<String, Settings> optimals = new HashMap<>();

		for (Future<Settings> fd : threadArray) {
			Settings result = fd.get();
			optimals.put(result.league, result);
			SQLiteJDBC.storeSettings(result, year, n);
		}

		// // TESTING
		// float totalProfit = 0f;
		// Iterator<Sheet> sheets = workbook.sheetIterator();
		// while (sheets.hasNext()) {
		// HSSFSheet sh = (HSSFSheet) sheets.next();
		// // if (sh.getSheetName().equals("D1")) {
		// ArrayList<FinalEntry> list = XlSUtils.runWithSettingsList(sh,
		// XlSUtils.selectAll(sh),
		// optimals.get(sh.getSheetName()));
		// float profit = Utils.getProfit(list,
		// optimals.get(sh.getSheetName()));
		// totalProfit += profit;
		// System.out.println(sh.getSheetName() + ": " + profit);
		// // }
		// }

		// System.out.println("Total for " + year + " : " + totalProfit);

		workbook.close();
		file.close();
		pool.shutdown();
	}

	public static void stats() throws IOException, ParseException {
		for (int year = 2005; year <= 2015; year++) {
			String base = new File("").getAbsolutePath();

			FileInputStream file = new FileInputStream(
					new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet("E0");
			ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);
			System.out.println(year + " over: " + Utils.countOverGamesPercent(all) + "% AVG: " + Utils.findAvg(all));
			System.out.println("Overs when draw: " + Utils.countOversWhenDraw(all));
			System.out.println("Overs when win/loss: " + Utils.countOversWhenNotDraw(all));
			Utils.byWeekDay(all);
			System.out.println();
			workbook.close();
			file.close();
		}
	}

	public static float simulation(int year, boolean parsedLeagues)
			throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();
		// ArrayList<String> dont = new
		// ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		FileInputStream file;
		if (!parsedLeagues)
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\odds" + year + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			// if (!sh.getSheetName().equals("I1"))
			// continue;
			if (!Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
				continue;

			// if (!Arrays.asList(MinMaxOdds.PFS).contains(sh.getSheetName()))
			// continue;
			// if
			// (sh.getSheetName().equals("D1")||sh.getSheetName().equals("SP1"))
			// continue;

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

	public static ArrayList<FinalEntry> finals(int year, DataType type)
			throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));
		ArrayList<String> draw = new ArrayList<String>(Arrays.asList(MinMaxOdds.DRAW));

		FileInputStream file;
		if (type.equals(DataType.ALLEURODATA))
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\odds" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		ArrayList<FinalEntry> all = new ArrayList<>();

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<ArrayList<FinalEntry>>> threadArray = new ArrayList<Future<ArrayList<FinalEntry>>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
//			if (!Arrays.asList(MinMaxOdds.SHOTS).contains(sh.getSheetName()))
//				continue;
			// if
			// (!Arrays.asList(MinMaxOdds.MANUAL).contains(sh.getSheetName()))
			// continue;

			// if (!sh.getSheetName().equals("ENG"))
			// continue;
			threadArray.add(pool.submit(new RunnerFinals(sh, year)));
		}

		for (Future<ArrayList<FinalEntry>> fd : threadArray) {
			all.addAll(fd.get());
		}

		workbook.close();
		file.close();
		pool.shutdown();

		// Utils.predictionCorrelation(all);

		// Utils.analysys(all, year);
		// Utils.drawAnalysis(all);
		// ArrayList<FinalEntry> overs = Utils.onlyOvers(all);
		// Utils.analysys(overs, year);
		// Utils.hyperReal(overs, year, 1000f, 0.05f);
		// Utils.evaluateRecord(all);
		// LineChart.draw(Utils.createProfitMovementData(all), year);

		float[] profits = new float[8];
		// System.out.println(year);
		// for (int i = 3; i <= 10; i++)
		// profits[i - 3] = Utils.bestNperWeek(all, i);

		return all;

	}

	public static float simulationIntersect(int year) throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();
		// ArrayList<String> dont = new
		// ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		float totalProfit = 0.0f;

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			// if (dont.contains(sh.getSheetName()))
			// continue;
			threadArray.add(pool.submit(new RunnerIntersect(sh, year)));
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

		for (int year = 2015; year <= 2015; year++) {
			float total = 0f;
			ExecutorService pool = Executors.newFixedThreadPool(1);
			ArrayList<Future<Float>> threadArray = new ArrayList<Future<Float>>();
			FileInputStream filedata = new FileInputStream(
					new File(basePath + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

			Iterator<Sheet> sh = workbookdata.sheetIterator();
			while (sh.hasNext()) {
				HSSFSheet i = (HSSFSheet) sh.next();
				// if (i.getSheetName().equals("SP2"))
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

	public static void optimalsbyCompetition() throws IOException, ParseException {

		HashMap<String, ArrayList<Settings>> optimals = new HashMap<>();

		String basePath = new File("").getAbsolutePath();

		for (int year = 2015; year <= 2015; year++) {

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
				float profit = Utils.getProfit(fes);
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

	// public static void findSettings(int year) throws IOException,
	// ParseException {
	// FileInputStream file = new FileInputStream(new File(
	// "C:\\Users\\Admin\\workspace\\Soccer\\data\\all-euro-data-" + year + "-"
	// + (year + 1) + ".xls"));
	// HSSFWorkbook workbook = new HSSFWorkbook(file);
	// Iterator<Sheet> sheet = workbook.sheetIterator();
	// float totalProfit = 0.0f;
	// while (sheet.hasNext()) {
	// HSSFSheet sh = (HSSFSheet) sheet.next();
	// Settings sett = /*
	// * XlSUtils.runForLeagueWithOdds(sh,
	// * xls.XlSUtils.selectAll(sh), 1.0d);
	// */
	//
	// XlSUtils.findInterval(XlSUtils.selectAll(sh), sh, year);
	// Settings stored = SQLiteJDBC.getSettings(sett.league, year);
	//
	// if (stored == null) {
	// SQLiteJDBC.storeSettings(sett, year);
	// System.out.println(sett);
	// totalProfit += sett.profit;
	// } else if (stored.profit >= sett.profit) {
	// System.out.println(stored);
	// totalProfit += stored.profit;
	// } else {
	// System.out.println(sett);
	// SQLiteJDBC.deleteSettings(sett.league, year);
	// SQLiteJDBC.storeSettings(sett, year);
	// System.out.println(sett);
	// totalProfit += sett.profit;
	// }
	// }
	// System.out.println("TotalProfit: " + totalProfit);
	// workbook.close();
	// }

	public static void makePredictions() throws IOException, InterruptedException, ParseException {
		String basePath = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File("C:\\Users\\Tereza\\Desktop\\fixtures.xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<ExtendedFixture> fixtures = XlSUtils.selectForPrediction(sheet);

		FileInputStream filedata = new FileInputStream(
				new File("C:\\Users\\Tereza\\Desktop\\all-euro-data-2015-2016.xls"));
		HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

		HashMap<String, Settings> optimal = new HashMap<>();
		Iterator<Sheet> sh = workbookdata.sheetIterator();
		while (sh.hasNext()) {
			HSSFSheet i = (HSSFSheet) sh.next();
			if (i.getSheetName().equals("SP2"))
				optimal.put(i.getSheetName(), XlSUtils.predictionSettings(i, 2015));
		}

		for (ExtendedFixture f : fixtures) {
			HSSFSheet league = workbookdata.getSheet(f.competition);
			XlSUtils.makePrediction(sheet, league, f, optimal.get(league.getSheetName()));
		}
		workbook.close();
		workbookdata.close();
	}

	public static void asianPredictions() throws IOException, InterruptedException, ParseException {
		String basePath = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File("C:\\Users\\Tereza\\Desktop\\fixtures.xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<ExtendedFixture> fixtures = XlSUtils.selectForPrediction(sheet);

		FileInputStream filedata = new FileInputStream(
				new File("C:\\Users\\Tereza\\Desktop\\all-euro-data-2015-2016.xls"));
		HSSFWorkbook workbookdata = new HSSFWorkbook(filedata);

		ArrayList<AsianEntry> all = new ArrayList<>();
		HashMap<String, SettingsAsian> optimal = new HashMap<>();
		Iterator<Sheet> sh = workbookdata.sheetIterator();
		while (sh.hasNext()) {
			HSSFSheet i = (HSSFSheet) sh.next();
			optimal.put(i.getSheetName(), XlSUtils.asianPredictionSettings(i, 2015));
		}

		for (ExtendedFixture f : fixtures) {
			HSSFSheet league = workbookdata.getSheet(f.competition);
			all.addAll(AsianUtils.makePrediction(sheet, league, f, optimal.get(league.getSheetName())));
		}

		all.sort(new Comparator<AsianEntry>() {

			@Override
			public int compare(AsianEntry o1, AsianEntry o2) {
				return ((Float) o2.expectancy).compareTo((Float) o1.expectancy);
			}
		});

		System.out.println(all);
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

	public enum DataType {
		ALLEURODATA, ODDSPORTAL
	}

}
