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

	public static Document parseFile(File file) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document result = db.parse(file);

		return result;
	}

	public static Document parseStream(InputStream in) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document result = db.parse(in);
		return result;
	}

	public static String getNodeValue(Node node) {
		return node.getTextContent();
	}

	public static NodeList getNodeList(org.w3c.dom.Node node, String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		NodeList result = (NodeList) oXpath.evaluate(xpath, node, XPathConstants.NODESET);
		return result;
	}

	public static Node getNode(org.w3c.dom.Node node, String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		Node result = (Node) oXpath.evaluate(xpath, node, XPathConstants.NODE);

		return result;
	}
}
