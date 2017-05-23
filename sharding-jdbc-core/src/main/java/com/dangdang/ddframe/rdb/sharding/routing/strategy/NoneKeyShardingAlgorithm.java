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

package com.dangdang.ddframe.rdb.sharding.routing.strategy;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;

import java.util.Collection;

/**
 * 无分片键算法接口.
 * 
 * @param <T> 无分片键传入的数值类型
 * @author gaohongtao
 */
public interface NoneKeyShardingAlgorithm<T extends Comparable<?>> extends ShardingAlgorithm {
    
    /**
     * 没有分片键的情况下,调用该方法.
     * 
     * <p>shardingValue来源于非SQL形式的传入</p>
     *
     * @param availableTargetNames 所有的可用目标名称集合, 一般是数据源或表名称
     * @param shardingValue 分片值
     * @return 分片后指向的目标名称, 一般是数据源或表名称
     */
    String doSharding(Collection<String> availableTargetNames, ShardingValue<T> shardingValue);
}
