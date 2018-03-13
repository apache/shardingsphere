package io.shardingjdbc.dbtest.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.shardingjdbc.dbtest.common.XMLUtil;

public class AnalyzeDataset {

	public static DatasetDefinition analyze(final String path)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		return analyze(new File(path));
	}

	public static DatasetDefinition analyze(final File file)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

		Document doc = XMLUtil.parseFile(file);
		Node rootNode = XMLUtil.getNode(doc, "/dataset");
		NodeList firstNodeList = rootNode.getChildNodes();
		DatasetDefinition result = new DatasetDefinition();
		for (int i = 0; i < firstNodeList.getLength(); i++) {
			Node firstNode = firstNodeList.item(i);
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {

				if ("table-config".equals(firstNode.getNodeName())) {
					analyzeTableConfig(result, firstNode);
				} else {
					Map<String, List<Map<String, String>>> datas = result.getDatas();
					String tableName = firstNode.getNodeName();
					List<Map<String, String>> datalists = datas.get(tableName);
					if (datalists == null) {
						datalists = new ArrayList<>();
						datas.put(tableName, datalists);
					}

					NamedNodeMap attrMap = firstNode.getAttributes();

					Map<String, String> datacols = new HashMap<>();
					datalists.add(datacols);
					for (int j = 0; j < attrMap.getLength(); j++) {
						Node nodeAttr = attrMap.item(j);
						Attr attr = (Attr) nodeAttr;
						String attrName = attr.getName();
						String attrValue = attr.getValue();
						datacols.put(attrName, attrValue);
					}
				}
			}
		}
		return result;
	}

	private static void analyzeTableConfig(final DatasetDefinition datasetDefinition, final Node firstNode) {
		NodeList secondNodeList = firstNode.getChildNodes();
		Map<String, Map<String, String>> configs = datasetDefinition.getConfigs();
		for (int j = 0; j < secondNodeList.getLength(); j++) {
            Node secondNode = secondNodeList.item(j);
            if (secondNode.getNodeType() == Node.ELEMENT_NODE) {
                Map<String, String> maps = new HashMap<>();
                configs.put(secondNode.getNodeName(), maps);
                NodeList thirdNodeList = secondNode.getChildNodes();
                for (int n = 0; n < thirdNodeList.getLength(); n++) {
                    Node thirdNode = thirdNodeList.item(n);
                    if (thirdNode.getNodeType() == Node.ELEMENT_NODE) {
                        maps.put(thirdNode.getNodeName(), thirdNode.getFirstChild().getNodeValue());
                    }
                }
            }
        }
	}

}
