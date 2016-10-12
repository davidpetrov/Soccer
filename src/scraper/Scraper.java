package scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.AsianLines;
import main.ExtendedFixture;
import main.FullFixture;
import main.GoalLines;
import main.Line;
import main.Result;
import runner.RunnerOdds;
import runner.UpdateRunner;
import utils.Utils;
import xls.XlSUtils;

public class Scraper {
	public static final DateFormat OPTAFORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
	public static final String BASE = "http://int.soccerway.com/";
	public static final String OUSUFFIX = "#over-under;2;2.50;0";
	public static final int CURRENT_YEAR = 2016;

	public static void main(String[] args)
			throws IOException, ParseException, InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();

		// ArrayList<ExtendedFixture> list = collect("BRA", 2016,
		// null/*"http://int.soccerway.com/national/england/premier-league/2010-2011/regular-season/"*/);
		// list.addAll(collect("JP", 2016,
		// "http://int.soccerway.com/national/japan/j1-league/2016/2nd-stage/"));
		//
		// XlSUtils.storeInExcel(list, "BRA", 2016, "manual");

		//
		ArrayList<FullFixture> list = fullOdds("SPA", 2014,
				"http://www.oddsportal.com/soccer/spain/primera-division-2014-2015/");
		// ArrayList<ExtendedFixture> list = oddsInParallel("ENG", 2013, null);
		//
		XlSUtils.storeInExcelFull(list, "SPA", 2014, "fullodds");

		// XlSUtils.combineFull("ENG", 2015);
		// ////
		// XlSUtils.fillMissingShotsData("USA", 2016);

		// ArrayList<ExtendedFixture> next = nextMatches("BRB", null);
		// nextMatches("BRB", null);

		// checkAndUpdate("SWI");
		// updateInParallel();

		System.out.println((System.currentTimeMillis() - start) / 1000d + "sec");
	}

	public static void updateInParallel() {

		ExecutorService executor = Executors.newFixedThreadPool(EntryPoints.MERGED.length);
		for (String i : EntryPoints.MERGED) {
			Runnable worker = new UpdateRunner(i);
			executor.execute(worker);
		}
		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();
		// Wait until all threads are finish
		// executor.awaitTermination(0, null);
		System.out.println("Finished all threads");
	}

	public static void checkAndUpdate(String competition) throws IOException, ParseException, InterruptedException {
		String base = new File("").getAbsolutePath();

		FileInputStream file = new FileInputStream(new File(base + "\\data\\odds" + CURRENT_YEAR + ".xls"));

		HSSFWorkbook workbook = new HSSFWorkbook(file);
		HSSFSheet sh = workbook.getSheet(competition);

		ArrayList<ExtendedFixture> all = XlSUtils.selectAll(sh, 0);
		Date oldestTocheck = Utils.findLastPendingFixture(all);
		System.out.println(oldestTocheck);

		ArrayList<ExtendedFixture> odds = oddsUpToDate(competition, CURRENT_YEAR, Utils.getYesterday(oldestTocheck),
				null);
		System.out.println(odds.size() + " odds ");

		ArrayList<ExtendedFixture> list = collectUpToDate(competition, CURRENT_YEAR, Utils.getYesterday(oldestTocheck),
				null);

		System.err.println(list.size() + "shots");

		ArrayList<ExtendedFixture> combined = XlSUtils.combine(odds, list, competition);
		System.out.println(combined.size() + " combined");
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
		ArrayList<ExtendedFixture> next = nextMatches(competition, null);

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

		if (withNext.size() >= all.size())
			XlSUtils.storeInExcel(withNext, competition, CURRENT_YEAR, "odds");
		System.out.println(competition + " successfully updated");

	}

	private static ArrayList<ExtendedFixture> oddsUpToDate(String competition, int currentYear, Date yesterday,
			String add) throws InterruptedException {
		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, currentYear);
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
					if (i.getText().contains("-") && isFixtureLink(i.getAttribute("href")))
						links.add(i.getAttribute("href"));
				}

				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false);

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
				Thread.sleep(30000);
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

		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hstp_14536_interstitial_pub"))).click();
			System.out.println("Successfully closed efbet add");
		} catch (Exception e) {
			System.out.println("Problem closing efbet add");
		}

		while (true) {
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

		}

		driver.close();

		if (result.size() != set.size())
			System.out.println("size problem of shots data");

		ArrayList<ExtendedFixture> setlist = new ArrayList<>();
		set.addAll(result);
		setlist.addAll(set);
		return setlist;
	}

	private static ArrayList<ExtendedFixture> nextMatches(String competition, Object object)
			throws ParseException, InterruptedException {
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
		for (WebElement i : list) {
			if (i.getText().contains("-")) {
				links.add(i.getAttribute("href"));
				System.out.println(i.getText());
			}
		}

		for (String i : links) {
			ExtendedFixture ef = getOddsFixture(driver, i, competition, true);
			if (ef.result.goalsHomeTeam == -1 && !teams.contains(ef.homeTeam) && !teams.contains(ef.awayTeam)) {
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

		}

		driver.close();
		System.out.println(result.size());
		System.out.println(set.size());

		ArrayList<ExtendedFixture> setlist = new ArrayList<>();
		set.addAll(result);
		setlist.addAll(set);
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

	public static ArrayList<ExtendedFixture> odds(String competition, int year, String add)
			throws IOException, ParseException, InterruptedException {

		String address;
		if (add == null) {
			address = EntryPoints.getOddsLink(competition, year);
			System.out.println(address);
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
					if (i.getText().contains("-") && isFixtureLink(i.getAttribute("href")))
						links.add(i.getAttribute("href"));
				}

				System.out.println(links);
				for (String i : links) {
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false);
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
					ExtendedFixture ef = getOddsFixture(driver, i, competition, false);
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

	public static ExtendedFixture getOddsFixture(WebDriver driver, String i, String competition,
			boolean liveMatchesFlag) throws ParseException, InterruptedException {
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

				if (!liveMatchesFlag && resString.contains("already started")) {
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
					overOdds = Float.parseFloat(textOdds.split("\n")[2].trim());
					underOdds = Float.parseFloat(textOdds.split("\n")[3].trim());
				}
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
				.with1X2Odds(homeOdds, drawOdds, awayOdds).withAsian(GLS.main.line, GLS.main.home, GLS.main.away)
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
