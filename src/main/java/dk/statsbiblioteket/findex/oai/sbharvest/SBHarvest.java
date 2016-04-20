package dk.statsbiblioteket.findex.oai.sbharvest;

import dk.statsbiblioteket.findex.oai.OAIPropertiesLoader;
import dk.statsbiblioteket.findex.oai.oaiproxy.OAIProxy;
import dk.statsbiblioteket.findex.oai.validation.XMLTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: hl
 * Date: 2006-07-24
 * Time: 13:59:21
 * To change this template use File | Settings | File Templates.
 */
public class SBHarvest {
    private static final Logger logger = LoggerFactory.getLogger(SBHarvest.class);

    public String targetName = null;
    public String url = null;
    public String dir = null;    
    public String timedir = null;
    public String target = null;
    public String set = null;
    public String timedelay = null;
    public boolean verifyxml = true;


    public boolean isVerifyxml() {
        return verifyxml;
    }

    public void setVerifyxml(boolean verifyxml) {
        this.verifyxml = verifyxml;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String prefix) {
        this.target = prefix;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getTimedelay() {
        return timedelay;
    }

    public void setTimedelay(String timedelay) {
        this.timedelay = timedelay;
    }

    private String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }


    private String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }


    public String getTimedir() {
        return timedir;
    }

    public void setTimedir(String timedir) {
        this.timedir = timedir;
    }


    public static final long FIRST_HARVEST_DATE = 0;
    public void harvestTarget() throws Exception{
        boolean gotsomething = false;
        Calendar now = new GregorianCalendar();
        Calendar until =null;
        if (getUrl() != null && getDir() != null) {
            OAIProxy proxy = new OAIProxy();
            String timestring = calendarToString(now,true);
            String buffer = proxy.getOAIXml(getUrl(),"?verb=Identify");
            if (buffer!= null && !buffer.equals("")) {
                String earliestdatestamp = getField("earliestDatestamp",buffer);
                Calendar cal = null;
                if (earliestdatestamp!= null && !earliestdatestamp.equals("")) {
                    cal = stringToCalendar(earliestdatestamp);
                }
                Calendar from = getFrom(cal);
                if (this.getTimedelay() == null) {
                    this.setTimedelay("-24");
                }
                int delay = new Integer(this.getTimedelay()).intValue();
                 until = getUntil(now,delay);
                if (until.compareTo(from) < 0) {
                    //System.out.println("INFO: Arkivet er opdateret");
                    logger.info("Archive allready up to date");
                    return;
                }
                String prefix = this.getTarget();
                if (prefix == null || prefix.trim().equals("")) {
                    prefix = "oai_dc";
                }
                String set = this.getSet();
                String appendset = "";
                if (set != null && !set.trim().equals("")) {
                    appendset = "&set=" + set;
                }

                                
                String request = "?verb=ListRecords&metadataPrefix=" + prefix + appendset + "&from=" + calendarToString(from,false) + "&until=" + calendarToString(until,false);
                //String request = "?verb=ListRecords&metadataPrefix=mtp_dc&from=" + calendarToString(from,false) + "&until=" + calendarToString(until,false);
                //request = "?verb=ListRecords&metadataPrefix=oai_dc&from=" + calendarToString(from,false) + "&until=2007-10-04";
                logger.info("request params:"+request);
                boolean finished = false;
                long count = 0;
                int retcount = 0;
                boolean success = (new File(getDir() + timestring + "/")).mkdirs();
                if (success) {
                    BufferedWriter out = null;
                    try {
                        out = new BufferedWriter(new FileWriter(getDir() + OAIPropertiesLoader.targetinfofile));
                        out.write(buffer);
                        out.close();
                    } catch (IOException e) {
                        logger.error("Could not write file '" + getDir() + OAIPropertiesLoader.targetinfofile +"' to disk");
                        //System.out.println("FEJL: Kunne ikke gemme filen: " + getDir() + "identify.xml");
                    }
                    //while (!finished && count < 2) { //til test
                    int retry = 5;
                    while (!finished) {
                        try {
                            if (getDir().indexOf("pubmed") > 0) {
                                Thread.sleep(testTimeZone_pubmed());
                            } else {
                                Thread.sleep(testTimeZone_standard());
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        //System.out.println(new Long(count + 1).longValue() + "------------------------------------------");
                        buffer = proxy.getOAIXml(getUrl(),request);
                        String token = "";
                        if (buffer.indexOf("<ListRecords") > 0) {
                            if (retcount >= 50) {
                                retcount = 0;
                                //System.out.println(".");
                            }
                            //System.out.print(".");
                            retcount++;
                            if (buffer.indexOf("<OAI-PMH") < 0 && buffer.indexOf("</OAI-PMH") > 0) {
                                buffer = buffer.replaceFirst("\\<ListRecords","<OAI-PMH><ListRecords");
                            }
                            if (buffer.indexOf("<resumptionToken") > 0) {
                                token = getField("resumptionToken", buffer);
                                if (token != null && !token.equals("")) {
                                    request = "?verb=ListRecords&resumptionToken=" + tokenEncode(token);                                   
                                } else {
                                    finished = true;
                                }
                            } else {
                                finished = true;
                            }
                            try {
                                buffer = getLegalXml(buffer);
                                out = new BufferedWriter(new FileWriter(getDir() + timestring + "/" +OAIPropertiesLoader.downloadfile + "_" + new Long(count).toString() + ".xml"));
                                out.write(buffer);
                                out.close();
                                retry = 5;
                                gotsomething = true;
                                count++;
                                if (this.isVerifyxml()) {
                                    logger.debug("Verifying xml");
                                    XMLTest xmltest = new XMLTest();
                                    xmltest.setFilename(getDir() + timestring + "/" + OAIPropertiesLoader.downloadfile + "_" + new Long(count - 1).toString() + ".xml");
                                    xmltest.start();
                                } else {
                                    logger.debug("Not verifying xml");
                                }
                            } catch (IOException e) {
                                //kunne ikke skrive fil til disk
                                //System.out.println("-");
                                //System.out.println("Fejl: Kunne ikke skrive fil til disk: " + getDir() + timestring + "/download_" + new Long(count).toString() + ".txt");
                                logger.error("Could not write '" + getDir() + timestring + "/" + OAIPropertiesLoader.downloadfile + "_" + new Long(count).toString() + ".xml' to disk");
                                gotsomething = false;
                                finished = true;
                            }
                        } else if (buffer.indexOf("noRecordsMatch") > 0) {
                            //System.out.println("-");
                            //System.out.println("INFO: Ingen poster matcher forespørgsel");
                            logger.info("No matching records");
                            finished = true;
                            gotsomething=true;
                        } else {
                            //System.out.println("-");
                            //System.out.println("FEJL: Ikke en normal fil???????????");
                            //System.out.println(buffer);
                            logger.error("Unknown answer from target");
                            logger.error(buffer);
                            if (retry <= 0) {
                                finished = true;
                                gotsomething = false;
                            } else {
                                retry--;
                                try {
                                    Thread.sleep(60*1000*10);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                        }
                    }
                } else {
                    //kunne ikke oprette downloaddir
                    //System.out.println("-");
                    //System.out.println("Fejl: Kunne ikke oprette downloaddir: " + getDir());
                    logger.error("Could not create downloaddir '" + getDir() + "'");
                    gotsomething = false;
                }

            } else {
                //System.out.println("-");
                //System.out.println("FEJL: Ingen fornuftig forbindelse");
                logger.error("Nothing received from target");
                gotsomething = false;
            }
            if (gotsomething) {
                try {
                    saveLastHarvestTime(until);
                } catch (Exception e) {                
                    logger.error("Could not update lastharvesttime for target:"+target);
                }
            } else {
                deleteDir(new File(getDir() + timestring + "/"));
            }
        } else {
            //mangler OAIurl eller downloaddir
            //System.out.println("-");
            //System.out.println("Fejl: Mangler OAIurl eller downloaddir");
            logger.error("Missing targeturl and/or downloaddir");
            gotsomething = false;
        }
        //System.out.println("-");
    }

    private String getField(String fieldname, String buf) {
        try {
            String[] dummy = buf.split("\\<"+fieldname,2);
            dummy = dummy[1].split(">",2);
            dummy = dummy[1].split("\\<",2);
            return dummy[0];
        } catch(Exception e) {
            return "";
        }
    }

    public static String calendarToString(Calendar cal,boolean withTime) {
        Format formatter;
        String retstr = "";
        if (cal == null) {
            return retstr;
        }
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        retstr = formatter.format(cal.getTime());
        formatter = new SimpleDateFormat("HH-mm-ss");
        if (withTime) {
            retstr = retstr + "T" + formatter.format(cal.getTime()) + "Z";
        }
        return retstr;
    }

    private String tokenEncode(String in) {
        in = in.replaceAll("%","%25");
        in = in.replaceAll("/","%2F").replaceAll("\\?","%3F").replaceAll("#","%23").replaceAll("=","%3D").replaceAll("&","%26").replaceAll(":","%3A").replaceAll(";","%3B").replaceAll(" ","%20").replaceAll("\\+","%2B");
        return in;
    }

    private long testTimeZone_standard() {
        return 1000;
    }

    private long testTimeZone_pubmed() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
        if (hour24 < 5 || hour24 > 20) {
            return 3000;
        } else {
            return 60000;
        }
    }

    private Calendar stringToCalendar(String calstr) {
        Calendar cal = null;
        if (calstr.length() < 10) {
            return cal;
        }
        if (calstr.toLowerCase().indexOf("t") > 0) {
            String[] dummy = calstr.toLowerCase().split("t",2);
            String date[] = dummy[0].split("-",3);
            String time[] = dummy[1].substring(0,8).replaceAll("-",":").split(":",3);
            cal = new GregorianCalendar();
            cal.set(new Integer(date[0]).intValue(),new Integer(date[1]).intValue() - 1,new Integer(date[2]).intValue(),new Integer(time[0]).intValue(),new Integer(time[1]).intValue(),new Integer(time[2]).intValue());
        } else {
            calstr = calstr.substring(0,10);
            String date[] = calstr.split("-",3);
            cal = new GregorianCalendar();
            cal.set(new Integer(date[0]).intValue(),new Integer(date[1]).intValue() - 1,new Integer(date[2]).intValue());
        }
        return cal;
    }


    private Calendar getFrom(Calendar incal) throws Exception{
        Calendar cal = null;
        if (incal==null) {
            cal =  stringToCalendar("1970-01-01");
        } else {
            cal = (Calendar) incal.clone();
        }
        if (cal==null) {
            cal = stringToCalendar("1970-01-01");
        }


        cal = getLastHarvestTime();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        cal.add(Calendar.HOUR_OF_DAY, -48); //2 days ago, make sure we get everything. Overlapping not a problem

        return cal;
    }

    private Calendar getUntil(Calendar incal,int deltahour) {
        Calendar cal = (Calendar) incal.clone();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        cal.add(Calendar.HOUR_OF_DAY, deltahour);
        return cal;
    }

    private void saveLastHarvestTime(Calendar cal) throws IOException {
        boolean exists = (new File(getTimedir())).exists();
        if (!exists) {
            boolean result = new File(getTimedir()).mkdirs();
            if (!result) {
                throw new IOException();
            }
        }
        
        File file = new File(getTimedir() + OAIPropertiesLoader.lastharvesttime);
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(calendarToString(cal,true));
        out.close();
    }

    private Calendar getLastHarvestTime() throws Exception {
        
        
        BufferedReader in = new BufferedReader(new FileReader(getTimedir() + OAIPropertiesLoader.lastharvesttime));
        String str;
        str = in.readLine();
        in.close();
        return stringToCalendar(str.trim());
    }

    public static boolean deleteDir(File dir) {
        logger.debug("Removing directory '" + dir.getAbsolutePath() + "' and all files in it");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children.length > 0) {
                return false;
            }
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    protected static String getLegalXml(final String text) {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!isLegalXml(c)) {
                if (buffer == null) {
                    // Start up a buffer.  Copy characters here from now on
                    // now we've found at least one bad character in original.
                    buffer = new StringBuffer(text.length());
                    buffer.append(text.substring(0, i));
                }
            } else {
                if (buffer != null) {
                    buffer.append(c);
                }
            }
        }
        return (buffer != null)? buffer.toString(): text;
    }

    private static boolean isLegalXml(final char c) {
        return c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff)
                || (c >= 0xe000 && c <= 0xfffd) || (c >= 0x10000 && c <= 0x10ffff);
    }

}
