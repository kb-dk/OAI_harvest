package dk.statsbiblioteket.findex.oai;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAIPropertiesLoader {
	
	private static final Logger log = LoggerFactory.getLogger(OAIPropertiesLoader.class);
	private static final String OAI_PROPERTY_FILE = "oai.properties";
	 
	
	
	private static final String oaitargetfile_PROPERTY="SBHarvester.oaitargetfile";	 
	private static final String datadir_PROPERTY="SBHarvester.datadir";
    private static final String timedir_PROPERTY="SBHarvester.timedir";
    private static final String lastharvesttime_PROPERTY="SBHarvest.lastharvesttime";
    private static final String targetinfofile_PROPERTY="SBHarvest.targetinfofile";
    private static final String downloadfile_PROPERTY="SBHarvest.downloadfile";
    private static final String maxtries_PROPERTY="OAIProxy.maxtries";
    private static final String user_agent_PROPERTY="OAIProxy.user_agent";
    private static final String user_email_PROPERTY="OAIProxy.user_email";
	
	
	public static String DBFILE = null;
	public static String DBBACKUPFOLDER = null;	
	public static String oaitargetfile= null;     
	public static String datadir= null;
    public static String timedir= null;    
    public static String lastharvesttime= null;
	public static String targetinfofile= null;
	public static String downloadfile= null;
	public static int maxtries= 0;
	public static String user_agent= null;
	public static String user_email= null;
	
	static{
		log.info("Initializing OAI-properties");
		try {
			initProperties();		
		} 
		catch (Exception e) {
			e.printStackTrace();
			log.error("Could not load property file:"+OAI_PROPERTY_FILE);					
		}
	}
		
	private static void initProperties()  throws Exception{

		String user_home=System.getProperty("user.home");
		log.info("Load properties: Using user.home folder:" + user_home);
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(user_home,OAI_PROPERTY_FILE)), "ISO-8859-1");

		Properties serviceProperties = new Properties();
		serviceProperties.load(isr);
		isr.close();

	
	    oaitargetfile= serviceProperties.getProperty(oaitargetfile_PROPERTY);     
	    datadir= serviceProperties.getProperty(datadir_PROPERTY);
	    timedir= serviceProperties.getProperty(timedir_PROPERTY);	    	  
	    lastharvesttime= serviceProperties.getProperty(lastharvesttime_PROPERTY);
	    targetinfofile= serviceProperties.getProperty(targetinfofile_PROPERTY);
	    downloadfile= serviceProperties.getProperty(downloadfile_PROPERTY);
	    maxtries= Integer.parseInt(serviceProperties.getProperty(maxtries_PROPERTY));
	    user_agent= serviceProperties.getProperty(user_agent_PROPERTY);
	    user_email= serviceProperties.getProperty(user_email_PROPERTY);
				    		
		log.info("Property:"+ oaitargetfile_PROPERTY +" = "+    oaitargetfile);
		log.info("Property:"+ datadir_PROPERTY +" = "+datadir );
	    log.info("Property:"+  lastharvesttime_PROPERTY +" = "+  lastharvesttime );
		log.info("Property:"+ timedir_PROPERTY +" = "+ timedir );
		log.info("Property:"+ targetinfofile_PROPERTY +" = "+ targetinfofile);
		log.info("Property:"+ maxtries_PROPERTY +" = "+downloadfile );
		log.info("Property:"+ user_agent_PROPERTY +" = "+  user_agent );
		log.info("Property:"+ user_email +" = "+  user_email_PROPERTY );
		
		
	}
	
}
