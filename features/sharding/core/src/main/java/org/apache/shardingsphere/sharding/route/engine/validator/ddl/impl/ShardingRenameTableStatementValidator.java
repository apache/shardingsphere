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
import org.apache.shardingsphere.infra.binder.context.statement.ddl.RenameTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;

import java.util.List;

/**
 * Sharding rename table statement validator.
 */
public final class ShardingRenameTableStatementValidator implements ShardingStatementValidator {
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        RenameTableStatement renameTableStatement = (RenameTableStatement) sqlStatementContext.getSqlStatement();
        for (RenameTableDefinitionSegment each : renameTableStatement.getRenameTables()) {
            String primaryTable = each.getTable().getTableName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!ShardingSupportedCheckUtils.isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, primaryTable),
                    () -> new ShardingDDLRouteException("RENAME", "TABLE", ((RenameTableStatementContext) sqlStatementContext).getTablesContext().getTableNames()));
        }
    }
}
