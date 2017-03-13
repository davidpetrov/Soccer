package runner;

import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import xls.AsianUtils;
import xls.XlSUtils;

public class RunnerAsianPredictions implements Callable<Float> {

	public HSSFSheet sh;
	public int year;

	public RunnerAsianPredictions(HSSFSheet sh, int year) {
		this.sh = sh;
		this.year = year;
	}

	@Override
	public Float call() throws Exception {
		Float profit = AsianUtils.predictions(sh, year);
		// System.out.println("Profit for " + sh.getSheetName() + " " + year + "
		// is: " + String.format("%.2f", profit));
		return profit;
	}
}
