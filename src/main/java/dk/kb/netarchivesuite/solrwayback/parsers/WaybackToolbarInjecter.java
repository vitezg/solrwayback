package dk.kb.netarchivesuite.solrwayback.parsers;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.javac.util.Log;

import dk.kb.netarchivesuite.solrwayback.properties.PropertiesLoader;
import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;
import dk.kb.netarchivesuite.solrwayback.service.dto.IndexDoc;
import dk.kb.netarchivesuite.solrwayback.service.dto.SearchResult;
import dk.kb.netarchivesuite.solrwayback.solr.SolrClient;
import dk.kb.netarchivesuite.solrwayback.solr.WaybackStatistics;


public class WaybackToolbarInjecter {
  private static final Logger log = LoggerFactory.getLogger(WaybackToolbarInjecter.class);
  

  
  public static void main(String[] args) throws Exception{
       
    
    String html="<?xml version=\"1.1\" encoding=\"iso-8859-1\"?>"+
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"da\">"+
        "<head>"+
        "   <title>kimse.rovfisk.dk/katte/</title>"+
        "   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />"+         
        "   <link rel=\"stylesheet\" href=\"/style.css\"  type=\"text/css\" media=\"screen\" />"+
        " <link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS 2.0\" href=\"/rss2.php\" />"+
        "</head>"+
        "<body>"+
        "<a class=\"toplink\" href=\"/\">kimse.rovfisk.dk  </a><a class=\"toplink\" href=\"/katte/\">katte / </a><br /><br /><table cellspacing=\"8\"><tr><td></td><td class=\"itemw\"><a href=\"/katte/?browse=DSC00175.JPG\"><img class=\"lo\" src=\"/cache/katte/DSC00175.JPG\" /></a></td>"+
        "<td></td><td class=\"itemw\"><a href=\"/katte/?browse=DSC00209.JPG\"><img class=\"lo\" src=\"/cache/katte/DSC00209.JPG\" /></a></td>"+
        "</table><br />  </body>"+
        "</html>";
    
    
    
   
    WaybackStatistics stats = new WaybackStatistics();
    stats.setUrl_norm("jp.dk");
    stats.setHarvestDate("2015-09-17T17:02:03Z");        
    stats.setFirstHarvestDate("2015-09-17T17:02:03Z");
    stats.setNextHarvestDate("2015-09-17T17:02:03Z");
    stats.setPreviousHarvestDate("2015-09-17T17:02:03Z");
    stats.setLastHarvestDate("2015-09-17T17:02:03Z");
    stats.setNumberOfHarvest(101);
     
    String injectedHtml = injectInHmtl(html,stats);
    System.out.println(injectedHtml);   
  }
  
  
  
  public static String injectWaybacktoolBar(String arcFilePath, long offset, String html) throws Exception{       
    //Inject tooolbar
    String paths[] = arcFilePath.split("/");
    String fileName = paths[paths.length - 1];
                
    SearchResult search = SolrClient.getInstance().search("source_file_s:"+ fileName+"@"+offset, 1); //TODO refactor, multiple uses
    
    if (search.getNumberOfResults() == 0){
      log.error("No harvest found for :"+arcFilePath + " offset:"+offset);
      return html;// Dont inject atm
   }
    
    IndexDoc indexDoc = search.getResults().get(0);
    
    WaybackStatistics stats = SolrClient.getInstance().getWayBackStatistics(indexDoc.getUrl_norm(), indexDoc.getCrawlDate());
            
    stats.setHarvestDate(search.getResults().get(0).getCrawlDate());
    
    String injectedHtml =injectInHmtl(html, stats);
    return injectedHtml;
   
  }
  
  public static String injectInHmtl(String orgHtml, WaybackStatistics stats) throws Exception{
    Document doc = Jsoup.parse(orgHtml);
        
    String injectHtml = generateToolbarHtml(stats);
    doc.body().append(injectHtml);                    
    return doc.toString();    
  }
  
  private static String generateToolbarHtml(WaybackStatistics stats) throws Exception{
    

    
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date d = dateFormat.parse(stats.getHarvestDate());
        
    SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
        
    log.info(stats.toString());
    String inject = 
    "<!-- BEGIN WAYBACK TOOLBAR INSERT -->" +
    "   <div class=\"open\" id=\"tegModal\" style=\"\">" +
    "       <div><a onclick=\"toggleModal();return false\" id=\"toggleSpinner\" href=\"#\">Hide</a></div>" +
    "       <div><a onclick=\"closeModal();return false\" id=\"closeSpinner\" href=\"#\">Close</a></div>" +
    "       <div id=\"tegContent\">" +
    "           <div class=\"infoLine\">" +
    "               <span class=\"label\">Harvest date:</span>" +
    "               <span class=\"dynamicData\">"+longFormat.format(d)+"</span>" +
    "           </div>" +
    "           <div class=\"infoLine\">" +
    "               <span class=\"label\">Url:</span>" +
    "               <span class=\"dynamicData\">"+stats.getUrl_norm()+"</span>" +
    "               <span class=\"inlineLabel\">#Harvested:</span>" +
    "               <span class=\"dynamicData\">"+stats.getNumberOfHarvest() +"</span>" +
    "            </div>" +
    "            <div class=\"infoLine\">" +
    "               <span class=\"label\">Domain:</span>" +
    "               <span class=\"dynamicData\">"+generateDomainGraphLink(stats.getDomain()) +"</span>" +
    "               <span class=\"inlineLabel\">#Harvested:</span>" +
    "               <span class=\"dynamicData\">"+stats.getNumberHarvestDomain()+"</span>" +
    "               <span class=\"inlineLabel\">#Content length harvested:</span>" +
    "               <span class=\"dynamicData\">"+stats.getDomainHarvestTotalContentLength()+"</span>" +
    "            </div>" +
    "           <div class=\"paging\">" +
    "               <div class=\"pagingBlock\">" +
    "                   <span class=\"inlineLabel\">First:</span>"+ generateWaybackLinkFromCrawlDateAndUrl(stats.getUrl_norm(), stats.getFirstHarvestDate()) +
   
    "               </div> " +
    "               <div class=\"pagingBlock\">" +
    "                   <span class=\"inlineLabel\">Previous:</span> " + generateWaybackLinkFromCrawlDateAndUrl(stats.getUrl_norm(), stats.getPreviousHarvestDate()) +

    "               </div> " +
    "               <div class=\"pagingBlock\">" +
    "                   <span class=\"inlineLabel\">Next:</span> " + generateWaybackLinkFromCrawlDateAndUrl(stats.getUrl_norm(), stats.getNextHarvestDate()) +
    
    "                   </div> " +
    "               <div class=\"pagingBlock\">" +
    "                   <span class=\"inlineLabel\">Last:</span>" + generateWaybackLinkFromCrawlDateAndUrl(stats.getUrl_norm(), stats.getLastHarvestDate()) + 
    
    "               </div>" +
    "           </div>" +
    "      </div>" +
    "       <style>" +
    "       #tegModal{z-index: 999999 !important; color: black; font-size: 14px; font-family: arial, Helvetica,sans-serif;background: #ffffff; border: 1px solid black;border-radius: 4px; box-shadow: 0 0 5px 5px #ccc; display: block; left: calc(50% - 450px); opacity: 1; padding: 1.5em 1.5em .5em;" +
    " position:fixed; top: 25%; width: 900px; z-index: 500;" +
    "               transition: left 0.4s, opacity 0.3s, padding 0.3s, top 0.4s, width 0.3s;}" +
    "       #tegModal.closed {box-shadow: 0 0 0 0; left: 3px;opacity: 0.5; padding:1em 1em 0 0; top: 3px; width: 30px;}" +
    "       #toggleSpinner, #closeSpinner{float: right; margin: -.8em -.5em 2em 2em;}" +
    "       #toggleSpinner{margin-left: 1em;}" +
    "       #tegModal.closed #tegContent,#tegModal.closed #closeSpinner{display: none}" +
    "       #tegModal .infoLine{margin-bottom: .5em;}" +
    "       #tegModal a {color: #0000cc; font-size: 14px; text-decoration: none}" +
    "       #tegModal a:hover {color: #0000cc; text-decoration: underline}"+    
    "       #tegModal .label{display: inline-block;font-weight: bold; min-width: 110px;}" +
    "       #tegModal .inlineLabel{display: inline-block;font-weight: bold; margin: 0 .2em 0 .8em;}" +
    "       #tegModal .paging .inlineLabel{margin: 0 .5em 0 .1em;}" +
    "       #tegModal .paging{border-top: 1px solid #ccc; margin-top: 1em; padding-top: 0.8em;}" +
    "       #tegModal .pagingBlock{display: inline-block; margin-right: .8em}" +
    "       </style>" +
    "       <script type=\"text/javascript\">" +
    "           function toggleModal(){" +
    "               if(document.getElementById(\"tegModal\").className == \"open\"){" +
    "                   document.getElementById(\"tegModal\").className = \"closed\";" +
    "                   document.getElementById(\"toggleSpinner\").innerHTML = \"Open\";" +
    "               }else{" +
    "                   document.getElementById(\"tegModal\").className = \"open\";" +
    "                   document.getElementById(\"toggleSpinner\").innerHTML = \"Hide\";" +
    "               }" +
    "           }           " +
    "           function closeModal(){" +
    "               document.getElementById(\"tegModal\").style.display = \"none\";" +
    "           }" +
    "       </script>" +
    "   </div>" +
    "<!-- END WAYBACK TOOLBAR INSERT -->";
  return inject;
  }
  
  private static String generateWaybackLinkFromCrawlDateAndUrl(String url_norm, String crawlDate) throws Exception{
    
    if (crawlDate != null){
      

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      Date d = dateFormat.parse(crawlDate);
                
      SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
      String dateFormatted = longFormat.format(d);
            
      String urlEncoded=URLEncoder.encode(url_norm, "UTF-8");  
      return "<a href=\""+PropertiesLoader.WAYBACK_BASEURL+"services/viewhref?url="+urlEncoded+"&crawlDate="+crawlDate+"\" title=\""+dateFormatted+"\">"+dateFormatted+"</a>";
    }
    else{
      return ("none");
    }          
  }
  
  private static String generateDomainGraphLink(String domain){
    //TODO property
    return "<a href=\""+PropertiesLoader.WAYBACK_BASEURL+"waybacklinkgraph.jsp?domain="+domain+"\" target=\"new\">"+domain+"</a>";
  }
  
  
}