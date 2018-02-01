package scraper;

import java.io.File;

import java.util.Optional;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import constants.Constants;
import entries.FinalEntry;
import main.Fixture;
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
import utils.FixtureListCombiner;
import utils.Pair;
import utils.ThrowingSupplier;
import utils.Utils;
import xls.XlSUtils;

public class Scraper {
	public static final DateFormat OPTAFORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
	public static final DateFormat FORMATFULL = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
	public static final DateFormat ODDSHISTORYFORMAT = new SimpleDateFormat("dd MMMM HH:mm", Locale.US);
	public static final String BASE = "http://int.soccerway.com/";
	public static final String OUSUFFIX = "#over-under;2;2.50;0";
	public static final int CURRENT_YEAR = 2017;

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();

		// =================================================================

		// ArrayList<Fixture> eng = SQLiteJDBC.selectFixtures("ENG", 2016);
		// System.out.println(eng.size());
		// eng.stream().limit(20).collect(Collectors.toList()).forEach(System.out::println);

		// ====================================================================

		// for (int i = 2017; i <= 2017; i++)
		// GameStatsCollector.of("ENG3", i).collectAndStore();

		for (int i = 2017; i <= 2017; i++)
			FullOddsCollector.of("ENG", i).collectAndStore();

		// ArrayList<Fixture> shotsList = collect("ENG", 2016, null);
		// list.addAll(collect("JP", 2016,
		// "http://int.soccerway.com/national/japan/j1-league/2016/2nd-stage/"));
		// shotsList = new ArrayList<>();
		// XlSUtils.storeInExcel(shotsList, "BRA", 2017, "manual");

		//
		// ArrayList<Fixture> list = oddsInParallel("ENG", 2013, null);

		// ArrayList<Fixture> list = odds("BRA", 2017, null);
		// XlSUtils.storeInExcel(list, "BRA", 2017, "odds");
		// nextMatches("SPA", null, OnlyTodayMatches.TRUE);
		// fastOdds("ENG", 2017, null);

		// ArrayList<Fixture> list3 = fullOdds("ENG", 2010, null);
		// SQLiteJDBC.storePlayerFixtures(list3);

		// ArrayList<Fixture> list2 = fullOdds("SPA", 2013,
		// "http://www.oddsportal.com/soccer/spain/primera-division-2013-2014");
		// XlSUtils.storeInExcelFull(list2, "SPA", 2013, "fullodds");

		// XlSUtils.combine("BRA", 2017, "manual");
		// XlSUtils.combineFull("SPA", 2015, "all-data");
		// ////
		// XlSUtils.fillMissingShotsData("BRA", 2017, false);

		// ArrayList<Fixture> next = nextMatches("BRB", null);
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
	 * Helper method for collecting and storing PFS from a a single fixture due to
	 * some sort of bug in the collection process of collectfull method which misses
	 * only 1 fixture for some reason
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

		Document page = Jsoup.connect("http://www.soccerway.com/matches/2018/" + (month >= 10 ? month : ("0" + month))
				+ "/" + (day >= 10 ? day : ("0" + day)) + "/").timeout(0).get();

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

	// TODO refactor
	public static void checkAndUpdate(String competition, OnlyTodayMatches onlyTodaysMatches)
			throws IOException, ParseException, InterruptedException {
		String base = new File("").getAbsolutePath();
		int collectYear = Arrays.asList(EntryPoints.SUMMER).contains(competition) ? EntryPoints.SUMMERCURRENT
				: EntryPoints.CURRENT;

		FileInputStream file = new FileInputStream(new File(base + "/data/odds" + collectYear + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sh = workbook.getSheet(competition);

		ArrayList<Fixture> all = sh == null ? new ArrayList<>() : XlSUtils.selectAll(sh, 0);
		// problem when no pendingma fixtures?
		Date oldestTocheck = Utils.findLastPendingFixture(all);
		System.out.println(oldestTocheck);
		workbook.close();

		ArrayList<Fixture> toAdd = new ArrayList<>();
		ArrayList<Fixture> combined = new ArrayList<>();
		// check if update of previous results is necessary
		if (new Date().after(oldestTocheck)) {
			ArrayList<Fixture> odds = oddsUpToDate(competition, collectYear, oldestTocheck, null);
			System.out.println(odds.size() + " odds ");

			ArrayList<Fixture> list = new ArrayList<>();
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

			FixtureListCombiner combiner = new FixtureListCombiner(odds, list, competition);
			combined = combiner.combineWithDictionary();

			System.out.println(combined.size() + " combined");
			System.out.println(competition + " "
					+ (combined.size() == list.size() ? " combined successfull" : " combined failed"));

			toAdd.addAll(combined);
		}

		// add the combined(updated) fixtures to the list of all finished
		// fixtures
		for (Fixture i : all) {
			boolean continueFlag = false;
			for (Fixture comb : combined) {
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
		ArrayList<Fixture> next = new ArrayList<>();

		next = nextMatches(competition, null, onlyTodaysMatches);

		ArrayList<Fixture> withNext = new ArrayList<>();

		for (Fixture i : toAdd) {
			boolean continueFlag = false;
			for (Fixture n : next) {
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
			XlSUtils.fillMissingShotsData(competition, CURRENT_YEAR);
		}

		System.out.println(competition + " successfully updated");

	}

	private static ArrayList<Fixture> oddsUpToDate(String competition, int currentYear, Date yesterday, String add)
			throws InterruptedException {
		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, currentYear);
		} else
			address = add;
		System.out.println(address);

		Set<Fixture> result = new HashSet<>();

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
					Fixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);

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

		ArrayList<Fixture> fin = new ArrayList<>();
		fin.addAll(result);
		return fin;

	}

	private static void login(WebDriver driver) {
		String label = "Login";
		driver.findElement(By.xpath("//button[contains(.,'" + label + "')]")).click();

		String pass = "";
		Path wiki_path = Paths.get(new File("").getAbsolutePath(), "pass.txt");

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

	private static boolean isFixtureLink(String attribute) {
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

	private static ArrayList<Fixture> collectUpToDate(String competition, int currentYear, Date yesterday, String add)
			throws IOException, ParseException, InterruptedException {
		ArrayList<Fixture> result = new ArrayList<>();
		Set<Fixture> set = new HashSet<>();
		String address;
		if (add == null) {
			address = EntryPoints.getLink(competition, currentYear);
			System.out.println(address);
		} else
			address = add;
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
				Fixture ef = getFixture(fixture, competition);
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

		ArrayList<Fixture> setlist = new ArrayList<>();
		set.addAll(result);
		setlist.addAll(set);
		return setlist;
	}

	private static ArrayList<Fixture> nextMatches(String competition, Object object, OnlyTodayMatches onlyTodaysMatches)
			throws ParseException, InterruptedException, IOException {
		String address = EntryPoints.getOddsLink(competition, EntryPoints.CURRENT);
		System.out.println(address);

		ArrayList<Fixture> result = new ArrayList<>();
		Set<String> teams = new HashSet<>();

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

			Fixture ef = getOddsFixture(driver, i, competition, true, onlyTodaysMatches);
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

	public static ArrayList<Fixture> collect(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {
		ArrayList<Fixture> result = new ArrayList<>();
		Set<Fixture> set = new HashSet<>();
		String address;
		if (add == null) {
			address = EntryPoints.getLink(competition, year);
			System.out.println(address);
		} else
			address = add;

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
					Fixture ef = getFixture(fixture, competition);
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

		ArrayList<Fixture> setlist = new ArrayList<>();
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

	// TODO clean
	public static Fixture getFixture(Document fixture, String competition) throws IOException, ParseException {

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
		Pair shots = Pair.of(-1, -1);
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

		Fixture ef = new Fixture(date, competition, homeTeam, awayTeam, result).withHTResult(ht)
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

	// TODO refactor
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
		Fixture fix = new Fixture(date, competition, homeTeam, awayTeam, result);
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
		// Fixture ef = new Fixture(date, homeTeam, awayTeam,
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

	public static ArrayList<Fixture> odds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
		} else
			address = add;
		System.out.println(address);

		Set<Fixture> result = new HashSet<>();

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
					Fixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);
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

		ArrayList<Fixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println(fin.size());
		return fin;
	}

	// private static int getMaxPageCount(WebDriver driver) {
	// int maxPage = -1;
	// try {
	// WebElement pagin = driver.findElement(By.xpath("//*[@id='pagination']"));
	// List<WebElement> spans = pagin.findElements(By.tagName("span"));
	// for (WebElement i : spans) {
	// if (isNumeric(i.getText())) {
	// if (Integer.parseInt(i.getText().trim()) > maxPage)
	// maxPage = Integer.parseInt(i.getText().trim());
	// }
	// }
	// } catch (Exception e) {
	//
	// }
	// return maxPage;
	// }

	public static ArrayList<Fixture> oddsByPage(String competition, int year, String add, int page) {
		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
		} else
			address = add;
		System.out.println(address);

		Set<Fixture> result = new HashSet<>();

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
					Fixture ef = getOddsFixture(driver, i, competition, false, OnlyTodayMatches.FALSE);
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

		ArrayList<Fixture> fin = new ArrayList<>();
		fin.addAll(result);
		System.out.println("Thread at page " + page + "finished successfuly with " + fin.size());
		return fin;

	}

	public static ArrayList<Fixture> oddsInParallel(String competition, int year, String add)
			throws InterruptedException, ExecutionException {
		ArrayList<Fixture> result = new ArrayList<>();

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
		ArrayList<Future<ArrayList<Fixture>>> threadArray = new ArrayList<Future<ArrayList<Fixture>>>();
		for (int i = 1; i <= maxPage; i++) {
			threadArray.add(pool.submit(new RunnerOdds(competition, year, add, i)));
		}

		for (Future<ArrayList<Fixture>> fd : threadArray) {
			result.addAll(fd.get());
		}

		pool.shutdown();

		System.out.println("Final odds size " + result.size());
		return result;
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
	// TODO refactor
	public static Fixture getOddsFixture(WebDriver driver, String i, String competition, boolean liveMatchesFlag,
			OnlyTodayMatches onlyToday) throws ParseException, InterruptedException, IOException {

		driver.navigate().to(i);

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

		Date now = new Date();
		MatchOdds mo = new MatchOdds("Pinnacle", now, homeOdds, drawOdds, awayOdds);
		AsianOdds ao = new AsianOdds("Pinnacle", now, line, asianHome, asianAway);
		OverUnderOdds overUnder = new OverUnderOdds("Pinnacle", now, 2.5f, overOdds, underOdds);

		Fixture ef = new Fixture(date, competition, home, away, fullResult).withHTResult(htResult);
		ef.matchOdds.add(mo);
		ef.asianOdds.add(ao);
		ef.overUnderOdds.add(overUnder);

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
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static void updateDB(ArrayList<String> list, int n, OnlyTodayMatches onlyToday, UpdateType automatic,
			int day, int month) throws Exception {
		// ExecutorService executor = Executors.newFixedThreadPool(n);
		ArrayList<String> leagues = automatic.equals(UpdateType.AUTOMATIC) ? getTodaysLeagueList(day, month) : list;
		System.out.println("Updating for: ");
		System.out.println(leagues);

		for (String league : leagues) {
			// Runnable worker = new UpdateRunner(i, onlyToday);
			// executor.execute(worker);
			updateDBfor(league, onlyToday);
		}
		// executor.shutdown();
	}

	private static void updateDBfor(String league, OnlyTodayMatches onlyToday) throws Exception {
		int collectYear = Arrays.asList(EntryPoints.SUMMER).contains(league) ? EntryPoints.SUMMERCURRENT
				: EntryPoints.CURRENT;

		Date oldestTocheckGS = SQLiteJDBC.findLastPendingGameStatsDate(league, collectYear);
		System.out.println("GS " + oldestTocheckGS);
		Date oldestTocheck = SQLiteJDBC.findLastPendingFixtureDate(league, collectYear);
		// oldestTocheck = oldestTocheck.before(oldestTocheckGS) ? oldestTocheck :
		// oldestTocheckGS;
		System.out.println(oldestTocheck);

		// ArrayList<Fixture> gameStats = SQLiteJDBC.selectGameStats(league,
		// collectYear);

		ArrayList<Fixture> list = new ArrayList<>();
		// check if update of previous results is necessary
		if (new Date().after(oldestTocheck)) {
			ArrayList<Fixture> odds = FullOddsCollector.of(league, collectYear).collectUpToDate(oldestTocheck);
			System.out.println(odds.size() + " odds ");
			SQLiteJDBC.storeFixtures(odds, CURRENT_YEAR);

			list = GameStatsCollector.of(league, collectYear).collectUpToDate(oldestTocheckGS);
			System.out.println(list.size() + "shots");
			SQLiteJDBC.storeGameStats(list, league, collectYear);
		}

		ArrayList<Fixture> next = FullOddsCollector.of(league, collectYear).nextMatches(onlyToday);
		SQLiteJDBC.storeFixtures(next, CURRENT_YEAR);

		System.out.println(league + " successfully updated");
	}
}
