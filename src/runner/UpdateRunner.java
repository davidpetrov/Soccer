package runner;

import java.io.IOException;
import java.text.ParseException;

import predictions.Predictions.OnlyTodayMatches;
import scraper.Scraper;

public class UpdateRunner implements Runnable {
	public String competition;
	public OnlyTodayMatches onlyTodaysMatches;

	public UpdateRunner(String competition, OnlyTodayMatches onlyToday) {
		this.competition = competition;
		this.onlyTodaysMatches = onlyToday;
	}

	@Override
	public void run() {
		try {
			Scraper.checkAndUpdate(competition, onlyTodaysMatches);
		} catch (IOException | ParseException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
