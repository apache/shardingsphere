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

package org.apache.shardingsphere.sharding.route.engine.checker.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding drop index route context checker.
 */
public final class ShardingDropIndexRouteContextChecker implements ShardingRouteContextChecker {
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) queryContext.getSqlStatementContext().getSqlStatement();
        Collection<IndexSegment> indexSegments = dropIndexStatement.getIndexes();
        Optional<String> logicTableName = dropIndexStatement.getSimpleTable().map(optional -> optional.getTableName().getIdentifier().getValue());
        if (logicTableName.isPresent()) {
            validateDropIndexRouteUnit(shardingRule, routeContext, indexSegments, logicTableName.get());
        } else {
            String defaultSchemaName = new DatabaseTypeRegistry(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
            for (IndexSegment each : indexSegments) {
                ShardingSphereSchema schema = each.getOwner().map(optional -> optional.getIdentifier().getValue()).map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
                logicTableName = schema.getAllTables().stream().filter(table -> table.containsIndex(each.getIndexName().getIdentifier().getValue())).findFirst().map(ShardingSphereTable::getName);
                logicTableName.ifPresent(optional -> validateDropIndexRouteUnit(shardingRule, routeContext, indexSegments, optional));
            }
        }
    }
    
    private void validateDropIndexRouteUnit(final ShardingRule shardingRule, final RouteContext routeContext, final Collection<IndexSegment> indexSegments, final String logicTableName) {
        if (ShardingSupportedCheckUtils.isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, logicTableName)) {
            Collection<String> indexNames = indexSegments.stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList());
            throw new ShardingDDLRouteException("DROP", "INDEX", indexNames);
        }
    }
}
