package chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
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

  public static int jsonextract(final String fileName) {
    int c = 0;
    try {
      final JSONParser parser = new JSONParser();
      final Object obj = parser.parse(new FileReader(fileName));
      final JSONObject jsonObject = (JSONObject) obj;
      final JSONArray results = (JSONArray) jsonObject.get("results");
      final Iterator<?> iterator = results.iterator();

      while (iterator.hasNext()) {
        final JSONObject innerobj = (JSONObject) iterator.next();
        final JSONArray sub = (JSONArray) innerobj.get("subtests");
        final Iterator<JSONObject> i = sub.iterator();
        while (i.hasNext()) {
          final JSONObject in = (JSONObject) i.next();
          final String st = (String) in.get("status");

          if (st.contains("FAIL")) {
            c++;
          }
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return c;
  }
  
  
  public static int grinderExtract(final String filename){
    int count=0;
    try{
      final JSONParser parser = new JSONParser();
      final Object obj = parser.parse(new FileReader(filename));
      final JSONObject jsonObject = (JSONObject) obj;
      final JSONObject reftests = (JSONObject) jsonObject.get("css21-reftests");
      final JSONArray results = (JSONArray) reftests.get("results");
      final Iterator<JSONObject> tests = results.iterator();
      while(tests.hasNext()){
        final JSONObject subtests = (JSONObject)tests.next();
        final String res = (String) subtests.get("pass");
        
        if(res.contains("false")){
          count++;
        }
      }
      
    }catch(final Exception e){
      e.printStackTrace();
    }
    return count;
  }

  public static void main(final String[] args) throws Exception {

    final JFreeChart xylineChart = ChartFactory.createXYLineChart(
        "Web-Platform Test Results", "Tests Conducted", "Tests Failed",
        collection(), PlotOrientation.VERTICAL, true, true,
        false);
    
    final JFreeChart grinderChart = ChartFactory.createXYLineChart(
        "Grinder Test Results", "Tests Conducted", "Tests Failed",
        collection2(), PlotOrientation.VERTICAL, true, true,
        false);
    
    System.out.println("Creating charts");
    
    System.out.println("Creating web-platform test chart");
    exportChart(xylineChart,"Chart1.PNG");
    System.out.println("Creating grinder test chart");
    exportChart(grinderChart,"Chart2.PNG");

  }
  
  
  public static void exportChart(final JFreeChart chart, final String fileName) throws Exception{
    final ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(600, 600));
    final XYPlot plot = chart.getXYPlot();
    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.RED);
    renderer.setSeriesPaint(1, Color.GREEN);
    renderer.setSeriesPaint(2, Color.YELLOW);
    renderer.setSeriesStroke(0, new BasicStroke(4.0f));
    renderer.setSeriesStroke(1, new BasicStroke(4.0f));
    renderer.setSeriesStroke(2, new BasicStroke(4.0f));
    plot.setRenderer(renderer);
    plot.setBackgroundPaint(new Color(235,235,235));
    plot.setDomainGridlinePaint(Color.DARK_GRAY);
    plot.setRangeGridlinePaint(Color.DARK_GRAY);
    
    final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
   
    final File XYLine = new File(fileName);
    ChartUtilities.saveChartAsPNG(XYLine, chart , 500, 500);
    System.out.println("Exported chart to: " + fileName);
  }
  

  public static XYDataset collection() throws Exception {
    final XYSeriesCollection dataset = new XYSeriesCollection();
   
    try {
      System.out.println("Processing Web platform Results");
      final JSONParser parser = new JSONParser();
      final JSONArray readfile = (JSONArray) parser.parse(new FileReader("config.json"));

      for (final Object o : readfile) {
        final JSONObject browser = (JSONObject) o;
        final String browsername = (String) browser.get("name");
        System.out.println("Processing: " + browsername);
        final JSONArray files = (JSONArray) browser.get("web-platform-files"); 

        final XYSeries dat = new XYSeries(browsername);
      
        int i = 1;
        for (final Object f : files) {
          final String filename = (String) f.toString();
          dat.add(i, jsonextract(filename));
          i++;
        }
        dataset.addSeries(dat);
      }
      System.out.println("\n");
      
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return dataset;
  
  }
  
   
  public static XYDataset collection2() throws Exception {
    final XYSeriesCollection grdataset = new XYSeriesCollection();
  
    try {
      System.out.println("Processing grinder test Results");
      final JSONParser parser = new JSONParser();
      final JSONArray readfile = (JSONArray) parser.parse(new FileReader("config.json"));

      for (final Object o : readfile) {
        final JSONObject browser = (JSONObject) o;
        final String browsername = (String) browser.get("name");
        System.out.println("Processing: " + browsername);
       
        final JSONArray grinderFiles = (JSONArray) browser.get("grinder-files");

        final XYSeries grinder = new XYSeries(browsername);

        int i=1;
        for(final Object g : grinderFiles){
          final String grinderFilename = (String) g.toString(); 
          grinder.add(i,grinderExtract(grinderFilename));
          i++;
        }
        grdataset.addSeries(grinder);
      }
        System.out.println("\n");
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return grdataset;
  }
 
}
