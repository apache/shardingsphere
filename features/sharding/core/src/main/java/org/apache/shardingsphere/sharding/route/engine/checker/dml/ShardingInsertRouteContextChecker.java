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

package org.apache.shardingsphere.sharding.route.engine.checker.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.algorithm.DuplicateInsertDataRecordException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.util.ShardingRouteContextCheckUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Sharding insert route context checker.
 */
@RequiredArgsConstructor
public final class ShardingInsertRouteContextChecker implements ShardingRouteContextChecker {
    
    private final ShardingConditions shardingConditions;
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext,
                      final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        InsertStatement insertStatement = (InsertStatement) sqlStatementContext.getSqlStatement();
        Optional<SubquerySegment> insertSelect = insertStatement.getInsertSelect();
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        if (insertSelect.isPresent() && shardingConditions.isNeedMerge()) {
            boolean singleRoutingOrSameShardingCondition = routeContext.isSingleRouting() || shardingConditions.isSameShardingCondition();
            ShardingSpherePreconditions.checkState(singleRoutingOrSameShardingCondition, () -> new UnsupportedShardingOperationException("INSERT ... SELECT ...", tableName));
        }
        Collection<ColumnAssignmentSegment> assignments = insertStatement.getOnDuplicateKeyColumns().map(OnDuplicateKeyColumnsSegment::getColumns).orElse(Collections.emptyList());
        Optional<ShardingConditions> onDuplicateKeyShardingConditions =
                ShardingRouteContextCheckUtils.createShardingConditions(sqlStatementContext, shardingRule, assignments, queryContext.getParameters());
        Optional<RouteContext> onDuplicateKeyRouteContext = onDuplicateKeyShardingConditions
                .map(optional -> new ShardingStandardRouteEngine(tableName, optional, sqlStatementContext, queryContext.getHintValueContext(), props).route(shardingRule));
        if (onDuplicateKeyRouteContext.isPresent() && !ShardingRouteContextCheckUtils.isSameRouteContext(routeContext, onDuplicateKeyRouteContext.get())) {
            throw new UnsupportedUpdatingShardingValueException(tableName);
        }
        if (!routeContext.isSingleRouting()) {
            boolean isSingleDataNode = routeContext.getOriginalDataNodes().stream().allMatch(dataNodes -> 1 == dataNodes.size());
            ShardingSpherePreconditions.checkState(isSingleDataNode, () -> new DuplicateInsertDataRecordException(shardingConditions, tableName));
        }
    }
}
