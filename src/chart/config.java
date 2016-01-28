package chart;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public class config {
  public static void main(final String[] args) throws ParseException, java.text.ParseException {
    try {
      new config().run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  
  class dateCompartor implements Comparator<TagInfo>{
    public int compare(TagInfo t1,TagInfo t2){
      return t1.date.compareTo(t2.date);
    } 
  }

  private void run() throws IOException, ParseException, java.text.ParseException {
    final String tags = fetch("https://api.github.com/repos/UprootStaging/grinderbaselines/git/refs/tags");
    
    final JSONObject config = new JSONObject();   
    
    final JSONArray tagArray = new JSONArray();
    
    final JSONArray dateArray = new JSONArray();
    
    JSONObject gngrObj = new JSONObject();
    gngrObj.put("name", "gngr");
    JSONArray gngrFiles = new JSONArray();
    
    JSONObject firefoxObj = new JSONObject();
    firefoxObj.put("name", "firefox");
    JSONArray firefoxFiles = new JSONArray();
    
    JSONArray files = new JSONArray();
   
    List<TagInfo> infos =gettagInfo(tags);
    Collections.sort(infos,new dateCompartor());
    
    for(TagInfo ti:infos){  
      FileWriter firefox = new FileWriter("data/firefox"+ti.name+".json");
      firefox.write(fetch("https://raw.githubusercontent.com/UprootStaging/grinderBaselines/"+ti.name+"/firefox/results.json"));
      firefox.close();
      FileWriter gngr = new FileWriter("data/gngr"+ti.name+".json");
      gngr.write(fetch("https://raw.githubusercontent.com/UprootStaging/grinderBaselines/"+ti.name+"/gngr/results.json"));
      gngr.close(); 
      tagArray.add(ti.name);
      
      dateArray.add(ti.date);
      
      gngrFiles.add("gngr"+ti.name);
      firefoxFiles.add("firefox"+ti.name);
      
     /* JSONObject tiObj = new JSONObject();
      tiObj.put("tag",ti.name);
      tiObj.put("date",ti.date);
      tiObj.put("messages", ti.message);
      config.add(tiObj);*/
    }
    System.out.println("Done 2");
    
    gngrObj.put("grinder-files", gngrFiles);
    firefoxObj.put("grinder-files", firefoxFiles);
    
    files.add(gngrObj);
    files.add(firefoxObj);
    
    config.put("Tags",tagArray);
    config.put("Dates",dateArray);
    config.put("Files", files);
    
    
    ObjectMapper mapper = new ObjectMapper();
    String test =config.toJSONString();
    System.out.println(test);
    Object json = mapper.readValue(test, Object.class);
    String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    try(FileWriter file = new FileWriter("Config.json")){
      file.write(indented);
    }
    
    Collections.sort(infos,new dateCompartor().reversed());
    
    try(FileWriter file = new FileWriter("testResult.html")){
      file.write("<!DOCTYPE html>");
      file.write("<html>");
      file.write("<head>");
      file.write("<title></title>");
      file.write("<body bgcolor=\"grey\"><br>");
      file.write("<div id=\"images\">");
      file.write("<img class=\"img\" border=\"0\" alt=\"Pic 1\" src=\"/home/akshay/workspace/chart/Chart1.PNG\"/></a>");
      file.write("<img class=\"img\" border=\"0\" alt=\"Pic 2\" src=\"/home/akshay/workspace/chart/Chart2.PNG\"/></a>");
      file.write("</div><br><br>");
      file.write("<h2 align=\"center\">UPDATES</h2>");
      file.write("<style>");
      file.write("table, th, td {");
      file.write("border: 1px solid black;");
      file.write("border-collapse: collapse; }");
      file.write("th, td {");
      file.write("padding: 5px;");
      file.write("text-align: left;}");    
      file.write("</style></head>");
      file.write("<body>");
      file.write("<div id=\"table\">");
      file.write("<table style=\"width:100%\" align=\"center\">");
      file.write("<tr><th><center>Date</center></th><th><center>Message</center></th></tr>");
      

     for(TagInfo ti:infos){
       file.write("<tr><th><center>"+ti.date+"</center></th><th><center>"+ti.message+"</center></th></tr>");
     }
    
     file.write("</table></div>");
     file.write("<style type=\"text/css\">");
     file.write(".img { display: inline-block; margin-left: auto; margin-right: auto; width:450px; height:400px; padding:30px; }");
     file.write("#images{ text-align:center; }");
     file.write("#table{ width:400px; height:200px; margin-left:460px; }");
     file.write("</style> </body> </html>");
    }
    
    //System.out.println(config);
    
   /* try(FileWriter file = new FileWriter("Config.json")){
      file.write(config.toJSONString());
     
    }*/
    
    System.out.println("Dpone 3");
   
  }
  
  public static List<TagInfo> gettagInfo (String tags) throws ParseException, IOException, java.text.ParseException{
    List<TagInfo> taginfos = new ArrayList<>();
    
    
    final JSONParser parser = new JSONParser();
    final JSONArray readObject = (JSONArray) parser.parse(tags);
    
    for(Object o : readObject){
      final JSONObject innerObj = (JSONObject) o;
      final String  tagName1 = (String) innerObj.get("ref");
      final String tagName = tagName1.split("/")[2];
      
      final JSONObject object = (JSONObject) innerObj.get("object");
      final String url = (String) object.get("url"); 
      final String innerJson = fetch(url);
      
      final JSONObject tagInfo =(JSONObject)parser.parse(innerJson);
  
      final String message =(String)tagInfo.get("message");
      
      final JSONObject tagger =(JSONObject)tagInfo.get("tagger");
      final String date1 = (String)tagger.get("date");
      final String date =(String)date1.split("T")[0];
      //String dateInString = date;
      
      //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      //Date newDate;
      
      //newDate = formatter.parse(dateInString);
      
      TagInfo ti=new TagInfo();
      ti.name=tagName;
      ti.date=date;
      ti.message=message;
      taginfos.add(ti); 
    }
    System.out.println("Done 1");
    return taginfos;
  }
  
  static class TagInfo{
    public String name,message;
    public String date;
  }
  

  final private static OkHttpClient client = new OkHttpClient();

  private static String fetch(final String url) throws IOException {
    final Request request = new Request.Builder()
        .url(url)
        .build();

    final Response response = client.newCall(request).execute();
    return response.body().string();
  }
}
