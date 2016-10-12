package runner;

import java.io.IOException;
import java.text.ParseException;

import scraper.Scraper;

public class UpdateRunner implements Runnable {
	public String competition;

	public UpdateRunner(String competition) {
		this.competition = competition;
	}

	@Override
	public void run() {
		try {
			Scraper.checkAndUpdate(competition);
		} catch (IOException | ParseException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
