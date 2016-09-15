package hw2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|jpeg|png|ico|mp3|mp3|zip|gz))$");
	private final static Pattern filterXML = Pattern.compile(".*(format=xml|feed).*");
	private final static Pattern filterJSON = Pattern.compile(".*(\\wp-json).*");
	private final static Pattern filterCSS = Pattern.compile(".*(\\.css).*");
	
	private static final Pattern docPatterns = Pattern.compile(".*(\\.(doc|docx|pdf|html|htm?))$");
		
	private static File storageFolder;
	private static File fetch, visit, urls, pagerankdata, docid;
	
	public static void configure(String storageFolderName) {

	    storageFolder = new File(storageFolderName);
	    if (!storageFolder.exists()) {
	      storageFolder.mkdirs();
	    }
	    
	    fetch = new File(storageFolder.getAbsolutePath() + "/result/fetch.csv");
	    visit = new File(storageFolder.getAbsolutePath() + "/result/visit.csv");
	    urls = new File(storageFolder.getAbsolutePath() + "/result/urls.csv");
	    pagerankdata = new File(storageFolder.getAbsolutePath() + "/result/pagerankdata.csv");
	    docid = new File(storageFolder.getAbsolutePath() + "/result/docid.csv");
	  }
	
			 /**
			 * This method receives two parameters. The first parameter is the page
			 * in which we have discovered this new url and the second parameter is
			 * the new url. You should implement this function to specify whether
			 * the given url should be crawled or not (based on your crawling logic).
			 * In this example, we are instructing the crawler to ignore urls that
			 * have css, js, git, ... extensions and to only accept urls that start
			 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
			 * referringPage parameter to make the decision.
			 */
			 @Override
			 public boolean shouldVisit(Page referringPage, WebURL url) {
			 String href = url.getURL().toLowerCase();
			 
			 try
				{
				    FileWriter writer = new FileWriter(urls, true);
					 
				    writer.append(url.getURL());
				    writer.append(',');
				    if (href.startsWith("http://priceschool.usc.edu"))
				    	writer.append("OK");
				    else if (href.contains("usc.edu"))
				    	writer.append("USC");
				    else
				    	writer.append("outUSC");
				    writer.append('\n');
				    
				    writer.flush();
				    writer.close();
				}
				catch(IOException e)
				{
				     e.printStackTrace();
				} 
			 
			 return !FILTERS.matcher(href).matches() && !filterCSS.matcher(href).matches() &&
					 !filterXML.matcher(href).matches() && !filterJSON.matcher(href).matches() &&
					 href.startsWith("http://priceschool.usc.edu");
			 }
			 
			 /**
			  * This function is called when a page is fetched and ready
			  * to be processed by your program.
			  */
			  @Override
			  public void visit(Page page) {
			  String url = page.getWebURL().getURL();
			  String contentType = new String();
			  
			  try {
			  URL url1 = new URL(url);
			  HttpURLConnection connection = (HttpURLConnection)  url1.openConnection();
			  connection.setRequestMethod("HEAD");
			  connection.connect();
			  contentType = connection.getContentType();
			  }
			  catch (MalformedURLException e) {
		            e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			  
			  if (page.getParseData() instanceof HtmlParseData) {
			  HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			  String html = htmlParseData.getHtml();
			  Set<WebURL> links = htmlParseData.getOutgoingUrls();

			  try
              {
				  String hashedName = URLEncoder.encode(url, "UTF-8");
				  File outputfile = new File(storageFolder.getAbsolutePath() + "/" + hashedName + ".html");
				  //If file doesnt exists, then create it
                   if(!outputfile.exists()){
                       outputfile.createNewFile();
                   }

                  FileWriter fw = new FileWriter(outputfile); 
                    BufferedWriter bufferWritter = new BufferedWriter(fw);
                    bufferWritter.write(html);
                    fw.write(html);
                    bufferWritter.close();
                    fw.close();
                  
                  FileWriter writer = new FileWriter(visit, true);
				    writer.append(url);
				    writer.append(',');
				    Long size = (Long) outputfile.length()/1024; 
				    writer.append(size.toString());
				    writer.append(',');
				    Integer outlinks = (Integer) links.size(); 
				    writer.append(outlinks.toString());
				    writer.append(',');
				    writer.append(contentType);
				    writer.append('\n');
				    writer.flush();
				    writer.close();
				    
				  FileWriter pagerank = new FileWriter(pagerankdata, true);
				    pagerank.append(url);
				    for (WebURL link : links){
				    	pagerank.append(',');
				    	pagerank.append(link.getURL());
				    }
				    pagerank.append('\n');
				    pagerank.flush();
				    pagerank.close(); 
				    
				  FileWriter id = new FileWriter(docid, true);
				    id.append(url);
				    id.append(',');
				    id.append(hashedName);
				    id.append('\n');
				    id.flush();
				    id.close();
                  
              }catch(IOException e)
              {
                  System.out.println("IOException : " + e.getMessage() );
                  e.printStackTrace();
              }
			  }
			  
			  else if (docPatterns.matcher(url).matches()) {

				    // get a unique name for storing this document
				    String filename = "";
				    
				    try {
				    	String hashedName = URLEncoder.encode(url, "UTF-8");

					    // store document
					    filename = storageFolder.getAbsolutePath() + "/" + hashedName;
					    File outputfile = new File(filename);
				      Files.write(page.getContentData(), outputfile);
				      
				      FileWriter writer = new FileWriter(visit, true);
						 
					    writer.append(url);
					    writer.append(',');
					    Long size = (Long) outputfile.length()/1024;
					    writer.append(size.toString());
					    writer.append(',');
					    if (page.getParseData() instanceof BinaryParseData){
					    	BinaryParseData binaryParseData = (BinaryParseData) page.getParseData();
					    	Set<WebURL> links = binaryParseData.getOutgoingUrls();
					    	Integer outlinks = (Integer) links.size();
					    	writer.append(outlinks.toString());
					    }
					    else if (page.getParseData() instanceof TextParseData){
					    	TextParseData textParseData = (TextParseData) page.getParseData();
					    	Set<WebURL> links = textParseData.getOutgoingUrls();
					    	Integer outlinks = (Integer) links.size();
					    	writer.append(outlinks.toString());
					    }

					    else
					    	writer.append(hashedName);
					    writer.append(',');
					    writer.append(contentType);
					    writer.append('\n');
					    
					    writer.flush();
					    writer.close();
				    } catch (IOException iox) {
				      logger.error("Failed to write file: " + filename, iox);
					}
			  }  		  
  	}
			  
			  @Override
				protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
					
					try
					{
					    FileWriter writer = new FileWriter(fetch, true);
						 
					    writer.append(webUrl.getURL());
					    writer.append(',');
					    Integer status = (Integer)statusCode;
					    writer.append(status.toString());
					    writer.append('\n');
					    
					    writer.flush();
					    writer.close();
					}
					catch(IOException e)
					{
					     e.printStackTrace();
					} 
					
				}
			  
			  @Override
			  protected void onUnhandledException(WebURL webUrl, Throwable e) {
				      String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
				      logger.warn("Unhandled exception while fetching {}: {}", urlStr, e.getMessage());
}
