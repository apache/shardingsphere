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
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLPrepareStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding prepare statement validator.
 */
public final class ShardingPrepareStatementValidator extends ShardingDDLStatementValidator<PostgreSQLPrepareStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<PostgreSQLPrepareStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (!shardingRule.tableRuleExists(tableNames) && !shardingRule.isSingleTablesInSameDataSource(tableNames)) {
            throw new ShardingSphereException("Single tables must be in the same datasource.");
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<PostgreSQLPrepareStatement> sqlStatementContext, 
                             final RouteContext routeContext, final ShardingSphereSchema schema) {
        if (routeContext.getRouteUnits().isEmpty()) {
            throw new ShardingSphereException("Can not get route result, please check your sharding table config.");
        }
        if (routeContext.getRouteUnits().stream().collect(Collectors.groupingBy(RouteUnit::getDataSourceMapper)).entrySet().stream().anyMatch(each -> each.getValue().size() > 1)) {
            throw new ShardingSphereException("Prepare ... statement can not support sharding tables route to same data sources.");
        }
    }
}
