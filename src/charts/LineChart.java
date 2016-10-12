package charts;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class LineChart extends ApplicationFrame {
	public LineChart(float[] data, int year) {
		super("Profit " + year);
		JFreeChart lineChart = ChartFactory.createLineChart("", "Bets", "Units", createDataset(data),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel(lineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);
	}

	private DefaultCategoryDataset createDataset(float[] data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < data.length; i++) {
			dataset.addValue(data[i], "", new Float(i));
		}
		return dataset;
	}

	public static void draw(float[] data, int year) {
		LineChart chart = new LineChart(data, year);

		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}
}
