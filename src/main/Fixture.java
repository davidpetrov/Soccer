//package main;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import utils.Utils;
//
//public class Fixture implements Comparable<Fixture> {
//	String date;
//	public Date dt;
//	public String status;
//	public int matchday;
//	public String homeTeamName;
//	public String awayTeamName;
//	public Result result;
//	public String links_homeTeam;
//	public String links_awayTeam;
//	public String links_competition;// corresponds to link soccerseasons
//
//	public Fixture(String date, String status, int matchday, String homeTeamName, String awayTeamName, Result result,
//			String links_homeTeam, String links_awayTeam, String links_competition) {
//		this.date = date;
//		this.status = status;
//		this.matchday = matchday;
//		this.homeTeamName = homeTeamName;
//		this.awayTeamName = awayTeamName;
//		this.result = result;
//		this.links_homeTeam = links_homeTeam;
//		this.links_awayTeam = links_awayTeam;
//		this.links_competition = links_competition;
//	}
//
//	public Fixture(Date date, String status, int matchday, String homeTeamName, String awayTeamName, Result result,
//			String links_homeTeam, String links_awayTeam, String links_competition) {
//		this.dt = date;
//		this.status = status;
//		this.matchday = matchday;
//		this.homeTeamName = homeTeamName;
//		this.awayTeamName = awayTeamName;
//		this.result = result;
//		this.links_homeTeam = links_homeTeam;
//		this.links_awayTeam = links_awayTeam;
//		this.links_competition = links_competition;
//	}
//
//	// currently returns all finished fixtures in the given competition for the
//	// home/away team for the current season only
//	public ArrayList<Fixture> getTeamRelevantFixtures(String side) throws JSONException, IOException {
//		String address;
//		if (side.equals("home"))
//			address = links_homeTeam + "/fixtures";
//		else
//			address = links_awayTeam + "/fixtures";
//		String queryResult = Utils.query(address);
//
//		JSONArray jsonAll = new JSONObject(queryResult).getJSONArray("fixtures");
//		ArrayList<Fixture> all = Utils.createFixtureList(jsonAll);
//		ArrayList<Fixture> finished = new ArrayList<>();
//		for (Fixture f : all) {
//			if (f.status.equals("FINISHED") && f.links_competition.equals(links_competition))
//				finished.add(f);
//		}
//		return finished;
//	}
//
//	public int getTotalGoals() {
//		return result.goalsHomeTeam + result.goalsAwayTeam;
//	}
//
//	public boolean bothTeamScore() {
//		return ((result.goalsAwayTeam > 0) && (result.goalsHomeTeam > 0));
//	}
//
//	@Override
//	public String toString() {
//		return date + " " + status + " " + matchday + "\n" + homeTeamName + " " + result.goalsHomeTeam + " : "
//				+ result.goalsAwayTeam + " " + awayTeamName + "\n";
//	}
//
//	@Override
//	public int compareTo(Fixture o) {
//		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//		try {
//			return format.parse(date).compareTo(format.parse(o.date));
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return 0;
//		}
//	}
//}
