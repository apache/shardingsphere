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

package io.shardingjdbc.core.routing.type.none;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.simple.SimpleRoutingEngine;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * None table routing engine.
 * 
 * @author caohao
 */
@RequiredArgsConstructor
public final class NoneTableRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        return new SimpleRoutingEngine(shardingRule, parameters, getLogicTableName(), sqlStatement).route();
    }
    
    private String getLogicTableName() {
        Preconditions.checkState(sqlStatement.getSqlTokens().size() == 1);
        IndexToken indexToken = (IndexToken) sqlStatement.getSqlTokens().get(0);
        String indexName = indexToken.getIndexName();
        String logicTableName = "";
        for (TableRule each : shardingRule.getTableRules()) {
            if (indexName.equals(each.getLogicIndex())) {
                logicTableName = each.getLogicTable();
            }
        }
        return logicTableName;
    }
}
