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

package org.apache.shardingsphere.infra.route.fixture.router;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.fixture.rule.DataSourceRouteRuleFixture;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.route.lifecycle.EntranceSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.util.Collection;
import java.util.Collections;

public final class DataSourceSQLRouterFixture implements EntranceSQLRouter<DataSourceRouteRuleFixture>, DecorateSQLRouter<DataSourceRouteRuleFixture> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final DataSourceRouteRuleFixture rule,
                                           final Collection<String> tableNames, final ConfigurationProperties props) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.emptyList()));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database, final DataSourceRouteRuleFixture rule,
                                     final Collection<String> tableNames, final ConfigurationProperties props) {
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.emptyList()));
    }
    
    @Override
    public Type getType() {
        return Type.DATA_SOURCE;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public Class<DataSourceRouteRuleFixture> getTypeClass() {
        return DataSourceRouteRuleFixture.class;
    }
}
