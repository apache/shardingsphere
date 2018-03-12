package io.shardingjdbc.dbtest.data;

import io.shardingjdbc.dbtest.common.XMLUtil;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析表文件
 */
public class AnalyzeDataset {

    /**
     *
     * @param path
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public static DatasetDefinition analyze(String path) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return analyze(new File(path));
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public static DatasetDefinition analyze(File file) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        Document doc = XMLUtil.parseFile(file);
        Node rootNode = XMLUtil.getNode(doc,"/dataset");
        NodeList firstNodeList = rootNode.getChildNodes();
        DatasetDefinition datasetDefinition = new DatasetDefinition();
        for (int i = 0; i < firstNodeList.getLength(); i++) {
            Node firstNode = firstNodeList.item(i);
            if(firstNode.getNodeType()==Node.ELEMENT_NODE){

                //解析配置信息
                if("table-config".equals(firstNode.getNodeName())) {
                    NodeList secondNodeList = firstNode.getChildNodes();
                    Map<String,Map<String,String>> configs = datasetDefinition.getConfigs();
                    for (int j = 0; j < secondNodeList.getLength(); j++) {
                        Node secondNode = secondNodeList.item(j);
                        if(secondNode.getNodeType()==Node.ELEMENT_NODE){
                            Map<String,String> maps = new HashMap<>();
                            configs.put(secondNode.getNodeName(),maps);
                            NodeList thirdNodeList = secondNode.getChildNodes();
                            for (int n = 0; n < thirdNodeList.getLength(); n++) {
                                Node thirdNode = thirdNodeList.item(n);
                                if(thirdNode.getNodeType()==Node.ELEMENT_NODE){
                                    maps.put(thirdNode.getNodeName(),thirdNode.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    }
                }else{
                    // 解析数据
                    Map<String,List<Map<String,String>>> datas = datasetDefinition.getDatas();
                    String tableName = firstNode.getNodeName();
                    List<Map<String,String>> datalists = datas.get(tableName);
                    if(datalists == null){
                        datalists = new ArrayList<>();
                        datas.put(tableName,datalists);
                    }

                    NamedNodeMap attrMap = firstNode.getAttributes();

                    Map<String,String> datacols = new HashMap<>();
                    datalists.add(datacols);
                    for(int j = 0; j < attrMap.getLength(); j++){
                        Node node_attr = attrMap.item(j);//3
                        Attr attr = (Attr)node_attr;//4
                        String attr_name = attr.getName();
                        String attr_value = attr.getValue();
                        datacols.put(attr_name,attr_value);
                    }
                }
            }
        }
        return datasetDefinition;
    }

}
