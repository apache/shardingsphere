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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.List;

/**
 * CDC table rule util.
 */
public final class CDCTableRuleUtil {
    
    /**
     * Get actual data nodes.
     *
     * @param shardingRule sharding rule
     * @param tableName table name
     * @return data nodes
     */
    public static List<DataNode> getActualDataNodes(final ShardingRule shardingRule, final String tableName) {
        TableRule tableRule = shardingRule.getTableRule(tableName);
        // TODO support virtual data source name
        return tableRule.getActualDataNodes();
    }
}
