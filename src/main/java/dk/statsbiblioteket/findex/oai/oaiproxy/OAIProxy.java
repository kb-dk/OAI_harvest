package dk.statsbiblioteket.findex.oai.oaiproxy;

import dk.statsbiblioteket.findex.oai.OAIPropertiesLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: hl
 * Date: 2006-07-24
 * Time: 13:21:56
 * To change this template use File | Settings | File Templates.
 */

public class OAIProxy {
    static Logger logger = LoggerFactory.getLogger(OAIProxy.class);
    

    int maxtries = OAIPropertiesLoader.maxtries;

    String buffer = "";

    public String getOAIXml(String urlstring, String request) {
        buffer = "";
        URL url = null;
        boolean communicating = false;
        try {
            url = new URL(urlstring + request);
            communicating = true;
            logger.debug("connecting to '" + urlstring + "' with request:'" + request + "'");
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            communicating = false;
            logger.debug("connecting to '" + urlstring + "' with request:'" + request + "' failed");
        }
        int tries = 0;
        //System.out.println(request);
        while (communicating && tries < maxtries) {
            try {
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent",OAIPropertiesLoader.user_agent);
                conn.setRequestProperty("From",OAIPropertiesLoader.user_email);
                Map header = conn.getHeaderFields();
                Object rescode = null;
                try {
                    rescode = header.get(null);
                } catch (Exception e) {
                    //System.out.println("FEJL:");
                    logger.error("No responsecode from target");
                }
                if (rescode == null || rescode.toString().equals("")) {
                    communicating = false;
                    buffer = "";
                    //System.out.println("FEJL: Ingen response code: " + rescode);
                    logger.error("Target returned no responsecode - no action defined");
                } else if (rescode.toString().indexOf(" 200 ") > 0) {
                    //System.out.println("INFO: Forbundet til: " + urlstring);
                    logger.debug("Connected to '" + urlstring + "'");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        //System.out.println(line);
                        if (line.trim().startsWith("<") && line.trim().endsWith(">")) {
                            buffer = buffer + line.trim();
                        } else {
                            buffer = buffer + line;
                        }
                    }
                    in.close();
                    communicating = false;
                    tries = 0;
                } else if (rescode.toString().indexOf(" 503 ") > 0) {
                    Object retry = header.get("Retry-After");
                    if (retry != null && !retry.toString().equals("")) {
                        if (retry.toString().indexOf(":") > 0) {
                            //System.out.println("................Retrystring: " + retry.toString());
                            logger.debug(".....................Retrystring: " + retry.toString());
                            tries = maxtries + 2;
                            communicating = false;
                            buffer = "";
                            return "";
                        }
                        retry = retry.toString().replaceAll("\\D","");
                        //long numMillisecondsToSleep = new Long(retry.toString()).longValue() * 60000;
                        //System.out.println("INFO: Foreslår at forsøge igen om " + retry + " minutter. Forsøg nr.: " + (tries + 1) + " af ialt " + (maxtries + 1) + " forsøg");
                        long numMillisecondsToSleep = new Long(retry.toString()).longValue() * 1000;
                        if (numMillisecondsToSleep < 10000) {
                            numMillisecondsToSleep = numMillisecondsToSleep * 60;
                        }
                        //System.out.println("");
                        //System.out.println("INFO: Foreslår at forsøge igen om " + retry + " sekunder. Venter " + new Long(numMillisecondsToSleep / 60).toString() +" sekunder. Forsøg nr.: " + (tries + 1) + " af ialt " + (maxtries + 1) + " forsøg");
                        logger.info("Waiting " + new Long(numMillisecondsToSleep /1000).toString() +" secs. Try no. " + (tries + 1) + " of total " + (maxtries + 1));
                        Thread.sleep(numMillisecondsToSleep);
                    } else {
                        long numMillisecondsToSleep = 60000 * 10; //vent 10 min før nyt forsøg
                        //System.out.println("");
                        //System.out.println("FEJL: Foreslår at forsøge igen, men uden tidsangivelse. Forsøg nr.: " + (tries + 1) + " af ialt " + (maxtries + 1) + " forsøg");
                        logger.info("Waiting " + new Long((numMillisecondsToSleep / 60)).toString() +" secs. Try no. " + (tries + 1) + " of total " + (maxtries + 1));
                        Thread.sleep(numMillisecondsToSleep);
                    }
                    tries++;
                } else if (rescode.toString().indexOf(" 302 ") > 0) {
                    Object redirect = header.get("Location");
                    if (redirect != null && !redirect.toString().equals("")) {
                        String redir = redirect.toString().replaceAll("\\[","").replaceAll("\\]","");
                        if (redir.indexOf(request) >= 0) {
                            url = new URL(redir);
                        } else {
                            url = new URL(redir + request);
                        }
                        //System.out.println("INFO: Anviser redirect");
                        logger.info("Redirect from '" + urlstring + "' to '" + redir + "'");
                        long numMillisecondsToSleep = 5000;
                        Thread.sleep(numMillisecondsToSleep);
                    } else {
                        communicating = false;
                        buffer = "";
                        //System.out.println("FEJL: Anviser redirect uden target");
                        logger.error("Redirect from '" + urlstring + "' with no new target");
                    }
                    tries++;
                } else if (rescode.toString().indexOf(" 404 ") > 0) {
                    communicating = false;
                    buffer = "";
                    //System.out.println("FEJL: Not found");
                    logger.error("Target returned responsecode '404 : Not found'");
                } else {
                    communicating = false;
                    buffer = "";
                    //System.out.println("FEJL: Uventet response code: " + rescode);
                    logger.error("Target returned responsecode '" + rescode + "' - no action defined");
                }
            } catch (IOException e) {
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                logger.error("error",e);
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    logger.error("     at " + e.getStackTrace()[i].toString());
                }
                buffer = "";
            } catch (InterruptedException e) {
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                logger.error("error",e);
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    logger.error("     at " + e.getStackTrace()[i].toString());
                }
                buffer = "";
            }
        }
        if (buffer.equals("")) {
            //System.out.println(request);
            logger.error("Request '" + request + "' produced no acceptable response");
            return "";
        } else {
            return buffer.split("\\<\\/OAI-PMH>")[0] + "</OAI-PMH>";
        }
    }
}

