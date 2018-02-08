package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONException;

import constants.Constants;
import entries.AsianEntry;
import entries.FinalEntry;
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
import utils.Utils;
import xls.AsianUtils;
import xls.XlSUtils;

public class Test {

	public static void main(String[] args) throws JSONException, IOException, InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();
		long initialMemory = getUsedMemory();
		// Results.eval("test");

		// float total = 0f;
		// int startY = 2016;
		// int end = 2016;
		// for (int year = startY; year <= end; year++)
		// total += simulation(year, DataType.ODDSPORTAL);
		// System.out.println("Avg profit is " + (total / (end - startY + 1)));

		// analysis(2017, 2017, DataType.ALLEURODATA);


		String[] all = new String[] { /* "BRA", */ "ENG", "ENG2", "GER", "FR", "SPA", "SPA2", "IT", "NED", "SWI", "POR",
				"TUR" };

		Analysis analysis = new Analysis(2017, 2017, "GER");
		analysis.makePredictions();
		analysis.printAnalysis();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");
		System.out.println("Total used by program = " + (getUsedMemory() - initialMemory) / 1024 + " MB");

	}

	private static long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
	}

	private static void analysis(int start, int end, DataType type)
			throws InterruptedException, ExecutionException, IOException {
		ArrayList<FinalEntry> all = new ArrayList<>();
		HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear = new HashMap<>();

		populateForAnalysis(start, end, all, byLeagueYear, type);
		// populateForAnalysisFromDB(start, end, all, byLeagueYear, type,
		// "shots");

		// ArrayList<FinalEntry> eng = (ArrayList<FinalEntry>)
		// byLeagueYear.get("FR2").values().stream()
		// .flatMap(List::stream).collect(Collectors.toList());

		// HashMap<String, ArrayList<FinalEntry>> byLeague =
		// Utils.byLeague(all);
		// for (java.util.Map.Entry<String, ArrayList<FinalEntry>> i :
		// byLeague.entrySet()) {
		// System.out.println(i.getKey());
		// System.out.println(i.getValue().size());
		// Utils.analysys(i.getValue(), i.getKey(), false);
		// }

		// ArrayList<FinalEntry> withTH1 = Utils.withBestThreshold(byLeagueYear,
		// 1, MaximizingBy.OVERS);
		// ArrayList<FinalEntry> withTH2 = Utils.withBestThreshold(byLeagueYear,
		// 2, MaximizingBy.OVERS);
		// ArrayList<FinalEntry> withTH3 = Utils.withBestThreshold(byLeagueYear,
		// 3, MaximizingBy.OVERS);
		// ArrayList<FinalEntry> withTH4 = Utils.withBestThreshold(byLeagueYear,
		// 4, MaximizingBy.OVERS);

		// ArrayList<FinalEntry> withTH5 = Utils.withBestThreshold(byLeagueYear,
		// 1, MaximizingBy.BOTH);
		// ArrayList<FinalEntry> withTH6 = Utils.withBestThreshold(byLeagueYear,
		// 2, MaximizingBy.BOTH);
		// ArrayList<FinalEntry> withTH7 = Utils.withBestThreshold(byLeagueYear,
		// 3, MaximizingBy.BOTH);
		// ArrayList<FinalEntry> withTH8 = Utils.withBestThreshold(byLeagueYear,
		// 4, MaximizingBy.BOTH);

		// ArrayList<FinalEntry> withTH9 = Utils.withBestThreshold(byLeagueYear,
		// 1, MaximizingBy.UNDERS);
		// ArrayList<FinalEntry> withTH10 =
		// Utils.withBestThreshold(byLeagueYear, 2, MaximizingBy.UNDERS);
		// ArrayList<FinalEntry> withTH11 =
		// Utils.withBestThreshold(byLeagueYear, 3, MaximizingBy.UNDERS);
		// ArrayList<FinalEntry> withTH12 =
		// Utils.withBestThreshold(byLeagueYear, 4, MaximizingBy.UNDERS);

		Utils.fullAnalysys(all, "all");

		// Utils.fullAnalysys(withTH1, "maxByThOvers(1)");

		// ArrayList<FinalEntry> restricted =
		// Utils.filterByOdds(Utils.cotRestrict(Utils.onlyUnders(all), 0.175f),
		// 1f,
		// 2.2f);
		//
		// all = Utils.filterByOdds(Utils.onlyUnders(Utils.noequilibriums(all)),
		// 1.55f, 1.87f);
		// System.out.println("from " + all.size());
		// System.out.println(Utils.getNormalizedProfit(all));
		// System.out.println(Utils.getNormalizedYield(all));
		// System.out.println("1 in " + Utils.evaluateRecordNormalized(all));
		// LineChart.draw(Utils.createProfitMovementData(all), 3000);

		// ArrayList<FinalEntry> withTH1 = Utils.withBestSettings(byLeagueYear,
		// 4);

	}

	private static void populateForAnalysisFromDB(int start, int end, ArrayList<FinalEntry> all,
			HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear, DataType type, String description)
			throws InterruptedException {
		for (int i = start; i <= end; i++) {
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (String comp : Arrays.asList(Constants.SHOTS)) {
				finals.addAll(SQLiteJDBC.selectFinals(comp, i, description));
			}

			HashMap<String, ArrayList<FinalEntry>> byLeague = Utils.byLeague(finals);
			for (java.util.Map.Entry<String, ArrayList<FinalEntry>> league : byLeague.entrySet()) {
				if (!byLeagueYear.containsKey(league.getKey()))
					byLeagueYear.put(league.getKey(), new HashMap<>());

				byLeagueYear.get(league.getKey()).put(i, league.getValue());

			}

			all.addAll(finals);
		}
	}

	private static void populateForAnalysis(int start, int end, ArrayList<FinalEntry> all,
			HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear, DataType type)
			throws InterruptedException, ExecutionException, IOException {
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
			// if (!sh.getSheetName().equals("IT"))
			// continue;
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

	public static final void aggregateInterval() throws IOException, InterruptedException, ExecutionException {
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(Constants.DONT));
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
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(Constants.DONT));
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
			ArrayList<Fixture> all = XlSUtils.selectAllAll(sheet);
			System.out.println(year + " over: " + Utils.countOverGamesPercent(all) + "% AVG: " + Utils.findAvg(all));
			System.out.println("Overs when draw: " + Utils.countOversWhenDraw(all));
			System.out.println("Overs when win/loss: " + Utils.countOversWhenNotDraw(all));
			Utils.byWeekDay(all);
			System.out.println();
			workbook.close();
			file.close();
		}
	}

	public static float simulation(int year, DataType alleurodata)
			throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();
		// ArrayList<String> dont = new
		// ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));

		FileInputStream file;
		if (alleurodata.equals(DataType.ALLEURODATA))
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
			// if (!sh.getSheetName().equals("E0"))
			// continue;
			if (!Arrays.asList(Constants.SHOTS).contains(sh.getSheetName()))
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
		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(Constants.DONT));
		ArrayList<String> draw = new ArrayList<String>(Arrays.asList(Constants.DRAW));

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
			// if (!Arrays.asList(Constants.SHOTS).contains(sh.getSheetName()))
			// continue;

			if (!sh.getSheetName().equals("E0"))
				continue;
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

	@Deprecated
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

		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(Constants.DONT));

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

	// for making predictions from footballdata excel files
	@Deprecated
	public static void makePredictions() throws IOException, InterruptedException, ParseException {
		String basePath = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File("C:\\Users\\Tereza\\Desktop\\fixtures.xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<Fixture> fixtures = XlSUtils.selectForPrediction(sheet);

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

		for (Fixture f : fixtures) {
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
		ArrayList<Fixture> fixtures = XlSUtils.selectForPrediction(sheet);

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

		for (Fixture f : fixtures) {
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

	// public static void printSuccessRate(ArrayList<FinalEntry> list, String
	// listName) {
	// int successOver50 = 0, failureOver50 = 0;
	// for (FinalEntry fe : list) {
	// if (fe.success())
	// successOver50++;
	// else
	// failureOver50++;
	// }
	// System.out.println("success" + listName + ": " + successOver50 +
	// "failure" + listName + ": " + failureOver50);
	// System.out
	// .println("Rate" + listName + ": " + String.format("%.2f", ((float)
	// successOver50 / list.size()) * 100));
	// System.out.println("Profit" + listName + ": " + String.format("%.2f",
	// successOver50 * 0.9 - failureOver50));
	// }

	public enum DataType {
		ALLEURODATA, ODDSPORTAL
	}

}
