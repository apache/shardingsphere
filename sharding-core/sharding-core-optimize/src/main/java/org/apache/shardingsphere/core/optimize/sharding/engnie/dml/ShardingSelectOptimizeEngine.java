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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.engine.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupByEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.engine.SelectItemsEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.engine.PaginationEngine;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.SubqueryPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.List;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSelectOptimizeEngine implements ShardingOptimizeEngine<SelectStatement> {
    
    @Override
    public ShardingSelectOptimizedStatement optimize(final ShardingRule shardingRule,
                                                     final ShardingTableMetaData shardingTableMetaData, final String sql, final List<Object> parameters, final SelectStatement sqlStatement) {
        WhereClauseShardingConditionEngine shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, shardingTableMetaData);
        WhereClauseEncryptConditionEngine encryptConditionEngine = new WhereClauseEncryptConditionEngine(shardingRule.getEncryptRule(), shardingTableMetaData);
        GroupByEngine groupByEngine = new GroupByEngine();
        OrderByEngine orderByEngine = new OrderByEngine();
        SelectItemsEngine selectItemsEngine = new SelectItemsEngine(shardingTableMetaData);
        PaginationEngine paginationEngine = new PaginationEngine();
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(sqlStatement, parameters);
        List<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(sqlStatement);
        GroupBy groupBy = groupByEngine.createGroupBy(sqlStatement);
        OrderBy orderBy = orderByEngine.createOrderBy(sqlStatement, groupBy);
        SelectItems selectItems = selectItemsEngine.createSelectItems(sql, sqlStatement, groupBy, orderBy);
        Pagination pagination = paginationEngine.createPagination(sqlStatement, selectItems, parameters);
        ShardingSelectOptimizedStatement result = new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditions, groupBy, orderBy, selectItems, pagination);
        setContainsSubquery(sqlStatement, result);
        return result;
    }
    
    private void setContainsSubquery(final SelectStatement sqlStatement, final ShardingSelectOptimizedStatement optimizedStatement) {
        Collection<SubqueryPredicateSegment> subqueryPredicateSegments = sqlStatement.findSQLSegments(SubqueryPredicateSegment.class);
        for (SubqueryPredicateSegment each : subqueryPredicateSegments) {
            if (!each.getAndPredicates().isEmpty()) {
                optimizedStatement.setContainsSubquery(true);
                break;
            }
        }
    }
}
