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

package org.apache.shardingsphere.sharding.checker.sql.dml;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.common.ShardingSupportedCommonChecker;
import org.apache.shardingsphere.sharding.exception.syntax.InsertSelectTableViolationException;
import org.apache.shardingsphere.sharding.exception.syntax.MissingGenerateKeyColumnWithInsertSelectException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * Insert supported checker for sharding.
 */
@HighFrequencyInvocation
public final class ShardingInsertSupportedChecker implements SupportedSQLChecker<InsertStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final InsertStatementContext sqlStatementContext) {
        if (null == sqlStatementContext.getInsertSelectContext()) {
            ShardingSupportedCommonChecker.checkMultipleTable(rule, sqlStatementContext);
        }
        InsertStatement insertStatement = sqlStatementContext.getSqlStatement();
        Optional<SubquerySegment> insertSelectSegment = insertStatement.getInsertSelect();
        if (!insertSelectSegment.isPresent()) {
            return;
        }
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        if (isContainsKeyGenerateStrategy(rule, tableName) && !isContainsKeyGenerateColumn(rule, insertStatement.getColumns(), tableName)) {
            throw new MissingGenerateKeyColumnWithInsertSelectException();
        }
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        if (rule.containsShardingTable(tablesContext.getTableNames()) && !isAllSameTables(tablesContext.getTableNames()) && !rule.isAllConfigBindingTables(tablesContext.getTableNames())) {
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
        return 1L == tableNames.stream().distinct().count();
    }
}
