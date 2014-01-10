package dk.statsbiblioteket.findex.oai.validation;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
* Still not sure when this class manually is used and why.
 */
public class Txt2Xml {
    public static void main(String[] args) throws IOException {
        Txt2Xml tx = new Txt2Xml();
        File dir = new File("/home/teg/workspace/oai/fetch_folderharvest_2014-01-06T14-25-18Z");
        String[] children = dir.list();
        System.out.println("files:"+children.length);
        String buffer = "";
        if (children == null) {
            //System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (int i=0; i<children.length; i++) {
                // Get filename of file or directory
                //System.out.println(children[i]);
                if (children[i].endsWith(".txt") && children[i].startsWith("download")) {
                    //tx.convertFile(children[i],"download_"  + i + ".xml");
                    //System.out.println(children[i]);

                    try {
                        BufferedReader in = new BufferedReader(new FileReader(dir +"/" + children[i]));
                        BufferedWriter out = new BufferedWriter(new FileWriter(dir +"/" + children[i].replace(".txt",".xml")));
                        String str;
                        while ((str = in.readLine()) != null) {
                            str = getLegalXml(str);
                            out.write(str);
                        }
                        in.close();
                        out.close();
                        try {
                            validate(dir +"/" + children[i].replace(".txt",".xml"));
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }
            }
        }
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

    public static boolean validate(String file) throws ParserConfigurationException, IOException{
        boolean validating = false;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.isValidating();
        docBuilder.isNamespaceAware();
        try {
            Document doc = docBuilder.parse (new File(file));
            validating = true;
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //System.out.println("INFO: XML-Validering:" + file + " - " + validating);
        return validating;
    }
}
