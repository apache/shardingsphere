/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.type.broadcast;

import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Broadcast routing engine for instance databases.
 * 
 * @author panjuan
 */
@RequiredArgsConstructor
public final class InstanceDatabaseBroadcastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        for (String each : shardingRule.getShardingDataSourceNames().getDataSourceNames()) {
            result.getTableUnits().getTableUnits().add(new TableUnit(each));
        }
        removeRedundantTableUnits(result);
        return result;
    }
    
    private void removeRedundantTableUnits(final RoutingResult routingResult) {
        Collection<TableUnit> toRemoved = new LinkedList<>();
        for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
            if (!shardingDataSourceMetaData.getAllInstanceDataSourceNames().contains(each.getDataSourceName())) {
                toRemoved.add(each);
            }
        }
        routingResult.getTableUnits().getTableUnits().removeAll(toRemoved);
    }
}
