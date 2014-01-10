package dk.statsbiblioteket.findex.oai.validation;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * This class is not active pt. it can be used to validate OAI xml. The problem they are bundled in 100 and 
 * error in one of them will make the complete XML file fail to validate.
 * MAYBE try fix this later but checking if it validate and try cut out the bad part.
 */


public class XMLTest  extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(XMLTest.class);
    String filename = "";

    private String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void run() {
       if (true){
           logger.error("XMLTest must not be called atm.");      
       return;
       }
     
        
        
        if (!getFilename().equals("")) {
            //System.out.println("INFO: XML-Validerer: " + getFilename());
            logger.debug("Validating '" + getFilename() + "'");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
                docBuilder.isValidating();
                docBuilder.isNamespaceAware();
                Document doc = docBuilder.parse (new File(getFilename()));
                //System.out.println("INFO: " + getFilename() + " validerer");
                logger.debug("File '" + getFilename() + "' validates");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                File file = new File(getFilename());
                File file2 = new File(getFilename().replaceAll("\\.xml",".txt"));
                boolean success = file.renameTo(file2);
                logger.debug(getFilename() + " is not a valid xml-file. Renaming '.xml' to '.txt'");
                if (!success) {
                    logger.error("Could not rename '" + getFilename() + "'");
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                //System.out.println("FEJL: Forkert filnavn - filen kan ikke findes/åbnes");
                logger.error("Could not open file '" + getFilename() + "'");
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                File file = new File(getFilename());
                File file2 = new File(getFilename().replaceAll("\\.xml",".txt"));
                boolean success = file.renameTo(file2);
                //System.out.println("FEJL: Filen: " +  getFilename() + " validerer ikke - omdøbes til .txt");
                logger.debug(getFilename() + " is not a valid xml-file. Renaming '.xml' to '.txt'");
                if (!success) {
                    //System.out.println("FEJL: Filen: " +  getFilename() + " kunne ikke omdøbes");
                    logger.error("Could not rename '" + getFilename() + "'");
                }
            }
        } else {
            //System.out.println("FEJL: Intet filnavn angivet");
            logger.error("No filename");
        }
    }
}
