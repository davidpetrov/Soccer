package utils;

import java.util.ArrayList;

import entries.FinalEntry;

public class NormalizedStats extends Stats{

	public NormalizedStats(ArrayList<FinalEntry> all, String description) {
		super(all, description);
	}
	
	public float getProfit() {
		return Utils.getNormalizedProfit(all);
	}


	public float getYield() {
		return Utils.getNormalizedYield(all);
	}

	public float getPvalueOdds() {
		return Utils.evaluateRecordNormalized(all);
	}
	
}
