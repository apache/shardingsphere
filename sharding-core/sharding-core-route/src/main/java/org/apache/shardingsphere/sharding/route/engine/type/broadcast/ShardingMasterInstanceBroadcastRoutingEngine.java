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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Sharding broadcast routing engine for master instance of databases.
 * 
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ShardingMasterInstanceBroadcastRoutingEngine implements ShardingRouteEngine {
    
    private final DataSourceMetas dataSourceMetas;
    
    @Override
    public RouteResult route(final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        for (String each : shardingRule.getShardingDataSourceNames().getDataSourceNames()) {
            if (dataSourceMetas.getAllInstanceDataSourceNames().contains(each)) {
                Optional<MasterSlaveRule> masterSlaveRule = shardingRule.findMasterSlaveRule(each);
                if (!masterSlaveRule.isPresent() || masterSlaveRule.get().getMasterDataSourceName().equals(each)) {
                    result.getRouteUnits().add(new RouteUnit(each));
                }
            }
        }
        return result;
    }
}
