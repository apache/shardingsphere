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

package io.shardingsphere.core.routing.router.sharding;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding router factory.
 * 
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouterFactory {
    
    /**
     * Create sharding router.
     * 
     * @param shardingRule sharding rule
     * @param shardingTableMetaData sharding table meta data
     * @param databaseType database type
     * @param showSQL show SQL or not
     * @param shardingDataSourceMetaData sharding data source meta data
     * @return sharding router instance
     */
    public static ShardingRouter createSQLRouter(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, 
                                                 final DatabaseType databaseType, final boolean showSQL, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        return HintManagerHolder.isDatabaseShardingOnly() ? new DatabaseHintSQLRouter(shardingRule, showSQL)
                : new ParsingSQLRouter(shardingRule, shardingTableMetaData, databaseType, showSQL, shardingDataSourceMetaData);
    }
}
