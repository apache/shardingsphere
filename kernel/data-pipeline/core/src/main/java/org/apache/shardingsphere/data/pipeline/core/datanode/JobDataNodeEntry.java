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

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Job data node entry.
 */
@RequiredArgsConstructor
@Getter
public final class JobDataNodeEntry {
    
    private final String logicTableName;
    
    private final List<DataNode> dataNodes;
    
    /**
     * Unmarshal from text.
     *
     * @param text marshalled entry
     * @return entry
     */
    public static JobDataNodeEntry unmarshal(final String text) {
        List<String> segments = Splitter.on(":").splitToList(text);
        String logicTableName = segments.get(0);
        List<DataNode> dataNodes = new LinkedList<>();
        for (String each : Splitter.on(",").omitEmptyStrings().splitToList(segments.get(1))) {
            dataNodes.add(DataNodeUtils.parseWithSchema(each));
        }
        return new JobDataNodeEntry(logicTableName, dataNodes);
    }
    
    /**
     * Marshal to text.
     *
     * @return text, format: logicTableName:dataNode1,dataNode2, e.g. t_order:ds_0.t_order_0,ds_0.t_order_1
     */
    public String marshal() {
        StringBuilder result = new StringBuilder();
        result.append(logicTableName).append(':');
        for (DataNode each : dataNodes) {
            result.append(DataNodeUtils.formatWithSchema(each)).append(',');
        }
        if (!dataNodes.isEmpty()) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }
}
