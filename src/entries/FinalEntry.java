package entries;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.print.attribute.HashAttributeSet;

import constants.Constants;
import main.Fixture;
import main.Result;
import odds.OverUnderOdds;
import utils.Pair;

public class FinalEntry implements Comparable<FinalEntry> {
	public Fixture fixture;
	public Float prediction;
	public float line;
	public OverUnderOdds overOdds;
	public OverUnderOdds underOdds;
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

	public FinalEntry withOdds(OverUnderOdds over, OverUnderOdds under) {
		this.overOdds = over;
		this.underOdds = under;
		return this;
	}

	public float getPrediction() {
		return prediction;
	}

	public Date getDate() {
		return fixture.getDate();
	}

	public float getCertainty() {
		return prediction > threshold ? prediction : (1f - prediction);
	}

	public float getCOT() {
		return prediction > threshold ? (prediction - threshold) : (threshold - prediction);
	}

	public float getValue() {
		float over = overOdds != null ? overOdds.getOverOdds()
				: fixture.getMaxClosingOUOddsByLine(line).get(0).overOdds;
		float under = underOdds != null ? underOdds.getUnderOdds()
				: fixture.getMaxClosingOUOddsByLine(line).get(1).underOdds;

		float gain = prediction > threshold ? over : under;
		return getCertainty() * gain;
	}

	@Override
	public String toString() {
		int totalGoals = result.goalsAwayTeam + result.goalsHomeTeam;
		String out = prediction >= upper ? "over" : "under";
		float coeff = prediction >= upper ? overOdds.getOverOdds() : underOdds.getUnderOdds();
		float valueOverPinnacle = getValueOverPinnacle();
		String bookie = prediction >= upper ? overOdds.bookmaker : underOdds.bookmaker;
		if (fixture.result.goalsHomeTeam == -1)
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + out + " " + line + " " + bookie + " " + String.format("%.2f", coeff)
					+ " " + (valueOverPinnacle > 0 ? String.format("%.2f", valueOverPinnacle) + "%" : "") + "\n";
		else
			return String.format("%.2f", prediction * 100) + " " + fixture.date + " " + fixture.homeTeam + " : "
					+ fixture.awayTeam + " " + totalGoals + " " + out + " " + line + " " + bookie + " " + successFull()
					+ " " + String.format("%.2f", getProfit()) + " "
					+ (valueOverPinnacle > 0 ? String.format("%.2f", valueOverPinnacle) + "%" : "") + "\n";
	}

	public float getValueOverPinnacle() {
		float coeff = prediction >= upper ? overOdds.getOverOdds() : underOdds.getUnderOdds();
		OverUnderOdds pinnOU = fixture.pinnOdds.get(line);
		if (pinnOU == null)
			return -1;
		OverUnderOdds trueOdds = pinnOU.getTrueOddsMarginal();
		float pinnOdds = prediction >= upper ? trueOdds.getOverOdds() : trueOdds.getUnderOdds();
		float value = coeff / pinnOdds;

		return value >= 1 ? 100 * (value - 1f) : -1f;
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

		float over = overOdds != null ? overOdds.getOverOdds()
				: fixture.getMaxClosingOUOddsByLine(line).get(0).overOdds;
		float under = underOdds != null ? underOdds.getUnderOdds()
				: fixture.getMaxClosingOUOddsByLine(line).get(1).underOdds;

		float coeff = prediction >= upper ? over : under;
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
		float coeff = prediction >= upper ? overOdds.getOverOdds() : underOdds.getUnderOdds();
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

		Fixture ff = new Fixture(fixture);
		ff.overUnderOdds = filtered;
		ff.asianOdds = fixture.asianOdds;
		ff.matchOdds = fixture.matchOdds;
		FinalEntry fe = new FinalEntry(ff, prediction, result, threshold, lower, upper);
		return fe;
	}

	// returns max clsoing odds on all alowed bookies for the given line
	public FinalEntry getPredictionForLine(int i) {
		float line = fixture.getBaseOULines().get(i);
		ArrayList<OverUnderOdds> odds = fixture.getMaxClosingOUOddsByLine(line);
		if (odds.get(0).equals(OverUnderOdds.defaultOdds()))
			return null;

		FinalEntry fe = this.withLine(line).withOdds(odds.get(0), odds.get(1));
		return fe;
	}

	public FinalEntry getValueBetOverPinnacle(boolean withPrediction, float valueThreshold) {
		float maxValue = -1f;
		OverUnderOdds maxVlaueOdds = null;
		boolean isOver = false;

		ArrayList<Float> lines = fixture.getBaseOULines();
		for (int i = 0; i < 5; i++) {
			ArrayList<OverUnderOdds> odds = fixture.getMaxClosingOUOddsByLine(lines.get(i));
			OverUnderOdds pinnOdds = fixture.getMaxCloingByLineAndBookie(lines.get(i), "Pinnacle");

			if (pinnOdds == null)
				continue;

			OverUnderOdds trueOdds = pinnOdds.getTrueOddsMarginal();
			if ((withPrediction && prediction >= 0.55f) || !withPrediction) {
				if (odds.get(0).overOdds > trueOdds.overOdds
						&& odds.get(0).overOdds / trueOdds.overOdds > valueThreshold) {
					float value = odds.get(0).overOdds / trueOdds.overOdds;
					if (value > maxValue) {
						maxVlaueOdds = odds.get(0);
						isOver = true;
					}
				}
			}

			if ((withPrediction && prediction < 0.55f) || !withPrediction) {
				if (odds.get(1).underOdds > trueOdds.underOdds
						&& odds.get(1).underOdds / trueOdds.underOdds > valueThreshold) {
					float value = odds.get(1).underOdds / trueOdds.underOdds;
					if (value > maxValue) {
						maxVlaueOdds = odds.get(1);
						isOver = false;
					}
				}
			}

		}

		FinalEntry res = maxVlaueOdds == null ? null
				: new FinalEntry(this).withLine(maxVlaueOdds.line).withOdds(
						isOver ? maxVlaueOdds : OverUnderOdds.defaultOdds(),
						isOver ? OverUnderOdds.defaultOdds() : maxVlaueOdds);

		if (!withPrediction && res != null)
			res.prediction = isOver ? 1f : 0f;

		return res;
	}

	private FinalEntry createCopyWithSingleOdds(OverUnderOdds maxVlaueOdds) {
		if (maxVlaueOdds == null)
			return null;
		ArrayList<OverUnderOdds> overUnderOdds = new ArrayList<>();
		overUnderOdds.add(maxVlaueOdds);

		Fixture ff = new Fixture(fixture);
		ff.overUnderOdds = overUnderOdds;
		ff.asianOdds = fixture.asianOdds;
		ff.matchOdds = fixture.matchOdds;

		float prediction = maxVlaueOdds.overOdds == -1f ? 0f : 1f;

		FinalEntry fe = new FinalEntry(ff, prediction, result, threshold, lower, upper).withLine(maxVlaueOdds.line);
		return fe;
	}

	public void printValueOddsHistory() {
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> byLineAndBookie = fixture.getOUByLineandBookie();
		for (Float line : fixture.getBaseOULines()) {
			ArrayList<OverUnderOdds> pinnHistory = fixture.getOddsHistoryForLineAndBookie(line, "Pinnacle");
			if (pinnHistory != null) {
				pinnHistory.sort(Comparator.comparing(OverUnderOdds::getTime));

				HashMap<String, ArrayList<OverUnderOdds>> byBookie = byLineAndBookie.get(line);

				for (Entry<String, ArrayList<OverUnderOdds>> book : byBookie.entrySet()) {
					if (book.getKey().equals("Pinnacle") || Arrays.asList(Constants.FAKEBOOKS).contains(book.getKey()))
						continue;

					ArrayList<OverUnderOdds> history = book.getValue();
					history.sort(Comparator.comparing(OverUnderOdds::getTime));
					checkForValueOverPinnacleInHistory(pinnHistory, history, 1f);
				}
			}
		}

	}

	private void checkForValueOverPinnacleInHistory(ArrayList<OverUnderOdds> pinnHistory,
			ArrayList<OverUnderOdds> history, float valueCutoff) {
		if (pinnHistory == null || history == null)
			return;

		for (OverUnderOdds ou : history) {
			OverUnderOdds pinnOdds = getActualPinnOdds(pinnHistory, ou.getTime());
			if (pinnOdds == null)
				continue;

			OverUnderOdds trueOdds = pinnOdds.getTrueOddsMarginal();
			if (ou.overOdds > trueOdds.overOdds) {
				float value = ou.overOdds / trueOdds.overOdds;
				if (value > valueCutoff)
					System.out.println(ou.bookmaker + " " + ou.time + " over " + ou.line + " at " + ou.overOdds + " "
							+ String.format("%.2f", 100 * (value - 1f)) + "%");

			}

			if (ou.underOdds > trueOdds.underOdds) {
				float value = ou.underOdds / trueOdds.underOdds;
				if (value > valueCutoff)
					System.out.println(ou.bookmaker + " " + ou.time + " under " + ou.line + " at " + ou.underOdds + " "
							+ String.format("%.2f", 100 * (value - 1f)) + "%");
			}
		}

	}

	/**
	 * Return the actual pinn odds at a given time
	 * 
	 * @param pinnHistory
	 * @param time
	 * @return
	 */
	private OverUnderOdds getActualPinnOdds(ArrayList<OverUnderOdds> pinnHistory, Date time) {
		OverUnderOdds currentOdds = null;
		for (int i = 0; i < pinnHistory.size(); i++) {
			OverUnderOdds pinnOU = pinnHistory.get(i);
			if (pinnOU.getTime().before(time)
					&& (i + 1 == pinnHistory.size() || pinnHistory.get(i + 1).getTime().after(time)))
				if (!pinnOU.isOpening)
					currentOdds = pinnOU;
		}
		return currentOdds;
	}

}