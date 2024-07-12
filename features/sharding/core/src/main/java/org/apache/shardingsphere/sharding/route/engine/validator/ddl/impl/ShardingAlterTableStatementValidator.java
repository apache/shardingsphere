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
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding alter table statement validator.
 */
public final class ShardingAlterTableStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                            final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        AlterTableStatementContext alterTableStatementContext = (AlterTableStatementContext) sqlStatementContext;
        Collection<String> tableNames = alterTableStatementContext.getTablesContext().getSimpleTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        Optional<SimpleTableSegment> renameTable = ((AlterTableStatement) sqlStatementContext.getSqlStatement()).getRenameTable();
        ShardingSpherePreconditions.checkState(!renameTable.isPresent() || !shardingRule.containsShardingTable(tableNames),
                () -> new UnsupportedShardingOperationException("ALTER TABLE ... RENAME TO ...", renameTable.map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("")));
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        AlterTableStatementContext alterTableStatementContext = (AlterTableStatementContext) sqlStatementContext;
        String primaryTable = alterTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(!isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, primaryTable),
                () -> new ShardingDDLRouteException("ALTER", "TABLE", alterTableStatementContext.getTablesContext().getTableNames()));
    }
}
