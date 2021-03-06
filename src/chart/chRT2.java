package chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class chRT2 {

  public static int webPlatformParse(final String fileName) {
    int count = 0;
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
            count++;
          }
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return count;
  }
  
  
  public static int grinderParse(final String filename){
    int count=0;
    try{
      final JSONParser parser = new JSONParser();
      final Object obj = parser.parse(new FileReader("data/"+filename+".json"));
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
    /*final JFreeChart xylineChart = ChartFactory.createXYLineChart(
        "Web-Platform Test Results", "Tests Conducted", "Tests Failed",
        configParser("web-platform-files"), PlotOrientation.VERTICAL, true, true,
        false);*/
    
    /*final JFreeChart grinderChart = ChartFactory.createXYLineChart(
        "Grinder Test Results", "Tests Conducted", "Tests Failed",
        configParser("grinder-files"), PlotOrientation.VERTICAL, true, true,
        false);*/
    
    final JFreeChart grinderChart = ChartFactory.createTimeSeriesChart("Grinder Test Results","Date","Tests Failed",configParser("grinder-files"),true,true,false);
    
    System.out.println("\n*********Creating charts*********");
    
    //System.out.println("Creating web-platform test chart");
    //exportChart(xylineChart,"Chart1.PNG");
    System.out.println("\nCreating grinder test chart");
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
    
 
    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
    
    //final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
    //domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
   
    final File XYLine = new File(fileName);
    ChartUtilities.saveChartAsPNG(XYLine, chart , 500, 500);
    System.out.println("Exported chart to: " + fileName);
  }
  

  public static TimeSeriesCollection configParser(String testName){
    List<Integer> ResultList;
    XYSeries newSeries;
    TimeSeries newTimeSeries;
    final XYSeriesCollection dataSet = new XYSeriesCollection();
    final TimeSeriesCollection timeSeriesdataset = new TimeSeriesCollection();
    
    try {
      System.out.println("\nProcessing "+ testName +" test Results");
      final JSONParser parser = new JSONParser();
      final JSONObject readfile = (JSONObject) parser.parse(new FileReader("Config.json"));
      
      final List<String> dates= (List<String>) readfile.get("Dates");
      
      final JSONArray files = (JSONArray) readfile.get("Files");

      for (final Object o : files) {
        final JSONObject browser = (JSONObject) o;
        final String browsername = (String) browser.get("name");
        System.out.println("Processing: " + browsername);
        
        final JSONArray TestArrayFiles = (JSONArray) browser.get(testName);
      
        //if(testName=="web-platform-files")
        //{
          //ResultList = jsonTestFileResult(TestArrayFiles,testName);
          //newSeries = createSeries(ResultList,browsername);
          //dataSet.addSeries(newSeries);
        //}
        //else
        //{
          ResultList = jsonTestFileResult(TestArrayFiles,testName);
          newTimeSeries = timeSeries(ResultList,browsername,dates);
          timeSeriesdataset.addSeries(newTimeSeries);
        //}
     }
    }catch(final Exception e){
      e.printStackTrace();
    }
    return timeSeriesdataset;
  }
    
  
  public static List<Integer> jsonTestFileResult(JSONArray TestArrayFiles,String testName){
   List<Integer> testResults = new ArrayList<>();
     
   for(final Object o : TestArrayFiles){     
     final String Filename = (String) o.toString();
     if(testName == "web-platform-files")
        testResults.add(webPlatformParse(Filename));
     else
       testResults.add(grinderParse(Filename));
   }
   return testResults;
  } 
  
  
  public static XYSeries createSeries(List<Integer> ResultList,String browsername){
    final XYSeries Series = new XYSeries(browsername);  
    
    int i=1;
    for(int result : ResultList){
      Series.add(i,result);
      i++;
    }
    return Series;
  }
  
  @SuppressWarnings("deprecation")
  public static TimeSeries timeSeries(List<Integer> ResultList,String browsername,List<String> dates){
    TimeSeries Series = new TimeSeries(browsername, Day.class);
    int len = dates.size();
    
    for(int i=0;i<len;i++)
    {
      int year = Integer.parseInt(dates.get(i).split("-")[0]);
      int  month =Integer.parseInt(dates.get(i).split("-")[1]);
      int date =Integer.parseInt(dates.get(i).split("-")[2]);
      
      Series.addOrUpdate(new Day(date,month,year),ResultList.get(i));
     
    }
    return Series;
  }
}
