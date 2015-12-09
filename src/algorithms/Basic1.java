package algorithms;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import main.Fixture;
import utils.Utils;

public class Basic1 extends Algorithm {
	/*
	 * Uses average of last 5 of all mathches with 40% average of last 5 of
	 * home/away matches with 40% and BTT average of last 5 of all matches with
	 * 20%
	 */

	public Basic1(Fixture fixture) throws JSONException, IOException {
		super(fixture);
	}

	@Override
	public float calculate() {
		ArrayList<Fixture> lastHomeTeam = Utils.getLastFixtures(homeSideFixtures, 10);
		ArrayList<Fixture> lastAwayTeam = Utils.getLastFixtures(awaySideFixtures, 10);
		ArrayList<Fixture> lastHomeHomeTeam = Utils.getLastFixtures(Utils.getHomeFixtures(fixture, homeSideFixtures),
				5);
		ArrayList<Fixture> lastAwayAwayTeam = Utils.getLastFixtures(Utils.getAwayFixtures(fixture, awaySideFixtures),
				5);

		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return 0.6f * allGamesAVG + 0.3f * homeAwayAVG + 0.1f * BTSAVG;
	}

}
