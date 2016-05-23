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

import java.util.Properties;

/**
 * 支持分片的数据源.
 * 
 * <p>
 * 已废弃, 请使用ShardingDataSourceFactory创建数据源. 未来版本中将删除此类.
 * </p>
 * 
 * @deprecated 已废弃, 请使用ShardingDataSourceFactory创建数据源. 未来版本中将删除此类.
 * @author zhangliang
 */
@Deprecated
public class ShardingDataSource extends com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource {
    
    public ShardingDataSource(final ShardingRule shardingRule) {
        super(shardingRule);
    }
    
    public ShardingDataSource(final ShardingRule shardingRule, final Properties props) {
        super(shardingRule, props);
    }
}
