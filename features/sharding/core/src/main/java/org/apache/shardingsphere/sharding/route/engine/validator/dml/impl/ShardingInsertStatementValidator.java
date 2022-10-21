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

package org.apache.shardingsphere.sharding.route.engine.validator.dml.impl;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.syntax.InsertSelectTableViolationException;
import org.apache.shardingsphere.sharding.exception.syntax.MissingGenerateKeyColumnWithInsertSelectException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding insert statement validator.
 */
@RequiredArgsConstructor
public final class ShardingInsertStatementValidator extends ShardingDMLStatementValidator<InsertStatement> {
    
    private final ShardingConditions shardingConditions;
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<InsertStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        if (null == ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            validateMultipleTable(shardingRule, sqlStatementContext);
        }
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Optional<SubquerySegment> insertSelectSegment = sqlStatementContext.getSqlStatement().getInsertSelect();
        if (insertSelectSegment.isPresent() && isContainsKeyGenerateStrategy(shardingRule, tableName)
                && !isContainsKeyGenerateColumn(shardingRule, sqlStatementContext.getSqlStatement().getColumns(), tableName)) {
            throw new MissingGenerateKeyColumnWithInsertSelectException();
        }
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        if (insertSelectSegment.isPresent() && shardingRule.tableRuleExists(tablesContext.getTableNames())
                && !isAllSameTables(tablesContext.getTableNames()) && !shardingRule.isAllBindingTables(tablesContext.getTableNames())) {
            throw new InsertSelectTableViolationException();
        }
    }
    
    private boolean isContainsKeyGenerateStrategy(final ShardingRule shardingRule, final String tableName) {
        return shardingRule.findGenerateKeyColumnName(tableName).isPresent();
    }
    
    private boolean isContainsKeyGenerateColumn(final ShardingRule shardingRule, final Collection<ColumnSegment> columns, final String tableName) {
        return columns.isEmpty() || columns.stream().anyMatch(each -> shardingRule.isGenerateKeyColumn(each.getIdentifier().getValue(), tableName));
    }
    
    private boolean isAllSameTables(final Collection<String> tableNames) {
        return 1 == tableNames.stream().distinct().count();
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<InsertStatement> sqlStatementContext, final List<Object> parameters,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        Optional<SubquerySegment> insertSelect = sqlStatementContext.getSqlStatement().getInsertSelect();
        if (insertSelect.isPresent() && shardingConditions.isNeedMerge()) {
            boolean singleRoutingOrSameShardingCondition = routeContext.isSingleRouting() || shardingConditions.isSameShardingCondition();
            Preconditions.checkState(singleRoutingOrSameShardingCondition, "Subquery sharding conditions must be same with primary query.");
        }
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<AssignmentSegment> assignments = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatementContext.getSqlStatement())
                .map(OnDuplicateKeyColumnsSegment::getColumns).orElse(Collections.emptyList());
        Optional<ShardingConditions> onDuplicateKeyShardingConditions = createShardingConditions(sqlStatementContext, shardingRule, assignments, parameters);
        Optional<RouteContext> onDuplicateKeyRouteContext = onDuplicateKeyShardingConditions.map(optional -> new ShardingStandardRoutingEngine(tableName, optional,
                sqlStatementContext, props).route(shardingRule));
        if (onDuplicateKeyRouteContext.isPresent() && !isSameRouteContext(routeContext, onDuplicateKeyRouteContext.get())) {
            throw new UnsupportedUpdatingShardingValueException(tableName);
        }
        if (!routeContext.isSingleRouting() && !shardingRule.isBroadcastTable(tableName)) {
            boolean isSingleDataNode = routeContext.getOriginalDataNodes().stream().allMatch(dataNodes -> dataNodes.size() == 1);
            Preconditions.checkState(isSingleDataNode, "Insert statement does not support sharding table routing to multiple data nodes.");
        }
    }
}
