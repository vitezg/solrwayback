package dk.kb.netarchivesuite.solrwayback.parsers;

import java.io.File;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;

public class WarcParser {

    private static final Logger log = LoggerFactory.getLogger(WarcParser.class);
    
    
    /*
     *Header example(notice the two different parts):
     *WARC/1.0
     *WARC-Type: response
     *WARC-Target-URI: http://www.boerkopcykler.dk/images/low_Trance-27.5-2-LTD-_20112013_151813.jpg
     *WARC-Date: 2014-02-03T18:18:53Z
     *WARC-Payload-Digest: sha1:C4HTYCUOGJ2PCQIKSRDAOCIDMFMFAWKK
     *WARC-IP-Address: 212.97.133.94
     *WARC-Record-ID: <urn:uuid:1068b604-f3d5-40b9-8aaf-7ed0df0a20b3>
     *Content-Type: application/http; msgtype=response
     *Content-Length: 7446
     *
     *HTTP/1.1 200 OK
     *Content-Type: image/jpeg
     *Last-Modified: Wed, 20 Nov 2013 14:18:21 GMT
     *Accept-Ranges: bytes
     *ETag: "8034965dfbe5ce1:0"
     *Server: Microsoft-IIS/7.0
     *X-Powered-By: ASP.NET
     *Date: Mon, 03 Feb 2014 18:18:53 GMT
     *Connection: close
     *Content-Length: 7178
     */
    public static ArcEntry getWarcEntry(String warcFilePath, long warcEntryPosition) throws Exception {
        RandomAccessFile raf=null;
        try{
            ArcEntry warcEntry = new ArcEntry();
            raf = new RandomAccessFile(new File(warcFilePath), "r");
            raf.seek(warcEntryPosition);

            String line = raf.readLine(); // First line

            if  (!(line.startsWith("WARC/"))) //No version check yet
            {            
                throw new IllegalArgumentException("WARC header is not WARC/'version', instead it is : "+line);
            }            
            
            while (!"".equals(line)) { // End of warc first header block is an empty line
                line = raf.readLine();
                populateWarcFirstHeader(warcEntry, line);
            }
            long afterFirst = raf.getFilePointer(); //Now we are past the WARC header and back to the ARC standard 
            line = raf.readLine();
            while (!"".equals(line)) { // End of warc second header block is an empty line
                line = raf.readLine();
                populateWarcSecondHeader(warcEntry, line);
            }

            int totalSize= (int) warcEntry.getWarcEntryContentLength();            
            
            // Load the binary blog. We are now right after the header. Rest will be the binary
            long headerSize = raf.getFilePointer() - afterFirst;
            long binarySize = totalSize - headerSize;

            //log.debug("Warc entry : totalsize:"+totalSize +" headersize:"+headerSize+" binary size:"+binarySize);
                        
            byte[] bytes = new byte[(int) binarySize];
            raf.read(bytes);
            raf.close();
            warcEntry.setBinary(bytes);
            return warcEntry;
        }
        catch(Exception e){
            throw e;
        }
        finally {
            if (raf!= null){
                raf.close();
             }
        }
    }

      public static String getWarcLastUrlPart(String warcHeaderLine) {        
        //Example:
        //WARC-Target-URI: http://www.boerkopcykler.dk/images/low_Trance-27.5-2-LTD-_20112013_151813.jpg
        String urlPath = warcHeaderLine.substring(16); // Skip WARC-Target-URI:                     
        String paths[] = urlPath.split("/");
        String fileName = paths[paths.length - 1];
        //log.debug("file:"+fileName +" was extracted from URL:"+warcHeaderLine);
        if (fileName == null){
            fileName="";
        }
        return fileName.trim();
    }

    private static String getWarcUrl(String warcHeaderLine) {        
        //Example:
        //WARC-Target-URI: http://www.boerkopcykler.dk/images/low_Trance-27.5-2-LTD-_20112013_151813.jpg
        String urlPath = warcHeaderLine.substring(16);                      
        return urlPath;
    }
    
    private static void populateWarcFirstHeader(ArcEntry warcEntry, String headerLine) {
        //log.debug("Parsing warc headerline(part 1):"+headerLine);                              
        if (headerLine.startsWith("WARC-Target-URI:")) {
            warcEntry.setFileName(getWarcLastUrlPart(headerLine));
            warcEntry.setUrl(getWarcUrl(headerLine));
        }    
        
        //Example:
        //Content-Length: 31131
        else if (headerLine.startsWith("Content-Length:")) {
            String[] contentLine = headerLine.split(" ");
            int totalSize = Integer.parseInt(contentLine[1].trim());               
            warcEntry.setWarcEntryContentLength(totalSize);                       
        }       
        
        else if (headerLine.startsWith("WARC-Date:")) {
            String[] contentLine = headerLine.split(" ");
                           
            warcEntry.setCrawlDate(contentLine[1].trim());                       
        }
        
        
     }

     private static void populateWarcSecondHeader(ArcEntry warcEntry, String headerLine) {
        //  log.debug("parsing warc headerline(part 2):"+headerLine);                
          //Content-Type: image/jpeg
          if (headerLine.startsWith("Content-Type:")) {
               String[] part1 = headerLine.split(":");
               String[] part2= part1[1].split(";");                        
               warcEntry.setContentType(part2[0].trim());          
          }  //Content-Length: 31131
          else if (headerLine.startsWith("Content-Length:")) {
              String[] contentLine = headerLine.split(" ");
              int totalSize = Integer.parseInt(contentLine[1].trim());               
              warcEntry.setContentLength(totalSize);                       
          }                         
      }
     
     
    
    
}