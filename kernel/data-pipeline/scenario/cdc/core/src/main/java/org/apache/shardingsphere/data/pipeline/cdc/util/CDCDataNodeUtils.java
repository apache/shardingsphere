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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CDC data node utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCDataNodeUtils {
    
    /**
     * Build data nodes map.
     *
     * @param database database
     * @param tableNames table names
     * @return data nodes map
     * @throws PipelineInvalidParameterException thrown invalid parameter exception when can't get data nodes.
     */
    public static Map<String, List<DataNode>> buildDataNodesMap(final ShardingSphereDatabase database, final Collection<String> tableNames) {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        Optional<SingleRule> singleRule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        Map<String, List<DataNode>> result = new HashMap<>();
        // TODO support virtual data source name
        for (String each : tableNames) {
            if (singleRule.isPresent() && singleRule.get().getAllDataNodes().containsKey(each)) {
                result.put(each, new ArrayList<>(singleRule.get().getAllDataNodes().get(each)));
                continue;
            }
            if (shardingRule.isPresent() && shardingRule.get().findTableRule(each).isPresent()) {
                TableRule tableRule = shardingRule.get().getTableRule(each);
                result.put(each, tableRule.getActualDataNodes());
                continue;
            }
            throw new PipelineInvalidParameterException(String.format("Not find actual data nodes of `%s`", each));
        }
        return result;
    }
}
