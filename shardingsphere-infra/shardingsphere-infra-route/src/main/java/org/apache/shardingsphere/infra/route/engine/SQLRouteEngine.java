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

package org.apache.shardingsphere.infra.route.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.impl.AllSQLRouteExecutor;
import org.apache.shardingsphere.infra.route.engine.impl.PartialSQLRouteExecutor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * SQL route engine.
 */
@RequiredArgsConstructor
public final class SQLRouteEngine {
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    /**
     * Route SQL.
     *
     * @param logicSQL logic SQL
     * @param metaData ShardingSphere meta data
     * @return route context
     */
    public RouteContext route(final LogicSQL logicSQL, final ShardingSphereMetaData metaData) {
        Optional<RouteContext> routeContext = routeByHint(logicSQL.getSqlStatementContext());
        if (routeContext.isPresent()) {
            return routeContext.get();
        }
        SQLRouteExecutor executor = isNeedAllSchemas(logicSQL.getSqlStatementContext().getSqlStatement()) ? new AllSQLRouteExecutor() : new PartialSQLRouteExecutor(rules, props);
        return executor.route(logicSQL, metaData);
    }
    
    private Optional<RouteContext> routeByHint(final SQLStatementContext<?> sqlStatementContext) {
        RouteContext result = new RouteContext();
        if (HintManager.isInstantiated() && HintManager.getDatasourceName().isPresent()) {
            result.getRouteUnits().addAll(Collections.singletonList(new RouteUnit(new RouteMapper(HintManager.getDatasourceName().get(), HintManager.getDatasourceName().get()),
                    Collections.emptyList())));
        } else {
            ((CommonSQLStatementContext<?>) sqlStatementContext).extractHintDataSourceName().ifPresent(datasourceName ->
                    result.getRouteUnits().addAll(Collections.singletonList(new RouteUnit(new RouteMapper(datasourceName, datasourceName), Collections.emptyList()))));
        }
        return result.getRouteUnits().size() > 0 ? Optional.of(result) : Optional.empty();
    }
    
    // TODO use dynamic config to judge UnconfiguredSchema
    private boolean isNeedAllSchemas(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLShowTablesStatement || sqlStatement instanceof MySQLShowTableStatusStatement;
    }
}
