package runner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import main.FinalEntry;
import xls.XlSUtils;

public class RunnerTriples implements Callable<ArrayList<FinalEntry>> {

	public HSSFSheet sh;
	public int year;

	public RunnerTriples(HSSFSheet sh, int year) {
		this.sh = sh;
		this.year = year;
	}

	@Override
	public ArrayList<FinalEntry> call() throws Exception {
		ArrayList<FinalEntry> tobet = XlSUtils.triples(sh, year);
		// System.out.println("Profit for " + sh.getSheetName() + " " + year + "
		// is: " + String.format("%.2f", profit));
		return tobet;
	}
}