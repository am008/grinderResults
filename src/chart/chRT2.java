package chart;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class chRT2 {

	public static int jsonextract(String fileName) {
		int c = 0;
		try {

			String fullFileName = fileName + ".json";

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(fullFileName));
			JSONObject jsonObject = (JSONObject) obj;

			JSONArray results = (JSONArray) jsonObject.get("results");
			Iterator<?> iterator = results.iterator();

			while (iterator.hasNext()) {
				JSONObject innerobj = (JSONObject) iterator.next();
				JSONArray sub = (JSONArray) innerobj.get("subtests");
				Iterator<JSONObject> i = sub.iterator();
				JSONObject in = (JSONObject) i.next();
				String st = (String) in.get("status");

				if (st.contains("FAIL")) {
					c++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static void main(String[] args) throws Exception {
		int numFiles;
		List<String> br = new ArrayList<>();

		numFiles = 2;
		br.add("gngr");
		br.add("mozilla");

		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				"Browser Test Results", "Tests Conducted", "Tests Failed",
				collection(br, numFiles), PlotOrientation.VERTICAL, true, true,
				false);
		System.out.println("Creating chart");

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 600));
		final XYPlot plot = xylineChart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.YELLOW);
		renderer.setSeriesStroke(0, new BasicStroke(4.0f));
		renderer.setSeriesStroke(1, new BasicStroke(3.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		plot.setRenderer(renderer);
		File XYLine = new File("Chart2.PNG");
		System.out.println("Saving to Chart2.png");
		ChartUtilities.saveChartAsPNG(XYLine, xylineChart, 500, 500);
		System.out.println("Done");

	}

	public static XYDataset collection(List<String> browsers, int numFiles) {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		for (String browser : browsers) {
			System.out.println("Processing: " + browser);
			dataset.addSeries(series(browser, numFiles));
		}

		return dataset;
	}

	public static XYSeries series(String browserName, int numFiles) {
		final XYSeries dat = new XYSeries(browserName);
		for (int k = 1; k < numFiles + 1; k++) {
			dat.add(k, jsonextract(browserName + k));
		}

		return dat;
	}

}
