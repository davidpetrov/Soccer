package main;

import java.util.Date;

public interface Combinable {

	String getHomeTeam();

	String getAwayTeam();
	
	Date getDate();
	
	Result getResult();

	GameStats getGameStats();

	Combinable withGameStats(GameStats gameStats);

}
