package predictions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;

import entries.FinalEntry;
import runner.RunnerAsianPredictions;
import runner.RunnerPredictions;
import scraper.Scraper;

public class Predictions {
	

	public static final ArrayList<String> CHECKLIST = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

		CHECKLIST.add("ENG");
		CHECKLIST.add("ENG2");
		CHECKLIST.add("ENG3");
		CHECKLIST.add("ENG4");
		CHECKLIST.add("ENG5");
		CHECKLIST.add("IT");
		CHECKLIST.add("IT2");
		CHECKLIST.add("FR");
		CHECKLIST.add("FR2");
		CHECKLIST.add("SPA");
		CHECKLIST.add("SPA2");
		CHECKLIST.add("GER");
		CHECKLIST.add("GER2");
		CHECKLIST.add("SCO");
		CHECKLIST.add("NED");
		CHECKLIST.add("BEL");
		CHECKLIST.add("SWI");
		CHECKLIST.add("POR");
		CHECKLIST.add("GRE");
		CHECKLIST.add("TUR");
		CHECKLIST.add("BUL");
		CHECKLIST.add("RUS");
		CHECKLIST.add("AUS");
		CHECKLIST.add("DEN");
		CHECKLIST.add("CZE");
		CHECKLIST.add("ARG");
		CHECKLIST.add("POL");
		CHECKLIST.add("CRO");
		CHECKLIST.add("SLO");

		 Scraper.updateInParallel(CHECKLIST, 2, true, UpdateType.AUTOMATIC);

//		predictions(2016, true, UpdateType.AUTOMATIC);

		// asianPredictions(2016, true);

	}

	public static ArrayList<FinalEntry> predictions(int year, boolean parsedLeagues, UpdateType automatic)
			throws InterruptedException, ExecutionException, IOException {
		String base = new File("").getAbsolutePath();

		FileInputStream file;
		if (!parsedLeagues)
			file = new FileInputStream(new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		else
			file = new FileInputStream(new File(base + "\\data\\odds" + year + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();
		ArrayList<FinalEntry> all = new ArrayList<>();

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<ArrayList<FinalEntry>>> threadArray = new ArrayList<Future<ArrayList<FinalEntry>>>();
		ArrayList<String> leagues = automatic.equals(UpdateType.AUTOMATIC) ? Scraper.getTodaysLeagueList() : CHECKLIST;
		System.out.println(leagues);
		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			// if (!sh.getSheetName().equals("ENG2"))
			// continue;
			if (!leagues.contains(sh.getSheetName()))
				continue;

			threadArray.add(pool.submit(new RunnerPredictions(sh, year)));
		}

		for (Future<ArrayList<FinalEntry>> fd : threadArray) {
			all.addAll(fd.get());
			// System.out.println("Total profit: " + String.format("%.2f",
			// totalProfit));
		}
		// System.out.println("Total profit for season " + year + " is " +
		// String.format("%.2f", totalProfit));
		workbook.close();
		file.close();
		pool.shutdown();

		all.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {
				return ((Float) o1.prediction).compareTo((Float) o2.prediction);
			}
		});
		System.out.println(all);

		return all;
	}

	public static float asianPredictions(int year, boolean parsedLeagues)
			throws InterruptedException, ExecutionException, IOException {
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
			// if (!sh.getSheetName().equals("ENG2"))
			// continue;
			if (!Predictions.CHECKLIST.contains(sh.getSheetName()))
				continue;

			threadArray.add(pool.submit(new RunnerAsianPredictions(sh, year)));
		}

		for (Future<Float> fd : threadArray) {
			totalProfit += fd.get();
			// System.out.println("Total profit: " + String.format("%.2f",
			// totalProfit));
		}
		// System.out.println("Total profit for season " + year + " is " +
		// String.format("%.2f", totalProfit));
		workbook.close();
		file.close();
		pool.shutdown();
		return totalProfit;
	}

}
