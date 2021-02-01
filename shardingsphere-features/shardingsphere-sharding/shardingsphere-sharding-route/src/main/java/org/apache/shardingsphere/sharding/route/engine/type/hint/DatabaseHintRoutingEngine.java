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

package org.apache.shardingsphere.sharding.route.engine.type.hint;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.DataBaseHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * DatabaseSqlHintRoutingEngine.
 */
@RequiredArgsConstructor
public class DatabaseHintRoutingEngine implements ShardingRouteEngine {

    private final SelectStatementContext selectStatementContext;

    private final ConfigurationProperties properties;

    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        String routedDataSource = routeDataSourceByHint(shardingRule, selectStatementContext.getHintContext().getDataBaseHintSegment());
        String tableName = selectStatementContext.getAllTables().iterator().next().getTableName().getIdentifier().getValue();
        routeContext.getRouteUnits().add(new RouteUnit(
                new RouteMapper(routedDataSource, routedDataSource), Collections.singletonList(new RouteMapper(tableName, tableName))));
    }

    private String routeDataSourceByHint(final ShardingRule shardingRule, final DataBaseHintSegment dataBaseHintSegment) {
        Collection<String> datasourceAllNames = shardingRule.getDataSourceNames();
        IdentifierValue identifierValue = dataBaseHintSegment.getValue();
        Preconditions.checkState(identifierValue != null, "Datasource can not be null");
        Preconditions.checkState(datasourceAllNames.contains(identifierValue.getValue()), "There is an error datasource name");
        return identifierValue.getValue();
    }
}
