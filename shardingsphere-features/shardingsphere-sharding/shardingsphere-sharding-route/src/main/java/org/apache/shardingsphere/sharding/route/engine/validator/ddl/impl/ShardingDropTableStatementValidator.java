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
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropTableStatementHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding drop table statement validator.
 */
public final class ShardingDropTableStatementValidator extends ShardingDDLStatementValidator<DropTableStatement> {
    
    private ShardingRule shardingRule;
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<DropTableStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        this.shardingRule = shardingRule;
        if (!DropTableStatementHandler.containsIfExistClause(sqlStatementContext.getSqlStatement())) {
            validateTableExist(schema, sqlStatementContext.getTablesContext().getTables());
        }
    }
    
    @Override
    public void postValidate(final DropTableStatement sqlStatement, final RouteContext routeContext) {
        checkTableInUsed(sqlStatement, routeContext);
    }
    
    private void checkTableInUsed(final DropTableStatement sqlStatement, final RouteContext routeContext) {
        Collection<String> inUsedTable = new LinkedList<>();
        Set<String> dropTables = sqlStatement.getTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
        Set<String> actualTables = routeContext.getRouteUnits().stream().flatMap(each -> each.getTableMappers().stream().map(RouteMapper::getActualName)).collect(Collectors.toSet());
        Collection<String> tableMeta = shardingRule.getTableRules().stream().filter(each -> !dropTables.contains(each.getLogicTable()))
                .flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
        for (String each : actualTables) {
            if (tableMeta.contains(each)) {
                inUsedTable.add(each);
            }
        }
        if (!inUsedTable.isEmpty()) {
            throw new ShardingSphereException("Actual Tables: [%s] are in use.", inUsedTable);
        }
    }
}
