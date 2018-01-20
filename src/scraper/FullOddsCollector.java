package scraper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import main.Fixture;
import main.Result;
import main.SQLiteJDBC;
import odds.AsianOdds;
import odds.MatchOdds;
import odds.OverUnderOdds;
import utils.ThrowingSupplier;

public class FullOddsCollector {
	public static final DateFormat FORMATFULL = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
	public static final long WAITTOLOAD = 1400;

	public String competition;
	public int year;
	public String optionalFullAddress;

	public FullOddsCollector(String competition, int year, String optionalFullAddress) {
		super();
		this.competition = competition;
		this.year = year;
		this.optionalFullAddress = optionalFullAddress;
	}

	public static FullOddsCollector of(String competition, int year) {
		return new FullOddsCollector(competition, year, null);
	}

	public void collectAndStore() throws InterruptedException {
		ArrayList<Fixture> fixtures = collect();
		SQLiteJDBC.storeFixtures(fixtures);
	}

	public ArrayList<Fixture> collect() throws InterruptedException {
		String address;
		if (optionalFullAddress == null) {
			address = EntryPoints.getOddsLink(competition, year);
		} else
			address = optionalFullAddress;
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
		int maxPage = getMaxPageCount(driver);

		HashMap<Integer, String> booksMap = new HashMap<>();
		for (int page = 1; page <= maxPage; page++) {
			try {
				driver.navigate().to(address + "/results/#/page/" + page + "/");

				ArrayList<String> links = new ArrayList<>();
				WebElement table = driver.findElement(By.id("tournamentTable"));
				List<WebElement> tagrows = table.findElements(By.tagName("tr"));

				for (int i = 0; i < tagrows.size(); i++) {

					WebElement elem = tagrows.get(i);
					Optional<String> classValue = ThrowingSupplier.tryTimes(10, () -> {
						return elem.getAttribute("class");
					});

					if (classValue.isPresent() && !classValue.get().contains("deactivate"))
						continue;

					WebElement aElem = elem.findElement(By.tagName("a"));
					if (aElem != null) {
						String href = aElem.getAttribute("href");
						if (isFixtureLink(href))
							links.add(href);
					}

				}

				for (String i : links) {
					Fixture f = null;
					int count = 0;
					while (true) {
						try {
							f = getFixtureTest(driver, i, competition, year, booksMap);
							break;
						} catch (Exception e) {
							System.out.println(" retry " + count);
							if (++count == 10) {
								System.out.println("Error when parsing \n" + i);
								throw e;
							}
						}
					}

					if (f != null)
						result.add(f);

				}

			} catch (Exception e) {
				e.printStackTrace();
				page--;
				System.out.println("Starting over from page:" + page);
				try {
					driver.close();
				} catch (Exception e1) {
				}

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

	// TODO for big leagues i.e 24 teams finds incorrect result
	private static int getMaxPageCount(WebDriver driver) {
		int maxPage = -1;
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
		// WebElement lastPage = driver.findElement(By.xpath("//*[contains(text(),
		// '>>|')]"));

		return maxPage;
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

	public static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static Fixture getFixtureTest(WebDriver driver, String i, String competition, int year,
			HashMap<Integer, String> booksMap) throws Exception {
		long start = System.currentTimeMillis();
		driver.navigate().to(i);

		String title = driver.findElement(By.xpath("//*[@id='col-content']/h1")).getText();
		String home = title.split(" - ")[0].trim();
		String away = title.split(" - ")[1].trim();

		String dateString = driver.findElement(By.xpath("//*[@id='col-content']/p[1]")).getText();
		dateString = dateString.split(",")[1] + dateString.split(",")[2];
		Date date = FORMATFULL.parse(dateString);

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

		if (booksMap.isEmpty())
			booksMap.putAll(getBooksMapIfEmpty(driver));

		HashMap<String, ArrayList<MatchOdds>> matchOdds = getMatchDataFromJS(driver, booksMap);
		HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> overUnderOdds = getOverUnderDataFromJS(driver,
				booksMap);
		HashMap<Float, HashMap<String, ArrayList<AsianOdds>>> asianOdds = getAsianDataFromJS(driver, booksMap);

		Fixture f = new Fixture(date, competition, home, away, fullResult).withHTResult(htResult).withYear(year)
				.withOUodds(overUnderOdds).withAsianOdds(asianOdds).withMatchOdds(matchOdds);

		System.out.println(f);
		System.out.println("full odds data time " + (System.currentTimeMillis() - start) / 1000d + "sec");
		return f;
	}

	private static HashMap<Integer, String> getBooksMapIfEmpty(WebDriver driver) throws InterruptedException {
		Thread.sleep(2500);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		// for bookmaker name hash --
		jse.executeScript(
				"document.body.innerHTML += '<div style=\"display:none;\" id=\"hackerman2\">' + JSON.stringify(globals.bookmakerData) + '</div>'");
		String bookmakerHashString = (String) jse
				.executeScript("return document.getElementById('hackerman2').innerHTML");

		return getBookmakersMap(bookmakerHashString);
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

	private static HashMap<String, ArrayList<MatchOdds>> getMatchDataFromJS(WebDriver driver,
			HashMap<Integer, String> booksMap) throws Exception {
		JSONObject json = getJsonDataFromJS(driver, "1X2");

		HashMap<String, ArrayList<MatchOdds>> matchOdds = getFullMatchOddsHistory(json, booksMap);
		return matchOdds;
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
					// Object unknown = hentry.get(1);
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
					JSONArray closingArr = closingOddsObject.getJSONArray(b);
					homeOdds = (float) closingArr.getDouble(0);
					if (closingArr.length() > 1)
						drawOdds = (float) closingOddsObject.getJSONArray(b).getDouble(1);
					if (closingArr.length() > 2)
						awayOdds = (float) closingOddsObject.getJSONArray(b).getDouble(2);
				} else {
					if (closingOddsObject.getJSONObject(b).has("0"))
						homeOdds = (float) closingOddsObject.getJSONObject(b).getDouble("0");
					if (closingOddsObject.getJSONObject(b).has("1"))
						drawOdds = (float) closingOddsObject.getJSONObject(b).getDouble("1");
					if (closingOddsObject.getJSONObject(b).has("2"))
						awayOdds = (float) closingOddsObject.getJSONObject(b).getDouble("2");
				}

				Date timeHome = null, timeDraw = null, timeAway = null;

				Object changeTimeJSON = changeTime.get(b);

				if (changeTimeJSON instanceof JSONArray) {
					JSONArray changeTimeArray = changeTime.getJSONArray(b);

					cal.setTimeInMillis(changeTimeArray.getLong(0) * 1000);
					timeHome = cal.getTime();
					if (changeTimeArray.length() > 1) {
						cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(1) * 1000);
						timeDraw = cal.getTime();
					}
					if (changeTimeArray.length() > 1) {
						cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(2) * 1000);
						timeAway = cal.getTime();
					}
				} else {
					if (changeTime.getJSONObject(b).has("0")) {
						cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("0") * 1000);
						timeHome = cal.getTime();
					}
					if (changeTime.getJSONObject(b).has("1")) {
						cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("1") * 1000);
						timeDraw = cal.getTime();
					}
					if (changeTime.getJSONObject(b).has("2")) {
						cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("2") * 1000);
						timeAway = cal.getTime();
					}
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

			if (homeHistory == null || drawHistory == null || awayHistory == null)
				continue;

			try {
				homeHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());
				drawHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());
				awayHistory.sort(Comparator.comparing(MatchOdds::getTime).reversed());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
					// Object unknown = hentry.get(1);
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

			if (overBookMap == null || underBookMap == null) {
				System.out.println();
				continue;
			}

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
				if (arr.length() > 1)
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
				float overOdds = 1f, underOdds = 1f;

				try {
					if (closingObjectJSON instanceof JSONArray) {
						overOdds = (float) closingOddsObject.getJSONArray(b).getDouble(0);
						if (closingOddsObject.getJSONArray(b).length() > 1)
							underOdds = (float) closingOddsObject.getJSONArray(b).getDouble(1);
					} else {
						if (closingOddsObject.getJSONObject(b).has("0"))
							overOdds = (float) closingOddsObject.getJSONObject(b).getDouble("0");
						if (closingOddsObject.getJSONObject(b).has("1"))
							underOdds = (float) closingOddsObject.getJSONObject(b).getDouble("1");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Date timeOver = null, timeUnder = null;

				Object changeTimeJSON = changeTime.get(b);

				try {
					if (changeTimeJSON instanceof JSONArray) {
						cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(0) * 1000);
						timeOver = cal.getTime();
						if (changeTime.getJSONArray(b).length() > 1) {
							cal.setTimeInMillis(changeTime.getJSONArray(b).getLong(1) * 1000);
							timeUnder = cal.getTime();
						}
					} else {
						if (changeTime.getJSONObject(b).has("0")) {
							cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("0") * 1000);
							timeOver = cal.getTime();
						}
						if (changeTime.getJSONObject(b).has("1")) {
							cal.setTimeInMillis(changeTime.getJSONObject(b).getLong("1") * 1000);
							timeUnder = cal.getTime();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				OverUnderOdds closingOver = new OverUnderOdds(bookmaker, timeOver == null ? timeUnder : timeOver,
						handicapValue, overOdds, -1f).withIsClosing();
				OverUnderOdds closingUnder = new OverUnderOdds(bookmaker, timeUnder == null ? timeOver : timeUnder,
						handicapValue, -1f, underOdds).withIsClosing();
				closingOdds.add(closingOver);
				closingOdds.add(closingUnder);
			}

		}

	}

	private static HashMap<Float, HashMap<String, ArrayList<OverUnderOdds>>> getOverUnderDataFromJS(WebDriver driver,
			HashMap<Integer, String> booksMap) throws Exception {

		JSONObject json = getJsonDataFromJS(driver, "O/U");

		return getFullOddsHistoryOverUnder(json, booksMap);

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
				Thread.sleep(WAITTOLOAD);
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
}
