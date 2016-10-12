package scraper;

import java.util.Arrays;
import java.util.HashMap;

import org.omg.CORBA.Current;

public class EntryPoints {
	public static final int CURRENT = 2016;

	public static final String[] TRACKING = { "BRA", "BRB", "SWE", "NOR", "FIN", "USA", };
	public static final String[] MERGED = { "FR2", "BEL", "SWI", "E0" };
	public static final String[] HYPHENODDS = { "ENG", "SWI", "SPA" };

	public static HashMap<String, String> map = new HashMap<>();
	public static HashMap<String, String> odds = new HashMap<>();

	static {

		map.put("BRA", "http://int.soccerway.com/national/brazil/serie-a/");
		map.put("BRB", "http://int.soccerway.com/national/brazil/serie-b/");
		map.put("ARG", "http://int.soccerway.com/national/argentina/primera-division/");
		map.put("ARG2", "http://int.soccerway.com/national/argentina/prim-b-nacional/");
		map.put("SWE", "http://int.soccerway.com/national/sweden/allsvenskan/");
		map.put("SWE2", "http://int.soccerway.com/national/sweden/superettan/");
		map.put("NOR", "http://int.soccerway.com/national/norway/eliteserien/");
		map.put("USA", "http://int.soccerway.com/national/united-states/mls/");
		map.put("ICE", "http://int.soccerway.com/national/iceland/urvalsdeild/");
		map.put("FIN", "http://int.soccerway.com/national/finland/veikkausliiga/");
		map.put("JP", "http://int.soccerway.com/national/japan/j1-league/");
		map.put("USA", "http://int.soccerway.com/national/united-states/mls/");
		map.put("FR2", "http://int.soccerway.com/national/france/ligue-2/");
		map.put("BEL", "http://int.soccerway.com/national/belgium/pro-league/");
		map.put("SWI", "http://int.soccerway.com/national/switzerland/super-league/");
		map.put("ENG", "http://int.soccerway.com/national/england/premier-league/");
		map.put("SPA", "http://int.soccerway.com/national/spain/primera-division/");

	}

	// oddsportal links
	static {
		odds.put("SWE", "http://www.oddsportal.com/soccer/sweden/allsvenskan");
		odds.put("NOR", "http://www.oddsportal.com/soccer/norway/tippeligaen");
		odds.put("BRB", "http://www.oddsportal.com/soccer/brazil/serie-b");
		odds.put("BRA", "http://www.oddsportal.com/soccer/brazil/serie-a");
		odds.put("ARG2", "http://www.oddsportal.com/soccer/argentina/primera-b-nacional");
		odds.put("ICE", "http://www.oddsportal.com/soccer/iceland/pepsideild");
		odds.put("FIN", "http://www.oddsportal.com/soccer/finland/veikkausliiga");
		odds.put("JP", "http://www.oddsportal.com/soccer/japan/j-league");
		odds.put("USA", "http://www.oddsportal.com/soccer/usa/mls");
		odds.put("FR2", "http://www.oddsportal.com/soccer/france/ligue-2");
		odds.put("BEL", "http://www.oddsportal.com/soccer/belgium/jupiler-league");
		odds.put("SWI", "http://www.oddsportal.com/soccer/switzerland/super-league");
		odds.put("ENG", "http://www.oddsportal.com/soccer/england/premier-league");
		odds.put("SPA", "http://www.oddsportal.com/soccer/spain/laliga");

	}

	public static String getLink(String competition, int year) {
		String result = map.get(competition);
		if (Arrays.asList(MERGED).contains(competition))
			result += year + "" + (year + 1);
		else
			result += year;

		return result + "/regular-season/matches/";
	}

	public static String getOddsLink(String competition, int year) {
		String result = odds.get(competition);
		if (year != CURRENT) {
			if (Arrays.asList(HYPHENODDS).contains(competition))
				result += "-" + year + "-" + (year + 1);
			else
				result += "-" + year;
		}

		return result;
	}

}
