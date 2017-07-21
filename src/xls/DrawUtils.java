package xls;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import entries.AsianEntry;
import entries.DrawEntry;
import main.ExtendedFixture;
import settings.SettingsAsian;
import settings.SettingsDraws;
import utils.FixtureUtils;
import utils.Pair;
import utils.Utils;

public class DrawUtils {

	public static float poissonDraw(ExtendedFixture f, HSSFSheet sheet) throws ParseException {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonDraw(lambda, mu, 0);
	}

	public static float basic(ExtendedFixture f, HSSFSheet sheet, float d, float e) throws ParseException {
		ArrayList<ExtendedFixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, f.date);
		ArrayList<ExtendedFixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, f.date);

		ArrayList<ExtendedFixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 5, f.date);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 5, f.date);

		float allGamesAVG = (Utils.countDraws(lastHomeTeam) + Utils.countDraws(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countDraws(lastHomeHomeTeam) + Utils.countDraws(lastAwayAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG;
	}

	public static float realistic(HSSFSheet sheet, int year) throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<ExtendedFixture> all = XlSUtils.selectAllAll(sheet);

		int maxMatchDay = XlSUtils.addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = FixtureUtils.getByMatchday(all, i);
			ArrayList<ExtendedFixture> data = FixtureUtils.getBeforeMatchday(all, i);

			// SettingsDraws set = runForLeague(sheet, data, year);

			ArrayList<DrawEntry> finals = new ArrayList<>();

			for (int j = 0; j < data.size(); j++) {
				ExtendedFixture f = data.get(j);
				float score = shotsDraw(f, sheet);
				float value = score;
				if (score != 0f) {
					DrawEntry d = new DrawEntry(f, true, value);
					finals.add(d);
				}
			}

			float bestExp = bestValue(finals);

			finals = new ArrayList<>();
			for (int j = 0; j < current.size(); j++) {
				ExtendedFixture f = current.get(j);
				float score = shotsDraw(f, sheet);
				float value = score;
				if (score != 0f) {
					DrawEntry d = new DrawEntry(f, true, value);
					finals.add(d);
				}
			}

			ArrayList<DrawEntry> bets = new ArrayList<>();
			for (DrawEntry j : finals) {
				if (j.expectancy > bestExp)
					bets.add(j);
			}

			profit += getProfit(bets);
			// System.out.println("Curr: "+ profit);
			played += bets.size();

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		// analysis(analysis);
		return profit;
	}

	private static ArrayList<DrawEntry> intersect(ArrayList<DrawEntry> bets, ArrayList<DrawEntry> betsPoissons) {
		ArrayList<DrawEntry> result = new ArrayList<>();
		for (DrawEntry i : bets) {
			for (DrawEntry j : betsPoissons) {
				if (i.fixture.equals(j.fixture))
					result.add(i);
			}
		}
		return result;
	}

	private static SettingsDraws runForLeague(HSSFSheet sheet, ArrayList<ExtendedFixture> data, int year) throws ParseException {
		float bestProfit = Float.NEGATIVE_INFINITY;
		SettingsDraws best = null;

		float[] basics = new float[data.size()];
		float[] poissons = new float[data.size()];

		for (int i = 0; i < data.size(); i++) {
			basics[i] = basic(data.get(i), sheet, 0.6f, 0.4f);
			poissons[i] = poissonDraw(data.get(i), sheet);
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<DrawEntry> finals = new ArrayList<>();
			for (int i = 0; i < data.size(); i++) {
				ExtendedFixture f = data.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];
				DrawEntry ae = new DrawEntry(f, true, finalScore);
			}

			SettingsDraws set = new SettingsDraws(sheet.getSheetName(), year, x * 0.05f, y * 0.05f, 1f, 0f);

			float bestExp = bestValue(finals);

			ArrayList<DrawEntry> bets = new ArrayList<>();
			for (DrawEntry j : finals) {
				if (j.expectancy > bestExp)
					bets.add(j);
			}

			finals = bets;
			set.value = bestExp;

			float currentProfit = getProfit(bets);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}

		}

		best.profit = bestProfit;
		return best;
	}

	public static float getProfit(ArrayList<DrawEntry> bets) {
		float profit = 0f;
		for (DrawEntry i : bets)
			profit += i.getProfit();
		return profit;
	}

	private static float bestValue(ArrayList<DrawEntry> finals) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float best = 0;

		for (int i = 0; i <= 20; i++) {
			float current = 0.f + i * 0.02f;
			float profit = 0f;
			for (DrawEntry f : finals) {
				if (f.expectancy > current)
					profit += f.getProfit();
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = current;
			}

		}
		// System.out.println(bestProfit);
		return best;
	}

	private static float bestPredictiveValue(ArrayList<DrawEntry> finals, int matchaday) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float best = 0;

		for (int i = 0; i <= 20; i++) {
			float current = 0.85f + i * 0.02f;

			for (int day = 11; day < matchaday; day++) {
				float profit = 0f;
				for (DrawEntry f : finals) {
					if (f.expectancy > current)
						profit += f.getProfit();
				}

				if (profit > bestProfit) {
					bestProfit = profit;
					best = current;
				}
			}

		}
		return best;
	}

	public static float shotsDraw(ExtendedFixture f, HSSFSheet sheet) throws ParseException {
		// float avgTotal = selectAvgShotsTotal(sheet, f.date);

		float avgHome = XlSUtils.selectAvgShotsHome(sheet, f.date);
		float avgAway = XlSUtils.selectAvgShotsAway(sheet, f.date);
		float homeShotsFor = XlSUtils.selectAvgHomeShotsFor(sheet, f.homeTeam, f.date);
		float homeShotsAgainst = XlSUtils.selectAvgHomeShotsAgainst(sheet, f.homeTeam, f.date);
		float awayShotsFor = XlSUtils.selectAvgAwayShotsFor(sheet, f.awayTeam, f.date);
		float awayShotsAgainst = XlSUtils.selectAvgAwayShotsAgainst(sheet, f.awayTeam, f.date);

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		float diff = Math.abs(lambda - mu);

		// float homeAvgFor = selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		// float awayAvgFor = selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);

		// float homeRatio = homeAvgFor / homeShotsFor;
		// float awayRatio = awayAvgFor / awayShotsFor;

		// return Utils.poissonOver(homeRatio * lambda, awayRatio * mu);
		// float avgShotsUnder = AvgShotsWhenUnder(sheet, f.date);
		float avgDiff = Math.abs(avgShotsDiffDraw(sheet, f.date));

		if (diff < avgDiff)
			return 1 - diff / avgDiff;
		else
			return 0f;

	}

	private static float avgShotsDiffDraw(HSSFSheet sheet, Date date) {
		int totalHome = 0;
		int totalAway = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(XlSUtils.getColumnIndex(sheet, "Date"));
			int homegoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awaygoal = (int) row.getCell(XlSUtils.getColumnIndex(sheet, "FTAG")).getNumericCellValue();
			if (row.getCell(XlSUtils.getColumnIndex(sheet, "HST")) != null
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")) != null && dateCell != null
					&& dateCell.getDateCellValue().before(date)
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getCellType() == 0 && homegoal == awaygoal) {
				totalHome += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "HST")).getNumericCellValue();
				totalAway += (int) row.getCell(XlSUtils.getColumnIndex(sheet, "AST")).getNumericCellValue();

				count++;
			}
		}
		return count == 0 ? 0 : ((float) (totalHome - totalAway)) / count;
	}

}
