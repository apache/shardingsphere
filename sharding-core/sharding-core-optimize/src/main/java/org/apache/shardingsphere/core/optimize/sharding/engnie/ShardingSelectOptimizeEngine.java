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

package org.apache.shardingsphere.core.optimize.sharding.engnie;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.groupby.GroupByEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.item.engine.SelectItemsEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.orderby.OrderByEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.segment.pagination.engine.PaginationEngine;
import org.apache.shardingsphere.core.optimize.api.statement.SelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.SubqueryPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.List;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSelectOptimizeEngine implements OptimizeEngine<SelectStatement> {
    
    @Override
    public SelectOptimizedStatement optimize(final TableMetas tableMetas, final String sql, final List<Object> parameters, final SelectStatement sqlStatement) {
        GroupByEngine groupByEngine = new GroupByEngine();
        OrderByEngine orderByEngine = new OrderByEngine();
        SelectItemsEngine selectItemsEngine = new SelectItemsEngine(tableMetas);
        PaginationEngine paginationEngine = new PaginationEngine();
        GroupBy groupBy = groupByEngine.createGroupBy(sqlStatement);
        OrderBy orderBy = orderByEngine.createOrderBy(sqlStatement, groupBy);
        SelectItems selectItems = selectItemsEngine.createSelectItems(sql, sqlStatement, groupBy, orderBy);
        Pagination pagination = paginationEngine.createPagination(sqlStatement, selectItems, parameters);
        SelectOptimizedStatement result = new SelectOptimizedStatement(sqlStatement, groupBy, orderBy, selectItems, pagination);
        setContainsSubquery(sqlStatement, result);
        return result;
    }
    
    private void setContainsSubquery(final SelectStatement sqlStatement, final SelectOptimizedStatement optimizedStatement) {
        Collection<SubqueryPredicateSegment> subqueryPredicateSegments = sqlStatement.findSQLSegments(SubqueryPredicateSegment.class);
        for (SubqueryPredicateSegment each : subqueryPredicateSegments) {
            if (!each.getAndPredicates().isEmpty()) {
                optimizedStatement.setContainsSubquery(true);
                return;
            }
        }
    }
}
