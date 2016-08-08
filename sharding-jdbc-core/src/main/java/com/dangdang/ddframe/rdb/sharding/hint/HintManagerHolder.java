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

package com.dangdang.ddframe.rdb.sharding.hint;

import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 线索分片管理器的本地线程持有者.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManagerHolder {
    
    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置线索分片管理器.
     *
     * @param hintManager 线索分片管理器
     */
    public static void setHintManager(final HintManager hintManager) {
        Preconditions.checkState(null == HINT_MANAGER_HOLDER.get(), "HintManagerHolder has previous value, please clear first.");
        HINT_MANAGER_HOLDER.set(hintManager);
    }
    
    /**
     * 判断当前线程是否使用线索分片.
     * @return 当前线程是否使用线索分片
     */
    public static boolean isUseShardingHint() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().isShardingHint();
    }
    
    /**
     * 获取分库分片键值.
     * 
     * @param shardingKey 分片键
     * @return 分库分片键值
     */
    public static Optional<ShardingValue<?>> getDatabaseShardingValue(final ShardingKey shardingKey) {
        return isUseShardingHint() ? Optional.<ShardingValue<?>>fromNullable(HINT_MANAGER_HOLDER.get().getDatabaseShardingValue(shardingKey)) : Optional.<ShardingValue<?>>absent();
    }
    
    /**
     * 获取分表分片键值.
     *
     * @param shardingKey 分片键
     * @return 分表分片键值
     */
    public static Optional<ShardingValue<?>> getTableShardingValue(final ShardingKey shardingKey) {
        return isUseShardingHint() ? Optional.<ShardingValue<?>>fromNullable(HINT_MANAGER_HOLDER.get().getTableShardingValue(shardingKey)) : Optional.<ShardingValue<?>>absent();
    }
    
    /**
     * 判断是否数据库操作只路由至主库.
     * 
     * @return 是否数据库操作只路由至主库
     */
    public static boolean isMasterRouteOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().isMasterRouteOnly();
    }
    
    /**
     * 清理线索分片管理器的本地线程持有者.
     */
    public static void clear() {
        HINT_MANAGER_HOLDER.remove();
    }
}
