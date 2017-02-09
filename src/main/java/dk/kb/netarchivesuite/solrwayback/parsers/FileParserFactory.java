package dk.kb.netarchivesuite.solrwayback.parsers;

import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;

public class FileParserFactory {

    public static ArcEntry getArcEntry(String arcFilePath, long offset) throws Exception{

        if (arcFilePath == null ){           
            throw new IllegalArgumentException("arcFilePath is null");        
        }

        ArcEntry arcEntry = null; 

        if (arcFilePath.toLowerCase().endsWith(".warc")){
            arcEntry = WarcParser.getWarcEntry(arcFilePath, offset);     
        }
        else if (arcFilePath.toLowerCase().endsWith(".arc")){
            arcEntry = ArcParser.getArcEntry(arcFilePath, offset);
        }
        else{
            throw new IllegalArgumentException("File not arc or warc:"+arcFilePath);
        }

        return arcEntry;
    }

}