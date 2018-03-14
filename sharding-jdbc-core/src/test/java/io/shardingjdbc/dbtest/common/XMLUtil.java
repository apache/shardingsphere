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

	public static Document parseFile(final File file) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document result = db.parse(file);

		return result;
	}

	public static Document parseStream(final InputStream in) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document result = db.parse(in);
		return result;
	}

	public static String getNodeValue(final Node node) {
		return node.getTextContent();
	}

	public static NodeList getNodeList(final Node node, final String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		NodeList result = (NodeList) oXpath.evaluate(xpath, node, XPathConstants.NODESET);
		return result;
	}

	public static Node getNode(final Node node, final String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		Node result = (Node) oXpath.evaluate(xpath, node, XPathConstants.NODE);

		return result;
	}
}
