package runner;

import java.io.IOException;
import java.text.ParseException;

import scraper.Scraper;

public class UpdateRunner implements Runnable {
	public String competition;
	public boolean onlyTodaysMatches;

	public UpdateRunner(String competition, boolean onlyTodaysMatches) {
		this.competition = competition;
		this.onlyTodaysMatches = onlyTodaysMatches;
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
