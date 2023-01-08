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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

/**
 * Sharding condition engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingConditionEngineFactory {
    
    /**
     * Create new instance of sharding condition engine.
     *
     * @param database database
     * @param rule sharding rule
     * @return created instance
     */
    public static ShardingConditionEngine<?> createShardingConditionEngine(final ShardingSphereDatabase database, final ShardingRule rule) {
        ShardingConditionEngine<?> result = RequiredSPIRegistry.getRegisteredService(ShardingConditionEngine.class);
        result.init(rule, database);
        return result;
    }
}
