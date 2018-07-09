/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.dbtest.cases.dataset.expected.dml;

import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetRow;
import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * DML data set.
 *
 * @author zhangliang
 */
@Getter
@XmlRootElement(name = "datasets")
public final class DMLDataSet {
    
    @XmlElement(name = "metadata")
    private List<DataSetMetadata> metadataList;
    
    @XmlElement(name = "dataset")
    private List<DataSetRow> dataSetRows = new LinkedList<>();
    
    /**
     * Find data set meta data via data node.
     * 
     * @param dataNode data node
     * @return data set meta data belong to current data node
     */
    public DataSetMetadata findDataSetMetadata(final DataNode dataNode) {
        for (DataSetMetadata each : metadataList) {
            if (contains(new InlineExpressionParser(each.getDataNodes()).splitAndEvaluate(), dataNode)) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find data node: %s", dataNode.toString()));
    }
    
    private boolean contains(final List<String> dataNodes, final DataNode dataNode) {
        for (String each : dataNodes) {
            if (new DataNode(each).equals(dataNode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find data set rows via data node.
     * 
     * @param dataNode data node
     * @return data set rows belong to current data node
     */
    public List<DataSetRow> findDataSetRows(final DataNode dataNode) {
        List<DataSetRow> result = new ArrayList<>(dataSetRows.size());
        for (DataSetRow each : dataSetRows) {
            if (new DataNode(each.getDataNode()).equals(dataNode)) {
                result.add(each);
            }
        }
        return result;
    }
}
