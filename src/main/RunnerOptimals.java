package main;

import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import settings.Settings;
import xls.XlSUtils;

public class RunnerOptimals implements Callable<Float> {

	public HSSFSheet sh;
	public int year;

	RunnerOptimals(HSSFSheet sh, int year) {
		this.sh = sh;
		this.year = year;
	}

	@Override
	public Float call() throws Exception {
		Settings set = XlSUtils.predictionSettings(sh, year);
//		System.out.println("Total profit for " + year + " is: " + set.profit);
		return set.profit;
	}
}
