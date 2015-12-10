package algorithms;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import main.ExtendedFixture;

public abstract class Algorithm {
	public ExtendedFixture fixture;
	public ArrayList<ExtendedFixture> homeSideFixtures;
	public ArrayList<ExtendedFixture> awaySideFixtures;

	public Algorithm(ExtendedFixture fixture) throws JSONException, IOException {
		this.fixture = fixture;
		// homeSideFixtures = fixture.getTeamRelevantFixtures("home");
		// awaySideFixtures = fixture.getTeamRelevantFixtures("away");
	}

	public abstract float calculate();
}
