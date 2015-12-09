package algorithms;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import main.Fixture;

public abstract class Algorithm {
	public Fixture fixture;
	public ArrayList<Fixture> homeSideFixtures;
	public ArrayList<Fixture> awaySideFixtures;

	public Algorithm(Fixture fixture) throws JSONException, IOException {
		this.fixture = fixture;
		homeSideFixtures = fixture.getTeamRelevantFixtures("home");
		awaySideFixtures = fixture.getTeamRelevantFixtures("away");
	}

	public abstract float calculate();
}
