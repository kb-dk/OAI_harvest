package dk.statsbiblioteket.findex.oai.sbharvest;

import dk.statsbiblioteket.findex.oai.OAIPropertiesLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  com.sun.org.apache.xpath.internal.XPathAPI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.security.Security;

/**
 * Created by IntelliJ IDEA.
 * User: hl
 * Date: 2006-07-24
 * Time: 13:57:10
 * To change this template use File | Settings | File Templates.
 */
public class SBHarvester {
    private static final Logger logger = LoggerFactory.getLogger(SBHarvester.class);
    static String oaitargets = "";
    static String datadir = "";
    static String timedir = "";
    static String scptarget = "";
    //static String configdir = null;

    public static void main(String[] args) throws Exception{
     
        
        String propertyFile = System.getProperty("OaiPropertyFile");
        if (propertyFile == null || "".equals(propertyFile)){
            System.out.println("Propertyfile location must be set. Use -DOaiPropertyFile={path to file}");            
            System.exit(1);
        }
        /*
        String log4JFile = System.getProperty("log4j.configuration");
                
        if (log4JFile  == null || "".equals(log4JFile )){
            System.out.println("Log4j configuration not defined, using default. Use -Dlog4j.configuration={path to file}");                        
        }
        */
     
        
        System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        SBHarvester sb = new SBHarvester();
        if (args != null && args.length > 0) {
            //configdir = args[0];
        }
        try {
            sb.executeHarvest();
        } catch (Exception e) {
            logger.error("harvest failed",e);
            for (int i = 0; i < e.getStackTrace().length; i++) {
                logger.error("     at " + e.getStackTrace()[i].toString());
            }
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void executeHarvest() throws Exception {
        Calendar now = new GregorianCalendar();
        String timestring = SBHarvest.calendarToString(now,true);
        this.logger.debug("Harvest initialized - " + timestring);
                        
        //System.out.println("Propurl: " + propurl);
    
        oaitargets = OAIPropertiesLoader.oaitargetfile;
        datadir = OAIPropertiesLoader.datadir + "harvest_" + timestring + "/";
        timedir = OAIPropertiesLoader.timedir;        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.isValidating();
        docBuilder.isNamespaceAware();
        Document doc = docBuilder.parse (new File(oaitargets));
        String xpath = "//targets//target";
        NodeList nodelist = XPathAPI.selectNodeList(doc, xpath);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node url = XPathAPI.selectSingleNode(nodelist.item(i),"url//text()");
            Node dest = XPathAPI.selectSingleNode(nodelist.item(i),"dest//text()");
            Node timedelay = XPathAPI.selectSingleNode(nodelist.item(i),"delaytime//text()");
            Node prefix = XPathAPI.selectSingleNode(nodelist.item(i),"prefix//text()");
            Node set = XPathAPI.selectSingleNode(nodelist.item(i),"set//text()");
            Node userNode = XPathAPI.selectSingleNode(nodelist.item(i),"user//text()");
            Node passwordNode = XPathAPI.selectSingleNode(nodelist.item(i),"password//text()");
            Node validate = XPathAPI.selectSingleNode(nodelist.item(i),"validatexml//text()");
            SBHarvester harvester = new SBHarvester();

            String user = null;
            String password= null;
            if (userNode != null && passwordNode != null){
              user = userNode.getNodeValue();
              password = passwordNode.getNodeValue();              
            }
            
            System.out.println("user:"+user);
            //System.out.println("password:"+password);
            
            //  boolean validatexml = true;
            boolean validatexml = false; //DISABLED
            if (validate != null && validate.getNodeValue().trim().equals("false")) {
                validatexml = false;
            }
            if (set == null) {
                harvester.doHarvest(dest.getNodeValue(),url.getNodeValue(),datadir + dest.getNodeValue() +"/",timedir + dest.getNodeValue() +"/",timedelay.getNodeValue(), prefix.getNodeValue(), null,validatexml, user, password);
            } else {
                harvester.doHarvest(dest.getNodeValue(),url.getNodeValue(),datadir + dest.getNodeValue() +"/",timedir + dest.getNodeValue() +"/",timedelay.getNodeValue(), prefix.getNodeValue(), set.getNodeValue(),validatexml, user, password);
            }
            //System.out.print(".");
        }
        
        logger.info("Harvest finished");
    }

    public void doHarvest(String targetName,String url, String outdirectory, String timedir, String timedelay, String target, String set, boolean validatexml, String user, String password) throws Exception{
       
        
        //logger.info("HarvestUrl: " + url +"    Destination: " + outdirectory);
        logger.info("HarvestUrl: " + url +"    Destination: " + outdirectory);
        SBHarvest sb = new SBHarvest();
        sb.setUrl(url);
        sb.setDir(outdirectory);
        sb.setTimedelay(timedelay);
        sb.setTimedir(timedir);
        sb.setTarget(target);
        sb.setSet(set);
        sb.setVerifyxml(validatexml);
        sb.setTargetName(targetName);
        sb.setUser(user);
        sb.setPassword(password);
        sb.harvestTarget();
    }

   
}
