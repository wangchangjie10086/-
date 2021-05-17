
package com.ingcore.generator.build;

import com.ingcore.generator.xml.IgnoreDTDEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.HashMap;


public class BuilderClassManager {

    public BuilderClassManager() {
    }

    /**
     * @throws SAXException
     */
    public void render() throws SAXException {
        String classPath = BuilderClassManager.class.getResource("/").toString();
        String generatorConfigPath = classPath.substring(6) + "generatorConfig.xml";
        this.renderFile(generatorConfigPath);
    }

    public void delete(String filenames) throws IOException {

        if (filenames == null) return;

        BuilderJavaClassFile bf = new BuilderJavaClassFile();

        String[] fs = filenames.split(",");

        for (String f : fs) {
            f = f.trim();
            if (f.length() > 0) {
                bf.delete(FileType.BEAN, f);
                bf.delete(FileType.BEANSEARCH, f);
                bf.delete(FileType.MAPPER, f);
                bf.delete(FileType.XML, f);
                bf.delete(FileType.SERVICE, f);
                bf.delete(FileType.IMPL, f);
                bf.delete(FileType.JUNITTEST, f);
            }
        }
    }


    public void renderFile(String generatorConfigPath) throws SAXException {
        Document doc = readDocument(generatorConfigPath);
        NodeList tables = this.selectNodes("/generatorConfiguration/context/table", doc);
        BuilderJavaClassFile bf = new BuilderJavaClassFile();

        if (tables != null) {
            for (int i = 0; i < tables.getLength(); i++) {
                NamedNodeMap as = tables.item(i).getAttributes();
                String tableName = as.getNamedItem("tableName").getNodeValue();
                String modelName = as.getNamedItem("domainObjectName").getNodeValue();
                HashMap<String, String> cmap = BuilderTableMapping.queryFileds(tableName);
                String ckType = "String";
                if (cmap.containsKey(BuilderTableMapping.C_K)) {
                    ckType = cmap.get(BuilderTableMapping.C_K);
                    cmap.remove(BuilderTableMapping.C_K);
                }
                if (ckType != null) {
                    if (ckType.equals("int")) {
                        ckType = "Integer";
                    } else if (ckType.equals("bigint")) {
                        ckType = "Long";
                    } else {
                        ckType = "String";
                    }
                }
                bf.set_fileds(cmap);
                try {

                    bf.save(FileType.DAO, tableName, modelName, ckType);
                    bf.save(FileType.DAO_XML, tableName, modelName, ckType);

                    bf.save(FileType.BASE_SERVICE_IMPL, tableName, modelName, ckType);
                    bf.save(FileType.BASE_SERVICE, tableName, modelName, ckType);

                    bf.save(FileType.SERVICE, tableName, modelName, ckType);
                    bf.save(FileType.SERVICE_IMPL, tableName, modelName, ckType);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Document readDocument(String filePath) throws SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            System.err.println(pce);
            System.exit(1);
        }

        InputStream in = null;

        try {

            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

        Document doc = null;
        try {
            builder.setEntityResolver(
                    new IgnoreDTDEntityResolver()
            );
            doc = builder.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public Node selectSingleNode(String express, Object source) {

        Node result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            result = (Node) xpath
                    .evaluate(express, source, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public NodeList selectNodes(String express, Object source) {
        NodeList result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            result = (NodeList) xpath.evaluate(express, source,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveXml(String fileName, Document doc) {

        TransformerFactory transFactory = TransformerFactory.newInstance();

        try {
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            DOMSource source = new DOMSource();
            source.setNode(doc);
            StreamResult result = new StreamResult();
            result.setOutputStream(new FileOutputStream(fileName));
            transformer.transform(source, result);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
