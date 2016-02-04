package runner;

import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import xls.XlSUtils;

public class RunnerAggregateInterval implements Callable<Float> {

	public HSSFSheet sh;
	public int startYear;
	public int endYear;

	public RunnerAggregateInterval(int startYear, int endYear, HSSFSheet sh) {
		this.sh = sh;
		this.startYear = startYear;
		this.endYear = endYear;
	}

	@Override
	public Float call() throws Exception {

		System.out.println(XlSUtils.aggregateOptimals(startYear, endYear, sh.getSheetName()));
		return 0f;
	}
}