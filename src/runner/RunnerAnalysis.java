package runner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import entries.FinalEntry;
import main.Analysis;
import main.Fixture;
import main.Prediction;

public class RunnerAnalysis implements Callable<ArrayList<FinalEntry>> {

	public int year;
	public String league;
	public ArrayList<Fixture> fixtures;

	public RunnerAnalysis(ArrayList<Fixture> fixtures, String league, int year) {
		this.fixtures = fixtures;
		this.year = year;
		this.league = league;
	}

	@Override
	public ArrayList<FinalEntry> call() throws Exception {
		return Analysis.predict(fixtures, league, year);
	}
}