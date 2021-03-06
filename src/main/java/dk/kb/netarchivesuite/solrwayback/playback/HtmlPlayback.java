package dk.kb.netarchivesuite.solrwayback.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.netarchivesuite.solrwayback.parsers.HtmlParseResult;
import dk.kb.netarchivesuite.solrwayback.parsers.HtmlParserUrlRewriter;
import dk.kb.netarchivesuite.solrwayback.parsers.WaybackToolbarInjecter;
import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;
import dk.kb.netarchivesuite.solrwayback.service.dto.IndexDoc;

public class HtmlPlayback  extends PlaybackHandler{
  
  private static final Logger log = LoggerFactory.getLogger(HtmlPlayback.class);
  
  public HtmlPlayback(ArcEntry arc, IndexDoc doc, boolean showToolbar){
    super(arc,doc,showToolbar);
  }

  @Override
  public ArcEntry playback() throws Exception{    
    log.debug(" Generate webpage from FilePath:" + doc.getSource_file_path() + " offset:" + doc.getOffset());
    long start = System.currentTimeMillis();
    HtmlParseResult htmlReplaced = HtmlParserUrlRewriter.replaceLinks(arc);        
      String textReplaced=htmlReplaced.getHtmlReplaced();             
      boolean xhtml =doc.getContentType().toLowerCase().indexOf("application/xhtml") > -1;            
    //Inject tooolbar
     if (showToolbar ){ //If true or null. 
        textReplaced = WaybackToolbarInjecter.injectWaybacktoolBar(doc.getSource_file_path(),doc.getOffset(),htmlReplaced , xhtml);
     }
    
      arc.setBinary(textReplaced.getBytes(arc.getContentEncoding()));        
     log.info("Generating webpage total processing:"+(System.currentTimeMillis()-start));
    return arc;
  }
  
}