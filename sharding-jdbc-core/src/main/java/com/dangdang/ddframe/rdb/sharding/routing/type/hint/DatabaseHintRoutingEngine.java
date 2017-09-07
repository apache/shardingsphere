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

package com.dangdang.ddframe.rdb.sharding.routing.type.hint;

import com.dangdang.ddframe.rdb.sharding.api.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ListShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.SingleShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.SingleKeyShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Database hint only routing engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class DatabaseHintRoutingEngine implements RoutingEngine {
    
    private final DataSourceRule dataSourceRule;
    
    private final DatabaseShardingStrategy databaseShardingStrategy;
    
    @Override
    public RoutingResult route() {
        Optional<ShardingValue> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(HintManagerHolder.DB_TABLE_NAME, HintManagerHolder.DB_COLUMN_NAME));
        Preconditions.checkState(shardingValue.isPresent());
        log.debug("Before database sharding only db:{} sharding values: {}", dataSourceRule.getDataSourceNames(), shardingValue.get());
        Collection<String> routingDataSources;
        if (isAccurateSharding(shardingValue.get(), databaseShardingStrategy)) {
            routingDataSources = new HashSet<>();
            List<SingleShardingValue> singleShardingValues = transferToShardingValues((ListShardingValue<?>) shardingValue.get());
            for (SingleShardingValue each : singleShardingValues) {
                routingDataSources.add(databaseShardingStrategy.doAccurateSharding(dataSourceRule.getDataSourceNames(), each));
            }
        } else {
            routingDataSources = databaseShardingStrategy.doStaticSharding(dataSourceRule.getDataSourceNames(), Collections.singleton(shardingValue.get()));
        }
        Preconditions.checkState(!routingDataSources.isEmpty(), "no database route info");
        log.debug("After database sharding only result: {}", routingDataSources);
        RoutingResult result = new RoutingResult();
        for (String each : routingDataSources) {
            result.getTableUnits().getTableUnits().add(new TableUnit(each, "", ""));
        }
        return result;
    }
    
    private boolean isAccurateSharding(final ShardingValue shardingValue, final ShardingStrategy shardingStrategy) {
        return shardingStrategy.getShardingAlgorithm() instanceof SingleKeyShardingAlgorithm && !(shardingValue instanceof RangeShardingValue);
    }
    
    @SuppressWarnings("unchecked")
    private List<SingleShardingValue> transferToShardingValues(final ListShardingValue<?> shardingValue) {
        List<SingleShardingValue> result = new ArrayList<>(shardingValue.getValues().size());
        for (Comparable<?> each : shardingValue.getValues()) {
            result.add(new SingleShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), each));
        }
        return result;
    }
}
