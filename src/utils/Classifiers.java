package utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import constants.Constants;
import main.ExtendedFixture;
import xls.XlSUtils;

public class Classifiers {

	public static float shots(ExtendedFixture f, ArrayList<ExtendedFixture> all) {
		// The shots data from soccerway(opta) does not add the goals as shots,
		// must be added for more accurate predictions and equivalancy with
		// alleurodata
		boolean manual = Arrays.asList(Constants.MANUAL).contains(f.competition);

		float goalsWeight = 1f;

		Pair avgShotsGeneral = FixtureUtils.selectAvgShots(all, f.date, manual, goalsWeight);
		float avgHome = avgShotsGeneral.home;
		float avgAway = avgShotsGeneral.away;
		Pair avgShotsHomeTeam = FixtureUtils.selectAvgShotsHome(all, f.homeTeam, f.date, manual, goalsWeight);
		float homeShotsFor = avgShotsHomeTeam.home;
		float homeShotsAgainst = avgShotsHomeTeam.away;
		Pair avgShotsAwayTeam = FixtureUtils.selectAvgShotsAway(all, f.awayTeam, f.date, manual, goalsWeight);
		float awayShotsFor = avgShotsAwayTeam.home;
		float awayShotsAgainst = avgShotsAwayTeam.away;

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		Pair avgShotsByType = FixtureUtils.selectAvgShotsByType(all, f.date, manual, goalsWeight);
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

	/**
	 * Half time based classifier - weighted halftime >=1 and halftime >= 2
	 * 
	 * @param f
	 * @param all
	 * @param halfTimeOverOne
	 * @return
	 */
	public static float halfTime(ExtendedFixture f, ArrayList<ExtendedFixture> all, float halfTimeOverOne) {
		ArrayList<ExtendedFixture> lastHomeTeam = FixtureUtils.selectLastAll(all, f.homeTeam, 50, f.date);
		ArrayList<ExtendedFixture> lastAwayTeam = FixtureUtils.selectLastAll(all, f.awayTeam, 50, f.date);

		// float overOneAvg = (Utils.countOverHalfTime(lastHomeTeam, 1) +
		// Utils.countOverHalfTime(lastAwayTeam, 1)) / 2;
		// float overTwoAvg = (Utils.countOverHalfTime(lastHomeTeam, 2) +
		// Utils.countOverHalfTime(lastAwayTeam, 2)) / 2;
		// return overOneAvg * halfTimeOverOne + overTwoAvg * (1f -
		// halfTimeOverOne);

		float zero = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 0)
				+ Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 0)) / 2;
		float one = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 1)
				+ Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 1)) / 2;
		float two = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 2)
				+ Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 2)) / 2;
		float more = (Utils.countOverHalfTime(lastHomeTeam, 3) + Utils.countOverHalfTime(lastHomeTeam, 3)) / 2;

		return 0.05f * zero + 0.1f * one + 0.7f * two + 0.15f * more;
	}

}
