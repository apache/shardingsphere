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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.syntax.hint.DataSourceHintNotExistsException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.SQLRouter.Type;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.tableless.router.TablelessSQLRouter;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.route.lifecycle.EntranceSQLRouter;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * SQL route engine.
 */
@HighFrequencyInvocation
public final class SQLRouteEngine {
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRouter> dataNodeRouters;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRouter> dataSourceRouters;
    
    @SuppressWarnings("rawtypes")
    public SQLRouteEngine(final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) {
        this.props = props;
        Map<ShardingSphereRule, SQLRouter> routers = OrderedSPILoader.getServices(SQLRouter.class, rules);
        dataNodeRouters = filterRouters(routers, Type.DATA_NODE);
        dataSourceRouters = filterRouters(routers, Type.DATA_SOURCE);
    }
    
    @SuppressWarnings("rawtypes")
    private Map<ShardingSphereRule, SQLRouter> filterRouters(final Map<ShardingSphereRule, SQLRouter> routers, final Type type) {
        Map<ShardingSphereRule, SQLRouter> result = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, SQLRouter> entry : routers.entrySet()) {
            if (type == entry.getValue().getType()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Route.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @param database sharding sphere database
     * @return route context
     */
    public RouteContext route(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database) {
        RouteContext result = new RouteContext();
        Optional<String> dataSourceName = findDataSourceByHint(queryContext.getHintValueContext(), database.getResourceMetaData().getStorageUnits());
        if (dataSourceName.isPresent()) {
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName.get(), dataSourceName.get()), Collections.emptyList()));
            return result;
        }
        Collection<String> tableNames = SQLStatementContextExtractor.getTableNames(database, queryContext.getSqlStatementContext());
        result = route(queryContext, globalRuleMetaData, database, dataNodeRouters, tableNames, result);
        result = new TablelessSQLRouter().route(queryContext, globalRuleMetaData, database, tableNames, result);
        result = route(queryContext, globalRuleMetaData, database, dataSourceRouters, tableNames, result);
        if (result.getRouteUnits().isEmpty() && 1 == database.getResourceMetaData().getStorageUnits().size()) {
            String singleDataSourceName = database.getResourceMetaData().getStorageUnits().keySet().iterator().next();
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(singleDataSourceName, singleDataSourceName), Collections.emptyList()));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private RouteContext route(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                               final Map<ShardingSphereRule, SQLRouter> routers, final Collection<String> tableNames, final RouteContext routeContext) {
        RouteContext result = routeContext;
        for (Entry<ShardingSphereRule, SQLRouter> entry : routers.entrySet()) {
            if (result.getRouteUnits().isEmpty() && entry.getValue() instanceof EntranceSQLRouter) {
                result = ((EntranceSQLRouter) entry.getValue()).createRouteContext(queryContext, globalRuleMetaData, database, entry.getKey(), tableNames, props);
            } else if (entry.getValue() instanceof DecorateSQLRouter) {
                ((DecorateSQLRouter) entry.getValue()).decorateRouteContext(result, queryContext, database, entry.getKey(), tableNames, props);
            }
        }
        return result;
    }
    
    private Optional<String> findDataSourceByHint(final HintValueContext hintValueContext, final Map<String, StorageUnit> storageUnits) {
        Optional<String> result = HintManager.isInstantiated() && HintManager.getDataSourceName().isPresent() ? HintManager.getDataSourceName() : hintValueContext.findHintDataSourceName();
        if (result.isPresent() && !storageUnits.containsKey(result.get())) {
            throw new DataSourceHintNotExistsException(result.get());
        }
        return result;
    }
}
