package scraper;

import java.io.File;

import org.json.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.json.Json;

import constants.Constants;
import entries.FinalEntry;
import main.AsianLines;
import main.ExtendedFixture;
import main.Fixture;
import main.FullFixture;
import main.GoalLines;
import main.Line;
import main.PlayerFixture;
import main.Result;
import main.SQLiteJDBC;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.Odds;
import odds.OverUnderOdds;
import predictions.Predictions.OnlyTodayMatches;
import predictions.UpdateType;
import runner.RunnerOdds;
import runner.UpdateRunner;
import utils.Pair;
import utils.Utils;
import xls.XlSUtils;

public class Scraper {
	public static final DateFormat OPTAFORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
	public static final DateFormat FORMATFULL = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
	public static final DateFormat ODDSHISTORYFORMAT = new SimpleDateFormat("dd MMMM HH:mm", Locale.US);
	public static final String BASE = "http://int.soccerway.com/";
	public static final String OUSUFFIX = "#over-under;2;2.50;0";
	public static final int CURRENT_YEAR = 2017;

	public static void main(String[] args)
			throws IOException, ParseException, InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();

		// =================================================================

		// ArrayList<Fixture> eng = SQLiteJDBC.selectFixtures("ENG", 2016);
		// System.out.println(eng.size());
		// eng.stream().limit(20).collect(Collectors.toList()).forEach(System.out::println);

		// for (int i = 2012; i <= 2012; i++) {
		// ArrayList<PlayerFixture> list = collectFull("BRA", i, null);
		// // //
		// //
		// "http://int.soccerway.com/national/scotland/premier-league/2007-2008/regular-season/");
		// // //
		// //
		// "http://int.soccerway.com/national/germany/bundesliga/2010-2011/regular-season/");
		// SQLiteJDBC.storePlayerFixtures(list, i, "BRA");
		// }

		// collectAndStoreSinglePFS("BRA", 2016,
		// "http://int.soccerway.com/matches/2016/06/23/brazil/serie-a/clube-atletico-mineiro/sport-club-corinthians-paulista/2217995/");

		// ArrayList<PlayerFixture> list =
		// SQLiteJDBC.selectPlayerFixtures("ENG", 2015);
		// System.out.println(list.size());
		// ====================================================================

		ArrayList<Fixture> stats = GameStatsCollector.of("ENG", 2016).collect();

		// ArrayList<ExtendedFixture> shotsList = collect("ENG", 2016, null);
		// list.addAll(collect("JP", 2016,
		// "http://int.soccerway.com/national/japan/j1-league/2016/2nd-stage/"));
		// shotsList = new ArrayList<>();
		// XlSUtils.storeInExcel(shotsList, "BRA", 2017, "manual");

		//
		// ArrayList<ExtendedFixture> list = oddsInParallel("ENG", 2013, null);

		// ArrayList<ExtendedFixture> list = odds("BRA", 2017, null);
		// XlSUtils.storeInExcel(list, "BRA", 2017, "odds");
		// nextMatches("SPA", null, OnlyTodayMatches.TRUE);
		// fastOdds("ENG", 2017, null);
		// ArrayList<Fixture> list = fullOdds("ENG", 2010, null);
		// SQLiteJDBC.storePlayerFixtures(list);
		// ArrayList<Fixture> list2 = fullOdds("ENG", 2009, null);
		// SQLiteJDBC.storePlayerFixtures(list2);
		// ArrayList<Fixture> list3 = fullOdds("ENG", 2010, null);
		// SQLiteJDBC.storePlayerFixtures(list3);
		// XlSUtils.storeInExcelFull(list, "GER", 2016, "fullodds");

		// ArrayList<FullFixture> list2 = fullOdds("SPA", 2013,
		// "http://www.oddsportal.com/soccer/spain/primera-division-2013-2014");
		// XlSUtils.storeInExcelFull(list2, "SPA", 2013, "fullodds");

		// XlSUtils.combine("BRA", 2017, "manual");
		// XlSUtils.combineFull("SPA", 2015, "all-data");
		// ////
		// XlSUtils.fillMissingShotsData("BRA", 2017, false);

		// ArrayList<ExtendedFixture> next = nextMatches("BRB", null);
		// nextMatchesValues("ENG2", null, OnlyTodayMatches.FALSE, null, 27,
		// 11);
		// collect("FR", 2017, null);
		// collectUpToDate("GER2", 2017, new Date(), null);

		// checkAndUpdate("IT", OnlyTodayMatches.FALSE);
		// checkAndUpdate("BEL", OnlyTodayMatches.FALSE);
		// checkAndUpdate("FR2", OnlyTodayMatches.TRUE);
		// checkAndUpdate("BRA", OnlyTodayMatches.FALSE);
		// checkAndUpdate("SWE", OnlyTodayMatches.FALSE);
		// checkAndUpdate("ENG5", OnlyTodayMatches.TRUE);
		// checkAndUpdate("BUL", OnlyTodayMatches.TRUE);
		// checkAndUpdate("NED", OnlyTodayMatches.FALSE);
		// checkAndUpdate("FR", OnlyTodayMatches.FALSE);

		// updateInParallel();

		// fastOdds("SPA", 2016, null);

		// Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
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
	 * @param k
	 * @param j
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void updateInParallel(ArrayList<String> list, int n, OnlyTodayMatches onlyToday, UpdateType automatic,
			int day, int month) throws IOException, InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(n);
		ArrayList<String> leagues = automatic.equals(UpdateType.AUTOMATIC) ? getTodaysLeagueList(day, month) : list;
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

	public static ArrayList<String> getTodaysLeagueList(int day, int month) throws IOException {
		ArrayList<String> result = new ArrayList<>();

		Document page = Jsoup
				.connect("http://www.soccerway.com/matches/2017/" + month + "/" + (day >= 10 ? day : ("0" + day)) + "/")
				.timeout(0).get();

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

		ArrayList<ExtendedFixture> all = sh == null ? new ArrayList<>() : XlSUtils.selectAll(sh, 0);
		// problem when no pendingma fixtures?
		Date oldestTocheck = Utils.findLastPendingFixture(all);
		System.out.println(oldestTocheck);

		ArrayList<ExtendedFixture> toAdd = new ArrayList<>();
		ArrayList<ExtendedFixture> combined = new ArrayList<>();
		// check if update of previous results is necessary
		if (new Date().after(oldestTocheck)) {
			ArrayList<ExtendedFixture> odds = oddsUpToDate(competition, collectYear, oldestTocheck, null);
			System.out.println(odds.size() + " odds ");

			ArrayList<ExtendedFixture> list = new ArrayList<>();
			int count = 0;
			int maxTries = 1;
			while (true) {
				try {
					list = collectUpToDate(competition, collectYear, oldestTocheck, null);
					break;
				} catch (Exception e) {
					if (++count == maxTries)
						throw e;
				}
			}

			System.out.println(list.size() + "shots");

			HashMap<String, String> dictionary = XlSUtils.deduceDictionary(odds, list);

			combined = XlSUtils.combineWithDictionary(odds, list, competition, dictionary);
			System.out.println(combined.size() + " combined");
			System.out.println(competition + " "
					+ (combined.size() == list.size() ? " combined successfull" : " combined failed"));

			toAdd.addAll(combined);
		}

		// add the combined(updated) fixtures to the list of all finished
		// fixtures
		for (ExtendedFixture i : all) {
			boolean continueFlag = false;
			for (ExtendedFixture comb : combined) {
				if (i.homeTeam.equals(comb.homeTeam) && i.awayTeam.equals(comb.awayTeam)
						&& (Math.abs(i.date.getTime() - comb.date.getTime()) <= 24 * 60 * 60 * 1000)) {
					continueFlag = true;
				}
			}
			if (!continueFlag)
				toAdd.add(i);
		}

		workbook.close();
		System.out.println("to add " + toAdd.size());
		ArrayList<ExtendedFixture> next = new ArrayList<>();

		next = nextMatches(competition, null, onlyTodaysMatches);

		ArrayList<ExtendedFixture> withNext = new ArrayList<>();

		for (ExtendedFixture i : toAdd) {
			boolean continueFlag = false;
			for (ExtendedFixture n : next) {
				if (i.homeTeam.equals(n.homeTeam) && i.awayTeam.equals(n.awayTeam)
						&& (Math.abs(i.date.getTime() - n.date.getTime()) <= 24 * 60 * 60 * 1000)) {
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
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		login(driver);

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

				ArrayList<String> links = new ArrayList<>();
				WebElement table = driver.findElement(By.id("tournamentTable"));
				List<WebElement> rows = table.findElements(By.xpath("//tbody/tr"));

				for (WebElement i : rows) {
					if (i.getText().contains("-")) {
						WebElement aElem = i.findElement(By.cssSelector("a"));
						if (aElem != null) {
							String href = aElem.getAttribute("href");
							// System.out.println(href);
							if (isFixtureLink(href))
								links.add(href);
						}
					}

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
				driver = new ChromeDriver(options);
				// driver = new ChromeDriver();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();

				driver.navigate().to(address + "/results/");
				login(driver);
				driver.navigate().to(address + "/results/");
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
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
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
			Element list = matches.select("table[class=matches   ]").first();
			Elements linksM = list.select("a[href]");
			Elements fixtures = new Elements();
			for (Element linkM : linksM) {
				if (isScore(linkM.text())) {
					fixtures.add(linkM);
				}
			}

			for (int i = fixtures.size() - 1; i >= 0; i--) {
				Document fixture = Jsoup.connect(BASE + fixtures.get(i).attr("href")).timeout(0).get();
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

			Actions actions = new Actions(driver);
			actions.moveToElement(driver.findElement(By.className("previous"))).click().perform();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;

			// Additional stopping condition - no new entries
			// if (set.size() == setSize)
			// break;

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
		String address = EntryPoints.getOddsLink(competition, EntryPoints.CURRENT);
		System.out.println(address);

		ArrayList<ExtendedFixture> result = new ArrayList<>();
		Set<String> teams = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address);

		login(driver);

		driver.navigate().to(address);

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

	public static ArrayList<ExtendedFixture> nextMatchesValues(String competition, Object object,
			OnlyTodayMatches onlyTodaysMatches, ArrayList<FinalEntry> predictions, int day, int month)
					throws ParseException, InterruptedException, IOException {
		String address = EntryPoints.getOddsLink(competition, EntryPoints.CURRENT);
		System.out.println(address);

		ArrayList<ExtendedFixture> result = new ArrayList<>();
		Set<String> teams = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address);

		login(driver);

		driver.navigate().to(address);

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

			ExtendedFixture ef = getFullFixtureOUValue(driver, i, competition, predictions, day, month);
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

		while (true) {
			String html = driver.getPageSource();
			Document matches = Jsoup.parse(html);
			int setSize = set.size();
			Element list = matches.select("table[class=matches   ]").first();
			Elements linksM = list.select("a[href]");
			for (Element linkM : linksM) {
				// System.out.println(linkM.text());
				if (isScore(linkM.text())) {
					Document fixture = Jsoup.connect(BASE + linkM.attr("href")).get();
					ExtendedFixture ef = getFixture(fixture, competition);
					result.add(ef);
					set.add(ef);
				}
			}

			Actions actions = new Actions(driver);
			actions.moveToElement(driver.findElement(By.className("previous"))).click().perform();
			Thread.sleep(1000);
			String htmlAfter = driver.getPageSource();

			if (html.equals(htmlAfter))
				break;

			// Additional stopping condition - no new entries
			// if (set.size() == setSize)
			// break;

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
		Pair shots = null;
		// Possession can't be parsed using jsoup only
		// possible if using selenium but performance would be worse

		Elements frames = fixture.select("iframe");
		for (Element i : frames) {
			if (i.attr("src").contains("/charts/statsplus")) {
				Document stats = Jsoup.connect(BASE + i.attr("src")).timeout(0).get();
				try {
					shots = selectStatsWithTextDescription(stats, "Shots on target");
				} catch (Exception exp) {
					System.out.println("Exception when parsing stats");
				}
				break;
			}
		}

		String dateString = fixture.select("dt:contains(Date) + dd").first().text();
		String timeString = "21:00";
		try {
			timeString = fixture.select("dt:contains(Kick-off) + dd").first().text();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("DA");
		}
		// the time is in gmt+1
		long hour = 3600 * 1000;
		Date date = new Date(FORMATFULL.parse(dateString + " " + timeString).getTime() + hour);
		System.out.println(date);

		String teams = fixture.select("h1").first().text();
		// System.out.println(getHome(teams));
		// System.out.println(getAway(teams));
		String homeTeam = Utils.replaceNonAsciiWhitespace(getHome(teams));
		String awayTeam = Utils.replaceNonAsciiWhitespace(getAway(teams));

		ExtendedFixture ef = new ExtendedFixture(date, homeTeam, awayTeam, result, "BRA").withHTResult(ht)
				.withShots((int) shots.home, (int) shots.away);
		if (matchday != -1)
			ef = ef.withMatchday(matchday);
		System.out.println(ef);

		return ef;
	}

	private static Pair selectStatsWithTextDescription(Document stats, String text) {
		Element element = stats.select("tr:contains(" + text + ")").get(1);
		int homeStats = Integer.parseInt(element.select("td.legend.left.value").text());
		int awayStats = Integer.parseInt(element.select("td.legend.right.value").text());
		return Pair.of(homeStats, awayStats);
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
		Element divSubstitutes = divsPlayers.size() > 1 ? divsPlayers.get(1) : null;
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

		login(driver);

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
					// System.out.println(href);
					if (i.getText().contains("-") && isFixtureLink(href)) {
						links.add(href);

					}
				}

				// System.out.println(links);
				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);
					if (ef != null)
						result.add(ef);

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

				driver.navigate().to(address + "/results/");
				login(driver);
				driver.navigate().to(address + "/results/");
			}
		}

		driver.close();

		ArrayList<ExtendedFixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	public static ArrayList<Fixture> fullOdds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
		} else
			address = add;
		System.out.println(address);

		Set<Fixture> result = new HashSet<>();

		System.setProperty("webdriver.chrome.drive", "C:/Windows/system32/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.navigate().to(address + "/results/");

		login(driver);

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

		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				ArrayList<String> links = new ArrayList<>();
				WebElement table = driver.findElement(By.id("tournamentTable"));
				// List<WebElement> rows =
				// table.findElements(By.xpath("//tbody/tr"));
				List<WebElement> tagrows = table.findElements(By.tagName("tr"));

				for (WebElement i : tagrows) {
					if (i.getText().contains("-")) {

						WebElement aElem = i.findElement(By.cssSelector("a"));
						if (aElem != null) {
							String href = aElem.getAttribute("href");
							// System.out.println(href);
							if (isFixtureLink(href))
								links.add(href);
						}
					}

				}

				for (String i : links) {
					Fixture f = getFullFixtureTest(driver, i, competition, year);
					if (f != null)
						result.add(f);

					// break;

				}

				// break;
			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				driver.close();
				Thread.sleep(5000);
				driver = new ChromeDriver(options);
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().window().maximize();

				driver.navigate().to(address + "/results/");
				login(driver);
				driver.navigate().to(address + "/results/");
			}
		}

		driver.close();

		ArrayList<Fixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	private static void login(WebDriver driver) {
		String label = "Login";
		driver.findElement(By.xpath("//button[contains(.,'" + label + "')]")).click();

		String pass = "";
		Path wiki_path = Paths.get(new File("").getAbsolutePath(), "pass.txt");

		Charset charset = Charset.forName("ISO-8859-1");
		try {
			List<String> lines = Files.readAllLines(wiki_path, Charset.defaultCharset());

			for (String line : lines)
				pass += (line.trim());
		} catch (IOException e) {
			System.out.println(e);
		}

		driver.findElement(By.id("login-username1")).sendKeys("Vortex84");
		driver.findElement(By.id("login-password1")).sendKeys(pass);

		// click the local login button
		driver.findElements(By.xpath("//button[contains(.,'" + label + "')]")).get(1).click();
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
		// int count = 0;
		// int maxTries = 10;
		// while (true) {
		// try {
		driver.navigate().to(i);
		// break;
		// } catch (Exception e) {
		// if (++count == maxTries)
		// throw e;
		// }
		// }

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();
		System.out.println(home + " : " + away);

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		dateString = dateString.split(",")[1] + dateString.split(",")[2];
		Date date = FORMATFULL.parse(dateString);
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
				try {
					t.click();
				} catch (Exception e) {
					System.out.println("click error o/u");
					e.printStackTrace();
				}
				break;
			}
		}

		WebElement div25 = null;
		List<WebElement> divs = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divs) {
			if (div.getText().contains("+2.5")) {
				// System.out.println(div.getText());
				div25 = div;
				try {
					JavascriptExecutor jse = (JavascriptExecutor) driver;
					jse.executeScript("window.scrollBy(0,250)", "");
					div.click();
				} catch (Exception e) {
					System.out.println("click error o/u 2.5");
					e.printStackTrace();
				}
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
				try {
					t.click();
				} catch (Exception e) {
					System.out.println("click error AH");
				}
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

		if (opt != null) {
			try {
				Actions actions = new Actions(driver);
				actions.moveToElement(opt).click().perform();
			} catch (Exception e) {
				System.out.println("click error ah line ==");
				e.printStackTrace();
			}

			WebElement AHTable = opt.findElement(By.xpath("//table"));

			// find the row
			List<WebElement> rowsAsian = AHTable.findElements(By.xpath("//tr"));

			for (WebElement row : rowsAsian) {
				if (row.getText().contains("Pinnacle")) {
					String textOdds = row.getText();
					line = Float.parseFloat(textOdds.split("\n")[1].trim());
					asianHome = Float.parseFloat(textOdds.split("\n")[2].trim());
					asianAway = Float.parseFloat(textOdds.split("\n")[3].trim());
					break;
				}
			}

			// System.out.println(line + " " + asianHome + " " + asianAway);

		}

		ExtendedFixture ef = new ExtendedFixture(date, home, away, fullResult, competition).withHTResult(htResult)
				.with1X2Odds(homeOdds, drawOdds, awayOdds).withAsian(line, asianHome, asianAway)
				.withOdds(overOdds, underOdds, overOdds, underOdds).withShots(-1, -1);
		return ef;
	}

	public static Fixture getFullFixtureTest(WebDriver driver, String i, String competition, int year)
			throws Exception {
		long start = System.currentTimeMillis();
		driver.navigate()
				.to(/* "http://www.oddsportal.com/soccer/england/premier-league/burnley-watford-j95WUhWk/" */i);

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();
		System.out.println(home + " : " + away);

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		dateString = dateString.split(",")[1] + dateString.split(",")[2];
		Date date = FORMATFULL.parse(dateString);

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

		Thread.sleep(1500);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		// for bookmaker name hash --
		jse.executeScript(
				"document.body.innerHTML += '<div style=\"display:none;\" id=\"hackerman2\">' + JSON.stringify(globals.bookmakerData) + '</div>'");
		String bookmakerHash = (String) jse.executeScript("return document.getElementById('hackerman2').innerHTML");

		HashMap<Integer, String> booksMap = getBookmakersMap(bookmakerHash);
		// System.out.println(booksMap);

		HashMap<String, ArrayList<MatchOdds>> matchOdds = getMatchDataFromJS(driver, booksMap);
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overUnderOdds = getOverUnderDataFromJS(driver,
				booksMap);
		HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> asianOdds = getAsianDataFromJS(driver, booksMap);

		Fixture f = new Fixture(date, year, competition, home, away, fullResult).withHTResult(htResult)
				.withOUodds(overUnderOdds).withAsianOdds(asianOdds).withMatchOdds(matchOdds);

		// match odds analysis over pinnacle
		// ArrayList<MatchOdds> matchOdds = fullMatchOddsOverPinnacle(driver,
		// date);
		// System.out.println("-----------------------------------------------------");

		System.out.println("full odds data time " + (System.currentTimeMillis() - start) / 1000d + "sec");
		return f;
	}

	private static HashMap<String, ArrayList<MatchOdds>> getMatchDataFromJS(WebDriver driver,
			HashMap<Integer, String> booksMap) throws Exception {
		JSONObject json = getJsonDataFromJS(driver, "1X2");

		HashMap<String, ArrayList<MatchOdds>> matchOdds = getFullMatchOddsHistory(json, booksMap);
		return matchOdds;
	}

	/**
	 * Executes java script to get odds data in json format with retrying
	 * 
	 * @param driver
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private static JSONObject getJsonDataFromJS(WebDriver driver, String type) throws Exception {
		JSONObject json = new JSONObject();
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		int count = 0;
		int maxTries = 7;
		while (true) {
			try {
				navigateToTab(driver, type);
				navigateToTab(driver, type);
				Thread.sleep(2000);
				jse.executeScript(
						"document.body.innerHTML += '<div style=\"display:none;\" id=\"hackerman\">' + JSON.stringify(page.getTableSet(page.bettingType, page.scopeId).data) + '</div>'");
				String data = (String) jse.executeScript("return document.getElementById('hackerman').innerHTML");

				json = new JSONObject(data);
				break;
			} catch (Exception e) {
				System.out.println(type + " retry " + count);
				if (++count == maxTries) {
					System.out.println("Json parsing timeout for:" + type + " try " + count);
					throw e;
				}
			}
		}
		return json;
	}

	private static HashMap<String, ArrayList<MatchOdds>> getFullMatchOddsHistory(JSONObject json,
			HashMap<Integer, String> booksMap) {
		HashMap<String, Float> homeOutcomesID = new HashMap<>();
		HashMap<String, Float> drawOutcomesID = new HashMap<>();
		HashMap<String, Float> awayOutcomesID = new HashMap<>();

		ArrayList<MatchOdds> closingOdds = new ArrayList<>();

		// from odds data get outcomeid hash
		getOutcomesIDandClosingMatchOdds(json, homeOutcomesID, drawOutcomesID, awayOutcomesID, closingOdds, booksMap);

		// map key is the line value -list of odds history (for over and under
		// outcomes)
		HashMap<String, ArrayList<MatchOdds>> homeHistoriesMap = new HashMap<>();
		HashMap<String, ArrayList<MatchOdds>> drawHistoriesMap = new HashMap<>();
		HashMap<String, ArrayList<MatchOdds>> awayHistoriesMap = new HashMap<>();

		JSONObject hist = json.getJSONObject("history");
		// JSONObject lay = hist.getJSONObject("lay");
		JSONObject back = hist.getJSONObject("back");

		Set<String> keys = back.keySet();
		for (String i : keys) {
			// String bookmaker = "";
			boolean isHome = false;
			boolean isDraw = false;
			boolean isAway = false;
			// System.out.println(i);
			if (homeOutcomesID.containsKey(i))
				isHome = true;

			if (drawOutcomesID.containsKey(i))
				isDraw = true;

			if (awayOutcomesID.containsKey(i))
				isAway = true;

			JSONObject outcome1 = back.getJSONObject(i);
			Set<String> bookies = outcome1.keySet();
			for (String b : bookies) {
				String bookmaker = booksMap.get(Integer.parseInt(b));
				// System.out.println(bookmaker);
				JSONArray history = outcome1.getJSONArray(b);
				ArrayList<MatchOdds> oddsHistory = new ArrayList<>();
				for (int j = 0; j < history.length(); j++) {
					JSONArray hentry = history.getJSONArray(j);
					float odds = (float) hentry.getDouble(0);
					// sometimes null sometimes integer unknown meaning
					Object unknown = hentry.get(1);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(hentry.getLong(2) * 1000);
					Date date = cal.getTime();
					// System.out.println(odds + " " + date);

					MatchOdds modds = new MatchOdds(bookmaker, date, isHome ? odds : -1f, isDraw ? odds : -1f,
							isAway ? odds : -1f);
					// System.out.println(modds);
					oddsHistory.add(modds);

				}
				// oddsHistory.stream().forEach(System.out::println);

				if (isHome)
					homeHistoriesMap.put(bookmaker, oddsHistory);
				if (isDraw)
					drawHistoriesMap.put(bookmaker, oddsHistory);
				if (isAway)
					awayHistoriesMap.put(bookmaker, oddsHistory);

			}
		}

		// add the closing odds before combining
		for (MatchOdds i : closingOdds) {
			if (i.homeOdds != -1f) {
				if (homeHistoriesMap.get(i.bookmaker) == null)
					homeHistoriesMap.put(i.bookmaker, new ArrayList<>());

				if (!homeHistoriesMap.get(i.bookmaker).contains(i))
					homeHistoriesMap.get(i.bookmaker).add(i);
			}

			if (i.drawOdds != -1f) {
				if (drawHistoriesMap.get(i.bookmaker) == null)
					drawHistoriesMap.put(i.bookmaker, new ArrayList<>());

				if (!drawHistoriesMap.get(i.bookmaker).contains(i))
					drawHistoriesMap.get(i.bookmaker).add(i);
			}

			if (i.awayOdds != -1f) {
				if (awayHistoriesMap.get(i.bookmaker) == null)
					awayHistoriesMap.put(i.bookmaker, new ArrayList<>());

				if (!awayHistoriesMap.get(i.bookmaker).contains(i))
					awayHistoriesMap.get(i.bookmaker).add(i);
			}
		}

		HashMap<String, ArrayList<MatchOdds>> combined = combineMatchOddsHistories(homeHistoriesMap, drawHistoriesMap,
				awayHistoriesMap);

		return combined;
	}

	private static HashMap<String, ArrayList<MatchOdds>> combineMatchOddsHistories(
			HashMap<String, ArrayList<MatchOdds>> homeHistoriesMap,
			HashMap<String, ArrayList<MatchOdds>> drawHistoriesMap,
			HashMap<String, ArrayList<MatchOdds>> awayHistoriesMap) {
		HashMap<String, ArrayList<MatchOdds>> result = new HashMap<>();

		for (String bookmaker : homeHistoriesMap.keySet()) {
			// System.out.println(bookmaker);
			ArrayList<MatchOdds> booklist = new ArrayList<>();
			ArrayList<MatchOdds> homeHistory = homeHistoriesMap.get(bookmaker);
			ArrayList<MatchOdds> drawHistory = drawHistoriesMap.get(bookmaker);
			ArrayList<MatchOdds> awayHistory = awayHistoriesMap.get(bookmaker);

			homeHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());
			drawHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());
			awayHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());

			if (!(homeHistory.get(homeHistory.size() - 1).time.equals(drawHistory.get(drawHistory.size() - 1).time)
					&& drawHistory.get(drawHistory.size() - 1).time
							.equals(awayHistory.get(awayHistory.size() - 1).time)))
				System.out.println("Opening 1x2 odds dates differ:" + bookmaker);

			MatchOdds openingHome = homeHistory.remove(homeHistory.size() - 1);
			MatchOdds openingDraw = drawHistory.remove(drawHistory.size() - 1);
			MatchOdds openingAway = awayHistory.remove(awayHistory.size() - 1);
			MatchOdds opening = new MatchOdds(bookmaker, openingHome.time, openingHome.homeOdds, openingDraw.drawOdds,
					openingAway.awayOdds).withIsOpening();

			// in the corner case where there is no change in odds, i.e. opening
			// and closing are the same
			if (homeHistory.isEmpty() && drawHistory.isEmpty() && awayHistory.isEmpty())
				opening.isClosing = true;
			booklist.add(opening);

			// corner case only one of the histories doesn't change, but the
			// others do, for example draw odds are the same for opening and
			if (homeHistory.isEmpty())
				homeHistory.add(openingHome.withIsClosing());
			if (drawHistory.isEmpty())
				drawHistory.add(openingDraw.withIsClosing());
			if (awayHistory.isEmpty())
				awayHistory.add(openingAway.withIsClosing());

			TreeSet<Date> changeTimes = new TreeSet<>();
			changeTimes.addAll(homeHistory.stream().map(v -> v.time).collect(Collectors.toSet()));
			changeTimes.addAll(drawHistory.stream().map(v -> v.time).collect(Collectors.toSet()));
			changeTimes.addAll(awayHistory.stream().map(v -> v.time).collect(Collectors.toSet()));

			Map<Date, MatchOdds> homeMap = homeHistory.stream()
					.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));
			Map<Date, MatchOdds> drawMap = drawHistory.stream()
					.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));
			Map<Date, MatchOdds> awayMap = awayHistory.stream()
					.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));

			Date last = getLastDateFromOddsHistories(homeHistory, drawHistory, awayHistory);

			for (Date t : changeTimes.tailSet(last)) {
				boolean homeIsNull = homeMap.get(t) != null;
				boolean drawIsNull = drawMap.get(t) != null;
				boolean awayIsNull = awayMap.get(t) != null;

				MatchOdds homeOdds = homeIsNull ? homeMap.get(t)
						: homeMap.get(new TreeMap<Date, MatchOdds>(homeMap).headMap(t).lastKey());
				MatchOdds drawOdds = drawIsNull ? drawMap.get(t)
						: drawMap.get(new TreeMap<Date, MatchOdds>(drawMap).headMap(t).lastKey());
				MatchOdds awayOdds = awayIsNull ? awayMap.get(t)
						: awayMap.get(new TreeMap<Date, MatchOdds>(awayMap).headMap(t).lastKey());
				MatchOdds newm = new MatchOdds(bookmaker, t, homeOdds.homeOdds, drawOdds.drawOdds, awayOdds.awayOdds);
				newm.isClosing = homeOdds.isClosing && drawOdds.isClosing && awayOdds.isClosing;
				if (!booklist.contains(newm))
					booklist.add(newm);

			}
			result.put(bookmaker, booklist);
		}

		return result;
	}

	private static void getOutcomesIDandClosingMatchOdds(JSONObject json, HashMap<String, Float> homeOutcomesID,
			HashMap<String, Float> drawOutcomesID, HashMap<String, Float> awayOutcomesID,
			ArrayList<MatchOdds> closingOdds, HashMap<Integer, String> booksMap) {
		JSONObject oddsdata = json.getJSONObject("oddsdata");
		JSONObject backOdds = oddsdata.getJSONObject("back");
		Set<String> keysOdds = backOdds.keySet();
		for (String i : keysOdds) {
			JSONObject entry = backOdds.getJSONObject(i);
			float handicapValue = (float) entry.getDouble("handicapValue" + "");
			// System.out.println(handicapValue);
			Object outcomedID = entry.get("OutcomeID");
			if (outcomedID instanceof JSONArray) {
				JSONArray arr = (JSONArray) outcomedID;
				homeOutcomesID.put(arr.getString(0), handicapValue);
				drawOutcomesID.put(arr.getString(1), handicapValue);
				awayOutcomesID.put(arr.getString(2), handicapValue);
			} else {
				JSONObject obj = (JSONObject) outcomedID;
				homeOutcomesID.put(obj.getString("0"), handicapValue);
				drawOutcomesID.put(obj.getString("1"), handicapValue);
				awayOutcomesID.put(obj.getString("2"), handicapValue);
			}

			JSONObject closingOddsObject = entry.getJSONObject("odds");
			JSONObject changeTime = entry.getJSONObject("change_time");
			Set<String> bookies = closingOddsObject.keySet();
			Calendar cal = Calendar.getInstance();

			for (String b : bookies) {
				String bookmaker = booksMap.get(Integer.parseInt(b));
				Object closingObjectJSON = closingOddsObject.get(b);
				float homeOdds = -1f, drawOdds = -1f, awayOdds = -1f;

				if (closingObjectJSON instanceof JSONArray) {
					homeOdds = (float) closingOddsObject.getJSONArray(b).getDouble(0);
					drawOdds = (float) closingOddsObject.getJSONArray(b).getDouble(1);
					awayOdds = (float) closingOddsObject.getJSONArray(b).getDouble(2);
				} else {
					homeOdds = (float) closingOddsObject.getJSONObject(b).getDouble("0");
					drawOdds = (float) closingOddsObject.getJSONObject(b).getDouble("1");
					awayOdds = (float) closingOddsObject.getJSONObject(b).getDouble("2");
				}

				Date timeHome = null, timeDraw = null, timeAway = null;

				Object changeTimeJSON = changeTime.get(b);

				if (changeTimeJSON instanceof JSONArray) {
					cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(0) * 1000);
					timeHome = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(1) * 1000);
					timeDraw = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(2) * 1000);
					timeAway = cal.getTime();
				} else {
					cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("0") * 1000);
					timeHome = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("1") * 1000);
					timeDraw = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("2") * 1000);
					timeAway = cal.getTime();
				}

				MatchOdds closingHome = new MatchOdds(bookmaker, timeHome, homeOdds, -1f, -1f).withIsClosing();
				MatchOdds closingDraw = new MatchOdds(bookmaker, timeDraw, -1f, drawOdds, -1f).withIsClosing();
				MatchOdds closingAway = new MatchOdds(bookmaker, timeAway, -1f, -1f, awayOdds).withIsClosing();

				closingOdds.add(closingHome);
				closingOdds.add(closingDraw);
				closingOdds.add(closingAway);

			}

		}

	}

	private static HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> getAsianDataFromJS(WebDriver driver,
			HashMap<Integer, String> booksMap) throws Exception {
		JSONObject json = getJsonDataFromJS(driver, "AH");

		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overUnderOdds = getFullOddsHistoryOverUnder(json,
				booksMap);

		// convert to hashmap of lists of asianodds
		HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> asianOdds = new HashMap<>();
		for (Entry<Float, HashMap<String, ArrayList<OverUnderOdds>>> i : overUnderOdds.entrySet()) {
			float line = i.getKey();
			HashMap<String, ArrayList<AsianOdds>> lineMap = new HashMap<>();
			for (Entry<String, ArrayList<OverUnderOdds>> b : i.getValue().entrySet()) {
				String bookmaker = b.getKey();
				ArrayList<OverUnderOdds> oulist = b.getValue();
				ArrayList<AsianOdds> asianList = new ArrayList<>();
				for (OverUnderOdds ou : oulist)
					asianList.add(new AsianOdds(ou));

				lineMap.put(bookmaker, asianList);
			}
			asianOdds.put(line, lineMap);
		}

		return asianOdds;
	}

	private static HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> getOverUnderDataFromJS(WebDriver driver,
			HashMap<Integer, String> booksMap) throws Exception {

		JSONObject json = getJsonDataFromJS(driver, "O/U");

		return getFullOddsHistoryOverUnder(json, booksMap);

	}

	private static HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> getFullOddsHistoryOverUnder(
			JSONObject json, HashMap<Integer, String> booksMap) {

		HashMap<String, Float> underOutcomesID = new HashMap<>();
		HashMap<String, Float> overOutcomesID = new HashMap<>();

		ArrayList<OverUnderOdds> closingOdds = new ArrayList<>();

		// from odds data get outcomeid hashs
		getOutcomesIDandClosingOdds(json, underOutcomesID, overOutcomesID, closingOdds, booksMap);

		// map key is the line value -list of odds history (for over and under
		// outcomes)
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overHistoriesMap = new HashMap<>();
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> underHistoriesMap = new HashMap<>();

		JSONObject hist = json.getJSONObject("history");
		// JSONObject lay = hist.getJSONObject("lay");
		JSONObject back = hist.getJSONObject("back");

		Set<String> keys = back.keySet();
		for (String i : keys) {
			boolean isOver = true;
			Float line = -1f;
			// System.out.println(i);
			if (overOutcomesID.containsKey(i)) {
				line = overOutcomesID.get(i);
				isOver = true;
			} else if (underOutcomesID.containsKey(i)) {
				line = underOutcomesID.get(i);
				isOver = false;
			} else
				System.out.println("outcomeid not found!");

			// System.out.println(isOver ? "OVER " : "UNDER " + line);

			JSONObject outcome1 = back.getJSONObject(i);
			Set<String> bookies = outcome1.keySet();
			for (String b : bookies) {
				String bookmaker = booksMap.get(Integer.parseInt(b));
				// System.out.println(bookmaker);
				JSONArray history = outcome1.getJSONArray(b);
				ArrayList<OverUnderOdds> oddsHistory = new ArrayList<>();
				for (int j = 0; j < history.length(); j++) {
					JSONArray hentry = history.getJSONArray(j);
					float odds = (float) hentry.getDouble(0);
					// sometimes null sometimes integer unknown meaning
					Object unknown = hentry.get(1);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(hentry.getLong(2) * 1000);
					Date date = cal.getTime();
					// System.out.println(odds + " " + date);
					OverUnderOdds ouodds = new OverUnderOdds(bookmaker, date, line, isOver ? odds : -1f,
							isOver ? -1f : odds);
					// System.out.println(ouodds);
					oddsHistory.add(ouodds);

				}
				// oddsHistory.stream().forEach(System.out::println);

				if (isOver) {
					HashMap<String, ArrayList<OverUnderOdds>> booksSoFar = overHistoriesMap.get(line);
					if (booksSoFar == null) {
						HashMap<String, ArrayList<OverUnderOdds>> newBooksHash = new HashMap<>();
						newBooksHash.put(bookmaker, oddsHistory);
						overHistoriesMap.put(line, newBooksHash);
					} else {
						HashMap<String, ArrayList<OverUnderOdds>> newBooksHash = overHistoriesMap.get(line);
						newBooksHash.put(bookmaker, oddsHistory);
						overHistoriesMap.put(line, newBooksHash);
					}
				} else {
					HashMap<String, ArrayList<OverUnderOdds>> booksSoFar = underHistoriesMap.get(line);
					if (booksSoFar == null) {
						HashMap<String, ArrayList<OverUnderOdds>> newBooksHash = new HashMap<>();
						newBooksHash.put(bookmaker, oddsHistory);
						underHistoriesMap.put(line, newBooksHash);
					} else {
						HashMap<String, ArrayList<OverUnderOdds>> newBooksHash = underHistoriesMap.get(line);
						newBooksHash.put(bookmaker, oddsHistory);
						underHistoriesMap.put(line, newBooksHash);
					}
				}

			}
		}

		// add the closing odds before combining
		for (OverUnderOdds i : closingOdds)
			if (i.underOdds == -1f) {
				if (overHistoriesMap.get(i.line) == null)
					overHistoriesMap.put(i.line, new HashMap<>());

				if (overHistoriesMap.get(i.line).get(i.bookmaker) == null)
					overHistoriesMap.get(i.line).put(i.bookmaker, new ArrayList<>());

				if (!overHistoriesMap.get(i.line).get(i.bookmaker).contains(i))
					overHistoriesMap.get(i.line).get(i.bookmaker).add(i);
			} else if (i.overOdds == -1f) {

				if (underHistoriesMap.get(i.line) == null)
					underHistoriesMap.put(i.line, new HashMap<>());

				if (underHistoriesMap.get(i.line).get(i.bookmaker) == null)
					underHistoriesMap.get(i.line).put(i.bookmaker, new ArrayList<>());

				if (!underHistoriesMap.get(i.line).get(i.bookmaker).contains(i))
					underHistoriesMap.get(i.line).get(i.bookmaker).add(i);
			}

		// CLOSING odds are missing
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> historyDataOU = combineOddsHistories(overHistoriesMap,
				underHistoriesMap);

		return historyDataOU;
	}

	private static void getOutcomesIDandClosingOdds(JSONObject json, HashMap<String, Float> underOutcomesID,
			HashMap<String, Float> overOutcomesID, ArrayList<OverUnderOdds> closingOdds,
			HashMap<Integer, String> booksMap) {
		JSONObject oddsdata = json.getJSONObject("oddsdata");
		JSONObject backOdds = oddsdata.getJSONObject("back");
		Set<String> keysOdds = backOdds.keySet();
		for (String i : keysOdds) {
			JSONObject entry = backOdds.getJSONObject(i);
			float handicapValue = (float) entry.getDouble("handicapValue" + "");
			// System.out.println(handicapValue);
			Object outcomedID = entry.get("OutcomeID");
			if (outcomedID instanceof JSONArray) {
				JSONArray arr = (JSONArray) outcomedID;
				overOutcomesID.put(arr.getString(0), handicapValue);
				underOutcomesID.put(arr.getString(1), handicapValue);
			} else {
				JSONObject obj = (JSONObject) outcomedID;
				try {
					overOutcomesID.put(obj.getString("0"), handicapValue);
					underOutcomesID.put(obj.getString("1"), handicapValue);
				} catch (Exception e) {
					System.out.println("daad");
				}
			}

			JSONObject closingOddsObject = entry.getJSONObject("odds");
			JSONObject changeTime = entry.getJSONObject("change_time");
			Set<String> bookies = closingOddsObject.keySet();
			Calendar cal = Calendar.getInstance();

			for (String b : bookies) {
				String bookmaker = booksMap.get(Integer.parseInt(b));
				Object closingObjectJSON = closingOddsObject.get(b);
				float overOdds = -1f, underOdds = -1f;

				if (closingObjectJSON instanceof JSONArray) {
					overOdds = (float) closingOddsObject.getJSONArray(b).getDouble(0);
					underOdds = (float) closingOddsObject.getJSONArray(b).getDouble(1);
				} else {
					overOdds = (float) closingOddsObject.getJSONObject(b).getDouble("0");
					underOdds = (float) closingOddsObject.getJSONObject(b).getDouble("1");
				}

				Date timeOver = null, timeUnder = null;

				Object changeTimeJSON = changeTime.get(b);

				if (changeTimeJSON instanceof JSONArray) {
					cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(0) * 1000);
					timeOver = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(1) * 1000);
					timeUnder = cal.getTime();
				} else {
					cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("0") * 1000);
					timeOver = cal.getTime();
					cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("1") * 1000);
					timeUnder = cal.getTime();
				}

				OverUnderOdds closingOver = new OverUnderOdds(bookmaker, timeOver, handicapValue, overOdds, -1f)
						.withIsClosing();
				OverUnderOdds closingUnder = new OverUnderOdds(bookmaker, timeUnder, handicapValue, -1f, underOdds)
						.withIsClosing();
				closingOdds.add(closingOver);
				closingOdds.add(closingUnder);
			}

		}

	}

	private static HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> combineOddsHistories(
			HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overHistoriesMap,
			HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> underHistoriesMap) {

		if (overHistoriesMap.size() != underHistoriesMap.size())
			System.out.println("Histories map size differ");

		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> result = new HashMap<>();

		for (Entry<Float, HashMap<String, ArrayList<OverUnderOdds>>> i : overHistoriesMap.entrySet()) {
			float line = i.getKey();
			HashMap<String, ArrayList<OverUnderOdds>> overBookMap = i.getValue();
			HashMap<String, ArrayList<OverUnderOdds>> underBookMap = underHistoriesMap.get(line);

			if (overBookMap.size() != underBookMap.size())
				System.out.println("Histories map size differ for line: " + line);

			HashMap<String, ArrayList<OverUnderOdds>> resultBookMap = new HashMap<>();
			for (Entry<String, ArrayList<OverUnderOdds>> b : overBookMap.entrySet()) {
				String bookie = b.getKey();
				ArrayList<OverUnderOdds> overOdds = b.getValue();
				ArrayList<OverUnderOdds> underOdds = underBookMap.get(bookie);

				overOdds.sort(Comparator.comparing(OverUnderOdds::getTime).reversed());
				underOdds.sort(Comparator.comparing(OverUnderOdds::getTime).reversed());

				if (!overOdds.get(overOdds.size() - 1).time.equals(overOdds.get(overOdds.size() - 1).time))
					System.out.println("Opening  odds dates differ: " + line + " at " + bookie);

				ArrayList<OverUnderOdds> oulist = new ArrayList<>();
				OverUnderOdds openingOver = overOdds.remove(overOdds.size() - 1);
				OverUnderOdds openingUnder = underOdds.remove(underOdds.size() - 1);
				OverUnderOdds opening = new OverUnderOdds(bookie, openingOver.time, line, openingOver.overOdds,
						openingUnder.underOdds).withIsOpening();

				// in the corner case where there is no change in odds, i.e.
				// opening and closing are the same
				if (overOdds.isEmpty() && underOdds.isEmpty())
					opening.isClosing = true;

				// corner case only one of the histories doesn't change, but the
				// others do, for example draw odds are the same for opening and
				if (overOdds.isEmpty())
					overOdds.add(openingOver.withIsClosing());
				if (underOdds.isEmpty())
					underOdds.add(openingUnder.withIsClosing());

				oulist.add(opening);

				TreeSet<Date> changeTimes = new TreeSet<>();
				changeTimes.addAll(overOdds.stream().map(v -> v.time).collect(Collectors.toSet()));
				changeTimes.addAll(underOdds.stream().map(v -> v.time).collect(Collectors.toSet()));

				Map<Date, OverUnderOdds> overMap = overOdds.stream()
						.collect(Collectors.toMap(OverUnderOdds::getTime, Function.identity(), (p1, p2) -> p1));
				Map<Date, OverUnderOdds> underMap = underOdds.stream()
						.collect(Collectors.toMap(OverUnderOdds::getTime, Function.identity(), (p1, p2) -> p1));

				TreeSet<Date> set = new TreeSet<>();
				if (!overOdds.isEmpty())
					set.add(overOdds.get(overOdds.size() - 1).getTime());
				if (!underOdds.isEmpty())
					set.add(underOdds.get(underOdds.size() - 1).getTime());

				Date last = set.last();

				for (Date t : changeTimes.tailSet(last)) {
					boolean drawIsNull = overMap.get(t) != null;
					boolean awayIsNull = underMap.get(t) != null;

					OverUnderOdds over = drawIsNull ? overMap.get(t)
							: overMap.get(new TreeMap<Date, OverUnderOdds>(overMap).headMap(t).lastKey());
					OverUnderOdds under = awayIsNull ? underMap.get(t)
							: underMap.get(new TreeMap<Date, OverUnderOdds>(underMap).headMap(t).lastKey());

					OverUnderOdds newm = new OverUnderOdds(bookie, t, line, over.overOdds, under.underOdds);
					newm.isClosing = over.isClosing && under.isClosing;
					if (!oulist.contains(newm))
						oulist.add(newm);
				}
				resultBookMap.put(bookie, oulist);
			}
			result.put(line, resultBookMap);
		}
		return result;
	}

	private static HashMap<Integer, String> getBookmakersMap(String bookmakerHash) {
		HashMap<Integer, String> result = new HashMap<>();
		JSONObject json = new JSONObject(bookmakerHash);
		Set<String> keys = json.keySet();
		for (String i : keys) {
			String name = json.getJSONObject(i).getString("WebName");
			result.put(Integer.parseInt(i), name);
		}
		return result;

	}

	public static FullFixture getFullFixtureOUValue(WebDriver driver, String i, String competition,
			ArrayList<FinalEntry> predictions, int day, int month) throws ParseException, InterruptedException {
		// stupid hash for search
		HashMap<ExtendedFixture, FinalEntry> map = new HashMap<>();
		for (FinalEntry pred : predictions) {
			map.put(pred.fixture, pred);
		}

		driver.navigate().to(i);

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		dateString = dateString.split(",")[1] + dateString.split(",")[2];
		Date date = FORMATFULL.parse(dateString);

		FinalEntry prediction = map.get(new ExtendedFixture(date, home, away, new Result(-1, -1), i));

		LocalDate searchDate = LocalDate.of(2017, month, day);
		if (!date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(searchDate)
				|| date.before(new Date()))
			return null;

		System.out.println(home + " : " + away);
		System.out.println(date);

		// overUnderOverPinnacle(driver, prediction);
		System.out.println("------------------------------------------------------");

		// asianOverPinnacle(driver);
		// System.out.println("========================================================");
		return null;

	}

	private static void asianOverPinnacle(WebDriver driver) {
		long start = System.currentTimeMillis();
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs) {
			if (t.getText().contains("AH")) {
				t.click();
				break;
			}
		}

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

		if (opt != null)

		{
			int lower = (indexOfOptimal - 5) < 0 ? 0 : (indexOfOptimal - 5);
			int higher = (indexOfOptimal + 5) > (divsAsian.size() - 1) ? (divsAsian.size() - 1) : (indexOfOptimal + 5);

			for (int j = lower; j <= higher; j++) {
				WebElement currentDiv = divsAsian.get(j);
				if (currentDiv == null || currentDiv.getText().split("\n").length < 3)
					continue;

				// currentDiv.click();
				Actions actions = new Actions(driver);
				actions.moveToElement(currentDiv).click().perform();

				WebElement AHTable = currentDiv.findElement(By.xpath("//table"));

				List<WebElement> rowsGoals = AHTable.findElements(By.xpath("//tbody/tr"));
				float line = -1f, home = -1f, away = -1f;

				Odds pinnOdds = null;

				ArrayList<Odds> matchOdds = new ArrayList<>();
				for (WebElement row : rowsGoals) {
					String rowText = row.getText();
					if (row.getText().contains("Average"))
						break;
					String[] oddsArray = rowText.split("\n");
					// System.out.println(rowText);
					if (oddsArray.length != 5)
						continue;
					String bookmaker = oddsArray[0].trim();

					if (Arrays.asList(Constants.FAKEBOOKS).contains(bookmaker) || bookmaker.isEmpty())
						continue;

					try {
						line = Float.parseFloat(oddsArray[1].trim());
						home = Float.parseFloat(oddsArray[2].trim());
						away = Float.parseFloat(oddsArray[3].trim());
					} catch (Exception e) {
						continue;
					}

					Odds modds = new AsianOdds(bookmaker, new Date(), line, home, away);
					matchOdds.add(modds);

					if (bookmaker.equals("Pinnacle"))
						pinnOdds = modds;

				}

				checkValueOverPinnacleOdds(matchOdds, pinnOdds);

				List<WebElement> closeLink = currentDiv.findElements(By.className("odds-co"));
				if (!closeLink.isEmpty()) {
					actions.moveToElement(closeLink.get(0)).click().perform();
				}

			}
		}
		System.out.println("asian total time " + (System.currentTimeMillis() - start) / 1000d + "sec");
	}

	private static ArrayList<MatchOdds> fullMatchOddsOverPinnacle(WebDriver driver, Date date)
			throws InterruptedException, ParseException {
		long start = System.currentTimeMillis();
		ArrayList<MatchOdds> result = new ArrayList<>();
		WebElement table = driver.findElement(By.xpath("//div[@id='odds-data-table']"));
		List<WebElement> rows = table.findElements(By.tagName("tr"));
		Odds pinnOdds = null;

		ArrayList<Odds> matchOdds = new ArrayList<>();
		for (WebElement row : rows) {
			if (row.getText().contains("Average"))
				break;
			// System.out.println(row.getText());
			List<WebElement> columns = row.findElements(By.tagName("td"));
			if (columns.size() < 4)
				continue;

			String bookmaker = columns.get(0).getText().trim();
			if (Arrays.asList(Constants.FAKEBOOKS).contains(bookmaker))
				continue;

			// hover for odds history
			Actions ToolTip1 = new Actions(driver);
			WebElement homeElement = columns.get(1);
			WebElement drawElement = columns.get(2);
			WebElement awayElement = columns.get(3);

			ToolTip1.moveToElement(homeElement).clickAndHold(homeElement).perform();
			WebElement hover = homeElement.findElement(By.xpath("//*[@id='tooltiptext']"));
			String homeText = hover.getText();

			Thread.sleep(200);

			ToolTip1.moveToElement(drawElement).clickAndHold(drawElement).perform();
			hover = drawElement.findElement(By.xpath("//*[@id='tooltiptext']"));
			String drawText = hover.getText();

			Thread.sleep(200);
			ToolTip1.moveToElement(awayElement).clickAndHold(awayElement).perform();
			hover = awayElement.findElement(By.xpath("//*[@id='tooltiptext']"));
			String awayText = hover.getText();

			Thread.sleep(200);
			TreeSet<MatchOdds> oddsHistory = getMatchOddsHistory(homeText, drawText, awayText, date, bookmaker);

			System.out.println(bookmaker + " " + oddsHistory.size());

			if (oddsHistory.isEmpty())
				System.out.println(" sempt");

			MatchOdds modds = oddsHistory.last();
			matchOdds.add(modds);
			result.add(modds);

			if (bookmaker.equals("Pinnacle"))
				pinnOdds = modds;

			// System.out.println(modds);
		}

		System.out.println("match odds total time " + (System.currentTimeMillis() - start) / 1000d + "sec");

		checkValueOverPinnacleOdds(matchOdds, pinnOdds);
		return result;

	}

	private static TreeSet<MatchOdds> getMatchOddsHistory(String text, String drawText, String awayText, Date date,
			String bookmaker) throws ParseException {
		TreeSet<MatchOdds> result = new TreeSet<>(Comparator.comparing(MatchOdds::getTime));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);

		MatchOdds openingHome = getOpeningOdds(text, date, year);
		MatchOdds openingDraw = getOpeningOdds(drawText, date, year);
		MatchOdds openingAway = getOpeningOdds(awayText, date, year);

		if (!((openingHome.time.equals(openingDraw.time) && openingDraw.time.equals(openingAway.time))))
			System.out.println("possible odds history time mismatch");

		MatchOdds opening = new MatchOdds(bookmaker, openingHome.time, openingHome.homeOdds, openingDraw.homeOdds,
				openingAway.homeOdds);

		ArrayList<MatchOdds> homeHistory = getOddsHistory(text, date, year);
		ArrayList<MatchOdds> drawHistory = getOddsHistory(drawText, date, year);
		ArrayList<MatchOdds> awayHistory = getOddsHistory(awayText, date, year);

		TreeSet<Date> changeTimes = new TreeSet<>();
		changeTimes.addAll(homeHistory.stream().map(v -> v.time).collect(Collectors.toSet()));
		changeTimes.addAll(drawHistory.stream().map(v -> v.time).collect(Collectors.toSet()));
		changeTimes.addAll(awayHistory.stream().map(v -> v.time).collect(Collectors.toSet()));

		Map<Date, MatchOdds> homeMap = homeHistory.stream()
				.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));
		Map<Date, MatchOdds> drawMap = drawHistory.stream()
				.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));
		Map<Date, MatchOdds> awayMap = awayHistory.stream()
				.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));

		Date last = getLastDateFromOddsHistories(homeHistory, drawHistory, awayHistory);

		result.add(opening);
		homeMap.put(opening.getTime(), openingHome);
		drawMap.put(opening.getTime(), openingDraw);
		awayMap.put(opening.getTime(), openingAway);

		for (Date t : changeTimes.tailSet(last)) {
			boolean homeIsNull = homeMap.get(t) != null;
			boolean drawIsNull = drawMap.get(t) != null;
			boolean awayIsNull = awayMap.get(t) != null;

			Float homeOdds = homeIsNull ? homeMap.get(t).homeOdds
					: homeMap.get(new TreeMap<Date, MatchOdds>(homeMap).headMap(t).lastKey()).homeOdds;
			Float drawOdds = drawIsNull ? drawMap.get(t).homeOdds
					: drawMap.get(new TreeMap<Date, MatchOdds>(drawMap).headMap(t).lastKey()).homeOdds;
			Float awayOdds = awayIsNull ? awayMap.get(t).homeOdds
					: awayMap.get(new TreeMap<Date, MatchOdds>(awayMap).headMap(t).lastKey()).homeOdds;
			MatchOdds newm = new MatchOdds(bookmaker, t, homeOdds, drawOdds, awayOdds);
			result.add(newm);
		}
		// System.out.println(result.size());
		// result.forEach(System.out::println);
		return result;
	}

	/**
	 * Helper method for getting a start date for determening oddshistory with
	 * different change times for 1x2 odds
	 * 
	 * @param homeHistory
	 * @param drawHistory
	 * @param awayHistory
	 * @return
	 */
	private static Date getLastDateFromOddsHistories(ArrayList<MatchOdds> homeHistory, ArrayList<MatchOdds> drawHistory,
			ArrayList<MatchOdds> awayHistory) {
		TreeSet<Date> set = new TreeSet<>();
		if (!homeHistory.isEmpty())
			set.add(homeHistory.get(homeHistory.size() - 1).getTime());
		if (!drawHistory.isEmpty())
			set.add(drawHistory.get(drawHistory.size() - 1).getTime());
		if (!awayHistory.isEmpty())
			set.add(awayHistory.get(awayHistory.size() - 1).getTime());

		return set.last();
	}

	private static ArrayList<MatchOdds> getOddsHistory(String text, Date date, int year) throws ParseException {
		ArrayList<MatchOdds> result = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		String[] homeText = text.split("Opening odds:");
		String oddsText = homeText[0].trim();
		String[] list = oddsText.split("\n");

		for (String i : list) {
			String[] splitted = i.split(" ");
			if (splitted.length < 3)
				continue;
			Float openingOdds = -1f;
			try {
				openingOdds = Float.parseFloat(splitted[3].trim());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			String dateOpeningString = splitted[0] + " " + splitted[1] + " " + year + " " + splitted[2];

			Date openingDate = FORMATFULL.parse(dateOpeningString.replace(",", ""));
			if (openingDate.after(date)) {
				cal.setTime(openingDate);
				cal.set(Calendar.YEAR, year - 1);
				openingDate = cal.getTime();
			}

			result.add(new MatchOdds("", openingDate, openingOdds, -1f, -1f));
		}
		return result;
	}

	private static MatchOdds getOpeningOdds(String text, Date date, int year) throws ParseException {
		Calendar cal = Calendar.getInstance();
		String[] homeText = text.split("Opening odds:");
		if (homeText.length < 2) {
			System.out.println("lengs");
			return null;
		}
		String openingOddsText = homeText[1].trim();
		String[] splitted = openingOddsText.split(" ");
		Float openingOdds = Float.parseFloat(splitted[splitted.length - 1].trim());
		String dateOpeningString = splitted[0] + " " + splitted[1] + " " + year + " " + splitted[2];

		Date openingDate = FORMATFULL.parse(dateOpeningString.replace(",", ""));
		if (openingDate.after(date)) {
			cal.setTime(openingDate);
			cal.set(Calendar.YEAR, year - 1);
			openingDate = cal.getTime();
		}

		return new MatchOdds("", openingDate, openingOdds, -1f, -1f);
	}

	public static ArrayList<OverUnderOdds> overUnderOverPinnacle(WebDriver driver, Date date)
			throws InterruptedException, ParseException {
		long start = System.currentTimeMillis();

		HashMap<Float, ArrayList<OverUnderOdds>> result = new HashMap<>();
		navigateToTab(driver, "O/U");

		WebElement optGoals = null;
		float minGoals = 100f;
		List<WebElement> divsGoals = driver.findElements(By.xpath("//*[@id='odds-data-table']/div"));
		for (WebElement div : divsGoals) {
			String text = div.getText();
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

		if (optGoals == null)
			return null;
		int lower = (indexOfOptimalGoals - 6) < 0 ? 0 : (indexOfOptimalGoals - 6);
		int higher = (indexOfOptimalGoals + 6) > (divsGoals.size() - 1) ? (divsGoals.size() - 1)
				: (indexOfOptimalGoals + 6);

		for (int j = lower; j <= higher; j++) {
			WebElement currentDiv = divsGoals.get(j);
			if (currentDiv == null || currentDiv.getText().split("\n").length < 3
					|| currentDiv.getText().contains("EXCHANGES"))
				continue;

			float line = Float.parseFloat(currentDiv.getText().split("\n")[0].split(" ")[1]);
			System.out.println(line);

			Actions actions = new Actions(driver);
			actions.moveToElement(currentDiv).click().perform();

			WebElement goalLineTable = currentDiv.findElement(By.cssSelector("table.table-main.detail-odds"));

			// find the row
			List<WebElement> rowsGoals = goalLineTable.findElements(By.tagName("tr"));
			System.out.println("size  " + rowsGoals.size());
			float over = -1f, under = -1f;

			Odds pinnOdds = null;

			ArrayList<Odds> matchOdds = new ArrayList<>();
			for (WebElement row : rowsGoals) {
				String rowText = row.getText();
				// System.out.println(rowText);
				if (row.getText().contains("Average"))
					break;
				String[] oddsArray = rowText.split("\n");
				if (oddsArray.length != 5)
					continue;
				String bookmaker = oddsArray[0].trim();

				if (Arrays.asList(Constants.FAKEBOOKS).contains(bookmaker) || bookmaker.isEmpty())
					continue;

				List<WebElement> columns = row.findElements(By.tagName("td"));
				// hover for odds history
				Actions ToolTip1 = new Actions(driver);
				WebElement overElement = columns.get(2);
				WebElement underElement = columns.get(3);

				ToolTip1.moveToElement(overElement).clickAndHold(overElement).perform();
				WebElement hover = overElement.findElement(By.xpath("//*[@id='tooltiptext']"));
				String drawText = hover.getText();

				Thread.sleep(200);
				ToolTip1.moveToElement(underElement).clickAndHold(underElement).perform();
				hover = underElement.findElement(By.xpath("//*[@id='tooltiptext']"));
				String awayText = hover.getText();

				Thread.sleep(200);
				TreeSet<OverUnderOdds> oddsHistory = getOUOddsHistory(line, drawText, awayText, date, bookmaker);

				System.out.println(bookmaker + " O/U " + line + " " + oddsHistory.size());

				try {
					line = Float.parseFloat(oddsArray[1].trim());
					over = Float.parseFloat(oddsArray[2].trim());
					under = Float.parseFloat(oddsArray[3].trim());
				} catch (Exception e) {
					continue;
				}

				Odds modds = new OverUnderOdds(bookmaker, new Date(), line, over, under);
				matchOdds.add(modds);

				if (bookmaker.equals("Pinnacle"))
					pinnOdds = modds;

				// System.out.println(modds);
			}

			// checkValueOverPinnacleOdds(matchOdds, pinnOdds);

			// checkValueOverPinnacleOddsWithPrediction(matchOdds, pinnOdds,
			// date);

			List<WebElement> closeLink = currentDiv.findElements(By.className("odds-co"));
			if (!closeLink.isEmpty()) {
				actions.moveToElement(closeLink.get(0)).click().perform();
			}
		}

		System.out.println("over under total time " + (System.currentTimeMillis() - start) / 1000d + "sec");
		return null;

	}

	/**
	 * Returns sorted set of the history of the O/U odds for the given line and
	 * bookmaker
	 * 
	 * @param line
	 * @param drawText
	 * @param awayText
	 * @param date
	 * @param bookmaker
	 * @return
	 * @throws ParseException
	 */
	private static TreeSet<OverUnderOdds> getOUOddsHistory(float line, String overText, String underText, Date date,
			String bookmaker) throws ParseException {
		TreeSet<OverUnderOdds> result = new TreeSet<>(Comparator.comparing(OverUnderOdds::getTime));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);

		MatchOdds openingOver = getOpeningOdds(overText, date, year);
		MatchOdds openingUnder = getOpeningOdds(underText, date, year);

		if (!openingOver.time.equals(openingOver.time))
			System.out.println("possible odds history time mismatch for o/u" + line + " " + bookmaker);

		OverUnderOdds opening = new OverUnderOdds(bookmaker, openingOver.time, line, openingOver.homeOdds,
				openingUnder.homeOdds);

		ArrayList<MatchOdds> overHistory = getOddsHistory(overText, date, year);
		ArrayList<MatchOdds> underHistory = getOddsHistory(underText, date, year);

		TreeSet<Date> changeTimes = new TreeSet<>();
		changeTimes.addAll(overHistory.stream().map(v -> v.time).collect(Collectors.toSet()));
		changeTimes.addAll(underHistory.stream().map(v -> v.time).collect(Collectors.toSet()));

		Map<Date, MatchOdds> drawMap = overHistory.stream()
				.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));
		Map<Date, MatchOdds> awayMap = underHistory.stream()
				.collect(Collectors.toMap(MatchOdds::getTime, Function.identity(), (p1, p2) -> p1));

		result.add(opening);
		drawMap.put(opening.getTime(), openingOver);
		awayMap.put(opening.getTime(), openingUnder);

		if (changeTimes.isEmpty())
			return result;

		Date last = getLastDateFromOddsHistories(new ArrayList<>(), overHistory, underHistory);

		for (Date t : changeTimes.tailSet(last)) {
			boolean drawIsNull = drawMap.get(t) != null;
			boolean awayIsNull = awayMap.get(t) != null;

			Float overOdds = drawIsNull ? drawMap.get(t).homeOdds
					: drawMap.get(new TreeMap<Date, MatchOdds>(drawMap).headMap(t).lastKey()).homeOdds;
			Float underOdds = awayIsNull ? awayMap.get(t).homeOdds
					: awayMap.get(new TreeMap<Date, MatchOdds>(awayMap).headMap(t).lastKey()).homeOdds;

			OverUnderOdds newm = new OverUnderOdds(bookmaker, t, line, overOdds, underOdds);
			// System.out.println(newm);
			result.add(newm);
		}
		// System.out.println(result.size());
		// result.forEach(System.out::println);
		return result;

	}

	/**
	 * Helper method for navigating to tab in oddsportal
	 * 
	 * @param driver
	 * @param tabName
	 */
	private static void navigateToTab(WebDriver driver, String tabName) {
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs)
			if (t.getText().contains(tabName)) {
				t.click();
				break;
			}
	}

	private static void checkValueOverPinnacleOddsWithPrediction(ArrayList<Odds> matchOdds, Odds pinnOdds,
			FinalEntry prediction) {
		if (matchOdds.isEmpty() || pinnOdds == null)
			return;

		boolean compareToTrueOdds = false;
		Odds trueOdds = pinnOdds.getTrueOddsMarginal();

		if (matchOdds.get(0) instanceof OverUnderOdds) {
			OverUnderOdds trueOverUnderOdds = (OverUnderOdds) trueOdds;
			OverUnderOdds pinnOverUnderOdds = (OverUnderOdds) pinnOdds;
			for (Odds i : matchOdds) {
				OverUnderOdds m = (OverUnderOdds) i;
				if (prediction.isOver() && m.overOdds >= (compareToTrueOdds ? trueOverUnderOdds.overOdds
						: pinnOverUnderOdds.overOdds)) {
					String msg = i.bookmaker + " O " + m.line + " at " + m.overOdds + " true: "
							+ Utils.format(trueOverUnderOdds.overOdds) + " "
							+ Utils.format(100 * m.overOdds / trueOverUnderOdds.overOdds - 100) + "%";
					if (m.overOdds > trueOverUnderOdds.overOdds)
						System.err.println(msg);
					else
						System.out.println(msg);
				}
				if (prediction.isUnder() && m.underOdds >= (compareToTrueOdds ? trueOverUnderOdds.underOdds
						: pinnOverUnderOdds.underOdds)) {
					String msg = i.bookmaker + " U " + m.line + " at " + m.underOdds + " true: "
							+ Utils.format(trueOverUnderOdds.underOdds) + " "
							+ Utils.format(100 * m.underOdds / trueOverUnderOdds.underOdds - 100) + "%";
					if (m.underOdds > trueOverUnderOdds.underOdds)
						System.err.println(msg);
					else
						System.out.println(msg);
				}
			}
		}

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
		Date date = FORMATFULL.parse(dateString);

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
		List<WebElement> rows = table.findElements(By.xpath("//div[1]/table/tbody/tr"));
		Odds pinnOdds = null;

		ArrayList<Odds> matchOdds = new ArrayList<>();
		for (WebElement row : rows) {
			List<WebElement> columns = row.findElements(By.xpath("td"));
			if (columns.size() < 4)
				continue;
			String bookmaker = columns.get(0).getText().trim();
			if (Arrays.asList(Constants.FAKEBOOKS).contains(bookmaker))
				continue;
			float homeOdds = Float.parseFloat(columns.get(1).getText().trim());
			float drawOdds = Float.parseFloat(columns.get(2).getText().trim());
			float awayOdds = Float.parseFloat(columns.get(3).getText().trim());

			Odds modds = new MatchOdds(bookmaker, new Date(), homeOdds, drawOdds, awayOdds);
			matchOdds.add(modds);

			if (bookmaker.equals("Pinnacle"))
				pinnOdds = modds;

			// System.out.println(modds);
		}

		checkValueOverPinnacleOdds(matchOdds, pinnOdds);

		// Over and under odds
		float overOdds = -1f, underOdds = -1f;
		List<WebElement> tabs = driver.findElements(By.xpath("//*[@id='bettype-tabs']/ul/li"));
		for (WebElement t : tabs)

		{
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
		for (WebElement div : divsGoals)

		{
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

		if (optGoals != null)

		{
			int lower = (indexOfOptimalGoals - 6) < 0 ? 0 : (indexOfOptimalGoals - 6);
			int higher = (indexOfOptimalGoals + 6) > (divsGoals.size() - 1) ? (divsGoals.size() - 1)
					: (indexOfOptimalGoals + 6);

			long startt = System.currentTimeMillis();
			for (int j = lower; j <= higher; j++) {
				WebElement currentDiv = divsGoals.get(j);
				if (currentDiv == null || currentDiv.getText().split("\n").length < 3)
					continue;

				Actions actions = new Actions(driver);
				actions.moveToElement(currentDiv).click().perform();
				// currentDiv.click();
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
				if (!closeLink.isEmpty()) {
					// closeLink.get(0).click();
					actions.moveToElement(closeLink.get(0)).click().perform();
				}

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
		for (

		WebElement t : tabs)

		{
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
		for (WebElement div : divsAsian)

		{
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

		if (opt != null)

		{
			int lower = (indexOfOptimal - 5) < 0 ? 0 : (indexOfOptimal - 5);
			int higher = (indexOfOptimal + 5) > (divsAsian.size() - 1) ? (divsAsian.size() - 1) : (indexOfOptimal + 5);

			for (int j = lower; j <= higher; j++) {
				WebElement currentDiv = divsAsian.get(j);
				if (currentDiv == null || currentDiv.getText().split("\n").length < 3)
					continue;

				// currentDiv.click();
				Actions actions = new Actions(driver);
				actions.moveToElement(currentDiv).click().perform();

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
				if (!closeLink.isEmpty()) {

					actions.moveToElement(closeLink.get(0)).click().perform();
					// closeLink.get(0).click();
				}

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
				.with1X2Odds(-1f, -1f, -1f).withAsian(asianLines.main.line, asianLines.main.home, asianLines.main.away)
				.withOdds(overOdds, underOdds, overOdds, underOdds).withShots(-1, -1);

		return ((FullFixture) ef).withAsianLines(asianLines).withGoalLines(GLS);

	}

	private static void checkValueOverPinnacleOdds(ArrayList<Odds> matchOdds, Odds pinnOdds) {
		if (matchOdds.isEmpty() || pinnOdds == null)
			return;

		boolean compareToTrueOdds = true;
		Odds trueOdds = pinnOdds.getTrueOddsMarginal();

		if (matchOdds.get(0) instanceof MatchOdds) {
			MatchOdds trueMatchOdds = (MatchOdds) trueOdds;
			MatchOdds pinnMatchOdds = (MatchOdds) pinnOdds;
			List<MatchOdds> casted = matchOdds.stream().map(MatchOdds.class::cast).collect(Collectors.toList());

			casted.sort(Comparator.comparing(MatchOdds::getHomeOdds).reversed());
			casted.stream()
					.filter(m -> m.homeOdds > (compareToTrueOdds ? trueMatchOdds.homeOdds : pinnMatchOdds.homeOdds))
					.forEach(i -> System.out.println(
							i.bookmaker + " 1 at " + i.homeOdds + " true: " + Utils.format(trueMatchOdds.homeOdds) + " "
									+ Utils.format(100 * i.homeOdds / trueMatchOdds.homeOdds - 100) + "%"));
			casted.sort(Comparator.comparing(MatchOdds::getDrawOdds).reversed());
			casted.stream()
					.filter(m -> m.drawOdds > (compareToTrueOdds ? trueMatchOdds.drawOdds : pinnMatchOdds.drawOdds))
					.forEach(i -> System.out.println(
							i.bookmaker + " X at " + i.drawOdds + " true: " + Utils.format(trueMatchOdds.drawOdds) + " "
									+ Utils.format(100 * i.drawOdds / trueMatchOdds.drawOdds - 100) + "%"));
			casted.sort(Comparator.comparing(MatchOdds::getAwayOdds).reversed());
			casted.stream()
					.filter(m -> m.awayOdds > (compareToTrueOdds ? trueMatchOdds.awayOdds : pinnMatchOdds.awayOdds))
					.forEach(i -> System.out.println(
							i.bookmaker + " 2 at " + i.awayOdds + " true: " + Utils.format(trueMatchOdds.awayOdds) + " "
									+ Utils.format(100 * i.awayOdds / trueMatchOdds.awayOdds - 100) + "%"));

		}

		if (matchOdds.get(0) instanceof AsianOdds) {
			AsianOdds trueAsianOdds = (AsianOdds) trueOdds;
			AsianOdds pinnAsianOdds = (AsianOdds) pinnOdds;
			for (Odds i : matchOdds) {
				AsianOdds m = (AsianOdds) i;
				if (m.homeOdds > (compareToTrueOdds ? trueAsianOdds.homeOdds : pinnAsianOdds.homeOdds))
					System.out.println(i.bookmaker + " H " + m.line + " at " + m.homeOdds + " true: "
							+ Utils.format(trueAsianOdds.homeOdds) + " "
							+ Utils.format(100 * m.homeOdds / trueAsianOdds.homeOdds - 100) + "%");
				if (m.awayOdds > (compareToTrueOdds ? trueAsianOdds.awayOdds : pinnAsianOdds.awayOdds))
					System.out.println(i.bookmaker + " A " + m.line + " at " + m.awayOdds + " true: "
							+ Utils.format(trueAsianOdds.awayOdds) + " "
							+ Utils.format(100 * m.awayOdds / trueAsianOdds.awayOdds - 100) + "%");
			}
		}

		if (matchOdds.get(0) instanceof OverUnderOdds) {
			OverUnderOdds trueOverUnderOdds = (OverUnderOdds) trueOdds;
			OverUnderOdds pinnOverUnderOdds = (OverUnderOdds) pinnOdds;
			for (Odds i : matchOdds) {
				OverUnderOdds m = (OverUnderOdds) i;
				if (m.overOdds > (compareToTrueOdds ? trueOverUnderOdds.overOdds : pinnOverUnderOdds.overOdds))
					System.out.println(i.bookmaker + " O " + m.line + " at " + m.overOdds + " true: "
							+ Utils.format(trueOverUnderOdds.overOdds) + " "
							+ Utils.format(100 * m.overOdds / trueOverUnderOdds.overOdds - 100) + "%");
				if (m.underOdds > (compareToTrueOdds ? trueOverUnderOdds.underOdds : pinnOverUnderOdds.underOdds))
					System.out.println(i.bookmaker + " U " + m.line + " at " + m.underOdds + " true: "
							+ Utils.format(trueOverUnderOdds.underOdds) + " "
							+ Utils.format(100 * m.underOdds / trueOverUnderOdds.underOdds - 100) + "%");
			}
		}

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
