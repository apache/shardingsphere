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

package org.apache.shardingsphere.core.route.router.sharding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

/**
 * Sharding router factory.
 * 
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouterFactory {
    
    /**
     * Create new instance of sharding router.
     * 
     * @param shardingRule sharding rule
     * @param metaData meta data of ShardingSphere
     * @param databaseType database type
     * @param sqlParseEngine parsing engine
     * @return sharding router instance
     */
    public static ShardingRouter newInstance(
            final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final DatabaseType databaseType, final SQLParseEngine sqlParseEngine) {
        return HintManager.isDatabaseShardingOnly()
                ? new DatabaseHintSQLRouter(databaseType, shardingRule) : new ParsingSQLRouter(shardingRule, metaData, sqlParseEngine);
    }
}
