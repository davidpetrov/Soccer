package scraper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import jdbc.PostgreSQL;
import main.Fixture;
import main.GameStats;
import main.Result;
import main.SQLiteJDBC;
import utils.FixtureListCombiner;
import utils.Pair;
import utils.Utils;

public class GameStatsCollector {
	public static final String BASE = "http://int.soccerway.com/";
	public static final DateFormat FORMATFULL = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);

	public String competition;
	public int year;
	public String optionalFullAddress;

	public GameStatsCollector(String competition, int year, String optionalFullAddress) {
		super();
		this.competition = competition;
		this.year = year;
		this.optionalFullAddress = optionalFullAddress;
	}

	public static GameStatsCollector of(String competition, int year) {
		return new GameStatsCollector(competition, year, null);
	}

	public void collectAndStore() throws Exception, IOException, ParseException {
		ArrayList<Fixture> stats = collect();
		PostgreSQL.storeGameStats(stats, competition, year);
	}

	public ArrayList<Fixture> collect() throws InterruptedException, IOException, ParseException {
		return collectUpToDate(null);
	}

	// TODO possible smarter impl with js calls
	public ArrayList<Fixture> collectUpToDate(Date oldestTocheck)
			throws InterruptedException, IOException, ParseException {
		Set<Fixture> set = new HashSet<>();
		String address = getAddress();
		System.out.println(address);

		WebDriver driver = createDriver();
		driver.navigate().to(address);

		getFixtures(driver, set, oldestTocheck);

		// try to list by game week
//		Actions actions = new Actions(driver);
//		actions.moveToElement(driver.findElement(By.xpath("//*[text()[contains(.,'By game week')]]"))).click()
//				.perform();

//		getFixtures(driver, set, oldestTocheck);

		driver.close();

		ArrayList<Fixture> setlist = new ArrayList<>(set);
		System.out.println(setlist.size());

		int missingData = countMissingShotsData(setlist);

		// does not store if missing data is too much
		return (oldestTocheck == null && missingData > setlist.size() / 2) ? new ArrayList<>() : setlist;
	}

	private void getFixtures(WebDriver driver, Set<Fixture> set, Date oldestTocheck)
			throws IOException, ParseException, InterruptedException {
		boolean breakFlag = false;
		while (true) {
			String html = driver.getPageSource();
			Document matches = Jsoup.parse(html);
			Element list = matches.select("table[class=matches   ]").first();
			Elements linksM = list.select("a[href]");
			for (int i = linksM.size() - 1; i >= 0; i--) {
				if (isScore(linksM.get(i).text())) {
					Document fixture = Jsoup.connect(BASE + linksM.get(i).attr("href")).get();

					Fixture ef = getGameStatsFixture(fixture, competition);
					if (ef != null && oldestTocheck != null && ef.date.before(oldestTocheck)) {
						breakFlag = true;
						break;
					}
					set.add(ef);
				}
			}

			if (breakFlag)
				break;

			Actions actions = new Actions(driver);
			actions.moveToElement(driver.findElement(By.className("previous"))).click().perform();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;
		}
	}

	private WebDriver createDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		return driver;
	}

	private int countMissingShotsData(ArrayList<Fixture> setlist) {
		int missingDataCount = 0;
		for (Fixture i : setlist) {
			if (i.gameStats == null)
				i.gameStats = GameStats.initial();

			if (i.gameStats.equals(GameStats.initial()) || i.gameStats.getShotsHome() == -1) {
				missingDataCount++;
			}
		}
		System.out.println("Missing data for: " + missingDataCount);
		return missingDataCount;
	}

	private Fixture getGameStatsFixture(Document fixture, String competition) throws IOException, ParseException {
		Result ht = getHalfTimeResult(fixture);
		Result result = getResult(fixture);
		if (result.equals(Result.of(-1, -1)))
			return null;
		int matchday = getMatchday(fixture);
		Date date = getDate(fixture);

		String teams = fixture.select("h1").first().text();
		String homeTeam = Utils.replaceNonAsciiWhitespace(getHome(teams));
		String awayTeam = Utils.replaceNonAsciiWhitespace(getAway(teams));

		GameStats gameStats = getGameStats(fixture);

		Fixture f = new Fixture(date, competition, homeTeam, awayTeam, result).withHTResult(ht).withYear(year)
				.withGameStats(gameStats);
		// System.out.println(f);

		return f;

	}

	private GameStats getGameStats(Document fixture) throws IOException {
		GameStats gameStats = GameStats.initial();
		// Possession can't be parsed using jsoup only
		// possible if using selenium but performance would be worse

		Elements frames = fixture.select("iframe");
		for (Element i : frames) {
			if (i.attr("src").contains("/charts/statsplus")) {
				Document stats = Jsoup.connect(BASE + i.attr("src")).timeout(0).get();
				String text = stats.text();
				String str = text.replaceAll("[^-?0-9]+", " ");
				String[] arr = str.trim().split(" ");
				if (text.isEmpty() || arr.length != 10)
					return null;

				Pair shots = Pair.of(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
				Pair shotsWide = Pair.of(Integer.parseInt(arr[2]), Integer.parseInt(arr[3]));
				Pair corners = Pair.of(Integer.parseInt(arr[4]), Integer.parseInt(arr[5]));
				Pair fouls = Pair.of(Integer.parseInt(arr[6]), Integer.parseInt(arr[7]));
				Pair offsides = Pair.of(Integer.parseInt(arr[8]), Integer.parseInt(arr[9]));

				gameStats = new GameStats(shots, shotsWide, corners, fouls, offsides);
				break;
			}
		}

		return gameStats;

	}

	private Date getDate(Document fixture) throws ParseException {
		String dateString = fixture.select("dt:contains(Date) + dd").first().text();
		// default starting time, in case of missing info
		String timeString = "12:00";
		try {
			timeString = fixture.select("dt:contains(Kick-off) + dd").first().text();
		} catch (Exception e) {
			System.out.println("Problem parsing starting time");
		}
		// the time is in gmt+1
		long hour = 3600 * 1000;
		Date date = new Date(FORMATFULL.parse(dateString + " " + timeString).getTime() + hour);
		return date;
	}

	private int getMatchday(Document fixture) {
		int matchday = -1;
		try {
			matchday = Integer.parseInt(fixture.select("dt:contains(Game week) + dd").first().text());
		} catch (Exception e) {
		}
		return matchday;
	}

	private Result getResult(Document fixture) {
		Result result = new Result(-1, -1);
		try {
			result = parseResult(fixture.select("dt:contains(Full-time) + dd").first().text());
		} catch (Exception e) {
		}
		return result;
	}

	private Result getHalfTimeResult(Document fixture) {
		Result ht = new Result(-1, -1);
		try {
			ht = parseResult(fixture.select("dt:contains(Half-time) + dd").first().text());
		} catch (Exception e) {
			System.out.println("No ht result!");
		}
		return ht;
	}

	private String getAddress() {
		return optionalFullAddress == null ? EntryPoints.getLink(competition, year) : optionalFullAddress;
	}

	private static boolean isScore(String text) {
		String[] splitted = text.split("-");

		return splitted.length == 2 && isNumeric(splitted[0].trim()) && isNumeric(splitted[1].trim());
	}

	private static Result parseResult(String text) {
		String[] splitted = text.split("-");
		return new Result(Integer.parseInt(splitted[0].trim()), Integer.parseInt(splitted[1].trim()));
	}

	private static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static String getHome(String teams) {
		String[] split = teams.split(" vs. ");
		return split[0].trim();
	}

	private static String getAway(String teams) {
		String[] split = teams.split(" vs. ");

		if (!split[1].contains("-"))
			return split[1];
		else {
			String[] splitAway = split[1].split(" ");
			String awayTeam = "";
			for (int j = 0; j < splitAway.length - 3; j++)
				awayTeam += splitAway[j] + " ";

			return awayTeam.trim();
		}

	}

}
