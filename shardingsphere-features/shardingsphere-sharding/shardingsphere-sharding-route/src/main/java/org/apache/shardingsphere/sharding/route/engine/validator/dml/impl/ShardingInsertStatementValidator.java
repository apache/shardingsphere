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
import org.apache.shardingsphere.infra.metadata.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
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
    
    @Override
    public void postValidate(final InsertStatement sqlStatement, final RouteContext routeContext) {
    }
}
