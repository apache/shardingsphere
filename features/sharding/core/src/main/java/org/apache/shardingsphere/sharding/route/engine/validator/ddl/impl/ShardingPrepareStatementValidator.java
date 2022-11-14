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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.connection.EmptyShardingRouteResultException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedPrepareRouteToSameDataSourceException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.PrepareStatement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding prepare statement validator.
 */
public final class ShardingPrepareStatementValidator extends ShardingDDLStatementValidator<PrepareStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<PrepareStatement> sqlStatementContext, final List<Object> params, final ShardingSphereDatabase database,
                            final ConfigurationProperties props) {
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<PrepareStatement> sqlStatementContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        if (routeContext.getRouteUnits().isEmpty()) {
            throw new EmptyShardingRouteResultException();
        }
        if (routeContext.getRouteUnits().stream().collect(Collectors.groupingBy(RouteUnit::getDataSourceMapper)).entrySet().stream().anyMatch(each -> each.getValue().size() > 1)) {
            throw new UnsupportedPrepareRouteToSameDataSourceException();
        }
    }
}
