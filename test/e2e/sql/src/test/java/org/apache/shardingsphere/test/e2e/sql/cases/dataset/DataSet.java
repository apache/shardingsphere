/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.sql.cases.dataset;

import lombok.Getter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data sets root xml entry.
 */
@XmlRootElement(name = "dataset")
@Getter
public final class DataSet {
    
    @XmlAttribute(name = "update-count")
    private int updateCount;
    
    @XmlElement(name = "metadata")
    private final List<DataSetMetaData> metaDataList = new LinkedList<>();
    
    @XmlElement(name = "row")
    private final List<DataSetRow> rows = new LinkedList<>();
    
    /**
     * Find data set meta data via table name.
     *
     * @param tableName table name
     * @return data set meta data belong to current table
     */
    public DataSetMetaData findMetaData(final String tableName) {
        Optional<DataSetMetaData> result = metaDataList.stream().filter(each -> tableName.equals(each.getTableName())).findFirst();
        return result.orElseThrow(() -> new IllegalArgumentException(String.format("Can not find expected meta data via table `%s`", tableName)));
    }
    
    /**
     * Find data set meta data via data node.
     *
     * @param dataNode data node
     * @return data set meta data belong to current data node
     */
    public DataSetMetaData findMetaData(final DataNode dataNode) {
        Optional<DataSetMetaData> result = metaDataList.stream().filter(each -> contains(InlineExpressionParserFactory.newInstance(each.getDataNodes()).splitAndEvaluate(), dataNode)).findFirst();
        return result.orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find data node: %s", dataNode)));
    }
    
    private boolean contains(final List<String> dataNodes, final DataNode dataNode) {
        return dataNodes.stream().anyMatch(each -> new DataNode(each).equals(dataNode));
    }
    
    /**
     * Find data set rows via data node.
     *
     * @param dataNode data node
     * @return data set rows belong to current data node
     */
    public List<DataSetRow> findRows(final DataNode dataNode) {
        return rows.stream().filter(each -> new DataNode(each.getDataNode()).equals(dataNode)).collect(Collectors.toList());
    }
}
