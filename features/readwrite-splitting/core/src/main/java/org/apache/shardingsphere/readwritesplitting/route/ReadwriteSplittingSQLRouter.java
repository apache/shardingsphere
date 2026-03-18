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

package org.apache.shardingsphere.readwritesplitting.route;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Readwrite-splitting SQL router.
 */
@HighFrequencyInvocation
public final class ReadwriteSplittingSQLRouter implements DecorateSQLRouter<ReadwriteSplittingRule> {
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final ReadwriteSplittingRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String logicDataSourceName = each.getDataSourceMapper().getActualName();
            rule.findDataSourceGroupRule(logicDataSourceName).ifPresent(optional -> {
                toBeRemoved.add(each);
                String actualDataSourceName =
                        new ReadwriteSplittingDataSourceRouter(optional, queryContext.getConnectionContext()).route(queryContext.getSqlStatementContext(), queryContext.getHintValueContext());
                toBeAdded.add(new RouteUnit(new RouteMapper(logicDataSourceName, actualDataSourceName), each.getTableMappers()));
            });
        }
        routeContext.getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    @Override
    public Type getType() {
        return Type.DATA_SOURCE;
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getTypeClass() {
        return ReadwriteSplittingRule.class;
    }
}
