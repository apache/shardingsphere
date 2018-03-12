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

/**
 * <br>
 * xpath辅助类
 * @version 1.0
 */
public class XMLUtil {
	/**
	 * 构造xml文档对应的document对象
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Document parseFile(File file) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = db.parse(file);

		// 创建XPath对象
		XPathFactory factory = XPathFactory.newInstance();
		return doc;
	}
	/**
	 * 构造输入流对应的document对象
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static Document parseStream(InputStream in) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = db.parse(in);


		return doc;
	}
	/**
	 * 获取结点值
	 * @param node
	 * @return
	 */
	public static String getNodeValue(Node node)
	{
		String dataValue = node.getTextContent();
		return dataValue;
	}
	/**
	 * 获取结点List
	 * @param node
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public static NodeList getNodeList(org.w3c.dom.Node node, String xpath) throws XPathExpressionException
	{
		// 创建XPath对象
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		NodeList nodeList = (NodeList) oXpath.evaluate(xpath, node, XPathConstants.NODESET);

		return nodeList;
	}
	/**
	 * 获取单个结点
	 * @param node
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public static Node getNode(org.w3c.dom.Node node, String xpath) throws XPathExpressionException
	{
		// 创建XPath对象
		XPathFactory factory = XPathFactory.newInstance();
		XPath oXpath = factory.newXPath();
		Node nodeRet = (Node) oXpath.evaluate(xpath, node, XPathConstants.NODE);

		return nodeRet;
	}
}
