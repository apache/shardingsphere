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

import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;

/**
 * Sharding alter table route context checker.
 */
public final class ShardingAlterTableRouteContextChecker implements ShardingRouteContextChecker {
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        CommonSQLStatementContext alterTableStatementContext = (CommonSQLStatementContext) queryContext.getSqlStatementContext();
        String primaryTable = ((AlterTableStatement) alterTableStatementContext.getSqlStatement()).getTable().getTableName().getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(!ShardingSupportedCheckUtils.isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, primaryTable),
                () -> new ShardingDDLRouteException("ALTER", "TABLE", alterTableStatementContext.getTablesContext().getTableNames()));
    }
}
