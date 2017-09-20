/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.type.hint;

import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.hint.ShardingKey;
import io.shardingjdbc.core.routing.strategy.hint.HintShardingStrategy;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Database hint only routing engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class DatabaseHintRoutingEngine implements RoutingEngine {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final HintShardingStrategy databaseShardingStrategy;
    
    @Override
    public RoutingResult route() {
        Optional<ShardingValue> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(HintManagerHolder.DB_TABLE_NAME, HintManagerHolder.DB_COLUMN_NAME));
        Preconditions.checkState(shardingValue.isPresent());
        log.debug("Before database sharding only db:{} sharding values: {}", dataSourceMap.keySet(), shardingValue.get());
        Collection<String> routingDataSources;
        routingDataSources = databaseShardingStrategy.doSharding(dataSourceMap.keySet(), Collections.singletonList(shardingValue.get()));
        Preconditions.checkState(!routingDataSources.isEmpty(), "no database route info");
        log.debug("After database sharding only result: {}", routingDataSources);
        RoutingResult result = new RoutingResult();
        for (String each : routingDataSources) {
            result.getTableUnits().getTableUnits().add(new TableUnit(each, "", ""));
        }
        return result;
    }
}
