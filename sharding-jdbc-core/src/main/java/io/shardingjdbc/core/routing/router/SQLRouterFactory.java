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

package io.shardingjdbc.core.routing.router;

import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SQL router factory.
 * 
 * @author zhangiang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLRouterFactory {
    
    /**
     * Create SQL router.
     * 
     * @param shardingContext Sharding runtime context
     * @return SQL router instance
     */
    public static SQLRouter createSQLRouter(final ShardingContext shardingContext) {
        return HintManagerHolder.isDatabaseShardingOnly() ? new DatabaseHintSQLRouter(shardingContext) : new ParsingSQLRouter(shardingContext);
    }
}
