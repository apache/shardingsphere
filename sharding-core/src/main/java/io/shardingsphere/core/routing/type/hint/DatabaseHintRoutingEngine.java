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

package io.shardingsphere.core.routing.type.hint;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.routing.strategy.hint.HintShardingStrategy;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * Database hint only routing engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class DatabaseHintRoutingEngine implements RoutingEngine {
    
    private final Collection<String> dataSourceNames;
    
    private final HintShardingStrategy databaseShardingStrategy;
    
    @Override
    public RoutingResult route() {
        Optional<ShardingValue> shardingValue = HintManagerHolder.getDatabaseShardingValue(HintManagerHolder.DB_TABLE_NAME);
        Preconditions.checkState(shardingValue.isPresent());
        Collection<String> routingDataSources;
        routingDataSources = databaseShardingStrategy.doSharding(dataSourceNames, Collections.singletonList(shardingValue.get()));
        Preconditions.checkState(!routingDataSources.isEmpty(), "no database route info");
        RoutingResult result = new RoutingResult();
        for (String each : routingDataSources) {
            result.getTableUnits().getTableUnits().add(new TableUnit(each));
        }
        return result;
    }
}
