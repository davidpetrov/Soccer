package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import entries.AllEntry;
import entries.FinalEntry;
import entries.HTEntry;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;
import scraper.Scraper;
import settings.Settings;
import utils.FixtureListCombiner;
import utils.Pair;
import xls.XlSUtils;

public class SQLiteJDBC {

	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static ArrayList<Fixture> selectLastAll(String team, int count, int season, int matchday,
			String competition) {
		ArrayList<Fixture> results = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from results" + season + " where matchday < " + matchday
					+ " and competition='" + competition + "' and ((hometeamname = '" + team + "') or (awayteamname = '"
					+ team + "')) order by matchday" + " desc limit " + count + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homegoals");
				int awayGoals = rs.getInt("awaygoals");
				String competit = rs.getString("competition");
				int matchd = rs.getInt("matchday");
				Fixture ef = new Fixture(format.parse(date), competit, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withMatchday(matchd);
				results.add(ef);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		// System.out.println("Operation done successfully");

		return results;
	}

	public static ArrayList<Fixture> selectLastHome(String team, int count, int season, int matchday,
			String competition) {
		ArrayList<Fixture> results = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from results" + season + " where matchday < " + matchday
					+ " and competition='" + competition + "' and (hometeamname = '" + team + "')  order by matchday"
					+ " desc limit " + count + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homegoals");
				int awayGoals = rs.getInt("awaygoals");
				String competit = rs.getString("competition");
				int matchd = rs.getInt("matchday");
				Fixture ef = new Fixture(format.parse(date), competit, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withMatchday(matchd);
				results.add(ef);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		// System.out.println("Operation done successfully");

		return results;
	}

	public static boolean checkExistense(String hometeam, String awayteam, String date, int season) {
		boolean flag = false;

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet rs = stmt
					.executeQuery("select * from results" + season + " where hometeamname = " + addQuotes(hometeam)
							+ " and awayteamname = " + addQuotes(awayteam) + " and date = " + addQuotes(date));
			flag = rs.next();

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return flag;
	}

	public static ArrayList<String> getLeagues(int season) {
		ArrayList<String> leagues = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("select distinct competition from results" + season);
			while (rs.next()) {
				leagues.add(rs.getString("competition"));
			}

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return leagues;
	}

	public static ArrayList<Fixture> selectLastAway(String team, int count, int season, int matchday,
			String competition) {
		ArrayList<Fixture> results = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from results" + season + " where matchday < " + matchday
					+ " and competition='" + competition + "' and  (awayteamname = '" + team + "') order by matchday"
					+ " desc limit " + count + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homegoals");
				int awayGoals = rs.getInt("awaygoals");
				String competit = rs.getString("competition");
				int matchd = rs.getInt("matchday");
				Fixture ef = new Fixture(format.parse(date), competit, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withMatchday(matchd).withStatus("FINISHED");
				results.add(ef);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		// System.out.println("Operation done successfully");

		return results;
	}

	public static float selectAvgLeagueHome(String competition, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(homegoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday);
			average = rs.getFloat("avg(homegoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	public static float selectAvgLeagueAway(String competition, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(awaygoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday);
			average = rs.getFloat("avg(awaygoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	public static float selectAvgHomeTeamFor(String competition, String team, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(homegoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday + " and hometeamname=" + addQuotes(team));
			average = rs.getFloat("avg(homegoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	public static float selectAvgHomeTeamAgainst(String competition, String team, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(awaygoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday + " and hometeamname=" + addQuotes(team));
			average = rs.getFloat("avg(awaygoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	public static float selectAvgAwayTeamFor(String competition, String team, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(awaygoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday + " and awayteamname=" + addQuotes(team));
			average = rs.getFloat("avg(awaygoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	public static float selectAvgAwayTeamAgainst(String competition, String team, int season, int matchday) {
		float average = -1.0f;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select avg(homegoals) from results" + season + " where competition="
					+ addQuotes(competition) + " and matchday<" + matchday + " and awayteamname=" + addQuotes(team));
			average = rs.getFloat("avg(homegoals)");

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return average;
	}

	// insert Fixture entry into DB
	public static void storeSettings(Settings s, int year, int period) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();

			String sql = "INSERT INTO SETTINGS"
					+ " (LEAGUE,PERIOD,SEASON,BASIC,POISSON,WPOISSON,HTCOMBO,HTOVERONE,THRESHOLD,LOWER,UPPER,MINUNDER,MAXUNDER,MINOVER,MAXOVER,VALUE,SUCCESSRATE,PROFIT)"
					+ "VALUES (" + addQuotes(s.league) + "," + period + "," + year + "," + s.basic + "," + s.poisson
					+ "," + s.weightedPoisson + "," + s.htCombo + "," + s.halfTimeOverOne + "," + s.threshold + ","
					+ s.upperBound + "," + s.lowerBound + "," + s.minUnder + "," + s.maxUnder + "," + s.minOver + ","
					+ s.maxOver + "," + s.value + "," + s.successRate + "," + s.profit + " );";
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e) {
				System.out.println("tuka");

			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	// insert score
	public static void insertBasic(Fixture f, float score, int year, String tableName) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "INSERT INTO " + tableName + " (DATE,HOMETEAMNAME,AWAYTEAMNAME,YEAR,COMPETITION,SCORE)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + year + "," + addQuotes(f.competition) + "," + score + " );";
			try {
				if (!Float.isNaN(score))
					stmt.executeUpdate(sql);
			} catch (SQLException e) {
				System.out.println("tuka");
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	// refavtor after min max odds change
	public static Settings getSettings(String league, int year, int period) {
		Settings sett = null;
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from settings where league=" + addQuotes(league) + " and SEASON="
					+ year + " and PERIOD=" + period + ";");
			while (rs.next()) {
				float basic = rs.getFloat("basic");
				float poisson = rs.getFloat("poisson");
				float wpoisson = rs.getFloat("wpoisson");
				float htCombo = rs.getFloat("htcombo");
				float htOverOne = rs.getFloat("htoverone");
				float threshold = rs.getFloat("threshold");
				float lower = rs.getFloat("lower");
				float upper = rs.getFloat("upper");
				float minUnder = rs.getFloat("minunder");
				float maxUnder = rs.getFloat("maxunder");
				float minOver = rs.getFloat("minover");
				float maxOver = rs.getFloat("maxover");
				float value = rs.getFloat("value");
				float success = rs.getFloat("successrate");
				float profit = rs.getFloat("profit");
				sett = new Settings(league, basic, poisson, wpoisson, threshold, upper, lower, success, profit)
						.withYear(year).withValue(value).withMinMax(minUnder, maxUnder, minOver, maxOver)
						.withHT(htOverOne, htCombo);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return sett;
	}

	public static String addQuotes(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == '\'')
				sb.append('\\');
			else
				sb.append(c);
		}
		String escaped = sb.toString();
		return "'" + escaped + "'";
	}

	public static synchronized HashMap<Fixture, Float> selectScores(ArrayList<Fixture> all, String table, int year,
			String competition) throws InterruptedException {

		HashMap<Fixture, Float> result = new HashMap<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " + table + " where year=" + year + " AND competition="
					+ addQuotes(competition) + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				Float score = rs.getFloat("score");
				Fixture ef = new Fixture(format.parse(date), competition, homeTeamName, awayTeamName,
						new Result(-1, -1));
				result.put(ef, score);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	public static void insertBasic(HSSFSheet sheet, ArrayList<Fixture> all, int year, String tableName) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (Fixture f : all) {
				float score = Float.NaN;
				if (tableName.equals("BASICS")) {
					score = XlSUtils.basic2(f, sheet, 0.6f, 0.3f, 0.1f);
				} else if (tableName.equals("POISSON")) {
					score = XlSUtils.poisson(f, sheet);
				} else if (tableName.equals("WEIGHTED")) {
					score = XlSUtils.poissonWeighted(f, sheet);
				} else if (tableName.equals("HALFTIME1")) {
					score = XlSUtils.halfTimeOnly(f, sheet, 1);
				} else if (tableName.equals("HALFTIME2")) {
					score = XlSUtils.halfTimeOnly(f, sheet, 2);
				} else if (tableName.equals("SHOTS")) {
					score = XlSUtils.shots(f, sheet);
				}

				String sql = "INSERT INTO " + tableName + " (DATE,HOMETEAMNAME,AWAYTEAMNAME,YEAR,COMPETITION,SCORE)"
						+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
						+ addQuotes(f.awayTeam) + "," + year + "," + addQuotes(f.competition) + "," + score + " );";
				try {
					if (!Float.isNaN(score))
						stmt.executeUpdate(sql);
				} catch (SQLException e) {
					System.out.println("tuka");
				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}

	}

	public static synchronized void storeFinals(ArrayList<FinalEntry> finals, int year, String competition,
			String description) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (FinalEntry f : finals) {

				String sql = "INSERT INTO FINALS "
						+ "(DESCRIPTION,YEAR,DATE,COMPETITION,MATCHDAY,HOMETEAMNAME,AWAYTEAMNAME,HOMEGOALS,AWAYGOALS,OVER,UNDER,SCORE,THOLD,LOWER,UPPER,VALUE)"
						+ "VALUES (" + addQuotes(description) + "," + year + ","
						+ addQuotes(format.format(f.fixture.date)) + "," + addQuotes(competition) + ","
						+ f.fixture.matchday + "," + addQuotes(f.fixture.homeTeam) + "," + addQuotes(f.fixture.awayTeam)
						+ "," + f.fixture.result.goalsHomeTeam + "," + f.fixture.result.goalsAwayTeam + ","
						+ f.fixture.getMaxClosingOverOdds() + "," + f.fixture.getMaxClosingUnderOdds() + ","
						+ (float) Math.round(f.prediction * 100000f) / 100000f + "," + f.threshold + "," + f.lower + ","
						+ f.upper + "," + f.value + " );";
				try {
					if (!Float.isNaN(f.prediction))
						stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("tuka");
				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	public static synchronized void storePlayerFixtures(ArrayList<PlayerFixture> finals, int year, String competition) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (PlayerFixture f : finals) {
				String sql = "INSERT INTO PLAYERFIXTURES "
						+ "(DATE,HOMETEAMNAME,AWAYTEAMNAME,HOMEGOALS,AWAYGOALS,YEAR,COMPETITION,TEAM,NAME,MINUTESPLAYED,LINEUP,SUBSTITUTE,GOALS,ASSISTS)"
						+ "VALUES (" + addQuotes(format.format(f.fixture.date)) + "," + addQuotes(f.fixture.homeTeam)
						+ "," + addQuotes(f.fixture.awayTeam) + "," + f.fixture.result.goalsHomeTeam + ","
						+ f.fixture.result.goalsAwayTeam + "," + year + "," + addQuotes(competition) + ","
						+ addQuotes(f.team) + "," + addQuotes(f.name) + "," + f.minutesPlayed + "," + (f.lineup ? 1 : 0)
						+ "," + (f.substitute ? 1 : 0) + "," + f.goals + "," + f.assists + " );";
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();

				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	public static synchronized ArrayList<PlayerFixture> selectPlayerFixtures(String competition, int year)
			throws InterruptedException {

		ArrayList<PlayerFixture> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from playerfixtures" + " where year=" + year
					+ " AND competition=" + addQuotes(competition) + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homegoals");
				int awayGoals = rs.getInt("awaygoals");
				String team = rs.getString("team");
				String name = rs.getString("name");
				int minutesPlayed = rs.getInt("minutesPlayed");
				int lineup = rs.getInt("lineup");
				int substitute = rs.getInt("substitute");
				int goals = rs.getInt("goals");
				int assists = rs.getInt("assists");

				PlayerFixture pf = new PlayerFixture(
						new Fixture(format.parse(date), competition, homeTeamName, awayTeamName,
								new Result(homeGoals, awayGoals)),
						team, name, minutesPlayed, lineup == 1, substitute == 1, goals, assists);

				result.add(pf);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	public static synchronized ArrayList<FinalEntry> selectFinals(String competition, int year, String description)
			throws InterruptedException {

		ArrayList<FinalEntry> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from finals" + " where year=" + year + " AND competition="
					+ addQuotes(competition) + " AND description=" + addQuotes(description) + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				int matchday = rs.getInt("matchday");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homeGoals");
				int awayGoals = rs.getInt("awayGoals");
				float over = rs.getFloat("over");
				float under = rs.getFloat("under");
				Float score = rs.getFloat("score");
				float thold = rs.getFloat("thold");
				float lower = rs.getFloat("lower");
				float upper = rs.getFloat("upper");
				float value = rs.getFloat("value");

				Fixture ef = new Fixture(format.parse(date), competition, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withMatchday(matchday).withYear(year);
				OverUnderOdds ou = new OverUnderOdds("Max", format.parse(date), 2.5f, over, under);
				ef.overUnderOdds.add(ou);
				FinalEntry f = new FinalEntry(ef, score, new Result(homeGoals, awayGoals), thold, lower, upper);
				f.value = value;

				result.add(f);
			}
			rs.close();

			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	public static synchronized ArrayList<HTEntry> selectHTData(String competition, int year, String description)
			throws InterruptedException {

		ArrayList<HTEntry> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from halftimedata" + " where year=" + year + " AND competition="
					+ addQuotes(competition) + " AND description=" + addQuotes(description) + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				int matchday = rs.getInt("matchday");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homeGoals");
				int awayGoals = rs.getInt("awayGoals");
				float over = rs.getFloat("over");
				float under = rs.getFloat("under");
				Float score = rs.getFloat("score");
				float thold = rs.getFloat("thold");
				float lower = rs.getFloat("lower");
				float upper = rs.getFloat("upper");
				float value = rs.getFloat("value");
				float zero = rs.getFloat("zero");
				float one = rs.getFloat("one");
				float two = rs.getFloat("two");
				float more = rs.getFloat("more");

				Fixture ef = new Fixture(format.parse(date), competition, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withMatchday(matchday).withYear(year);
				OverUnderOdds ou = new OverUnderOdds("Max", format.parse(date), 2.5f, over, under);
				ef.overUnderOdds.add(ou);
				FinalEntry f = new FinalEntry(ef, score, new Result(homeGoals, awayGoals), thold, lower, upper);
				f.value = value;
				HTEntry hte = new HTEntry(f, zero, one, two, more);

				result.add(hte);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	public static synchronized void storeHTData(ArrayList<HTEntry> halftimeData, int year, String competition,
			String description) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (HTEntry f : halftimeData) {

				String sql = "INSERT INTO HALFTIMEDATA "
						+ "(DESCRIPTION,YEAR,DATE,COMPETITION,MATCHDAY,HOMETEAMNAME,AWAYTEAMNAME,HOMEGOALS,AWAYGOALS,OVER,UNDER,SCORE,THOLD,LOWER,UPPER,VALUE,ZERO,ONE,TWO,MORE)"
						+ "VALUES (" + addQuotes(description) + "," + year + ","
						+ addQuotes(format.format(f.fe.fixture.date)) + "," + addQuotes(competition) + ","
						+ f.fe.fixture.matchday + "," + addQuotes(f.fe.fixture.homeTeam) + ","
						+ addQuotes(f.fe.fixture.awayTeam) + "," + f.fe.fixture.result.goalsHomeTeam + ","
						+ f.fe.fixture.result.goalsAwayTeam + "," + f.fe.fixture.getMaxClosingOverOdds() + ","
						+ f.fe.fixture.getMaxClosingUnderOdds() + ","
						+ (float) Math.round(f.fe.prediction * 100000f) / 100000f + "," + f.fe.threshold + ","
						+ f.fe.lower + "," + f.fe.upper + "," + f.fe.value + "," + f.zero + "," + f.one + "," + f.two
						+ "," + f.more + " );";
				try {
					if (!Float.isNaN(f.fe.prediction))
						stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("tuka");
				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	public synchronized static void storeAllData(ArrayList<AllEntry> halftimeData, int year, String competition,
			String description) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (AllEntry f : halftimeData) {

				String sql = "INSERT INTO ALLDATA "
						+ "(DESCRIPTION,YEAR,DATE,COMPETITION,MATCHDAY,HOMETEAMNAME,AWAYTEAMNAME,HOMEGOALS,AWAYGOALS,OVER,UNDER,SCORE,THOLD,LOWER,UPPER,VALUE,ZERO,ONE,TWO,MORE,BASIC,POISSON,WEIGHTED,SHOTS)"
						+ "VALUES (" + addQuotes(description) + "," + year + ","
						+ addQuotes(format.format(f.fe.fixture.date)) + "," + addQuotes(competition) + ","
						+ f.fe.fixture.matchday + "," + addQuotes(f.fe.fixture.homeTeam) + ","
						+ addQuotes(f.fe.fixture.awayTeam) + "," + f.fe.fixture.result.goalsHomeTeam + ","
						+ f.fe.fixture.result.goalsAwayTeam + "," + f.fe.fixture.getMaxClosingOverOdds() + ","
						+ f.fe.fixture.getMaxClosingUnderOdds() + ","
						+ (float) Math.round(f.fe.prediction * 100000f) / 100000f + "," + f.fe.threshold + ","
						+ f.fe.lower + "," + f.fe.upper + "," + f.fe.value + "," + f.zero + "," + f.one + "," + f.two
						+ "," + f.more + "," + f.basic + "," + f.poisson + "," + f.weighted + "," + f.shots + " );";
				try {
					if (!Float.isNaN(f.fe.prediction))
						stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("tuka");
				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	public static void storeFixtures(ArrayList<Fixture> list, int year) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (Fixture f : list) {

				String sql = "REPLACE INTO Fixtures "
						+ "(Date,Competition,StartYear,EndYear,Matchday,HomeTeamName,AwayTeamName,HomeGoals,AwayGoals,HTHome,HTAway)"
						+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.competition) + "," + f.year
						+ "," + f.year + "," + +f.matchday + "," + addQuotes(f.homeTeam) + "," + addQuotes(f.awayTeam)
						+ "," + f.result.goalsHomeTeam + "," + f.result.goalsAwayTeam + "," + f.HTresult.goalsHomeTeam
						+ "," + f.HTresult.goalsAwayTeam + " );";
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Store Fixture in db problem");
					System.out.println(f);
				}

				storeMatchOdds(f, stmt);
				storeOverUnderOdds(f, stmt);
				storeAsianOdds(f, stmt);

			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}

	private static void storeAsianOdds(Fixture f, Statement stmt) {
		// store all Asian odds for the fixture
		for (AsianOdds i : f.asianOdds) {
			String sqlOU = "REPLACE INTO AsianOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,Line,HomeOdds,AwayOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.line + "," + i.homeOdds + "," + i.awayOdds + "," + (i.isOpening ? 1 : 0) + ","
					+ (i.isClosing ? 1 : 0) + "," + (i.isActive ? 1 : 0) + " );";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store asian in db problem ");
				System.out.println(i);
			}
		}
	}

	private static void storeOverUnderOdds(Fixture f, Statement stmt) {
		// store all O/U odds for the fixture
		for (OverUnderOdds i : f.overUnderOdds) {
			String sqlOU = "REPLACE INTO OverUnderOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,Line,OverOdds,UnderOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.line + "," + i.overOdds + "," + i.underOdds + "," + (i.isOpening ? 1 : 0) + ","
					+ (i.isClosing ? 1 : 0) + "," + (i.isActive ? 1 : 0) + " );";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store OU in db problem ");
				System.out.println(i);
			}
		}

	}

	private static void storeMatchOdds(Fixture f, Statement stmt) {
		// store all 1x2 odds for the fixture
		for (MatchOdds i : f.matchOdds) {
			String sqlOU = "REPLACE INTO MatchOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,HomeOdds,DrawOdds,AwayOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.homeOdds + "," + i.drawOdds + "," + i.awayOdds + "," + (i.isOpening ? 1 : 0) + ","
					+ (i.isClosing ? 1 : 0) + "," + (i.isActive ? 1 : 0) + " );";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store 1x2 in db problem ");
				System.out.println(i);
			}
		}
	}

	/**
	 * Selects full data fixtures from db
	 * 
	 * @param competition
	 * @param year
	 * @return
	 */
	public static ArrayList<Fixture> selectFixtures(String competition, int year) {
		ArrayList<Fixture> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Fixtures" + " where StartYear=" + year + " AND competition="
					+ addQuotes(competition) + ";");
			while (rs.next()) {
				String date = rs.getString("date");
				// int matchday = rs.getInt("matchday");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homeGoals");
				int awayGoals = rs.getInt("awayGoals");
				int htHome = rs.getInt("HTHome");
				int htAway = rs.getInt("HTAway");

				Fixture f = new Fixture(format.parse(date), competition, homeTeamName, awayTeamName,
						new Result(homeGoals, awayGoals)).withHTResult(new Result(htHome, htAway)).withYear(year);

				result.add(f);
			}
			rs.close();

			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		addOddsData(result, competition, year);
		ArrayList<Fixture> combined = addGameStatsData(result, competition, year);

		return combined;
	}

	private static ArrayList<Fixture> addGameStatsData(ArrayList<Fixture> result, String competition, int year) {
		ArrayList<Fixture> gameStats = selectGameStats(competition, year);

		fillMissingShotsData(gameStats, competition);

		ArrayList<Fixture> pending = result.stream().filter(f -> f.result.equals(Result.of(-1, -1)))
				.collect(Collectors.toCollection(ArrayList::new));
		ArrayList<Fixture> finished = result.stream().filter(f -> !f.result.equals(Result.of(-1, -1)))
				.collect(Collectors.toCollection(ArrayList::new));

		FixtureListCombiner combiner = new FixtureListCombiner(finished, gameStats, competition);
		ArrayList<Fixture> combined = combiner.combineWithDictionary();
		combined.addAll(pending);

		return combined;
	}

	private static void fillMissingShotsData(ArrayList<Fixture> gameStats, String competition) {
		int missingDataCount = 0;
		for (Fixture i : gameStats) {
			if (i.gameStats == null)
				i.gameStats = GameStats.initial();

			if (i.gameStats.equals(GameStats.initial()) || i.gameStats.getShotsHome() == -1) {
				missingDataCount++;
				i.gameStats.shots = Pair.of(i.result.goalsHomeTeam, i.result.goalsAwayTeam);
			}
		}
		if (missingDataCount > 0)
			System.out.println(
					"Missing data for: " + competition + " : " + missingDataCount + " filled with goals equivalents");
	}

	public static ArrayList<Fixture> selectGameStats(String competition, int year) {
		ArrayList<Fixture> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery(
					"select * from GameStats " + " where competition=" + addQuotes(competition) + " and year =" + year);
			while (matchRs.next()) {
				String date = matchRs.getString("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				int homeGoals = matchRs.getInt("homeGoals");
				int awayGoals = matchRs.getInt("awayGoals");
				int hthome = matchRs.getInt("hthome");
				int htaway = matchRs.getInt("htaway");
				int allEuroShotsHome = matchRs.getInt("alllEuroShotsHome");
				int allEuroShotsAway = matchRs.getInt("allEuroShotsAway");
				int shotsHome = matchRs.getInt("shotsHome");
				int shotsAway = matchRs.getInt("shotsAway");
				int shotsWideHome = matchRs.getInt("shotsWideHome");
				int shotsWideAway = matchRs.getInt("shotsWideAway");
				int cornersHome = matchRs.getInt("cornersHome");
				int cornersAway = matchRs.getInt("cornersAway");
				int foulsHome = matchRs.getInt("foulsHome");
				int foulsAway = matchRs.getInt("foulsAway");
				int offsidesHome = matchRs.getInt("offsidesHome");
				int offsidesAway = matchRs.getInt("offsidesAway");
				int possessionHome = matchRs.getInt("possessionHome");

				GameStats gs = new GameStats(Pair.of(shotsHome, shotsAway), Pair.of(shotsWideHome, shotsWideAway),
						Pair.of(cornersHome, cornersAway), Pair.of(foulsHome, foulsAway),
						Pair.of(offsidesHome, offsidesAway))
								.withAllEuroShots(Pair.of(allEuroShotsHome, allEuroShotsAway))
								.withPossession(possessionHome);

				Fixture f = new Fixture(format.parse(date), competition, homeTeam, awayTeam,
						new Result(homeGoals, awayGoals)).withHTResult(new Result(hthome, htaway)).withYear(year)
								.withGameStats(gs);

				result.add(f);
			}

			matchRs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	// TODO loads only the OU odds for now, because of performance resa
	private static void addOddsData(ArrayList<Fixture> result, String competition, int year) {
		// ArrayList<MatchOdds> matchOdds = selectMatchOdds(competition, year);
		// ArrayList<AsianOdds> asianOdds = selectAsianOdds(competition, year);
		ArrayList<OverUnderOdds> overUnderOdds = selectOverUnderOdds(competition, year);

		for (Fixture i : result) {
			try {
				// i.matchOdds = matchOdds.stream().filter(mo -> mo.fixtureDate.equals(i.date)
				// && mo.homeTeamName.equals(i.homeTeam) && mo.awayTeamName.equals(i.awayTeam))
				// .collect(Collectors.toCollection(ArrayList::new));
				// i.asianOdds = asianOdds.stream().filter(mo -> mo.fixtureDate.equals(i.date)
				// && mo.homeTeamName.equals(i.homeTeam) && mo.awayTeamName.equals(i.awayTeam))
				// .collect(Collectors.toCollection(ArrayList::new));
				i.overUnderOdds = overUnderOdds.stream().filter(mo -> mo.fixtureDate.equals(i.date)
						&& mo.homeTeamName.equals(i.homeTeam) && mo.awayTeamName.equals(i.awayTeam))
						.collect(Collectors.toCollection(ArrayList::new));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static ArrayList<OverUnderOdds> selectOverUnderOdds(String competition, int year) {
		ArrayList<OverUnderOdds> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join overunderodds on"
					+ " (fixtures.date = overunderodds.date and fixtures.hometeamname=overunderodds.hometeamname  and fixtures.awayteamname=overunderodds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				String fixtureDate = matchRs.getString("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				String time = matchRs.getString("time");
				// int matchday = matchRs.getInt("matchday");
				float line = matchRs.getFloat("line");
				float overOdds = matchRs.getFloat("overOdds");
				float underOdds = matchRs.getFloat("underOdds");
				int isOpening = matchRs.getInt("isOpening");
				int isClosing = matchRs.getInt("isClosing");
				int isActive = -1;
				if (year >= 2017)
					isActive = matchRs.getInt("isActive");

				OverUnderOdds mo = new OverUnderOdds(bookmaker, format.parse(time), line, overOdds, underOdds)
						.withFixtureFields(format.parse(fixtureDate), homeTeam, awayTeam);
				mo.isOpening = isOpening == 1;
				mo.isClosing = isClosing == 1;
				if (year >= 2017)
					mo.isActive = isActive == 1;
				result.add(mo);
			}

			matchRs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	private static ArrayList<AsianOdds> selectAsianOdds(String competition, int year) {
		ArrayList<AsianOdds> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join AsianOdds on"
					+ " (fixtures.date = AsianOdds.date and fixtures.hometeamname=AsianOdds.hometeamname  and fixtures.awayteamname=AsianOdds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				String fixtureDate = matchRs.getString("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				String time = matchRs.getString("date");
				// int matchday = matchRs.getInt("matchday");
				float line = matchRs.getFloat("line");
				float homeOdds = matchRs.getFloat("homeOdds");
				float awayOdds = matchRs.getFloat("awayOdds");
				int isOpening = matchRs.getInt("isOpening");
				int isClosing = matchRs.getInt("isClosing");
				int isActive = matchRs.getInt("isActive");

				AsianOdds mo = new AsianOdds(bookmaker, format.parse(time), line, homeOdds, awayOdds)
						.withFixtureFields(format.parse(fixtureDate), homeTeam, awayTeam);
				mo.isOpening = isOpening == 1;
				mo.isClosing = isClosing == 1;
				mo.isActive = isActive == 1;
				result.add(mo);
			}

			matchRs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	private static ArrayList<MatchOdds> selectMatchOdds(String competition, int year) {
		ArrayList<MatchOdds> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join matchodds on"
					+ " (fixtures.date = matchodds.date and fixtures.hometeamname=matchodds.hometeamname  and fixtures.awayteamname=matchodds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				String fixtureDate = matchRs.getString("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				String time = matchRs.getString("time");
				// int matchday = matchRs.getInt("matchday");
				float homeOdds = matchRs.getFloat("homeOdds");
				float drawOdds = matchRs.getFloat("drawOdds");
				float awayOdds = matchRs.getFloat("awayOdds");
				int isOpening = matchRs.getInt("isOpening");
				int isClosing = matchRs.getInt("isClosing");
				int isActive = matchRs.getInt("isActive");

				MatchOdds mo = new MatchOdds(bookmaker, format.parse(time), homeOdds, drawOdds, awayOdds)
						.withFixtureFields(format.parse(fixtureDate), homeTeam, awayTeam);
				mo.isOpening = isOpening == 1;
				mo.isClosing = isClosing == 1;
				mo.isActive = isActive == 1;
				result.add(mo);
			}

			matchRs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return result;
	}

	public static void storeGameStats(ArrayList<Fixture> stats, String competition, int year) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (year == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (Fixture f : stats) {

				String sql = "INSERT OR IGNORE INTO GameStats "
						+ "(Competition,Year,Date,HomeTeamName,AwayTeamName,HomeGoals,AwayGoals,HTHome,HTAway,AlllEuroShotsHome ,AllEuroShotsAway ,"
						+ "ShotsHome ,ShotsAway ,ShotsWideHome ,ShotsWideAway ,CornersHome ,CornersAway ,FoulsHome ,FoulsAway ,OffsidesHome ,OffsidesAway ,PossessionHome)"
						+ "VALUES (" + addQuotes(competition) + "," + year + "," + addQuotes(format.format(f.date))
						+ "," + addQuotes(f.homeTeam) + "," + addQuotes(f.awayTeam) + "," + f.result.goalsHomeTeam + ","
						+ f.result.goalsAwayTeam + "," + f.HTresult.goalsHomeTeam + "," + f.HTresult.goalsAwayTeam + ","
						+ f.gameStats.AllEuroShots.home + "," + f.gameStats.AllEuroShots.away + ","
						+ f.gameStats.shots.home + "," + f.gameStats.shots.away + "," + f.gameStats.shotsWide.home + ","
						+ f.gameStats.shotsWide.away + "," + f.gameStats.corners.home + "," + f.gameStats.corners.away
						+ "," + f.gameStats.fouls.home + "," + f.gameStats.fouls.away + "," + f.gameStats.offsides.home
						+ "," + f.gameStats.offsides.away + "," + f.gameStats.possessionHome + " );";
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
					System.out.println("Error storing game stats for");
					System.out.println(f);
					e.printStackTrace();
				}
			}

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			try {
				c.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}

	}

	public static Date findLastPendingGameStatsDate(String league, int collectYear) {
		Connection c = null;
		Statement stmt = null;
		Date date = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (collectYear == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select max(date) as date from gamestats where competition="
					+ addQuotes(league) + " and year=" + collectYear + ";");
			while (rs.next()) {
				String dateStr = rs.getString("date");
				if (dateStr != null)
					date = format.parse(rs.getString("date"));
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		if (date == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Scraper.CURRENT_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, 1);
			date = cal.getTime();
		}

		return date;
	}

	public static Date findLastPendingFixtureDate(String league, int collectYear) {
		Connection c = null;
		Statement stmt = null;
		Date date = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbname = "full_data" + (collectYear == 2017 ? "2017" : "");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbname + ".db");
			c.setAutoCommit(false);
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select min(date) as date from fixtures where competition="
					+ addQuotes(league) + " and startyear=" + collectYear + " and homegoals=-1;");
			while (rs.next()) {
				String dateStr = rs.getString("date");
				if (dateStr != null)
					date = format.parse(rs.getString("date"));
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		if (date == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Scraper.CURRENT_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, 1);
			date = cal.getTime();
		}

		return date;
	}

}
