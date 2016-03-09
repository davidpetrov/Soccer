package xls;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import constants.MinMaxOdds;
import entries.AsianEntry;
import entries.FinalEntry;
import main.ExtendedFixture;
import main.SQLiteJDBC;
import settings.Settings;
import utils.Pair;
import utils.Utils;

public class AsianUtils {

	public static Pair poissonAsianHome(ExtendedFixture f, HSSFSheet sheet) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonAsianHome(lambda, mu, f.line, f.asianHome, f.asianAway);
	}

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);

			ArrayList<AsianEntry> finals = new ArrayList<>();
			for (ExtendedFixture f : data) {
				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, pair.away);
				if (home.expectancy >= away.expectancy)
					finals.add(home);
				else
					finals.add(away);
			}

			float bestExp = bestExpectancy(finals);

			ArrayList<AsianEntry> bets = new ArrayList<>();
			for (ExtendedFixture f : current) {

				Pair pair = poissonAsianHome(f, sheet);
				AsianEntry home = new AsianEntry(f, true, f.line, pair.home);
				AsianEntry away = new AsianEntry(f, false, f.line, pair.away);
				if (home.expectancy >= away.expectancy)
					bets.add(home);
				else
					bets.add(away);

				// System.out.println(f + " " + home.expectancy + " to " +
				// away.expectancy + " " + f.line);
				//// System.out.println(home.expectancy+away.expectancy);
				// System.out.println(home.success() + " " + home.getProfit());
				// System.out.println(away.success() + " " + away.getProfit());
			}

			bets = restrict(bets, bestExp);

			profit += getProfit(bets);
			// System.out.println("Curr: "+ profit);
			played += bets.size();

		}
		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		return profit;
	}

	private static ArrayList<AsianEntry> restrict(ArrayList<AsianEntry> bets, float f) {
		ArrayList<AsianEntry> result = new ArrayList<>();
		for (AsianEntry i : bets)
			if (i.expectancy > f)
				result.add(i);
		return result;
	}

	private static float getProfit(ArrayList<AsianEntry> bets) {
		float profit = 0f;
		for (AsianEntry i : bets)
			profit += i.getProfit();
		return profit;
	}

	private static float bestExpectancy(ArrayList<AsianEntry> finals) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float best = 0;

		for (int i = 0; i < 30; i++) {
			float current = i * 0.05f;
			float profit = 0f;
			for (AsianEntry f : finals) {
				if (f.expectancy > current)
					profit += f.getProfit();
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = current;
			}

		}
		return best;
	}
}
