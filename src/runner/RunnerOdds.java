package runner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import main.Fixture;
import scraper.Scraper;

public class RunnerOdds implements Callable<ArrayList<Fixture>> {

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
	public ArrayList<Fixture> call() throws Exception {
		ArrayList<Fixture> tobet = Scraper.oddsByPage(competition, year, add, page);
		return tobet;
	}
}
