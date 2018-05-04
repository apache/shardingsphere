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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.shardingjdbc.dbtest.config.bean.ColumnDefinition;
import io.shardingjdbc.dbtest.config.bean.DatasetDefinition;
import io.shardingjdbc.dbtest.config.bean.IndexDefinition;
import io.shardingjdbc.dbtest.exception.DbTestException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AnalyzeDataset {
    
    /**
     * Parsing the Dataset file.
     *
     * @param path path
     * @param tableName tableName
     * @return DatasetDefinition
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws XPathExpressionException     XPathExpressionException
     */
    public static DatasetDefinition analyze(final String path, final String tableName)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return analyze(new File(path), tableName);
    }
    
    /**
     * Parsing the Dataset file.
     *
     * @param file file
     * @param tableName tableName
     * @return DatasetDefinition
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws XPathExpressionException     XPathExpressionException
     */
    public static DatasetDefinition analyze(final File file, final String tableName)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        XPathFactory factory = XPathFactory.newInstance();
        XPath oXpath = factory.newXPath();
        Node rootNode = (Node) oXpath.evaluate("/init", doc, XPathConstants.NODE);
        if (rootNode == null) {
            throw new DbTestException("file :" + file.getPath() + "analyze error,Missing init tag");
        }
        
        NodeList firstNodeList = rootNode.getChildNodes();
        DatasetDefinition result = new DatasetDefinition();
        for (int i = 0; i < firstNodeList.getLength(); i++) {
            Node firstNode = firstNodeList.item(i);
            if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                
                if ("metadata".equals(firstNode.getNodeName())) {
                    analyzeTableConfig(result, firstNode);
                } else if ("dataset".equals(firstNode.getNodeName())) {
                    analyzeDataset(result, tableName, firstNode);
                }
            }
        }
        return result;
    }
    
    private static void analyzeTableConfig(final DatasetDefinition datasetDefinition, final Node firstNode) {
        NodeList secondNodeList = firstNode.getChildNodes();
        Map<String, List<ColumnDefinition>> metadatas = datasetDefinition.getMetadatas();
        for (int j = 0; j < secondNodeList.getLength(); j++) {
            Node secondNode = secondNodeList.item(j);
            if (secondNode.getNodeType() == Node.ELEMENT_NODE) {
                List<ColumnDefinition> tableDefinitions = new ArrayList<>();
                metadatas.put(getAttr("name", secondNode), tableDefinitions);
                
                NodeList columnNodeList = secondNode.getChildNodes();
                for (int n = 0; n < columnNodeList.getLength(); n++) {
                    Node attNode = columnNodeList.item(n);
                    if (attNode.getNodeType() == Node.ELEMENT_NODE) {
                        ColumnDefinition cd = new ColumnDefinition();
                        tableDefinitions.add(cd);
                        String name = getAttr("name", attNode);
                        if (StringUtils.isNotEmpty(name)) {
                            cd.setName(name);
                        }
                        
                        String type = getAttr("type", attNode);
                        if (StringUtils.isNotEmpty(type)) {
                            cd.setType(type);
                        }
                        
                        String size = getAttr("size", attNode);
                        if (StringUtils.isNotEmpty(size)) {
                            cd.setSize(Integer.valueOf(type));
                        }
                        
                        String decimalDigits = getAttr("decimal-digits", attNode);
                        if (StringUtils.isNotEmpty(decimalDigits)) {
                            cd.setSize(Integer.valueOf(decimalDigits));
                        }
                        
                        String nullAble = getAttr("null-able", attNode);
                        if (StringUtils.isNotEmpty(nullAble)) {
                            cd.setSize(Integer.valueOf(nullAble));
                        }
                        
                        String numPrecRadix = getAttr("num-prec-radix", attNode);
                        if (StringUtils.isNotEmpty(numPrecRadix)) {
                            cd.setSize(Integer.valueOf(numPrecRadix));
                        }
                        
                        NodeList indexNodeList = attNode.getChildNodes();
                        if (indexNodeList != null && indexNodeList.getLength() != 0) {
                            getIndexs(indexNodeList, cd);
                        }
                    }
                    
                }
            }
        }
        
    }
    
    private static void getIndexs(final NodeList indexNodeList, final ColumnDefinition cd) {
        List<IndexDefinition> indexs = new ArrayList<>();
        cd.setIndexs(indexs);
        for (int w = 0; w < indexNodeList.getLength(); w++) {
            Node indexNode = indexNodeList.item(w);
            if (indexNode.getNodeType() == Node.ELEMENT_NODE) {
                IndexDefinition index = new IndexDefinition();
                String nameIndex = getAttr("name", indexNode);
                if (StringUtils.isNotEmpty(nameIndex)) {
                    index.setName(nameIndex);
                }
                
                String typeIndex = getAttr("type", indexNode);
                if (StringUtils.isNotEmpty(typeIndex)) {
                    index.setType(typeIndex);
                }
                
                String uniqueIndex = getAttr("unique", indexNode);
                if (StringUtils.isNotEmpty(uniqueIndex)) {
                    index.setUnique(Boolean.valueOf(uniqueIndex));
                }
                
                indexs.add(index);
            }
        }
    }
    
    private static String getAttr(final String nodeName, final Node node) {
        NamedNodeMap attNodeList = node.getAttributes();
        for (int n = 0; n < attNodeList.getLength(); n++) {
            Node attNode = attNodeList.item(n);
            if (nodeName.equals(attNode.getNodeName())) {
                return attNode.getFirstChild().getNodeValue();
            }
        }
        return "";
    }
    
    private static void analyzeDataset(final DatasetDefinition result, final String tableName1, final Node firstNode) {
        NodeList secondNodeList = firstNode.getChildNodes();
        for (int n = 0; n < secondNodeList.getLength(); n++) {
            Node secondNode = secondNodeList.item(n);
            if (secondNode.getNodeType() == Node.ELEMENT_NODE) {
                Map<String, List<Map<String, String>>> datas = result.getDatas();
                String tableNameTmp = tableName1;
                if (StringUtils.isBlank(tableNameTmp)) {
                    tableNameTmp = secondNode.getNodeName();
                }
                List<Map<String, String>> datalists = datas.get(tableNameTmp);
                if (datalists == null) {
                    datalists = new ArrayList<>();
                    datas.put(tableNameTmp, datalists);
                }
                
                NamedNodeMap attrMap = secondNode.getAttributes();
                
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
    
}
