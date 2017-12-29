package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import entries.FinalEntry;
import runner.RunnerAnalysis;
import settings.Settings;
import utils.FixtureUtils;

public class Analysis {

	public int startYear;
	public int endYear;
	public ArrayList<String> leagues;
	public ArrayList<Prediction> predictions;

	public Analysis(int startYear, int endYear, ArrayList<String> leagues) {
		super();
		this.startYear = startYear;
		this.endYear = endYear;
		this.leagues = leagues;
	}

	public void makePredictions() throws InterruptedException, ExecutionException {
		HashMap<String, HashMap<Integer, ArrayList<Prediction>>> byLeagueYear = new HashMap<>();

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<ArrayList<Prediction>>> threadArray = new ArrayList<Future<ArrayList<Prediction>>>();

		for (int year = startYear; year <= endYear; year++) {
			for (String league : leagues) {
				ArrayList<Fixture> fixtures = SQLiteJDBC.selectFixtures(league, year);

				threadArray.add(pool.submit(new RunnerAnalysis(fixtures, league, year)));
			}
		}

		for (Future<ArrayList<Prediction>> fd : threadArray)
			predictions.addAll(fd.get());

		pool.shutdown();
	}

	
	//TODO add support for different classifiers if needed
	public static ArrayList<Prediction> predict(ArrayList<Fixture> fixtures, String league, int year) {
		ArrayList<Prediction> result = new ArrayList<>();


		int maxMatchDay = FixtureUtils.addMatchDay(fixtures);

		for (int i = /* dictionary == null ? 100 : */ 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(fixtures, i);

//			ArrayList<FinalEntry> finals = FixtureUtils.runWithSettingsList(fixtures, current, temp);

//			result.addAll(finals);
		}

		return result;
	}

}
