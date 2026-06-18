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

package org.apache.shardingsphere.infra.route.engine.tableless.router;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.tableless.TablelessRouteEngineFactory;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.util.Collection;
import java.util.Collections;

/**
 * Tableless sql router.
 */
public final class TablelessSQLRouter {
    
    /**
     * Route.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @param database sharding sphere database
     * @param tableNames table names
     * @param routeContext route context
     * @return route context
     */
    public RouteContext route(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                              final Collection<String> tableNames, final RouteContext routeContext) {
        if (tableNames.isEmpty() && routeContext.getRouteUnits().isEmpty()) {
            Collection<String> aggregatedDataSources = getAggregatedDataSources(database);
            if (1 == aggregatedDataSources.size()) {
                return createSingleRouteContext(aggregatedDataSources.iterator().next());
            }
            return TablelessRouteEngineFactory.newInstance(queryContext, database).route(globalRuleMetaData, aggregatedDataSources);
        }
        return routeContext;
    }
    
    private RouteContext createSingleRouteContext(final String aggregatedDataSource) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(aggregatedDataSource, aggregatedDataSource), Collections.emptyList()));
        return result;
    }
    
    private Collection<String> getAggregatedDataSources(final ShardingSphereDatabase database) {
        Collection<AggregatedDataSourceRuleAttribute> attributes = database.getRuleMetaData().getAttributes(AggregatedDataSourceRuleAttribute.class);
        ShardingSpherePreconditions.checkNotEmpty(attributes, () -> new UnsupportedOperationException("Can not find aggregated data source"));
        return attributes.iterator().next().getAggregatedDataSources().keySet();
    }
}
