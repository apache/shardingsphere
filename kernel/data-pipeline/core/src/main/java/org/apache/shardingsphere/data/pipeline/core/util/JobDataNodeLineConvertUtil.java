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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Job data node line convert util.
 */
public final class JobDataNodeLineConvertUtil {
    
    /**
     * Convert data nodes to lines.
     *
     * @param actualDataNodes actual data nodes
     * @return job data node line list.
     */
    public static List<JobDataNodeLine> convertDataNodesToLines(final Map<String, List<DataNode>> actualDataNodes) {
        List<Pair<String, JobDataNodeLine>> result = new LinkedList<>();
        Map<String, Map<String, List<DataNode>>> groupedDataSourceDataNodesMap = groupDataSourceDataNodesMapByDataSourceName(actualDataNodes);
        for (String each : groupedDataSourceDataNodesMap.keySet()) {
            List<JobDataNodeEntry> dataNodeEntries = new LinkedList<>();
            for (Entry<String, List<DataNode>> entry : groupedDataSourceDataNodesMap.get(each).entrySet()) {
                dataNodeEntries.add(new JobDataNodeEntry(entry.getKey(), entry.getValue()));
            }
            result.add(Pair.of(each, new JobDataNodeLine(dataNodeEntries)));
        }
        // Sort by dataSourceName, make sure data node lines have the same ordering
        result.sort(Entry.comparingByKey());
        return result.stream().map(Pair::getValue).collect(Collectors.toList());
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
}
