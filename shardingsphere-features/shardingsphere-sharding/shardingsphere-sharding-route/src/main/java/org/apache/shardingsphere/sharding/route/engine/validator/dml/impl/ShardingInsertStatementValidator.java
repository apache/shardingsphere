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

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding insert statement validator.
 */
public final class ShardingInsertStatementValidator extends ShardingDMLStatementValidator<InsertStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<InsertStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        if (null == ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            validateShardingMultipleTable(shardingRule, sqlStatementContext);
        }
        InsertStatement sqlStatement = sqlStatementContext.getSqlStatement();
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatement);
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (onDuplicateKeyColumnsSegment.isPresent() && isUpdateShardingKey(shardingRule, onDuplicateKeyColumnsSegment.get(), tableName)) {
            throw new ShardingSphereException("INSERT INTO ... ON DUPLICATE KEY UPDATE can not support update for sharding column.");
        }
        Optional<SubquerySegment> insertSelectSegment = sqlStatement.getInsertSelect();
        if (insertSelectSegment.isPresent() && isContainsKeyGenerateStrategy(shardingRule, tableName)
                && !isContainsKeyGenerateColumn(shardingRule, sqlStatement.getColumns(), tableName)) {
            throw new ShardingSphereException("INSERT INTO ... SELECT can not support applying keyGenerator to absent generateKeyColumn.");
        }
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        if (insertSelectSegment.isPresent() && !isAllSameTables(tablesContext.getTableNames()) && !shardingRule.isAllBindingTables(tablesContext.getTableNames())) {
            throw new ShardingSphereException("The table inserted and the table selected must be the same or bind tables.");
        }
        if (insertSelectSegment.isPresent()) {
            checkSubqueryShardingValues(shardingRule, sqlStatementContext, parameters, schema);
        }
    }
    
    private boolean isUpdateShardingKey(final ShardingRule shardingRule, final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment, final String tableName) {
        for (AssignmentSegment each : onDuplicateKeyColumnsSegment.getColumns()) {
            if (shardingRule.isShardingColumn(each.getColumn().getIdentifier().getValue(), tableName)) {
                return true;
            }
        }
        return false;
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

    private void checkSubqueryShardingValues(final ShardingRule shardingRule, final SQLStatementContext<InsertStatement> sqlStatementContext,
        final List<Object> parameters, final ShardingSphereSchema schema) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(shardingRule, tableRule.get())
                && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        ShardingConditions shardingConditions = createShardingConditions(sqlStatementContext, parameters, schema, shardingRule);
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingRule, shardingConditions), "Sharding value must same with subquery.");
        }
    }

    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration
            && shardingRule.getTableShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration;
    }

    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            if (!isSameShardingCondition(shardingRule, example, each)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getValues().size() != shardingCondition2.getValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getValues().size(); i++) {
            ShardingConditionValue shardingConditionValue1 = shardingCondition1.getValues().get(i);
            ShardingConditionValue shardingConditionValue2 = shardingCondition2.getValues().get(i);
            if (!isSameShardingConditionValue(shardingRule, shardingConditionValue1, shardingConditionValue2)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameShardingConditionValue(final ShardingRule shardingRule, final ShardingConditionValue shardingConditionValue1, final ShardingConditionValue shardingConditionValue2) {
        return isSameLogicTable(shardingRule, shardingConditionValue1, shardingConditionValue2) && shardingConditionValue1.getColumnName().equals(shardingConditionValue2.getColumnName())
            && isSameValue(shardingConditionValue1, shardingConditionValue2);
    }

    private boolean isSameLogicTable(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingRule, shardingValue1, shardingValue2);
    }

    private boolean isBindingTable(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean isSameValue(final ShardingConditionValue shardingConditionValue1, final ShardingConditionValue shardingConditionValue2) {
        if (shardingConditionValue1 instanceof ListShardingConditionValue && shardingConditionValue2 instanceof ListShardingConditionValue) {
            return SafeNumberOperationUtils.safeCollectionEquals(
                ((ListShardingConditionValue) shardingConditionValue1).getValues(), ((ListShardingConditionValue) shardingConditionValue2).getValues());
        } else if (shardingConditionValue1 instanceof RangeShardingConditionValue && shardingConditionValue2 instanceof RangeShardingConditionValue) {
            return SafeNumberOperationUtils.safeRangeEquals(
                ((RangeShardingConditionValue) shardingConditionValue1).getValueRange(), ((RangeShardingConditionValue) shardingConditionValue2).getValueRange());
        }
        return false;
    }

    private ShardingConditions createShardingConditions(final SQLStatementContext<InsertStatement> sqlStatementContext,
        final List<Object> parameters, final ShardingSphereSchema schema, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions;
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingConditionEngine shardingConditionEngine = new WhereClauseShardingConditionEngine(rule, schema);
            shardingConditions = shardingConditionEngine.createShardingConditions(sqlStatementContext, parameters);
        } else {
            shardingConditions = Collections.emptyList();
        }
        return new ShardingConditions(shardingConditions);
    }
    
    @Override
    public void postValidate(final InsertStatement sqlStatement, final RouteContext routeContext) {
    }
}
