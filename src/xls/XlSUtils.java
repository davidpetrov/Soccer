package xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.spi.CalendarDataProvider;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.TEAL;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.StringUtil;
import org.sqlite.util.StringUtils;

import constants.MinMaxOdds;
import main.ExtendedFixture;
import main.FinalEntry;
import main.Result;
import main.SQLiteJDBC;
import results.Results;
import runner.Runner;
import settings.Settings;
import utils.Utils;

public class XlSUtils {

	// public static final DateFormat format = new
	// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static int getColumnIndex(HSSFSheet sheet, String columnName) {
		Iterator<Cell> it = sheet.getRow(0).cellIterator();
		while (it.hasNext()) {
			Cell cell = it.next();
			if (cell.getStringCellValue().equals(columnName))
				return cell.getColumnIndex();
		}
		return -1;
	}

	public static ArrayList<ExtendedFixture> selectLastAll(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if ((home.equals(team) || away.equals(team)) && fdate.before(date)) {
				ExtendedFixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<ExtendedFixture> selectLastHome(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (home.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				ExtendedFixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<ExtendedFixture> selectLastAway(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (away.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				ExtendedFixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}

		}

		return Utils.getLastFixtures(results, count);
	}

	public static float basic2(ExtendedFixture f, HSSFSheet sheet, float d, float e, float z) {
		ArrayList<ExtendedFixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, f.date);
		ArrayList<ExtendedFixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, f.date);

		ArrayList<ExtendedFixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 5, f.date);
		ArrayList<ExtendedFixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 5, f.date);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG + z * BTSAVG;
	}

	public static float drawBased(ExtendedFixture f, HSSFSheet sheet) {
		float drawChance = f.drawOdds / (f.awayOdds + f.drawOdds + f.homeOdds);
		ArrayList<ExtendedFixture> all = selectToDate(sheet, f.date);
		return drawChance * Utils.countOversWhenDraw(all) + (1 - drawChance) * Utils.countOversWhenNotDraw(all);
	}

	private static ArrayList<ExtendedFixture> selectToDate(HSSFSheet sheet, Date date) {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		for (ExtendedFixture ef : selectAllAll(sheet)) {
			if (ef.date.before(date))
				result.add(ef);
		}
		return result;
	}

	public static float halfTimeOnly(ExtendedFixture f, HSSFSheet sheet, int over) {
		ArrayList<ExtendedFixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 40, f.date);
		ArrayList<ExtendedFixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 40, f.date);

		float overAVG = (Utils.countOverHalfTime(lastHomeTeam, over) + Utils.countOverHalfTime(lastAwayTeam, over)) / 2;
		return overAVG;
	}

	public static float poisson(ExtendedFixture f, HSSFSheet sheet) {
		ArrayList<ExtendedFixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, f.date);
		ArrayList<ExtendedFixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, f.date);

		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(ExtendedFixture f, HSSFSheet sheet) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, f.date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, f.date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, f.date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, f.date);

		float lambda = leagueAvgAway == 0 ? 0 : homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = leagueAvgHome == 0 ? 0 : awayAvgFor * homeAvgAgainst / leagueAvgHome;

		return Utils.poissonOver(lambda, mu);
	}

	private static float selectAvgAwayTeamAgainst(HSSFSheet sheet, String awayTeamName, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			String atname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && atname.equals(awayTeamName) && dateCell != null
					&& dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	private static float selectAvgAwayTeamFor(HSSFSheet sheet, String awayTeamName, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			String htname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && htname.equals(awayTeamName) && dateCell != null
					&& dateCell.getDateCellValue().before(date)) {
				int awaygoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += awaygoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	private static float selectAvgHomeTeamAgainst(HSSFSheet sheet, String homeTeamName, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && htname.equals(homeTeamName) && dateCell != null
					&& dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	private static float selectAvgHomeTeamFor(HSSFSheet sheet, String homeTeamName, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && htname.equals(homeTeamName) && dateCell != null
					&& dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	private static float selectAvgLeagueAway(HSSFSheet sheet, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && dateCell != null
					&& dateCell.getDateCellValue() != null && dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	private static float selectAvgLeagueHome(HSSFSheet sheet, Date date) {
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && dateCell != null
					&& dateCell.getDateCellValue() != null && dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	// selects all fixtures after the 10 matchday
	public static ArrayList<ExtendedFixture> selectAll(HSSFSheet sheet) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (selectLastAll(sheet, home, 10, fdate).size() >= 10
					&& selectLastAll(sheet, home, 10, fdate).size() >= 10) {
				ExtendedFixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}
		return results;

	}

	// selects all fixtures after the 10 matchday
	public static ArrayList<ExtendedFixture> selectAllAll(HSSFSheet sheet) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0 || row.getCell(getColumnIndex(sheet, "HomeTeam")) == null)
				continue;

			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (selectLastAll(sheet, home, 10, fdate).size() >= 1
					&& selectLastAll(sheet, home, 10, fdate).size() >= 1) {
				ExtendedFixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return results;
	}

	public static ExtendedFixture getFixture(HSSFSheet sheet, Row row) {
		ExtendedFixture f = null;
		String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
		String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
		Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
		if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && row.getCell(getColumnIndex(sheet, "FTAG")) != null
				&& row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null
				&& row.getCell(getColumnIndex(sheet, "HTHG")) != null
				&& (row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getCellType() == 0
						&& row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getCellType() == 0)) {
			int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

			int halfTimeHome = (int) row.getCell(getColumnIndex(sheet, "HTHG")).getNumericCellValue();
			int halfTimeAway = (int) row.getCell(getColumnIndex(sheet, "HTAG")).getNumericCellValue();
			float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
			float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
			float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
			float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

			// with 1X2 odds from Pinnacle

			float homeOdds, drawOdds, awayOdds;
			if (row.getCell(getColumnIndex(sheet, "PSH")) != null
					&& row.getCell(getColumnIndex(sheet, "PSH")).getCellType() == 0) {
				homeOdds = (float) row.getCell(getColumnIndex(sheet, "PSH")).getNumericCellValue();
				drawOdds = (float) row.getCell(getColumnIndex(sheet, "PSD")).getNumericCellValue();
				awayOdds = (float) row.getCell(getColumnIndex(sheet, "PSA")).getNumericCellValue();
			} else {
				homeOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxH")).getNumericCellValue();
				drawOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxD")).getNumericCellValue();
				awayOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxA")).getNumericCellValue();
			}

			f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals), sheet.getSheetName())
					.withStatus("FINISHED").withOdds(overOdds, underOdds, maxOver, maxUnder)
					.withHTResult(new Result(halfTimeHome, halfTimeAway)).with1X2Odds(homeOdds, drawOdds, awayOdds);
		}
		return f;
	}

	public static ArrayList<ExtendedFixture> selectForPrediction(HSSFSheet sheet) {
		ArrayList<ExtendedFixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0 || row.getCell(getColumnIndex(sheet, "HomeTeam")) == null)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String div = row.getCell(getColumnIndex(sheet, "Div")).getStringCellValue();

			float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
			float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
			float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
			float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

			ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(-1, -1), div);
			f.withStatus("FINISHED");
			f.withOdds(overOdds, underOdds, maxOver, maxUnder);

			results.add(f);

		}
		return results;
	}

	public static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year, float initTH)
			throws IOException {
		float bestWinPercent = 0.5f;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;

		float overOneHT = checkHalfTimeOptimal(sheet, all, year);

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] htCombos = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = poissonWeighted(f, sheet);
			htCombos[i] = (overOneHT * halfTimeOnly(f, sheet, 1) + (1f - overOneHT) * halfTimeOnly(f, sheet, 2));
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagw = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagw = true;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagHT = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagHT = true;
				flagw = false;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		if (flagw) {
			Settings s = new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 1.0f - bestBasic * 0.05f, initTH,
					initTH, initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
			return s;
		} else if (flagHT) {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 0f, initTH, initTH, initTH, bestWinPercent,
					bestProfit).withYear(year).withHT(overOneHT, 1.0f - bestBasic * 0.05f).withValue(0.9f);
		} else {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 1.0f - bestBasic * 0.05f, 0.0f, initTH, initTH,
					initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
		}

	}

	public static Settings runWithTH(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year) throws IOException {
		float best = Float.NEGATIVE_INFINITY;
		Settings bestSetts = null;
		for (int i = 0; i <= 10; i++) {
			float current = 0.4f + 0.02f * i;
			Settings set = runForLeagueWithOdds(sheet, all, year, current);
			if (set.profit > best) {
				best = set.profit;
				bestSetts = set;
			}
		}

		return bestSetts;
	}

	public static Settings runForWithTH(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year) throws IOException {

		float overOneHT = checkHalfTimeOptimal(sheet, all, year);

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] htCombos = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = poissonWeighted(f, sheet);
			htCombos[i] = (overOneHT * halfTimeOnly(f, sheet, 1) + (1f - overOneHT) * halfTimeOnly(f, sheet, 2));
		}

		float best = Float.NEGATIVE_INFINITY;
		Settings bestSetts = null;
		for (int i = 0; i <= 15; i++) {
			float current = 0.35f + 0.02f * i;
			Settings set = helperRunFor(sheet, all, year, current, overOneHT, basics, poissons, weightedPoissons,
					htCombos);
			if (set.profit > best) {
				best = set.profit;
				bestSetts = set;
			}
		}

		return bestSetts;
	}

	private static Settings helperRunFor(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year, float initTH,
			float overOneHT, float[] basics, float[] poissons, float[] weightedPoissons, float[] htCombos) {

		float bestWinPercent = 0.5f;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagw = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagw = true;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagHT = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagHT = true;
				flagw = false;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		if (flagw) {
			Settings s = new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 1.0f - bestBasic * 0.05f, initTH,
					initTH, initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
			return s;
		} else if (flagHT) {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 0f, initTH, initTH, initTH, bestWinPercent,
					bestProfit).withYear(year).withHT(overOneHT, 1.0f - bestBasic * 0.05f).withValue(0.9f);
		} else {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 1.0f - bestBasic * 0.05f, 0.0f, initTH, initTH,
					initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
		}
	}

	private static Settings aggregateRun(ArrayList<HSSFSheet> sheets, ArrayList<ArrayList<ExtendedFixture>> byYear,
			int start, int end) {
		int size = end - start + 1;

		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;

		Settings best = null;

		// float overOneHT = aggregateHalfTimeOptimal(sheets, byYear, start,
		// end);
		// overOneHT = 0.5f;

		if (byYear.isEmpty())
			return null;

		int maxSize = -1;
		for (ArrayList<ExtendedFixture> i : byYear)
			if (i.size() > maxSize) {
				maxSize = i.size();
			}

		float[][] basics = new float[size][maxSize];
		float[][] poissons = new float[size][maxSize];
		float[][] weightedPoissons = new float[size][maxSize];
		float[][] htCombos = new float[size][maxSize];
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < byYear.get(j).size(); i++) {
				ExtendedFixture f = byYear.get(j).get(i);

				basics[j][i] = basic2(f, sheets.get(j), 0.6f, 0.3f, 0.1f);
				poissons[j][i] = poisson(f, sheets.get(j));
				weightedPoissons[j][i] = poissonWeighted(f, sheets.get(j));
				htCombos[j][i] = (0.5f * halfTimeOnly(f, sheets.get(j), 1)
						+ (1f - 0.5f) * halfTimeOnly(f, sheets.get(j), 2));
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheets.get(0).getSheetName(), x * 0.05f, y * 0.05f, 0.0f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				finals = runWithSettingsList(sheets.get(i), byYear.get(i), set);

				profit += Utils.getProfit(finals, set);
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheets.get(0).getSheetName(), x * 0.05f, 0.0f, y * 0.05f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				finals = runWithSettingsList(sheets.get(i), byYear.get(i), set);

				profit += Utils.getProfit(finals, set);
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheets.get(0).getSheetName(), x * 0.05f, 0.0f, 0.0f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f).withHT(0.5f, y * 0.05f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				finals = runWithSettingsList(sheets.get(i), byYear.get(i), set);

				profit += Utils.getProfit(finals, set);
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}
		}

		return best;
	}

	public static float singleMethod(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year) {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		// for (int i = 0; i < all.size(); i++) {
		// ExtendedFixture f = all.get(i);
		// float finalScore = halfTimeOnly(f, sheet, 2);
		//
		// FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
		// new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f,
		// 0.55f, 0.55f);
		// if (!fe.prediction.equals(Float.NaN))
		// finals.add(fe);
		// }

		finals = intersectAllClassifier(sheet, all, year, 0f, 0f, 0f, 0f, 0f);

		// float current = Utils.getSuccessRate(finals);
		// System.out.println(current);
		float bestthold = finals.get(0).threshold;
		Settings set = new Settings(sheet.getSheetName(), 1f, 0f, 0f, bestthold, bestthold, bestthold, 0, 0);
		set = findIntervalReal(finals, year, set);
		finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), set);
		set = findThreshold(sheet, finals, set);

		// set = trustInterval(sheet, finals, set);
		float currentProfit = Utils.getProfit(finals, set);
		return currentProfit;
	}

	public static ArrayList<FinalEntry> intersectAllClassifier(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			int year, float basicThreshold, float poissonThreshold, float weightedThreshold, float htThreshold,
			float drawThreshold) {
		ArrayList<FinalEntry> finalsBasic = new ArrayList<>();
		ArrayList<FinalEntry> finalsPoisson = new ArrayList<>();
		ArrayList<FinalEntry> finalsWeighted = new ArrayList<>();
		ArrayList<FinalEntry> finalsHT2 = new ArrayList<>();
		ArrayList<FinalEntry> finalsDraw = new ArrayList<>();

		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);
			float basic = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			float poisson = poisson(f, sheet);
			float weighted = poissonWeighted(f, sheet);
			float ht2 = halfTimeOnly(f, sheet, 2);
			float draw = drawBased(f, sheet);

			FinalEntry feBasic = new FinalEntry(f, basic, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					0.55f, 0.55f, 0.55f);
			FinalEntry fePoisson = new FinalEntry(f, poisson,
					new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
			FinalEntry feWeighted = new FinalEntry(f, weighted,
					new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
			FinalEntry feht2 = new FinalEntry(f, ht2, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f,
					0.55f, 0.55f);
			FinalEntry feDraw = new FinalEntry(f, draw, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					0.55f, 0.55f, 0.55f);

			if (!feBasic.prediction.equals(Float.NaN) && !fePoisson.prediction.equals(Float.NaN)
					&& !feWeighted.prediction.equals(Float.NaN) && !feht2.prediction.equals(Float.NaN)) {
				finalsBasic.add(feBasic);
				finalsPoisson.add(fePoisson);
				finalsWeighted.add(feWeighted);
				finalsHT2.add(feht2);
				finalsDraw.add(feDraw);
			}

		}
		Settings set = new Settings(sheet.getSheetName(), 1f, 1f, 1f, 0.55f, 0.55f, 0.55f, 0f, -1000f);
		for (FinalEntry fe : finalsBasic) {
			fe.threshold = basicThreshold;
			fe.lower = basicThreshold;
			fe.upper = basicThreshold;
		}

		for (FinalEntry fe : finalsPoisson) {
			fe.threshold = poissonThreshold;
			fe.lower = poissonThreshold;
			fe.upper = poissonThreshold;
		}

		for (FinalEntry fe : finalsWeighted) {
			fe.threshold = weightedThreshold;
			fe.lower = weightedThreshold;
			fe.upper = weightedThreshold;
		}

		for (FinalEntry fe : finalsHT2) {
			fe.threshold = htThreshold;
			fe.lower = htThreshold;
			fe.upper = htThreshold;
		}

		for (FinalEntry fe : finalsDraw) {
			fe.threshold = drawThreshold;
			fe.lower = drawThreshold;
			fe.upper = drawThreshold;
		}

		return Utils.intersectVotes(finalsBasic, finalsPoisson, finalsWeighted, finalsHT2, finalsDraw);

	}

	public static Settings runForLeagueWithOddsFull(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year)
			throws IOException {
		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;
		float bestWeighed = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = 0.5f * halfTimeOnly(f, sheet, 1) + 0.5f * poissonWeighted(f, sheet);
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			for (int z = 0; z < y; z++) {
				int w = y - z;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 0; i < all.size(); i++) {
					ExtendedFixture f = all.get(i);
					float finalScore = x * 0.05f * basics[i] + z * 0.05f * poissons[i] + w * weightedPoissons[i];

					FinalEntry fe = new FinalEntry(f, finalScore,
							new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
					if (!fe.prediction.equals(Float.NaN))
						finals.add(fe);
				}

				float current = Utils.getSuccessRate(finals);
				if (current > bestWinPercent) {
					bestWinPercent = current;
				}

				Settings set = new Settings(sheet.getSheetName(), x * 0.05f, z * 0.05f, w * 0.05f, 0.55f, 0.55f, 0.55f,
						bestWinPercent, bestProfit);
				float currentProfit = Utils.getProfit(finals, set);
				if (currentProfit > bestProfit) {
					bestProfit = currentProfit;
					bestBasic = x;
					bestPoisson = z;
					bestWeighed = w;
				}
			}
		}

		// System.out.println("Best profit found by find xy " + bestProfit);
		return new Settings(sheet.getSheetName(), bestBasic * 0.05f, bestPoisson * 0.05f, bestWeighed * 0.05f, 0.55f,
				0.55f, 0.55f, bestWinPercent, bestProfit).withYear(year);

	}

	public static float checkHalfTimeOptimal(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float overOneValue = 0f;

		float[] overOnes = new float[all.size()];
		float[] overTwos = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			overOnes[i] = halfTimeOnly(f, sheet, 1);
			overTwos[i] = halfTimeOnly(f, sheet, 2);
		}

		for (int x = 0; x <= 2; x++) {
			int y = 2 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int j = 0; j < all.size(); j++) {
				ExtendedFixture f = all.get(j);
				float finalScore = x * 0.5f * overOnes[j] + y * 0.5f * overTwos[j];

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}
			Settings set = new Settings(sheet.getSheetName(), 0, 0, 0, 0.55f, 0.55f, 0.55f, 0, bestProfit)
					.withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				overOneValue = x * 0.5f;
			}

		}

		return overOneValue;
	}

	// TODO
	private static float aggregateHalfTimeOptimal(ArrayList<HSSFSheet> sheets,
			ArrayList<ArrayList<ExtendedFixture>> byYear, int start, int end) {

		return 0;
	}

	// public static Settings findInterval(ArrayList<ExtendedFixture> all,
	// HSSFSheet sheet, int year)
	// throws ParseException, IOException {
	// Settings initial = SQLiteJDBC.getSettings(sheet.getSheetName(), year);
	// ArrayList<FinalEntry> finals = runWithoutMinOddsSettings(sheet, all,
	// initial);
	// float profit = initial.profit;
	// for (int x = 0; x < 50; x++) {
	// float currentMin = 1.3f + x * 0.02f;
	// Settings newSetts = new Settings(initial);
	// newSetts.minOdds = currentMin;
	// for (int y = x + 1; 1.3f + y * 0.02f < 2.5f; y++) {
	// float currentMax = 1.3f + y * 0.02f;
	// Settings newSetts1 = new Settings(newSetts);
	// newSetts1.maxOdds = currentMax;
	// ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals,
	// currentMin);
	// ArrayList<FinalEntry> filtered1 = Utils.filterMaxFinals(sheet, filtered,
	// currentMax);
	// float currentProfit = Utils.getProfit(sheet, filtered1);
	//
	// if (currentProfit > profit) {
	// profit = currentProfit;
	// initial.minOdds = currentMin;
	// initial.maxOdds = currentMax;
	// initial.profit = profit;
	// initial.successRate = Utils.getSuccessRate(filtered1);
	// }
	// }
	//
	// }
	//
	// return initial;
	// }

	public static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			Settings settings) {
		ArrayList<FinalEntry> finals = calculateScores(sheet, all, settings);

		return restrict(finals, settings);
	}

	public static ArrayList<FinalEntry> calculateScores(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			Settings settings) {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (ExtendedFixture f : all) {
			// float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f,
			// 0.1f)
			// + (settings.poisson == 0 ? 0 : settings.poisson * poisson(f,
			// sheet))
			// + (settings.weightedPoisson == 0 ? 0 : settings.weightedPoisson *
			// poissonWeighted(f, sheet))
			// + (settings.htCombo == 0 ? 0
			// : settings.htCombo * (settings.halfTimeOverOne * halfTimeOnly(f,
			// sheet, 1)
			// + (1f - settings.halfTimeOverOne) * halfTimeOnly(f, sheet, 2)));

			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet) + settings.weightedPoisson * poissonWeighted(f, sheet)
					+ settings.htCombo * (settings.halfTimeOverOne * halfTimeOnly(f, sheet, 1)
							+ (1f - settings.halfTimeOverOne) * halfTimeOnly(f, sheet, 2));

			FinalEntry fe = new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					settings.threshold, settings.lowerBound, settings.upperBound);
			// if (!fe.prediction.equals(Float.NaN))
			finals.add(fe);
		}
		return finals;
	}

	public static ArrayList<FinalEntry> restrict(ArrayList<FinalEntry> finals, Settings settings) {
		ArrayList<FinalEntry> result = new ArrayList<>();
		for (FinalEntry fe : finals) {

			float gain = fe.prediction > settings.threshold ? fe.fixture.maxOver : fe.fixture.maxUnder;
			float certainty = fe.prediction > settings.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (value > settings.value && Utils.oddsInRange(gain, fe.prediction, settings)
					&& (fe.prediction >= settings.upperBound || fe.prediction <= settings.lowerBound)) {
				result.add(fe);
			}
		}
		return result;
	}

	public static ArrayList<FinalEntry> runWithoutMinOddsSettings(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			Settings settings) throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (ExtendedFixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet);
			finals.add(new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f,
					settings.lowerBound, settings.upperBound));
		}

		return finals;
	}

	public static void makePrediction(HSSFSheet odds, HSSFSheet league, ExtendedFixture f, Settings sett)
			throws IOException {

		ArrayList<ExtendedFixture> one = new ArrayList<>();

		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(MinMaxOdds.DONT));
		if (sett == null)
			return;
		float score = sett.basic * basic2(f, league, 0.6f, 0.3f, 0.1f) + sett.poisson * poisson(f, league)
				+ sett.weightedPoisson * poissonWeighted(f, league)
				+ sett.htCombo * (sett.halfTimeOverOne * halfTimeOnly(f, league, 1)
						+ (1f - sett.halfTimeOverOne) * halfTimeOnly(f, league, 2));

		float certainty = score > sett.threshold ? score : (1f - score);
		float coeff = score > sett.threshold ? f.maxOver : f.maxUnder;
		float value = certainty * coeff;

		float cot = score > sett.threshold ? (score - sett.threshold) : (sett.threshold - score);
		if (Utils.oddsInRange(coeff, score, sett) && (value > sett.value) && (cot >= 0.15f)
				&& (score >= sett.upperBound || score <= sett.lowerBound) && !dont.contains(league.getSheetName())) {
			String prediction = score > sett.threshold ? "over" : "under";
			System.err.println(league.getSheetName() + " " + f.homeTeam + " : " + f.awayTeam + " " + score + " "
					+ prediction + " " + coeff);
		} else {
			String prediction = score > sett.threshold ? "over" : "under";
			System.out.println(league.getSheetName() + " " + f.homeTeam + " : " + f.awayTeam + " " + score + " "
					+ prediction + " " + coeff);
		}

	}

	public static Settings predictionSettings(HSSFSheet sheet, int year) throws IOException {
		ArrayList<ExtendedFixture> data = selectAllAll(sheet);

		Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f);
		ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

		temp = findThreshold(sheet, finals, temp);
		finals = restrict(finals, temp);

		temp = findIntervalReal(finals, year, temp);
		finals = restrict(finals, temp);

		temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
		finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

		temp = findThreshold(sheet, finals, temp);
		finals = restrict(finals, temp);

		temp = findIntervalReal(finals, year, temp);
		finals = restrict(finals, temp);

		temp = findValue(finals, sheet, temp);

		return temp;
	}

	public static Settings optimalSettings(HSSFSheet sheet, int year) throws IOException {
		ArrayList<ExtendedFixture> data = selectAllAll(sheet);

		Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f);

		float initpr = temp.profit;

		ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

		System.out.println("Run for? " + initpr + " == " + Utils.getProfit(finals, temp));

		temp = findThreshold(sheet, finals, temp);
		float th1 = temp.profit;
		finals = restrict(finals, temp);
		System.out.println("Th1: " + temp.profit + "==" + Utils.getProfit(finals, temp));

		temp = findIntervalReal(finals, year, temp);
		float pr = temp.profit;
		finals = restrict(finals, temp);
		System.out.println("Interval? " + pr + " == " + Utils.getProfit(finals, temp));
		System.out.println("Under over breakdown? " + Utils.getProfit(finals, temp) + " == "
				+ (Utils.getProfit(Utils.onlyOvers(finals)) + Utils.getProfit(Utils.onlyUnders(finals))));

		temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);

		float initpr2 = temp.profit;

		finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

		System.out.println("Run for2? " + initpr2 + " == " + Utils.getProfit(finals, temp));

		// System.out.println(temp);
		temp = findThreshold(sheet, finals, temp);
		finals = restrict(finals, temp);

		System.out.println("Thold: " + temp.profit + "==" + Utils.getProfit(finals, temp));
		// temp = trustInterval(sheet, finals, temp);
		// System.out.println(temp);

		temp = findValue(finals, sheet, temp);
		float val = temp.profit;
		finals = restrict(finals, temp);

		System.out.println("value " + val + " == " + Utils.getProfit(finals, temp));

		temp = findIntervalReal(finals, year, temp);
		float pr2 = temp.profit;
		finals = restrict(finals, temp);

		System.out.println("Interval? " + pr2 + " == " + Utils.getProfit(finals, temp));
		temp = findValue(finals, sheet, temp);

		temp.successRate = Utils.getSuccessRate(finals);
		// System.out.println(temp);
		// temp = findValue(finals, sheet, year, temp);

		// temp = findIntervalReal(finals, sheet, year, temp);

		// System.out.println("======================================================");
		// finals = runWithSettingsList(sheet, data, temp);
		// Utils.overUnderStats(finals);
		return temp;
	}

	public static Settings aggregateOptimals(int start, int end, String league) throws IOException {
		float bestProfit = Float.NEGATIVE_INFINITY;
		Settings set = new Settings(league, 0.6f, 0.2f, 0.2f, 0.55f, 0.55f, 0.55f, 0.5f, bestProfit);

		ArrayList<ArrayList<ExtendedFixture>> byYear = new ArrayList<>();
		ArrayList<HSSFSheet> sheets = new ArrayList<>();
		for (int year = start; year <= end; year++) {
			String base = new File("").getAbsolutePath();
			FileInputStream file = new FileInputStream(
					new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet(league);

			byYear.add(selectAllAll(sheet));
			sheets.add(sheet);

			workbook.close();
			file.close();
		}

		return aggregateRun(sheets, byYear, start, end);

	}

	public static Settings predictivePower(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int maxMatchDay, int year) {

		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		Settings best = null;

		// float overOneHT = checkHalfTimeOptimal(sheet, all, year);

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] htCombos = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = poissonWeighted(f, sheet);
			htCombos[i] = (0.5f * halfTimeOnly(f, sheet, 1) + (1f - 0.5f) * halfTimeOnly(f, sheet, 2));
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals, set);

			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0.0f, y * 0.05f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals, set);

			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			float profit = 0f;

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0.0f, 0.0f, 0.55f, 0.55f, 0.55f,
					bestWinPercent, bestProfit).withValue(0.9f).withHT(0.5f, y * 0.05f);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals, set);

			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}

		}

		return best;
	}

	private static Settings findPredictiveThreshold(HSSFSheet sheet, ArrayList<FinalEntry> finals, int maxDay,
			Settings initial) {
		if (finals.isEmpty())
			return new Settings(initial).withYear(initial.year);
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		float bestProfit = initial.profit;
		float bestThreshold = initial.threshold;

		for (int i = 0; i <= 40; i++) {
			float profit = 0.0f;
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;

			for (int day = 11; day < maxDay; day++) {
				ArrayList<ExtendedFixture> currentEntries = Utils.getByMatchday(Utils.onlyFixtures(finals), day);
				ArrayList<FinalEntry> finalEntries = runWithSettingsList(sheet, currentEntries, trset);
				profit += Utils.getProfit(finalEntries, trset);

			}

			if (profit > bestProfit) {
				bestProfit = profit;
				bestThreshold = current;
			}

		}

		trset.profit = bestProfit;
		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;

		for (FinalEntry fe : finals) {
			fe.threshold = bestThreshold;
			fe.lower = bestThreshold;
			fe.upper = bestThreshold;
		}
		return trset;
	}

	public static float realisticPredictive(HSSFSheet sheet, int year) throws IOException {
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);

			float minOdds = MinMaxOdds.getMinOdds(sheet.getSheetName());
			float maxOdds = MinMaxOdds.getMaxOdds(sheet.getSheetName());

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);

			data = Utils.filterByOdds(data, minOdds, maxOdds);

			Settings temp = predictivePower(sheet, data, i - 1, year);

			// System.out.println("before " + temp.profit);

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			temp = findValue(finals, sheet, temp);
			finals = runWithSettingsList(sheet, data, temp);
			temp = findPredictiveThreshold(sheet, finals, i - 1, temp);
			// System.out.println("after" + temp.profit);

			temp = findIntervalReal(finals, year, temp);
			finals = runWithSettingsList(sheet, data, temp);
			// temp = findThreshold(sheet, finals, temp);
			// temp = findIntervalReal(finals, sheet, year, temp);
			// finals = runWithSettingsList(sheet, data, temp);
			temp = findValue(finals, sheet, temp);
			//
			finals = runWithSettingsList(sheet, current, temp);

			float trprofit = Utils.getProfit(finals, temp);
			profit += trprofit;
		}
		return profit;
	}

	public static float realisticRun(HSSFSheet sheet, int year) throws IOException {
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 1, 1);
			// if (!current.isEmpty() &&
			// current.get(0).date.after(cal.getTime())) {
			// return profit;
			// }

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			// data = Utils.filterByOdds(data, minOdds, maxOdds);
			Settings temp = runForLeagueWithOdds(sheet, data, year,
					0.55f) /* runWithTH(sheet, data, year) */;
			// System.out.println("match " + i + temp);
			// temp.maxOdds = maxOdds;
			// temp.minOdds = minOdds;

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);
			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);
			temp = findValue(finals, sheet, temp);
			// System.out.println(temp);

			// temp = findIntervalReal(finals, sheet, year, temp);
			// current = Utils.filterByOdds(current, minOdds, maxOdds);

			// ArrayList<FinalEntry> prev = calculateScores(sheet, current,
			// SQLiteJDBC.getSettings(sheet.getSheetName(), year, 1));

			finals = runWithSettingsList(sheet, current, temp);

			// finals = Utils.intersectDiff(finals, prev);
			// finals = Utils.intersectDiff(finals,
			// intersectAllClassifier(sheet, current, year));

			// System.out.println(finals);
			float trprofit = Utils.getProfit(finals, temp);
			profit += trprofit;
		}

		return profit;
	}

	public static float realisticByTeam(HSSFSheet sheet, int year) throws IOException {
		Map<String, Integer> played = new HashMap<>();
		Map<String, Integer> success = new HashMap<>();
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 1, 1);
			// if (!current.isEmpty() &&
			// current.get(0).date.after(cal.getTime())) {
			// return profit;
			// }

			float minOdds = MinMaxOdds.getMinOdds(sheet.getSheetName());
			float maxOdds = MinMaxOdds.getMaxOdds(sheet.getSheetName());

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			// data = Utils.filterByOdds(data, minOdds, maxOdds);
			Settings temp = runForLeagueWithOdds(sheet, data, year,
					0.55f) /* runWithTH(sheet, data, year) */;
			// System.out.println("match " + i + temp);
			// temp.maxOdds = maxOdds;
			// temp.minOdds = minOdds;

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);
			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);
			temp = findValue(finals, sheet, temp);
			// System.out.println(temp);

			// temp = findIntervalReal(finals, sheet, year, temp);
			// current = Utils.filterByOdds(current, minOdds, maxOdds);

			ArrayList<FinalEntry> prev = calculateScores(sheet, current,
					SQLiteJDBC.getSettings(sheet.getSheetName(), year, 1));

			finals = runWithSettingsList(sheet, current, temp);

			finals = Utils.ratioRestrict(finals, played, success);

			for (FinalEntry fi : finals) {
				if (!played.containsKey(fi.fixture.homeTeam))
					played.put(fi.fixture.homeTeam, 0);
				if (!played.containsKey(fi.fixture.awayTeam))
					played.put(fi.fixture.awayTeam, 0);
				if (!success.containsKey(fi.fixture.homeTeam))
					success.put(fi.fixture.homeTeam, 0);
				if (!success.containsKey(fi.fixture.awayTeam))
					success.put(fi.fixture.awayTeam, 0);

				int curr = played.get(fi.fixture.homeTeam);
				played.put(fi.fixture.homeTeam, curr + 1);
				if (fi.success()) {
					int succ = success.get(fi.fixture.homeTeam);
					success.put(fi.fixture.homeTeam, succ + 1);
				}

				int currAway = played.get(fi.fixture.awayTeam);
				played.put(fi.fixture.awayTeam, currAway + 1);
				if (fi.success()) {
					int succAway = success.get(fi.fixture.awayTeam);
					success.put(fi.fixture.awayTeam, succAway + 1);
				}

			}

			finals = Utils.intersectDiff(finals, prev);
			// finals = Utils.intersectDiff(finals,
			// intersectAllClassifier(sheet, current, year));

			// System.out.println(finals);
			float trprofit = Utils.getProfit(finals, temp);
			// System.out.println(i + " " + trprofit);
			// System.out.println("--------------------------");
			profit += trprofit;
		}

		// TreeMap<String, Float> ratios = new TreeMap<>();
		// for (String i : played.keySet())
		// ratios.put(i, success.get(i) == 0 ? 0f : ((float) success.get(i) /
		// played.get(i)));

		// ratios.forEach((team, ratio) -> System.out.println(team + " " +
		// Results.format(ratio)));

		return profit;
	}

	public static float realisticFromDB(HSSFSheet sheet, int year) throws IOException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		HashMap<ExtendedFixture, Float> basics = SQLiteJDBC.selectScores(all, "BASICS", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> poissons = SQLiteJDBC.selectScores(all, "POISSON", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> weighted = SQLiteJDBC.selectScores(all, "WEIGHTED", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> ht1 = SQLiteJDBC.selectScores(all, "HALFTIME1", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> ht2 = SQLiteJDBC.selectScores(all, "HALFTIME2", year, sheet.getSheetName());

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 24; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			Settings temp = runForLeagueWithOdds(sheet, data, year, basics, poissons, weighted, ht1, ht2, 0.55f)
					.withValue(0.9f);

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = findValue(finals, sheet, temp);

			finals = runWithSettingsList(sheet, current, temp);

			// finals = Utils.certaintyRestrict(finals, 0.6f);
			finals = Utils.cotRestrict(finals, 0.10f);
			played += finals.size();

			float trprofit = Utils.getProfit(finals, temp);

			profit += trprofit;

		}
		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));
		return profit;
	}

	public static float realisticAggregate(HSSFSheet sheet, int year, int period) throws IOException {
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 1, 1);
			// if (!current.isEmpty() &&
			// current.get(0).date.after(cal.getTime())) {
			// return profit;
			// }

			float minOdds = MinMaxOdds.getMinOdds(sheet.getSheetName());
			float maxOdds = MinMaxOdds.getMaxOdds(sheet.getSheetName());

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			// data = Utils.filterByOdds(data, minOdds, maxOdds);
			Settings temp = SQLiteJDBC.getSettings(sheet.getSheetName(), year, period);
			// System.out.println("match " + i + temp);
			// temp.maxOdds = maxOdds;
			// temp.minOdds = minOdds;

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			temp = findIntervalReal(finals, year, temp);
			finals = runWithSettingsList(sheet, data, temp);
			temp = findThreshold(sheet, finals, temp);
			temp = findIntervalReal(finals, year, temp);
			finals = runWithSettingsList(sheet, data, temp);
			temp = findValue(finals, sheet, temp);
			// System.out.println(temp);

			// temp = findIntervalReal(finals, sheet, year, temp);
			// current = Utils.filterByOdds(current, minOdds, maxOdds);
			finals = runWithSettingsList(sheet, current, temp);

			// finals = Utils.intersectDiff(finals,
			// intersectAllClassifier(sheet, current, year));

			// System.out.println(finals);
			float trprofit = Utils.getProfit(finals, temp);
			// System.out.println(i + " " + trprofit);
			// System.out.println("--------------------------");
			profit += trprofit;
		}
		return profit;
	}

	private static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<ExtendedFixture> all, int year,
			HashMap<ExtendedFixture, Float> basicMap, HashMap<ExtendedFixture, Float> poissonsMap,
			HashMap<ExtendedFixture, Float> weightedMap, HashMap<ExtendedFixture, Float> ht1Map,
			HashMap<ExtendedFixture, Float> ht2Map, float initTH) {

		boolean escapeFlag = sheet.getSheetName().equals("D1") || sheet.getSheetName().equals("D2");

		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;

		float overOneHT = checkHalfTimeOptimal(sheet, all, year);

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] htCombos = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);
			String homeTeam = escapeFlag && f.homeTeam.contains("\'") ? f.homeTeam.replace("\'", "\\") : f.homeTeam;
			String awayTeam = escapeFlag && f.awayTeam.contains("\'") ? f.awayTeam.replace("\'", "\\") : f.awayTeam;

			ExtendedFixture key = new ExtendedFixture(f.date, homeTeam, awayTeam, new Result(-1, -1), f.competition);

			basics[i] = basicMap.get(key) == null ? Float.NaN : basicMap.get(key);
			poissons[i] = poissonsMap.get(key) == null ? Float.NaN : poissonsMap.get(key);
			weightedPoissons[i] = weightedMap.get(key) == null ? Float.NaN : weightedMap.get(key);
			float ht1 = ht1Map.get(key) == null ? Float.NaN : ht1Map.get(key);
			float ht2 = ht2Map.get(key) == null ? Float.NaN : ht2Map.get(key);
			htCombos[i] = (overOneHT * ht1 + (1f - overOneHT) * ht2);

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagw = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagw = true;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		boolean flagHT = false;
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.maxOver : f.maxUnder;
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals, set);
			if (currentProfit > bestProfit) {
				flagHT = true;
				flagw = false;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}

		if (flagw) {
			Settings s = new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 1.0f - bestBasic * 0.05f, initTH,
					initTH, initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
			return s;
		} else if (flagHT) {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 0f, initTH, initTH, initTH, bestWinPercent,
					bestProfit).withYear(year).withHT(overOneHT, 1.0f - bestBasic * 0.05f).withValue(0.9f);
		} else {
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 1.0f - bestBasic * 0.05f, 0.0f, initTH, initTH,
					initTH, bestWinPercent, bestProfit).withYear(year).withValue(0.9f);
		}

	}

	public static ArrayList<FinalEntry> triples(HSSFSheet sheet, int year) throws IOException, InterruptedException {
		ArrayList<FinalEntry> toBet = new ArrayList<>();

		ArrayList<ExtendedFixture> all = selectAllAll(sheet);

		HashMap<ExtendedFixture, Float> basics = SQLiteJDBC.selectScores(all, "BASICS", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> poissons = SQLiteJDBC.selectScores(all, "POISSON", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> weighted = SQLiteJDBC.selectScores(all, "WEIGHTED", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> ht1 = SQLiteJDBC.selectScores(all, "HALFTIME1", year, sheet.getSheetName());
		HashMap<ExtendedFixture, Float> ht2 = SQLiteJDBC.selectScores(all, "HALFTIME2", year, sheet.getSheetName());

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			Settings temp = runForLeagueWithOdds(sheet, data, year, basics, poissons, weighted, ht1, ht2, 0.55f)
					.withValue(0.9f);

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(sheet, finals, temp);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = findValue(finals, sheet, temp);

			finals = runWithSettingsList(sheet, current, temp);

			finals = Utils.cotRestrict(finals, 0.15f);
			toBet.addAll(finals);
		}
		return toBet;
	}

	public static float realisticIntersect(HSSFSheet sheet, int year) throws IOException {
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);
		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 11; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);

			float minOdds = MinMaxOdds.getMinOdds(sheet.getSheetName());
			float maxOdds = MinMaxOdds.getMaxOdds(sheet.getSheetName());

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			data = Utils.filterByOdds(data, minOdds, maxOdds);
			// ------------------------------------------------------------
			// ArrayList<FinalEntry> finals = intersectAllClassifier(sheet,
			// data, year);
			ArrayList<FinalEntry> finalsBasic = new ArrayList<>();
			ArrayList<FinalEntry> finalsPoisson = new ArrayList<>();
			ArrayList<FinalEntry> finalsWeighted = new ArrayList<>();
			ArrayList<FinalEntry> finalsHT2 = new ArrayList<>();
			ArrayList<FinalEntry> finalsDraw = new ArrayList<>();

			for (int j = 0; j < data.size(); j++) {
				ExtendedFixture f = data.get(j);
				float basic = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
				float poisson = poisson(f, sheet);
				float weighted = poissonWeighted(f, sheet);
				float ht2 = halfTimeOnly(f, sheet, 2);
				float draw = drawBased(f, sheet);

				FinalEntry feBasic = new FinalEntry(f, basic,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				FinalEntry fePoisson = new FinalEntry(f, poisson,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				FinalEntry feWeighted = new FinalEntry(f, weighted,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				FinalEntry feht2 = new FinalEntry(f, ht2, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
						0.55f, 0.55f, 0.55f);
				FinalEntry feDraw = new FinalEntry(f, draw, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
						0.55f, 0.55f, 0.55f);

				if (!feBasic.prediction.equals(Float.NaN) && !fePoisson.prediction.equals(Float.NaN)
						&& !feWeighted.prediction.equals(Float.NaN) && !feht2.prediction.equals(Float.NaN)) {
					finalsBasic.add(feBasic);
					finalsPoisson.add(fePoisson);
					finalsWeighted.add(feWeighted);
					finalsHT2.add(feht2);
					finalsDraw.add(feDraw);
				}

			}
			Settings set = new Settings(sheet.getSheetName(), 1f, 1f, 1f, 0.55f, 0.55f, 0.55f, 0f, -1000f);
			Settings basicThreshold = findThreshold(sheet, finalsBasic, set);
			Settings basicPoisson = findThreshold(sheet, finalsPoisson, set);
			Settings basicWeighted = findThreshold(sheet, finalsWeighted, set);
			Settings basicHT2 = findThreshold(sheet, finalsHT2, set);
			Settings basicDraw = findThreshold(sheet, finalsDraw, set);
			ArrayList<FinalEntry> finals;
			// ArrayList<FinalEntry> finals = Utils.intersectVotes(finalsBasic,
			// finalsPoisson, finalsWeighted);

			// ------------------------------------------------------------
			// Settings temp = findThreshold(sheet, finals,
			// new Settings(sheet.getSheetName(), 1f, 0f, 0f, 0.55f, 0.55f,
			// 0.55f, 1, 10, 0, -100f));

			current = Utils.filterByOdds(current, minOdds, maxOdds);
			finals = intersectAllClassifier(sheet, current, year, basicThreshold.threshold, basicPoisson.threshold,
					basicWeighted.threshold, basicHT2.threshold, basicDraw.threshold);
			Settings temp = new Settings(sheet.getSheetName(), 1f, 0f, 0f, 0.55f, 0.55f, 0.55f, 0, 0);
			float trprofit = Utils.getProfit(finals, temp);
			profit += trprofit;
		}
		return profit;
	}

	private static Settings trustInterval(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial) {
		// System.out.println("===========================");
		// System.out.println(
		// "lower: " + initial.lowerBound + " upper: " + initial.upperBound + "
		// profit: " + initial.profit);
		Settings trset = new Settings(initial).withValue(initial.value);

		finals.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {

				return o2.prediction.compareTo(o1.prediction);
			}
		});

		Settings set = new Settings(initial).withValue(initial.value);
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestUpper = initial.upperBound;

		ArrayList<FinalEntry> sofar = new ArrayList<>();
		float current = initial.threshold + 0.3f;
		set.upperBound = current;

		for (FinalEntry fe : finals) {
			if (current < initial.threshold)
				break;
			if (fe.prediction >= current) {
				sofar.add(fe);
			} else {
				set.upperBound = current;
				float currentProfit = Utils.getProfit(sofar, set);
				if (currentProfit > bestProfit) {
					bestProfit = currentProfit;
					bestUpper = current;
				}
				current -= 0.025d;
			}
		}

		float bestProfitLower = Float.NEGATIVE_INFINITY;
		float bestLower = initial.lowerBound;

		ArrayList<FinalEntry> sofarLower = Utils.underPredictions(finals, set);
		current = initial.threshold;
		set.lowerBound = current;

		for (FinalEntry fe : finals) {
			if (current < initial.threshold - 0.3f)
				break;
			if (fe.prediction >= current) {
				continue;
			} else {
				set.lowerBound = current;
				sofarLower = Utils.underPredictions(sofarLower, set);
				float currentProfit = Utils.getProfit(sofarLower, set);
				if (currentProfit > bestProfitLower) {
					bestProfitLower = currentProfit;
					bestLower = current;
				}
				current -= 0.025d;
			}
		}

		trset.upperBound = bestUpper;
		trset.lowerBound = bestLower;
		float bestFinalProfit = Utils.getProfit(Utils.filterTrust(finals, trset), trset);
		if (bestFinalProfit >= trset.profit) {
			trset.profit = bestFinalProfit;
		} else {
			trset = initial;
		}

		for (FinalEntry fe : finals) {
			fe.threshold = trset.threshold;
			fe.lower = trset.lowerBound;
			fe.upper = trset.upperBound;
			fe.value = trset.value;
		}

		// System.out.println("lower: " + trset.lowerBound + " upper: " +
		// trset.upperBound + " profit: " + trset.profit);
		return trset.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo).withValue(initial.value);
	}

	public static Settings findThreshold(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial) {
		// System.out.println("thold: " + initial.threshold + " profit: " +
		// initial.profit);
		if (finals.isEmpty())
			return new Settings(initial).withYear(initial.year);
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		float bestProfit = initial.profit;
		float bestThreshold = initial.threshold;

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<FinalEntry> result = restrict(finals, trset);
			float currentProfit = Utils.getProfit(result, trset);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestThreshold = current;
			}
		}

		trset.profit = bestProfit;
		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;

		// finals = restrict(finals, trset);

		for (FinalEntry fe : finals) {
			fe.threshold = bestThreshold;
			fe.lower = bestThreshold;
			fe.upper = bestThreshold;
			fe.value = trset.value;
		}

		finals = restrict(finals, trset);
		// System.out.println("thold: " + trset.threshold + " profit: " +
		// trset.profit + " lower: " + trset.lowerBound
		// + " upper: " + trset.upperBound);
		return trset;

	}

	private static int addMatchDay(HSSFSheet sheet, ArrayList<ExtendedFixture> all) {
		int max = -1;
		for (ExtendedFixture f : all) {
			if (f.matchday == -1 || f.matchday == 0) {
				f.matchday = selectLastAll(sheet, f.homeTeam, 50, f.date).size() + 1;
				if (f.matchday > max)
					max = f.matchday;
			}
		}
		return max;
	}

	private static Settings findIntervalReal(ArrayList<FinalEntry> finals, int year, Settings initial) {
		Settings underSetts = new Settings(initial);
		ArrayList<FinalEntry> unders = Utils.onlyUnders(finals);
		float profitUnders = Utils.getProfit(unders, underSetts);
		underSetts.profit = profitUnders;

		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterByOdds(unders, currentMin, 10f, initial.threshold);
			float currentProfit = Utils.getProfit(filtered, underSetts);

			if (currentProfit > profitUnders) {
				bestminx = x;
				profitUnders = currentProfit;
				underSetts.minUnder = currentMin;
				underSetts.profit = profitUnders;
			}
		}

		for (int x = bestminx; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filteredMax = Utils.filterByOdds(unders, underSetts.minUnder, currentMax,
					initial.threshold);
			float currentProfit = Utils.getProfit(filteredMax, underSetts);

			if (currentProfit > profitUnders) {
				profitUnders = currentProfit;
				underSetts.maxUnder = currentMax;
				underSetts.profit = profitUnders;
			}

		}

		Settings overSetts = new Settings(initial);
		ArrayList<FinalEntry> overs = Utils.onlyOvers(finals);
		float profitOvers = Utils.getProfit(overs, overSetts);
		overSetts.profit = profitOvers;

		int bestminy = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterByOdds(overs, currentMin, 10f, initial.threshold);
			float currentProfit = Utils.getProfit(filtered, overSetts);

			if (currentProfit > profitOvers) {
				bestminy = x;
				profitOvers = currentProfit;
				overSetts.minOver = currentMin;
				overSetts.profit = profitOvers;
			}
		}

		for (int x = bestminy; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filteredMax = Utils.filterByOdds(overs, overSetts.minOver, currentMax,
					initial.threshold);
			float currentProfit = Utils.getProfit(filteredMax, overSetts);

			if (currentProfit > profitOvers) {
				profitOvers = currentProfit;
				overSetts.maxOver = currentMax;
				overSetts.profit = profitOvers;
			}
		}

		Settings result = new Settings(initial).withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo)
				.withValue(initial.value)
				.withMinMax(underSetts.minUnder, underSetts.maxUnder, overSetts.minOver, overSetts.maxOver);
		result.profit = underSetts.profit + overSetts.profit;
		return result;
	}

	private static Settings findIntervalAggregate(ArrayList<ArrayList<FinalEntry>> byYear, Settings initial) {

		float bestProfit = initial.profit;

		Settings best = null;

		Settings newSetts = new Settings(initial);
		ArrayList<ArrayList<FinalEntry>> unders = new ArrayList<>();
		for (ArrayList<FinalEntry> yearly : byYear)
			unders.add(Utils.onlyUnders(yearly));

		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;

			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders)
				filtered.add(Utils.filterByOdds(under, currentMin, 10f, initial.threshold));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter, newSetts);

			if (currentProfit > bestProfit) {
				bestminx = x;
				bestProfit = currentProfit;
				newSetts.minUnder = currentMin;
				newSetts.profit = bestProfit;
			}
		}

		for (int x = bestminx; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<ArrayList<FinalEntry>> filteredMax = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders)
				filteredMax.add(Utils.filterByOdds(under, newSetts.minUnder, currentMax, initial.threshold));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter, newSetts);

			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				newSetts.maxUnder = currentMax;
				newSetts.profit = bestProfit;
			}
		}

		ArrayList<ArrayList<FinalEntry>> overs = new ArrayList<>();
		for (ArrayList<FinalEntry> yearly : byYear)
			overs.add(Utils.onlyOvers(yearly));
		int bestminy = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;

			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders)
				filtered.add(Utils.filterByOdds(under, currentMin, 10f, initial.threshold));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter, newSetts);

			if (currentProfit > bestProfit) {
				bestminy = x;
				bestProfit = currentProfit;
				newSetts.minUnder = currentMin;
				newSetts.profit = bestProfit;
			}
		}

		for (int x = bestminy; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<ArrayList<FinalEntry>> filteredMax = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders)
				filteredMax.add(Utils.filterByOdds(under, newSetts.minUnder, currentMax, initial.threshold));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter, newSetts);

			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				newSetts.maxUnder = currentMax;
				newSetts.profit = bestProfit;
			}
		}

		return newSetts.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo)
				.withValue(initial.value);
	}

	public static Settings findValue(ArrayList<FinalEntry> finals, HSSFSheet sheet, Settings initial) {
		float profit = initial.profit;
		Settings newSetts = new Settings(initial);
		float bestValue = 0.9f;

		for (int x = 0; x <= 30; x++) {
			float currentValue = 0.7f + x * 0.02f;
			float currentProfit = Utils.getProfit(sheet, finals, newSetts, currentValue);

			if (currentProfit > profit) {
				bestValue = currentValue;
				profit = currentProfit;
				newSetts.profit = profit;
				newSetts.value = currentValue;
			}
		}

		return newSetts.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo).withValue(bestValue);
	}

	// public static Settings aggregateInterval(int start, int end, String
	// league) throws IOException {
	// float bestProfit = Float.NEGATIVE_INFINITY;
	// float bestMinOdds = 1.0f;
	// int bestminx = 0;
	// float bestMaxOdds = 10f;
	// Settings set = new Settings(league, 0.6f, 0.2f, 0.2f, 0.55f, 0.55f,
	// 0.55f, 1, 10, 0.5f, bestProfit);
	//
	// ArrayList<ArrayList<ExtendedFixture>> byYear = new ArrayList<>();
	// ArrayList<HSSFSheet> sheets = new ArrayList<>();
	// for (int year = start; year <= end; year++) {
	// String base = new File("").getAbsolutePath();
	// FileInputStream file = new FileInputStream(
	// new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) +
	// ".xls"));
	// HSSFWorkbook workbook = new HSSFWorkbook(file);
	// HSSFSheet sheet = workbook.getSheet(league);
	//
	// byYear.add(selectAllAll(sheet));
	// sheets.add(sheet);
	//
	// workbook.close();
	// file.close();
	// }
	//
	// for (int x = 0; x < 40; x++) {
	// float currentMin = 1.3f + x * 0.025f;
	// float currentProfit = 0f;
	// for (int i = 0; i < sheets.size(); i++) {
	// HSSFSheet sheet = sheets.get(i);
	// ArrayList<ExtendedFixture> filtered = Utils.filterByOdds(byYear.get(i),
	// currentMin, 10f);
	//
	// Settings temp = runForLeagueWithOdds(sheets.get(i), filtered, start + i);
	//
	// temp.minOdds = currentMin;
	// temp.maxOdds = 10f;
	//
	// ArrayList<FinalEntry> finals = runWithSettingsList(sheet, filtered,
	// temp);
	//
	// temp = findThreshold(sheet, finals, temp);
	// temp = trustInterval(sheet, finals, temp);
	// currentProfit += Utils.getProfit(finals, temp);
	// }
	//
	// if (currentProfit > bestProfit) {
	// bestProfit = currentProfit;
	// bestMinOdds = currentMin;
	// }
	//
	// }
	//
	// for (int x = bestminx; 1.3f + x * 0.025f < 2.5f; x++) {
	// float currentMax = 1.3f + x * 0.025f;
	// float currentProfit = 0f;
	// for (int i = 0; i < sheets.size(); i++) {
	// HSSFSheet sheet = sheets.get(i);
	// ArrayList<ExtendedFixture> filtered = Utils.filterByOdds(byYear.get(i),
	// bestMinOdds, currentMax);
	//
	// Settings temp = runForLeagueWithOdds(sheets.get(i), filtered, start + i);
	//
	// temp.minOdds = bestMinOdds;
	// temp.maxOdds = currentMax;
	//
	// ArrayList<FinalEntry> finals = runWithSettingsList(sheet, filtered,
	// temp);
	//
	// temp = findThreshold(sheet, finals, temp);
	// temp = trustInterval(sheet, finals, temp);
	// currentProfit += Utils.getProfit(finals, temp);
	// }
	//
	// if (currentProfit > bestProfit) {
	// bestProfit = currentProfit;
	// bestMaxOdds = currentMax;
	// }
	//
	// }
	// set.minOdds = bestMinOdds;
	// set.maxOdds = bestMaxOdds;
	// set.profit = bestProfit;
	//
	// return set;
	// }

	public static void populateScores(int year) throws IOException {
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(
				new File(base + "\\data\\all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();

		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			ArrayList<ExtendedFixture> all = selectAllAll(sh);

			SQLiteJDBC.insertBasic(sh, all, year, "BASICS");
			SQLiteJDBC.insertBasic(sh, all, year, "POISSON");
			SQLiteJDBC.insertBasic(sh, all, year, "WEIGHTED");
			SQLiteJDBC.insertBasic(sh, all, year, "HALFTIME1");
			SQLiteJDBC.insertBasic(sh, all, year, "HALFTIME2");
		}
		workbook.close();
		file.close();
	}

}
