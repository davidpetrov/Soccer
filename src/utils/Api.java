package utils;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.Fixture;

public class Api {

	public static final String TIMEFRAME = "?timeFrame=";
	public static final String BASE = "http://api.football-data.org/alpha/";

	public static JSONArray getFixtures(int period) throws JSONException, IOException {
		JSONObject obj = new JSONObject(Utils.query(BASE + "fixtures/" + TIMEFRAME + "n" + period));
		return obj.getJSONArray("fixtures");
	}

	// currently returns all finished fixtures in the given competition for the
	// home/away team for the current season only
	public static ArrayList<Fixture> getTeamRelevantFixtures(Fixture fixture, String side)
			throws JSONException, IOException {
		String queryResult = Utils.query(
				side.equals("home") ? (fixture.links_homeTeam + "/fixtures") : (fixture.links_awayTeam + "/fixtures"));

		JSONArray jsonAll = new JSONObject(queryResult).getJSONArray("fixtures");
		ArrayList<Fixture> all = Utils.createFixtureList(jsonAll);
		ArrayList<Fixture> finished = new ArrayList<>();
		for (Fixture f : all) {
			if (f.status.equals("FINISHED") && f.links_competition.equals(fixture.links_competition))
				finished.add(f);
		}
		return finished;
	}

	// finds fixtures that will be played in the next 7 days
	public static ArrayList<Fixture> findFixtures(int period) throws JSONException, IOException {
		JSONArray arr = getFixtures(period);
		return Utils.createFixtureList(arr);
	}

}
