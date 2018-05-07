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

import com.google.common.base.Preconditions;
import io.shardingjdbc.dbtest.config.bean.ColumnDefinition;
import io.shardingjdbc.dbtest.config.bean.DatasetDefinition;
import io.shardingjdbc.dbtest.config.bean.IndexDefinition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnalyzeDataset {
    
    /**
     * Parsing the Dataset file.
     *
     * @param file file
     * @param tableName tableName
     * @return DatasetDefinition
     * @throws IOException IO exception
     * @throws SAXException SAX exception
     * @throws ParserConfigurationException Parser configuration exception
     * @throws XPathExpressionException XPath expression exception
     */
    public static DatasetDefinition analyze(final File file, final String tableName) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        XPathFactory factory = XPathFactory.newInstance();
        XPath oXpath = factory.newXPath();
        Node rootNode = (Node) oXpath.evaluate("/datasets", doc, XPathConstants.NODE);
        Preconditions.checkNotNull(rootNode, "file :" + file.getPath() + "analyze error,Missing init tag");
        NodeList firstNodeList = rootNode.getChildNodes();
        DatasetDefinition result = new DatasetDefinition();
        for (int i = 0; i < firstNodeList.getLength(); i++) {
            Node firstNode = firstNodeList.item(i);
            if (Node.ELEMENT_NODE == firstNode.getNodeType()) {
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
        for (int i = 0; i < secondNodeList.getLength(); i++) {
            Node secondNode = secondNodeList.item(i);
            if (secondNode.getNodeType() == Node.ELEMENT_NODE) {
                List<ColumnDefinition> tableDefinitions = new ArrayList<>();
                metadatas.put(getAttribute("name", secondNode), tableDefinitions);
                NodeList columnNodeList = secondNode.getChildNodes();
                for (int n = 0; n < columnNodeList.getLength(); n++) {
                    Node attNode = columnNodeList.item(n);
                    if (attNode.getNodeType() == Node.ELEMENT_NODE) {
                        ColumnDefinition cd = new ColumnDefinition();
                        tableDefinitions.add(cd);
                        String name = getAttribute("name", attNode);
                        if (StringUtils.isNotEmpty(name)) {
                            cd.setName(name);
                        }
                        String type = getAttribute("type", attNode);
                        if (StringUtils.isNotEmpty(type)) {
                            cd.setType(type);
                        }
                        String size = getAttribute("size", attNode);
                        if (StringUtils.isNotEmpty(size)) {
                            cd.setSize(Integer.valueOf(type));
                        }
                        String decimalDigits = getAttribute("decimal-digits", attNode);
                        if (StringUtils.isNotEmpty(decimalDigits)) {
                            cd.setSize(Integer.valueOf(decimalDigits));
                        }
                        String nullAble = getAttribute("null-able", attNode);
                        if (StringUtils.isNotEmpty(nullAble)) {
                            cd.setSize(Integer.valueOf(nullAble));
                        }
                        String numPrecRadix = getAttribute("num-prec-radix", attNode);
                        if (StringUtils.isNotEmpty(numPrecRadix)) {
                            cd.setSize(Integer.valueOf(numPrecRadix));
                        }
                        NodeList indexNodeList = attNode.getChildNodes();
                        if (indexNodeList != null && indexNodeList.getLength() != 0) {
                            getIndexes(indexNodeList, cd);
                        }
                    }
                }
            }
        }
    }
    
    private static void getIndexes(final NodeList indexNodeList, final ColumnDefinition columnDefinition) {
        List<IndexDefinition> indexes = new ArrayList<>();
        columnDefinition.setIndexs(indexes);
        for (int i = 0; i < indexNodeList.getLength(); i++) {
            Node indexNode = indexNodeList.item(i);
            if (indexNode.getNodeType() == Node.ELEMENT_NODE) {
                IndexDefinition index = new IndexDefinition();
                String nameIndex = getAttribute("name", indexNode);
                if (StringUtils.isNotEmpty(nameIndex)) {
                    index.setName(nameIndex);
                }
                String typeIndex = getAttribute("type", indexNode);
                if (StringUtils.isNotEmpty(typeIndex)) {
                    index.setType(typeIndex);
                }
                String uniqueIndex = getAttribute("unique", indexNode);
                if (StringUtils.isNotEmpty(uniqueIndex)) {
                    index.setUnique(Boolean.valueOf(uniqueIndex));
                }
                indexes.add(index);
            }
        }
    }
    
    private static String getAttribute(final String nodeName, final Node node) {
        NamedNodeMap attNodeList = node.getAttributes();
        for (int i = 0; i < attNodeList.getLength(); i++) {
            Node attNode = attNodeList.item(i);
            if (nodeName.equals(attNode.getNodeName())) {
                return attNode.getFirstChild().getNodeValue();
            }
        }
        return "";
    }
    
    private static void analyzeDataset(final DatasetDefinition datasetDefinition, final String tableName, final Node firstNode) {
        NodeList secondNodeList = firstNode.getChildNodes();
        for (int i = 0; i < secondNodeList.getLength(); i++) {
            Node secondNode = secondNodeList.item(i);
            if (Node.ELEMENT_NODE == secondNode.getNodeType()) {
                Map<String, List<Map<String, String>>> datas = datasetDefinition.getDatas();
                String tableNameTmp = tableName;
                if (StringUtils.isBlank(tableNameTmp)) {
                    tableNameTmp = secondNode.getNodeName();
                }
                List<Map<String, String>> dataList = datas.get(tableNameTmp);
                if (dataList == null) {
                    dataList = new ArrayList<>();
                    datas.put(tableNameTmp, dataList);
                }
                NamedNodeMap attributesMap = secondNode.getAttributes();
                Map<String, String> dataColumnsMap = new HashMap<>();
                dataList.add(dataColumnsMap);
                for (int j = 0; j < attributesMap.getLength(); j++) {
                    Attr attr = (Attr) attributesMap.item(j);
                    dataColumnsMap.put(attr.getName(), attr.getValue());
                }
            }
        }
    }
}
