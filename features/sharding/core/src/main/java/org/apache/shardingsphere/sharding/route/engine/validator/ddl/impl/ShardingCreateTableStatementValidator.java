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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;

import java.util.Collections;
import java.util.List;

/**
 * Sharding create table statement validator.
 */
public final class ShardingCreateTableStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                            final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        CreateTableStatementContext createTableStatementContext = (CreateTableStatementContext) sqlStatementContext;
        CreateTableStatement createTableStatement = createTableStatementContext.getSqlStatement();
        if (!createTableStatement.isIfNotExists()) {
            String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName());
            ShardingSphereSchema schema = createTableStatementContext.getTablesContext().getSchemaName()
                    .map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
            validateTableNotExist(schema, Collections.singleton(createTableStatement.getTable()));
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        CreateTableStatementContext createTableStatementContext = (CreateTableStatementContext) sqlStatementContext;
        String primaryTable = (createTableStatementContext.getSqlStatement()).getTable().getTableName().getIdentifier().getValue();
        if (isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, primaryTable)) {
            throw new ShardingDDLRouteException("CREATE", "TABLE", createTableStatementContext.getTablesContext().getTableNames());
        }
    }
}
