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

package org.apache.shardingsphere.test.integration.cases.dataset;

import lombok.Getter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;

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
@Getter
@XmlRootElement(name = "dataset")
public final class DataSet {
    
    @XmlAttribute(name = "update-count")
    private int updateCount;
    
    @XmlElement(name = "metadata")
    private final List<DataSetMetadata> metadataList = new LinkedList<>();
    
    @XmlElement(name = "row")
    private final List<DataSetRow> rows = new LinkedList<>();
    
    /**
     * Find data set meta data via table name.
     *
     * @param tableName table name
     * @return data set meta data belong to current table
     */
    public DataSetMetadata findMetadata(final String tableName) {
        Optional<DataSetMetadata> result = metadataList.stream().filter(each -> tableName.equals(each.getTableName())).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        throw new IllegalArgumentException(String.format("Cannot find expected metadata via table name: '%s'", tableName));
    }
    
    /**
     * Find data set meta data via data node.
     * 
     * @param dataNode data node
     * @return data set meta data belong to current data node
     */
    public DataSetMetadata findMetadata(final DataNode dataNode) {
        Optional<DataSetMetadata> result = metadataList.stream().filter(each -> contains(new InlineExpressionParser(each.getDataNodes()).splitAndEvaluate(), dataNode)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        throw new IllegalArgumentException(String.format("Cannot find data node: %s", dataNode));
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
