package xls;

import java.io.IOException;
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
import utils.Utils;

public class AsianUtils {

	public static float poissonAsianHome(ExtendedFixture f, HSSFSheet sheet) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonAsianHome(lambda, mu, f.line, f.asianHome);
	}

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);

			for (ExtendedFixture f : current) {

				float expectancy = poissonAsianHome(f, sheet);
				// System.out.println(f + " " + expectancy + " " + f.line);
				AsianEntry ae = new AsianEntry(f, true, f.line, expectancy);
				float pr = ae.getProfit();
				// System.out.println(ae.success() + " " + pr);
				if (expectancy > 0.8f) {
					profit += ae.getProfit();
					played++;
				}
			}

		}
		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		return profit;
	}
}
