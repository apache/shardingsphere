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

package org.apache.shardingsphere.broadcast.route.engine.type.unicast;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Broadcast unicast route engine.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class BroadcastUnicastRouteEngine implements BroadcastRouteEngine {
    
    private final SQLStatementContext sqlStatementContext;
    
    private final Collection<String> logicTables;
    
    private final ConnectionContext connectionContext;
    
    @Override
    public RouteContext route(final BroadcastRule rule) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(createDataSourceRouteMapper(rule.getDataSourceNames()), createTableRouteMappers()));
        return result;
    }
    
    private RouteMapper createDataSourceRouteMapper(final Collection<String> dataSourceNames) {
        String dataSourceName = getDataSourceName(dataSourceNames);
        return new RouteMapper(dataSourceName, dataSourceName);
    }
    
    private String getDataSourceName(final Collection<String> dataSourceNames) {
        return isRouteToFirstDataSource() ? dataSourceNames.iterator().next() : getRandomDataSourceName(dataSourceNames);
    }
    
    private boolean isRouteToFirstDataSource() {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent() || isViewStatementContext(sqlStatementContext);
    }
    
    private boolean isViewStatementContext(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateViewStatementContext || sqlStatementContext instanceof AlterViewStatementContext
                || sqlStatementContext.getSqlStatement() instanceof DropViewStatement;
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        Collection<String> usedDataSourceNames = connectionContext.getUsedDataSourceNames();
        List<String> availableDataSourceNames = new ArrayList<>(usedDataSourceNames.isEmpty() ? dataSourceNames : usedDataSourceNames);
        return availableDataSourceNames.get(ThreadLocalRandom.current().nextInt(availableDataSourceNames.size()));
    }
    
    private Collection<RouteMapper> createTableRouteMappers() {
        if (logicTables.isEmpty()) {
            return Collections.emptyList();
        }
        List<RouteMapper> result = new ArrayList<>(logicTables.size());
        for (String each : logicTables) {
            result.add(new RouteMapper(each, each));
        }
        return result;
    }
}
