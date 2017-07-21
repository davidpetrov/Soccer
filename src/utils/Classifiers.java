package utils;

import java.util.ArrayList;

import main.ExtendedFixture;

public class Classifiers {

	public static float shots(ExtendedFixture f, ArrayList<ExtendedFixture> all) {
		Pair avgShotsGeneral = FixtureUtils.selectAvgShots(all, f.date);
		float avgHome = avgShotsGeneral.home;
		float avgAway = avgShotsGeneral.away;
		Pair avgShotsHomeTeam = FixtureUtils.selectAvgShotsHome(all, f.homeTeam, f.date);
		float homeShotsFor = avgShotsHomeTeam.home;
		float homeShotsAgainst = avgShotsHomeTeam.away;
		Pair avgShotsAwayTeam = FixtureUtils.selectAvgShotsAway(all, f.awayTeam, f.date);
		float awayShotsFor = avgShotsAwayTeam.home;
		float awayShotsAgainst = avgShotsAwayTeam.away;

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		Pair avgShotsByType = FixtureUtils.selectAvgShotsByType(all, f.date);
		float avgShotsUnder = avgShotsByType.home;
		float avgShotsOver = avgShotsByType.away;
		float expected = lambda + mu;

		float dist = avgShotsOver - avgShotsUnder;

		if (avgShotsUnder > avgShotsOver) {
			return 0.5f;
		}
		if (expected >= avgShotsOver && expected > avgShotsUnder) {
			float score = 0.5f + 0.5f * (expected - avgShotsOver) / dist;
			return (score >= 0 && score <= 1f) ? score : 1f;
		} else if (expected <= avgShotsUnder && expected < avgShotsOver) {
			float score = 0.5f - 0.5f * (-expected + avgShotsUnder) / dist;
			return (score >= 0 && score <= 1f) ? score : 0f;
		} else {
			return 0.5f;
		}
	}

}
