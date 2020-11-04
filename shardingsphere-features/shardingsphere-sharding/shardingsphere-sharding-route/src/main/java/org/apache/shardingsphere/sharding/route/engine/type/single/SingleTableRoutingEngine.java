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

package org.apache.shardingsphere.sharding.route.engine.type.single;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.schema.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Single table engine.
 */
@RequiredArgsConstructor
public final class SingleTableRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final TableAddressingMetaData tableAddressingMetaData;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        Optional<String> dataSourceName = sqlStatement instanceof CreateTableStatement ? getRandomDataSourceName(shardingRule.getDataSourceNames()) : findDataSourceName();
        if (!dataSourceName.isPresent()) {
            throw new ShardingSphereException("Can not route tables for `%s`, please make sure the tables are in same schema.", logicTables);
        }
        List<RouteMapper> routingTables = logicTables.stream().map(table -> new RouteMapper(table, table)).collect(Collectors.toList());
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName.get(), dataSourceName.get()), routingTables));
    }
    
    // TODO maybe enhance here, only return one data source for multiple tables for now
    private Optional<String> findDataSourceName() {
        for (String each : logicTables) {
            if (tableAddressingMetaData.getTableDataSourceNamesMapper().containsKey(each)) {
                return Optional.of(tableAddressingMetaData.getTableDataSourceNamesMapper().get(each).iterator().next());
            }
        }
        return Optional.empty();
    }
    
    private Optional<String> getRandomDataSourceName(final Collection<String> dataSourceNames) {
        String dataSourceName = Lists.newArrayList(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
        return Optional.of(dataSourceName);
    }
}
