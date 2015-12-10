package xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.spi.CalendarDataProvider;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import main.ExtendedFixture;
import main.FinalEntry;
import main.Result;
import main.SQLiteJDBC;
import settings.Settings;
import utils.Utils;

public class XlSUtils {

	public static final int DATE = 1;
	public static final int HOMETEAM = 2;
	public static final int AWAYTEAM = 3;
	// public static final DateFormat format = new
	// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static void test() {
		try {
			FileInputStream file = new FileInputStream(
					new File("C:\\Users\\Admin\\workspace\\Soccer\\all-euro-data-2014-2015.xls"));

			// Get the workbook instance for XLS file
			HSSFWorkbook workbook = new HSSFWorkbook(file);

			// Get first sheet from the workbook
			HSSFSheet sheet = workbook.getSheetAt(0);

			// extractExcelContentByColumnIndex(sheet, 2);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getColumnIndex(HSSFSheet sheet, String columnName) {
		Iterator<Cell> it = sheet.getRow(0).cellIterator();
		while (it.hasNext()) {
			Cell cell = it.next();
			if (cell.getStringCellValue().equals(columnName))
				return cell.getColumnIndex();
		}
		return -1;
	}

	public static ArrayList<String> extractExcelContentByColumnIndex(HSSFSheet sheet, int columnIndex) {
		ArrayList<String> columndata = null;
		Iterator<Row> rowIterator = sheet.iterator();
		columndata = new ArrayList<>();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if (row.getRowNum() > 0) { // To filter column headings
					if (cell.getColumnIndex() == columnIndex) {// To match
																// column index
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							columndata.add(cell.getStringCellValue());
							break;

						case Cell.CELL_TYPE_NUMERIC:
							columndata.add(cell.getNumericCellValue() + "");
							break;
						}
					}
				}
			}
		}
		System.out.println(columndata);

		return columndata;
	}

	public static Date getDate(HSSFSheet sheet, String home, String away) {
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getCell(HOMETEAM).getStringCellValue().equals(home)
					&& row.getCell(AWAYTEAM).getStringCellValue().equals(away))
				return row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
		}
		return null;
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
				if (row.getCell(getColumnIndex(sheet, "FTHG")) != null
						&& row.getCell(getColumnIndex(sheet, "FTAG")) != null
						&& row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
					int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

					float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
					float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
					float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
					float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

					ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals),
							sheet.getSheetName());
					f.withStatus("FINISHED");
					f.withOdds(overOdds, underOdds, maxOver, maxUnder);

					results.add(f);
				}
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
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (home.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

				float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
				float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
				float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
				float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

				ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals),
						sheet.getSheetName());
				f.withStatus("FINISHED");
				f.withOdds(overOdds, underOdds, maxOver, maxUnder);

				results.add(f);
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
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (away.equals(team) && fdate.before(date) && row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

				float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
				float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
				float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
				float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

				ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals),
						sheet.getSheetName());
				f.withStatus("FINISHED");
				f.withOdds(overOdds, underOdds, maxOver, maxUnder);

				results.add(f);

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

	public static float poisson(ExtendedFixture f, HSSFSheet sheet, Date date) {
		ArrayList<ExtendedFixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeam, 10, date);
		ArrayList<ExtendedFixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeam, 10, date);

		float lambda = Utils.avgFor(f.homeTeam, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeam, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(ExtendedFixture f, HSSFSheet sheet, Date date) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeam, date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeam, date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeam, date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeam, date);

		float lambda = homeAvgFor * awayAvgAgainst / leagueAvgAway;
		float mu = awayAvgFor * homeAvgAgainst / leagueAvgHome;

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
		return total / count;
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
		return total / count;
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
		return total / count;
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
		return total / count;
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
					&& dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return total / count;
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
					&& dateCell.getDateCellValue().before(date)) {
				int homegoal = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				total += homegoal;
				count++;
			}
		}
		return total / count;
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
				if (row.getCell(getColumnIndex(sheet, "FTHG")) != null
						&& row.getCell(getColumnIndex(sheet, "FTAG")) != null) {
					int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

					float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
					float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
					float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
					float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

					ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals),
							sheet.getSheetName());
					f.withStatus("FINISHED");
					f.withOdds(overOdds, underOdds, maxOver, maxUnder);

					results.add(f);
				}
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
			if (row.getRowNum() == 0)
				continue;
			if (row.getCell(getColumnIndex(sheet, "HomeTeam")) == null)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null && row.getCell(getColumnIndex(sheet, "FTAG")) != null
					&& row.getCell(getColumnIndex(sheet, "BbAv>2.5")) != null) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();
				float overOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv>2.5")).getNumericCellValue();
				float underOdds = (float) row.getCell(getColumnIndex(sheet, "BbAv<2.5")).getNumericCellValue();
				float maxOver = (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
				float maxUnder = (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();

				ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(homeGoals, awayGoals),
						sheet.getSheetName()).withStatus("FINISHED").withOdds(overOdds, underOdds, maxOver, maxUnder);

				results.add(f);
			}
		}
		return results;

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

			ExtendedFixture f = new ExtendedFixture(fdate, home, away, new Result(-1, -1), sheet.getSheetName());
			f.withStatus("FINISHED");
			f.withOdds(overOdds, underOdds, maxOver, maxUnder);

			results.add(f);

		}
		return results;
	}

	public static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<ExtendedFixture> all, float minOdds)
			throws IOException {
		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		for (int i = 0; i < all.size(); i++) {
			ExtendedFixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet, f.date);
			weightedPoissons[i] = poissonWeighted(f, sheet, f.date);
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				ExtendedFixture f = all.get(i);
				float finalScore = x * 0.05f * basics[i] + y * 0.05f * poissons[i];

				FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}

			float current = Utils.getSuccessRate(finals);
			if (current > bestWinPercent) {
				bestWinPercent = current;
			}

			Settings set = new Settings(sheet.getSheetName(), bestBasic * 0.05f, 1.0f - bestBasic * 0.05f, 0.0f, 0.55f,
					0.55f, 0.55f, 1, 10, bestWinPercent, bestProfit);
			float currentProfit = Utils.getProfit(sheet, finals, set);
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

				FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}

			float current = Utils.getSuccessRate(finals);
			if (current > bestWinPercent) {
				bestWinPercent = current;
			}

			Settings set = new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 1.0f - bestBasic * 0.05f, 0.55f,
					0.55f, 0.55f, 1, 10, bestWinPercent, bestProfit);
			float currentProfit = Utils.getProfit(sheet, finals, set);
			if (currentProfit > bestProfit) {
				flagw = true;
				bestProfit = currentProfit;
				bestBasic = x;
			}

		}
		// System.out.println("Best profit found by find xy " + bestProfit);
		if (!flagw)
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 1.0f - bestBasic * 0.05f, 0.0f, 0.55f, 0.55f,
					0.55f, 1, 10, bestWinPercent, bestProfit);
		else
			return new Settings(sheet.getSheetName(), bestBasic * 0.05f, 0f, 1.0f - bestBasic * 0.05f, 0.55f, 0.55f,
					0.55f, 1, 10, bestWinPercent, bestProfit);

	}

	public static Settings runForLeagueWithOddsFull(HSSFSheet sheet, ArrayList<ExtendedFixture> all, float minOdds)
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
			poissons[i] = poisson(f, sheet, f.date);
			weightedPoissons[i] = poissonWeighted(f, sheet, f.date);
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			for (int z = 0; z < y; z++) {
				int w = y - z;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 0; i < all.size(); i++) {
					ExtendedFixture f = all.get(i);
					float finalScore = x * 0.05f * basics[i] + z * 0.05f * poissons[i] + w * weightedPoissons[i];

					FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
							new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f);
					if (!fe.prediction.equals(Float.NaN))
						finals.add(fe);
				}

				float current = Utils.getSuccessRate(finals);
				if (current > bestWinPercent) {
					bestWinPercent = current;
				}

				Settings set = new Settings(sheet.getSheetName(), x * 0.05f, z * 0.05f, w * 0.05f, 0.55f, 0.55f, 0.55f,
						1, 10, bestWinPercent, bestProfit);
				float currentProfit = Utils.getProfit(sheet, finals, set);
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
				0.55f, 0.55f, 1, 10, bestWinPercent, bestProfit);

	}

	public static Settings runForLeagueWithRestrictedOdds(HSSFSheet sheet) throws IOException, ParseException {
		long start = System.currentTimeMillis();
		ArrayList<ExtendedFixture> all = selectAll(sheet);
		Settings initial = runForLeagueWithOdds(sheet, all, 1);
		ArrayList<FinalEntry> finals = runWithoutMinOddsSettings(sheet, all, initial);
		float profit = initial.profit;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			Settings newSetts = new Settings(initial);
			newSetts.minOdds = currentMin;
			ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals, currentMin);
			float currentProfit = Utils.getProfit(sheet, Utils.filterFinals(sheet, finals, currentMin));

			if (currentProfit > profit) {
				profit = currentProfit;
				initial.minOdds = currentMin;
				initial.profit = profit;
				initial.successRate = Utils.getSuccessRate(filtered);

			}

		}

		for (int x = 0; x < 50; x++) {
			float currentMax = initial.minOdds + (x + 1) * 0.02f;
			Settings newSetts = new Settings(initial);
			newSetts.maxOdds = currentMax;
			ArrayList<FinalEntry> filtered = Utils.filterMaxFinals(sheet, finals, currentMax);
			float currentProfit = Utils.getProfit(sheet, filtered);

			if (currentProfit > profit) {
				profit = currentProfit;
				initial.maxOdds = currentMax;
				initial.profit = profit;
				initial.successRate = Utils.getSuccessRate(filtered);

			}

		}
		System.out.println((System.currentTimeMillis() - start) / 1000d + "trust  sec ");
		return initial;
	}

	public static Settings findInterval(ArrayList<ExtendedFixture> all, HSSFSheet sheet, int year)
			throws ParseException, IOException {
		Settings initial = SQLiteJDBC.getSettings(sheet.getSheetName(), year);
		ArrayList<FinalEntry> finals = runWithoutMinOddsSettings(sheet, all, initial);
		float profit = initial.profit;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			Settings newSetts = new Settings(initial);
			newSetts.minOdds = currentMin;
			for (int y = x + 1; 1.3f + y * 0.02f < 2.5f; y++) {
				float currentMax = 1.3f + y * 0.02f;
				Settings newSetts1 = new Settings(newSetts);
				newSetts1.maxOdds = currentMax;
				ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals, currentMin);
				ArrayList<FinalEntry> filtered1 = Utils.filterMaxFinals(sheet, filtered, currentMax);
				float currentProfit = Utils.getProfit(sheet, filtered1);

				if (currentProfit > profit) {
					profit = currentProfit;
					initial.minOdds = currentMin;
					initial.maxOdds = currentMax;
					initial.profit = profit;
					initial.successRate = Utils.getSuccessRate(filtered1);
				}
			}

		}

		return initial;
	}

	public static float runWithSettings(HSSFSheet sheet, ArrayList<ExtendedFixture> all, Settings settings)
			throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (ExtendedFixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.date);

			float gain = finalScore > 0.55d ? f.maxOver : f.maxUnder;
			if (gain >= settings.minOdds || gain <= settings.maxOdds)
				finals.add(new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
		}

		return Utils.getProfit(sheet, finals);
	}

	public static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			Settings settings) throws IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (ExtendedFixture f : all) {
			float finalScore = 0.5f;

			finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.date)
					+ settings.weightedPoisson * poissonWeighted(f, sheet, f.date);

			float gain = finalScore > settings.threshold ? f.maxOver : f.maxUnder;
			if (gain >= settings.minOdds && gain <= settings.maxOdds
					&& (finalScore >= settings.upperBound || finalScore <= settings.lowerBound)) {
				FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), settings.threshold,
						settings.lowerBound, settings.upperBound);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}
		}
		return finals;
	}

	public static ArrayList<FinalEntry> runWithoutMinOddsSettings(HSSFSheet sheet, ArrayList<ExtendedFixture> all,
			Settings settings) throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (ExtendedFixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.date);
			finals.add(
					new FinalEntry(f, finalScore, "Basic1", new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
							0.55f, settings.lowerBound, settings.upperBound));
		}

		return finals;
	}

	public static void makePrediction(HSSFSheet odds, HSSFSheet league, ExtendedFixture f, Settings sett)
			throws IOException {
		if (sett == null)
			return;
		float score = sett.basic * basic2(f, league, 0.6f, 0.3f, 0.1f) + sett.poisson * poisson(f, league, f.date)
				+ sett.weightedPoisson * poissonWeighted(f, league, f.date);

		float coeff = score > sett.threshold ? f.maxOver : f.maxUnder;
		if (coeff >= sett.minOdds && coeff <= sett.maxOdds && (score >= sett.upperBound) || score <= sett.lowerBound) {
			String prediction = score > sett.threshold ? "over" : "under";
			System.out.println(league.getSheetName() + " " + f.homeTeam + " : " + f.awayTeam + " " + score + " "
					+ prediction + " " + coeff);
		}

	}

	public static Settings predictionSettings(HSSFSheet sheet, int year) throws IOException {
		ArrayList<ExtendedFixture> data = selectAllAll(sheet);
		Settings temp = runForLeagueWithOdds(sheet, data, 1);
		ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
		temp = findThreshold(sheet, finals, temp);
		temp = trustInterval(sheet, finals, temp);
		temp = findIntervalReal(finals, sheet, year, temp);
		return temp;
	}

	public static float realisticRun(HSSFSheet sheet, int year) throws IOException {
		ArrayList<FinalEntry> totals = new ArrayList<>();
		float profit = 0.0f;
		ArrayList<ExtendedFixture> all = selectAllAll(sheet);
		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 11; i < maxMatchDay; i++) {
			ArrayList<ExtendedFixture> current = Utils.getByMatchday(all, i);
			// Calendar cal = Calendar.getInstance();
			// cal.set(year + 1, 4, 1);
			// if (!current.isEmpty() && current.get(0).dt.after(cal.getTime()))
			// {
			// return profit;
			// }

			ArrayList<ExtendedFixture> data = Utils.getBeforeMatchday(all, i);
			Settings temp = runForLeagueWithOdds(sheet, data, 1);
			// System.out.println("match " + i + temp);
			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			temp = findThreshold(sheet, finals, temp);
			temp = trustInterval(sheet, finals, temp);

			temp = findIntervalReal(finals, sheet, year, temp);
			finals = runWithSettingsList(sheet, current, temp);
			// System.out.println(finals);
			float trprofit = Utils.getProfit(sheet, finals, temp);
			// System.out.println(i + " " + trprofit);
			// System.out.println("--------------------------");
			profit += trprofit;
		}
		return profit;
	}

	private static Settings trustInterval(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial)
			throws IOException {
//		System.out.println("===========================");
//		System.out.println(
//				"lower: " + initial.lowerBound + " upper: " + initial.upperBound + " profit: " + initial.profit);
		Settings trset = new Settings(initial);

		finals.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {

				return o2.prediction.compareTo(o1.prediction);
			}
		});

		Settings set = new Settings(initial);
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
				float currentProfit = Utils.getProfit(sheet, sofar, set);
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
				float currentProfit = Utils.getProfit(sheet, sofarLower, set);
				if (currentProfit > bestProfitLower) {
					bestProfitLower = currentProfit;
					bestLower = current;
				}
				current -= 0.025d;
			}
		}

		trset.upperBound = bestUpper;
		trset.lowerBound = bestLower;
		float bestFinalProfit = Utils.getProfit(sheet, Utils.filterTrust(finals, trset), trset);
		if (bestFinalProfit >= trset.profit) {
			trset.profit = bestFinalProfit;
		} else {
			trset = initial;
		}

//		System.out.println("lower: " + trset.lowerBound + " upper: " + trset.upperBound + " profit: " + trset.profit);
		return trset;
	}

	public static Settings findThreshold(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial) {
		// System.out.println("thold: " + initial.threshold + " profit: " +
		// initial.profit);
		Settings trset = new Settings(initial);

		float bestProfit = initial.profit;
		float bestThreshold = initial.threshold;

		for (int i = 0; i <= 40; i++) {
			float current = 0.30f + i * 0.01f;
			trset.threshold = current;
			trset.lowerBound = current;
			trset.upperBound = current;
			float currentProfit = Utils.getProfit(sheet, finals, trset);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestThreshold = current;
			}
		}

		trset.profit = bestProfit;
		trset.threshold = bestThreshold;
		trset.lowerBound = bestThreshold;
		trset.upperBound = bestThreshold;
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

	private static Settings findIntervalReal(ArrayList<FinalEntry> finals, HSSFSheet sheet, int year,
			Settings initial) {
		float profit = initial.profit;
		Settings newSetts = new Settings(initial);
		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals, currentMin);
			float currentProfit = Utils.getProfit(sheet, filtered, newSetts);

			if (currentProfit > profit) {
				bestminx = x;
				profit = currentProfit;
				newSetts.minOdds = currentMin;
				newSetts.profit = profit;
				newSetts.successRate = Utils.getSuccessRate(filtered, newSetts.threshold);
			}
		}

		for (int x = bestminx; 1.3f + x * 0.02 < 2.5f; x++) {
			float currentMax = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals, newSetts.minOdds);
			ArrayList<FinalEntry> filtered1 = Utils.filterMaxFinals(sheet, filtered, currentMax);
			float currentProfit = Utils.getProfit(sheet, filtered1, newSetts);

			if (currentProfit > profit) {
				profit = currentProfit;
				newSetts.maxOdds = currentMax;
				newSetts.profit = profit;
				newSetts.successRate = Utils.getSuccessRate(filtered1, newSetts.threshold);
			}
		}

		// System.out.println("after finding interval" + initial);
		return newSetts;
	}

}
