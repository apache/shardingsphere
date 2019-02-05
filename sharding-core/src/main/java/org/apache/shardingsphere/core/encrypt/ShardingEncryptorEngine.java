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

package org.apache.shardingsphere.core.encrypt;

import org.apache.shardingsphere.core.routing.strategy.ShardingEncryptorStrategy;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sharding encryptor engine.
 *
 * @author panjuan
 */
public final class ShardingEncryptorEngine {
    
    private final Map<String, ShardingEncryptorStrategy> shardingEncryptorStrategies = new LinkedHashMap<>();
    
    public ShardingEncryptorEngine(final List<TableRule> tableRules) {
        for (TableRule each : tableRules) {
            if (null != each.getShardingEncryptorStrategy()) {
                shardingEncryptorStrategies.put(each.getLogicTable(), each.getShardingEncryptorStrategy());
            }
        }
    }
}
