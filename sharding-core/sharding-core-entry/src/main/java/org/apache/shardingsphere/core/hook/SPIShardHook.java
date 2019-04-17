/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.hook;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;

import java.util.Collection;

/**
 * Shard hook for SPI.
 *
 * @author zhaojun
 */
public final class SPIShardHook implements ShardHook {
    
    private final Collection<ShardHook> shardHooks = NewInstanceServiceLoader.newServiceInstances(ShardHook.class);
    
    static {
        NewInstanceServiceLoader.register(ShardHook.class);
    }
    
    @Override
    public void start(final String sql) {
        for (ShardHook each : shardHooks) {
            each.start(sql);
        }
    }
    
    @Override
    public void finishSuccess(final SQLRouteResult sqlRouteResult, final ShardingTableMetaData shardingTableMetaData) {
        for (ShardHook each : shardHooks) {
            each.finishSuccess(sqlRouteResult, shardingTableMetaData);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (ShardHook each : shardHooks) {
            each.finishFailure(cause);
        }
    }
}
