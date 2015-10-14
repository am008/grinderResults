package chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

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

      JSONParser parser = new JSONParser();
      Object obj = parser.parse(new FileReader(fileName));
      JSONObject jsonObject = (JSONObject) obj;
      JSONArray results = (JSONArray) jsonObject.get("results");
      Iterator<?> iterator = results.iterator();

      while (iterator.hasNext()) {
        JSONObject innerobj = (JSONObject) iterator.next();
        JSONArray sub = (JSONArray) innerobj.get("subtests");
        Iterator<JSONObject> i = sub.iterator();
        while (i.hasNext()) {
          JSONObject in = (JSONObject) i.next();
          String st = (String) in.get("status");

          if (st.contains("FAIL")) {
            c++;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return c;
  }

  public static void main(String[] args) throws Exception {

    JFreeChart xylineChart = ChartFactory.createXYLineChart(
        "Browser Test Results", "Tests Conducted", "Tests Failed",
        collection(), PlotOrientation.VERTICAL, true, true,
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

  public static XYDataset collection() throws Exception {
    final XYSeriesCollection dataset = new XYSeriesCollection();

    try {
      JSONParser parser = new JSONParser();
      JSONArray readfile = (JSONArray) parser.parse(new FileReader("config.json"));

      for (Object o : readfile) {
        JSONObject browser = (JSONObject) o;
        String browsername = (String) browser.get("name");
        System.out.println("Processing: " + browsername);
        JSONArray files = (JSONArray) browser.get("files");

        final XYSeries dat = new XYSeries(browsername);

        int i = 1;
        for (Object f : files) {
          //System.out.println(filename);
          String filename = (String) f.toString();
          System.out.println(filename);

          dat.add(i, jsonextract(filename));
          i++;
        }

        dataset.addSeries(dat);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return dataset;
  }

}
