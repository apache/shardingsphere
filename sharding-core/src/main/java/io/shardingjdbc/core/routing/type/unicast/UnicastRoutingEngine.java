/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.routing.type.unicast;

import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Unicast routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class UnicastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final Collection<String> logicTables;
    
    @Override
    public RoutingResult route() {
        RoutingResult result;
        if (logicTables.isEmpty()) {
            result = new RoutingResult();
            result.getTableUnits().getTableUnits().add(new TableUnit(shardingRule.getDataSourceNames().iterator().next(), "", ""));
        } else if (1 == logicTables.size()) {
            String logicTableName = logicTables.iterator().next();
            DataNode dataNode = shardingRule.findDataNode(logicTableName);
            result = new RoutingResult();
            result.getTableUnits().getTableUnits().add(new TableUnit(dataNode.getDataSourceName(), logicTableName, dataNode.getTableName()));
        } else {
            String dataSourceName = null;
            Map<String, DataNode> dataNodeMap = new LinkedHashMap<>(logicTables.size(), 1);
            for (String each : logicTables) {
                DataNode dataNode = shardingRule.findDataNode(dataSourceName, each);
                dataNodeMap.put(each, dataNode);
                if (null == dataSourceName) {
                    dataSourceName = dataNode.getDataSourceName();
                }
            }
            result = new UnicastRoutingResult(dataSourceName);
            for (Entry<String, DataNode> entry : dataNodeMap.entrySet()) {
                result.getTableUnits().getTableUnits().add(new TableUnit(entry.getValue().getDataSourceName(), entry.getKey(), entry.getValue().getTableName()));
            }
        }
        return result;
    }
}
