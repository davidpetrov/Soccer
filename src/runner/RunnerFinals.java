package runner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import entries.FinalEntry;
import main.Fixture;
import xls.XlSUtils;

public class RunnerFinals implements Callable<ArrayList<FinalEntry>> {

	public ArrayList<Fixture> all;
	public int year;
	public String competition;

	public RunnerFinals(ArrayList<Fixture> allSh, String competition, int year) {
		this.all = allSh;
		this.year = year;
		this.competition = competition;
	}

	@Override
	public ArrayList<FinalEntry> call() throws Exception {
		ArrayList<FinalEntry> tobet = XlSUtils.finalsShots(all, competition, year);
		return tobet;
	}
}