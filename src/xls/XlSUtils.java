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

import main.FinalEntry;
import main.Fixture;
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

	public static float getOverOdds(HSSFSheet sheet, String date, String home, String away) {

		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getCell(HOMETEAM).getStringCellValue().equals(home)
					&& row.getCell(AWAYTEAM).getStringCellValue().equals(away)
					&& row.getCell(getColumnIndex(sheet, "BbMx>2.5")) != null)
				return (float) row.getCell(getColumnIndex(sheet, "BbMx>2.5")).getNumericCellValue();
		}
		return -1.0f;
	}

	public static float getUnderOdds(HSSFSheet sheet, Object object, String home, String away) {

		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getCell(HOMETEAM).getStringCellValue().equals(home)
					&& row.getCell(AWAYTEAM).getStringCellValue().equals(away)
					&& row.getCell(getColumnIndex(sheet, "BbMx<2.5")) != null)
				return (float) row.getCell(getColumnIndex(sheet, "BbMx<2.5")).getNumericCellValue();
		}
		return -1.0f;
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

	public static ArrayList<Fixture> selectLastAll(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<Fixture> results = new ArrayList<>();
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
						&& row.getCell(getColumnIndex(sheet, "FTAG")) != null) {
					int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
					int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

					DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

					results.add(new Fixture(format.format(fdate), "FINISHED", -1, home, away,
							new Result(homeGoals, awayGoals), "", "", sheet.getSheetName()));
				}
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<Fixture> selectLastHome(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (home.equals(team) && fdate.before(date)) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

				results.add(new Fixture(format.format(fdate), "FINISHED", -1, home, away,
						new Result(homeGoals, awayGoals), "", "", sheet.getSheetName()));
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static ArrayList<Fixture> selectLastAway(HSSFSheet sheet, String team, int count, Date date) {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			if (away.equals(team) && fdate.before(date)) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

				results.add(new Fixture(format.format(fdate), "FINISHED", -1, home, away,
						new Result(homeGoals, awayGoals), "", "", sheet.getSheetName()));
			}
		}

		return Utils.getLastFixtures(results, count);
	}

	public static float basic2(Fixture f, HSSFSheet sheet, float d, float e, float z) {
		Date date = new Date();
		date = f.dt;

		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeamName, 10, date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeamName, 10, date);

		ArrayList<Fixture> lastHomeHomeTeam = XlSUtils.selectLastHome(sheet, f.homeTeamName, 5, date);
		ArrayList<Fixture> lastAwayAwayTeam = XlSUtils.selectLastAway(sheet, f.awayTeamName, 5, date);
		float allGamesAVG = (Utils.countOverGamesPercent(lastHomeTeam) + Utils.countOverGamesPercent(lastAwayTeam)) / 2;
		float homeAwayAVG = (Utils.countOverGamesPercent(lastHomeHomeTeam)
				+ Utils.countOverGamesPercent(lastAwayAwayTeam)) / 2;
		float BTSAVG = (Utils.countBTSPercent(lastHomeTeam) + Utils.countBTSPercent(lastAwayTeam)) / 2;

		return d * allGamesAVG + e * homeAwayAVG + z * BTSAVG;
	}

	public static float poisson(Fixture f, HSSFSheet sheet, Date date) {
		ArrayList<Fixture> lastHomeTeam = XlSUtils.selectLastAll(sheet, f.homeTeamName, 10, date);
		ArrayList<Fixture> lastAwayTeam = XlSUtils.selectLastAll(sheet, f.awayTeamName, 10, date);

		float lambda = Utils.avgFor(f.homeTeamName, lastHomeTeam);
		float mu = Utils.avgFor(f.awayTeamName, lastAwayTeam);
		return Utils.poissonOver(lambda, mu);
	}

	public static float poissonWeighted(Fixture f, HSSFSheet sheet, Date date) {

		float leagueAvgHome = XlSUtils.selectAvgLeagueHome(sheet, date);
		float leagueAvgAway = XlSUtils.selectAvgLeagueAway(sheet, date);
		float homeAvgFor = XlSUtils.selectAvgHomeTeamFor(sheet, f.homeTeamName, date);
		float homeAvgAgainst = XlSUtils.selectAvgHomeTeamAgainst(sheet, f.homeTeamName, date);
		float awayAvgFor = XlSUtils.selectAvgAwayTeamFor(sheet, f.awayTeamName, date);
		float awayAvgAgainst = XlSUtils.selectAvgAwayTeamAgainst(sheet, f.awayTeamName, date);

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
	public static ArrayList<Fixture> selectAll(HSSFSheet sheet) {
		ArrayList<Fixture> results = new ArrayList<>();
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

					results.add(new Fixture(fdate, "FINISHED", -1, home, away, new Result(homeGoals, awayGoals), "", "",
							sheet.getSheetName()));
				}
			}
		}
		return results;

	}

	// selects all fixtures after the 10 matchday
	public static ArrayList<Fixture> selectAllAll(HSSFSheet sheet) {
		ArrayList<Fixture> results = new ArrayList<>();
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
			if (row.getCell(getColumnIndex(sheet, "FTHG")) != null
					&& row.getCell(getColumnIndex(sheet, "FTAG")) != null) {
				int homeGoals = (int) row.getCell(getColumnIndex(sheet, "FTHG")).getNumericCellValue();
				int awayGoals = (int) row.getCell(getColumnIndex(sheet, "FTAG")).getNumericCellValue();

				results.add(new Fixture(fdate, "FINISHED", -1, home, away, new Result(homeGoals, awayGoals), "", "",
						sheet.getSheetName()));
			}
		}
		return results;

	}

	public static ArrayList<Fixture> selectForPrediction(HSSFSheet sheet) {
		ArrayList<Fixture> results = new ArrayList<>();
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
				continue;
			String home = row.getCell(getColumnIndex(sheet, "HomeTeam")).getStringCellValue();
			String away = row.getCell(getColumnIndex(sheet, "AwayTeam")).getStringCellValue();
			Date fdate = row.getCell(getColumnIndex(sheet, "Date")).getDateCellValue();
			String div = row.getCell(getColumnIndex(sheet, "Div")).getStringCellValue();

			results.add(new Fixture(fdate, "FINISHED", -1, home, away, new Result(-1, -1), "", "", div));

		}
		return results;
	}

	public static Settings runForLeagueWithOdds(HSSFSheet sheet, ArrayList<Fixture> all, float minOdds)
			throws IOException {
		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		HashMap<Fixture, Float> underOdds = new HashMap<>();
		HashMap<Fixture, Float> overOdds = new HashMap<>();
		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet, f.dt);
			weightedPoissons[i] = poissonWeighted(f, sheet, f.dt);

			underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			ArrayList<FinalEntry> finals = new ArrayList<>();
			for (int i = 0; i < all.size(); i++) {
				Fixture f = all.get(i);
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
			float currentProfit = Utils.getProfit(sheet, finals, underOdds, overOdds, set);
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
			float currentProfit = Utils.getProfit(sheet, finals, underOdds, overOdds, set);
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

	public static Settings runForLeagueWithOddsFull(HSSFSheet sheet, ArrayList<Fixture> all, float minOdds)
			throws IOException {
		float bestWinPercent = 0;
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestBasic = 0;
		float bestPoisson = 0;
		float bestWeighed = 0;

		float[] basics = new float[all.size()];
		float[] poissons = new float[all.size()];
		float[] weightedPoissons = new float[all.size()];
		HashMap<Fixture, Float> underOdds = new HashMap<>();
		HashMap<Fixture, Float> overOdds = new HashMap<>();
		for (int i = 0; i < all.size(); i++) {
			Fixture f = all.get(i);

			basics[i] = basic2(f, sheet, 0.6f, 0.3f, 0.1f);
			poissons[i] = poisson(f, sheet, f.dt);
			weightedPoissons[i] = poissonWeighted(f, sheet, f.dt);

			underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
		}

		for (int x = 0; x <= 20; x++) {
			int y = 20 - x;
			for (int z = 0; z < y; z++) {
				int w = y - z;
				ArrayList<FinalEntry> finals = new ArrayList<>();
				for (int i = 0; i < all.size(); i++) {
					Fixture f = all.get(i);
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
				float currentProfit = Utils.getProfit(sheet, finals, underOdds, overOdds, set);
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
		ArrayList<Fixture> all = selectAll(sheet);
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

	public static Settings findInterval(ArrayList<Fixture> all, HSSFSheet sheet, int year)
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

	public static float runWithSettings(HSSFSheet sheet, ArrayList<Fixture> all, Settings settings)
			throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.dt);

			float gain = finalScore > 0.55d ? XlSUtils.getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName)
					: XlSUtils.getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName);
			if (gain >= settings.minOdds || gain <= settings.maxOdds)
				finals.add(new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), 0.55f, 0.55f, 0.55f));
		}

		return Utils.getProfit(sheet, finals);
	}

	public static ArrayList<FinalEntry> runWithSettingsList(HSSFSheet sheet, ArrayList<Fixture> all, Settings settings)
			throws IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {
			float finalScore = 0.5f;

			finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.dt)
					+ settings.weightedPoisson * poissonWeighted(f, sheet, f.dt);

			float gain = finalScore > settings.threshold
					? XlSUtils.getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName)
					: XlSUtils.getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName);
			if (gain >= settings.minOdds && gain <= settings.maxOdds && (finalScore >= settings.upperBound)
					|| finalScore <= settings.lowerBound) {
				FinalEntry fe = new FinalEntry(f, finalScore, "Basic1",
						new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam), settings.threshold,
						settings.lowerBound, settings.upperBound);
				if (!fe.prediction.equals(Float.NaN))
					finals.add(fe);
			}
		}
		return finals;
	}

	public static ArrayList<FinalEntry> runWithoutMinOddsSettings(HSSFSheet sheet, ArrayList<Fixture> all,
			Settings settings) throws ParseException, IOException {
		ArrayList<FinalEntry> finals = new ArrayList<>();
		for (Fixture f : all) {
			float finalScore = settings.basic * basic2(f, sheet, 0.6f, 0.3f, 0.1f)
					+ settings.poisson * poisson(f, sheet, f.dt);
			finals.add(
					new FinalEntry(f, finalScore, "Basic1", new Result(f.result.goalsHomeTeam, f.result.goalsAwayTeam),
							0.55f, settings.lowerBound, settings.upperBound));
		}

		return finals;
	}

	public static void makePrediction(HSSFSheet odds, HSSFSheet league, Fixture f, Settings sett)
			throws ParseException, IOException {
		if (sett == null)
			return;
		float score = sett.basic * basic2(f, league, 0.6f, 0.3f, 0.1f) + sett.poisson * poisson(f, league, f.dt);
		float coeff = score > 0.55d ? XlSUtils.getOverOdds(odds, null, f.homeTeamName, f.awayTeamName)
				: XlSUtils.getUnderOdds(odds, null, f.homeTeamName, f.awayTeamName);
		if (coeff < sett.minOdds || coeff > sett.maxOdds)
			return;
		String prediction = score > 0.55d ? "over" : "under";
		System.out.println(
				league.getSheetName() + " " + f.homeTeamName + " : " + f.awayTeamName + " " + prediction + " " + coeff);

	}

	public static float realisticRun(HSSFSheet sheet, int year) throws IOException {
		ArrayList<FinalEntry> totals = new ArrayList<>();
		float profit = 0.0f;
		ArrayList<Fixture> all = selectAllAll(sheet);
		int maxMatchDay = addMatchDay(sheet, all);
		for (int i = 11; i < maxMatchDay; i++) {
			ArrayList<Fixture> current = Utils.getByMatchday(all, i);
			Calendar cal = Calendar.getInstance();
			cal.set(year + 1, 4, 1);
			if (!current.isEmpty() && current.get(0).dt.after(cal.getTime())) {
				return profit;
			}

			ArrayList<Fixture> data = Utils.getBeforeMatchday(all, i);
			Settings temp = runForLeagueWithOdds(sheet, data, 1);
			// System.out.println("match " + i + temp);
			ArrayList<FinalEntry> finals = runWithSettingsList(sheet, data, temp);
			// System.out.println("first run should be best xy settings " +
			// Utils.getProfit(sheet, finals));
			// temp.minOdds = 1.7f;
			// temp.maxOdds = 2.1f;
			// temp = findIntervalReal(finals, sheet, year, temp);
			// finals = runWithSettingsList(sheet, data, temp);
			temp = findThreshold(sheet, finals, temp);
			temp = trustInterval(sheet, finals, temp);

			// System.out.println("threshold " + temp);
			// finals = runWithSettingsList(sheet, data, temp);
			// System.out.println("first run should be best xy settings " +
			// Utils.getProf);
			// finals = runWithSettingsList(sheet, data, temp);
			temp = findIntervalReal(finals, sheet, year, temp);
			// temp.minOdds = 1.7f;
			// temp.maxOdds = 2.1f;
			// System.out.println(temp);
			finals = runWithSettingsList(sheet, current, temp);
			HashMap<Fixture, Float> underOdds = new HashMap<>();
			HashMap<Fixture, Float> overOdds = new HashMap<>();
			for (int j = 0; j < finals.size(); j++) {
				Fixture f = finals.get(j).fixture;
				underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
				overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			}
			float trprofit = Utils.getProfit(sheet, finals, underOdds, overOdds, temp);
			// System.out.println(i + " " + trprofit);
			profit += trprofit;
			// float currProfit = Utils.getProfit(sheet, finals);
			// float successRate = Utils.getSuccessRate(finals);
			// System.out.println(
			// "Profit for matchday " + i + " is: " + currProfit + " rate: " +
			// String.format("%.2f", successRate));
			// profit += currProfit;
		}
		return profit;
	}

	private static Settings trustInterval(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial)
			throws IOException {
		long start = System.currentTimeMillis();
		Settings trset = new Settings(initial);
		HashMap<Fixture, Float> underOdds = new HashMap<>();
		HashMap<Fixture, Float> overOdds = new HashMap<>();
		for (int i = 0; i < finals.size(); i++) {
			Fixture f = finals.get(i).fixture;
			underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
		}

		finals.sort(new Comparator<FinalEntry>() {

			@Override
			public int compare(FinalEntry o1, FinalEntry o2) {

				return o2.prediction.compareTo(o1.prediction);
			}
		});

		Settings set = new Settings(initial);
		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestUpper = 0.0f;

		ArrayList<FinalEntry> sofar = new ArrayList<>();
		float current = initial.threshold + 0.3f;
		set.upperBound = current;

		for (FinalEntry fe : finals) {
			if (current < initial.threshold)
				break;
			if (fe.prediction >= current) {
				sofar.add(fe);
			} else {
				float currentProfit = Utils.getProfit(sheet, sofar, underOdds, overOdds, set);
				if (currentProfit > bestProfit) {
					bestProfit = currentProfit;
					bestUpper = current;
				}
				current -= 0.025d;
			}
		}

		float bestLower = 0.0f;
		float bestProfitLower = Float.NEGATIVE_INFINITY;

		ArrayList<FinalEntry> sofarLower = new ArrayList<>();
		current = initial.threshold - 0.3f;
		set.lowerBound = current;

		for (int i = finals.size() - 1; i >= 0; i--) {
			if (current > initial.threshold)
				break;
			if (finals.get(i).prediction <= current) {
				sofarLower.add(finals.get(i));
			} else {
				float currentProfit = Utils.getProfit(sheet, sofarLower, underOdds, overOdds, set);
				if (currentProfit > bestProfitLower) {
					bestProfitLower = currentProfit;
					bestLower = current;
				}
				current += 0.025d;
			}
		}

		trset.upperBound = bestUpper;
		trset.lowerBound = bestLower;
		float bestFinalProfit = Utils.getProfit(sheet, runWithSettingsList(sheet, Utils.onlyFixtures(finals), trset),
				underOdds, overOdds, trset);
		if (bestFinalProfit >= trset.profit) {
			// System.out.println("Trust profit is better with " +
			// (bestFinalProfit - trset.profit));
			trset.profit = bestFinalProfit;
		} else {
			// System.out.println("smth is wrong");
			// System.out.println("Trust profit is better with " +
			// (bestFinalProfit - trset.profit));
			trset.lowerBound = initial.lowerBound;
			trset.upperBound = initial.upperBound;
		}
		// System.out.println(trset);
		// System.out.println((System.currentTimeMillis() - start) / 1000d + "
		// trust sec");
		return trset;
	}

	public static Settings findThreshold(HSSFSheet sheet, ArrayList<FinalEntry> finals, Settings initial) {
		Settings trset = new Settings(initial);
		HashMap<Fixture, Float> underOdds = new HashMap<>();
		HashMap<Fixture, Float> overOdds = new HashMap<>();
		for (int i = 0; i < finals.size(); i++) {
			Fixture f = finals.get(i).fixture;
			underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
		}

		float bestProfit = Float.NEGATIVE_INFINITY;
		float bestThreshold = 0.55f;

		for (int i = 0; i <= 30; i++) {
			float current = 0.35f + i * 0.01f;
			float currentProfit = Utils.getProfit(sheet, finals, underOdds, overOdds, initial);
			if (currentProfit > bestProfit) {
				bestProfit = currentProfit;
				bestThreshold = current;
			}
		}
		trset.profit = bestProfit;
		trset.threshold = bestThreshold;
		// System.out.println(trset);
		return trset;

	}

	private static int addMatchDay(HSSFSheet sheet, ArrayList<Fixture> all) {
		int max = -1;
		for (Fixture f : all) {
			if (f.matchday == -1) {
				f.matchday = selectLastAll(sheet, f.homeTeamName, 50, f.dt).size() + 1;
				if (f.matchday > max)
					max = f.matchday;
			}
		}
		return max;
	}

	private static Settings findIntervalReal(ArrayList<FinalEntry> finals, HSSFSheet sheet, int year,
			Settings initial) {

		HashMap<Fixture, Float> underOdds = new HashMap<>();
		HashMap<Fixture, Float> overOdds = new HashMap<>();
		for (int i = 0; i < finals.size(); i++) {
			Fixture f = finals.get(i).fixture;
			underOdds.put(f, getUnderOdds(sheet, null, f.homeTeamName, f.awayTeamName));
			overOdds.put(f, getOverOdds(sheet, null, f.homeTeamName, f.awayTeamName));
		}
		float profit = initial.profit;
		Settings newSetts = new Settings(initial);
		int bestminx = 0;
		for (int x = 0; x < 50; x++) {
			float currentMin = 1.3f + x * 0.02f;
			ArrayList<FinalEntry> filtered = Utils.filterFinals(sheet, finals, currentMin);
			float currentProfit = Utils.getProfit(sheet, filtered, underOdds, overOdds, newSetts);

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
			float currentProfit = Utils.getProfit(sheet, filtered1, underOdds, overOdds, newSetts);

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
