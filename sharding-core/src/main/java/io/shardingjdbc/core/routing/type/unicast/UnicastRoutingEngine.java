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

import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.complex.CartesianRoutingEngine;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unicast routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class UnicastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final SQLStatement sqlStatement;
    
    private final Collection<String> logicTables;
    
    @Override
    public RoutingResult route() {
        Collection<RoutingResult> result = new ArrayList<>(logicTables.size());
        if (logicTables.isEmpty()) {
            RoutingResult routingResult = new RoutingResult();
            result.add(routingResult);
            routingResult.getTableUnits().getTableUnits().add(new TableUnit(shardingRule.getDataSourceNames().iterator().next(), "", ""));
        } else if (logicTables.size() == 1) {
            String logicTableName = logicTables.iterator().next();
            DataNode dataNode = shardingRule.findDataNodeByLogicTable(logicTableName);
            RoutingResult routingResult = new RoutingResult();
            result.add(routingResult);
            routingResult.getTableUnits().getTableUnits().add(new TableUnit(dataNode.getDataSourceName(), logicTableName, dataNode.getTableName()));
        } else {
            String dataSourceName = null;
            for (String each : logicTables) {
                RoutingResult routingResult = new RoutingResult();
                result.add(routingResult);
                if (null == dataSourceName) {
                    DataNode dataNode = shardingRule.findDataNodeByLogicTable(each);
                    dataSourceName = dataNode.getDataSourceName();
                    routingResult.getTableUnits().getTableUnits().add(new TableUnit(dataNode.getDataSourceName(), each, dataNode.getTableName()));
                } else {
                    DataNode dataNode = shardingRule.findDataNodeByDataSourceAndLogicTable(dataSourceName, each);
                    routingResult.getTableUnits().getTableUnits().add(new TableUnit(dataNode.getDataSourceName(), each, dataNode.getTableName()));
                }
            }
        }
        if (1 == result.size()) {
            return result.iterator().next();
        }
        return new CartesianRoutingEngine(result).route();
    }
}
