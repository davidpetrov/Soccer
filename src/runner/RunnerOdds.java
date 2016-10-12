package runner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import main.ExtendedFixture;
import scraper.Scraper;

public class RunnerOdds implements Callable<ArrayList<ExtendedFixture>> {

	public String competition;
	public int year;
	public String add;
	public int page;

	public RunnerOdds(String competition, int year, String add, int page) {
		this.competition = competition;
		this.year = year;
		this.add = add;
		this.page = page;
	}

	@Override
	public ArrayList<ExtendedFixture> call() throws Exception {
		ArrayList<ExtendedFixture> tobet = Scraper.oddsByPage(competition, year, add, page);
		return tobet;
	}
}
