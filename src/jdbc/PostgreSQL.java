package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import main.Fixture;
import main.GameStats;
import main.Result;
import main.SQLiteJDBC;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;
import scraper.FullOddsCollector;
import scraper.Scraper;
import utils.FixtureListCombiner;
import utils.Pair;

public class PostgreSQL {
	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	final String postgreformat = "yyyy-MM-dd HH:mm:ss.SSSX";

	public static void main(String args[]) throws InterruptedException {

		String[] competitions = new String[] { "BRA", "SWI", "SPA", "BEL", "IT", "ENG5", "IT2", "ENG2", "TUR", "FR2",
				"ENG3", "ENG4", "GRE", "FR", "NED", "POR", "SPA2", "SCO", "GER2", "GER", };

		for (int year = 2010; year <= 2014; year++) {
			for (String i : SQLiteJDBC.getLeagues(year)) {
				long start = System.currentTimeMillis();
				ArrayList<Fixture> list = SQLiteJDBC.selectFixtures(i, year);
				System.out.println((System.currentTimeMillis() - start) / 1000d + "sec loading data for: " + i);
				storeFixtures(list, year);
				storeGameStats(list, i, year);
			}
		}
		// Calendar cal = Calendar.getInstance();
		// cal.set(2017, Calendar.DECEMBER, 1); // Year, month and day of month
		// Date date = cal.getTime();
		// ArrayList<Fixture> list = FullOddsCollector.of("BUL",
		// 2017).collectUpToDate(date);
		// storeFixtures(list, 2017);

		// storeGameStats(list, i, 2017);

		// Connection c = null;
		// try {
		// Class.forName("org.postgresql.Driver");
		// c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
		// "postgres", "postgres");
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.err.println(e.getClass().getName() + ": " + e.getMessage());
		// System.exit(0);
		// }
		// System.out.println("Opened database successfully");
	}

	public static void storeFixtures(ArrayList<Fixture> list, int year) {
		Connection c = null;
		Statement stmt = null;
		try {

			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			for (Fixture f : list) {

				String sql = "INSERT INTO Fixtures "
						+ "(Date,Competition,StartYear,EndYear,Matchday,HomeTeamName,AwayTeamName,HomeGoals,AwayGoals,HTHome,HTAway)"
						+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.competition) + "," + f.year
						+ "," + f.year + "," + +f.matchday + "," + addQuotes(f.homeTeam) + "," + addQuotes(f.awayTeam)
						+ "," + f.result.goalsHomeTeam + "," + f.result.goalsAwayTeam + "," + f.HTresult.goalsHomeTeam
						+ "," + f.HTresult.goalsAwayTeam
						+ " )  ON CONFLICT (Date,HomeTeamName,awayteamname)  DO UPDATE SET"
						+ "    homegoals = EXCLUDED.homegoals," + "    awaygoals = EXCLUDED.awaygoals,"
						+ "    hthome = EXCLUDED.hthome," + "    htaway= EXCLUDED.htaway ;";
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Store Fixture in db problem");
					System.out.println(f);
				}

				storeMatchOdds(f, stmt);
				// TODO change that to normal storing
				storeOverUnderOddsPart(f, stmt, year);
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

	private static void storeOverUnderOddsPart(Fixture f, Statement stmt, int year) {
		if (f.result.equals(Result.postponed()))
			return;
		// store all O/U odds for the fixture
		for (OverUnderOdds i : f.overUnderOdds) {
			String sqlOU = "INSERT INTO OverUnderOddsPart "
					+ "(year,competition,Date,HomeTeamName,AwayTeamName,Bookmaker,Time,Line,OverOdds,UnderOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + year + "," + addQuotes(f.competition) + "," + addQuotes(format.format(f.date)) + ","
					+ addQuotes(f.homeTeam) + "," + addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + ","
					+ addQuotes(format.format(i.time)) + "," + i.line + "," + i.overOdds + "," + i.underOdds + ","
					+ i.isOpening + "," + i.isClosing + "," + i.isActive
					+ " ) ON CONFLICT (date, hometeamname, awayteamname, bookmaker, line, time) DO NOTHING;";
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
		if (f.result.equals(Result.postponed()))
			return;
		// store all 1x2 odds for the fixture
		for (MatchOdds i : f.matchOdds) {
			String sqlOU = "INSERT INTO MatchOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,HomeOdds,DrawOdds,AwayOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.homeOdds + "," + i.drawOdds + "," + i.awayOdds + "," + i.isOpening + "," + i.isClosing
					+ "," + i.isActive
					+ " ) ON CONFLICT (date, hometeamname, awayteamname, bookmaker, time) DO NOTHING;";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store 1x2 in db problem ");
				System.out.println(i);
			}
		}
	}

	private static void storeOverUnderOdds(Fixture f, Statement stmt) {
		if (f.result.equals(Result.postponed()))
			return;
		// store all O/U odds for the fixture
		for (OverUnderOdds i : f.overUnderOdds) {
			String sqlOU = "INSERT INTO OverUnderOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,Line,OverOdds,UnderOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.line + "," + i.overOdds + "," + i.underOdds + "," + i.isOpening + "," + i.isClosing + ","
					+ i.isActive
					+ " ) ON CONFLICT (date, hometeamname, awayteamname, bookmaker, line, time) DO NOTHING;";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store OU in db problem ");
				System.out.println(i);
			}
		}
	}

	private static void storeAsianOdds(Fixture f, Statement stmt) {
		if (f.result.equals(Result.postponed()))
			return;
		// store all Asian odds for the fixture
		for (AsianOdds i : f.asianOdds) {
			String sqlOU = "INSERT INTO AsianOdds "
					+ "(Date,HomeTeamName,AwayTeamName,Bookmaker,Time,Line,HomeOdds,AwayOdds,isOpening,isClosing,isActive)"
					+ "VALUES (" + addQuotes(format.format(f.date)) + "," + addQuotes(f.homeTeam) + ","
					+ addQuotes(f.awayTeam) + "," + addQuotes(i.bookmaker) + "," + addQuotes(format.format(i.time))
					+ "," + i.line + "," + i.homeOdds + "," + i.awayOdds + "," + i.isOpening + "," + i.isClosing + ","
					+ i.isActive
					+ " ) ON CONFLICT (date, hometeamname, awayteamname, bookmaker, line, time) DO NOTHING;";
			try {
				stmt.executeUpdate(sqlOU);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Store asian in db problem ");
				System.out.println(i);
			}
		}
	}

	public static void storeGameStats(ArrayList<Fixture> stats, String competition, int year) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			for (Fixture f : stats) {

				String sql = "INSERT INTO GameStats "
						+ "(Competition,Year,Date,HomeTeamName,AwayTeamName,HomeGoals,AwayGoals,HTHome,HTAway,AlllEuroShotsHome ,AllEuroShotsAway ,"
						+ "ShotsHome ,ShotsAway ,ShotsWideHome ,ShotsWideAway ,CornersHome ,CornersAway ,FoulsHome ,FoulsAway ,OffsidesHome ,OffsidesAway ,PossessionHome)"
						+ "VALUES (" + addQuotes(competition) + "," + year + "," + addQuotes(format.format(f.date))
						+ "," + addQuotes(f.homeTeam) + "," + addQuotes(f.awayTeam) + "," + f.result.goalsHomeTeam + ","
						+ f.result.goalsAwayTeam + "," + f.HTresult.goalsHomeTeam + "," + f.HTresult.goalsAwayTeam + ","
						+ f.gameStats.AllEuroShots.home + "," + f.gameStats.AllEuroShots.away + ","
						+ f.gameStats.shots.home + "," + f.gameStats.shots.away + "," + f.gameStats.shotsWide.home + ","
						+ f.gameStats.shotsWide.away + "," + f.gameStats.corners.home + "," + f.gameStats.corners.away
						+ "," + f.gameStats.fouls.home + "," + f.gameStats.fouls.away + "," + f.gameStats.offsides.home
						+ "," + f.gameStats.offsides.away + "," + f.gameStats.possessionHome
						+ " ) ON CONFLICT (date, hometeamname, awayteamname) DO NOTHING;";
				;
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

	public static ArrayList<Fixture> selectFixtures(String competition, int year) {
		ArrayList<Fixture> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("select * from Fixtures" + " where StartYear=" + year + " AND competition="
					+ addQuotes(competition) + ";");
			while (rs.next()) {
				Timestamp ts = rs.getTimestamp("date");
				String homeTeamName = rs.getString("hometeamname");
				String awayTeamName = rs.getString("awayteamname");
				int homeGoals = rs.getInt("homeGoals");
				int awayGoals = rs.getInt("awayGoals");
				int htHome = rs.getInt("HTHome");
				int htAway = rs.getInt("HTAway");

				Fixture f = new Fixture(new Date(ts.getTime()), competition, homeTeamName, awayTeamName,
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

	// TODO loads only the OU odds for now, because of performance resa
	private static void addOddsData(ArrayList<Fixture> result, String competition, int year) {
		// ArrayList<MatchOdds> matchOdds = selectMatchOdds(competition, year);
		// ArrayList<AsianOdds> asianOdds = selectAsianOdds(competition, year);
		// ArrayList<OverUnderOdds> overUnderOdds = selectOverUnderOdds(competition,
		// year);
		ArrayList<OverUnderOdds> overUnderOdds = selectOverUnderOddsPart(competition, year);

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

	private static ArrayList<OverUnderOdds> selectOverUnderOddsPart(String competition, int year) {
		ArrayList<OverUnderOdds> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			stmt.execute("SET constraint_exclusion = on;");
			ResultSet matchRs = stmt.executeQuery("select * from  overunderoddspart" + " where competition="
					+ addQuotes(competition) + "and  year =" + year);
			while (matchRs.next()) {
				Timestamp ts = matchRs.getTimestamp("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				Timestamp tsTime = matchRs.getTimestamp("time");
				float line = matchRs.getFloat("line");
				float overOdds = matchRs.getFloat("overOdds");
				float underOdds = matchRs.getFloat("underOdds");
				boolean isOpening = matchRs.getBoolean("isOpening");
				boolean isClosing = matchRs.getBoolean("isClosing");
				boolean isActive = matchRs.getBoolean("isActive");

				OverUnderOdds mo = new OverUnderOdds(bookmaker, new Date(tsTime.getTime()), line, overOdds, underOdds)
						.withFixtureFields(new Date(ts.getTime()), homeTeam, awayTeam);
				mo.isOpening = isOpening;
				mo.isClosing = isClosing;
				mo.isActive = isActive;
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

	private static ArrayList<OverUnderOdds> selectOverUnderOdds(String competition, int year) {
		ArrayList<OverUnderOdds> result = new ArrayList<>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join overunderodds on"
					+ " (fixtures.date = overunderodds.date and fixtures.hometeamname=overunderodds.hometeamname  and fixtures.awayteamname=overunderodds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				Timestamp ts = matchRs.getTimestamp("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				Timestamp tsTime = matchRs.getTimestamp("time");
				float line = matchRs.getFloat("line");
				float overOdds = matchRs.getFloat("overOdds");
				float underOdds = matchRs.getFloat("underOdds");
				boolean isOpening = matchRs.getBoolean("isOpening");
				boolean isClosing = matchRs.getBoolean("isClosing");
				boolean isActive = matchRs.getBoolean("isActive");

				OverUnderOdds mo = new OverUnderOdds(bookmaker, new Date(tsTime.getTime()), line, overOdds, underOdds)
						.withFixtureFields(new Date(ts.getTime()), homeTeam, awayTeam);
				mo.isOpening = isOpening;
				mo.isClosing = isClosing;
				mo.isActive = isActive;
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
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join AsianOdds on"
					+ " (fixtures.date = AsianOdds.date and fixtures.hometeamname=AsianOdds.hometeamname  and fixtures.awayteamname=AsianOdds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				Timestamp ts = matchRs.getTimestamp("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				Timestamp tsTime = matchRs.getTimestamp("time");
				float line = matchRs.getFloat("line");
				float homeOdds = matchRs.getFloat("homeOdds");
				float awayOdds = matchRs.getFloat("awayOdds");
				boolean isOpening = matchRs.getBoolean("isOpening");
				boolean isClosing = matchRs.getBoolean("isClosing");
				boolean isActive = matchRs.getBoolean("isActive");

				AsianOdds mo = new AsianOdds(bookmaker, new Date(tsTime.getTime()), line, homeOdds, awayOdds)
						.withFixtureFields(new Date(ts.getTime()), homeTeam, awayTeam);
				mo.isOpening = isOpening;
				mo.isClosing = isClosing;
				mo.isActive = isActive;
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
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery("select * from fixtures " + "join matchodds on"
					+ " (fixtures.date = matchodds.date and fixtures.hometeamname=matchodds.hometeamname  and fixtures.awayteamname=matchodds.awayteamname )"
					+ " where competition=" + addQuotes(competition) + " and startyear =" + year);
			while (matchRs.next()) {
				Timestamp ts = matchRs.getTimestamp("date");
				String homeTeam = matchRs.getString("hometeamname");
				String awayTeam = matchRs.getString("awayteamname");
				String bookmaker = matchRs.getString("Bookmaker");
				Timestamp tsTime = matchRs.getTimestamp("time");
				float homeOdds = matchRs.getFloat("homeOdds");
				float drawOdds = matchRs.getFloat("drawOdds");
				float awayOdds = matchRs.getFloat("awayOdds");
				boolean isOpening = matchRs.getBoolean("isOpening");
				boolean isClosing = matchRs.getBoolean("isClosing");
				boolean isActive = matchRs.getBoolean("isActive");

				MatchOdds mo = new MatchOdds(bookmaker, new Date(tsTime.getTime()), homeOdds, drawOdds, awayOdds)
						.withFixtureFields(new Date(ts.getTime()), homeTeam, awayTeam);
				mo.isOpening = isOpening;
				mo.isClosing = isClosing;
				mo.isActive = isActive;
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
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			ResultSet matchRs = stmt.executeQuery(
					"select * from GameStats " + " where competition=" + addQuotes(competition) + " and year =" + year);
			while (matchRs.next()) {
				Timestamp date = matchRs.getTimestamp("date");
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

				Fixture f = new Fixture(date, competition, homeTeam, awayTeam, new Result(homeGoals, awayGoals))
						.withHTResult(new Result(hthome, htaway)).withYear(year).withGameStats(gs);
				if (!f.result.equals(Result.of(-1, -1)))
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

	public static Date findLastPendingGameStatsDate(String league, int collectYear) {
		Connection c = null;
		Statement stmt = null;
		Date date = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select max(date) as date from gamestats where competition="
					+ addQuotes(league) + " and year=" + collectYear + ";");
			while (rs.next()) {
				String dateStr = rs.getString("date");
				if (dateStr != null)
					date = new Date(rs.getTimestamp("date").getTime());
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
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("select min(date) as date from fixtures where competition="
					+ addQuotes(league) + " and startyear=" + collectYear + " and homegoals=-1;");
			while (rs.next()) {
				String dateStr = rs.getString("date");
				if (dateStr != null)
					date = new Date(rs.getTimestamp("date").getTime());
			}

			if (date == null) {
				rs = stmt.executeQuery("select max(date) as date from fixtures where competition=" + addQuotes(league)
						+ " and startyear=" + collectYear + ";");
				while (rs.next()) {
					String dateStr = rs.getString("date");
					if (dateStr != null)
						date = new Date(rs.getTimestamp("date").getTime());
				}
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
}
