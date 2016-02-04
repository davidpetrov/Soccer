package runner;

import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import settings.Settings;
import xls.XlSUtils;

public class RunnerAggregateInterval implements Callable<Settings> {

	public HSSFSheet sh;
	public int startYear;
	public int endYear;

	public RunnerAggregateInterval(int startYear, int endYear, HSSFSheet sh) {
		this.sh = sh;
		this.startYear = startYear;
		this.endYear = endYear;
	}

	@Override
	public Settings call() throws Exception {

		Settings setts = XlSUtils.aggregateOptimals(startYear, endYear, sh.getSheetName());
		System.out.println(setts + " avg: " + setts.profit / (endYear - startYear + 1));

		return setts;
	}
}