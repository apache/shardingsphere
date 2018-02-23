/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.type.unicast;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

/**
 * Unicast routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class UnicastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        if (sqlStatement.getTables().isEmpty()) {
            result.getTableUnits().getTableUnits().add(new TableUnit(shardingRule.getDataSourceMap().keySet().iterator().next(), "", ""));
        } else if (sqlStatement.getTables().isSingleTable()) {
            TableRule tableRule = shardingRule.getTableRule(sqlStatement.getTables().getSingleTableName());
            for (DataNode each : tableRule.getActualDataNodes()) {
                if (shardingRule.getDataSourceMap().containsKey(each.getDataSourceName())) {
                    result.getTableUnits().getTableUnits().add(new TableUnit(each.getDataSourceName(), tableRule.getLogicTable(), each.getTableName()));
                    break;
                }
            }
            Preconditions.checkState(1 == result.getTableUnits().getTableUnits().size(), String.format("Cannot find actual data node for logic table name: '%s'", tableRule.getLogicTable()));
        } else {
            throw new UnsupportedOperationException("Cannot support unicast routing for multiple tables.");
        }
        return result;
    }
}
