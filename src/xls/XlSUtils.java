package xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.plaf.ComponentInputMapUIResource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import constants.Constants;
import entries.AllEntry;
import entries.AsianEntry;
import entries.FinalEntry;
import entries.HTEntry;
import main.AsianLines;
import main.Fixture;
import main.Fixture;
import main.Fixture;
import main.GoalLines;
import main.PlayerFixture;
import main.Result;
import main.SQLiteJDBC;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;
import scraper.Names;
import settings.Settings;
import settings.SettingsAsian;
import settings.ShotsSettings;
import tables.Table;
import utils.FixtureListCombiner;
import utils.FixtureUtils;
import utils.LinearRegression;
import utils.Pair;
import utils.Triple;
import utils.Utils;

public class XlSUtils {

	// public static final DateFormat XLSformat = new
	// SimpleDateFormat("d.M.yyyy");

	public static int getColumnIndex(HSSFSheet sheet, String columnName) {
		Iterator<Cell> it = sheet.getRow(0).cellIterator();
		while (it.hasNext()) {
			Cell cell = it.next();
			if (cell.getStringCellValue().equals(columnName))
				return cell.getColumnIndex();
		}
		return -1;
	}

	public static ArrayList<Fixture> selectLastAll(HSSFSheet sheet, String team, int count, Date date)
			throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;

			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate;
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();

			if ((home.equals(team) || away.equals(team)) && fdate.before(date)) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<Fixture> selectLastHome(HSSFSheet sheet, String team, int count, Date date)
			throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (home.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<Fixture> selectLastAway(HSSFSheet sheet, String team, int count, Date date)
			throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (away.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}

		}

		return Utils.getLastFixtures(results, count);
	}

	public static float basic2(Fixture f, HSSFSheet sheet, float d, float e, float z) throws ParseException {
		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, f.date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, f.date);

		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 5, f.date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 5, f.date);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG + z * BTSAVG;
	}

	public static float reverseOfFortune(Fixture f, HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 50, f.date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 50, f.date);

		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeam, 25, f.date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeam, 25, f.date);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;

		float lastHome = XlSUtils.selectLastAll(sheet, f.homeTeam, 1, f.date).get(0).getTotalGoals();
		float lastAway = XlSUtils.selectLastAll(sheet, f.awayTeam, 1, f.date).get(0).getTotalGoals();

		float tendency = 0.7f * allGamesAVG + 0.3f * homeAwayAVG;
		if (tendency < 0.5f && lastHome > 2.5f && lastAway > 2.5f) {
			return 0f;

		} else if (tendency > 0.5f && lastHome < 2.5f && lastAway < 2.5f)
			return 1f;
		else
			return 0.5f;
	}

	public static float drawBased(Fixture f, HSSFSheet sheet) throws ParseException {
		float drawChance = f.getMaxClosingDrawOdds()
				/ (f.getMaxClosingAwayOdds() + f.getMaxClosingDrawOdds() + f.getMaxClosingHomeOdds());
		ArrayList<Fixture> all = selectToDate(sheet, f.date);
		return drawChance * Utils.countOversWhenDraw(all) + (1 - drawChance) * Utils.countOversWhenNotDraw(all);
	}

	private static ArrayList<Fixture> selectToDate(HSSFSheet sheet, Date date) throws ParseException {
		ArrayList<Fixture> result = new ArrayList<>();
		for (Fixture ef : selectAllAll(sheet)) {
			if (ef.date.before(date))
				result.add(ef);
		}
		return result;
	}

	public static float halfTimeOnly(Fixture f, HSSFSheet sheet, int over) throws ParseException {
		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 40, f.date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 40, f.date);

		float overAVG = (Utils.countOverHalfTime(lastHomeTeam, over) + Utils.countOverHalfTime(lastAwayTeam, over)) / 2;
		return overAVG;
	}

	public static float poisson(Fixture f, HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, f.date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, f.date);

		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(Fixture f, HSSFSheet sheet) throws ParseException {

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

	public static float shots(Fixture f, HSSFSheet sheet) throws ParseException {
		// float avgTotal = selectAvgShotsTotal(sheet, f.date);

		float avgHome = selectAvgShotsHome(sheet, f.date);
		float avgAway = selectAvgShotsAway(sheet, f.date);
		float homeShotsFor = selectAvgHomeShotsFor(sheet, f.homeTeam, f.date);
		float homeShotsAgainst = selectAvgHomeShotsAgainst(sheet, f.homeTeam, f.date);
		float awayShotsFor = selectAvgAwayShotsFor(sheet, f.awayTeam, f.date);
		float awayShotsAgainst = selectAvgAwayShotsAgainst(sheet, f.awayTeam, f.date);

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		// float homeAvgFor = selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		// float awayAvgFor = selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);

		// float homeRatio = homeAvgFor / homeShotsFor;
		// float awayRatio = awayAvgFor / awayShotsFor;

		// return Utils.poissonOver(homeRatio * lambda, awayRatio * mu);
		float avgShotsUnder = AvgShotsWhenUnder(sheet, f.date);
		float avgShotsOver = AvgShotsWhenOver(sheet, f.date);
		float expected = lambda + mu;

		float dist = avgShotsOver - avgShotsUnder;
		// System.out.println(dist);

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
			// System.out.println(f);
			return 0.5f;
		}
	}

	public static float regressionShots(Fixture f, HSSFSheet sheet) throws ParseException {
		// float avgTotal = selectAvgShotsTotal(sheet, f.date);

		ArrayList<Fixture> lastHome = selectLastAll(sheet, f.homeTeam, 50, f.date);
		ArrayList<Fixture> lastAway = selectLastAll(sheet, f.awayTeam, 50, f.date);

		LinearRegression regressionHome = Utils.getRegression(f.homeTeam, lastHome);
		LinearRegression regressionAway = Utils.getRegression(f.awayTeam, lastAway);

		float avgHome = selectAvgShotsHome(sheet, f.date);
		float avgAway = selectAvgShotsAway(sheet, f.date);
		float homeShotsFor = selectAvgHomeShotsFor(sheet, f.homeTeam, f.date);
		float homeShotsAgainst = selectAvgHomeShotsAgainst(sheet, f.homeTeam, f.date);
		float awayShotsFor = selectAvgAwayShotsFor(sheet, f.awayTeam, f.date);
		float awayShotsAgainst = selectAvgAwayShotsAgainst(sheet, f.awayTeam, f.date);

		float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
		float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

		// float homeAvgFor = selectAvgHomeTeamFor(sheet, f.homeTeam, f.date);
		// float awayAvgFor = selectAvgAwayTeamFor(sheet, f.awayTeam, f.date);

		// float homeRatio = homeAvgFor / homeShotsFor;
		// float awayRatio = awayAvgFor / awayShotsFor;

		// return Utils.poissonOver(homeRatio * lambda, awayRatio * mu);
		// float avgShotsUnder = AvgShotsWhenUnder(sheet, f.date);
		// float avgShotsOver = AvgShotsWhenOver(sheet, f.date);
		double expected = regressionHome.predict(lambda) + regressionAway.predict(mu);

		return Utils.poissonOver((float) regressionHome.predict(lambda), (float) regressionHome.predict(lambda));
		// if (expected > 2.5)
		// return 1f;
		// else
		// return 0f;
		// float dist = avgShotsOver - avgShotsUnder;
		// System.out.println(dist);

		// if (avgShotsUnder > avgShotsOver) {
		// return 0.5f;
		// }
		// if (expected >= avgShotsOver && expected > avgShotsUnder) {
		// float score = 0.5f + 0.5f * (expected - avgShotsOver) / dist;
		// return (score >= 0 && score <= 1f) ? score : 1f;
		// } else if (expected <= avgShotsUnder && expected < avgShotsOver) {
		// float score = 0.5f - 0.5f * (-expected + avgShotsUnder) / dist;
		// return (score >= 0 && score <= 1f) ? score : 0f;
		// } else {
		// // System.out.println(f);
		// return 0.5f;
		// }
	}

	public static float selectAvgHomeShotsFor(HSSFSheet sheet, String homeTeam, Date date) throws ParseException {
		int total = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && htname.equals(homeTeam) && dateCell != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && fdate.before(date)) {
				int homeShots = (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				total += homeShots;
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	public static float selectAvgHomeShotsAgainst(HSSFSheet sheet, String homeTeam, Date date) throws ParseException {
		int total = 0;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();

			if (row.getCell(getColumnIndex(sheet, "AST")) != null && htname.equals(homeTeam) && dateCell != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && fdate.before(date)) {
				int homeShots = (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				total += homeShots;
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	public static float selectAvgAwayShotsFor(HSSFSheet sheet, String awayTeam, Date date) throws ParseException {
		int total = 0;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String awayname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "AST")) != null && awayname.equals(awayTeam) && dateCell != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && fdate.before(date)) {
				int homeShots = (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				total += homeShots;
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	public static float selectAvgAwayShotsAgainst(HSSFSheet sheet, String awayTeam, Date date) throws ParseException {
		int total = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String awayname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && awayname.equals(awayTeam) && dateCell != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && fdate.before(date)) {
				int homeShots = (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				total += homeShots;
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	private static float selectAvgShotsTotal(HSSFSheet sheet, Date date) throws ParseException {
		int total = 0;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();

			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && fdate.before(date)) {
				total += (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				total += (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				}

				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float AvgShotsWhenUnder(HSSFSheet sheet, Date date) throws ParseException {
		int total = 0;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awaygoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& dateCell != null && fdate.before(date)
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && homegoal + awaygoal < 2.5f) {
				total += (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				total += (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				}

				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float AvgShotsWhenOver(HSSFSheet sheet, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int total = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awaygoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& dateCell != null && fdate.before(date)
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && homegoal + awaygoal > 2.5f) {
				total += (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				total += (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				}

				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float selectAvgShotsHome(HSSFSheet sheet, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int total = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0 && dateCell != null
					&& fdate.before(date)) {
				total += (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					total += (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	public static float selectAvgShotsAway(HSSFSheet sheet, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int total = 0;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& row.getCell(getColumnIndex(sheet, "AST")).getCellType() == 0 && dateCell != null
					&& fdate.before(date)) {
				total += (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();
				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					try {
						total += (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
					} catch (Exception e) {
						System.out.println("daasfgd");
					}
				}
				count++;
			}
		}
		return count == 0 ? 0 : ((float) total) / count;
	}

	public static float selectAvgAwayTeamAgainst(HSSFSheet sheet, String awayTeamName, Date date)
			throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String atname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && atname.equals(awayTeamName) && dateCell != null
					&& fdate.before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	public static float selectAvgAwayTeamFor(HSSFSheet sheet, String awayTeamName, Date date) throws ParseException {
		float total = 0f;
		int count = 0;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && htname.equals(awayTeamName) && dateCell != null
					&& fdate.before(date)) {
				int awaygoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += awaygoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	public static float selectAvgHomeTeamAgainst(HSSFSheet sheet, String homeTeamName, Date date)
			throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && htname.equals(homeTeamName) && dateCell != null
					&& fdate.before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float selectAvgHomeTeamFor(HSSFSheet sheet, String homeTeamName, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && htname.equals(homeTeamName) && dateCell != null
					&& fdate.before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float selectAvgFor(HSSFSheet sheet, String team, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String htname = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String atname = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && htname.equals(team) && dateCell != null
					&& fdate.before(date)) {
				int goal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += goal;
				count++;
			}

			if (row.getCell(getColumnIndex(sheet, "ATHG")) != null && atname.equals(team) && dateCell != null
					&& fdate.before(date)) {
				int goal = (int) row.getCell(getColumnIndex(sheet, "ATHG")).getNumericCellValue();
				total += goal;
				count++;
			}
		}
		return count == 0 ? 0 : (float) total / count;
	}

	public static float selectAvgLeagueAway(HSSFSheet sheet, Date date) throws ParseException {
		float total = 0f;
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTAG")) != null && dateCell != null && fdate.before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	public static float selectAvgLeagueHome(HSSFSheet sheet, Date date) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		float total = 0f;
		int count = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			Date fdate;
			Cell dateCell = row.getCell(getColumnIndex(sheet, "Date"));
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && dateCell != null && fdate.before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return count == 0 ? 0 : total / count;
	}

	// selects all fixtures after the 10 matchday
	public static ArrayList<Fixture> selectAll(HSSFSheet sheet, int limit) throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy HH:mm");
		DateFormat XLSformatOld = new SimpleDateFormat("d.M.yyyy");
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate;
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				try {
					fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
				} catch (Exception e) {
					fdate = XLSformatOld.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
				}
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();

			if (selectLastAll(sheet, home, 10, fdate).size() >= limit
					&& selectLastAll(sheet, away, 10, fdate).size() >= limit) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}
		return results;

	}

	// selects all fixtures with all lines
	public static ArrayList<Fixture> selectAllFull(HSSFSheet sheet, int limit) throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate;
			if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
				fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
			else
				fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();

			if (selectLastAll(sheet, home, 10, fdate).size() >= limit
					&& selectLastAll(sheet, away, 10, fdate).size() >= limit) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}
		return results;

	}

	// selects all fixtures after the 10 matchday
	public static ArrayList<Fixture> selectAllAll(HSSFSheet sheet) throws ParseException {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0 || row.getCell(getColumnIndex(sheet, "HomeTeam")) == null)
				continue;

			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (selectLastAll(sheet, home, 10, fdate).size() >= 1
					&& selectLastAll(sheet, away, 10, fdate).size() >= 1) {
				Fixture f = getFixture(sheet, row);
				if (f != null)
					results.add(f);
				else
					continue;
			}
		}

		return results;
	}

	public static Fixture getFixture(HSSFSheet sheet, Row row) throws ParseException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy");
		Fixture f = null;
		String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
		String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
		Date fdate;
		if (row.getCell(getColumnIndex(sheet, "Date")).getCellType() == 1)
			fdate = XLSformat.parse(row.getCell(getColumnIndex(sheet, "Date")).getStringCellValue());
		else
			fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
		if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && row.getCell(getColumnIndex(sheet, "FTAG")) != null
				&& row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null
				&& row.getCell(getColumnIndex(sheet, "HTHG")) != null
				&& (row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getCellType() == 0
						&& row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getCellType() == 0)) {
			int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
			int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

			int halfTimeHome = (int) row.getCell(getColumnIndex(sheet, "HTHG")).getNumericCellValue();
			int halfTimeAway = (int) row.getCell(getColumnIndex(sheet, "HTAG")).getNumericCellValue();
			float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
			float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

			// with 1X2 odds from Pinnacle

			float homeOdds, drawOdds, awayOdds;
			if (row.getCell(getColumnIndex(sheet, "PSH")) != null
					&& row.getCell(getColumnIndex(sheet, "PSH")).getCellType() == 0) {
				homeOdds = (float) row.getCell(getColumnIndex(sheet, "PSH")).getNumericCellValue();
				drawOdds = (float) row.getCell(getColumnIndex(sheet, "PSD")).getNumericCellValue();
				awayOdds = (float) row.getCell(getColumnIndex(sheet, "PSA")).getNumericCellValue();
			} else {
				homeOdds = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
				drawOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxD")).getNumericCellValue();
				awayOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxA")).getNumericCellValue();
			}

			f = new Fixture(fdate, sheet.getSheetName(), home, away, new Result(homeGoals, awayGoals))
					.withHTResult(new Result(halfTimeHome, halfTimeAway)).withStatus("FINISHED");
			OverUnderOdds ou = new OverUnderOdds("Max", fdate, 2.5f, overOdds, underOdds);
			MatchOdds mo = new MatchOdds("Max", fdate, homeOdds, drawOdds, awayOdds);
			f.overUnderOdds.add(ou);
			f.matchOdds.add(mo);

			// Shots on target
			if (row.getCell(getColumnIndex(sheet, "HST")) != null && row.getCell(getColumnIndex(sheet, "AST")) != null
					&& row.getCell(getColumnIndex(sheet, "HST")).getCellType() == 0) {
				f.gameStats.shots.home = (int) row.getCell(getColumnIndex(sheet, "HST")).getNumericCellValue();
				f.gameStats.shots.away = (int) row.getCell(getColumnIndex(sheet, "AST")).getNumericCellValue();

				if (Arrays.asList(Constants.MANUAL).contains(sheet.getSheetName())) {
					f.gameStats.shots.home = f.result.goalsHomeTeam + f.gameStats.shots.home;
					f.gameStats.shots.away = f.result.goalsAwayTeam + f.gameStats.shots.away;
				}

			}

			// Asian handicap
			if (row.getCell(getColumnIndex(sheet, "BbAHh")) != null
					&& row.getCell(getColumnIndex(sheet, "BbMxAHH")) != null
					&& row.getCell(getColumnIndex(sheet, "BbMxAHA")) != null
					&& row.getCell(getColumnIndex(sheet, "BbAHh")).getCellType() == 0) {
				float line = (float) row.getCell(getColumnIndex(sheet, "BbAHh")).getNumericCellValue();
				float asianHome = (float) row.getCell(getColumnIndex(sheet, "BbMxAHH")).getNumericCellValue();
				float asianAway = (float) row.getCell(getColumnIndex(sheet, "BbMxAHA")).getNumericCellValue();
				AsianOdds ao = new AsianOdds("Max", fdate, line, asianHome, asianAway);
				f.asianOdds.add(ao);
			}

			// // Red cards
			// if (row.getCell(getColumnIndex(sheet, "HR")) != null &&
			// row.getCell(getColumnIndex(sheet, "AR")) != null) {
			// f = (Fixture) f.withCards((int) row.getCell(getColumnIndex(sheet,
			// "HR")).getNumericCellValue(),
			// (int) row.getCell(getColumnIndex(sheet,
			// "AR")).getNumericCellValue());
			// }

			// Goal lines
			if (row.getCell(getColumnIndex(sheet, "GLM")) != null
					&& row.getCell(getColumnIndex(sheet, "ASM")) != null) {
				main.Line line1 = new main.Line((float) row.getCell(getColumnIndex(sheet, "GL1")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL1>2.5")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL1<2.5")).getNumericCellValue(), "Pinn");
				main.Line line2 = new main.Line((float) row.getCell(getColumnIndex(sheet, "GL2")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL2>2.5")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL2<2.5")).getNumericCellValue(), "Pinn");
				main.Line main = new main.Line((float) row.getCell(getColumnIndex(sheet, "GLM")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GLM>2.5")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GLM<2.5")).getNumericCellValue(), "Pinn");
				main.Line line3 = new main.Line((float) row.getCell(getColumnIndex(sheet, "GL3")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL3>2.5")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL3<2.5")).getNumericCellValue(), "Pinn");
				main.Line line4 = new main.Line((float) row.getCell(getColumnIndex(sheet, "GL4")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL4>2.5")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "GL4<2.5")).getNumericCellValue(), "Pinn");

				GoalLines GLS = new GoalLines(line1, line2, main, line3, line4);

				main.Line asian1 = new main.Line(
						(float) row.getCell(getColumnIndex(sheet, "AS1")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS1H")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS1A")).getNumericCellValue(), "Pinn");
				main.Line asian2 = new main.Line(
						(float) row.getCell(getColumnIndex(sheet, "AS2")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS2H")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS2A")).getNumericCellValue(), "Pinn");
				main.Line asianmain = new main.Line(
						(float) row.getCell(getColumnIndex(sheet, "ASM")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "ASMH")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "ASMA")).getNumericCellValue(), "Pinn");
				main.Line asian3 = new main.Line(
						(float) row.getCell(getColumnIndex(sheet, "AS3")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS3H")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS3A")).getNumericCellValue(), "Pinn");
				main.Line asian4 = new main.Line(
						(float) row.getCell(getColumnIndex(sheet, "AS4")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS4H")).getNumericCellValue(),
						(float) row.getCell(getColumnIndex(sheet, "AS4A")).getNumericCellValue(), "Pinn");

				AsianLines asianLines = new AsianLines(asian1, asian2, asianmain, asian3, asian4);

				// f = f.withAsianLines(asianLines).withGoalLines(GLS);

			}

		}
		return f;
	}

	public static ArrayList<Fixture> selectForPrediction(HSSFSheet sheet) {
		ArrayList<Fixture> results = new ArrayList<>();
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
			float getMaxClosingUnderOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();

			Fixture f = new Fixture(fdate, sheet.getSheetName(), home, away, new Result(-1, -1));
			f.withStatus("FINISHED");
			OverUnderOdds ou = new OverUnderOdds("Max", fdate, 2.5f, overOdds, getMaxClosingUnderOdds);
			f.overUnderOdds.add(ou);

			// Asian handicap
			if (row.getCell(getColumnIndex(sheet, "BbAHh")) != null
					&& row.getCell(getColumnIndex(sheet, "BbMxAHH")) != null
					&& row.getCell(getColumnIndex(sheet, "BbMxAHA")) != null
					&& row.getCell(getColumnIndex(sheet, "BbAHh")).getCellType() == 0) {
				float line = (float) row.getCell(getColumnIndex(sheet, "BbAHh")).getNumericCellValue();
				float homeOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxAHH")).getNumericCellValue();
				float awayOdds = (float) row.getCell(getColumnIndex(sheet, "BbMxAHA")).getNumericCellValue();
				AsianOdds ao = new AsianOdds("Max", fdate, line, homeOdds, awayOdds);
				f.asianOdds.add(ao);
			}

			results.add(f);

		}
		return results;
	}

	public static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<Fixture> all, int year, float initTH)
			throws IOException, ParseException {
		float bestWinPercent = 0.5f;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] ht1s = new float[all.size()];
		float[] ht2s = new float[all.size()];
		float[] htCombos = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = poissonWeighted(f, sheet);
			ht1s[i] = halfTimeOnly(f, sheet, 1);
			ht2s[i] = halfTimeOnly(f, sheet, 2);
		}

		float overOneHT = checkOptimal(sheet, all, ht1s, ht2s, 0.5f);

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
			htCombos[i] = (overOneHT * halfTimeOnly(f, sheet, 1) + (1f - overOneHT) * halfTimeOnly(f, sheet, 2));
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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

	public static Settings runWithTH(HSSFSheet sheet, ArrayList<Fixture> all, int year)
			throws IOException, ParseException {
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

	public static Settings runForWithTH(HSSFSheet sheet, ArrayList<Fixture> all, int year)
			throws IOException, ParseException {

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] ht1s = new float[all.size()];
		float[] ht2s = new float[all.size()];
		float[] htCombos = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet);
			weightedPoissons[i] = poissonWeighted(f, sheet);
			ht1s[i] = halfTimeOnly(f, sheet, 1);
			ht2s[i] = halfTimeOnly(f, sheet, 2);
		}

		float overOneHT = checkOptimal(sheet, all, ht1s, ht2s, 0.5f);

		for (int i = 0; i < all.size(); i++) {
			htCombos[i] = (overOneHT * ht1s[i] + (1f - overOneHT) * ht2s[i]);
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

	private static Settings helperRunFor(HSSFSheet sheet, ArrayList<Fixture> all, int year, float initTH,
			float overOneHT, float[] basics, float[] poissons, float[] weightedPoissons, float[] htCombos) {

		float bestWinPercent = 0.5f;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH,
					bestWinPercent, bestProfit).withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
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

	private static Settings aggregateRun(ArrayList<HSSFSheet> sheets, ArrayList<ArrayList<Fixture>> byYear, int start,
			int end) throws ParseException {
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
		for (ArrayList<Fixture> i : byYear)
			if (i.size() > maxSize) {
				maxSize = i.size();
			}

		float[][] basics = new float[size][maxSize];
		float[][] poissons = new float[size][maxSize];
		float[][] weightedPoissons = new float[size][maxSize];
		float[][] htCombos = new float[size][maxSize];
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < byYear.get(j).size(); i++) {
				Fixture f = byYear.get(j).get(i);

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

				profit += Utils.getProfit(finals);
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

				profit += Utils.getProfit(finals);
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

				profit += Utils.getProfit(finals);
			}

			if (profit > bestProfit) {
				bestProfit = profit;
				best = set;
				best.profit = bestProfit;
			}
		}

		return best;
	}

	public static ArrayList<FinalEntry> intersectAllClassifier(HSSFSheet sheet, ArrayList<Fixture> all, int year,
			float basicThreshold, float poissonThreshold, float weightedThreshold, float htThreshold,
			float drawThreshold) throws ParseException {
		ArrayList<FinalEntry> finalsBasic = new ArrayList<>();
		ArrayList<FinalEntry> finalsPoisson = new ArrayList<>();
		ArrayList<FinalEntry> finalsWeighted = new ArrayList<>();
		ArrayList<FinalEntry> finalsHT2 = new ArrayList<>();
		ArrayList<FinalEntry> finalsDraw = new ArrayList<>();

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
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

	public static Settings runForLeagueWithOddsFull(HSSFSheet sheet, ArrayList<Fixture> all, int year)
			throws IOException, ParseException {
		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;
		float bestWeighed = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);

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
					Fixture f = all.get(i);
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
				float currentProfit = Utils.getProfit(finals);
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

	// public static float checkHalfTimeOptimal(HSSFSheet sheet,
	// ArrayList<Fixture> all, float[] ht1, float[] ht2,
	// String type) {
	// float bestProfit = Float.NEGATIVE_INFINITY;
	// float overOneValue = 0f;
	//
	// for (int x = 0; x <= 2; x++) {
	// int y = 2 - x;
	// ArrayList<FinalEntry> finals = new ArrayList<>();
	// for (int j = 0; j < all.size(); j++) {
	// Fixture f = all.get(j);
	// float finalScore = x * 0.5f * ht1[j] + y * 0.5f * ht2[j];
	//
	// FinalEntry fe = new FinalEntry(f, finalScore,
	// new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f,
	// 0.55f);
	// if (!fe.prediction.equals(Float.NaN))
	// finals.add(fe);
	// }
	// Settings set = new Settings(sheet.getSheetName(), 0, 0, 0, 0.55f, 0.55f,
	// 0.55f, 0, bestProfit)
	// .withValue(0.9f);
	// float currentProfit = Utils.getProfit(finals, set, type);
	// if (currentProfit > bestProfit) {
	// bestProfit = currentProfit;
	// overOneValue = x * 0.5f;
	// }
	//
	// }
	//
	// return overOneValue;
	// }

	private static float checkOptimal(HSSFSheet sheet, ArrayList<Fixture> all, float[] basics, float[] similars,
			float step) {
		float bestProfit = Float.NEGATIVE_INFINITY;
		float overOneValue = 0f;

		int n = Math.round(1f / step);

		for (int x = 0; x <= n; x++) {
			int y = n - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int j = 0; j < all.size(); j++) {
				Fixture f = all.get(j);
				float finalScore = x * step * basics[j] + y * step * similars[j];

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}
			Settings set = new Settings(sheet.getSheetName(), 0, 0, 0, 0.55f, 0.55f, 0.55f, 0, bestProfit)
					.withValue(0.9f);
			float currentProfit = Utils.getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				overOneValue = x * step;
			}

		}

		return overOneValue;
	}

	// TODO
	private static float aggregateHalfTimeOptimal(ArrayList<HSSFSheet> sheets, ArrayList<ArrayList<Fixture>> byYear,
			int start, int end) {

		return 0;
	}

	// public static Settings findInterval(ArrayList<Fixture> all,
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

	public static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<Fixture> all, Settings settings,
			Table table) throws ParseException {
		ArrayList<FinalEntry> finals = calculateScores(sheet, all, settings, table);

		return restrict(finals, settings);
	}

	public static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet,
			ArrayList<? extends Fixture> currentEntries, Settings settings) throws ParseException {
		ArrayList<FinalEntry> finals = calculateScores(sheet, currentEntries, settings);

		return restrict(finals, settings);
	}

	public static ArrayList<FinalEntry> calculateScores(HSSFSheet sheet, ArrayList<Fixture> all, Settings settings,
			Table table) throws ParseException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {

			float finalScore = 0f;

			if (settings.basic != 0f)
				finalScore += settings.basic * ((1 - settings.similars) * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
						+ settings.similars * Utils.basicSimilar(f, sheet, table));

			if (settings.poisson != 0f)
				finalScore += settings.poisson * ((1 - settings.similarsPoisson) * poisson(f, sheet)
						+ settings.similarsPoisson * Utils.similarPoisson(f, sheet, table));

			if (settings.weightedPoisson != 0f)
				finalScore += settings.weightedPoisson * poissonWeighted(f, sheet);

			if (settings.htCombo != 0f)
				finalScore += settings.htCombo * (settings.halfTimeOverOne * halfTimeOnly(f, sheet, 1)
						+ (1f - settings.halfTimeOverOne) * halfTimeOnly(f, sheet, 2));

			if (settings.shots != 0f)
				finalScore += settings.shots * shots(f, sheet);

			FinalEntry fe = new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					settings.threshold, settings.lowerBound, settings.upperBound);
			if (!fe.prediction.equals(Float.NaN))
				finals.add(fe);
		}
		return finals;
	}

	public static ArrayList<FinalEntry> calculateScores(HSSFSheet sheet, ArrayList<? extends Fixture> currentEntries,
			Settings settings) throws ParseException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : currentEntries) {

			float finalScore = 0f;

			if (settings.basic != 0f)
				finalScore += settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f);

			if (settings.poisson != 0f)
				finalScore += settings.poisson * poisson(f, sheet);

			if (settings.weightedPoisson != 0f)
				finalScore += settings.weightedPoisson * poissonWeighted(f, sheet);

			if (settings.htCombo != 0f)
				finalScore += settings.htCombo * (settings.halfTimeOverOne * halfTimeOnly(f, sheet, 1)
						+ (1f - settings.halfTimeOverOne) * halfTimeOnly(f, sheet, 2));

			if (settings.shots != 0f)
				finalScore += settings.shots * shots(f, sheet);

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

			float gain = fe.prediction > settings.threshold ? fe.fixture.getMaxClosingOverOdds().getOverOdds()
					: fe.fixture.getMaxClosingUnderOdds().getUnderOdds();
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

	public static ArrayList<FinalEntry> runWithoutMinOddsSettings(HSSFSheet sheet, ArrayList<Fixture> all,
			Settings settings) throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet);
			finals.add(new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f,
					settings.lowerBound, settings.upperBound));
		}

		return finals;
	}

	public static void makePrediction(HSSFSheet odds, HSSFSheet league, Fixture f, Settings sett)
			throws IOException, InterruptedException, ParseException {

		ArrayList<Fixture> one = new ArrayList<>();

		ArrayList<String> dont = new ArrayList<String>(Arrays.asList(Constants.DONT));
		if (sett == null)
			return;
		float score = sett.basic * basic2(f, league, 0.6f, 0.3f, 0.1f) + sett.poisson * poisson(f, league)
				+ sett.weightedPoisson * poissonWeighted(f, league)
				+ sett.htCombo * (sett.halfTimeOverOne * halfTimeOnly(f, league, 1)
						+ (1f - sett.halfTimeOverOne) * halfTimeOnly(f, league, 2));

		float certainty = score > sett.threshold ? score : (1f - score);
		float coeff = score > sett.threshold ? f.getMaxClosingOverOdds().getOverOdds()
				: f.getMaxClosingUnderOdds().getUnderOdds();
		float value = certainty * coeff;

		float cot = score > sett.threshold ? (score - sett.threshold) : (sett.threshold - score);
		if (Utils.oddsInRange(coeff, score, sett) && (value > sett.value)
				&& (cot >= findCot(league.getSheetName(), 2015, 3, "realdouble24")) && (certainty >= 0.5f)
				&& (score >= sett.upperBound || score <= sett.lowerBound) && !dont.contains(league.getSheetName())) {
			String prediction = score > sett.threshold ? "over" : "under";
			// System.out.println(sett);
			System.err.println(league.getSheetName() + " " + f.homeTeam + " : " + f.awayTeam + " " + score + " "
					+ prediction + " " + coeff);
		} else {
			String prediction = score > sett.threshold ? "over" : "under";
			// System.out.println(sett);
			System.out.println(league.getSheetName() + " " + f.homeTeam + " : " + f.awayTeam + " " + score + " "
					+ prediction + " " + coeff);
		}

	}

	public static Settings predictionSettings(HSSFSheet sheet, int year) throws IOException, ParseException {
		ArrayList<Fixture> data = selectAllAll(sheet);

		Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f);
		ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

		temp = findThreshold(finals, temp, MaximizingBy.BOTH);
		finals = restrict(finals, temp);

		temp = findIntervalReal(finals, year, temp);
		finals = restrict(finals, temp);

		temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
		finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

		temp = findThreshold(finals, temp, MaximizingBy.BOTH);
		finals = restrict(finals, temp);

		temp = findIntervalReal(finals, year, temp);
		finals = restrict(finals, temp);

		temp = findValue(finals, sheet, temp);

		return temp;
	}

	public static SettingsAsian asianPredictionSettings(HSSFSheet sheet, int year) throws IOException, ParseException {
		ArrayList<Fixture> data = selectAllAll(sheet);

		SettingsAsian temp = AsianUtils.runForLeagueWithOdds(sheet, data, year);
		ArrayList<AsianEntry> finals = AsianUtils.runWithSettingsList(sheet, data, temp);

		float bestExp = AsianUtils.bestExpectancy(finals);
		temp.expectancy = bestExp;

		return temp;
	}

	public static Settings optimalSettings(HSSFSheet sheet, int year) throws IOException, ParseException {
		ArrayList<Fixture> data = selectAllAll(sheet);

		Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f);

		float initpr = temp.profit;

		ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

		System.out.println("Run for? " + initpr + " == " + Utils.getProfit(finals));

		temp = findThreshold(finals, temp, MaximizingBy.BOTH);
		float th1 = temp.profit;
		finals = restrict(finals, temp);
		System.out.println("Th1: " + temp.profit + "==" + Utils.getProfit(finals));

		temp = findIntervalReal(finals, year, temp);
		float pr = temp.profit;
		finals = restrict(finals, temp);
		System.out.println("Interval? " + pr + " == " + Utils.getProfit(finals));
		System.out.println("Under over breakdown? " + Utils.getProfit(finals) + " == "
				+ (Utils.getProfit(Utils.onlyOvers(finals)) + Utils.getProfit(Utils.onlyUnders(finals))));

		temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);

		float initpr2 = temp.profit;

		finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

		System.out.println("Run for2? " + initpr2 + " == " + Utils.getProfit(finals));

		// System.out.println(temp);
		temp = findThreshold(finals, temp, MaximizingBy.BOTH);
		finals = restrict(finals, temp);

		System.out.println("Thold: " + temp.profit + "==" + Utils.getProfit(finals));
		// temp = trustInterval(sheet, finals, temp);
		// System.out.println(temp);

		temp = findValue(finals, sheet, temp);
		float val = temp.profit;
		finals = restrict(finals, temp);

		System.out.println("value " + val + " == " + Utils.getProfit(finals));

		temp = findIntervalReal(finals, year, temp);
		float pr2 = temp.profit;
		finals = restrict(finals, temp);

		System.out.println("Interval? " + pr2 + " == " + Utils.getProfit(finals));
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

	public static Settings aggregateOptimals(int start, int end, String league) throws IOException, ParseException {
		float bestProfit = Float.NEGATIVE_INFINITY;
		Settings set = new Settings(league, 0.6f, 0.2f, 0.2f, 0.55f, 0.55f, 0.55f, 0.5f, bestProfit);

		ArrayList<ArrayList<Fixture>> byYear = new ArrayList<>();
		ArrayList<HSSFSheet> sheets = new ArrayList<>();
		for (int year = start; year <= end; year++) {
			String base = new File("").getAbsolutePath();
			FileInputStream file = new FileInputStream(
					new File(base + "/data/all-euro-data-" + year + "-" + (year + 1) + ".xls"));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet(league);

			byYear.add(selectAllAll(sheet));
			sheets.add(sheet);

			workbook.close();
			file.close();
		}

		return aggregateRun(sheets, byYear, start, end);

	}

	public static Settings predictivePower(HSSFSheet sheet, ArrayList<Fixture> all, int maxMatchDay, int year)
			throws ParseException {

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
			Fixture f = all.get(i);

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
				ArrayList<? extends Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals);

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
				ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals);

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
				ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit += Utils.getProfit(finals);

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
			Settings initial) throws ParseException {
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
				ArrayList<? extends Fixture> currentEntries = FixtureUtils.getByMatchday(Utils.onlyFixtures(finals),
						day);
				ArrayList<FinalEntry> finalEntries = runWithSettingsList(sheet, currentEntries, trset);
				profit += Utils.getProfit(finalEntries);

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

	public static float realisticPredictive(HSSFSheet sheet, int year) throws IOException, ParseException {
		float profit = 0.0f;
		ArrayList<Fixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);

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

			float trprofit = Utils.getProfit(finals);
			profit += trprofit;
		}
		return profit;
	}

	public static float realisticRun(HSSFSheet sheet, int year) throws IOException, ParseException {
		float profit = 0.0f;
		ArrayList<Fixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 1, 1);
			// if (!current.isEmpty() &&
			// current.get(0).date.after(cal.getTime())) {
			// return profit;
			// }

			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);
			// data = Utils.filterByOdds(data, minOdds, maxOdds);
			Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f) /* runWithTH(sheet, data, year) */;
			// System.out.println("match " + i + temp);
			// temp.maxOdds = maxOdds;
			// temp.minOdds = minOdds;

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

			temp = findThreshold(finals, temp, MaximizingBy.BOTH);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(finals, temp, MaximizingBy.BOTH);
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
			float trprofit = Utils.getProfit(finals);
			profit += trprofit;
		}

		return profit;
	}

	public static float realisticByTeam(HSSFSheet sheet, int year) throws IOException, ParseException {
		Map<String, Integer> played = new HashMap<>();
		Map<String, Integer> success = new HashMap<>();
		float profit = 0.0f;
		ArrayList<Fixture> all = selectAllAll(sheet);

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 1, 1);
			// if (!current.isEmpty() &&
			// current.get(0).date.after(cal.getTime())) {
			// return profit;
			// }

			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);
			// data = Utils.filterByOdds(data, minOdds, maxOdds);
			Settings temp = runForLeagueWithOdds(sheet, data, year, 0.55f) /* runWithTH(sheet, data, year) */;
			// System.out.println("match " + i + temp);
			// temp.maxOdds = maxOdds;
			// temp.minOdds = minOdds;

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);

			temp = findThreshold(finals, temp, MaximizingBy.BOTH);
			finals = restrict(finals, temp);

			temp = findIntervalReal(finals, year, temp);
			finals = restrict(finals, temp);

			temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals), year, temp.threshold);
			finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals), temp);

			temp = findThreshold(finals, temp, MaximizingBy.BOTH);
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
			float trprofit = Utils.getProfit(finals);
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

	public static float realisticAllLines(HSSFSheet sheet, int year) throws ParseException, InterruptedException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<Fixture> full = selectAllFull(sheet, 0);
		ArrayList<Fixture> all = new ArrayList<>();

		for (Fixture j : full) {
			all.add(j);
		}
		HashMap<Fixture, Fixture> map = new HashMap<>();
		for (int j = 0; j < all.size(); j++) {

			map.put(all.get(j), full.get(j));
		}

		// Utils.fairValue(all);
		float th = 0.55f;
		Settings temp = Settings.shots(sheet.getSheetName());
		// temp.value = 1.1f;
		// Settings ht1 = new Settings(sheet.getSheetName(), 0f, 0f, 0f, th, th,
		// th, 0.5f, 0f).withHT(0.2f, 1f);
		int maxMatchDay = addMatchDay(sheet, all);
		// ArrayList<PlayerFixture> pfs = SQLiteJDBC
		// .selectPlayerFixtures(Arrays.asList(MinMaxOdds.SHOTS).contains(sheet.getSheetName())
		// ? MinMaxOdds.reverseEquivalents.get(sheet.getSheetName()) :
		// sheet.getSheetName(), year);
		// ArrayList<Fixture> allPfs = Utils.getFixtures(pfs);
		// HashMap<String, String> dictionary = null;
		// if (!allPfs.isEmpty())
		// dictionary = XlSUtils.deduceDictionary(Utils.notPending(all),
		// allPfs);

		for (int i = 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<FinalEntry> finals = new ArrayList<>();

			finals = FixtureUtils.runWithSettingsList(all, current, temp);
			// finals = FixtureUtils.runWithSettingsList(all, current, temp);

			// finals = runBestTH(sheet, current, "IT", year, 3,
			// "shots", temp);
			// ShotsSettings shotSetts = checkOUoptimality(sheet.getSheetName(),
			// year, 3, "shots");

			// ArrayList<FinalEntry> finalsPFS = new ArrayList<>();
			// finalsPFS = Utils.runWithPlayersData(current, pfs, dictionary,
			// sheet, 0.525f);
			// finals = finalsPFS;
			// finals = Utils.cotRestrict(finals, 0.025f);
			finals = Utils.noequilibriums(finals);
			// SQLiteJDBC.storeFinals(finals, year, "IT",
			// "shots");
			// ArrayList<FinalEntry> ht1s = new ArrayList<>();

			// ht1s = runWithSettingsList(sheet, current, ht1);

			// finals = Utils.intersectDiff(finals, ht1s);
			finals = Utils.onlyUnders(finals);
			// finals = Utils.onlyOvers(finals);

			// finals = Utils.certaintyRestrict(finals, 0.70f);
			// finals = Utils.estimateOposite(current, map, sheet);
			// finals = Utils.mainGoalLine(finals, map);
			// Utils.theOposite(finals);
			// finals = Utils.customGoalLine(finals, map, -0.5f);
			// finals = Utils.specificLine(finals, map, 2.5f);
			// finals = Utils.setTrueOddsProportional(finals);
			// finals = Utils.bestValueByDistibution(finals, map, all, sheet);

			played += finals.size();

			// System.out.println(finals);
			float trprofit = Utils.getProfit(finals);

			profit += trprofit;

		}

		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));

		return profit;

	}

	public static float realisticFromDB(HSSFSheet sheet, int year)
			throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<Fixture> all = selectAll(sheet, 0);
		// System.out.println("VIG: " + Utils.avgReturn(all));
		// System.out.println(all.size());

		// HashMap<Fixture, Float> basics = SQLiteJDBC.selectScores(all,
		// "BASICS", year, sheet.getSheetName());
		// HashMap<Fixture, Float> poissons =
		// SQLiteJDBC.selectScores(all, "POISSON", year, sheet.getSheetName());
		// HashMap<Fixture, Float> weighted =
		// SQLiteJDBC.selectScores(all, "WEIGHTED", year, sheet.getSheetName());
		// HashMap<Fixture, Float> ht1 = SQLiteJDBC.selectScores(all,
		// "HALFTIME1", year, sheet.getSheetName());
		// HashMap<Fixture, Float> ht2 = SQLiteJDBC.selectScores(all,
		// "HALFTIME2", year, sheet.getSheetName());
		// HashMap<Fixture, Float> shots = SQLiteJDBC.selectScores(all,
		// "SHOTS", year, sheet.getSheetName());
		float th = 0.55f;
		Settings temp = Settings.shots(sheet.getSheetName());
		temp.threshold = th;
		temp.lowerBound = th;
		temp.upperBound = th;
		// temp.value = 0.86f;
		// temp.minOver = 1.9f;

		// float bestTH = findTH(sheet.getSheetName(), year, 3, "shots", temp);
		// float bestCot = findCot(sheet.getSheetName(), year, 2, "shots");
		// System.out.println(bestCot);
		// Pair pair = findTHandCOT(sheet.getSheetName(), year, 2, "shots",
		// temp);
		// bestTH = pair.home;
		// bestCot = pair.away;
		// System.out.println(bestCot);
		int maxMatchDay = FixtureUtils.addMatchDay(all);
		// ArrayList<PlayerFixture> pfs = SQLiteJDBC
		// .selectPlayerFixtures(Arrays.asList(MinMaxOdds.SHOTS).contains(sheet.getSheetName())
		// ? MinMaxOdds.reverseEquivalents.get(sheet.getSheetName()) :
		// sheet.getSheetName(), year);
		// ArrayList<Fixture> allPfs = Utils.getFixtures(pfs);
		// HashMap<String, String> dictionary = null;
		// if (!allPfs.isEmpty())
		// dictionary = XlSUtils.deduceDictionary(Utils.notPending(all),
		// allPfs);

		for (int i = /* dictionary == null ? 100 : */14; i < maxMatchDay; i++) {

			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			// Utils.fairValue(current);
			// ArrayList<Fixture> data = Utils.getBeforeMatchday(all,
			// i);

			// Table table = Utils.createTable(data, sheet.getSheetName(),
			// year,s
			// i);
			ArrayList<FinalEntry> finals = new ArrayList<>();
			// finals = Utils.allUnders(current);
			finals = FixtureUtils.runWithSettingsList(all, current, temp);
			// ArrayList<AllEntry> halftimeData = new ArrayList<>();
			// for (FinalEntry j : finals) {
			// ArrayList<Fixture> lastHomeTeam =
			// FixtureUtils.selectLastAll(all, j.fixture.homeTeam, 50,
			// j.fixture.date);
			// ArrayList<Fixture> lastAwayTeam =
			// FixtureUtils.selectLastAll(all, j.fixture.awayTeam, 50,
			// j.fixture.date);
			// float zero = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 0)
			// + Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 0)) / 2;
			// float one = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 1)
			// + Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 1)) / 2;
			// float two = (Utils.countHalfTimeGoalAvgExact(lastHomeTeam, 2)
			// + Utils.countHalfTimeGoalAvgExact(lastAwayTeam, 2)) / 2;
			// float more = (Utils.countOverHalfTime(lastHomeTeam, 3) +
			// Utils.countOverHalfTime(lastHomeTeam, 3)) / 2;
			//
			// float basic = basic2(j.fixture, sheet, 0.6f, 0.3f, 0.1f);
			// float poisson = poisson(j.fixture, sheet);
			// float weighted = poissonWeighted(j.fixture, sheet);
			// float shots =
			// Arrays.asList(MinMaxOdds.SHOTS).contains(sheet.getSheetName()) ?
			// shots(j.fixture, sheet)
			// : -1f;
			// AllEntry hte = new AllEntry(j, zero, one, two, more, basic,
			// poisson, weighted, shots);
			// halftimeData.add(hte);
			// }
			//
			// SQLiteJDBC.storeAllData(halftimeData, year, sheet.getSheetName(),
			// "all");

			// finals = runWithSettingsList(sheet, current, temp);
			// finals = Utils.onlyUnders(finals);
			// finals = runBestTH(sheet, current, sheet.getSheetName(), year, 3,
			// "shots", temp);
			// finals = runWithSettingsList(sheet, current, temp, pfs,
			// dictionary);

			// ArrayList<FinalEntry> finalsPFS = new ArrayList<>();
			// finalsPFS = Utils.runWithPlayersData(current, pfs, dictionary,
			// all, 0.525f);
			// finals = finalsPFS;
			// finals = Utils.similarRanking(finals, table);

			// Settings we = new Settings(sheet.getSheetName(), 0f, 0f, 0f, th,
			// th, th, 0.5f, 0f).withShots(1f);
			// ArrayList<FinalEntry> weights = new ArrayList<>();

			// weights = runWithSettingsList(sheet, current, we);

			// finals = Utils.cotRestrict(finals, 0.25f);
			// finals = Utils.certaintyRestrict(finals, 0.80f);
			// finals = Utils.allUnders(current);
			// finals = Utils.higherOdds(current);

			// finals = runBestTH(sheet, current, sheet.getSheetName(), year, 3,
			// "shots", temp);
			// ShotsSettings shotSetts = checkOUoptimality(sheet.getSheetName(),
			// year, 3, "shots");
			finals = Utils.noequilibriums(finals);

			// finals = Utils.intersectDiff(finals, finalsPFS);
			// if(shotSetts.doNotPlay)
			// finals = new ArrayList<>();
			// else if(shotSetts.onlyUnders)
			// finals = Utils.onlyUnders(finals);
			// else if(shotSetts.onlyOvers)
			// finals = Utils.onlyOvers(finals);
			// finals = Utils.cotRestrict(finals, 0.1f);
			// finals = Utils.onlyOvers(finals);

			// SQLiteJDBC.storeFinals(finals, year, sheet.getSheetName(),
			// "basic");

			// Settings temp = runForLeagueWithOdds(sheet, data, year, table,
			// basics, poissons, weighted, ht1, ht2, shots,
			// 0.55f).withValue(0.9f);

			//
			// ArrayList<Settings> list = runForBest(sheet, data, year,
			// table,
			// basics, poissons, weighted, ht1, ht2, 0.55f,
			// "all");
			//
			// finals = runWithSettingsList(sheet, data, temp, table);
			//
			// temp = findThreshold(sheet, finals, temp);
			// finals = restrict(finals, temp);
			//
			// temp = findIntervalReal(finals, year, temp);
			// finals = restrict(finals, temp);

			// temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals),
			// year, table, basics, poissons, weighted, ht1,
			// ht2, temp.threshold);
			// finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals),
			// temp, table);
			//
			// temp = findThreshold(sheet, finals, temp);
			// finals = restrict(finals, temp);
			//
			// temp = findIntervalReal(finals, year, temp);
			// finals = restrict(finals, temp);

			// temp = findValue(finals, sheet, temp);

			// float bestCot = Utils.bestCot(finals);
			// boolean flagShots = Utils.getProfit(Utils.shotsRestrict(finals,
			// sheet)) > temp.profit;
			// Pair poslimit = Utils.positionLimits(finals, table);
			// System.out.println(poslimit);

			// System.out.println(bestTH);
			// temp.threshold = bestTH;
			// temp.upperBound = bestTH;
			// temp.lowerBound = bestTH;
			// finals = runWithSettingsList(sheet, current, temp, table);
			// finals = Utils.onlyUnders(finals);

			// finals = Utils.cotRestrict(finals, bestCot);
			// finals = Utils.onlyOvers(finals);

			// temp.threshold = bestTH;
			// temp.upperBound = bestTH;
			// temp.lowerBound = bestTH;
			// if (i > maxMatchDay && !sheet.getSheetName().equals("SC0"))
			// finals = Utils.allOvers(current);
			// else {
			// finals = runWithSettingsList(sheet,
			// Utils.onlyFixtures(finals), temp, table);

			// finals = Utils.cotRestrict(finals, bestCot);
			// }
			// finals = restrict(finals, temp);
			//
			// SQLiteJDBC.storeFinals(finals, year, sheet.getSheetName(),
			// "pfs");
			played += finals.size();

			// System.out.println(finals);
			float trprofit = Utils.getProfit(finals);
			// System.out.println(i + " " + trprofit);
			// System.out.println(String.format("%.3f",
			// Utils.avgReturn(finals)));

			profit += trprofit;

		}

		// System.err.println("Played: " + played);
		float yield = (profit / played) * 100f;
		System.out.println("Profit for  " + sheet.getSheetName() + " " + year + " is: " + String.format("%.2f", profit)
				+ " yield is: " + String.format("%.2f%%", yield));

		return profit;
	}

	private static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<Fixture> current,
			Settings settings, ArrayList<PlayerFixture> pfs, HashMap<String, String> dictionary) throws ParseException {
		ArrayList<FinalEntry> finals = calculateScores(sheet, current, settings, pfs, dictionary);

		return restrict(finals, settings);
	}

	private static ArrayList<FinalEntry> calculateScores(HSSFSheet sheet, ArrayList<Fixture> all, Settings settings,
			ArrayList<PlayerFixture> pfs, HashMap<String, String> dictionary) throws ParseException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {

			float finalScore = 0f;

			if (settings.basic != 0f)
				finalScore += settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f);

			if (settings.poisson != 0f)
				finalScore += settings.poisson * poisson(f, sheet);

			if (settings.weightedPoisson != 0f)
				finalScore += settings.weightedPoisson * poissonWeighted(f, sheet);

			if (settings.htCombo != 0f)
				finalScore += settings.htCombo * (settings.halfTimeOverOne * halfTimeOnly(f, sheet, 1)
						+ (1f - settings.halfTimeOverOne) * halfTimeOnly(f, sheet, 2));

			if (settings.shots != 0f)
				finalScore += settings.shots * shots(f, sheet);

			FinalEntry fe = new FinalEntry(f, finalScore, new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
					settings.threshold, settings.lowerBound, settings.upperBound);
			// if (!fe.prediction.equals(Float.NaN))
			finals.add(fe);
		}
		return finals;
	}

	public static ArrayList<FinalEntry> runBestCot(HSSFSheet sheet, ArrayList<Fixture> current, String competition,
			int year, int period, String description, Settings temp) throws InterruptedException, ParseException {
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		result = Utils.cotRestrict(result, findCot(competition, year, period, description));

		return result;
	}

	public static ArrayList<FinalEntry> runBestTH(HSSFSheet sheet, ArrayList<Fixture> current, String competition,
			int year, int period, String description, Settings temp) throws InterruptedException, ParseException {
		float th = findTH(competition, year, period, description, temp);
		// System.out.println(th);
		temp.lowerBound = th;
		temp.upperBound = th;
		temp.threshold = th;

		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		return result;
	}

	public static ArrayList<FinalEntry> runBestCotandTH(HSSFSheet sheet, ArrayList<Fixture> current, String competition,
			int year, int period, String description, Settings temp) throws InterruptedException, ParseException {
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		Pair pair = findCOTandTH(sheet.getSheetName(), year, period, description, temp);
		float bestTH = pair.home;
		float bestCot = pair.away;

		result = Utils.cotRestrict(result, bestCot);

		temp.lowerBound = bestTH;
		temp.upperBound = bestTH;
		temp.threshold = bestTH;
		result = runWithSettingsList(sheet, Utils.onlyFixtures(result), temp);

		return result;
	}

	public static ArrayList<FinalEntry> runBestCotOUandTH(HSSFSheet sheet, ArrayList<Fixture> current,
			String competition, int year, int period, String description, Settings temp)
			throws InterruptedException, ParseException {
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		Triple triple = findCOTOUandTH(sheet.getSheetName(), year, period, description, temp);
		float bestTH = triple.first;

		result = Utils.cotRestrictOU(result, triple.pair);

		temp.lowerBound = bestTH;
		temp.upperBound = bestTH;
		temp.threshold = bestTH;
		result = runWithSettingsList(sheet, Utils.onlyFixtures(result), temp);

		return result;
	}

	private static Triple findCOTOUandTH(String league, int year, int period, String description, Settings initial)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfitOver = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfitOver += Utils.getProfit(Utils.onlyOvers(i));
		}

		float bestCotOver = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyOvers(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyOvers(i));

			if (currProfit > bestProfitOver) {
				bestProfitOver = currProfit;
				bestCotOver = cot;
			}

		}

		float bestProfitUnder = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfitUnder += Utils.getProfit(Utils.onlyUnders(i));
		}

		float bestCotUnder = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyUnders(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyUnders(i));

			if (currProfit > bestProfitUnder) {
				bestProfitUnder = currProfit;
				bestCotUnder = cot;
			}

		}

		Pair pair = Pair.of(5 * bestCotOver / 6, 5 * bestCotUnder / 6);

		ArrayList<ArrayList<FinalEntry>> cots = new ArrayList<>();
		byYear.stream().forEach(list -> cots.add(Utils.cotRestrictOU(list, pair)));

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : cots) {
			bestProfit += Utils.getProfit(i);
		}

		float bestThreshold = 0.55f;
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();

			cots.stream().forEach(list -> filtered.add(restrict(list, trset)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> j : filtered)
				currProfit += Utils.getProfit(j);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestThreshold = current;
			}
		}

		return Triple.of(bestThreshold, pair);

	}

	public static ArrayList<FinalEntry> runBestTHandCOT(HSSFSheet sheet, ArrayList<Fixture> current, String competition,
			int year, int period, String description, Settings temp) throws InterruptedException, ParseException {

		Pair pair = findTHandCOT(sheet.getSheetName(), year, period, description, temp);
		float bestTH = pair.home;
		float bestCot = pair.away;

		temp.lowerBound = bestTH;
		temp.upperBound = bestTH;
		temp.threshold = bestTH;
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		result = Utils.cotRestrict(result, bestCot);

		return result;
	}

	@Deprecated
	private static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<Fixture> all, int year, Table table,
			HashMap<Fixture, Float> basicMap, HashMap<Fixture, Float> poissonsMap, HashMap<Fixture, Float> weightedMap,
			HashMap<Fixture, Float> ht1Map, HashMap<Fixture, Float> ht2Map, HashMap<Fixture, Float> shotsMap,
			float initTH, String type) {

		boolean escapeFlag = sheet.getSheetName().equals("D1") || sheet.getSheetName().equals("D2");
		boolean flagShots = Arrays.asList(Constants.SHOTS).contains(sheet.getSheetName());

		float bestProfit = Float.NEGATIVE_INFINITY;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] ht1s = new float[all.size()];
		float[] ht2s = new float[all.size()];
		float[] htCombos = new float[all.size()];
		float[] shots = new float[all.size()];
		float[] similars = new float[all.size()];
		float[] similarPoissons = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
			String homeTeam = escapeFlag && f.homeTeam.contains("\'") ? f.homeTeam.replace("\'", "\\") : f.homeTeam;
			String awayTeam = escapeFlag && f.awayTeam.contains("\'") ? f.awayTeam.replace("\'", "\\") : f.awayTeam;

			Fixture key = new Fixture(f.date, f.competition, homeTeam, awayTeam, new Result(-1, -1));

			basics[i] = basicMap.get(key) == null ? Float.NaN : basicMap.get(key);
			poissons[i] = poissonsMap.get(key) == null ? Float.NaN : poissonsMap.get(key);
			weightedPoissons[i] = weightedMap.get(key) == null ? Float.NaN : weightedMap.get(key);
			ht1s[i] = ht1Map.get(key) == null ? Float.NaN : ht1Map.get(key);
			ht2s[i] = ht2Map.get(key) == null ? Float.NaN : ht2Map.get(key);
			// similars[i] = Utils.basicSimilar(f, sheet, table);
			// similarPoissons[i] = Utils.similarPoisson(f, sheet, table);
			if (flagShots)
				shots[i] = shotsMap.get(key) == null ? Float.NaN : shotsMap.get(key);
		}

		float overOneHT = /* checkOptimal(sheet, all, ht1s, ht2s, 0.2f, type) */0.3f;
		// float basicPart = checkOptimal(sheet, all, basics, similars, 0.5f,
		// type);
		// float poissonPart = checkOptimal(sheet, all, poissons,
		// similarPoissons, 0.5f, type);

		for (int i = 0; i < all.size(); i++) {
			htCombos[i] = (overOneHT * ht1s[i] + (1f - overOneHT) * ht2s[i]);
			// basics[i] = basicPart * basics[i] + (1f - basicPart) *
			// similars[i];
			// poissons[i] = poissonPart * poissons[i] + (1f - poissonPart) *
			// similarPoissons[i];
		}

		Settings best = new Settings(sheet.getSheetName(), 0f, 0f, 0f, initTH, initTH, initTH, 0f, bestProfit)
				.withSimilars(0).withValue(0.9f);

		// if (flagShots)
		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * shots[i] + y * 0.05f * basics[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), y * 0.05f, 0f, 0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f).withShots(x * 0.05f);
			float currentProfit = Utils.getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * shots[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), 0f, y * 0.05f, 0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f).withShots(x * 0.05f);
			float currentProfit = Utils.getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * shots[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), 0f, 0f, y * 0.05f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f).withShots(x * 0.05f);
			float currentProfit = Utils.getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * shots[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), 0f, 0f, 0f, initTH, initTH, initTH, 0.5f, bestProfit)
					.withValue(0.9f).withShots(x * 0.05f).withHT(overOneHT, y * 0.05f);
			float currentProfit = Utils.getProfit(finals);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}
		}

		best.profit = bestProfit;
		return best.withYear(year);

	}

	@Deprecated
	private static ArrayList<Settings> runForBest(HSSFSheet sheet, ArrayList<Fixture> all, int year, Table table,
			HashMap<Fixture, Float> basicMap, HashMap<Fixture, Float> poissonsMap, HashMap<Fixture, Float> weightedMap,
			HashMap<Fixture, Float> ht1Map, HashMap<Fixture, Float> ht2Map, float initTH, String type) {

		boolean escapeFlag = sheet.getSheetName().equals("D1") || sheet.getSheetName().equals("D2");
		boolean flagShots = /*
							 * Arrays.asList(MinMaxOdds.SHOTS).contains(sheet. getSheetName())
							 */false;

		float bestProfit = Float.NEGATIVE_INFINITY;

		ArrayList<Settings> list = new ArrayList<>();

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] ht1s = new float[all.size()];
		float[] ht2s = new float[all.size()];
		float[] htCombos = new float[all.size()];
		float[] shots = new float[all.size()];
		float[] similars = new float[all.size()];
		float[] similarPoissons = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
			String homeTeam = escapeFlag && f.homeTeam.contains("\'") ? f.homeTeam.replace("\'", "\\") : f.homeTeam;
			String awayTeam = escapeFlag && f.awayTeam.contains("\'") ? f.awayTeam.replace("\'", "\\") : f.awayTeam;

			Fixture key = new Fixture(f.date, f.competition, homeTeam, awayTeam, new Result(-1, -1));

			basics[i] = basicMap.get(key) == null ? Float.NaN : basicMap.get(key);
			poissons[i] = poissonsMap.get(key) == null ? Float.NaN : poissonsMap.get(key);
			weightedPoissons[i] = weightedMap.get(key) == null ? Float.NaN : weightedMap.get(key);
			ht1s[i] = ht1Map.get(key) == null ? Float.NaN : ht1Map.get(key);
			ht2s[i] = ht2Map.get(key) == null ? Float.NaN : ht2Map.get(key);
			// similars[i] = Utils.basicSimilar(f, sheet, table);
			// similarPoissons[i] = Utils.similarPoisson(f, sheet, table);
		}

		float overOneHT = checkOptimal(sheet, all, ht1s, ht2s, 0.5f);
		// float basicPart = checkOptimal(sheet, all, basics, similars, 0.5f,
		// type);
		// float poissonPart = checkOptimal(sheet, all, poissons,
		// similarPoissons, 0.5f, type);

		for (int i = 0; i < all.size(); i++) {
			htCombos[i] = (overOneHT * ht1s[i] + (1f - overOneHT) * ht2s[i]);
			// basics[i] = basicPart * basics[i] + (1f - basicPart) *
			// similars[i];
			// poissons[i] = poissonPart * poissons[i] + (1f - poissonPart) *
			// similarPoissons[i];
		}

		Settings best = new Settings(sheet.getSheetName(), 0f, 0f, 0f, initTH, initTH, initTH, 0f, bestProfit)
				.withSimilars(0).withValue(0.9f);

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f);
			set = set.withSimilars(0).withSimilarPoissons(0);
			float currentProfit = Utils.getProfit(finals);
			set.profit = currentProfit;
			list.add(set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * weightedPoissons[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f);
			set = set.withSimilars(0).withSimilarPoissons(0);
			float currentProfit = Utils.getProfit(finals);
			set.profit = currentProfit;
			list.add(set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * htCombos[i];

				float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
						: f.getMaxClosingUnderOdds().getUnderOdds();
				float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
				float value = certainty * gain;

				FinalEntry fe = new FinalEntry(f, finalScore,
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
				if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
					finals.add(fe);
			}

			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, 0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f).withHT(overOneHT, y * 0.05f);
			set = set.withSimilars(0).withSimilarPoissons(0);
			float currentProfit = Utils.getProfit(finals);
			set.profit = currentProfit;
			list.add(set);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				best = set;
			}
		}

		// for (int x = 0; x <= 20; x++) {
		// int y = 20 - x;
		// ArrayList<FinalEntry> finals = new ArrayList<>();
		// for (int i = 0; i < all.size(); i++) {
		// Fixture f = all.get(i);
		// float finalScore = x * 0.05f * poissons[i] + y * 0.05f * similars[i];
		//
		// float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds() :
		// f.getMaxClosingUnderOdds;
		// float certainty = finalScore > initTH ? finalScore : (1f -
		// finalScore);
		// float value = certainty * gain;
		//
		// FinalEntry fe = new FinalEntry(f, finalScore,
		// new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH,
		// initTH, initTH);
		// if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
		// finals.add(fe);
		// }
		//
		// Settings set = new Settings(sheet.getSheetName(), 0f, x * 0.05f, 0f,
		// initTH, initTH, initTH, bestWinPercent,
		// bestProfit).withSimilars(y * 0.05f).withValue(0.9f);
		// float currentProfit = Utils.getProfit(finals, set, type);
		// if (currentProfit > bestProfit) {
		// bestProfit = currentProfit;
		// best = set;
		// }
		//
		// }

		if (flagShots)
			for (int x = 0; x <= 20; x++) {
				int y = 20 - x;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 0; i < all.size(); i++) {
					Fixture f = all.get(i);
					float finalScore = x * 0.05f * basics[i] + y * 0.05f * shots[i];

					float gain = finalScore > initTH ? f.getMaxClosingOverOdds().getOverOdds()
							: f.getMaxClosingUnderOdds().getUnderOdds();
					float certainty = finalScore > initTH ? finalScore : (1f - finalScore);
					float value = certainty * gain;

					FinalEntry fe = new FinalEntry(f, finalScore,
							new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), initTH, initTH, initTH);
					if (!fe.prediction.equals(Float.NaN) && value > 0.9f)
						finals.add(fe);
				}

				Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, 0f, initTH, initTH, initTH, 0.5f,
						bestProfit).withValue(0.9f).withShots(y * 0.05f);
				float currentProfit = Utils.getProfit(finals);
				if (currentProfit > bestProfit) {
					bestProfit = currentProfit;
					best = set;
				}
			}

		best.profit = bestProfit;
		list.sort(new Comparator<Settings>() {

			@Override
			public int compare(Settings o1, Settings o2) {

				return ((Float) (o1.profit)).compareTo((Float) (o2.profit));
			}
		});

		return list;

	}

	private static Settings predictiveFromDB(HSSFSheet sheet, ArrayList<Fixture> all, int year, Table table,
			HashMap<Fixture, Float> basicMap, HashMap<Fixture, Float> poissonsMap, HashMap<Fixture, Float> weightedMap,
			HashMap<Fixture, Float> ht1Map, HashMap<Fixture, Float> ht2Map, float initTH, String type, int maxMatchDay)
			throws ParseException {

		boolean escapeFlag = sheet.getSheetName().equals("D1") || sheet.getSheetName().equals("D2");
		boolean flagShots = /*
							 * Arrays.asList(MinMaxOdds.SHOTS).contains(sheet. getSheetName())
							 */false;

		float bestProfit = Float.NEGATIVE_INFINITY;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		float[] ht1s = new float[all.size()];
		float[] ht2s = new float[all.size()];
		float[] htCombos = new float[all.size()];
		float[] shots = new float[all.size()];
		float[] similars = new float[all.size()];
		float[] similarPoissons = new float[all.size()];

		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);
			String homeTeam = escapeFlag && f.homeTeam.contains("\'") ? f.homeTeam.replace("\'", "\\") : f.homeTeam;
			String awayTeam = escapeFlag && f.awayTeam.contains("\'") ? f.awayTeam.replace("\'", "\\") : f.awayTeam;

			Fixture key = new Fixture(f.date, f.competition, homeTeam, awayTeam, new Result(-1, -1));

			basics[i] = basicMap.get(key) == null ? Float.NaN : basicMap.get(key);
			poissons[i] = poissonsMap.get(key) == null ? Float.NaN : poissonsMap.get(key);
			weightedPoissons[i] = weightedMap.get(key) == null ? Float.NaN : weightedMap.get(key);
			ht1s[i] = ht1Map.get(key) == null ? Float.NaN : ht1Map.get(key);
			ht2s[i] = ht2Map.get(key) == null ? Float.NaN : ht2Map.get(key);
			similars[i] = Utils.basicSimilar(f, sheet, table);
			similarPoissons[i] = Utils.similarPoisson(f, sheet, table);
		}

		float overOneHT = checkOptimal(sheet, all, ht1s, ht2s, 0.5f);
		float basicPart = checkOptimal(sheet, all, basics, similars, 0.5f);
		float poissonPart = checkOptimal(sheet, all, poissons, similarPoissons, 0.5f);

		for (int i = 0; i < all.size(); i++) {
			htCombos[i] = (overOneHT * ht1s[i] + (1f - overOneHT) * ht2s[i]);
			basics[i] = basicPart * basics[i] + (1f - basicPart) * similars[i];
			poissons[i] = poissonPart * poissons[i] + (1f - poissonPart) * similarPoissons[i];
		}

		Settings best = new Settings(sheet.getSheetName(), 0f, 0f, 0f, initTH, initTH, initTH, 0f, bestProfit)
				.withSimilars(1f - basicPart).withValue(0.9f);

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;

			float profit_curr = 0;
			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, y * 0.05f, 0.0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f);
			set = set.withSimilars(1f - basicPart).withSimilarPoissons(1f - poissonPart);

			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<? extends Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit_curr += Utils.getProfit(finals);

			}

			if (profit_curr > bestProfit) {
				bestProfit = profit_curr;
				best = set;
				best.profit = profit_curr;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;

			float profit_curr = 0;
			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, y * 0.05f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f);
			set = set.withSimilars(1f - basicPart).withSimilarPoissons(1f - poissonPart);

			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit_curr += Utils.getProfit(finals);

			}

			if (profit_curr > bestProfit) {
				bestProfit = profit_curr;
				best = set;
				best.profit = profit_curr;
			}

		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;

			float profit_curr = 0;
			Settings set = new Settings(sheet.getSheetName(), x * 0.05f, 0f, 0f, initTH, initTH, initTH, 0.5f,
					bestProfit).withValue(0.9f).withHT(overOneHT, y * 0.05f);
			set = set.withSimilars(1f - basicPart).withSimilarPoissons(1f - poissonPart);

			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 11; i < maxMatchDay; i++) {
				ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
				finals = runWithSettingsList(sheet, current, set);
				profit_curr += Utils.getProfit(finals);

			}

			if (profit_curr > bestProfit) {
				bestProfit = profit_curr;
				best = set;
				best.profit = profit_curr;
			}

		}

		best.profit = bestProfit;
		return best.withYear(year);

	}

	public static ArrayList<FinalEntry> triples(HSSFSheet sheet, int year)
			throws IOException, InterruptedException, ParseException {
		ArrayList<FinalEntry> toBet = new ArrayList<>();

		ArrayList<Fixture> all = selectAllAll(sheet);

		HashMap<Fixture, Float> basics = SQLiteJDBC.selectScores(all, "BASICS", year, sheet.getSheetName());
		HashMap<Fixture, Float> poissons = SQLiteJDBC.selectScores(all, "POISSON", year, sheet.getSheetName());
		HashMap<Fixture, Float> weighted = SQLiteJDBC.selectScores(all, "WEIGHTED", year, sheet.getSheetName());
		HashMap<Fixture, Float> ht1 = SQLiteJDBC.selectScores(all, "HALFTIME1", year, sheet.getSheetName());
		HashMap<Fixture, Float> ht2 = SQLiteJDBC.selectScores(all, "HALFTIME2", year, sheet.getSheetName());

		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 15; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);
			Settings temp = /*
							 * runForLeagueWithOdds(sheet, data, year, basics, poissons, weighted, ht1, ht2,
							 * 0.55f) .withValue(0.9f)
							 */ new Settings(sheet.getSheetName(), 0.5f, 0.5f, 0f, 0.55f, 0.55f, 0.55f, 0.5f, 0f);

			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			// temp = findThreshold(sheet, finals, temp);
			// finals = restrict(finals, temp);
			//
			// temp = findIntervalReal(finals, year, temp);
			// finals = restrict(finals, temp);
			//
			// temp = runForLeagueWithOdds(sheet, Utils.onlyFixtures(finals),
			// year, temp.threshold);
			// finals = runWithSettingsList(sheet, Utils.onlyFixtures(finals),
			// temp);
			//
			// temp = findThreshold(sheet, finals, temp);
			// finals = restrict(finals, temp);
			//
			// temp = findIntervalReal(finals, year, temp);
			// finals = restrict(finals, temp);
			//
			// temp = findValue(finals, sheet, temp);

			finals = runWithSettingsList(sheet, current, temp);

			// float drawPercent = ((float) Utils.countDraws(data)) /
			// data.size();
			// ArrayList<FinalEntry> draws = new ArrayList<>();
			// for (FinalEntry fe : finals)
			// if (poissonDraw(fe.fixture, sheet) >= drawPercent)
			// draws.add(fe);
			// finals = Utils.cotRestrict(finals, 0.15f);
			toBet.addAll(finals);
		}
		return toBet;
	}

	public static float realisticIntersect(HSSFSheet sheet, int year) throws IOException, ParseException {
		float profit = 0.0f;
		ArrayList<Fixture> all = selectAllAll(sheet);
		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 11; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<Fixture> data = FixtureUtils.getBeforeMatchday(all, i);
			// ------------------------------------------------------------
			// ArrayList<FinalEntry> finals = intersectAllClassifier(sheet,
			// data, year);
			ArrayList<FinalEntry> finalsBasic = new ArrayList<>();
			ArrayList<FinalEntry> finalsPoisson = new ArrayList<>();
			ArrayList<FinalEntry> finalsWeighted = new ArrayList<>();
			ArrayList<FinalEntry> finalsHT2 = new ArrayList<>();
			ArrayList<FinalEntry> finalsDraw = new ArrayList<>();

			for (int j = 0; j < data.size(); j++) {
				Fixture f = data.get(j);
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
			Settings basicThreshold = findThreshold(finalsBasic, set, MaximizingBy.BOTH);
			Settings basicPoisson = findThreshold(finalsPoisson, set, MaximizingBy.BOTH);
			Settings basicWeighted = findThreshold(finalsWeighted, set, MaximizingBy.BOTH);
			Settings basicHT2 = findThreshold(finalsHT2, set, MaximizingBy.BOTH);
			Settings basicDraw = findThreshold(finalsDraw, set, MaximizingBy.BOTH);
			ArrayList<FinalEntry> finals;
			// ArrayList<FinalEntry> finals = Utils.intersectVotes(finalsBasic,
			// finalsPoisson, finalsWeighted);

			// ------------------------------------------------------------
			// Settings temp = findThreshold(sheet, finals,
			// new Settings(sheet.getSheetName(), 1f, 0f, 0f, 0.55f, 0.55f,
			// 0.55f, 1, 10, 0, -100f));

			finals = intersectAllClassifier(sheet, current, year, basicThreshold.threshold, basicPoisson.threshold,
					basicWeighted.threshold, basicHT2.threshold, basicDraw.threshold);
			Settings temp = new Settings(sheet.getSheetName(), 1f, 0f, 0f, 0.55f, 0.55f, 0.55f, 0, 0);
			float trprofit = Utils.getProfit(finals);
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
				float currentProfit = Utils.getProfit(sofar);
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
				float currentProfit = Utils.getProfit(sofarLower);
				if (currentProfit > bestProfitLower) {
					bestProfitLower = currentProfit;
					bestLower = current;
				}
				current -= 0.025d;
			}
		}

		trset.upperBound = bestUpper;
		trset.lowerBound = bestLower;
		float bestFinalProfit = Utils.getProfit(Utils.filterTrust(finals, trset));
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

	/**
	 * Finds the best threshold for the given finals, currently it should be
	 * imutable considering the given finals properties
	 * 
	 * @param finals
	 * @param initial
	 * @return
	 */
	public static Settings findThreshold(ArrayList<FinalEntry> finals, Settings initial, MaximizingBy maxBy) {
		// System.out.println("thold: " + initial.threshold + " profit: " +
		// initial.profit);
		if (finals.isEmpty())
			return new Settings(initial).withYear(initial.year);
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo).withShots(initial.shots);

		float bestProfit = initial.profit;
		float bestThreshold = initial.threshold;

		for (int i = 0; i <= 80; i++) {
			float current = 0.30f + i * 0.005f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<FinalEntry> result = restrict(finals, trset);

			if (maxBy.equals(MaximizingBy.UNDERS))
				finals = Utils.onlyUnders(finals);
			else if (maxBy.equals(MaximizingBy.OVERS))
				finals = Utils.onlyOvers(finals);

			float currentProfit = Utils.getProfit(result);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestThreshold = current;
			}
		}

		trset.profit = bestProfit;
		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;

		finals = restrict(finals, initial);
		return trset;

	}

	static int addMatchDay(HSSFSheet sheet, ArrayList<Fixture> all) throws ParseException {
		int max = -1;
		for (Fixture f : all) {
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
		float profitUnders = Utils.getProfit(unders);
		underSetts.profit = profitUnders;

		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterByOdds(unders, currentMin, 10f);
			float currentProfit = Utils.getProfit(filtered);

			if (currentProfit > profitUnders) {
				bestminx = x;
				profitUnders = currentProfit;
				underSetts.minUnder = currentMin;
				underSetts.profit = profitUnders;
			}
		}

		for (int x = bestminx; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filteredMax = Utils.filterByOdds(unders, underSetts.minUnder, currentMax);
			float currentProfit = Utils.getProfit(filteredMax);

			if (currentProfit > profitUnders) {
				profitUnders = currentProfit;
				underSetts.maxUnder = currentMax;
				underSetts.profit = profitUnders;
			}

		}

		Settings overSetts = new Settings(initial);
		ArrayList<FinalEntry> overs = Utils.onlyOvers(finals);
		float profitOvers = Utils.getProfit(overs);
		overSetts.profit = profitOvers;

		int bestminy = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterByOdds(overs, currentMin, 10f);
			float currentProfit = Utils.getProfit(filtered);

			if (currentProfit > profitOvers) {
				bestminy = x;
				profitOvers = currentProfit;
				overSetts.minOver = currentMin;
				overSetts.profit = profitOvers;
			}
		}

		for (int x = bestminy; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filteredMax = Utils.filterByOdds(overs, overSetts.minOver, currentMax);
			float currentProfit = Utils.getProfit(filteredMax);

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
				filtered.add(Utils.filterByOdds(under, currentMin, 10f));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter);

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
				filteredMax.add(Utils.filterByOdds(under, newSetts.minUnder, currentMax));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter);

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
				filtered.add(Utils.filterByOdds(under, currentMin, 10f));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter);

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
				filteredMax.add(Utils.filterByOdds(under, newSetts.minUnder, currentMax));

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter);

			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				newSetts.maxUnder = currentMax;
				newSetts.profit = bestProfit;
			}
		}

		return newSetts.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo)
				.withValue(initial.value);
	}

	// needs testing
	public static Settings findValue(ArrayList<FinalEntry> finals, HSSFSheet sheet, Settings initial) {
		float profit = initial.profit;
		Settings newSetts = new Settings(initial);
		Settings best = new Settings(initial);
		float bestValue = 0.9f;

		for (int x = 0; x <= 30; x++) {
			float currentValue = 0.7f + x * 0.02f;
			newSetts.value = currentValue;
			ArrayList<FinalEntry> vals = restrict(finals, newSetts);
			float currentProfit = Utils.getProfit(vals);
			System.out.println(Utils.evaluateRecord(vals));
			if (currentProfit > profit) {
				bestValue = currentValue;
				profit = currentProfit;
				best.value = bestValue;
				best.profit = currentProfit;
			}
		}

		return best.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo).withValue(bestValue);
	}

	public static void populateScores(int year) throws IOException, ParseException {
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(
				new File(base + "/data/all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		Iterator<Sheet> sheet = workbook.sheetIterator();

		while (sheet.hasNext()) {
			HSSFSheet sh = (HSSFSheet) sheet.next();
			if (!Arrays.asList(Constants.SHOTS).contains(sh.getSheetName()))
				continue;
			ArrayList<Fixture> all = selectAllAll(sh);

			SQLiteJDBC.insertBasic(sh, all, year, "SHOTS");
			// SQLiteJDBC.insertBasic(sh, all, year, "BASICS");
			// SQLiteJDBC.insertBasic(sh, all, year, "POISSON");
			// SQLiteJDBC.insertBasic(sh, all, year, "WEIGHTED");
			// SQLiteJDBC.insertBasic(sh, all, year, "HALFTIME1");
			// SQLiteJDBC.insertBasic(sh, all, year, "HALFTIME2");
		}
		workbook.close();
		file.close();
	}

	public static float findCot(String league, int year, int period, String description) throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfit += Utils.getProfit(i);
		}

		float bestCot = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(list, cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(i);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestCot = cot;
			}

		}

		return 5 * bestCot / 6;

	}

	public static Pair findCotOU(String league, int year, int period, String description) throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfitOver = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfitOver += Utils.getProfit(Utils.onlyOvers(i));
		}

		float bestCotOver = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyOvers(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyOvers(i));

			if (currProfit > bestProfitOver) {
				bestProfitOver = currProfit;
				bestCotOver = cot;
			}

		}

		float bestProfitUnder = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfitUnder += Utils.getProfit(Utils.onlyUnders(i));
		}

		float bestCotUnder = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyUnders(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyUnders(i));

			if (currProfit > bestProfitUnder) {
				bestProfitUnder = currProfit;
				bestCotUnder = cot;
			}

		}

		return Pair.of(5 * bestCotOver / 6, 5 * bestCotUnder / 6);

	}

	public static float findTH(String league, int year, int period, String description, Settings initial)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfit += Utils.getProfit(i);
		}

		float bestThreshold = 0.55f;
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();

			byYear.stream().forEach(list -> filtered.add(restrict(list, trset)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> j : filtered)
				currProfit += Utils.getProfit(j);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestThreshold = current;
			}
		}

		return bestThreshold;

	}

	public static Pair findTHandCOT(String league, int year, int period, String description, Settings initial)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfit += Utils.getProfit(i);
		}

		float bestThreshold = 0.55f;
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();

			byYear.stream().forEach(list -> filtered.add(restrict(list, trset)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> j : filtered)
				currProfit += Utils.getProfit(j);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestThreshold = current;
			}
		}

		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;

		ArrayList<ArrayList<FinalEntry>> ths = new ArrayList<>();
		byYear.stream().forEach(list -> ths.add(restrict(list, trset)));

		bestProfit = 0f;
		for (ArrayList<FinalEntry> i : ths) {
			bestProfit += Utils.getProfit(i);
		}

		float bestCot = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			ths.stream().forEach(list -> filtered.add(Utils.cotRestrict(list, cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(i);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestCot = cot;
			}

		}

		return Pair.of(bestThreshold, 5 * bestCot / 6);

	}

	public static Pair findCOTandTH(String league, int year, int period, String description, Settings initial)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfit += Utils.getProfit(i);
		}

		float bestCot = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			byYear.stream().forEach(list -> filtered.add(Utils.cotRestrict(list, cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(i);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestCot = cot;
			}

		}

		float finalcot = 5 * bestCot / 6;
		ArrayList<ArrayList<FinalEntry>> cots = new ArrayList<>();
		byYear.stream().forEach(list -> cots.add(Utils.cotRestrict(list, finalcot)));

		bestProfit = 0f;
		for (ArrayList<FinalEntry> i : cots) {
			bestProfit += Utils.getProfit(i);
		}

		float bestThreshold = 0.55f;
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();

			cots.stream().forEach(list -> filtered.add(restrict(list, trset)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> j : filtered)
				currProfit += Utils.getProfit(j);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestThreshold = current;
			}
		}

		return Pair.of(bestThreshold, 5 * bestCot / 6);

	}

	// needs checking of filterbyodds related code if ever actually used
	@Deprecated
	public static ArrayList<FinalEntry> bestCot(String league, int year, int period, String description)
			throws InterruptedException {

		float bestCot = findCot(league, year, period, description);

		ArrayList<FinalEntry> result = SQLiteJDBC.selectFinals(league, year, description);

		ArrayList<FinalEntry> unders = Utils.onlyUnders(result);
		ArrayList<FinalEntry> overs = Utils.onlyOvers(result);
		ArrayList<FinalEntry> oddsList = new ArrayList<>();
		Settings oddsSetts = bestOdds(league, year, period, description);

		oddsList.addAll(Utils.filterByOdds(unders, oddsSetts.minUnder, oddsSetts.maxUnder));
		oddsList.addAll(Utils.filterByOdds(overs, oddsSetts.minOver, oddsSetts.maxOver));

		result = Utils.cotRestrict(result, bestCot);

		ArrayList<FinalEntry> values = new ArrayList<>();
		for (FinalEntry fe : result) {
			float gain = fe.prediction > fe.threshold ? fe.fixture.getMaxClosingOverOdds().getOverOdds()
					: fe.fixture.getMaxClosingUnderOdds().getUnderOdds();
			float certainty = fe.prediction > fe.threshold ? fe.prediction : (1f - fe.prediction);
			float value = certainty * gain;
			if (certainty > 0.5f)
				values.add(fe);
		}

		float profit = Utils.getProfit(values);

		// System.out.println(" Last: " + period + " Best cot: " + bestCot + "
		// best profit avg: " + bestProfit / period
		// + " result: " + profit);

		return values;

	}

	public static Settings bestOdds(String league, int year, int period, String description)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(SQLiteJDBC.selectFinals(league, i, description));
		}

		float bestProfitUnders = 0f;
		float bestProfitOvers = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfitUnders += Utils.getProfit(Utils.onlyUnders(i));
			bestProfitOvers += Utils.getProfit(Utils.onlyOvers(i));
		}

		Settings underSetts = new Settings(league, 0.5f, 0.5f, 0f, 0.55f, 0.55f, 0.55f, 0.5f, bestProfitUnders);

		ArrayList<ArrayList<FinalEntry>> unders = new ArrayList<>();
		for (ArrayList<FinalEntry> yearly : byYear)
			unders.add(Utils.onlyUnders(yearly));

		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;

			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders) {
				float th = 1f;
				filtered.add(Utils.filterByOdds(under, currentMin, 10f));
			}

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter);

			if (currentProfit > bestProfitUnders) {
				bestminx = x;
				bestProfitUnders = currentProfit;
				underSetts.minUnder = currentMin;
				underSetts.profit = bestProfitUnders;
			}
		}

		for (int x = bestminx; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<ArrayList<FinalEntry>> filteredMax = new ArrayList<>();
			for (ArrayList<FinalEntry> under : unders) {
				float th = 1f;
				filteredMax.add(Utils.filterByOdds(under, underSetts.minUnder, currentMax));
			}

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter);

			if (currentProfit > bestProfitUnders) {
				bestProfitUnders = currentProfit;
				underSetts.maxUnder = currentMax;
				underSetts.profit = bestProfitUnders;
			}
		}

		Settings overSetts = new Settings(league, 0.5f, 0.5f, 0f, 0.55f, 0.55f, 0.55f, 0.5f, bestProfitOvers);

		ArrayList<ArrayList<FinalEntry>> overs = new ArrayList<>();
		for (ArrayList<FinalEntry> yearly : byYear)
			overs.add(Utils.onlyOvers(yearly));
		int bestminy = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;

			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			for (ArrayList<FinalEntry> over : overs) {
				float th = 0f;
				filtered.add(Utils.filterByOdds(over, currentMin, 10f));
			}

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filtered)
				currentProfit += Utils.getProfit(filter);

			if (currentProfit > bestProfitOvers) {
				bestminy = x;
				bestProfitOvers = currentProfit;
				overSetts.minOver = currentMin;
				overSetts.profit = bestProfitOvers;
			}
		}

		for (int x = bestminy; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<ArrayList<FinalEntry>> filteredMax = new ArrayList<>();
			for (ArrayList<FinalEntry> over : overs) {
				float th = 0f;

				filteredMax.add(Utils.filterByOdds(over, overSetts.minOver, currentMax));
			}

			float currentProfit = 0f;
			for (ArrayList<FinalEntry> filter : filteredMax)
				currentProfit += Utils.getProfit(filter);

			if (currentProfit > bestProfitOvers) {
				bestProfitOvers = currentProfit;
				overSetts.maxOver = currentMax;
				overSetts.profit = bestProfitOvers;
			}
		}

		return underSetts.withMinMax(underSetts.minUnder, underSetts.maxUnder, overSetts.minOver, overSetts.maxOver);

	}

	public static ArrayList<FinalEntry> finalsShots(ArrayList<Fixture> all, String competition, int year)
			throws InterruptedException, ParseException {
		ArrayList<FinalEntry> result = new ArrayList<>();

		Settings temp = Settings.shots(competition);
		int maxMatchDay = FixtureUtils.addMatchDay(all);

		for (int i = /* dictionary == null ? 100 : */ 14; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);

			ArrayList<FinalEntry> finals = new ArrayList<>();

			finals = FixtureUtils.runWithSettingsList(all, current, temp);
			finals = Utils.noequilibriums(finals);
			finals = Utils.onlyUnders(finals);

			result.addAll(finals);
		}

		return result;
	}

	private static ShotsSettings checkOUoptimality(String league, int year, int period, String description)
			throws InterruptedException {
		ShotsSettings setts = new ShotsSettings();

		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();
		ArrayList<ArrayList<FinalEntry>> byYearOvers = new ArrayList<>();
		ArrayList<ArrayList<FinalEntry>> byYearUnders = new ArrayList<>();
		int start = year - period;

		for (int i = start; i < year; i++) {
			ArrayList<FinalEntry> finals = Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description));
			byYear.add(finals);
			byYearOvers.add(Utils.onlyOvers(finals));
			byYearUnders.add(Utils.onlyUnders(finals));
		}

		float profit = 0f;
		float profitOvers = 0f;
		float profitUnders = 0f;

		float avgOdds = 0f;
		float avgOddsOvers = 0f;
		float avgOddsUnders = 0f;

		int size = 0;
		int sizeOvers = 0;
		int sizeUnders = 0;

		for (ArrayList<FinalEntry> i : byYear) {
			profit += Utils.getProfit(i);
			avgOdds += Utils.getAvgOdds(i);
			size += i.size();
		}

		for (ArrayList<FinalEntry> i : byYearOvers) {
			profitOvers += Utils.getProfit(i);
			avgOddsOvers += Utils.getAvgOdds(i);
			sizeOvers += i.size();
		}

		for (ArrayList<FinalEntry> i : byYearUnders) {
			profitUnders += Utils.getProfit(i);
			avgOddsUnders += Utils.getAvgOdds(i);
			sizeUnders += i.size();
		}

		// if (size > 0 && sizeOvers > 0 && sizeUnders > 0) {
		float pValue = profit < 0 ? -1f : Utils.pValueCalculator(size, profit / size, avgOdds / period);
		float pValueOvers = profitOvers < 0 ? -1f
				: Utils.pValueCalculator(sizeOvers, profitOvers / sizeOvers, avgOddsOvers / period);
		float pValueUnders = profitUnders < 0 ? -1f
				: Utils.pValueCalculator(sizeUnders, profitUnders / sizeUnders, avgOddsUnders / period);

		if (pValueOvers > pValue && pValueOvers > pValueUnders)
			setts.onlyOvers();
		else if (pValueUnders > pValue && pValueUnders > pValueOvers)
			setts.onlyUnders();
		if (pValueOvers < 0 && pValueUnders < 0)
			setts.doNotPlay();

		return setts;

	}

	public static ArrayList<FinalEntry> runBestTHandCotOU(HSSFSheet sheet, ArrayList<Fixture> current,
			String competition, int year, int period, String description, Settings temp)
			throws InterruptedException, ParseException {

		Pair pair = findTHandCOTOU(sheet.getSheetName(), year, period, description, temp);
		float bestTH = findTH(sheet.getSheetName(), year, period, description, temp);

		temp.lowerBound = bestTH;
		temp.upperBound = bestTH;
		temp.threshold = bestTH;
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		result = Utils.cotRestrictOU(result, pair);

		return result;
	}

	public static Pair findTHandCOTOU(String league, int year, int period, String description, Settings initial)
			throws InterruptedException {
		ArrayList<ArrayList<FinalEntry>> byYear = new ArrayList<>();

		int start = year - period;

		for (int i = start; i < year; i++) {
			byYear.add(Utils.noequilibriums(SQLiteJDBC.selectFinals(league, i, description)));
		}

		float bestProfit = 0f;
		for (ArrayList<FinalEntry> i : byYear) {
			bestProfit += Utils.getProfit(i);
		}

		float bestThreshold = 0.55f;
		Settings trset = new Settings(initial).withYear(initial.year).withValue(initial.value)
				.withHT(initial.halfTimeOverOne, initial.htCombo);

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();

			byYear.stream().forEach(list -> filtered.add(restrict(list, trset)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> j : filtered)
				currProfit += Utils.getProfit(j);

			if (currProfit > bestProfit) {
				bestProfit = currProfit;
				bestThreshold = current;
			}
		}

		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;

		ArrayList<ArrayList<FinalEntry>> ths = new ArrayList<>();
		byYear.stream().forEach(list -> ths.add(restrict(list, trset)));

		float bestProfitOver = 0f;
		for (ArrayList<FinalEntry> i : ths) {
			bestProfitOver += Utils.getProfit(Utils.onlyOvers(i));
		}

		float bestCotOver = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			ths.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyOvers(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyOvers(i));

			if (currProfit > bestProfitOver) {
				bestProfitOver = currProfit;
				bestCotOver = cot;
			}

		}

		float bestProfitUnder = 0f;
		for (ArrayList<FinalEntry> i : ths) {
			bestProfitUnder += Utils.getProfit(Utils.onlyUnders(i));
		}

		float bestCotUnder = 0f;

		for (int j = 1; j <= 12; j++) {
			ArrayList<ArrayList<FinalEntry>> filtered = new ArrayList<>();
			float cot = j * 0.02f;
			ths.stream().forEach(list -> filtered.add(Utils.cotRestrict(Utils.onlyUnders(list), cot)));

			float currProfit = 0f;
			for (ArrayList<FinalEntry> i : filtered)
				currProfit += Utils.getProfit(Utils.onlyUnders(i));

			if (currProfit > bestProfitUnder) {
				bestProfitUnder = currProfit;
				bestCotUnder = cot;
			}

		}

		return Pair.of(5 * bestCotOver / 6, 5 * bestCotUnder / 6);

	}

	private static ArrayList<FinalEntry> runBestCotOU(HSSFSheet sheet, ArrayList<Fixture> current, String competition,
			int year, int period, String description, Settings temp) throws ParseException, InterruptedException {
		ArrayList<FinalEntry> result = runWithSettingsList(sheet, current, temp);

		result = Utils.cotRestrictOU(result, findCotOU(competition, year, period, description));

		return result;
	}

	public static synchronized void storeInExcel(ArrayList<Fixture> all, String competition, int year, String table)
			throws IOException, InterruptedException {
		DateFormat XLSformat = new SimpleDateFormat("d.M.yyyy HH:mm");
		String base = new File("").getAbsolutePath();

		all.sort(new Comparator<Fixture>() {

			@Override
			public int compare(Fixture o1, Fixture o2) {
				return o1.date.compareTo(o2.date);
			}
		});

		FileInputStream file = new FileInputStream(new File(base + "/data/" + table + "" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		if (workbook.getSheet(competition) != null)
			workbook.removeSheetAt(workbook.getSheetIndex(competition));

		HSSFSheet sheet = workbook.createSheet(competition);

		CreationHelper createHelper = workbook.getCreationHelper();
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("d.M.yyyy HH:mm"));

		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("Date");
		row0.createCell(1).setCellValue("HomeTeam");
		row0.createCell(2).setCellValue("AwayTeam");
		row0.createCell(3).setCellValue("FTHG");
		row0.createCell(4).setCellValue("FTAG");
		row0.createCell(5).setCellValue("HTHG");
		row0.createCell(6).setCellValue("HTAG");
		row0.createCell(7).setCellValue("HST");
		row0.createCell(8).setCellValue("AST");
		row0.createCell(9).setCellValue("PSH");
		row0.createCell(10).setCellValue("PSD");
		row0.createCell(11).setCellValue("PSA");
		row0.createCell(12).setCellValue("BbMx>2.5");
		row0.createCell(13).setCellValue("BbAv>2.5");
		row0.createCell(14).setCellValue("BbAv<2.5");
		row0.createCell(15).setCellValue("BbMx<2.5");
		row0.createCell(16).setCellValue("BbAHh");
		row0.createCell(17).setCellValue("BbMxAHH");
		row0.createCell(18).setCellValue("BbMxAHA");

		for (int r = 1; r < all.size() + 1; r++) {
			HSSFRow row = sheet.createRow(r);

			Fixture ef = all.get(r - 1);

			HSSFCell cellDate = row.createCell(0);
			cellDate.setCellValue(XLSformat.format(ef.date));
			cellDate.setCellStyle(cellStyle);

			row.createCell(1).setCellValue(ef.homeTeam);
			row.createCell(2).setCellValue(ef.awayTeam);
			row.createCell(3).setCellValue(ef.result.goalsHomeTeam);
			row.createCell(4).setCellValue(ef.result.goalsAwayTeam);
			row.createCell(5).setCellValue(ef.HTresult.goalsHomeTeam);
			row.createCell(6).setCellValue(ef.HTresult.goalsAwayTeam);
			row.createCell(7).setCellValue(ef.gameStats.getShotsHome());
			row.createCell(8).setCellValue(ef.gameStats.getShotsAway());
			row.createCell(9).setCellValue(ef.getMaxClosingHomeOdds());
			row.createCell(10).setCellValue(ef.getMaxClosingDrawOdds());
			row.createCell(11).setCellValue(ef.getMaxClosingAwayOdds());
			row.createCell(12).setCellValue(ef.getMaxClosingOverOdds().getOverOdds());
			row.createCell(13).setCellValue(ef.getMaxClosingOverOdds().getOverOdds());
			row.createCell(14).setCellValue(ef.getMaxClosingUnderOdds().getUnderOdds());
			row.createCell(15).setCellValue(ef.getMaxClosingUnderOdds().getUnderOdds());
			row.createCell(16).setCellValue(ef.getOptimalAsianLine());
			row.createCell(17).setCellValue(ef.getMaxClosingAsian().home);
			row.createCell(18).setCellValue(ef.getMaxClosingAsian().away);

		}

		int count = 0;
		int maxTries = 10;
		FileOutputStream fileOut = null;
		while (true) {
			try {
				fileOut = new FileOutputStream(base + "/data/" + table + "" + year + ".xls");
				break;
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(40000);
				if (++count == maxTries)
					throw e;
			}
		}

		// write this workbook to an Outputstream.
		workbook.write(fileOut);
		workbook.close();
		fileOut.flush();
		fileOut.close();
	}

	public static void combine(String competition, int year, String table)
			throws IOException, ParseException, InterruptedException {
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File(base + "/data/odds" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		ArrayList<Fixture> odds = selectAll(workbook.getSheet(competition), 0);
		ArrayList<Fixture> pending = new ArrayList<>();
		ArrayList<Fixture> finished = new ArrayList<>();
		for (Fixture i : odds) {
			if (i.result.goalsHomeTeam == -1)
				pending.add(i);
			else
				finished.add(i);
		}

		odds = finished;
		System.out.println("odds size " + odds.size() + " pending: " + pending.size());

		FileInputStream fileWay;
		HSSFSheet sheet;
		if (table.equals("all-data")) {
			fileWay = new FileInputStream(new File(base + "/data/all-euro-data-" + year + "-" + (year + 1) + ".xls"));
		} else {
			fileWay = new FileInputStream(new File(base + "/data/manual" + year + ".xls"));
		}
		HSSFWorkbook workbookWay = new HSSFWorkbook(fileWay);
		if (table.equals("all-data")) {
			sheet = workbookWay.getSheet(Constants.equivalents.get(competition));
		} else {
			sheet = workbookWay.getSheet(competition);
		}

		ArrayList<Fixture> ways = selectAll(sheet, 0);

		System.out.println("ways size " + ways.size());

		FixtureListCombiner combiner = new FixtureListCombiner(odds, ways, competition);
		ArrayList<? extends Fixture> combinedGeneric = combiner.combineWithDictionary();

		ArrayList<Fixture> combined = combinedGeneric.stream().map(Fixture.class::cast)
				.collect(Collectors.toCollection(ArrayList::new));

		System.out.println(combined.size());
		// System.out.println(combined);
		if (combined.size() == odds.size()) {
			combined.addAll(pending);
			storeInExcel(combined, competition, year, "odds");
		}
		workbook.close();
		workbookWay.close();

	}

	public static ArrayList<Fixture> combine(ArrayList<Fixture> odds, ArrayList<Fixture> ways, String competition) {

		ArrayList<Fixture> result = new ArrayList<>();

		for (Fixture i : odds) {
			Fixture match = findCorresponding(i, ways, competition);
			if (match == null) {
				System.out.println(i);
				break;
			} else
				result.add(match);

		}

		return result;
	}

	private static Fixture findCorresponding(Fixture oddsFixture, ArrayList<Fixture> ways, String competition) {

		for (Fixture i : ways) {
			if (oddsFixture.homeTeam.equals(Names.getOddsName(competition, i.homeTeam))
					&& oddsFixture.awayTeam.equals(Names.getOddsName(competition, i.awayTeam))
					&& (oddsFixture.date.equals(i.date) || oddsFixture.date.equals(Utils.getYesterday(i.date))
							|| oddsFixture.date.equals(Utils.getTommorow(i.date)))) {
				Fixture ef = oddsFixture.withShots((int) i.gameStats.getShotsHome(), (int) i.gameStats.getShotsAway());
				return ef;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param competition
	 * @param year
	 * @param full
	 *            - true for filling full data(all lines)
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException
	 */
	public static void fillMissingShotsData(String competition, int year)
			throws IOException, ParseException, InterruptedException {
		String base = new File("").getAbsolutePath();
		FileInputStream file = new FileInputStream(new File(base + "/data/odds" + year + ".xls"));
		HSSFWorkbook workbook = new HSSFWorkbook(file);
		ArrayList<Fixture> odds = new ArrayList<>();
		odds = selectAll(workbook.getSheet(competition), 0);
		System.out.println("odds size " + odds.size());

		ArrayList<Fixture> filled = new ArrayList<>();
		int filledCount = 0;

		for (Fixture i : odds) {
			if (i.gameStats.getShotsHome() == -1) {
				float avgHome = selectAvgShotsHome(workbook.getSheet(competition), i.date);
				float avgAway = selectAvgShotsAway(workbook.getSheet(competition), i.date);
				float homeShotsFor = selectAvgHomeShotsFor(workbook.getSheet(competition), i.homeTeam, i.date);
				float homeShotsAgainst = selectAvgHomeShotsAgainst(workbook.getSheet(competition), i.homeTeam, i.date);
				float awayShotsFor = selectAvgAwayShotsFor(workbook.getSheet(competition), i.awayTeam, i.date);
				float awayShotsAgainst = selectAvgAwayShotsAgainst(workbook.getSheet(competition), i.awayTeam, i.date);

				float lambda = avgAway == 0 ? 0 : homeShotsFor * awayShotsAgainst / avgAway;
				float mu = avgHome == 0 ? 0 : awayShotsFor * homeShotsAgainst / avgHome;

				lambda -= i.result.goalsHomeTeam;
				mu -= i.result.goalsAwayTeam;

				lambda = lambda < 0 ? 0 : lambda;
				mu = mu < 0 ? 0 : mu;

				Fixture ef = i.withShots(/* Math.round(lambda), Math.round(mu) */i.result.goalsHomeTeam,
						i.result.goalsAwayTeam);
				System.out.println(lambda + " s " + mu);
				System.out.println(ef);
				filled.add(ef);
				filledCount++;

			} else {
				filled.add(i);
			}
		}

		System.out.println(filledCount + " filled missing shots by same number as goals scored");

		if (filledCount != 0) {
			System.out.println(filled.size() + "stored");
			if (filled.size() == odds.size()) {
				storeInExcel(filled, competition, year, "odds");
			}
		}

		workbook.close();

	}

	public static ArrayList<FinalEntry> predictions(HSSFSheet sheet, int year)
			throws IOException, InterruptedException, ParseException {
		float profit = 0.0f;
		int played = 0;
		ArrayList<Fixture> all = selectAll(sheet, 0);
		float th = 0.55f;
		Settings temp = Settings.shots(sheet.getSheetName());

		ArrayList<FinalEntry> pending = new ArrayList<>();

		int maxMatchDay = FixtureUtils.addMatchDay(all);
		for (int i = maxMatchDay >= 14 ? 14 : 5; i <= maxMatchDay; i++) {
			ArrayList<Fixture> current = FixtureUtils.getByMatchday(all, i);
			// ArrayList<Fixture> data = Utils.getBeforeMatchday(all,
			// i);

			ArrayList<FinalEntry> finals = new ArrayList<>();

			finals = FixtureUtils.runWithSettingsList(all, current, temp);
			pending.addAll(finals);

		}

		return pending;
	}

	/**
	 * 
	 * @param all
	 *            - list of final entries
	 * @param initial
	 *            - Settings
	 * @return Settings with best value maximizing based on the statistic
	 *         significance of the record by the Utils.evaluateRecord method
	 */

	public static Settings findValueByEvaluation(ArrayList<FinalEntry> all, Settings initial) {
		float probability = Utils.evaluateRecord(all);
		Settings newSetts = new Settings(initial);
		Settings best = new Settings(initial);
		float bestValue = 0.9f;

		for (int x = 0; x <= 30; x++) {
			float currentValue = 0.7f + x * 0.02f;
			newSetts.value = currentValue;
			ArrayList<FinalEntry> vals = restrict(all, newSetts);
			float currentProbability = Utils.evaluateRecord(vals);

			if (currentProbability > probability) {
				bestValue = currentValue;
				probability = currentProbability;
				best.value = bestValue;
				best.profit = Utils.getProfit(vals);
			}
		}

		return best.withYear(initial.year).withHT(initial.halfTimeOverOne, initial.htCombo).withValue(bestValue);
	}

	/**
	 * Describing maximizaion base on: BOTH - all finals UNDERS - only unders OVERS
	 * - only overs
	 *
	 */
	public enum MaximizingBy {
		BOTH, UNDERS, OVERS
	}
}
