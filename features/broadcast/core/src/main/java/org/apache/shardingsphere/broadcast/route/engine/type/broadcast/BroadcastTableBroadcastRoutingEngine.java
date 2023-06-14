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

package org.apache.shardingsphere.broadcast.route.engine.type.broadcast;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Broadcast routing engine for databases.
 */
@RequiredArgsConstructor
public final class BroadcastTableBroadcastRoutingEngine implements BroadcastRouteEngine {
    
    private final ShardingSphereDatabase database;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final Collection<String> broadcastRuleTableNames;
    
    @Override
    public RouteContext route(final RouteContext routeContext, final BroadcastRule broadcastRule) {
        Collection<String> logicTableNames = broadcastRule.getBroadcastRuleTableNames(getLogicTableNames());
        if (logicTableNames.isEmpty()) {
            routeContext.getRouteUnits().addAll(getRouteContext(broadcastRule).getRouteUnits());
        } else {
            routeContext.getRouteUnits().addAll(getRouteContext(broadcastRule, logicTableNames).getRouteUnits());
        }
        return routeContext;
    }
    
    private Collection<String> getLogicTableNames() {
        if (!broadcastRuleTableNames.isEmpty()) {
            return broadcastRuleTableNames;
        }
        return sqlStatementContext instanceof IndexAvailable
                ? getTableNames(database, sqlStatementContext.getDatabaseType(), ((IndexAvailable) sqlStatementContext).getIndexes())
                : Collections.emptyList();
    }
    
    private Collection<String> getTableNames(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<IndexSegment> indexes) {
        Collection<String> result = new LinkedList<>();
        for (QualifiedTable each : IndexMetaDataUtils.getTableNames(database, databaseType, indexes)) {
            result.add(each.getTableName());
        }
        return result;
    }
    
    private RouteContext getRouteContext(final BroadcastRule broadcastRule) {
        RouteContext result = new RouteContext();
        for (String each : broadcastRule.getAvailableDataSourceNames()) {
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.singletonList(new RouteMapper("", ""))));
        }
        return result;
    }
    
    private RouteContext getRouteContext(final BroadcastRule broadcastRule, final Collection<String> logicTableNames) {
        RouteContext result = new RouteContext();
        Collection<RouteMapper> tableRouteMappers = getTableRouteMappers(logicTableNames);
        for (String each : broadcastRule.getAvailableDataSourceNames()) {
            RouteMapper dataSourceMapper = new RouteMapper(each, each);
            result.getRouteUnits().add(new RouteUnit(dataSourceMapper, tableRouteMappers));
        }
        return result;
    }
    
    private Collection<RouteMapper> getTableRouteMappers(final Collection<String> logicTableNames) {
        Collection<RouteMapper> result = new ArrayList<>(logicTableNames.size());
        for (String logicTableName : logicTableNames) {
            result.add(new RouteMapper(logicTableName, logicTableName));
        }
        return result;
    }
}
