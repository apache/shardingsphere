/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.routing.type.broadcast;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.RoutingTable;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Broadcast routing engine for tables.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class TableBroadcastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        for (String each : getLogicTableNames()) {
            result.getTableUnits().getTableUnits().addAll(getAllTableUnits(each));
        }
        return result;
    }
    
    private Collection<String> getLogicTableNames() {
        if (isOperateIndexWithoutTable()) {
            return Collections.singletonList(shardingRule.getLogicTableName(getIndexToken().getIndexName()));
        }
        return sqlStatement.getTables().getTableNames();
    }
    
    private boolean isOperateIndexWithoutTable() {
        return sqlStatement instanceof DDLStatement && sqlStatement.getTables().isEmpty();
    }
    
    private IndexToken getIndexToken() {
        List<SQLToken> sqlTokens = sqlStatement.getSQLTokens();
        Preconditions.checkState(1 == sqlTokens.size());
        return (IndexToken) sqlTokens.get(0);
    }
    
    private Collection<TableUnit> getAllTableUnits(final String logicTableName) {
        Collection<TableUnit> result = new LinkedList<>();
        TableRule tableRule = shardingRule.getTableRuleByLogicTableName(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            TableUnit tableUnit = new TableUnit(each.getDataSourceName());
            tableUnit.getRoutingTables().add(new RoutingTable(logicTableName, each.getTableName()));
            result.add(tableUnit);
        }
        return result;
    }
}
