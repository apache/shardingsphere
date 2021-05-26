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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding alter table statement validator.
 */
public final class ShardingAlterTableStatementValidator extends ShardingDDLStatementValidator<AlterTableStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<AlterTableStatement> sqlStatementContext, final List<Object> parameters, final ShardingSphereSchema schema) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList())
                : sqlStatementContext.getTablesContext().getTableNames();
        Optional<SimpleTableSegment> renameTable = sqlStatementContext.getSqlStatement().getRenameTable();
        if (!renameTable.isPresent() && !shardingRule.tableRuleExists(tableNames) && !shardingRule.isSingleTablesInSameDataSource(tableNames)) {
            throw new ShardingSphereException("Single tables must be in the same datasource.");
        }
        if (renameTable.isPresent() && containsShardingBroadcastTable(shardingRule, tableNames)) {
            throw new ShardingSphereException("ALTER TABLE ... RENAME TO ... statement can not support sharding tables and broadcast tables.");
        }
    }
    
    private boolean containsShardingBroadcastTable(final ShardingRule shardingRule, final Collection<String> tableNames) {
        return shardingRule.tableRuleExists(tableNames) || tableNames.stream().anyMatch(shardingRule::isBroadcastTable);
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final AlterTableStatement sqlStatement, final RouteContext routeContext) {
        String primaryTable = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (isRouteUnitPrimaryTableDataNodeDifferentSize(shardingRule, routeContext, primaryTable)) {
            throw new ShardingSphereException("ALTER TABLE ... statement route unit size must be same with primary table '%s' data node size.", primaryTable);
        }
    }
}
