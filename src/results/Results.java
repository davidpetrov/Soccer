package results;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Results {

	public static final String[] leagues = { "E0", "E1", "E2", "E3", "EC", "SC0", "SC1", "SC2", "SC3", "D1", "D2",
			"SP1", "SP2", "I1", "I2", "F1", "F2", "B1", "N1", "P1", "T1", "G1" };

	public static void eval(String name) throws IOException {
		HashMap<Integer, Map<String, Float>> results = new HashMap<>();

		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Tereza\\Desktop\\" + name + ".txt"));
		String line = br.readLine();

		while (line != null) {
			if (line.startsWith("Profit")) {
				String[] split = line.split(" ");
				String league = split[3];
				int year = Integer.parseInt(split[4]);
				float profit = Float.parseFloat(split[6].replace(",", "."));
				if (results.containsKey(year)) {
					results.get(year).put(league, profit);
				} else {
					results.put(year, new HashMap<String, Float>());
					results.get(year).put(league, profit);
				}
			}
			line = br.readLine();
		}

		br.close();
		stats(results);

		avgByLeague(results);

		restric(results);

		fullRestric(results);

	}

	public static void stats(HashMap<Integer, Map<String, Float>> results) {
		HashMap<Integer, Float> byYear = new HashMap<>();

		for (Entry<Integer, Map<String, Float>> entry : results.entrySet()) {
			float sum = entry.getValue().values().stream().reduce(0f, (a, b) -> a + b);
			byYear.put(entry.getKey(), sum);
		}

		float avg = byYear.values().stream().reduce(0f, (a, b) -> a + b) / byYear.size();

		byYear.forEach((k, v) -> System.out.println(k + " " + format(v)));
		System.out.println("Average: " + format(avg));
	}

	public static HashMap<String, Float> avgByLeague(HashMap<Integer, Map<String, Float>> results) {
		HashMap<String, Float> leagues = new HashMap<>();

		for (Entry<Integer, Map<String, Float>> entry : results.entrySet()) {
			for (Entry<String, Float> league : entry.getValue().entrySet()) {
				if (leagues.containsKey(league.getKey())) {
					float curr = leagues.get(league.getKey());
					leagues.put(league.getKey(), curr + league.getValue());
				} else {
					leagues.put(league.getKey(), league.getValue());
				}
			}
		}

		leagues.forEach((league, profit) -> System.out.println(league + " " + format(profit)));

		float fgnRestrict = leagues.values().stream().filter(v -> v >= 0).collect(Collectors.toList()).stream()
				.reduce(0f, (a, b) -> a + b) / results.size();
		// System.out.println("Full negative restrict avg: " + fgnRestrict);

		return leagues;
	}

	public static void restric(HashMap<Integer, Map<String, Float>> results) {
		HashMap<Integer, Float> byYear = new HashMap<>();
		for (Entry<Integer, Map<String, Float>> entry : results.entrySet()) {
			int year = entry.getKey();
			if (!results.containsKey(year - 1))
				continue;

			Map<String, Float> prev = results.get(year - 1);
			for (Entry<String, Float> i : prev.entrySet()) {
				if (i.getValue() >= 0f) {
					if (byYear.containsKey(year)) {
						float curr = byYear.get(year);
						byYear.put(year, curr + entry.getValue().get(i.getKey()));
					} else {
						byYear.put(year, entry.getValue().get(i.getKey()));
					}
				}

			}
		}

		float avg = byYear.values().stream().reduce(0f, (a, b) -> a + b) / byYear.size();

		byYear.forEach((k, v) -> System.out.println(k + " " + format(v)));
		System.out.println("Simple restrict average: " + format(avg));

	}

	public static void fullRestric(HashMap<Integer, Map<String, Float>> results) {
		HashMap<String, Float> competitions = new HashMap<>();
		HashMap<Integer, Float> byYear = new HashMap<>();
		HashMap<Integer, ArrayList<String>> donts = new HashMap<>();
		for (Entry<Integer, Map<String, Float>> entry : results.entrySet()) {
			int year = entry.getKey();
			donts.put(year, new ArrayList<>());
			Map<String, Float> currLeagues = entry.getValue();
			for (Entry<String, Float> i : currLeagues.entrySet()) {
				if (!competitions.containsKey(i.getKey()) || competitions.get(i.getKey()) >= 0f) {
					if (byYear.containsKey(year)) {
						float curr = byYear.get(year);
						byYear.put(year, curr + entry.getValue().get(i.getKey()));
					} else {
						byYear.put(year, entry.getValue().get(i.getKey()));
					}
				} else {
					donts.get(year).add(i.getKey());
				}
			}

			for (Entry<String, Float> i : currLeagues.entrySet()) {
				String league = i.getKey();

				if (competitions.containsKey(league)) {
					float curr = competitions.get(league);
					competitions.put(league, curr + currLeagues.get(league));
				} else {
					competitions.put(league, currLeagues.get(league));
				}
			}

		}

		float avg = byYear.values().stream().reduce(0f, (a, b) -> a + b) / byYear.size();

		byYear.forEach((k, v) -> System.out.println(k + " " + format(v) + "     " + donts.get(k)));
		System.out.println("Full restrict average: " + format(avg));

	}

	private static String format(float d) {
		return String.format("%.2f", d);
	}
}
