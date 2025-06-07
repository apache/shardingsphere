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

package org.apache.shardingsphere.infra.route.engine.tableless.type.unicast;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.tableless.TablelessRouteEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tableless datasource unicast route engine.
 */
@RequiredArgsConstructor
@HighFrequencyInvocation
public final class TablelessDataSourceUnicastRouteEngine implements TablelessRouteEngine {
    
    @Override
    public RouteContext route(final RuleMetaData globalRuleMetaData, final Collection<String> aggregatedDataSources) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(getDataSourceRouteMapper(aggregatedDataSources), Collections.emptyList()));
        return result;
    }
    
    private RouteMapper getDataSourceRouteMapper(final Collection<String> dataSourceNames) {
        String dataSourceName = getRandomDataSourceName(dataSourceNames);
        return new RouteMapper(dataSourceName, dataSourceName);
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return new ArrayList<>(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
    }
}
