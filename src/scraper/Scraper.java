package scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import main.AsianLines;
import main.ExtendedFixture;
import main.FullFixture;
import main.GoalLines;
import main.Line;
import main.PlayerFixture;
import main.Result;
import main.SQLiteJDBC;
import predictions.Predictions.OnlyTodayMatches;
import predictions.UpdateType;
import runner.RunnerOdds;
import runner.UpdateRunner;
import utils.Utils;
import xls.XlSUtils;

public class Scraper {
	public static final DateFormat OPTAFORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
	public static final DateFormat FORMATFULL = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
	public static final String BASE = "http://int.soccerway.com/";
	public static final String OUSUFFIX = "#over-under;2;2.50;0";
	public static final int CURRENT_YEAR = 2017;

	public static void main(String[] args)
			throws IOException, ParseException, InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();

		// =================================================================

		for (int i = 2012; i <= 2012; i++) {
			ArrayList<PlayerFixture> list = collectFull("BRA", i, null);
			// //
			// "http://int.soccerway.com/national/scotland/premier-league/2007-2008/regular-season/");
			// //
			// "http://int.soccerway.com/national/germany/bundesliga/2010-2011/regular-season/");
			SQLiteJDBC.storePlayerFixtures(list, i, "BRA");
		}

//		 collectAndStoreSinglePFS("BRA", 2016,
//		 "http://int.soccerway.com/matches/2016/06/23/brazil/serie-a/clube-atletico-mineiro/sport-club-corinthians-paulista/2217995/");

		// ArrayList<PlayerFixture> list =
		// SQLiteJDBC.selectPlayerFixtures("ENG", 2015);
		// System.out.println(list.size());
		// ====================================================================

		// ArrayList<ExtendedFixture> list = collect("BRB", 2017, null);
		// list.addAll(collect("JP", 2016,
		// "http://int.soccerway.com/national/japan/j1-league/2016/2nd-stage/"));
		// XlSUtils.storeInExcel(list, "BRB", 2017, "manual");

		//
		// ArrayList<ExtendedFixture> list = oddsInParallel("ENG", 2013, null);

		// ArrayList<ExtendedFixture> list = odds("NOR", 2017, null);
		// XlSUtils.storeInExcel(list, "NOR", 2017, "odds");
		// nextMatches("GER", null);
		// ArrayList<FullFixture> list = fullOdds("GER", 2016, null);
		// XlSUtils.storeInExcelFull(list, "GER", 2016, "fullodds");

		// ArrayList<FullFixture> list2 = fullOdds("SPA", 2013,
		// "http://www.oddsportal.com/soccer/spain/primera-division-2013-2014");
		// XlSUtils.storeInExcelFull(list2, "SPA", 2013, "fullodds");

		// XlSUtils.combine("BRB", 2017, "manual");
		// XlSUtils.combineFull("SPA", 2015, "all-data");
		// ////
		// XlSUtils.fillMissingShotsData("BRB", 2017, false);

		// ArrayList<ExtendedFixture> next = nextMatches("BRB", null);
		// nextMatches("BRB", null);

//		checkAndUpdate("BRA", OnlyTodayMatches.FALSE);
		// checkAndUpdate("USA", OnlyTodayMatches.FALSE);
		// updateInParallel();

		// fastOdds("SPA", 2016, null);

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");
	}

	/**
	 * Helper method for collecting and storing PFS from a a single fixture due
	 * to some sort of bug in the collection process of collectfull method which
	 * misses only 1 fixture for some reason
	 * 
	 * @param string
	 * @param i
	 * @param string2
	 * @throws IOException
	 * @throws ParseException
	 */
	private static void collectAndStoreSinglePFS(String competition, int year, String link)
			throws IOException, ParseException {
		ArrayList<PlayerFixture> result = new ArrayList<>();
		Set<PlayerFixture> set = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(link);

		Document fixture = Jsoup.connect(link).timeout(0).get();
		ArrayList<PlayerFixture> ef = getFixtureFull(fixture, competition);
		result.addAll(ef);

		driver.close();
		System.out.println(result.size());

		SQLiteJDBC.storePlayerFixtures(result, year, competition);

	}

	/**
	 * Updates list of leagues in parallel
	 * 
	 * @param list
	 *            - list of leagues to be updated
	 * @param n
	 *            - number of leagues updating in parallel
	 * @param onlyToday
	 *            - flag for getting next matches only for today (for speed up)
	 * @param automatic
	 *            - type of update - manual - hardcoded leagues, automatic -
	 *            tracking leagues that have games today
	 * @throws IOException
	 */
	public static void updateInParallel(ArrayList<String> list, int n, OnlyTodayMatches onlyToday, UpdateType automatic)
			throws IOException {

		ExecutorService executor = Executors.newFixedThreadPool(n);
		ArrayList<String> leagues = automatic.equals(UpdateType.AUTOMATIC) ? getTodaysLeagueList() : list;
		System.out.println("Updating for: ");
		System.out.println(leagues);
		for (String i : leagues) {
			Runnable worker = new UpdateRunner(i, onlyToday);
			executor.execute(worker);
		}
		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();
		// Wait until all threads are finish
		// executor.awaitTermination(0, null);
	}

	public static ArrayList<String> getTodaysLeagueList() throws IOException {
		ArrayList<String> result = new ArrayList<>();

		Document page = Jsoup.connect("http://www.soccerway.com/").timeout(0).get();

		HashMap<String, String> leagueDescriptions = EntryPoints.getTrackingLeagueDescriptions();
		Elements linksM = page.select("th.competition-link");
		for (Element i : linksM) {
			String href = i.childNode(1).attr("href");
			if (href.contains("national")) {
				href = href.substring(0, StringUtils.ordinalIndexOf(href, "/", 4) + 1);
				if (leagueDescriptions.keySet().contains(href))
					result.add(leagueDescriptions.get(href));
			}
		}

		return result;
	}

	public static void checkAndUpdate(String competition, OnlyTodayMatches onlyTodaysMatches)
			throws IOException, ParseException, InterruptedException {
		String base = new File("").getAbsolutePath();
		int collectYear = Arrays.asList(EntryPoints.SUMMER).contains(competition) ? EntryPoints.SUMMERCURRENT
				: EntryPoints.CURRENT;

		FileInputStream file = new FileInputStream(new File(base + "\\data\\odds" + collectYear + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sh = workbook.getSheet(competition);

		ArrayList<ExtendedFixture> all = XlSUtils.selectAll(sh, 0);
		// problem when no pendingma fixtures?
		Date oldestTocheck = Utils.findLastPendingFixture(all);
		System.out.println(oldestTocheck);

		ArrayList<ExtendedFixture> odds = oddsUpToDate(competition, collectYear, Utils.getYesterday(oldestTocheck),
				null);
		System.out.println(odds.size() + " odds ");

		ArrayList<ExtendedFixture> list = new ArrayList<>();
		int count = 0;
		int maxTries = 10;
		while (true) {
			try {
				list = collectUpToDate(competition, collectYear, Utils.getYesterday(oldestTocheck), null);
				break;
			} catch (Exception e) {
				if (++count == maxTries)
					throw e;
			}
		}

		System.out.println(list.size() + "shots");

		HashMap<String, String> dictionary = XlSUtils.deduceDictionary(odds, list);

		ArrayList<ExtendedFixture> combined = XlSUtils.combineWithDictionary(odds, list, competition, dictionary);
		System.out.println(combined.size() + " combined");
		System.out.println(
				competition + " " + (combined.size() == list.size() ? " combined successfull" : " combined failed"));
		workbook.close();

		ArrayList<ExtendedFixture> toAdd = new ArrayList<>();

		toAdd.addAll(combined);

		for (ExtendedFixture i : all) {
			boolean continueFlag = false;
			for (ExtendedFixture comb : combined) {
				if (i.homeTeam.equals(comb.homeTeam) && i.awayTeam.equals(comb.awayTeam)
						&& (i.date.equals(comb.date) || i.date.equals(Utils.getYesterday(comb.date))
								|| i.date.equals(Utils.getTommorow(comb.date)))) {
					continueFlag = true;
				}
			}
			if (!continueFlag)
				toAdd.add(i);
		}

		System.out.println("to add " + toAdd.size());
		ArrayList<ExtendedFixture> next = new ArrayList<>();
		// int countTries = 0;
		// int maxTriesNext = 5;
		// while (true) {
		// try {
		next = nextMatches(competition, null, onlyTodaysMatches);
		// break;
		// } catch (Exception e) {
		// if (++countTries == maxTriesNext)
		// throw e;
		// }
		// }

		ArrayList<ExtendedFixture> withNext = new ArrayList<>();

		for (ExtendedFixture i : toAdd) {
			boolean continueFlag = false;
			for (ExtendedFixture n : next) {
				if (i.homeTeam.equals(n.homeTeam) && i.awayTeam.equals(n.awayTeam) && (i.date.equals(n.date)
						|| i.date.equals(Utils.getYesterday(n.date)) || i.date.equals(Utils.getTommorow(n.date)))) {
					continueFlag = true;
					break;
				}
			}
			if (!continueFlag)
				withNext.add(i);
		}

		withNext.addAll(next);

		if (withNext.size() >= all.size()) {
			XlSUtils.storeInExcel(withNext, competition, CURRENT_YEAR, "odds");
			XlSUtils.fillMissingShotsData(competition, CURRENT_YEAR, false);
		}

		System.out.println(competition + " successfully updated");

	}

	private static ArrayList<ExtendedFixture> oddsUpToDate(String competition, int currentYear, Date yesterday,
			String add) throws InterruptedException {
		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, currentYear);
		} else
			address = add;
		System.out.println(address);

		Set<ExtendedFixture> result = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		// Get page count
		int maxPage = 1;
		List<WebElement> pages = driver.findElements(By.cssSelector("a[href*='#/page/']"));
		for (WebElement i : pages) {
			if (isNumeric(i.getText())) {
				if (Integer.parseInt(i.getText().trim()) > maxPage)
					maxPage = Integer.parseInt(i.getText().trim());
			}
		}

		boolean breakFlag = false;
		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				String[] splitAddress = address.split("/");
				String leagueYear = splitAddress[splitAddress.length - 1];
				List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
				ArrayList<String> links = new ArrayList<>();
				for (WebElement i : list) {
					// better logic here?
					Thread.sleep(100);
					if (i.getText().contains("-"))
						if (isFixtureLink(i.getAttribute("href")))
							links.add(i.getAttribute("href"));
				}

				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);

					if (ef != null) {
						if (ef.date.before(yesterday)) {
							breakFlag = true;
							break;
						}
						result.add(ef);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				driver.close();
				Thread.sleep(20000);
				driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();
			}

			if (breakFlag) {
				page = maxPage + 1;
				break;
			}

		}

		driver.close();

		ArrayList<ExtendedFixture> fin = new ArrayList<>();
		fin.addAll(result);
		return fin;
	}

	private static ArrayList<ExtendedFixture> collectUpToDate(String competition, int currentYear, Date yesterday,
			String add) throws IOException, ParseException, InterruptedException {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		Set<ExtendedFixture> set = new HashSet<>();
		String address;
		if (add == null) {
			address = EntryPoints.getLink(competition, currentYear);
			System.out.println(address);
		} else
			address = add;
		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address);

		// try {
		// WebDriverWait wait = new WebDriverWait(driver, 10);
		// wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hstp_14536_interstitial_pub"))).click();
		// System.out.println("Successfully closed efbet add");
		// } catch (Exception e) {
		// System.out.println("Problem closing efbet add");
		// }

		while (true) {
			int setSize = set.size();
			String html = driver.getPageSource();
			Document matches = Jsoup.parse(html);

			boolean breakFlag = false;
			Elements linksM = matches.select("a[href]");
			Elements fixtures = new Elements();
			for (Element linkM : linksM) {
				if (isScore(linkM.text())) {
					fixtures.add(linkM);
				}
			}

			for (int i = fixtures.size() - 1; i >= 0; i--) {
				Document fixture = Jsoup.connect(BASE + fixtures.get(i).attr("href")).get();
				ExtendedFixture ef = getFixture(fixture, competition);
				if (ef != null && ef.date.before(yesterday)) {
					breakFlag = true;
					break;
				}
				result.add(ef);
				set.add(ef);
			}

			if (breakFlag)
				break;

			driver.findElement(By.className("previous")).click();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;

			// Additional stopping condition - no new entries
			if (set.size() == setSize)
				break;

		}

		driver.close();

		if (result.size() != set.size())
			System.out.println("size problem of shots data");

		ArrayList<ExtendedFixture> setlist = new ArrayList<>();
		set.addAll(result);
		setlist.addAll(set);
		return setlist;
	}

	private static ArrayList<ExtendedFixture> nextMatches(String competition, Object object,
			OnlyTodayMatches onlyTodaysMatches) throws ParseException, InterruptedException, IOException {
		String address = EntryPoints.getOddsLink(competition, 2016);
		System.out.println(address);

		ArrayList<ExtendedFixture> result = new ArrayList<>();
		Set<String> teams = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address);

		driver.findElement(By.partialLinkText("GMT")).click();

		// Hardcoded timezone
		driver.findElement(By.xpath("//*[@id='timezone-content']/a[32]")).click();

		String[] splitAddress = address.split("/");
		String leagueYear = splitAddress[splitAddress.length - 1];
		List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
		ArrayList<String> links = new ArrayList<>();
		HashMap<String, String> texts = new HashMap<>();
		for (WebElement i : list) {
			if (i.getText().contains("-")) {
				String href = i.getAttribute("href");
				links.add(href);
				// System.out.println(i.getText());
				texts.put(href, i.getText());
			}
		}

		for (String i : links) {
			String homeTeam = texts.get(i).split("-")[0].trim();
			String awayTeam = texts.get(i).split("-")[1].trim();
			if (teams.contains(homeTeam) && teams.contains(awayTeam))
				continue;

			ExtendedFixture ef = getOddsFixture(driver, i, competition, true, onlyTodaysMatches);
			if (ef != null && ef.result.goalsHomeTeam == -1 && !teams.contains(ef.homeTeam)
					&& !teams.contains(ef.awayTeam)) {
				result.add(ef);
				teams.add(ef.awayTeam);
				teams.add(ef.homeTeam);
			}

			// break;

		}
		driver.close();

		System.out.println(result);
		return result;
	}

	public static ArrayList<ExtendedFixture> collect(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {
		ArrayList<ExtendedFixture> result = new ArrayList<>();
		Set<ExtendedFixture> set = new HashSet<>();
		String address;
		if (add == null) {
			address = EntryPoints.getLink(competition, year);
			System.out.println(address);
		} else
			address = add;

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address);

		// try {
		// WebDriverWait wait = new WebDriverWait(driver, 10);
		// wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hstp_14536_interstitial_pub"))).click();
		// System.out.println("Successfully closed efbet add");
		// } catch (Exception e) {
		// System.out.println("Problem closing efbet add");
		// }

		while (true) {
			String html = driver.getPageSource();
			Document matches = Jsoup.parse(html);
			int setSize = set.size();

			Elements linksM = matches.select("a[href]");
			for (Element linkM : linksM) {
				if (isScore(linkM.text())) {
					Document fixture = Jsoup.connect(BASE + linkM.attr("href")).get();
					ExtendedFixture ef = getFixture(fixture, competition);
					result.add(ef);
					set.add(ef);
				}
			}

			driver.findElement(By.className("previous")).click();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;

			// Additional stopping condition - no new entries
			if (set.size() == setSize)
				break;
		}

		driver.close();
		System.out.println(result.size());
		System.out.println(set.size());

		ArrayList<ExtendedFixture> setlist = new ArrayList<>();
		set.addAll(result);
		setlist.addAll(set);
		return setlist;

	}

	public static ArrayList<PlayerFixture> collectFull(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {
		ArrayList<PlayerFixture> result = new ArrayList<>();
		Set<PlayerFixture> set = new HashSet<>();
		String address;
		if (add == null) {
			address = EntryPoints.getLink(competition, year);
			System.out.println(address);
		} else
			address = add;

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address /* + "/matches/" */ );

		int fixtureCount = 0;
		while (true) {
			String html = driver.getPageSource();
			Document matches = Jsoup.parse(html);

			Elements linksM = matches.select("a[href]");
			for (Element linkM : linksM) {
				if (isScore(linkM.text())) {
					// Thread.sleep(50);
					// System.out.println(linkM.text());
					int count = 0;
					int maxTries = 10;
					while (true) {
						try {
							Document fixture = Jsoup.connect(BASE + linkM.attr("href")).timeout(30 * 1000).get();
							ArrayList<PlayerFixture> ef = getFixtureFull(fixture, competition);
							fixtureCount++;
							result.addAll(ef);
							break;
						} catch (Exception e) {
							if (++count == maxTries)
								throw e;
						}
					}
					// set.addAll(ef);
					// break;
				}
			}

			driver.findElement(By.className("previous")).click();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;

		}

		driver.close();
		System.out.println(fixtureCount + " fixtures");
		System.out.println(result.size());

		ArrayList<PlayerFixture> setlist = new ArrayList<>();
		// set.addAll(result);
		setlist = Utils.removeRepeats(result);
		System.out.println(setlist.size());
		// setlist.addAll(set);

		return setlist;

	}

	public static ExtendedFixture getFixture(Document fixture, String competition) throws IOException, ParseException {

		// System.out.println(fixture.select("dt:contains(Half-time) +
		// dd").first().text());
		Result ht = new Result(-1, -1);
		try {
			ht = getResult(fixture.select("dt:contains(Half-time) + dd").first().text());
		} catch (Exception e) {
			System.out.println("No ht result!");
		}
		// System.out.println(fixture.select("dt:contains(Full-time) +
		// dd").first().text());
		Result result = new Result(-1, -1);
		try {
			result = getResult(fixture.select("dt:contains(Full-time) + dd").first().text());
		} catch (Exception e) {
			System.out.println("No full time result!");
			return null;
		}
		// System.out.println(fixture.select("dt:contains(Date) +
		// dd").first().text());
		// System.out.println(fixture.select("dt:contains(Game week) +
		// dd").first().text());
		int matchday = -1;
		try {
			matchday = Integer.parseInt(fixture.select("dt:contains(Game week) + dd").first().text());
		} catch (Exception e) {

		}

		String iframe;

		int shotsHome = -1, shotsAway = -1;

		Elements frames = fixture.select("iframe");
		for (Element i : frames) {
			if (i.attr("src").contains("/charts/statsplus")) {
				Document stats = Jsoup.connect(BASE + i.attr("src")).get();
				try {
					shotsHome = Integer.parseInt(
							stats.select("tr:contains(Shots on target)").get(1).select("td.legend.left.value").text());

					shotsAway = Integer.parseInt(
							stats.select("tr:contains(Shots on target)").get(1).select("td.legend.right.value").text());
				} catch (Exception exp) {
				}
				break;
			}
		}

		System.out.println(shotsHome + " s " + shotsAway);

		Date date = OPTAFORMAT.parse(fixture.select("dt:contains(Date) + dd").first().text());
		// System.out.println(date);

		String teams = fixture.select("h1").first().text();
		// System.out.println(getHome(teams));
		// System.out.println(getAway(teams));
		String homeTeam = Utils.replaceNonAsciiWhitespace(getHome(teams));
		String awayTeam = Utils.replaceNonAsciiWhitespace(getAway(teams));

		ExtendedFixture ef = new ExtendedFixture(date, homeTeam, awayTeam, result, "BRA").withHTResult(ht)
				.withShots(shotsHome, shotsAway);
		if (matchday != -1)
			ef = ef.withMatchday(matchday);
		System.out.println(ef);

		return ef;
	}

	public static ArrayList<PlayerFixture> getFixtureFull(Document fixture, String competition)
			throws IOException, ParseException {
		boolean verbose = false;

		Result ht = new Result(-1, -1);
		try {
			ht = getResult(fixture.select("dt:contains(Half-time) + dd").first().text());
		} catch (Exception e) {
			System.out.println("No ht result!");
		}

		Result result = new Result(-1, -1);
		try {
			result = getResult(fixture.select("dt:contains(Full-time) + dd").first().text());
		} catch (Exception e) {
			System.out.println("No full time result!");
			return null;
		}

		int matchday = -1;
		try {
			matchday = Integer.parseInt(fixture.select("dt:contains(Game week) + dd").first().text());
		} catch (Exception e) {

		}

		Date date = OPTAFORMAT.parse(fixture.select("dt:contains(Date) + dd").first().text());
		// System.out.println(date);

		String teams = fixture.select("h1").first().text();
		String homeTeam = Utils.replaceNonAsciiWhitespace(getHome(teams));
		String awayTeam = Utils.replaceNonAsciiWhitespace(getAway(teams));

		// Lineups
		// =====================================================================
		ExtendedFixture fix = new ExtendedFixture(date, homeTeam, awayTeam, result, competition);
		System.out.println(fix);
		// if (homeTeam.equals("Freiburg") && awayTeam.equals("Kln"))
		// System.out.println("dadsa");

		ArrayList<PlayerFixture> playerFixtures = new ArrayList<>();

		Element divLineups = fixture.getElementsByClass("combined-lineups-container").first();
		if (divLineups != null) {
			Element tableHome = divLineups.select("div.container.left").first();
			Element tableAway = divLineups.select("div.container.right").first();

			Elements rowsHome = tableHome.select("table").first().select("tr");
			for (int i = 1; i < rowsHome.size(); i++) {// without coach
				Element row = rowsHome.get(i);
				if (row.text().contains("Coach") || row.text().contains("coach") || row.text().isEmpty())
					continue;

				Elements cols = row.select("td");
				if (cols.size() < 2)
					continue;
				// String shirtNumber = cols.get(0).text();
				String name = "";
				try {
					name = Utils.replaceNonAsciiWhitespace(cols.get(cols.size() == 2 ? 0 : 1).text());
				} catch (Exception e) {
					System.out.println("Empty column when parsing startin 11");
					continue;
				}
				// System.out.println(shirtNumber + " " + name);
				PlayerFixture pf = new PlayerFixture(fix, homeTeam, name, 90, true, false, 0, 0);
				playerFixtures.add(pf);

			}

			Elements rowsAway = tableAway.select("table").first().select("tr");
			for (int i = 1; i < rowsAway.size(); i++) {
				Element row = rowsAway.get(i);
				if (row.text().contains("Coach") || row.text().contains("coach") || row.text().isEmpty())
					continue;

				Elements cols = row.select("td");
				if (cols.size() < 2)
					continue;
				// String shirtNumber = cols.get(0).text();
				String name = Utils.replaceNonAsciiWhitespace(cols.get(cols.size() == 2 ? 0 : 1).text());
				// System.out.println(shirtNumber + " " + name);
				PlayerFixture pf = new PlayerFixture(fix, awayTeam, name, 90, true, false, 0, 0);
				playerFixtures.add(pf);
			}

		}

		// Substitutes
		// ==========================================================

		Elements divsPlayers = fixture.getElementsByClass("combined-lineups-container");
		Element divSubstitutes  = divsPlayers.size() > 1 ? divsPlayers.get(1) :  null; 
		if (divSubstitutes != null) {
			Element tableHome = divSubstitutes.select("div.container.left").first();
			Element tableAway = divSubstitutes.select("div.container.right").first();

			Elements rowsHome = tableHome.select("table").first().select("tr");
			for (int i = 1; i < rowsHome.size(); i++) {// without coach
														// information
				Element row = rowsHome.get(i);
				Elements cols = row.select("td");
				String shirtNumber = cols.get(0).text();
				String name = Utils.replaceNonAsciiWhitespace(cols.get(cols.size() == 2 ? 0 : 1).text());
				if (name.contains(" for ")) {
					String inPlayer = name.split(" for ")[0].trim();
					String outPlayer = "";
					try {
						outPlayer = name.split(" for ")[1].split("[0-9]+'")[0].trim();
					} catch (Exception e) {
						System.err.println("da ddassd");
					}
					int minute = 0;
					try {
						String cleanMinutes = name.split(" ")[name.split(" ").length - 1].split("'")[0];
						if (cleanMinutes.contains("+"))
							minute = Integer.parseInt(cleanMinutes.split("\\+")[0])
									+ Integer.parseInt(cleanMinutes.split("\\+")[1]);
						else
							minute = Integer.parseInt(cleanMinutes);
					} catch (Exception e) {
						System.out.println("parse");
					}
					PlayerFixture pf = new PlayerFixture(fix, homeTeam, inPlayer, 90 - minute, false, true, 0, 0);
					playerFixtures.add(pf);

					for (PlayerFixture player : playerFixtures) {
						if (player.name.equals(outPlayer)) {
							player.minutesPlayed = minute;
							break;
						}
					}

					if (verbose)
						System.out.println(inPlayer + " for " + outPlayer + " in " + minute);

				} else {
					PlayerFixture pf = new PlayerFixture(fix, homeTeam, name, 0, false, false, 0, 0);
					playerFixtures.add(pf);
					if (verbose)
						System.out.println(shirtNumber + " " + name);
				}

			}

			Elements rowsAway = tableAway.select("table").first().select("tr");
			for (int i = 1; i < rowsAway.size(); i++) {
				Element row = rowsAway.get(i);
				Elements cols = row.select("td");
				String shirtNumber = cols.get(0).text();
				String name = Utils.replaceNonAsciiWhitespace(cols.get(cols.size() == 2 ? 0 : 1).text());
				if (name.contains(" for ")) {
					String inPlayer = name.split(" for ")[0].trim();
					String outPlayer = name.split(" for ")[1].split("[0-9]+'")[0].trim();
					int minute = 0;
					try {
						String cleanMinutes = name.split(" ")[name.split(" ").length - 1].split("'")[0];
						if (cleanMinutes.contains("+"))
							minute = Integer.parseInt(cleanMinutes.split("\\+")[0])
									+ Integer.parseInt(cleanMinutes.split("\\+")[1]);
						else
							minute = Integer.parseInt(cleanMinutes);
					} catch (Exception e) {
						System.out.println("parse");
					}

					PlayerFixture pf = new PlayerFixture(fix, awayTeam, inPlayer, 90 - minute, false, true, 0, 0);
					playerFixtures.add(pf);

					for (PlayerFixture player : playerFixtures) {
						if (player.name.equals(outPlayer)) {
							player.minutesPlayed = minute;
							break;
						}
					}
					if (verbose)
						System.out.println(inPlayer + " for " + outPlayer + " in " + minute);

				} else {
					PlayerFixture pf = new PlayerFixture(fix, awayTeam, name, 0, false, false, 0, 0);
					playerFixtures.add(pf);
					if (verbose)
						System.out.println(shirtNumber + " " + name);
				}

			}

		}

		// Goals and assists
		// ========================================================================================

		Element divGoals = fixture.select("div[id*=match_goals").first();
		if (divGoals != null) {
			Element table = divGoals.select("div.content").first().select("table").first();
			Elements rows = table.select("table").first().select("tr");
			for (int i = 0; i < rows.size(); i++) {
				Element row = rows.get(i);
				if (verbose)
					System.out.println(row.text());
				Elements cols = row.select("td");
				for (Element j : cols) {
					if (!isScore(j.text()) && !j.text().isEmpty()) {
						// String[] splitted = j.text().split("-");
						// Result curr = new
						// Result(Integer.parseInt(splitted[0].trim()),
						// Integer.parseInt(splitted[1].trim()));
						// System.out.println(result);
						if (verbose)
							System.out.println(j.text());

						String[] splitByMinute = j.text().split("[0-9]+'");
						if (verbose)
							System.out.println(splitByMinute.length);
						if (!splitByMinute[0].isEmpty()) {
							// Home goal

							String goalScorer = splitByMinute[0].trim();
							goalScorer = Utils.replaceNonAsciiWhitespace(goalScorer).trim();
							if (goalScorer.contains("(PG)")) {
								goalScorer = goalScorer.replace("(PG)", "").trim();
							}

							if (!goalScorer.contains("(OG)"))
								updatePlayer(goalScorer, playerFixtures, true);

							if (splitByMinute.length > 1) {
								// Extra info like assistedS by, PG or OG
								String extraString = splitByMinute[1];

								if (extraString.contains("assist by")) {
									String assister = splitByMinute[1].split("\\(assist by ")[1].trim();
									assister = Utils.replaceNonAsciiWhitespace(assister);
									assister = assister.substring(0, assister.length() - 1).trim();
									updatePlayer(assister, playerFixtures, false);

								}
							}
						} else {
							// Away goal
							if (splitByMinute[1].contains("(PG)")) {
								String goalScorer = splitByMinute[1].replace("(PG)", "").trim();
								if (goalScorer.contains("+")) {// scored i
																// additional
																// time
									goalScorer = goalScorer.replace("+", "").replaceAll("\\d", "").trim();
								}

								goalScorer = Utils.replaceNonAsciiWhitespace(goalScorer).trim();

								if (goalScorer.contains("assist by")) {
									String assister = goalScorer.split("\\(assist by ")[1].trim();
									assister = Utils.replaceNonAsciiWhitespace(assister);
									assister = assister.substring(0, assister.length() - 1);
									updatePlayer(assister, playerFixtures, false);
									goalScorer = goalScorer.split("\\(assist by ")[0].trim();
								}
								updatePlayer(goalScorer, playerFixtures, true);
							} else if (splitByMinute[1].contains("assist by")) {
								String goalScorer = splitByMinute[1].split("\\(assist by ")[0].trim();
								goalScorer = Utils.replaceNonAsciiWhitespace(goalScorer).trim();
								if (goalScorer.contains("+")) {// scored in
									goalScorer = goalScorer.replace("+", "").replaceAll("\\d", "").trim();
								}

								updatePlayer(goalScorer, playerFixtures, true);

								String assister = splitByMinute[1].split("\\(assist by ")[1].trim();
								assister = Utils.replaceNonAsciiWhitespace(assister);
								assister = assister.substring(0, assister.length() - 1).trim();
								updatePlayer(assister, playerFixtures, false);
							} else if (!splitByMinute[1].contains("(OG)")) {
								// Solo goal no assists, no PG,no OG
								String goalScorer = Utils.replaceNonAsciiWhitespace(splitByMinute[1].trim()).trim();
								if (goalScorer.contains("+")) {// scored in
									// additional
									// time
									goalScorer = goalScorer.replace("+", "").replaceAll("\\d", "").trim();
								}
								updatePlayer(goalScorer, playerFixtures, true);
							}
						}
					}
				}
			}
		}

		// String iframe;
		//
		// int shotsHome = -1, shotsAway = -1;
		//
		// Elements frames = fixture.select("iframe");
		// for (Element i : frames) {
		// if (i.attr("src").contains("/charts/statsplus")) {
		// Document stats = Jsoup.connect(BASE +
		// i.attr("src")).timeout(0).get();
		// try {
		// shotsHome = Integer.parseInt(
		// stats.select("tr:contains(Shots on
		// target)").get(1).select("td.legend.left.value").text());
		//
		// shotsAway = Integer.parseInt(
		// stats.select("tr:contains(Shots on
		// target)").get(1).select("td.legend.right.value").text());
		// } catch (Exception exp) {
		// }
		// break;
		// }
		// }
		//
		// System.out.println(shotsHome + " s " + shotsAway);
		//
		// ExtendedFixture ef = new ExtendedFixture(date, homeTeam, awayTeam,
		// result, "BRA").withHTResult(ht)
		// .withShots(shotsHome, shotsAway);
		// if (matchday != -1)
		// ef = ef.withMatchday(matchday);
		// System.out.println(ef);

		return playerFixtures;

	}

	/**
	 * Updates the goals or assists (by 1) of the player in the playerFixtures
	 * collection
	 * 
	 * @param name
	 * @param playerFixtures
	 * @param goals
	 *            true if updating goals, false if updating assists
	 */

	private static void updatePlayer(String name, ArrayList<PlayerFixture> playerFixtures, boolean goals) {
		boolean updated = false;
		for (PlayerFixture player : playerFixtures) {
			if (player.name.equals(name)) {
				if (goals)
					player.goals++;
				else
					player.assists++;
				updated = true;
				break;
			}
		}
		// Utils.printPlayers(playerFixtures);

		if (!updated)
			System.err.println("Problem in updating " + (goals ? "goals " : "assists ") + "for " + name);

	}

	public static ArrayList<ExtendedFixture> odds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
		} else
			address = add;
		System.out.println(address);

		Set<ExtendedFixture> result = new HashSet<>();

		// System.setProperty("webdriver.chrome.drive",
		// "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new /* HtmlUnitDriver(); */ ChromeDriver();
		// ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		// driver.findElement(By.partialLinkText("GMT")).click();

		// Hardcoded timezone
		// driver.findElement(By.xpath("//*[@id='timezone-content']/a[32]")).click();

		// *[@id="pagination"]

		// Get page count
		int maxPage = 1;
		try {
			WebElement pagin = driver.findElement(By.xpath("//*[@id='pagination']"));
			List<WebElement> spans = pagin.findElements(By.tagName("span"));
			for (WebElement i : spans) {
				if (isNumeric(i.getText())) {
					if (Integer.parseInt(i.getText().trim()) > maxPage)
						maxPage = Integer.parseInt(i.getText().trim());
				}
			}
		} catch (Exception e) {

		}
		//

		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				String[] splitAddress = address.split("/");
				String leagueYear = splitAddress[splitAddress.length - 1];
				List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
				ArrayList<String> links = new ArrayList<>();
				for (WebElement i : list) {
					// better logic here?
					String href = i.getAttribute("href");
					System.out.println(href);
					if (i.getText().contains("-") && isFixtureLink(href)) {
						links.add(href);

					}
				}

				System.out.println(links);
				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);
					if (ef != null)
						result.add(ef);

					// break;

				}
			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				driver.close();
				Thread.sleep(30000);
				driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();
			}
		}

		driver.close();

		ArrayList<ExtendedFixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	public static ArrayList<FullFixture> fullOdds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
		} else
			address = add;
		System.out.println(address);

		Set<FullFixture> result = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		// driver.findElement(By.partialLinkText("GMT")).click();

		// Hardcoded timezone
		// driver.findElement(By.xpath("//*[@id='timezone-content']/a[32]")).click();

		// *[@id="pagination"]

		// Get page count
		int maxPage = 1;
		try {
			WebElement pagin = driver.findElement(By.xpath("//*[@id='pagination']"));
			List<WebElement> spans = pagin.findElements(By.tagName("span"));
			for (WebElement i : spans) {
				if (isNumeric(i.getText())) {
					if (Integer.parseInt(i.getText().trim()) > maxPage)
						maxPage = Integer.parseInt(i.getText().trim());
				}
			}
		} catch (Exception e) {

		}

		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				String[] splitAddress = address.split("/");
				String leagueYear = splitAddress[splitAddress.length - 1];
				List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
				ArrayList<String> links = new ArrayList<>();
				for (WebElement i : list) {
					// better logic here?
					if (i.getText().contains("-") && isFixtureLink(i.getAttribute("href")))
						links.add(i.getAttribute("href"));
				}

				System.out.println(links);
				for (String i : links) {
					FullFixture ef = getFullFixture(driver, i, competition);
					if (ef != null)
						result.add(ef);

					// break;

				}
			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				driver.close();
				Thread.sleep(15000);
				driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();
			}
		}

		driver.close();

		ArrayList<FullFixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	public static ArrayList<ExtendedFixture> oddsByPage(String competition, int year, String add, int page) {
		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
		} else
			address = add;
		System.out.println(address);

		Set<ExtendedFixture> result = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		while (true) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				String[] splitAddress = address.split("/");
				String leagueYear = splitAddress[splitAddress.length - 1];
				List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
				ArrayList<String> links = new ArrayList<>();
				for (WebElement i : list) {
					// better logic here?
					if (i.getText().contains("-") && isFixtureLink(i.getAttribute("href")))
						links.add(i.getAttribute("href"));
				}

				// System.out.println(links);
				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);
					if (ef != null)
						result.add(ef);

					// break;

				}
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Starting over from page:" + page);
				driver.close();
				try {
					Thread.sleep(10000);
				} catch (Exception e2) {
					e2.printStackTrace();
					System.out.println("Thread sleep problem");
				}
				driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();
			}
		}

		driver.close();

		ArrayList<ExtendedFixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println("Thread at page " + page + "finished successfuly with " + fin.size());
		return fin;

	}

	public static ArrayList<ExtendedFixture> oddsInParallel(String competition, int year, String add)
			throws InterruptedException, ExecutionException {
		ArrayList<ExtendedFixture> result = new ArrayList<>();

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
		} else
			address = add;
		System.out.println(address);

		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		// Get page count
		int maxPage = 1;
		try {
			WebElement pagin = driver.findElement(By.xpath("//*[@id='pagination']"));
			List<WebElement> spans = pagin.findElements(By.tagName("span"));
			for (WebElement i : spans) {
				if (isNumeric(i.getText())) {
					if (Integer.parseInt(i.getText().trim()) > maxPage)
						maxPage = Integer.parseInt(i.getText().trim());
				}
			}
		} catch (Exception e) {

		}

		driver.close();

		ExecutorService pool = Executors.newFixedThreadPool(2);
		ArrayList<Future<ArrayList<ExtendedFixture>>> threadArray = new ArrayList<Future<ArrayList<ExtendedFixture>>>();
		for (int i = 1; i <= maxPage; i++) {
			threadArray.add(pool.submit(new RunnerOdds(competition, year, add, i)));
		}

		for (Future<ArrayList<ExtendedFixture>> fd : threadArray) {
			result.addAll(fd.get());
		}

		pool.shutdown();

		System.out.println("Final odds size " + result.size());
		return result;
	}

	public static ArrayList<ExtendedFixture> fastOdds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
		} else
			address = add;
		System.out.println(address);

		Set<ExtendedFixture> result = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		// Get page count
		int maxPage = 1;
		List<WebElement> pages = driver.findElements(By.cssSelector("a[href*='#/page/']"));
		for (WebElement i : pages) {
			if (isNumeric(i.getText())) {
				if (Integer.parseInt(i.getText().trim()) > maxPage)
					maxPage = Integer.parseInt(i.getText().trim());
			}
		}

		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				String[] splitAddress = address.split("/");
				String leagueYear = splitAddress[splitAddress.length - 1];
				List<WebElement> list = driver.findElements(By.cssSelector("a[href*='" + leagueYear + "']"));
				ArrayList<String> links = new ArrayList<>();
				for (WebElement i : list) {
					// better logic here?
					if (i.getText().contains("-") && isFixtureLink(i.getAttribute("href")))
						links.add(i.getAttribute("href"));
				}

				System.out.println(links);
				for (String i : links) {
					ExtendedFixture ef = getFastOddsFixture(driver, i, competition);
					if (ef != null)
						result.add(ef);

					break;

				}
			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				driver.close();
				Thread.sleep(30000);
				driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();
			}
		}

		driver.close();

		ArrayList<ExtendedFixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	private static boolean isFixtureLink(String attribute) {
		// TODO Auto-generated method stub

		// http://www.oddsportal.com/soccer/japan/j-league-2015/hiroshima-g-osaka-EufnwCdk/
		String[] split = attribute.split("/");
		String fixturePart = split[split.length - 1];
		String[] split2 = fixturePart.split("-");
		if (split2.length < 3)
			return false;
		else {
			if (split2[split2.length - 1].length() == 8)
				return true;
			else
				return false;
		}

	}

	/**
	 * Gets fixture with odds data
	 * 
	 * @param driver
	 * @param i
	 *            - link to the fixture
	 * @param competition
	 * @param liveMatchesFlag
	 *            - flag for matches that are currently live - true in this case
	 * @param onlyToday
	 *            - true if we want to update only todays matches (to be played)
	 * @return
	 * @throws ParseException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static ExtendedFixture getOddsFixture(WebDriver driver, String i, String competition,
			boolean liveMatchesFlag, OnlyTodayMatches onlyToday)
					throws ParseException, InterruptedException, IOException {
		// System.out.println(i);
		int count = 0;
		int maxTries = 10;
		while (true) {
			try {
				driver.navigate().to(i);
				break;
			} catch (Exception e) {
				if (++count == maxTries)
					throw e;
			}
		}

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();
		System.out.println(home + " : " + away);

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		Date date = OPTAFORMAT.parse(dateString.split(",")[1].trim());
		System.out.println(date);

		// skipping to update matches that are played later than today (for
		// performance in nextMatches())
		if (onlyToday.equals(OnlyTodayMatches.TRUE) && !Utils.isToday(date))
			return null;

		// --------------------------
		// Document fixture = Jsoup.connect(i).get();
		//
		//
		// Element dt = fixture.select("p[class^=date]").first();
		//
		// String millisString = dt.outerHtml().split("
		// ")[3].split("-")[0].substring(1);
		//
		// long timeInMillis = Long.parseLong(millisString) * 1000;
		//
		// Calendar cal = Calendar.getInstance();
		// cal.setTimeInMillis(timeInMillis);
		// Date date2 = cal.getTime();
		// System.out.println(date2);
		// date=date2;
		// -----------------------------------------------------------

		// Resultss
		Result fullResult = new Result(-1, -1);
		Result htResult = new Result(-1, -1);
		try {
			WebElement resElement = driver.findElement(By.xpath("//*[@id='event-status']/p"));
			if (resElement != null) {
				String resString = resElement.getText();
				if (resString.contains("penalties") || resString.contains("ET") || resString.contains("Postponed")) {
					return null;
				}

				if ((!liveMatchesFlag && resString.contains("already started")) || resString.contains("Abandoned")) {
					return null;
				}
				if (resString.contains("awarded") && resString.contains(home)) {
					fullResult = new Result(3, 0);
					htResult = new Result(3, 0);
				} else if (resString.contains("awarded") && resString.contains(away)) {
					fullResult = new Result(0, 3);
					htResult = new Result(0, 3);
				} else if (resString.contains("(") && resString.contains(")")) {
					String full = resString.split(" ")[2];
					String half = resString.split(" ")[3].substring(1, 4);

					fullResult = new Result(Integer.parseInt(full.split(":")[0]), Integer.parseInt(full.split(":")[1]));
					htResult = new Result(Integer.parseInt(half.split(":")[0]), Integer.parseInt(half.split(":")[1]));
				} else {
					fullResult = new Result(-1, -1);
					htResult = new Result(-1, -1);
				}
			}

		} catch (Exception e) {
			System.out.println("next match");
		}
		// System.out.println(fullResult + " " + htResult);

		WebElement table = driver.findElement(By.xpath("//div[@id='odds-data-table']"));
		// find the row
		List<WebElement> customer = table.findElements(By.xpath("//div[1]/table/tbody/tr"));
		int pinnIndex = -2;
		int Index365 = -2;

		for (WebElement row : customer) {
			if (row.getText().contains("Pinnacle"))
				pinnIndex = customer.indexOf(row) + 1;
			if (row.getText().contains("bet365"))
				pinnIndex = customer.indexOf(row) + 1;
		}
		if (pinnIndex < 0) {
			System.out.println("Could not find pinnacle");
			pinnIndex = Index365;
			if (pinnIndex < 0)
				pinnIndex = 1;
		}

		float homeOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[2]")).getText());
		float drawOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[3]")).getText());
		float awayOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[4]")).getText());

		// System.out.println(homeOdds);
		// System.out.println(drawOdds);
		// System.out.println(awayOdds);

		// Over and under odds
		float overOdds = -1f, underOdds = -1f;
		float overOdds365 = -1f, underOdds365 = -1f;
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs) {
			if (t.getText().contains("O/U")) {
				t.click();
				break;
			}
		}

		WebElement div25 = null;
		List<WebElement> divs = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divs) {
			if (div.getText().contains("+2.5")) {
				// System.out.println(div.getText());
				div25 = div;
				div.click();
				break;
			}
		}

		if (div25 != null) {
			WebElement OUTable = div25.findElement(By.xpath("//table"));

			// find the row
			List<WebElement> rows = OUTable.findElements(By.xpath("//tr"));

			for (WebElement row : rows) {
				if (row.getText().contains("Pinnacle")) {
					String textOdds = row.getText();
					// Actions ToolTip1 = new Actions(driver);
					// WebElement overElement =
					// row.findElement(By.cssSelector("td:nth-child(3) > div"));
					// WebElement underElement =
					// row.findElement(By.cssSelector("td:nth-child(4) > div"));
					// Thread.sleep(200);
					//
					// ToolTip1.clickAndHold(overElement).perform();
					//
					// WebElement hover =
					// overElement.findElement(By.xpath("//*[@id='tooltiptext']"));
					// String openingOdds = hover.getText().split("Opening
					// odds:")[1];
					// Float openOddsOver = Float.parseFloat(openingOdds.split("
					// ")[openingOdds.split(" ").length - 1]);
					//
					// ToolTip1.clickAndHold(underElement).perform();
					// WebElement hoverUnder =
					// overElement.findElement(By.xpath("//*[@id='tooltiptext']"));
					// String openingOddsUnder =
					// hoverUnder.getText().split("Opening odds:")[1];
					// Float openOddsUnder = Float
					// .parseFloat(openingOddsUnder.split("
					// ")[openingOddsUnder.split(" ").length - 1]);
					//
					// System.out.println(openOddsOver + " " + openOddsUnder);

					overOdds = Float.parseFloat(textOdds.split("\n")[2].trim())
					/* openOddsOver */;
					underOdds = Float.parseFloat(textOdds.split("\n")[3].trim())
					/* openOddsUnder */;
					break;
				}

				if (row.getText().contains("bet365")) {
					String textOdds = row.getText();
					try {
						overOdds365 = Float.parseFloat(textOdds.split("\n")[2].trim());
						underOdds365 = Float.parseFloat(textOdds.split("\n")[3].trim());
					} catch (Exception e) {
						// nothing
					}
				}
			}

			if (overOdds == -1f) {
				overOdds = overOdds365;
				underOdds = underOdds365;
			}

		}

		// System.out.println("over: " + overOdds + " " + underOdds);

		// Asian handicap
		for (WebElement t : tabs) {
			if (t.getText().contains("AH")) {
				t.click();
				break;
			}
		}

		// Asian with closest line
		WebElement opt = null;
		float min = 100f;
		List<WebElement> divsAsian = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divsAsian) {
			String text = div.getText();
			if (text.split("\n").length > 3) {
				try {
					float diff = Math.abs(Float.parseFloat(text.split("\n")[2].trim())
							- Float.parseFloat(text.split("\n")[3].trim()));
					if (diff < min) {
						min = diff;
						opt = div;
					}
				} catch (Exception e) {
					System.out.println("asian problem" + home + " " + away);
				}
			}
		}

		float line = -1f, asianHome = -1f, asianAway = -1f;
		// if (home.equals("Sport Recife") && away.equals("Corinthians")){
		// System.out.println(min);
		// System.out.println(opt.getText());
		// }

		if (opt != null) {
			opt.click();

			WebElement AHTable = opt.findElement(By.xpath("//table"));

			// find the row
			List<WebElement> rowsAsian = AHTable.findElements(By.xpath("//tr"));

			for (WebElement row : rowsAsian) {
				if (row.getText().contains("Pinnacle")) {
					String textOdds = row.getText();
					line = Float.parseFloat(textOdds.split("\n")[1].trim());
					asianHome = Float.parseFloat(textOdds.split("\n")[2].trim());
					asianAway = Float.parseFloat(textOdds.split("\n")[3].trim());
				}
			}

			// System.out.println(line + " " + asianHome + " " + asianAway);

		}

		ExtendedFixture ef = new ExtendedFixture(date, home, away, fullResult, competition).withHTResult(htResult)
				.with1X2Odds(homeOdds, drawOdds, awayOdds).withAsian(line, asianHome, asianAway)
				.withOdds(overOdds, underOdds, overOdds, underOdds).withShots(-1, -1);
		return ef;
	}

	public static FullFixture getFullFixture(WebDriver driver, String i, String competition)
			throws ParseException, InterruptedException {
		// System.out.println(i);

		driver.navigate().to(i);

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();
		System.out.println(home + " : " + away);

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		Date date = OPTAFORMAT.parse(dateString.split(",")[1].trim());
		System.out.println(date);

		// Result
		Result fullResult = new Result(-1, -1);
		Result htResult = new Result(-1, -1);
		try {
			WebElement resElement = driver.findElement(By.xpath("//*[@id='event-status']/p"));
			if (resElement != null) {
				String resString = resElement.getText();
				if (resString.contains("penalties") || resString.contains("ET")) {
					return null;
				}
				if (resString.contains("awarded") && resString.contains(home)) {
					fullResult = new Result(3, 0);
					htResult = new Result(3, 0);
				} else if (resString.contains("awarded") && resString.contains(away)) {
					fullResult = new Result(0, 3);
					htResult = new Result(0, 3);
				} else if (resString.contains("(") && resString.contains(")")) {
					String full = resString.split(" ")[2];
					String half = resString.split(" ")[3].substring(1, 4);

					fullResult = new Result(Integer.parseInt(full.split(":")[0]), Integer.parseInt(full.split(":")[1]));
					htResult = new Result(Integer.parseInt(half.split(":")[0]), Integer.parseInt(half.split(":")[1]));
				} else {
					fullResult = new Result(-1, -1);
					htResult = new Result(-1, -1);
				}
			}

		} catch (Exception e) {
			System.out.println("next match");
		}
		// System.out.println(fullResult + " " + htResult);

		WebElement table = driver.findElement(By.xpath("//div[@id='odds-data-table']"));
		// find the row
		List<WebElement> customer = table.findElements(By.xpath("//div[1]/table/tbody/tr"));
		int pinnIndex = -2;
		int Index365 = -2;

		for (WebElement row : customer) {
			if (row.getText().contains("Pinnacle"))
				pinnIndex = customer.indexOf(row) + 1;
			if (row.getText().contains("bet365"))
				pinnIndex = customer.indexOf(row) + 1;
		}
		if (pinnIndex < 0) {
			System.out.println("Could not find pinnacle");
			pinnIndex = Index365;
			if (pinnIndex < 0)
				pinnIndex = 1;
		}

		float homeOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[2]")).getText());
		float drawOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[3]")).getText());
		float awayOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[4]")).getText());

		// System.out.println(homeOdds);
		// System.out.println(drawOdds);
		// System.out.println(awayOdds);

		// Over and under odds
		float overOdds = -1f, underOdds = -1f;
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs) {
			if (t.getText().contains("O/U")) {
				t.click();
				break;
			}
		}

		WebElement div25 = null;
		main.Line twoAndHalf = null;

		main.Line def = new Line(-1f, -1f, -1f, "Pinn");
		GoalLines GLS = new GoalLines(def, def, def, def, def);

		WebElement optGoals = null;
		float minGoals = 100f;
		List<WebElement> divsGoals = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divsGoals) {
			String text = div.getText();
			if (div.getText().contains("+2.5")) {
				div25 = div;
			}
			if (text.split("\n").length > 3) {
				try {
					float diff = Math.abs(Float.parseFloat(text.split("\n")[2].trim())
							- Float.parseFloat(text.split("\n")[3].trim()));
					if (diff < minGoals) {
						minGoals = diff;
						optGoals = div;
					}
				} catch (Exception e) {
					// System.out.println("asian problem" + home + " " + away);
				}
			}
		}

		int indexOfOptimalGoals = optGoals == null ? -1 : divsGoals.indexOf(optGoals);

		ArrayList<main.Line> goalLines = new ArrayList<>();

		if (optGoals != null) {
			int lower = (indexOfOptimalGoals - 6) < 0 ? 0 : (indexOfOptimalGoals - 6);
			int higher = (indexOfOptimalGoals + 6) > (divsGoals.size() - 1) ? (divsGoals.size() - 1)
					: (indexOfOptimalGoals + 6);

			long startt = System.currentTimeMillis();
			for (int j = lower; j <= higher; j++) {
				WebElement currentDiv = divsGoals.get(j);
				if (currentDiv == null || currentDiv.getText().split("\n").length < 3)
					continue;

				currentDiv.click();
				WebElement goalLineTable = currentDiv.findElement(By.xpath("//table"));

				// find the row
				List<WebElement> rowsGoals = goalLineTable.findElements(By.xpath("//tbody/tr"));
				float line = -1f, over = -1f, under = -1f;

				// System.out.println(rowsGoals.size());
				for (int r = rowsGoals.size() - 1; r >= 0; r--) {
					WebElement row = rowsGoals.get(r);
					if (row.getText().contains("Pinnacle")) {
						// System.out.println(rowsGoals.indexOf(row));
						String textOdds = row.getText();
						line = Float.parseFloat(textOdds.split("\n")[1].trim());
						over = Float.parseFloat(textOdds.split("\n")[2].trim());
						under = Float.parseFloat(textOdds.split("\n")[3].trim());
						break;
					}

					if (row.getText().contains("Average"))
						break;
				}

				if (over != -1f) {
					goalLines.add(new main.Line(line, over, under, "Pinn"));
					if (line == 2.5)
						twoAndHalf = new main.Line(line, over, under, "Pinn");
				}

				List<WebElement> closeLink = currentDiv.findElements(By.className("odds-co"));
				if (!closeLink.isEmpty())
					closeLink.get(0).click();

				if (goalLines.size() == 6)
					break;
			}
			// System.out.println("one click cycle " +
			// (System.currentTimeMillis() - startt) / 1000d + "sec");
			System.out.println(goalLines);

			int indexMinDiff = -1;
			float minDiff = 100f;
			for (int l = 0; l < goalLines.size(); l++) {
				float diff = Math.abs(goalLines.get(l).home - goalLines.get(l).away);
				if (diff < minDiff) {
					minDiff = diff;
					indexMinDiff = l;
				}
			}

			int start = indexMinDiff - 2 < 0 ? 0 : indexMinDiff - 2;
			int end = indexMinDiff + 2 > goalLines.size() - 1 ? goalLines.size() - 1 : indexMinDiff + 2;

			int expectedCaseSize = end - start + 1;
			if (goalLines.size() == 5) {
				GLS = new GoalLines(goalLines.get(0), goalLines.get(1), goalLines.get(2), goalLines.get(3),
						goalLines.get(4));
			} else if (goalLines.size() > 5) {
				if (expectedCaseSize == 5) {
					ArrayList<main.Line> bestLines = new ArrayList<>();
					for (int c = start; c <= end; c++) {
						bestLines.add(goalLines.get(c));
					}

					GLS = new GoalLines(bestLines.get(0), bestLines.get(1), bestLines.get(2), bestLines.get(3),
							bestLines.get(4));

				} else if (expectedCaseSize == 4) {
					if (indexMinDiff - 2 < 0) {
						GLS = new GoalLines(goalLines.get(indexMinDiff - 1), goalLines.get(indexMinDiff),
								goalLines.get(indexMinDiff + 1), goalLines.get(indexMinDiff + 2),
								goalLines.get(indexMinDiff + 3));
					} else if (indexMinDiff + 2 > goalLines.size() - 1) {
						GLS = new GoalLines(goalLines.get(indexMinDiff - 3), goalLines.get(indexMinDiff - 2),
								goalLines.get(indexMinDiff - 1), goalLines.get(indexMinDiff),
								goalLines.get(indexMinDiff + 1));
					} else {
						System.out.println("tuka");
					}

				} else {
					System.out.println("tuka");
				}
			} else {
				if (goalLines.size() == 4) {
					if (indexMinDiff - 2 < 0) {
						GLS = new GoalLines(new main.Line(-1, -1, -1, "Pinn"), goalLines.get(0), goalLines.get(1),
								goalLines.get(2), goalLines.get(3));
					} else {
						GLS = new GoalLines(goalLines.get(0), goalLines.get(1), goalLines.get(2), goalLines.get(3),
								new main.Line(-1, -1, -1, "Pinn"));
					}
				} else {
					if (goalLines.size() == 1)
						GLS.main = goalLines.get(0);
					System.out.println("tuka");
				}
			}

			if (twoAndHalf == null) {
				System.out.println("Missing 2.5 goal line");
				div25.click();

				WebElement goalLineTable = div25.findElement(By.xpath("//table"));

				// getFirstResult for over 2.5 if pinaccle lacks the line
				List<WebElement> rowsGoals = goalLineTable.findElements(By.xpath("//tbody/tr"));

				if (rowsGoals.size() >= 2) {
					WebElement row = rowsGoals.get(1);
					String textOdds = row.getText();
					try {
						overOdds = Float.parseFloat(textOdds.split("\n")[2].trim());
						underOdds = Float.parseFloat(textOdds.split("\n")[3].trim());
					} catch (Exception e) {

					}
				}
			} else {
				for (main.Line line : goalLines) {
					if (line.line == 2.5) {
						overOdds = line.home;
						underOdds = line.away;
						break;
					}
				}
			}
		}

		// -----------------------------------------------------------------------
		// Asian handicap
		for (WebElement t : tabs) {
			if (t.getText().contains("AH")) {
				t.click();
				break;
			}
		}

		// Asian with closest line
		AsianLines asianLines = new AsianLines(def, def, def, def, def);

		WebElement opt = null;
		float min = 100f;
		List<WebElement> divsAsian = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divsAsian) {
			String text = div.getText();
			if (text.split("\n").length > 3) {
				try {
					float diff = Math.abs(Float.parseFloat(text.split("\n")[2].trim())
							- Float.parseFloat(text.split("\n")[3].trim()));
					if (diff < min) {
						min = diff;
						opt = div;
					}
				} catch (Exception e) {
					// System.out.println("asian problem" + home + " " + away);
				}
			}
		}

		int indexOfOptimal = opt == null ? -1 : divsAsian.indexOf(opt);

		ArrayList<main.Line> lines = new ArrayList<>();

		if (opt != null) {
			int lower = (indexOfOptimal - 5) < 0 ? 0 : (indexOfOptimal - 5);
			int higher = (indexOfOptimal + 5) > (divsAsian.size() - 1) ? (divsAsian.size() - 1) : (indexOfOptimal + 5);

			for (int j = lower; j <= higher; j++) {
				WebElement currentDiv = divsAsian.get(j);
				if (currentDiv == null || currentDiv.getText().split("\n").length < 3)
					continue;

				currentDiv.click();
				WebElement AHTable = currentDiv.findElement(By.xpath("//table"));

				// find the row
				List<WebElement> rowsAsian = AHTable.findElements(By.xpath("//tbody/tr"));
				float line = -1f, asianHome = -1f, asianAway = -1f;

				for (int r = rowsAsian.size() - 1; r >= 0; r--) {
					WebElement row = rowsAsian.get(r);
					if (row.getText().contains("Pinnacle")) {
						String textOdds = row.getText();
						line = Float.parseFloat(textOdds.split("\n")[1].trim());
						asianHome = Float.parseFloat(textOdds.split("\n")[2].trim());
						asianAway = Float.parseFloat(textOdds.split("\n")[3].trim());
						break;
					}

					if (row.getText().contains("Average"))
						break;
				}

				if (asianHome != -1f)
					lines.add(new main.Line(line, asianHome, asianAway, "Pinn"));

				List<WebElement> closeLink = currentDiv.findElements(By.className("odds-co"));
				if (!closeLink.isEmpty())
					closeLink.get(0).click();

				if (lines.size() == 6)
					break;

			}
			System.out.println(lines);

			int indexMinDiff = -1;
			float minDiff = 100f;
			for (int l = 0; l < lines.size(); l++) {
				float diff = Math.abs(lines.get(l).home - lines.get(l).away);
				if (diff < minDiff) {
					minDiff = diff;
					indexMinDiff = l;
				}
			}

			int start = indexMinDiff - 2 < 0 ? 0 : indexMinDiff - 2;
			int end = indexMinDiff + 2 > lines.size() - 1 ? lines.size() - 1 : indexMinDiff + 2;

			int expectedCaseSize = end - start + 1;
			if (lines.size() == 5) {
				asianLines = new AsianLines(lines.get(0), lines.get(1), lines.get(2), lines.get(3), lines.get(4));
			} else if (lines.size() > 5) {
				if (expectedCaseSize == 5) {
					ArrayList<main.Line> bestLines = new ArrayList<>();
					for (int c = start; c <= end; c++) {
						bestLines.add(lines.get(c));
					}

					asianLines = new AsianLines(lines.get(0), lines.get(1), lines.get(2), lines.get(3), lines.get(4));

				} else if (expectedCaseSize == 4) {
					if (indexMinDiff - 2 < 0) {
						asianLines = new AsianLines(lines.get(indexMinDiff - 1), lines.get(indexMinDiff),
								lines.get(indexMinDiff + 1), lines.get(indexMinDiff + 2), lines.get(indexMinDiff + 3));
					} else if (indexMinDiff + 2 > lines.size() - 1) {
						asianLines = new AsianLines(lines.get(indexMinDiff - 3), lines.get(indexMinDiff - 2),
								lines.get(indexMinDiff - 1), lines.get(indexMinDiff), lines.get(indexMinDiff + 1));
					} else {
						System.out.println("tuka");
					}

				} else {
					System.out.println("tuka");
				}
			} else {
				if (lines.size() == 4) {
					if (indexMinDiff - 2 < 0) {
						asianLines = new AsianLines(new main.Line(-1, -1, -1, "Pinn"), lines.get(0), lines.get(1),
								lines.get(2), lines.get(3));
					} else {
						asianLines = new AsianLines(lines.get(0), lines.get(1), lines.get(2), lines.get(3),
								new main.Line(-1, -1, -1, "Pinn"));
					}
				} else {
					if (lines.size() == 1)
						asianLines.main = lines.get(0);
					System.out.println("tuka");
				}
			}
		}

		ExtendedFixture ef = new FullFixture(date, home, away, fullResult, competition).withHTResult(htResult)
				.with1X2Odds(homeOdds, drawOdds, awayOdds)
				.withAsian(asianLines.main.line, asianLines.main.home, asianLines.main.away)
				.withOdds(overOdds, underOdds, overOdds, underOdds).withShots(-1, -1);

		return ((FullFixture) ef).withAsianLines(asianLines).withGoalLines(GLS);
	}

	public static ExtendedFixture getFastOddsFixture(WebDriver driver, String i, String competition)
			throws ParseException, IOException {
		// System.out.println(i);
		driver.navigate().to(i);

		Document fixture = Jsoup.connect(i).get();

		String title = fixture.select("h1").first().text();

		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();
		System.out.println(home + " : " + away);

		Element dt = fixture.select("p[class^=date]").first();

		String millisString = dt.outerHtml().split(" ")[3].split("-")[0].substring(1);

		long timeInMillis = Long.parseLong(millisString) * 1000;

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMillis);
		Date date = cal.getTime();
		System.out.println(date);

		// Result
		Result fullResult = new Result(-1, -1);
		Result htResult = new Result(-1, -1);
		try {

			Element resultJsoup = fixture.select("p[class^=result]").first();
			System.out.println(resultJsoup.text());

			String resString = resultJsoup.text();
			if (resString.contains("penalties") || resString.contains("ET")) {
				return null;
			}
			if (resString.contains("awarded") && resString.contains(home)) {
				fullResult = new Result(3, 0);
				htResult = new Result(3, 0);
			} else if (resString.contains("awarded") && resString.contains(away)) {
				fullResult = new Result(0, 3);
				htResult = new Result(0, 3);
			} else if (resString.contains("(") && resString.contains(")")) {
				String full = resString.split(" ")[2];
				String half = resString.split(" ")[3].substring(1, 4);

				fullResult = new Result(Integer.parseInt(full.split(":")[0]), Integer.parseInt(full.split(":")[1]));
				htResult = new Result(Integer.parseInt(half.split(":")[0]), Integer.parseInt(half.split(":")[1]));
			} else {
				fullResult = new Result(-1, -1);
				htResult = new Result(-1, -1);
			}

		} catch (Exception e) {
			System.out.println("next match");
		}

		System.out.println(fullResult + " " + htResult);

		WebElement table = driver.findElement(By.xpath("//div[@id='odds-data-table']"));

		String winnerSuffix = "#1X2;2";
		fixture = Jsoup.connect(i + winnerSuffix).get();

		Elements tableJsoup = fixture.select("div#odds-data-table").select("div");
		System.out.println(fixture.text());

		Elements rowsJsoup = tableJsoup.select("tr");

		// find the row
		List<WebElement> customer = table.findElements(By.xpath("//div[1]/table/tbody/tr"));
		int pinnIndex = -2;
		int Index365 = -2;

		for (WebElement row : customer) {
			if (row.getText().contains("Pinnacle"))
				pinnIndex = customer.indexOf(row) + 1;
			if (row.getText().contains("bet365"))
				pinnIndex = customer.indexOf(row) + 1;
		}
		if (pinnIndex < 0) {
			System.out.println("Could not find pinnacle");
			pinnIndex = Index365;
			if (pinnIndex < 0)
				pinnIndex = 2;
		}

		float homeOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[2]")).getText());
		float drawOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[3]")).getText());
		float awayOdds = Float
				.parseFloat(table.findElement(By.xpath("//div[1]/table/tbody/tr[" + pinnIndex + "]/td[4]")).getText());

		// System.out.println(homeOdds);
		// System.out.println(drawOdds);
		// System.out.println(awayOdds);

		// Over and under odds
		float overOdds = -1f, underOdds = -1f;
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs) {
			if (t.getText().contains("O/U")) {
				t.click();
				break;
			}
		}

		WebElement div25 = null;
		List<WebElement> divs = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divs) {
			if (div.getText().contains("+2.5")) {
				// System.out.println(div.getText());
				div25 = div;
				div.click();
				break;
			}
		}

		WebElement OUTable = div25.findElement(By.xpath("//table"));

		// find the row
		List<WebElement> rows = OUTable.findElements(By.xpath("//tr"));

		for (WebElement row : rows) {
			if (row.getText().contains("Pinnacle")) {
				String textOdds = row.getText();
				overOdds = Float.parseFloat(textOdds.split("\n")[2].trim());
				underOdds = Float.parseFloat(textOdds.split("\n")[3].trim());
			}
		}

		// System.out.println("over: " + overOdds + " " + underOdds);

		// Asian handicap
		for (WebElement t : tabs) {
			if (t.getText().contains("AH")) {
				t.click();
				break;
			}
		}

		// Asian with closest line
		WebElement opt = null;
		float min = 100f;
		List<WebElement> divsAsian = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divsAsian) {
			String text = div.getText();
			if (text.split("\n").length > 3) {
				try {
					float diff = Math.abs(Float.parseFloat(text.split("\n")[2].trim())
							- Float.parseFloat(text.split("\n")[3].trim()));
					if (diff < min) {
						min = diff;
						opt = div;
					}
				} catch (Exception e) {
					System.out.println("asian problem" + home + " " + away);
				}
			}
		}

		float line = -1f, asianHome = -1f, asianAway = -1f;
		// if (home.equals("Sport Recife") && away.equals("Corinthians")){
		// System.out.println(min);
		// System.out.println(opt.getText());
		// }

		if (opt != null) {
			opt.click();

			WebElement AHTable = opt.findElement(By.xpath("//table"));

			// find the row
			List<WebElement> rowsAsian = AHTable.findElements(By.xpath("//tr"));

			for (WebElement row : rowsAsian) {
				if (row.getText().contains("Pinnacle")) {
					String textOdds = row.getText();
					line = Float.parseFloat(textOdds.split("\n")[1].trim());
					asianHome = Float.parseFloat(textOdds.split("\n")[2].trim());
					asianAway = Float.parseFloat(textOdds.split("\n")[3].trim());
				}
			}

			// System.out.println(line + " " + asianHome + " " + asianAway);

		}

		ExtendedFixture ef = new ExtendedFixture(date, home, away, fullResult, competition).withHTResult(htResult)
				.with1X2Odds(homeOdds, drawOdds, awayOdds).withAsian(line, asianHome, asianAway)
				.withOdds(overOdds, underOdds, overOdds, underOdds).withShots(-1, -1);
		return ef;
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

	private static String getHome(String teams) {
		String[] split = teams.split(" vs. ");
		return split[0].trim();
	}

	public static boolean isScore(String text) {
		String[] splitted = text.split("-");

		return splitted.length == 2 && isNumeric(splitted[0].trim()) && isNumeric(splitted[1].trim());
	}

	public static Result getResult(String text) {
		String[] splitted = text.split("-");

		return new Result(Integer.parseInt(splitted[0].trim()), Integer.parseInt(splitted[1].trim()));
	}

	public static boolean isNumeric(String str) {
		try {
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
