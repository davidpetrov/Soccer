package utils;

import java.util.ArrayList;

import entries.FinalEntry;
import settings.Settings;

public class FinalsUtils {

	public static ArrayList<FinalEntry> restrict(ArrayList<FinalEntry> finals, Settings settings) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry fe : finals) {

			float gain = fe.prediction > settings.threshold ? fe.fixture.getMaxClosingOverOdds()
					: fe.fixture.getMaxClosingUnderOdds();
			float certainty = fe.prediction > settings.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > settings.value && Utils.oddsInRange(gain, fe.prediction, settings)
					&& (fe.prediction >= settings.upperBound || fe.prediction <= settings.lowerBound)) {
				fe.lower = settings.threshold;
				fe.upper = settings.threshold;
				fe.threshold = settings.threshold;
				result.add(fe);
			} else {
				// System.out.println("skipped: " + fe);
			}
		}
		return result;
	}

}
