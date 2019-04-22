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

package org.apache.shardingsphere.core.route.type.unicast;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingTable;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unicast routing engine.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class UnicastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final Collection<String> logicTables;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        if (shardingRule.isAllBroadcastTables(logicTables)) {
            List<RoutingTable> routingTables = new ArrayList<>(logicTables.size());
            for (String each : logicTables) {
                routingTables.add(new RoutingTable(each, each));
            }
            TableUnit tableUnit = new TableUnit(shardingRule.getShardingDataSourceNames().getRandomDataSourceName());
            tableUnit.getRoutingTables().addAll(routingTables);
            result.getTableUnits().getTableUnits().add(tableUnit);
        } else if (logicTables.isEmpty()) {
            result.getTableUnits().getTableUnits().add(new TableUnit(shardingRule.getShardingDataSourceNames().getRandomDataSourceName()));
        } else if (1 == logicTables.size()) {
            String logicTableName = logicTables.iterator().next();
            DataNode dataNode = shardingRule.getDataNode(logicTableName);
            TableUnit tableUnit = new TableUnit(dataNode.getDataSourceName());
            tableUnit.getRoutingTables().add(new RoutingTable(logicTableName, dataNode.getTableName()));
            result.getTableUnits().getTableUnits().add(tableUnit);
        } else {
            List<RoutingTable> routingTables = new ArrayList<>(logicTables.size());
            Set<String> availableDatasourceNames = null;
            boolean first = true;
            for (String each : logicTables) {
                TableRule tableRule = shardingRule.getTableRule(each);
                DataNode dataNode = tableRule.getActualDataNodes().get(0);
                routingTables.add(new RoutingTable(each, dataNode.getTableName()));
                Set<String> currentDataSourceNames = new HashSet<>(tableRule.getActualDatasourceNames().size());
                for (DataNode eachDataNode : tableRule.getActualDataNodes()) {
                    currentDataSourceNames.add(eachDataNode.getDataSourceName());
                }
                if (first) {
                    availableDatasourceNames = currentDataSourceNames;
                    first = false;
                } else {
                    availableDatasourceNames = Sets.intersection(availableDatasourceNames, currentDataSourceNames);
                }
            }
            if (availableDatasourceNames.isEmpty()) {
                throw new ShardingConfigurationException("Cannot find actual datasource intersection for logic tables: %s", logicTables);
            }
            TableUnit tableUnit = new TableUnit(shardingRule.getShardingDataSourceNames().getRandomDataSourceName(availableDatasourceNames));
            tableUnit.getRoutingTables().addAll(routingTables);
            result.getTableUnits().getTableUnits().add(tableUnit);
        }
        return result;
    }
}
