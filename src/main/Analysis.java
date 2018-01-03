package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import entries.FinalEntry;
import runner.RunnerAnalysis;
import settings.Settings;
import utils.FixtureUtils;
import utils.Stats;
import utils.Utils;

public class Analysis {

	public int startYear;
	public int endYear;
	public ArrayList<String> leagues;
	public ArrayList<FinalEntry> predictions;

	public Analysis(int startYear, int endYear, ArrayList<String> leagues) {
		super();
		this.startYear = startYear;
		this.endYear = endYear;
		this.leagues = leagues;
		this.predictions = new ArrayList<>();
	}

	public Analysis(int startYear, int endYear, String league) {
		this(startYear, endYear, new ArrayList<>());
		this.leagues.add(league);
	}

	public void makePredictions() throws InterruptedException, ExecutionException {
		HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear = new HashMap<>();

		ExecutorService pool = Executors.newFixedThreadPool(3);
		ArrayList<Future<ArrayList<FinalEntry>>> threadArray = new ArrayList<Future<ArrayList<FinalEntry>>>();

		for (int year = startYear; year <= endYear; year++) {
			for (String league : leagues) {
				ArrayList<Fixture> fixtures = SQLiteJDBC.selectFixtures(league, year);

				threadArray.add(pool.submit(new RunnerAnalysis(fixtures, league, year)));
			}
		}

		for (Future<ArrayList<FinalEntry>> fd : threadArray)
			predictions.addAll(fd.get());

		pool.shutdown();
	}

	// TODO add support for different classifiers if needed
	public static ArrayList<FinalEntry> predict(ArrayList<Fixture> fixtures, String league, int year) {
		ArrayList<FinalEntry> result = new ArrayList<>();

		int maxMatchDay = FixtureUtils.addMatchDay(fixtures);

		for (int i = /* dictionary == null ? 100 : */ 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(fixtures, i);

			Settings temp = Settings.shots(league);
			ArrayList<FinalEntry> finals = FixtureUtils.runWithSettingsList(fixtures, current, temp);

			result.addAll(finals);
		}

		return result;
	}

	public void printAnalysis() {
		Utils.analysys(predictions, "ENG", true);

		byBookmaker(predictions);
		byLine();

		valueOverPinnacle();

	}

	private void valueOverPinnacle() {
		ArrayList<FinalEntry> finals = predictions.stream().map(fe -> fe.getValueBetOverPinnacle(false))
				.filter(fe -> fe != null).collect(Collectors.toCollection(ArrayList::new));
		System.out.println(new Stats(finals, "Values over pinnacle"));

		ArrayList<FinalEntry> valuesWithPrediction = predictions.stream().filter(fe -> fe.prediction != 0.5f)
				.map(fe -> fe.getValueBetOverPinnacle(true)).filter(fe -> fe != null)
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println(new Stats(valuesWithPrediction, "Values over pinnacle with predictions"));
		Utils.analysys(valuesWithPrediction, "Values over pinnacle with predictions", true);
		
		byBookmaker(valuesWithPrediction);

	}

	private void byBookmaker(ArrayList<FinalEntry> predictions) {
		ArrayList<Stats> stats = new ArrayList<>();
		HashSet<String> bookies = getOUbookmakerList(Utils.onlyFixtures(predictions));
		for (String b : bookies) {
			ArrayList<FinalEntry> finals = predictions.stream().map(fe -> fe.getPredictionBy(b))
					.collect(Collectors.toCollection(ArrayList::new));
			ArrayList<FinalEntry> missing = finals.stream().filter(fe -> fe.fixture.getMaxClosingOverOdds() >= 1f)
					.collect(Collectors.toCollection(ArrayList::new));
			if (!missing.isEmpty()) {
				stats.add(new Stats(Utils.noequilibriums(missing), b));
			}
		}

		stats.sort(Comparator.comparing(Stats::getPvalueOdds).reversed());
		stats.stream().forEach(System.out::println);
	}

	private void byLine() {
		ArrayList<Stats> stats = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			final Integer intI = new Integer(i);
			ArrayList<FinalEntry> finals = predictions.stream().map(fe -> fe.getPredictionForLine(intI))
					.filter(fe -> fe != null).collect(Collectors.toCollection(ArrayList::new));
			String description = new Float(-0.5f + i * 0.25f).toString();
			stats.add(new Stats(Utils.noequilibriums(finals), description));
		}

		stats.sort(Comparator.comparing(Stats::getPvalueOdds).reversed());
		stats.stream().forEach(System.out::println);

	}

	private HashSet<String> getOUbookmakerList(ArrayList<Fixture> fixtures) {
		HashSet<String> result = new HashSet<>();
		for (Fixture f : fixtures)
			f.overUnderOdds.stream().forEach(ou -> result.add(ou.bookmaker));

		return result;
	}

}
