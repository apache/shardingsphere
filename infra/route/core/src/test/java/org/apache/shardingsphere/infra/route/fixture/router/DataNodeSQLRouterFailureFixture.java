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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteFailureRuleFixture;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.util.Collection;

public final class DataNodeSQLRouterFailureFixture implements DecorateSQLRouter<RouteFailureRuleFixture> {
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final RouteFailureRuleFixture rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        throw new UnsupportedOperationException("Route failure.");
    }
    
    @Override
    public Type getType() {
        return Type.DATA_NODE;
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<RouteFailureRuleFixture> getTypeClass() {
        return RouteFailureRuleFixture.class;
    }
}
