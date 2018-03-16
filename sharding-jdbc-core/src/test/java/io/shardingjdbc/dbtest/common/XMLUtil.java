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

package io.shardingjdbc.dbtest.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtil {

    /**
     * Parse the file to Document.
     * @param file file
     * @return Document
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    public static Document parseFile(final File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.parse(file);

        return result;
    }

    /**
     * Parse the InputStream to Document.
     * @param in InputStream
     * @return Document
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    public static Document parseStream(final InputStream in) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.parse(in);
        return result;
    }

    /**
     * get node value.
     * @param node node
     * @return value
     */
    public static String getNodeValue(final Node node) {
        return node.getTextContent();
    }

    /**
     * Get the list of nodes.
     * @param node node
     * @param xpath xpath
     * @return list node
     * @throws XPathExpressionException XPathExpressionException
     */
    public static NodeList getNodeList(final Node node, final String xpath) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath oXpath = factory.newXPath();
        NodeList result = (NodeList) oXpath.evaluate(xpath, node, XPathConstants.NODESET);
        return result;
    }

    /**
     * Acquisition node.
     * @param node node
     * @param xpath xpath
     * @return node
     * @throws XPathExpressionException XPathExpressionException
     */
    public static Node getNode(final Node node, final String xpath) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath oXpath = factory.newXPath();
        Node result = (Node) oXpath.evaluate(xpath, node, XPathConstants.NODE);

        return result;
    }
}
