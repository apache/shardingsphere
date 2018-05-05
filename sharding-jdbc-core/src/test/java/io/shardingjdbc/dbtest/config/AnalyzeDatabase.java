/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.dbtest.config;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeDatabase {
    
    /**
     * Parsing the Dataset file.
     *
     * @param path path
     * @return database list
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws XPathExpressionException     XPathExpressionException
     */
    public static List<String> analyze(final String path) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return analyze(new File(path));
    }
    
    /**
     * Parsing the Dataset file.
     *
     * @param file file
     * @return database list
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws XPathExpressionException     XPathExpressionException
     */
    public static List<String> analyze(final File file) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        Document doc = parseFile(file);
        Node rootNode = getNode(doc, "/databases");
        NodeList firstNodeList = rootNode.getChildNodes();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < firstNodeList.getLength(); i++) {
            Node firstNode = firstNodeList.item(i);
            if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                
                if ("database".equals(firstNode.getNodeName())) {
                    result.add(firstNode.getFirstChild().getTextContent());
                }
            }
        }
        
        return result;
    }
    
    
    /**
     * Parse the file to Document.
     *
     * @param file file
     * @return Document
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     */
    private static Document parseFile(final File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.parse(file);
        
        return result;
    }
    
    /**
     * Acquisition node.
     *
     * @param node  node
     * @param xpath xpath
     * @return node
     * @throws XPathExpressionException XPathExpressionException
     */
    private static Node getNode(final Node node, final String xpath) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath oXpath = factory.newXPath();
        return (Node) oXpath.evaluate(xpath, node, XPathConstants.NODE);
    }
    
  
}
