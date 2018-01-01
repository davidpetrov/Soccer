package entries;

import java.util.ArrayList;
import java.util.stream.Collectors;

import main.Fixture;
import main.Result;
import odds.OverUnderOdds;
import utils.Pair;

public class FinalEntry implements Comparable<FinalEntry> {
	public Fixture fixture;
	public Float prediction;
	public float line;
	public Result result;
	public float threshold;
	public float upper;
	public float lower;
	public float value;

	public FinalEntry(Fixture f, float prediction, Result result, float threshold, float lower, float upper) {
		this.fixture = f;
		this.prediction = prediction;
		this.result = result;
		this.threshold = threshold;
		this.upper = upper;
		this.lower = lower;
		this.value = 0.9f;
		this.line = 2.5f;
	}

	/**
	 * Copy constructor (doesn't copy the fixture field, not necessary for now)
	 * 
	 * @param i
	 */
	public FinalEntry(FinalEntry i) {
		this.fixture = i.fixture;
		this.prediction = i.prediction;
		this.result = new Result(i.result);
		this.threshold = i.threshold;
		this.upper = i.upper;
		this.lower = i.lower;
		this.value = i.value;
	}

	public float getPrediction() {
		return prediction;
	}

	public float getCertainty() {
		return prediction > threshold ? prediction : (1f - prediction);
	}

	public float getCOT() {
		return prediction > threshold ? (prediction - threshold) : (threshold - prediction);
	}

	public float getValue() {
		float gain = prediction > threshold ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		return getCertainty() * gain;
	}

	@Override
	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		float coeff = prediction >= upper ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		if (fixture.result.goalsHomeTeam == -1)
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + out + " " + String.format("%.2f", coeff) + "\n";
		else
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + totalGoals + " " + out + " " + successFull() + " "
					+ String.format("%.2f", getProfit()) + "\n";
	}

	public boolean isOver() {
		return prediction >= upper;
	}

	public boolean isUnder() {
		return prediction < lower;
	}

	public String successFull() {
		int result = fixture.result.goalsHomeTeam + fixture.result.goalsAwayTeam;
		float diff = result - line;
		if (line == -1f)
			return "missing  data";

		if (prediction >= upper) {
			if (diff >= 0.5f)
				return "W";
			else if (diff == 0.25f) {
				return "HW";
			} else if (diff == 0f) {
				return "D";
			} else if (diff == -0.25f) {
				return "HL";
			} else {
				return "L";
			}
		} else {
			if (diff >= 0.5f)
				return "L";
			else if (diff == 0.25f) {
				return "HL";
			} else if (diff == 0f) {
				return "D";
			} else if (diff == -0.25f) {
				return "HW";
			} else {
				return "W";
			}
		}
	}

	public boolean success() {
		String outcome = successFull();
		return outcome.equals("W") || outcome.equals("HW");
	}

	public float getProfit() {
		if (line == -1f)
			return 0;
		Pair odds = fixture.getMaxClosingOUOddsByLine(line);
		float coeff = prediction >= upper ? odds.home : odds.away;
		String success = successFull();
		if (success.equals("W")) {
			return coeff - 1;
		} else if (success.equals("HW")) {
			return (coeff - 1) / 2;
		} else if (success.equals("D")) {
			return 0f;
		} else if (success.equals("HL")) {
			return -0.5f;
		} else {
			return -1;
		}
	}

	// TODO for all lines
	public float getNormalizedProfit() {
		if (fixture.getTotalGoals() < 0)
			return 0f;
		float coeff = prediction >= upper ? fixture.getMaxClosingOverOdds() : fixture.getMaxClosingUnderOdds();
		float betUnit = 1f / (coeff - 1);
		if (success())
			return 1f;
		else
			return -betUnit;
	}

	@Override
	public int compareTo(FinalEntry o) {
		return prediction.compareTo(o.prediction);
	}

	public FinalEntry withLine(float line) {
		this.line = line;
		return this;
	}

	// filtered OU odds only from bookie
	public FinalEntry getPredictionBy(String bookie) {
		ArrayList<OverUnderOdds> filtered = fixture.overUnderOdds.stream().filter(ou -> ou.bookmaker.equals(bookie))
				.collect(Collectors.toCollection(ArrayList::new));
		// if(filtered.isEmpty())
		// System.out.println();

		Fixture ff = new Fixture(fixture);
		ff.overUnderOdds = filtered;
		ff.asianOdds = fixture.asianOdds;
		ff.matchOdds = fixture.matchOdds;
		FinalEntry fe = new FinalEntry(ff, prediction, result, threshold, lower, upper);
		return fe;
	}

	// returns max clsoing odds on all alowed bookies for the given line
	public FinalEntry getPredictionForLine(int i) {
		float line = fixture.getBaseOULines()[i];
		Pair odds = fixture.getMaxClosingOUOddsByLine(line);
		if (odds.equals(Pair.defaultValue()))
			return null;
		OverUnderOdds maxOdds = new OverUnderOdds("Max", fixture.date, line, odds.home, odds.away);

		ArrayList<OverUnderOdds> overUnderOdds = new ArrayList<>();
		overUnderOdds.add(maxOdds);

		Fixture ff = new Fixture(fixture);
		ff.overUnderOdds = overUnderOdds;
		ff.asianOdds = fixture.asianOdds;
		ff.matchOdds = fixture.matchOdds;

		FinalEntry fe = new FinalEntry(ff, prediction, result, threshold, lower, upper).withLine(line);
		return fe;
	}

}