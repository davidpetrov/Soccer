package main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import entries.FinalEntry;
import odds.OverUnderOdds;
import runner.RunnerAnalysis;
import settings.Settings;
import utils.FixtureUtils;
import utils.Stats;
import utils.Utils;

public class Analysis {

	public int startYear;
	public int endYear;
	public String[] leagues;
	public ArrayList<FinalEntry> predictions;

	public Analysis(int startYear, int endYear, String[] strings) {
		super();
		this.startYear = startYear;
		this.endYear = endYear;
		this.leagues = strings;
		this.predictions = new ArrayList<>();
	}

	public Analysis(int startYear, int endYear, String league) {
		this(startYear, endYear, new String[] { league });
	}

	public void makePredictions() throws InterruptedException, ExecutionException {
		// HashMap<String, HashMap<Integer, ArrayList<FinalEntry>>> byLeagueYear = new
		// HashMap<>();

		// ExecutorService pool = Executors.newFixedThreadPool(2);
		// ArrayList<Future<ArrayList<FinalEntry>>> threadArray = new
		// ArrayList<Future<ArrayList<FinalEntry>>>();

		for (int year = startYear; year <= endYear; year++) {
			for (String league : leagues) {
				// long start = System.currentTimeMillis();
				ArrayList<Fixture> fixtures = SQLiteJDBC.selectFixtures(league, year);
				// System.out.println(
				// (System.currentTimeMillis() - start) / 1000d + "sec loading data for: " +
				// league + " " + year);
				// threadArray.add(pool.submit(new RunnerAnalysis(fixtures, league, year)));
				predictions.addAll(predict(fixtures, league, year));
			}
		}

		// for (Future<ArrayList<FinalEntry>> fd : threadArray)
		// predictions.addAll(fd.get());
		//
		// pool.shutdown();
	}

	// TODO add support for different classifiers if needed
	public static ArrayList<FinalEntry> predict(ArrayList<Fixture> fixtures, String league, int year) {
		ArrayList<FinalEntry> result = new ArrayList<>();

		int maxMatchDay = FixtureUtils.addMatchDay(fixtures);

		for (int i = /* dictionary == null ? 100 : */ 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(fixtures, i);

			Settings temp = Settings.shots(league);
			ArrayList<FinalEntry> finals = FixtureUtils.runWithSettingsList(fixtures, current, temp);

			cleanUpUnnecessaryOddsData(finals);

			result.addAll(finals);
		}

		return result;
	}

	/**
	 * 
	 * @param finals
	 */
	private static void cleanUpUnnecessaryOddsData(ArrayList<FinalEntry> finals) {
		for (FinalEntry i : finals) {
			if (i.fixture.homeTeam.equals("Flamengo RJ") && i.fixture.awayTeam.equals("America MG"))
				System.out.println();

			ArrayList<Float> lines = i.fixture.getBaseOULines();
			Float overOdds = i.fixture.getMaxClosingOverOdds();
			Float underOdds = i.fixture.getMaxClosingUnderOdds();

			i.overOdds = new OverUnderOdds("max", i.fixture.date, 2.5f, overOdds, -1f);
			i.underOdds = new OverUnderOdds("max", i.fixture.date, 2.5f, -1f, underOdds);

			for (Float l : lines) {
				i.fixture.getMaxClosingOUOddsByLine(l);
				i.fixture.getMaxCloingByLineAndBookie(l, "Pinnacle");
			}

			i.fixture.overUnderOdds = null;
		}

	}

	public void printAnalysis() {
		long start = System.currentTimeMillis();

		Utils.analysys(predictions, "All max odds", false);

		// byBookmaker(predictions);
		// byLine();

		ArrayList<Stats> stats = new ArrayList<>();
		System.out.println();
		for (int i = 0; i < 5; i++)
			stats.add(valueOverPinnacle(false, 1.0f + i * 0.01f));

		System.out.println();
		for (int i = 0; i < 5; i++)
			stats.add(valueOverPinnacle(true, 1.0f + i * 0.01f));

		stats.sort(Comparator.comparing(Stats::getPvalueOdds).reversed());

		Stats best = stats.get(0);
		Utils.analysys(best.all, best.description, false);
		byBookieContent(best.all);
		System.out.println(best.all);

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec for analysis");

	}

	private Stats valueOverPinnacle(boolean withPrediction, float valueThreshold) {
		ArrayList<FinalEntry> finals = predictions.stream().filter(fe -> fe.prediction != 0.5f)
				.map(fe -> fe.getValueBetOverPinnacle(withPrediction, valueThreshold)).filter(fe -> fe != null)
				.collect(Collectors.toCollection(ArrayList::new));
		Stats stats = new Stats(finals,
				"Values over pinnacle" + (withPrediction ? " with predictions > " : " > ") + valueThreshold);
		System.out.println(stats);
		return stats;
	}

	private void byBookieContent(ArrayList<FinalEntry> finals) {
		HashMap<String, ArrayList<FinalEntry>> map = new HashMap<>();

		for (FinalEntry i : finals) {
			if (i.isOver()) {
				String book = i.overOdds.bookmaker;
				if (!map.containsKey(book))
					map.put(book, new ArrayList<>());
				map.get(book).add(i);
			}

			if (i.isUnder()) {
				String book = i.underOdds.bookmaker;
				if (!map.containsKey(book))
					map.put(book, new ArrayList<>());
				map.get(book).add(i);
			}
		}

		ArrayList<Stats> stats = new ArrayList<>();
		for (Entry<String, ArrayList<FinalEntry>> i : map.entrySet())
			stats.add(new Stats(i.getValue(), i.getKey()));

		stats.sort(Comparator.comparing(Stats::getSize).reversed());
		stats.forEach(System.out::println);

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
		System.out.println("-----------------------------");
		System.out.println("By line");
		ArrayList<Stats> stats = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			final Integer intI = new Integer(i);
			ArrayList<FinalEntry> finals = predictions.stream().map(fe -> fe.getPredictionForLine(intI))
					.filter(fe -> fe != null).collect(Collectors.toCollection(ArrayList::new));
			String description = new Float(0.5f - i * 0.25f).toString();
			stats.add(new Stats(Utils.noequilibriums(finals), description));
			// System.out.println(i);
			// finals.stream().limit(10).collect(Collectors.toList()).forEach(System.out::println);
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
