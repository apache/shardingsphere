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

package org.apache.shardingsphere.data.pipeline.api.datanode;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.util.Collection;

/**
 * Job data node entry.
 */
@RequiredArgsConstructor
public final class JobDataNodeEntry {
    
    private final String logicTableName;
    
    private final Collection<DataNode> dataNodes;
    
    /**
     * Marshal to text.
     *
     * @return text, format: logicTableName:dataNode1,dataNode2, e.g. t_order:ds_0.t_order_0,ds_0.t_order_1
     */
    public String marshal() {
        StringBuilder result = new StringBuilder(getMarshalledTextEstimatedLength());
        result.append(logicTableName);
        result.append(":");
        for (DataNode each : dataNodes) {
            result.append(each.format()).append(',');
        }
        if (!dataNodes.isEmpty()) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }
    
    /**
     * Get marshalled text estimated length.
     *
     * @return marshalled text estimated length
     */
    public int getMarshalledTextEstimatedLength() {
        return logicTableName.length() + 1 + dataNodes.stream().mapToInt(DataNode::getFormattedTextLength).sum() + dataNodes.size();
    }
}
