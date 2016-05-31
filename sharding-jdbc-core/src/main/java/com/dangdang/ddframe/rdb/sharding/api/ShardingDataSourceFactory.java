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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 分片数据源工厂.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingDataSourceFactory {
    
    /**
     * 创建分片数据源.
     * 
     * @param shardingRule 分片规则
     * @return 分片数据源
     */
    public static DataSource createDataSource(final ShardingRule shardingRule) {
        return new ShardingDataSource(shardingRule);
    }
    
    /**
     * 创建分片数据源.
     * 
     * @param shardingRule 分片规则
     * @param props 属性配置
     * @return 分片数据源
     */
    public static DataSource createDataSource(final ShardingRule shardingRule, final Properties props) {
        return new ShardingDataSource(shardingRule, props);
    }
}
