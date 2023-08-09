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

package org.apache.shardingsphere.infra.route.engine.impl;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintDataSourceNotExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.SQLRouteExecutor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Partial SQL route executor.
 */
public final class PartialSQLRouteExecutor implements SQLRouteExecutor {
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRouter> routers;
    
    public PartialSQLRouteExecutor(final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) {
        this.props = props;
        routers = OrderedSPILoader.getServices(SQLRouter.class, rules);
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RouteContext route(final ConnectionContext connectionContext, final QueryContext queryContext, final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database) {
        RouteContext result = new RouteContext();
        Optional<String> dataSourceName = findDataSourceByHint(queryContext.getHintValueContext(), database.getResourceMetaData().getDataSources());
        if (dataSourceName.isPresent()) {
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName.get(), dataSourceName.get()), Collections.emptyList()));
            return result;
        }
        for (Entry<ShardingSphereRule, SQLRouter> entry : routers.entrySet()) {
            if (result.getRouteUnits().isEmpty()) {
                result = entry.getValue().createRouteContext(queryContext, globalRuleMetaData, database, entry.getKey(), props, connectionContext);
            } else {
                entry.getValue().decorateRouteContext(result, queryContext, database, entry.getKey(), props, connectionContext);
            }
        }
        if (result.getRouteUnits().isEmpty() && 1 == database.getResourceMetaData().getDataSources().size()) {
            String singleDataSourceName = database.getResourceMetaData().getDataSources().keySet().iterator().next();
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(singleDataSourceName, singleDataSourceName), Collections.emptyList()));
        }
        return result;
    }
    
    private Optional<String> findDataSourceByHint(final HintValueContext hintValueContext, final Map<String, DataSource> dataSources) {
        Optional<String> result;
        if (HintManager.isInstantiated() && HintManager.getDataSourceName().isPresent()) {
            result = HintManager.getDataSourceName();
        } else {
            result = hintValueContext.findHintDataSourceName();
        }
        if (result.isPresent() && !dataSources.containsKey(result.get())) {
            throw new SQLHintDataSourceNotExistsException(result.get());
        }
        return result;
    }
}
