package xls;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import entries.HomeEntry;
import main.ExtendedFixture;
import utils.FixtureUtils;
import utils.Utils;

public class HomeUtils {

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<ExtendedFixture> all = XlSUtils.selectAll(sheet, 1);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 5; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = FixtureUtils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = FixtureUtils.getBeforeMatchday(all, i);

			ArrayList<HomeEntry> finals = new ArrayList<>();
			for (int j = 0; j < current.size(); j++) {
				ExtendedFixture f = current.get(j);
				// float score = shotsHomeWin(f, sheet);
				if (f.homeOdds >= 2.2f && f.homeOdds <= 3.7f) {
					HomeEntry d = new HomeEntry(f, true, 1f);
					finals.add(d);
				}
			}

			profit += getProfit(finals);
			// System.out.println("Curr: "+ profit);
			played += finals.size();
			System.out.println(finals);

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		// analysis(analysis);
		return profit;
	}

	public static float shotsHomeWin(ExtendedFixture f, HSSFSheet sheet) throws ParseException {

		float avgHome = XlSUtils.selectAvgShotsHome(sheet, f.date);
		float avgAway = XlSUtils.selectAvgShotsAway(sheet, f.date);
		float homeShotsFor = XlSUtils.selectAvgHomeShotsFor(sheet, f.homeTeam, f.date);
		float homeShotsAgainst = XlSUtils.selectAvgHomeShotsAgainst(sheet, f.homeTeam, f.date);
		float awayShotsFor = XlSUtils.selectAvgAwayShotsFor(sheet, f.awayTeam, f.date);
		float awayShotsAgainst = XlSUtils.selectAvgAwayShotsAgainst(sheet, f.awayTeam, f.date);

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		float diff = lambda - mu;

		float avgDiff = Utils.avgShotsDiffHomeWin(sheet, f.date);

		float ratio = diff / avgDiff;

		if (ratio < 0f)
			return 0f;
		else if (ratio >= 2)
			return 1f;
		else
			return ratio / 2;

	}

	public static float getProfit(ArrayList<HomeEntry> bets) {
		float profit = 0f;
		for (HomeEntry i : bets)
			profit += i.getProfit();
		return profit;
	}

}
