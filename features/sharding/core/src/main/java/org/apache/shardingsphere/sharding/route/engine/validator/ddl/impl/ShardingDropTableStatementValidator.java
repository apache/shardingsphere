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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.metadata.InUsedTablesException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTableStatement;

import java.util.Collection;
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
        DropTableStatementContext dropTableStatementContext = (DropTableStatementContext) sqlStatementContext;
        DropTableStatement dropTableStatement = dropTableStatementContext.getSqlStatement();
        if (!dropTableStatement.isIfExists()) {
            String defaultSchemaName = new DatabaseTypeRegistry(dropTableStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName());
            ShardingSphereSchema schema = dropTableStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
            validateTableExist(schema, dropTableStatementContext.getTablesContext().getSimpleTables());
        }
        ShardingSpherePreconditions.checkState(!dropTableStatement.isContainsCascade(), () -> new UnsupportedShardingOperationException("DROP TABLE ... CASCADE",
                dropTableStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue()));
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        DropTableStatementContext dropTableStatementContext = (DropTableStatementContext) sqlStatementContext;
        DropTableStatement dropTableStatement = dropTableStatementContext.getSqlStatement();
        checkTableInUsed(shardingRule, dropTableStatementContext, routeContext);
        for (SimpleTableSegment each : dropTableStatement.getTables()) {
            if (isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, each.getTableName().getIdentifier().getValue())) {
                throw new ShardingDDLRouteException("DROP", "TABLE", dropTableStatementContext.getTablesContext().getTableNames());
            }
        }
    }
    
    private void checkTableInUsed(final ShardingRule shardingRule, final DropTableStatementContext sqlStatementContext, final RouteContext routeContext) {
        Collection<String> dropTables = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> otherRuleActualTables = shardingRule.getShardingTables().values().stream().filter(each -> !dropTables.contains(each.getLogicTable()))
                .flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        if (otherRuleActualTables.isEmpty()) {
            return;
        }
        // TODO check actual tables not be used in multi rules, and remove this check logic
        Set<String> actualTables = routeContext.getRouteUnits().stream().flatMap(each -> each.getTableMappers().stream().map(RouteMapper::getActualName)).collect(Collectors.toSet());
        Collection<String> inUsedTables = actualTables.stream().filter(otherRuleActualTables::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(inUsedTables, () -> new InUsedTablesException(inUsedTables));
    }
}
