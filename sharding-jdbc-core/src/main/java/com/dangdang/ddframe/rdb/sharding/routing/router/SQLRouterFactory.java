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

package com.dangdang.ddframe.rdb.sharding.routing.router;

import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 路由引擎工厂.
 * 
 * @author zhangiang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLRouterFactory {
    
    /**
     * 创建SQL路由器.
     * 
     * @param shardingContext 数据源运行期上下文
     * @return SQL路由器
     */
    public static SQLRouter createSQLRouter(final ShardingContext shardingContext) {
        return HintManagerHolder.isDatabaseShardingOnly() ? new DatabaseHintSQLRouter(shardingContext) : new ParsingSQLRouter(shardingContext);
    }
}
