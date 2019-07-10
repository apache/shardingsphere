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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupByEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItemsEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.PaginationEngine;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSelectOptimizeEngine implements OptimizeEngine {
    
    private final SelectStatement selectStatement;
    
    private final List<Object> parameters;
    
    private final WhereClauseShardingConditionEngine shardingConditionEngine;
    
    private final WhereClauseEncryptConditionEngine encryptConditionEngine;
    
    private final GroupByEngine groupByEngine;
    
    private final OrderByEngine orderByEngine;
    
    private final SelectItemsEngine selectItemsEngine;
    
    private final PaginationEngine paginationEngine;
    
    public ShardingSelectOptimizeEngine(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final SelectStatement selectStatement, final List<Object> parameters) {
        this.selectStatement = selectStatement;
        this.parameters = parameters;
        shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, shardingTableMetaData);
        encryptConditionEngine = new WhereClauseEncryptConditionEngine(shardingRule.getEncryptRule(), shardingTableMetaData);
        groupByEngine = new GroupByEngine();
        orderByEngine = new OrderByEngine();
        selectItemsEngine = new SelectItemsEngine(shardingTableMetaData);
        paginationEngine = new PaginationEngine();
    }
    
    @Override
    public ShardingSelectOptimizedStatement optimize() {
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(selectStatement, parameters);
        AndCondition encryptConditions = encryptConditionEngine.createEncryptConditions(selectStatement);
        GroupBy groupBy = groupByEngine.createGroupBy(selectStatement);
        OrderBy orderBy = orderByEngine.createOrderBy(selectStatement, groupBy);
        SelectItems selectItems = selectItemsEngine.createSelectItems(selectStatement, groupBy, orderBy);
        Pagination pagination = paginationEngine.createPagination(selectStatement, parameters);
        return new ShardingSelectOptimizedStatement(selectStatement, shardingConditions, encryptConditions, groupBy, orderBy, selectItems, pagination);
    }
}
