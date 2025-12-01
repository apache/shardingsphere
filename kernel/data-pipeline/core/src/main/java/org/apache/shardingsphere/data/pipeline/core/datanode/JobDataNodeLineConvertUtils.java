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

package org.apache.shardingsphere.data.pipeline.core.datanode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Job data node line convert utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobDataNodeLineConvertUtils {
    
    /**
     * Convert data nodes to lines.
     *
     * @param tableAndDataNodesMap table and data nodes map
     * @return job data node line list
     */
    public static List<JobDataNodeLine> convertDataNodesToLines(final Map<String, List<DataNode>> tableAndDataNodesMap) {
        List<Pair<String, JobDataNodeLine>> result = new LinkedList<>();
        for (Entry<String, Map<String, List<DataNode>>> entry : groupDataSourceDataNodesMapByDataSourceName(tableAndDataNodesMap).entrySet()) {
            result.add(Pair.of(entry.getKey(), new JobDataNodeLine(getJobDataNodeEntries(entry.getValue()))));
        }
        // Sort by dataSourceName, make sure data node lines have the same ordering
        result.sort(Entry.comparingByKey());
        return result.stream().map(Pair::getValue).collect(Collectors.toList());
    }
    
    private static List<JobDataNodeEntry> getJobDataNodeEntries(final Map<String, List<DataNode>> dataNodeMap) {
        return dataNodeMap.entrySet().stream().map(entry -> new JobDataNodeEntry(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
    
    private static Map<String, Map<String, List<DataNode>>> groupDataSourceDataNodesMapByDataSourceName(final Map<String, List<DataNode>> actualDataNodes) {
        Map<String, Map<String, List<DataNode>>> result = new LinkedHashMap<>();
        for (Entry<String, List<DataNode>> entry : actualDataNodes.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Map<String, List<DataNode>> groupedDataNodesMap = result.computeIfAbsent(each.getDataSourceName(), key -> new LinkedHashMap<>());
                groupedDataNodesMap.computeIfAbsent(entry.getKey(), key -> new LinkedList<>()).add(each);
            }
        }
        return result;
    }
    
    /**
     * Build actual and logic table name mapper.
     *
     * @param dataNodeLine data node line
     * @return actual and logic table name mapper
     */
    public static ActualAndLogicTableNameMapper buildTableNameMapper(final JobDataNodeLine dataNodeLine) {
        Map<ShardingSphereIdentifier, ShardingSphereIdentifier> map = new LinkedHashMap<>();
        for (JobDataNodeEntry each : dataNodeLine.getEntries()) {
            for (DataNode dataNode : each.getDataNodes()) {
                map.put(new ShardingSphereIdentifier(dataNode.getTableName()), new ShardingSphereIdentifier(each.getLogicTableName()));
            }
        }
        return new ActualAndLogicTableNameMapper(map);
    }
}
