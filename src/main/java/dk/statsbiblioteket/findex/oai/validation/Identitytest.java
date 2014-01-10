package dk.statsbiblioteket.findex.oai.validation;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * No idea what this class does.
 */
public class Identitytest {

    private Vector<byte[]> vec = new Vector<byte[]>();

    public static void main(String[] args) {
        Identitytest itt = new Identitytest();
        try {
            itt.visitAllDirs(new File("/home/teg/workspace/oai/"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void visitAllDirs(File dir) throws IOException, ParserConfigurationException, SAXException {
        if (dir.isDirectory() && !(dir.getName().endsWith("Z") && dir.getName().indexOf("T") == 10) ) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                visitAllDirs(new File(dir, children[i]));
            }
        } else {
            visitAllFiles(dir);

        }
    }
    public void visitAllFiles(File dir) throws ParserConfigurationException, IOException, SAXException {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                visitAllFiles(new File(dir, children[i]));
            }
        } else {
            if (dir.getName().endsWith(".xml")) {
                //System.out.println(dir.getAbsolutePath());
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = null;
                docBuilder = docBuilderFactory.newDocumentBuilder();
                docBuilder.isValidating();
                docBuilder.isNamespaceAware();
                Document doc = docBuilder.parse (dir);
                //NodeList nodelist = getNodesfromDocument(doc,"/oai:OAI-PMH/oai:ListRecords/oai:record");
                NodeList nodelist = getNodesfromDocument(doc,"/OAI-PMH/ListRecords/record/metadata/dc/identifier");
                for (int i=0; i < nodelist.getLength(); i++) {
                    //System.out.println(nodelist.item(i).getFirstChild().getNodeValue());
                    byte[] test = getKeyedDigest("".getBytes(),"".getBytes());
                    if (!vec.contains(test)) {
                        vec.add(test);
                    } else {
                        //System.out.println("DUBLET........................");
                    }
                }
            }
        }
    }

    public static byte[] getKeyedDigest(byte[] buffer, byte[] key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer);
            return md5.digest(key);
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    private  NodeList getNodesfromDocument(Node doc,String xpath) {
        XPathFactory  factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MyNamespaceContext());
        XPathExpression  xPathExpression = null;
        try {
            xPathExpression = xPath.compile(xpath);
            return (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return null;
        }
    }

    private class MyNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix)
        {
            if (prefix.equals("oai")) {
                return "http://www.openarchives.org/OAI/2.0/";
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        public String getPrefix(String namespace)
        {
            if (namespace.equals("http://www.openarchives.org/OAI/2.0/")) {
                return "oai";
            } else {
                return null;
            }
        }

        public Iterator getPrefixes(String namespace)
        {
            return null;
        }
    }

}
