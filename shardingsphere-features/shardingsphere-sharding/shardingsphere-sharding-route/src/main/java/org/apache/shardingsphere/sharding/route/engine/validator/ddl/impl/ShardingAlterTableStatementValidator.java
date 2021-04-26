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
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding alter table statement validator.
 */
public final class ShardingAlterTableStatementValidator extends ShardingDDLStatementValidator<AlterTableStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<AlterTableStatement> sqlStatementContext, final List<Object> parameters, final ShardingSphereSchema schema) {
        Collection<String> shardingLogicTableNames = getShardingLogicTableNames(shardingRule, sqlStatementContext);
        if (shardingLogicTableNames.size() <= 1) {
            return;
        }
        if (shardingRule.isAllSingleDataNodeTables(shardingLogicTableNames) && !isAllTableInSameDataSource(shardingRule, shardingLogicTableNames)) {
            throw new ShardingSphereException("Multiple sharding tables of single data node must be in the same database.");
        }
        if (!shardingRule.isAllSingleDataNodeTables(shardingLogicTableNames) && !shardingRule.isAllBindingTables(shardingLogicTableNames)) {
            throw new ShardingSphereException("Multiple sharding tables of multi data node must be binding tables.");
        }
    }
    
    @Override
    public void postValidate(final AlterTableStatement sqlStatement, final RouteContext routeContext) {
    }
    
    private Collection<String> getShardingLogicTableNames(final ShardingRule shardingRule, final SQLStatementContext<AlterTableStatement> sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList())
                : sqlStatementContext.getTablesContext().getTableNames();
        return shardingRule.getShardingLogicTableNames(tableNames);
    }
    
    private boolean isAllTableInSameDataSource(final ShardingRule shardingRule, final Collection<String> shardingLogicTableNames) {
        List<TableRule> tableRules = shardingLogicTableNames.stream().map(shardingRule::getTableRule).collect(Collectors.toList());
        return 1 == tableRules.stream().map(TableRule::getActualDatasourceNames).distinct().count();
    }
}
