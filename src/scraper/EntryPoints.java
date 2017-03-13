package scraper;

import java.util.Arrays;
import java.util.HashMap;

import org.omg.CORBA.Current;

public class EntryPoints {
	public static final int CURRENT = 2016;

	public static final String[] TRACKING = { "BRA", "BRB", "SWE", "NOR", "FIN", "USA", };
	public static final String[] MERGED = { "FR2", "BEL", "SWI", "E0", "ENG", "SPA", "SPA2", "GER", "GER2", "FR", "IT",
			"SCO", "IT2", "TUR", "ENG2", "ENG3", "ENG4", "ENG5", "ARG", "GRE", "POR", "NED", "BUL", "RUS", "AUS", "DEN",
			"CZE", "POL", "CRO", "SLO", "SLK" };
	public static final String[] HYPHENODDS = { "ENG", "SWI", "SPA", "SPA2", "GER", "GER2", "FR", "IT", "SCO", "IT2",
			"TUR", "ENG2", "ENG3", "ENG4", "ENG5", "ARG", "GRE", "POR", "NED", "BUL", "RUS", "AUS", "DEN", "CZE", "POL",
			"CRO", "SLO", "SLK" };

	public static final String[] EXCEPTIONS = { "SCO" };

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
		map.put("SPA2", "http://int.soccerway.com/national/spain/segunda-division/");
		map.put("GER", "http://int.soccerway.com/national/germany/bundesliga/");
		map.put("FR", "http://int.soccerway.com/national/france/ligue-1/");
		map.put("IT", "http://int.soccerway.com/national/italy/serie-a/");
		map.put("SCO", "http://int.soccerway.com/national/scotland/premier-league/");
		map.put("IT2", "http://int.soccerway.com/national/italy/serie-b/");
		map.put("TUR", "http://int.soccerway.com/national/turkey/super-lig/");
		map.put("ENG2", "http://int.soccerway.com/national/england/championship/");
		map.put("ENG3", "http://int.soccerway.com/national/england/league-one/");
		map.put("ENG4", "http://int.soccerway.com/national/england/league-two/");
		map.put("ENG5", "http://int.soccerway.com/national/england/conference-national/");
		map.put("GRE", "http://int.soccerway.com/national/greece/super-league/");
		map.put("POR", "http://int.soccerway.com/national/portugal/portuguese-liga-/");
		map.put("NED", "http://int.soccerway.com/national/netherlands/eredivisie/");
		map.put("BUL", "http://int.soccerway.com/national/bulgaria/a-pfg/");
		map.put("RUS", "http://int.soccerway.com/national/russia/premier-league/");
		map.put("AUS", "http://int.soccerway.com/national/austria/bundesliga/");
		map.put("DEN", "http://int.soccerway.com/national/denmark/superliga/");
		map.put("CZE", "http://int.soccerway.com/national/czech-republic/czech-liga/");
		map.put("GER2", "http://int.soccerway.com/national/germany/2-bundesliga/");
		map.put("POL", "http://int.soccerway.com/national/poland/ekstraklasa/");
		map.put("CRO", "http://int.soccerway.com/national/croatia/1-hnl/");
		map.put("SLO", "http://int.soccerway.com/national/slovenia/1-snl/");
		map.put("SLK", "http://int.soccerway.com/national/slovakia/super-liga/");
	}

	// oddsportal links
	static {
		odds.put("SWE", "http://www.oddsportal.com/soccer/sweden/allsvenskan");
		odds.put("NOR", "http://www.oddsportal.com/soccer/norway/tippeligaen");
		odds.put("BRB", "http://www.oddsportal.com/soccer/brazil/serie-b");
		odds.put("BRA", "http://www.oddsportal.com/soccer/brazil/serie-a");
		odds.put("ARG", "http://www.oddsportal.com/soccer/argentina/primera-division");
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
		odds.put("SPA2", "http://www.oddsportal.com/soccer/spain/laliga2");
		odds.put("GER", "http://www.oddsportal.com/soccer/germany/bundesliga");
		odds.put("FR", "http://www.oddsportal.com/soccer/france/ligue-1");
		odds.put("IT", "http://www.oddsportal.com/soccer/italy/serie-a");
		odds.put("SCO", "http://www.oddsportal.com/soccer/scotland/premiership");
		odds.put("IT2", "http://www.oddsportal.com/soccer/italy/serie-b");
		odds.put("TUR", "http://www.oddsportal.com/soccer/turkey/super-lig");
		odds.put("ENG2", "http://www.oddsportal.com/soccer/england/championship");
		odds.put("ENG3", "http://www.oddsportal.com/soccer/england/league-one");
		odds.put("ENG4", "http://www.oddsportal.com/soccer/england/league-two");
		odds.put("ENG5", "http://www.oddsportal.com/soccer/england/vanarama-national-league");
		odds.put("GRE", "http://www.oddsportal.com/soccer/greece/super-league");
		odds.put("POR", "http://www.oddsportal.com/soccer/portugal/primeira-liga");
		odds.put("NED", "http://www.oddsportal.com/soccer/netherlands/eredivisie");
		odds.put("BUL", "http://www.oddsportal.com/soccer/bulgaria/parva-liga");
		odds.put("RUS", "http://www.oddsportal.com/soccer/russia/premier-league");
		odds.put("AUS", "http://www.oddsportal.com/soccer/austria/tipico-bundesliga");
		odds.put("DEN", "http://www.oddsportal.com/soccer/denmark/superliga");
		odds.put("CZE", "http://www.oddsportal.com/soccer/czech-republic/1-liga");
		odds.put("GER2", "http://www.oddsportal.com/soccer/germany/2-bundesliga");
		odds.put("POL", "http://www.oddsportal.com/soccer/poland/ekstraklasa");
		odds.put("CRO", "http://www.oddsportal.com/soccer/croatia/1-hnl");
		odds.put("SLO", "http://www.oddsportal.com/soccer/slovenia/prva-liga");
		odds.put("SLK", "http://www.oddsportal.com/soccer/slovakia/fortuna-liga");
	}

	public static String getLink(String competition, int year) {
		String result = map.get(competition);
		if (Arrays.asList(EXCEPTIONS).contains(competition))
			return result;

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
