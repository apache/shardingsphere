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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.sharding.exception.metadata.DropInUsedTablesException;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
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
public final class ShardingDropTableStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                            final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        DropTableStatement dropTableStatement = (DropTableStatement) sqlStatementContext.getSqlStatement();
        if (!DropTableStatementHandler.ifExists(dropTableStatement)) {
            String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
            ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName()
                    .map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
            validateTableExist(schema, sqlStatementContext.getTablesContext().getSimpleTableSegments());
        }
        if (DropTableStatementHandler.containsCascade(dropTableStatement)) {
            throw new UnsupportedShardingOperationException("DROP TABLE ... CASCADE",
                    sqlStatementContext.getTablesContext().getSimpleTableSegments().iterator().next().getTableName().getIdentifier().getValue());
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        DropTableStatement dropTableStatement = (DropTableStatement) sqlStatementContext.getSqlStatement();
        checkTableInUsed(shardingRule, dropTableStatement, routeContext);
        for (SimpleTableSegment each : dropTableStatement.getTables()) {
            if (isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, each.getTableName().getIdentifier().getValue())) {
                throw new ShardingDDLRouteException("DROP", "TABLE", sqlStatementContext.getTablesContext().getTableNames());
            }
        }
    }
    
    private void checkTableInUsed(final ShardingRule shardingRule, final DropTableStatement sqlStatement, final RouteContext routeContext) {
        Collection<String> inUsedTables = new LinkedList<>();
        Set<String> dropTables = sqlStatement.getTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
        Set<String> actualTables = routeContext.getRouteUnits().stream().flatMap(each -> each.getTableMappers().stream().map(RouteMapper::getActualName)).collect(Collectors.toSet());
        Collection<String> tableMeta = shardingRule.getTableRules().values().stream().filter(each -> !dropTables.contains(each.getLogicTable()))
                .flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
        for (String each : actualTables) {
            if (tableMeta.contains(each)) {
                inUsedTables.add(each);
            }
        }
        if (!inUsedTables.isEmpty()) {
            throw new DropInUsedTablesException(inUsedTables);
        }
    }
}
