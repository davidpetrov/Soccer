package runner;

import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import settings.Settings;
import xls.XlSUtils;

public class RunnerOptimals implements Callable<Float> {

	public HSSFSheet sh;
	public int year;

	public RunnerOptimals(HSSFSheet sh, int year) {
		this.sh = sh;
		this.year = year;
	}

	@Override
	public Float call() throws Exception {
		Settings set = XlSUtils.optimalSettings(sh, year);
		System.out.println(year + " " + set);
		// System.out.println("Total profit for " + year + " is: " +
		// set.profit);
		return set.profit;
	}
}
